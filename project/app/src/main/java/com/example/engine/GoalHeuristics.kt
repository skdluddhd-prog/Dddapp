package com.example.engine

import android.webkit.WebView

enum class GoalType {
    RUN_CODE,
    LOGIN,
    CUSTOM
}

object GoalHeuristics {

    fun getKeywordsForGoal(goal: GoalType): List<String> {
        return when (goal) {
            GoalType.RUN_CODE -> listOf("run", "execute", "compile", "preview", "submit", "send", "রান", "পাঠান")
            GoalType.LOGIN -> listOf("sign in", "log in", "login", "continue", "submit")
            GoalType.CUSTOM -> listOf("send", "submit", "generate", "apply", "ok", "next")
        }
    }

    fun buildScanClickAndScrollScript(keywords: List<String>): String {
        val jsArray = keywords.joinToString(",") { "'${it.lowercase()}'" }
        return """
            (function() {
                var keys = [$jsArray];
                var candidates = document.querySelectorAll('button, a, [role="button"], input[type="submit"], input[type="button"]');
                for (var i = 0; i < candidates.length; i++) {
                    var el = candidates[i];
                    var txt = (el.innerText || el.value || el.getAttribute('aria-label') || el.title || '').toLowerCase().trim();
                    for (var k = 0; k < keys.length; k++) {
                        if (txt.indexOf(keys[k]) !== -1 && !el.disabled) {
                            el.scrollIntoView({ behavior: 'smooth', block: 'center' });
                            el.click();
                            return 'CLICKED:' + txt;
                        }
                    }
                }
                window.scrollBy({ top: 320, behavior: 'smooth' });
                return 'SCROLLED_SEARCHING';
            })();
        """.trimIndent()
    }

    fun executeGoalAction(
        webView: WebView,
        goal: GoalType = GoalType.RUN_CODE,
        onResult: (String) -> Unit = {}
    ) {
        val keywords = getKeywordsForGoal(goal)
        val script = buildScanClickAndScrollScript(keywords)
        webView.post {
            webView.evaluateJavascript(script) { raw ->
                onResult(raw?.trim('"').orEmpty())
            }
        }
    }
}
