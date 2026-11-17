package com.example.bridge

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.engine.AiChromiumDevEngine
import com.example.engine.ChromiumScript
import com.example.service.AutoTouchService
import com.example.service.ChromiumBackgroundService
import java.io.File
import java.io.FileWriter

class GodBridge(
    private val context: Context,
    private val webViewProvider: () -> WebView?,
    private val onStartVoice: (String) -> Unit = {},
    private val onRouteToCompiler: (String) -> Unit = {}
) {
    private fun getTempStreamFile(): File = File(context.cacheDir, "godbridge_stream.js")

    private fun notifyJs(script: String) {
        val wv = webViewProvider() ?: return
        wv.post {
            wv.evaluateJavascript(script, null)
        }
    }

    @JavascriptInterface
    fun startBackgroundWorker() {
        try {
            val intent = Intent(context, ChromiumBackgroundService::class.java)
            context.startService(intent)
            notifyJs("window.onBackgroundWorkerStarted && window.onBackgroundWorkerStarted(true);")
        } catch (_: Exception) {
            notifyJs("window.onBackgroundWorkerStarted && window.onBackgroundWorkerStarted(false);")
        }
    }

    @JavascriptInterface
    fun stopBackgroundWorker() {
        try {
            val intent = Intent(context, ChromiumBackgroundService::class.java)
            context.stopService(intent)
            notifyJs("window.onBackgroundWorkerStopped && window.onBackgroundWorkerStopped();")
        } catch (_: Exception) {
        }
    }

    @JavascriptInterface
    fun saveToTempFile(content: String): String {
        return try {
            val file = getTempStreamFile()
            FileWriter(file, false).use { it.write(content) }
            notifyJs("window.onTempFileSaved && window.onTempFileSaved('${file.absolutePath}', ${file.length()});")
            file.absolutePath
        } catch (e: Exception) {
            ""
        }
    }

    @JavascriptInterface
    fun readTempFileChunk(offset: Int, length: Int): String {
        return try {
            val file = getTempStreamFile()
            if (!file.exists()) return ""
            val text = file.readText()
            if (offset >= text.length) return ""
            val end = (offset + length).coerceAtMost(text.length)
            text.substring(offset, end)
        } catch (_: Exception) {
            ""
        }
    }

    @JavascriptInterface
    fun routeToCloudCompiler(code: String) {
        notifyJs("window.onCompilerStatus && window.onCompilerStatus('Routing to Tab B / Cloud Compiler...');")
        onRouteToCompiler(code)
        notifyJs("window.onCompilerResult && window.onCompilerResult(true, 'Dispatched to Compiler Pipeline');")
    }

    @JavascriptInterface
    fun flushRAM() {
        System.gc()
        webViewProvider()?.post {
            webViewProvider()?.clearCache(false)
        }
    }

    @JavascriptInterface
    fun startVoiceListening(lang: String) {
        onStartVoice(lang.ifBlank { "bn-IN" })
    }

    @JavascriptInterface
    fun injectAiCode(jsCode: String) {
        notifyJs(jsCode)
    }

    @JavascriptInterface
    fun addChromiumScript(
        name: String,
        description: String,
        code: String,
        autoInject: Boolean
    ): String {
        val script = ChromiumScript(
            name = name,
            description = description,
            code = code,
            autoInject = autoInject,
            isEnabled = true
        )
        AiChromiumDevEngine.addScript(context, script)
        return script.id
    }

    @JavascriptInterface
    fun installSelfFeatureZeroPressure(title: String, specOrCode: String) {
        val wv = webViewProvider()
        com.example.engine.MasterOmniControllerEngine.synthesizeAndInstallSelfFeatureZeroPressure(
            context = context,
            titleOrOrder = title,
            rawCodeOrSpec = specOrCode,
            webView = wv,
            autoRunOnEveryPage = true
        ) { installed ->
            notifyJs("window.onAiExecutionSuccess && window.onAiExecutionSuccess('Installed Self-Feature: ${installed.title.replace("'", "")}');")
        }
    }

    @JavascriptInterface
    fun isAutoTouchEnabled(): Boolean = AutoTouchService.instance != null

    @JavascriptInterface
    fun autoClick(x: Float, y: Float): Boolean {
        return AutoTouchService.instance?.performClickAt(x, y) ?: false
    }

    @JavascriptInterface
    fun autoSwipe(x1: Float, y1: Float, x2: Float, y2: Float): Boolean {
        return AutoTouchService.instance?.performSwipe(x1, y1, x2, y2) ?: false
    }

    @JavascriptInterface
    fun autoScroll(down: Boolean): Boolean {
        return AutoTouchService.instance?.performScroll(down) ?: false
    }

    @JavascriptInterface
    fun toast(message: String) {
        val wv = webViewProvider()
        if (wv != null) {
            wv.post { Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
        }
    }

    @JavascriptInterface
    fun vibrate(ms: Long) {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(ms.coerceIn(10L, 1000L), VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(ms.coerceIn(10L, 1000L))
            }
        } catch (_: Exception) {
        }
    }

    /**
     * Full-Body Native Bridge: Allows any dynamically generated feature, slot, or website
     * to control the entire Android app's UI, buttons, 3D styles, background shaders, and features.
     */
    @JavascriptInterface
    fun executeOmniCommand(commandText: String): String {
        val wv = webViewProvider()
        var resultSummary = "Executed"
        wv?.post {
            val res = com.example.engine.UniversalOmniControlEngine.processNaturalLanguageOrJsonCommand(
                context = context,
                rawText = commandText
            )
            resultSummary = res ?: "Command dispatched: $commandText"
            Toast.makeText(context, "⚡ [SovereignOS Bridge]: $commandText", Toast.LENGTH_SHORT).show()
        }
        return resultSummary
    }

    @JavascriptInterface
    fun createNativeButton(label: String, zone: String, is3D: Boolean, actionPayload: String): String {
        val newId = "bridge_btn_" + java.util.UUID.randomUUID().toString().take(6)
        val wv = webViewProvider()
        wv?.post {
            com.example.engine.UniversalOmniControlEngine.autoRegisterFeature(
                context = context,
                id = newId,
                label = label.ifBlank { "⚡ কাস্টম অ্যাকশন" },
                defaultZone = zone.ifBlank { "TOP_ROW_2" },
                bgColorHex = "#E0F2FE",
                accentColorHex = "#0284C7",
                isCustomAiFeature = true,
                customActionPayload = actionPayload.ifBlank { label },
                scaleFactor = 1.08f,
                is3DStyle = is3D
            )
            Toast.makeText(context, "➕ নতুন নেটিভ বাটন যুক্ত হয়েছে: $label", Toast.LENGTH_SHORT).show()
        }
        return newId
    }

    @JavascriptInterface
    fun triggerScreenTouch(xFraction: Float, yFraction: Float, actionType: String, selector: String) {
        val wv = webViewProvider()
        wv?.post {
            com.example.engine.UniversalOmniControlEngine.triggerAutonomousScreenTouch(
                context = context,
                xFraction = xFraction,
                yFraction = yFraction,
                actionType = actionType.ifBlank { "TAP" },
                targetLabel = "Bridge Touch ($actionType)",
                targetSelector = selector
            )
        }
    }

    @JavascriptInterface
    fun controlParticles(physicsMode: String, speedMultiplier: Float, count: Int, colorHex: String) {
        val wv = webViewProvider()
        wv?.post {
            com.example.engine.ParticleSwarmManager.updateCanvasPhysicsState(
                context = context,
                physicsMode = physicsMode.ifBlank { null },
                speedMultiplier = if (speedMultiplier > 0f) speedMultiplier else null,
                particleCount = if (count > 0) count else null,
                colorHex = colorHex.ifBlank { null }
            )
        }
    }

    @JavascriptInterface
    fun saveSharedState(key: String, value: String) {
        try {
            context.getSharedPreferences("sovereign_whole_sky_shared_state", Context.MODE_PRIVATE)
                .edit()
                .putString(key, value)
                .apply()
        } catch (_: Exception) {}
    }

    @JavascriptInterface
    fun readSharedState(key: String, defaultValue: String): String {
        return try {
            context.getSharedPreferences("sovereign_whole_sky_shared_state", Context.MODE_PRIVATE)
                .getString(key, defaultValue) ?: defaultValue
        } catch (_: Exception) {
            defaultValue
        }
    }

    @JavascriptInterface
    fun startGeminiLive(minimized: Boolean) {
        val wv = webViewProvider()
        wv?.post {
            com.example.engine.SovereignGeminiLiveOmniEngine.startLiveSession(
                context = context,
                startMinimized = minimized,
                rootViewProvider = { wv.rootView },
                activeWebViewProvider = { wv },
                onEnsureVoiceMicListening = {}
            )
        }
    }

    @JavascriptInterface
    fun stopGeminiLive() {
        val wv = webViewProvider()
        wv?.post {
            com.example.engine.SovereignGeminiLiveOmniEngine.stopLiveSession()
        }
    }
}

