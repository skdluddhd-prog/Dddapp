package com.example.engine

import android.content.Context
import android.webkit.WebView
import com.example.BlueprintMemory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object GodModeAutoSync {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private fun extractSignatures(code: String): List<String> {
        val regex = Regex("(?:function|class|const|let|var|fun)\\s+([a-zA-Z0-9_]+)")
        return regex.findAll(code).map { it.groupValues[1] }.distinct().take(15).toList()
    }

    private fun unescapeJsString(raw: String?): String {
        if (raw.isNullOrBlank() || raw == "null") return ""
        return raw.removeSurrounding("\"")
            .replace("\\n", "\n")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
    }

    fun scanAndSyncManualFix(
        context: Context,
        webViewB: WebView,
        taskEngine: AutonomousTaskEngine,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        webViewB.evaluateJavascript(
            "(function(){ var el = document.querySelector('textarea, pre, code, [contenteditable=\"true\"]'); return el ? (el.value || el.innerText) : document.body.innerText.slice(0, 3000); })();"
        ) { raw ->
            val code = unescapeJsString(raw)
            scope.launch {
                val signatures = extractSignatures(code)
                val summaryNote = "Verified Signatures: ${signatures.joinToString(", ")}"
                BlueprintMemory.saveBlueprintContext(context, summaryNote)
                taskEngine.markCurrentTaskManualSuccess(signatures)
                System.gc()
                onComplete(true, summaryNote)
            }
        }
    }
}
