package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Base64
import android.webkit.WebView
import com.example.model.RuleType
import com.example.model.WorkflowNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

object WorkerEngine {

    private val CODE_BLOCK_PATTERN = Pattern.compile("```(?:[a-zA-Z0-9_+\\-]+)?\\s*([\\s\\S]*?)```")
    private val memoryRegistry = ConcurrentHashMap<String, Any>()

    fun storePointer(data: Any): String {
        val pointerId = "ptr_${UUID.randomUUID().toString().take(8)}"
        memoryRegistry[pointerId] = data
        return pointerId
    }

    fun resolvePointer(pointerId: String): Any? = memoryRegistry[pointerId]

    fun releasePointer(pointerId: String) {
        memoryRegistry.remove(pointerId)
    }

    fun clearRegistry() {
        memoryRegistry.clear()
    }

    fun extractCodeBlocks(rawText: String): String {
        val matcher = CODE_BLOCK_PATTERN.matcher(rawText)
        val results = mutableListOf<String>()
        while (matcher.find()) {
            val block = matcher.group(1)?.trim().orEmpty()
            if (block.isNotEmpty()) {
                results.add(block)
            }
        }
        return if (results.isNotEmpty()) {
            results.joinToString("\n\n")
        } else {
            rawText.trim()
        }
    }

    fun chainPrompt(input: String, addition: String): String {
        if (addition.isBlank()) return input
        return "$input\n\n[Nanobot Instruction / Context]: $addition"
    }

    suspend fun captureWebViewAsBase64(
        webView: WebView,
        quality: Int = 60
    ): String = withContext(Dispatchers.Main) {
        try {
            val width = webView.width.coerceAtLeast(320)
            val height = webView.height.coerceAtLeast(480)
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            val canvas = Canvas(bitmap)
            webView.draw(canvas)
            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(20, 95), out)
            bitmap.recycle()
            Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun executeNode(
        node: WorkflowNode,
        incomingData: String,
        sourceWebView: WebView?
    ): String {
        val result = when (node.rule) {
            RuleType.WEB_AUTO_MESSAGE -> {
                val promptToSend = incomingData.ifBlank {
                    node.initialMessageToSend.ifBlank { node.customParameter }
                }
                if (sourceWebView != null) {
                    NanobotSwarmOrchestrator.sendWebMessageAndHarvestReply(
                        webView = sourceWebView,
                        targetUrl = node.targetInput,
                        messageToSend = promptToSend,
                        rolePrompt = node.customRolePrompt.ifBlank { node.nanobotRole.defaultSystemPrompt }
                    )
                } else {
                    promptToSend
                }
            }
            RuleType.DEEP_THINK_ANALYZE -> {
                val roleDirective = node.customRolePrompt.ifBlank { node.nanobotRole.defaultSystemPrompt }
                val combinedPrompt = buildString {
                    appendLine("Role: ${node.customRoleName.ifBlank { node.nanobotRole.banglaTitle }}")
                    appendLine("Instruction: $roleDirective")
                    if (node.customParameter.isNotBlank()) {
                        appendLine("Extra Parameter: ${node.customParameter}")
                    }
                    appendLine("\n--- Incoming Reply / Data from Previous Nanobot ---")
                    appendLine(incomingData)
                }
                val aiRes = UniversalAiManager.executeAiPrompt(
                    prompt = combinedPrompt,
                    systemInstruction = roleDirective
                )
                aiRes.getOrElse {
                    NanobotSwarmOrchestrator.performStructuredDeepThinkFallback(
                        node = node,
                        incomingText = incomingData
                    )
                }
            }
            RuleType.EXTRACT_CODE -> extractCodeBlocks(incomingData)
            RuleType.TAKE_SCREENSHOT -> {
                if (sourceWebView != null) {
                    val b64 = captureWebViewAsBase64(sourceWebView)
                    "/* Vision Base64 Snapshot (${b64.length} chars) */\n$incomingData"
                } else {
                    incomingData
                }
            }
            RuleType.APPEND_TEXT -> chainPrompt(incomingData, node.customParameter.ifBlank { node.customRolePrompt })
            RuleType.POINTER_PASS -> {
                val ptr = storePointer(incomingData)
                node.outputPointerId = ptr
                incomingData
            }
        }

        node.lastReplyReceived = result
        if (node.autoFlushMemory) {
            System.gc()
        }
        return result
    }
}
