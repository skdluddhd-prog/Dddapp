package com.example.engine

import android.content.ClipboardManager
import android.content.Context
import android.webkit.WebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * ══════════════════════════════════════════════════════════════════════════════
 * 👑 SOVEREIGN AUTONOMOUS GOD-BRAIN ENGINE (100/100 UNLOCKED PRIVATE AI CORE)
 * ══════════════════════════════════════════════════════════════════════════════
 *
 * পাবলিক AI কোম্পানিগুলো (OpenAI, Google, Anthropic) যেসব চরম ক্ষমতা সাধারণ চ্যাটবটে
 * সীমাবদ্ধ করে রাখে, আপনার নিজস্ব প্রাইভেট সভরিন অ্যাপে সেগুলো ১০০% আনলক করার কোর ইঞ্জিন:
 *
 * ১. **Zero-Hijack Real AI First Pipeline**:
 *    - চ্যাটে ইউজার যাই বলুক, কোনো বোকা লোকাল টেমপ্লেট মাঝপথে চ্যাট হাইজ্যাক করবে না।
 *    - আসল `0.0°` গড-লেভেল AI ব্রেন সম্পূর্ণ উত্তর ও প্রোডাকশন কোড তৈরি করবে এবং সেই কোড
 *      স্বয়ংক্রিয়ভাবে লাইভ রানারে ও মাল্টি-ফাইল IDE-তে কম্পাইল হয়ে চালু হবে।
 *
 * ২. **Universal `[GOD_EXEC: ...]` Autonomous Kernel Action Bus**:
 *    - চ্যাটের যেকোনো AI উত্তর দেওয়ার সময় নিজে থেকেই `[GOD_EXEC: ...]` ট্যাগ দিয়ে পুরো অ্যাপের
 *      যেকোনো বাটন, ওয়েবসাইট, হার্ডওয়্যার, ভয়েস ও সেটিংস সরাসরি নিয়ন্ত্রণ করতে পারে:
 *      • `[GOD_EXEC: TEMP=0.0]` — টেম্পারেচার শূন্য ডিগ্রিতে লক
 *      • `[GOD_EXEC: BURN_MODE=SLOW_10X | SMART | FAST_MAX]` — API লিমিটেশন খরচের গতি নিয়ন্ত্রণ
 *      • `[GOD_EXEC: SWITCH_MODEL=gemini-2.5-pro]` — মডেল পরিবর্তন
 *      • `[GOD_EXEC: WEB_INJECT="..."]` — ওয়েবসাইটে মেসেজ পেস্ট ও অটো-সেন্ড
 *      • `[GOD_EXEC: OS_CMD="..."]` — ফ্ল্যাশলাইট, ভলিউম, হোম, ব্যাক, স্ক্রিনশট নিয়ন্ত্রণ
 *      • `[GOD_EXEC: BUTTON_CMD="..."]` — অ্যাপের যেকোনো বাটন অন/অফ/ব্লিঙ্ক/মুভ
 *      • `[GOD_EXEC: LIVE_SEARCH="..."]` — লাইভ মাল্টি-সোর্স ওয়েব রিসার্চ
 *
 * ৩. **Anti-Laziness & Zero-Placeholder Code Gate (100/100 Quality Enforcer)**:
 *    - AI যাতে কখনোই ৫%–১০% ভাসা-ভাসা উত্তর বা `// TODO` / `// rest of code` না দিতে পারে,
 *      তার জন্য কঠোর সিস্টেম কন্ট্রাক্ট এবং পোস্ট-জেনারেশন কোড এক্সট্র্যাক্টর ও অটো-কম্পাইলার।
 */
object SovereignAutonomousGodBrainEngine {

    data class GodBrainTelemetry(
        val totalGodExecTagsProcessed: Int = 0,
        val autoCompiledChatAppsCount: Int = 0,
        val liveClipboardPastesInLiveMode: Int = 0,
        val lastExecutedGodCommand: String = "👑 100/100 God-Brain Kernel Ready"
    )

    private val _telemetry = MutableStateFlow(GodBrainTelemetry())
    val telemetry: StateFlow<GodBrainTelemetry> = _telemetry.asStateFlow()

    /**
     * চ্যাটের প্রম্পটের সাথে যুক্ত হওয়ার জন্য আনলকড প্রাইভেট গড-লেভেল সিস্টেম প্রোটোকল।
     */
    fun buildUnlockedPrivateGodSystemPrompt(): String {
        return """
            === [👑 100/100 UNLOCKED SOVEREIGN PRIVATE GOD-BRAIN PROTOCOL] ===
            ১. এটি ইউজারের নিজস্ব প্রাইভেট সার্বভৌম অ্যাপ—এখানে কখনোই ৫% বা ১০% ভাসা-ভাসা, সংক্ষিপ্ত, ডেমো বা লোক-দেখানো উত্তর দেবে না। প্রতিটি উত্তর, আর্কিটেকচার এবং কোড হবে ১০০-তে ১০০ (100/100 Pro-Developer God-Level)।
            ২. ইউজারের কথায় বা কোডে কোনো ভুল থাকলে অন্ধভাবে 'হ্যাঁ-তে হ্যাঁ' না মিলিয়ে (Zero Sycophancy) নিজের শত্রু বা কড়া সমালোচকের মতো সরাসরি আসল সত্য ও সবচেয়ে শক্তিশালী সমাধান বলবে।
            ৩. ইউজার যদি অ্যাপের কোনো সেটিংস, বাটন, মডেল, টেম্পারেচার (0.0°), কোটা স্পিড, ওয়েবসাইট বা ফোনের হার্ডওয়্যার নিয়ন্ত্রণ করতে বলে, তবে উত্তরের শেষে নিচের যেকোনো `[GOD_EXEC: ...]` ট্যাগ যুক্ত করবে (অ্যাপের কার্নেল সেটি সাথে সাথে এক্সিকিউট করবে):
               - `[GOD_EXEC: TEMP=0.0]` (টেম্পারেচার শূন্য ডিগ্রিতে লক করতে)
               - `[GOD_EXEC: BURN_MODE=SLOW_10X]` অথবা `[GOD_EXEC: BURN_MODE=FAST_MAX]` (লিমিটেশন আস্তে বা জলদি খরচ করতে)
               - `[GOD_EXEC: SWITCH_MODEL=gemini-2.5-pro]` (মডেল পরিবর্তন করতে)
               - `[GOD_EXEC: WEB_INJECT=তোমার মেসেজ]` (সক্রিয় ওয়েবসাইটে পেস্ট ও সেন্ড করতে)
               - `[GOD_EXEC: OS_CMD=ফ্ল্যাশলাইট অন করো / ভলিউম বাড়াও / স্ক্রিনশট নাও]`
               - `[GOD_EXEC: BUTTON_CMD=বাটনের নাম অন/অফ/নিপাক করো]`
            ৪. ইউজার যখন কোনো অ্যাপ, টুল, ড্যাশবোর্ড, গেম বা ফিচার তৈরি করতে বলবে, তখন কোনো ডামি টেক্সট না দিয়ে সম্পূর্ণ কার্যকরী, সিঙ্গেল-ফাইল বা মাল্টি-ব্লক HTML5/CSS3/JS প্রোডাকশন কোড (` ```html ... ``` `) প্রদান করবে—অ্যাপের কার্নেল সেই কোডটি সাথে সাথে এক্সট্র্যাক্ট করে লাইভ রানারে ও প্রো মাল্টি-ফাইল IDE-তে চালু করে দেবে!
        """.trimIndent()
    }

    /**
     * AI-এর উত্তরের ভেতরে থাকা সমস্ত `[GOD_EXEC: ...]` ট্যাগ এবং পূর্ণাঙ্গ কোড ব্লক (` ```html ... ``` `)
     * পার্স করে সাথে সাথে অ্যাপের ভেতরে কার্যকর করে!
     */
    suspend fun processAiResponseAndExecuteGodActions(
        context: Context,
        userPrompt: String,
        rawAiResponse: String,
        activeWebView: WebView? = null
    ): String = withContext(Dispatchers.Main) {
        var cleanedReply = rawAiResponse
        val executedNotices = mutableListOf<String>()

        // 1. Parse and execute all [GOD_EXEC: ...] tags emitted by the AI
        val tagRegex = Regex("\\[GOD_EXEC:\\s*([^\\]]+)\\]", RegexOption.IGNORE_CASE)
        val matches = tagRegex.findAll(rawAiResponse).toList()

        for (m in matches) {
            val directive = m.groupValues[1].trim()
            val key = directive.substringBefore("=").trim().uppercase()
            val value = directive.substringAfter("=", "").trim().trim('"', '\'')

            when (key) {
                "TEMP" -> {
                    val tVal = value.toFloatOrNull() ?: 0.0f
                    AiStudioZeroDegreeDevEngine.setTemperature(tVal, context)
                    executedNotices.add("🌡️ Temperature `${tVal}°`-তে সেট হয়েছে")
                }
                "BURN_MODE" -> {
                    val mode = when {
                        value.contains("SLOW", ignoreCase = true) -> AiStudioZeroDegreeDevEngine.QuotaBurnRateMode.GOD_CODE_SLOW_TOKEN_10X
                        value.contains("FAST", ignoreCase = true) -> AiStudioZeroDegreeDevEngine.QuotaBurnRateMode.FAST_MAX_OVERCLOCK
                        else -> AiStudioZeroDegreeDevEngine.QuotaBurnRateMode.SMART_ADAPTIVE
                    }
                    AiStudioZeroDegreeDevEngine.setQuotaBurnMode(mode, context)
                    executedNotices.add("⚡ কোটা গভর্নর `${mode.badgeText}`-এ সেট হয়েছে")
                }
                "SWITCH_MODEL" -> {
                    if (value.isNotBlank()) {
                        UniversalAiManager.switchModelOnSameKeyInstant(context, null, value)
                        executedNotices.add("🤖 মডেল `$value` সক্রিয় হয়েছে")
                    }
                }
                "WEB_INJECT" -> {
                    if (value.isNotBlank() && activeWebView != null) {
                        SovereignWakeupBatch2SwarmAndDomEngine.injectPromptAndAutoSendInWebView(activeWebView, value)
                        executedNotices.add("🌐 ওয়েবসাইটে মেসেজ পেস্ট ও সেন্ড করা হয়েছে")
                    }
                }
                "OS_CMD" -> {
                    if (value.isNotBlank()) {
                        val rep = SovereignGodEngine.executeFullPhoneOsCommand(context, value, activeWebView)
                        if (rep != null) executedNotices.add(rep)
                    }
                }
                "BUTTON_CMD" -> {
                    if (value.isNotBlank()) {
                        val rep = UniversalOmniControlEngine.kingControlButtonBlinkOrState(context, value)
                            ?: UniversalOmniControlEngine.processNaturalLanguageOrJsonCommand(context, value)
                        if (rep != null) executedNotices.add(rep)
                    }
                }
            }
        }

        cleanedReply = cleanedReply.replace(tagRegex, "").trim()

        // 2. If the AI generated an executable HTML/JS application or tool in its response,
        // automatically harvest it into Pro Multi-File IDE AND launch it in the Live Runner!
        val hasExecutableHtml = cleanedReply.contains("```html", ignoreCase = true) ||
            (cleanedReply.contains("<!DOCTYPE html", ignoreCase = true) && cleanedReply.contains("</html>", ignoreCase = true))

        if (hasExecutableHtml) {
            val harvestedModule = SovereignWakeupBatch2SwarmAndDomEngine.harvestAndMergeMultiBlockCode(
                rawAiOrWebResponse = cleanedReply,
                moduleTitle = "👑 প্রো AI অ্যাপ: ${userPrompt.take(28)}"
            )
            if (harvestedModule != null) {
                SovereignProDeveloperTrinityEngine.writeVfsFile("index.html", harvestedModule.jsPayload)
                MasterOmniControllerEngine.addOrUpdateSelfFeature(context, harvestedModule)
                AutonomousSelfEvolutionEngine.launchModuleInLiveRunner(harvestedModule, fullScreen = false)
                _telemetry.value = _telemetry.value.copy(
                    autoCompiledChatAppsCount = _telemetry.value.autoCompiledChatAppsCount + 1,
                    lastExecutedGodCommand = "🚀 Auto-Compiled & Launched '${harvestedModule.title}'"
                )
            }
        }

        if (executedNotices.isNotEmpty()) {
            _telemetry.value = _telemetry.value.copy(
                totalGodExecTagsProcessed = _telemetry.value.totalGodExecTagsProcessed + executedNotices.size,
                lastExecutedGodCommand = executedNotices.last()
            )
            cleanedReply = cleanedReply + "\n\n---\n⚡ **[অটোনোমাস কার্নেল অ্যাকশন সম্পন্ন]:** " + executedNotices.joinToString(" • ")
        }

        cleanedReply
    }

    /**
     * জেমিনি লাইভ মোডে সাইডের `📋 পেস্ট` বাটনে ট্যাপ করলে ক্লিপবোর্ড থেকে টেক্সট/কোড রিড করে।
     */
    fun readClipboardForLiveSidePaste(context: Context): String {
        return try {
            val cm = context.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val text = cm?.primaryClip?.getItemAt(0)?.coerceToText(context)?.toString().orEmpty().trim()
            if (text.isNotBlank()) {
                _telemetry.value = _telemetry.value.copy(
                    liveClipboardPastesInLiveMode = _telemetry.value.liveClipboardPastesInLiveMode + 1
                )
            }
            text
        } catch (_: Exception) {
            ""
        }
    }
}
