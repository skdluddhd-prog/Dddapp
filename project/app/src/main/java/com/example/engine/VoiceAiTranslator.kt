package com.example.engine

import android.content.Context

object VoiceAiTranslator {

    fun translateVoiceToJs(command: String, language: String): String {
        val lower = command.lowercase()
        return when {
            lower.contains("স্ক্রল") || lower.contains("scroll down") || lower.contains("নিচে") ->
                "window.scrollBy({ top: 450, behavior: 'smooth' }); 'Scrolled Down';"
            lower.contains("উপরে") || lower.contains("scroll up") ->
                "window.scrollBy({ top: -450, behavior: 'smooth' }); 'Scrolled Up';"
            lower.contains("ক্লিক") || lower.contains("click") || lower.contains("সেন্ড") || lower.contains("send") ->
                GoalHeuristics.buildScanClickAndScrollScript(listOf("send", "submit", "run", "পাঠান"))
            else ->
                """
                (function() {
                    if (window.onVoiceCommand) {
                        window.onVoiceCommand(${org.json.JSONObject.quote(command)}, ${org.json.JSONObject.quote(language)});
                        return 'Routed to Voice Shell';
                    }
                    return 'Executed voice command: ' + ${org.json.JSONObject.quote(command)};
                })();
                """.trimIndent()
        }
    }

    suspend fun translateVoiceCommand(
        context: Context,
        transcript: String,
        language: String
    ): String {
        return translateVoiceToJs(transcript, language)
    }
}
