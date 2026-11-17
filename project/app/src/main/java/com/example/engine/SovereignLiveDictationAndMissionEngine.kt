package com.example.engine

import android.content.Context
import com.example.service.AutoTouchService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

enum class MissionStatus { IDLE, RUNNING, PAUSED_RESOURCE, NEEDS_USER, DONE, STOPPED, FAILED }

data class MissionState(
    val status: MissionStatus = MissionStatus.IDLE,
    val goal: String = "",
    val step: Int = 0,
    val lastNote: String = "",
    val isDictating: Boolean = false,
    val dictationPreview: String = ""
)

data class VoiceHandleResult(val handled: Boolean, val speech: String? = null)

/**
 * Live Mode upgrade:
 *  1) Dictation: "ডিক্টেশন শুরু" -> speak freely -> "পেস্ট করো" types it into the focused input of the app
 *     on screen -> "সেন্ড করো" taps that app's send button.
 *  2) Mission: "মিশন শুরু: <লক্ষ্য>" runs a read-screen -> ask AI -> act loop through the accessibility
 *     service until DONE, with checkpointing ("মিশন চালিয়ে যাও" resumes), stuck detection,
 *     a sensitive-action gate (money / delete / password) and ResourceGuard (pauses when the phone is hot,
 *     low on memory or low on battery).
 *
 * Real limits: needs the accessibility service ON, screen ON and unlocked, and the Live capsule minimised
 * so the other app is the active window. Step cap per run is MAX_STEPS; it never claims "24h guaranteed".
 */
object SovereignLiveDictationAndMissionEngine {
    private const val PREFS = "skrl2_live_mission"
    private const val MAX_STEPS = 600
    private const val STEP_DELAY_MS = 1800L
    private const val MAX_CONSECUTIVE_FAILS = 6
    private const val MAX_SAME_ACTION = 4
    private const val MAX_SAME_SCROLL = 8
    private const val RESOURCE_PAUSE_MS = 60_000L
    private const val MAX_RESOURCE_PAUSES = 10

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var missionJob: Job? = null
    private val dictation = StringBuilder()
    @Volatile private var isDictating = false

    private val _state = MutableStateFlow(MissionState())
    val state: StateFlow<MissionState> = _state.asStateFlow()

    private val missionStartRegex = Regex(
        "^(মিশন শুরু(?:\\s*করো)?|start mission|mission start)\\s*[:,\\-]?\\s*(.*)$",
        setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
    )

    private val sensitiveWords = listOf(
        "payment", "pay now", "purchase", "buy now", "delete", "factory reset", "transfer", "password",
        "পেমেন্ট", "কিনুন", "ডিলিট", "ট্রান্সফার", "পাসওয়ার্ড", "টাকা পাঠা"
    )

    private fun has(lower: String, vararg keys: String) = keys.any { lower.contains(it) }

    // ------------------------------------------------------------------ voice entry point

    fun tryHandleVoice(context: Context, spoken: String): VoiceHandleResult {
        val app = context.applicationContext
        val text = spoken.trim()
        if (text.isEmpty()) return VoiceHandleResult(false)
        val lower = text.lowercase()

        // ---- mission control
        if (has(lower, "মিশন বন্ধ", "মিশন থামাও", "stop mission")) {
            return VoiceHandleResult(true, stopMission(app))
        }
        if (has(lower, "মিশনের অবস্থা", "mission status")) {
            val s = _state.value
            return VoiceHandleResult(
                true,
                "মিশনের অবস্থা ${s.status}, ধাপ ${s.step}। ${s.lastNote}".trim()
            )
        }
        if (has(lower, "মিশন চালিয়ে যাও", "resume mission")) {
            return VoiceHandleResult(true, resumeMission(app))
        }
        missionStartRegex.find(text)?.let { m ->
            val goal = m.groupValues[2].trim()
            return if (goal.length < 6) {
                VoiceHandleResult(true, "মিশনের লক্ষ্য পরিষ্কার করে বলুন। যেমন: মিশন শুরু, সেটিংস থেকে ডার্ক মোড চালু করো।")
            } else {
                VoiceHandleResult(true, startMission(app, goal, 0, emptyList()))
            }
        }

        // ---- dictation
        if (has(lower, "ডিক্টেশন শুরু", "dictation start", "start dictation", "লেখা শুরু করো")) {
            dictation.clear()
            isDictating = true
            publishDictation()
            return VoiceHandleResult(true, "ডিক্টেশন চালু। আপনি বলুন, শেষ হলে পেস্ট করো বলবেন।")
        }
        if (!isDictating) return VoiceHandleResult(false)

        val short = lower.length <= 24
        if (has(lower, "ডিক্টেশন বন্ধ", "stop dictation")) {
            isDictating = false
            dictation.clear()
            publishDictation()
            return VoiceHandleResult(true, "ডিক্টেশন বন্ধ করা হয়েছে।")
        }
        if (short && has(lower, "মুছে ফেলো", "clear text")) {
            dictation.clear()
            publishDictation()
            return VoiceHandleResult(true, "লেখা মুছে ফেলা হয়েছে।")
        }
        if (short && has(lower, "পেস্ট করো", "পেস্ট", "লিখে দাও", "paste")) {
            return VoiceHandleResult(true, pasteDictation())
        }
        if (short && has(lower, "সেন্ড করো", "পাঠাও", "সেন্ড", "send")) {
            return VoiceHandleResult(true, sendInOtherApp())
        }
        if (dictation.isNotEmpty()) dictation.append(' ')
        dictation.append(text)
        publishDictation()
        return VoiceHandleResult(true, null)
    }

    private fun publishDictation() {
        val preview = dictation.toString().takeLast(160)
        _state.value = _state.value.copy(isDictating = isDictating, dictationPreview = preview)
    }

    private fun pasteDictation(): String {
        if (dictation.isEmpty()) return "এখনো কিছু বলা হয়নি।"
        val svc = AutoTouchService.instance
            ?: return "অ্যাক্সেসিবিলিটি সার্ভিস চালু নেই, তাই পেস্ট করতে পারছি না।"
        val ok = try { svc.typeTextIntoExternalPhoneApp(dictation.toString()) } catch (_: Throwable) { false }
        return if (ok) "পেস্ট করা হয়েছে।"
        else "ইনপুট বক্স পাইনি। অন্য অ্যাপের লেখার বক্সে একবার ট্যাপ করে আবার পেস্ট করো বলুন।"
    }

    private fun sendInOtherApp(): String {
        val svc = AutoTouchService.instance
            ?: return "অ্যাক্সেসিবিলিটি সার্ভিস চালু নেই।"
        for (label in listOf("send", "পাঠান", "পাঠাও", "submit", "সেন্ড")) {
            val hit = try { svc.findAndClickNodeByText(label, false) } catch (_: Throwable) { null }
            if (hit != null) {
                // one single tap (findAndClickNodeByText with click=true fires two taps)
                val clicked = try {
                    svc.performClickAt(hit.first.exactCenterX(), hit.first.exactCenterY(), 70L)
                } catch (_: Throwable) { false }
                if (clicked) {
                    dictation.clear()
                    publishDictation()
                    return "পাঠানো হয়েছে।"
                }
            }
        }
        return "সেন্ড বাটন পাইনি। স্ক্রিনে বাটনটা দেখা যাচ্ছে কি না দেখুন।"
    }

    // ------------------------------------------------------------------ mission

    fun startMission(app: Context, goal: String, startStep: Int, seed: List<String>): String {
        if (missionJob?.isActive == true) return "একটি মিশন ইতিমধ্যে চলছে। আগে মিশন বন্ধ করো বলুন।"
        if (AutoTouchService.instance == null) {
            return "অ্যাক্সেসিবিলিটি সার্ভিস চালু নেই। সেটিংস থেকে চালু করে আবার বলুন।"
        }
        _state.value = _state.value.copy(
            status = MissionStatus.RUNNING, goal = goal, step = startStep, lastNote = "শুরু হয়েছে"
        )
        persist(app, goal, startStep, MissionStatus.RUNNING, seed)
        missionJob = scope.launch { runLoop(app, goal, startStep, seed) }
        return "মিশন শুরু হয়েছে। অন্য অ্যাপ স্ক্রিনে খোলা রাখুন; সমস্যা হলে আমি নিজেই জানাব।"
    }

    fun resumeMission(app: Context): String {
        val p = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val goal = p.getString("goal", "").orEmpty()
        if (goal.isBlank()) return "কোনো সংরক্ষিত মিশন নেই।"
        val step = p.getInt("step", 0)
        val hist = p.getString("history", "").orEmpty().split("\n").filter { it.isNotBlank() }
        return startMission(app, goal, step, hist)
    }

    fun stopMission(app: Context): String {
        val job = missionJob
        if (job?.isActive != true) return "এখন কোনো মিশন চলছে না।"
        job.cancel()
        val s = _state.value
        _state.value = s.copy(status = MissionStatus.STOPPED, lastNote = "আপনার নির্দেশে থামানো হয়েছে")
        persist(app, s.goal, s.step, MissionStatus.STOPPED, emptyList(), keepHistory = true)
        return "মিশন থামানো হয়েছে, ধাপ ${s.step} পর্যন্ত সংরক্ষিত। চালিয়ে যেতে মিশন চালিয়ে যাও বলুন।"
    }

    private suspend fun CoroutineScope.runLoop(
        app: Context, goal: String, startStep: Int, seed: List<String>
    ) {
        val history = ArrayDeque<String>()
        seed.forEach { history.addLast(it) }
        var step = startStep
        var fails = 0
        var sameCount = 0
        var lastSig = ""
        var resourcePauses = 0
        try {
            while (isActive) {
                if (step - startStep >= MAX_STEPS) {
                    finish(app, MissionStatus.NEEDS_USER, goal, step, history,
                        "এই দফার ধাপের সীমা $MAX_STEPS পূর্ণ হয়েছে। চালিয়ে যেতে মিশন চালিয়ে যাও বলুন।")
                    return
                }
                val reason = ResourceGuard.constraintReason(app)
                if (reason != null) {
                    resourcePauses++
                    if (resourcePauses > MAX_RESOURCE_PAUSES) {
                        finish(app, MissionStatus.NEEDS_USER, goal, step, history,
                            "ফোনের চাপ কমছে না ($reason)। চার্জ বা ঠান্ডা করে মিশন চালিয়ে যাও বলুন।")
                        return
                    }
                    _state.value = _state.value.copy(
                        status = MissionStatus.PAUSED_RESOURCE, lastNote = "ফোনের চাপ ($reason), অপেক্ষা করছি"
                    )
                    delay(RESOURCE_PAUSE_MS)
                    continue
                }
                resourcePauses = 0

                val svc = AutoTouchService.instance
                if (svc == null) {
                    finish(app, MissionStatus.FAILED, goal, step, history, "অ্যাক্সেসিবিলিটি সার্ভিস বন্ধ হয়ে গেছে।")
                    return
                }
                val screen = try { svc.dumpVisibleScreenNodesSummary(30) } catch (_: Throwable) { "" }
                if (screen.isBlank()) {
                    fails++
                    if (fails >= MAX_CONSECUTIVE_FAILS) {
                        finish(app, MissionStatus.NEEDS_USER, goal, step, history,
                            "স্ক্রিন পড়া যাচ্ছে না। ফোন লক বা স্ক্রিন বন্ধ থাকতে পারে।")
                        return
                    }
                    delay(STEP_DELAY_MS * 2)
                    continue
                }

                step++
                _state.value = _state.value.copy(
                    status = MissionStatus.RUNNING, step = step, lastNote = "ধাপ $step চলছে"
                )
                val decision = askAi(app, goal, step, history, screen)
                if (decision == null) {
                    fails++
                    if (fails >= MAX_CONSECUTIVE_FAILS) {
                        finish(app, MissionStatus.NEEDS_USER, goal, step, history,
                            "এআই থেকে বোঝার মতো উত্তর পাচ্ছি না। API কী বা ইন্টারনেট দেখুন।")
                        return
                    }
                    delay(STEP_DELAY_MS * 2)
                    continue
                }
                val (action, text, why) = decision

                if (action == "DONE") {
                    finish(app, MissionStatus.DONE, goal, step, history, "মিশন সম্পন্ন। ${why.take(120)}")
                    return
                }
                if (action == "ASK_USER") {
                    finish(app, MissionStatus.NEEDS_USER, goal, step, history, "আপনার সাহায্য লাগবে। ${(why.ifBlank { text }).take(160)}")
                    return
                }
                val textLower = text.lowercase()
                if ((action == "CLICK" || action == "TYPE") && sensitiveWords.any { textLower.contains(it) }) {
                    finish(app, MissionStatus.NEEDS_USER, goal, step, history,
                        "এই ধাপ সংবেদনশীল (টাকা, মোছা বা পাসওয়ার্ড): ${text.take(60)}। নিজে করে মিশন চালিয়ে যাও বলুন।")
                    return
                }

                val sig = "$action|$text"
                sameCount = if (sig == lastSig) sameCount + 1 else 1
                lastSig = sig
                val limit = if (action.startsWith("SCROLL")) MAX_SAME_SCROLL else MAX_SAME_ACTION
                if (action != "WAIT" && sameCount >= limit) {
                    finish(app, MissionStatus.NEEDS_USER, goal, step, history,
                        "একই কাজ বারবার হচ্ছে ($action), আটকে গেছি।")
                    return
                }

                val ok = withContext(Dispatchers.Main) { execute(svc, action, text) }
                fails = if (ok) 0 else fails + 1
                history.addLast("ধাপ $step: $action '${text.take(40)}' -> ${if (ok) "সফল" else "ব্যর্থ"}")
                while (history.size > 40) history.removeFirst()
                persist(app, goal, step, MissionStatus.RUNNING, history.toList())
                if (fails >= MAX_CONSECUTIVE_FAILS) {
                    finish(app, MissionStatus.NEEDS_USER, goal, step, history,
                        "পরপর $MAX_CONSECUTIVE_FAILS বার কাজ ব্যর্থ হয়েছে। স্ক্রিন দেখে সাহায্য করুন।")
                    return
                }
                delay(STEP_DELAY_MS)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            finish(app, MissionStatus.FAILED, goal, step, history, "ত্রুটি হয়েছে: ${t.message ?: "অজানা"}")
        }
    }

    private suspend fun execute(svc: AutoTouchService, action: String, text: String): Boolean {
        return try {
            when (action) {
                "CLICK" -> {
                    val hit = svc.findAndClickNodeByText(text, false)
                    if (hit != null) svc.performClickAt(hit.first.exactCenterX(), hit.first.exactCenterY(), 70L)
                    else false
                }
                "TYPE" -> svc.typeTextIntoExternalPhoneApp(text)
                "SCROLL_DOWN" -> svc.performScroll(true)
                "SCROLL_UP" -> svc.performScroll(false)
                "BACK" -> svc.performPhoneGlobalAction("BACK")
                "HOME" -> svc.performPhoneGlobalAction("HOME")
                "WAIT" -> { delay(3000); true }
                else -> false
            }
        } catch (_: CancellationException) {
            throw CancellationException("cancelled")
        } catch (_: Throwable) {
            false
        }
    }

    private data class Decision(val action: String, val text: String, val why: String)

    /** The step number is part of the prompt on purpose: it stops the SimHash cache from replaying an old action. */
    private suspend fun askAi(
        app: Context, goal: String, step: Int, history: List<String>, screen: String
    ): Decision? {
        val system = "তুমি একটি অ্যান্ড্রয়েড স্ক্রিন-অটোমেশন এজেন্ট। শুধু একটি JSON অবজেক্ট দাও, আর কিছু নয়: " +
            "{\"action\":\"CLICK|TYPE|SCROLL_DOWN|SCROLL_UP|BACK|HOME|WAIT|DONE|ASK_USER\",\"text\":\"...\",\"reason\":\"...\"}। " +
            "CLICK এর text হলো স্ক্রিনে দেখা বাটনের লেখা। TYPE এর text হলো টাইপ করার বার্তা। " +
            "লক্ষ্য পূর্ণ হলে DONE। বুঝতে না পারলে, বা টাকা, মোছা, পাসওয়ার্ড লাগলে ASK_USER। প্রতিবার একটিই কাজ।"
        val prompt = "STEP $step\nলক্ষ্য: $goal\nসাম্প্রতিক কাজ:\n${history.takeLast(8).joinToString("\n")}\nবর্তমান স্ক্রিন:\n$screen"
        val raw = UniversalAiManager.executeAiPrompt(
            prompt = prompt, systemInstruction = system, includeStandingRules = false
        ).getOrNull() ?: return null
        val a = raw.indexOf('{')
        val b = raw.lastIndexOf('}')
        if (a < 0 || b <= a) return null
        return try {
            val o = JSONObject(raw.substring(a, b + 1))
            val action = o.optString("action").trim().uppercase()
            if (action !in setOf("CLICK", "TYPE", "SCROLL_DOWN", "SCROLL_UP", "BACK", "HOME", "WAIT", "DONE", "ASK_USER")) {
                null
            } else {
                Decision(action, o.optString("text").trim(), o.optString("reason").trim())
            }
        } catch (_: Throwable) {
            null
        }
    }

    private suspend fun finish(
        app: Context, status: MissionStatus, goal: String, step: Int,
        history: List<String>, note: String
    ) {
        _state.value = _state.value.copy(status = status, goal = goal, step = step, lastNote = note)
        persist(app, goal, step, status, history.toList())
        withContext(Dispatchers.Main) {
            try { SovereignGeminiLiveOmniEngine.speakLiveAloud(app, note) } catch (_: Throwable) { }
        }
    }

    private fun persist(
        app: Context, goal: String, step: Int, status: MissionStatus,
        history: List<String>, keepHistory: Boolean = false
    ) {
        try {
            val e = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString("goal", goal)
                .putInt("step", step)
                .putString("status", status.name)
                .putLong("updatedAt", System.currentTimeMillis())
            if (!keepHistory) e.putString("history", history.takeLast(12).joinToString("\n"))
            e.apply()
        } catch (_: Throwable) {
        }
    }
}
