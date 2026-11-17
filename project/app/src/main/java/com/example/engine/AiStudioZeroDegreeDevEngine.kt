package com.example.engine

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Headers
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.max
import kotlin.math.min

/**
 * ══════════════════════════════════════════════════════════════════════════════
 * 🎛️ GOOGLE AI STUDIO 0.0° + 10X BEYOND-AI-STUDIO DEVELOPER & QUOTA GOVERNOR
 * ══════════════════════════════════════════════════════════════════════════════
 *
 * ১. **Google AI Studio 0.0° & Full Parameter Suite**:
 *    - Temperature (`0.0°` শূন্য ডিগ্রি ডিটারমিনিস্টিক লক থেকে `2.0°` পর্যন্ত সম্পূর্ণ নিয়ন্ত্রণ)
 *    - Top-P (`0.0` - `1.0`), Top-K (`1` - `100`), Presence Penalty (`-2.0` - `2.0`), Frequency Penalty (`-2.0` - `2.0`)
 *    - Deterministic Seed Lock (`seed = 42`), Stop Sequences, Response MIME Type (`text/plain`, `application/json`)
 *    - Max Output Tokens (`1,024` - `65,536`), Thinking Budget (`0` - `32,768` টোকেন)
 *    - Safety Settings (`BLOCK_NONE` সব Harm Category-তে), Google Search Grounding ও Code Execution Sandbox (`code_execution`)
 *
 * ২. **10x Better Than AI Studio — "কাজ গড-লেভেলের কোড দেবে, কিন্তু স্লো টোকেন খাবে" (`GOD_CODE_SLOW_TOKEN`)**:
 *    - সাধারণ স্লো মোডে মডেল ছোট উত্তর দেয়, কিন্তু আমাদের **Dense-Code Slow-Token Governor** মডেলের বুদ্ধি বা কোডের গভীরতা ১% ও কমায় না!
 *    - এটি প্রম্পটের অপ্রয়োজনীয় কথা (Conversational Fluff) ও পুরনো ডুপ্লিকেট টোকেন AST-লেভেলে ছেঁটে ফেলে এবং মডেলকে নির্দেশ দেয়:
 *      *"কোনো ভূমিকা বা ফালতু কথা না লিখে ১০০% নিখুঁত, গাঢ় (High-Density) প্রোডাকশন কোড ও লজিক দাও"*।
 *      ফলে প্রতি টোকেনে ১০ গুণ বেশি কোড পাওয়া যায় এবং API Key-র লিমিটেশন সবচেয়ে আস্তে আস্তে খরচ হয়!
 *
 * ৩. **On-Demand Silent Introspection ("নিজে থেকে ফালতু অভ্যন্তরীণ কথা আনবে না, কিন্তু জিজ্ঞেস করলে সব দেখে নিখুঁত বলবে")**:
 *    - সাধারণ চ্যাটের সময় অ্যাপের ভেতরে কি চলছে তা অযাচিতভাবে মেসেজে ঢুকিয়ে AI-কে বিভ্রান্ত করে না।
 *    - কিন্তু ইউজার যখনই জিজ্ঞেস করবে ("লিমিটেশন কত বাকি?", "ভেতরে কোন জায়গাতে কি চলছে?", "টোকেন কত খেল?"),
 *      তখনই রিয়েল-টাইম টেলিম্যাট্রি দেখে একদম নিখুঁত সংখ্যা ও স্ট্যাটাস বলে দেবে!
 */
object AiStudioZeroDegreeDevEngine {

    private const val PREF_NAME = "ai_studio_zero_degree_dev_prefs_v2"

    enum class QuotaBurnRateMode(
        val id: String,
        val titleBangla: String,
        val badgeText: String,
        val descriptionBangla: String,
        val historyTurnsLimit: Int,
        val maxCharsPerHistoryTurn: Int,
        val autoThinkingBudgetCap: Int,
        val allowParallelSpeculativeRacing: Boolean,
        val interCallPacingDelayMs: Long
    ) {
        GOD_CODE_SLOW_TOKEN_10X(
            id = "GOD_CODE_SLOW_TOKEN_10X",
            titleBangla = "🛡️ গড-লেভেল কোড + সবচেয়ে স্লো টোকেন (10x কোটা সেভার)",
            badgeText = "🛡️ গড-কোড স্লো টোকেন",
            descriptionBangla = "কাজ ও রিসার্চ হবে ১০০% গড-লেভেলের ও কোড-লেভেলের, কিন্তু ফালতু কথা ছেঁটে টোকেন খাবে সবচেয়ে আস্তে আস্তে!",
            historyTurnsLimit = 6,
            maxCharsPerHistoryTurn = 1200,
            autoThinkingBudgetCap = 8192,
            allowParallelSpeculativeRacing = false,
            interCallPacingDelayMs = 1400L
        ),
        SMART_ADAPTIVE(
            id = "SMART_ADAPTIVE",
            titleBangla = "⚖️ স্মার্ট ব্যালেন্সড (অ্যাডাপ্টিভ)",
            badgeText = "⚖️ স্মার্ট কোটা",
            descriptionBangla = "কোটা ও প্রশ্নের গভীরতা অনুযায়ী অটোমেটিক টোকেন ও স্পিড ব্যালেন্স করবে।",
            historyTurnsLimit = 14,
            maxCharsPerHistoryTurn = 2800,
            autoThinkingBudgetCap = 16384,
            allowParallelSpeculativeRacing = true,
            interCallPacingDelayMs = 300L
        ),
        FAST_MAX_OVERCLOCK(
            id = "FAST_MAX_OVERCLOCK",
            titleBangla = "🔥 ম্যাক্স পাওয়ার ফাস্ট বার্ন (Uncapped 64K + 3-Brain)",
            badgeText = "🔥 ম্যাক্স ফাস্ট বার্ন",
            descriptionBangla = "লিমিটেশনের তোয়াক্কা না করে সর্বোচ্চ ৬৫,৫৩৬ আউটপুট টোকেন, ৩২,৭৬৮ থিংকিং বাজেট ও প্যারালাল ব্রেন চালাবে।",
            historyTurnsLimit = 28,
            maxCharsPerHistoryTurn = 6500,
            autoThinkingBudgetCap = 32768,
            allowParallelSpeculativeRacing = true,
            interCallPacingDelayMs = 0L
        );

        companion object {
            fun fromId(id: String?): QuotaBurnRateMode =
                values().firstOrNull { it.id.equals(id, ignoreCase = true) }
                    ?: if (id == "SLOW_SAVER_10X") GOD_CODE_SLOW_TOKEN_10X else GOD_CODE_SLOW_TOKEN_10X
        }
    }

    data class DeveloperStudioConfig(
        val isDeveloperHudExpanded: Boolean = false,
        val isZeroDegreeLocked: Boolean = true,
        val temperature: Float = 0.0f, // 0.0° Zero-Degree Default
        val topP: Float = 0.95f,
        val topK: Int = 40,
        val presencePenalty: Float = 0.0f,
        val frequencyPenalty: Float = 0.0f,
        val deterministicSeed: Int = 42,
        val useDeterministicSeed: Boolean = true,
        val maxOutputTokens: Int = 32768,
        val thinkingBudgetTokens: Int = 16384,
        val unlockBlockNoneSafety: Boolean = true,
        val enableGoogleSearchGrounding: Boolean = false,
        val enableCodeExecutionSandbox: Boolean = false,
        val enableJsonStructuredMode: Boolean = false,
        val enableDenseCodeZeroFluffDirective: Boolean = true,
        val boldHardenedSystemInstruction: String = "১০০% গড-লেভেল কোড আর্কিটেকচার, শূন্য ডিগ্রি (0.0°) নিখুঁত যুক্তি, কোনো নরমাল বা ভাসা-ভাসা উত্তর নয়—প্রতিটি টোকেনে সর্বোচ্চ কোড ডেনসিটি বজায় রাখো।",
        val quotaBurnMode: QuotaBurnRateMode = QuotaBurnRateMode.GOD_CODE_SLOW_TOKEN_10X,
        val configuredDailyRpdLimitPerKey: Int = 1500,
        val configuredMinuteRpmLimitPerKey: Int = 15,
        val configuredMinuteTpmLimitPerKey: Int = 1_000_000
    )

    data class ApiQuotaLimitationTelemetry(
        val activeKeyCount: Int = 1,
        val rpmLimitTotal: Int = 15,
        val rpmUsedCurrentMinute: Int = 0,
        val rpmRemainingCurrentMinute: Int = 15,
        val rpdLimitTotal: Int = 1500,
        val rpdUsedToday: Int = 0,
        val rpdRemainingToday: Int = 1500,
        val tpmLimitTotal: Int = 1_000_000,
        val tokensUsedCurrentMinute: Int = 0,
        val tokensRemainingCurrentMinute: Int = 1_000_000,
        val lastPromptTokens: Int = 0,
        val lastCandidateOutputTokens: Int = 0,
        val lastThinkingTokens: Int = 0,
        val lastTotalTokens: Int = 0,
        val sessionTotalPromptTokens: Long = 0L,
        val sessionTotalOutputTokens: Long = 0L,
        val sessionTotalThinkingTokens: Long = 0L,
        val sessionTotalTokens: Long = 0L,
        val sessionSavedTokensByGovernor: Long = 0L,
        val remainingQuotaPercentage: Float = 100.0f,
        val headerReportedRemainingRequests: Int? = null,
        val headerReportedRemainingTokens: Int? = null,
        val headerReportedResetSeconds: Int? = null,
        val burnSpeedStatusBangla: String = "স্থির (০ টোকেন/মিনিট)",
        val limitationForecastBangla: String = "১০০% লিমিটেশন বাকি আছে (🛡️ গড-কোড স্লো টোকেন মোড)"
    )

    private val _devConfig = MutableStateFlow(DeveloperStudioConfig())
    val devConfig: StateFlow<DeveloperStudioConfig> = _devConfig.asStateFlow()

    private val _quotaTelemetry = MutableStateFlow(ApiQuotaLimitationTelemetry())
    val quotaTelemetry: StateFlow<ApiQuotaLimitationTelemetry> = _quotaTelemetry.asStateFlow()

    private val minuteCallLog = mutableListOf<Pair<Long, Int>>()
    private var currentDayEpoch: Long = System.currentTimeMillis() / 86_400_000L

    @Synchronized
    fun init(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val savedDay = prefs.getLong("quota_day_epoch", currentDayEpoch)
        val nowDay = System.currentTimeMillis() / 86_400_000L
        val rpdUsed = if (savedDay == nowDay) prefs.getInt("rpd_used_today", 0) else 0
        currentDayEpoch = nowDay

        val temp = prefs.getFloat("temperature", 0.0f).coerceIn(0.0f, 2.0f)
        val zeroLocked = prefs.getBoolean("is_zero_degree_locked", temp == 0.0f)
        val burnMode = QuotaBurnRateMode.fromId(
            prefs.getString("quota_burn_mode", QuotaBurnRateMode.GOD_CODE_SLOW_TOKEN_10X.id)
        )
        val dailyLimit = prefs.getInt("daily_rpd_limit", 1500).coerceAtLeast(50)
        val rpmLimit = prefs.getInt("minute_rpm_limit", 15).coerceAtLeast(2)

        _devConfig.value = DeveloperStudioConfig(
            isDeveloperHudExpanded = prefs.getBoolean("hud_expanded", false),
            isZeroDegreeLocked = zeroLocked,
            temperature = if (zeroLocked) 0.0f else temp,
            topP = prefs.getFloat("top_p", 0.95f).coerceIn(0.0f, 1.0f),
            topK = prefs.getInt("top_k", 40).coerceIn(1, 100),
            presencePenalty = prefs.getFloat("presence_penalty", 0.0f).coerceIn(-2.0f, 2.0f),
            frequencyPenalty = prefs.getFloat("frequency_penalty", 0.0f).coerceIn(-2.0f, 2.0f),
            deterministicSeed = prefs.getInt("deterministic_seed", 42),
            useDeterministicSeed = prefs.getBoolean("use_deterministic_seed", true),
            maxOutputTokens = prefs.getInt("max_output_tokens", 32768).coerceIn(1024, 65536),
            thinkingBudgetTokens = prefs.getInt("thinking_budget_tokens", 16384).coerceIn(0, 32768),
            unlockBlockNoneSafety = prefs.getBoolean("unlock_block_none", true),
            enableGoogleSearchGrounding = prefs.getBoolean("enable_search_grounding", false),
            enableCodeExecutionSandbox = prefs.getBoolean("enable_code_execution", false),
            enableJsonStructuredMode = prefs.getBoolean("enable_json_mode", false),
            enableDenseCodeZeroFluffDirective = prefs.getBoolean("enable_dense_code_directive", true),
            boldHardenedSystemInstruction = prefs.getString(
                "bold_hardened_sys_instruction",
                "১০০% গড-লেভেল কোড আর্কিটেকচার, শূন্য ডিগ্রি (0.0°) নিখুঁত যুক্তি, কোনো নরমাল বা ভাসা-ভাসা উত্তর নয়—প্রতিটি টোকেনে সর্বোচ্চ কোড ডেনসিটি বজায় রাখো।"
            ) ?: "",
            quotaBurnMode = burnMode,
            configuredDailyRpdLimitPerKey = dailyLimit,
            configuredMinuteRpmLimitPerKey = rpmLimit
        )

        val totalSessionTokens = prefs.getLong("session_total_tokens", 0L)
        val savedByGov = prefs.getLong("session_saved_tokens", 0L)
        _quotaTelemetry.value = _quotaTelemetry.value.copy(
            rpdLimitTotal = dailyLimit,
            rpdUsedToday = rpdUsed,
            rpdRemainingToday = max(0, dailyLimit - rpdUsed),
            rpmLimitTotal = rpmLimit,
            rpmRemainingCurrentMinute = rpmLimit,
            sessionTotalTokens = totalSessionTokens,
            sessionSavedTokensByGovernor = savedByGov
        )
        recalculateLiveQuotaWindow(activeProfileCount = 1)
    }

    private fun persistConfig(context: Context?) {
        val ctx = context?.applicationContext ?: return
        val cfg = _devConfig.value
        val tel = _quotaTelemetry.value
        ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
            .putBoolean("hud_expanded", cfg.isDeveloperHudExpanded)
            .putBoolean("is_zero_degree_locked", cfg.isZeroDegreeLocked)
            .putFloat("temperature", cfg.temperature)
            .putFloat("top_p", cfg.topP)
            .putInt("top_k", cfg.topK)
            .putFloat("presence_penalty", cfg.presencePenalty)
            .putFloat("frequency_penalty", cfg.frequencyPenalty)
            .putInt("deterministic_seed", cfg.deterministicSeed)
            .putBoolean("use_deterministic_seed", cfg.useDeterministicSeed)
            .putInt("max_output_tokens", cfg.maxOutputTokens)
            .putInt("thinking_budget_tokens", cfg.thinkingBudgetTokens)
            .putBoolean("unlock_block_none", cfg.unlockBlockNoneSafety)
            .putBoolean("enable_search_grounding", cfg.enableGoogleSearchGrounding)
            .putBoolean("enable_code_execution", cfg.enableCodeExecutionSandbox)
            .putBoolean("enable_json_mode", cfg.enableJsonStructuredMode)
            .putBoolean("enable_dense_code_directive", cfg.enableDenseCodeZeroFluffDirective)
            .putString("bold_hardened_sys_instruction", cfg.boldHardenedSystemInstruction)
            .putString("quota_burn_mode", cfg.quotaBurnMode.id)
            .putInt("daily_rpd_limit", cfg.configuredDailyRpdLimitPerKey)
            .putInt("minute_rpm_limit", cfg.configuredMinuteRpmLimitPerKey)
            .putLong("quota_day_epoch", currentDayEpoch)
            .putInt("rpd_used_today", tel.rpdUsedToday)
            .putLong("session_total_tokens", tel.sessionTotalTokens)
            .putLong("session_saved_tokens", tel.sessionSavedTokensByGovernor)
            .apply()
    }

    fun toggleDeveloperHudExpanded(context: Context? = null) {
        _devConfig.value = _devConfig.value.copy(
            isDeveloperHudExpanded = !_devConfig.value.isDeveloperHudExpanded
        )
        persistConfig(context)
    }

    fun setTemperature(temp: Float, context: Context? = null) {
        val clamped = (Math.round(temp.coerceIn(0.0f, 2.0f) * 100f) / 100f)
        _devConfig.value = _devConfig.value.copy(
            temperature = clamped,
            isZeroDegreeLocked = (clamped == 0.0f)
        )
        persistConfig(context)
    }

    fun lockZeroDegreeMode(locked: Boolean, context: Context? = null) {
        _devConfig.value = _devConfig.value.copy(
            isZeroDegreeLocked = locked,
            temperature = if (locked) 0.0f else 0.35f
        )
        persistConfig(context)
    }

    fun setTopP(topP: Float, context: Context? = null) {
        val clamped = (Math.round(topP.coerceIn(0.0f, 1.0f) * 100f) / 100f)
        _devConfig.value = _devConfig.value.copy(topP = clamped)
        persistConfig(context)
    }

    fun setTopK(topK: Int, context: Context? = null) {
        _devConfig.value = _devConfig.value.copy(topK = topK.coerceIn(1, 100))
        persistConfig(context)
    }

    fun setPenalties(presence: Float, frequency: Float, context: Context? = null) {
        _devConfig.value = _devConfig.value.copy(
            presencePenalty = (Math.round(presence.coerceIn(-2.0f, 2.0f) * 10f) / 10f),
            frequencyPenalty = (Math.round(frequency.coerceIn(-2.0f, 2.0f) * 10f) / 10f)
        )
        persistConfig(context)
    }

    fun setDeterministicSeed(seed: Int, enabled: Boolean, context: Context? = null) {
        _devConfig.value = _devConfig.value.copy(
            deterministicSeed = seed,
            useDeterministicSeed = enabled
        )
        persistConfig(context)
    }

    fun setMaxOutputTokens(tokens: Int, context: Context? = null) {
        _devConfig.value = _devConfig.value.copy(maxOutputTokens = tokens.coerceIn(1024, 65536))
        persistConfig(context)
    }

    fun setThinkingBudgetTokens(tokens: Int, context: Context? = null) {
        _devConfig.value = _devConfig.value.copy(thinkingBudgetTokens = tokens.coerceIn(0, 32768))
        persistConfig(context)
    }

    fun setUnlockBlockNoneSafety(enabled: Boolean, context: Context? = null) {
        _devConfig.value = _devConfig.value.copy(unlockBlockNoneSafety = enabled)
        persistConfig(context)
    }

    fun setGoogleSearchGrounding(enabled: Boolean, context: Context? = null) {
        _devConfig.value = _devConfig.value.copy(enableGoogleSearchGrounding = enabled)
        persistConfig(context)
    }

    fun setCodeExecutionSandbox(enabled: Boolean, context: Context? = null) {
        _devConfig.value = _devConfig.value.copy(enableCodeExecutionSandbox = enabled)
        persistConfig(context)
    }

    fun setDenseCodeZeroFluffDirective(enabled: Boolean, context: Context? = null) {
        _devConfig.value = _devConfig.value.copy(enableDenseCodeZeroFluffDirective = enabled)
        persistConfig(context)
    }

    fun setBoldHardenedSystemInstruction(instruction: String, context: Context? = null) {
        _devConfig.value = _devConfig.value.copy(boldHardenedSystemInstruction = instruction)
        persistConfig(context)
    }

    fun setQuotaBurnMode(mode: QuotaBurnRateMode, context: Context? = null) {
        _devConfig.value = _devConfig.value.copy(quotaBurnMode = mode)
        recalculateLiveQuotaWindow(activeProfileCount = _quotaTelemetry.value.activeKeyCount)
        persistConfig(context)
    }

    fun configureKeyQuotaLimits(dailyRpdPerKey: Int, minuteRpmPerKey: Int, context: Context? = null) {
        _devConfig.value = _devConfig.value.copy(
            configuredDailyRpdLimitPerKey = dailyRpdPerKey.coerceIn(20, 100_000),
            configuredMinuteRpmLimitPerKey = minuteRpmPerKey.coerceIn(2, 2000)
        )
        recalculateLiveQuotaWindow(activeProfileCount = _quotaTelemetry.value.activeKeyCount)
        persistConfig(context)
    }

    fun recordSavedTokensFromCompression(savedChars: Int, context: Context? = null) {
        if (savedChars <= 0) return
        val estimatedSavedTokens = max(1, savedChars / 4).toLong()
        _quotaTelemetry.value = _quotaTelemetry.value.copy(
            sessionSavedTokensByGovernor = _quotaTelemetry.value.sessionSavedTokensByGovernor + estimatedSavedTokens
        )
        persistConfig(context)
    }

    /**
     * ইউজার কি নিজে থেকে লিমিটেশন, কোটা বা অ্যাপের ভেতরে কোথায় কি চলছে তা জানতে চেয়েছে?
     * ("এর মধ্যে কোন জায়গাতে কি চলছে এমনি নিজে কিছুই আনবে না। জানবে না কিন্তু জিজ্ঞাস করলে তখন দেখে বলতে পারবে")
     */
    fun isUserAskingForLiveQuotaOrAppState(userPrompt: String): Boolean {
        val lower = userPrompt.lowercase()
        return lower.contains("লিমিটেশন") ||
            lower.contains("লিমিট কত") ||
            lower.contains("কোটা") ||
            lower.contains("কতটি বাকি") ||
            lower.contains("কত বাকি") ||
            lower.contains("টোকেন কত") ||
            lower.contains("কোন জায়গাতে কি চলছে") ||
            lower.contains("কোন জায়গাতে কি চলছে") ||
            lower.contains("ভেতরে কি চলছে") ||
            lower.contains("ডেভেলপার মোড") ||
            lower.contains("শূন্য ডিগ্রি") ||
            lower.contains("শিডিউল") ||
            lower.contains("কখন জাগবে")
    }

    /**
     * সংকলিত গড-লেভেল ডেভেলপার সিস্টেম ইনস্ট্রাকশন ব্লক:
     * - সবসময় কোড-লেভেল গভীরতা বজায় রাখে ও ফালতু কথা ছেঁটে স্লো টোকেন খরচ নিশ্চিত করে।
     * - শুধুমাত্র ইউজার যখন জিজ্ঞেস করে তখনই লাইভ লিমিটেশন ও অ্যাপের অভ্যন্তরীণ স্টেট যুক্ত করে!
     */
    fun buildCompiledDeveloperInstructionBlock(userPrompt: String): String {
        val cfg = _devConfig.value
        return buildString {
            if (cfg.boldHardenedSystemInstruction.isNotBlank()) {
                appendLine("=== [🎛️ GOOGLE AI STUDIO 0.0° HARDENED KERNEL INSTRUCTION] ===")
                appendLine(cfg.boldHardenedSystemInstruction.trim())
                appendLine()
            }
            if (cfg.enableDenseCodeZeroFluffDirective) {
                appendLine("=== [🛡️ GOD-LEVEL CODE DENSITY & SLOW-TOKEN EFFICIENCY LAW] ===")
                appendLine("১. তোমার প্রতিটি উত্তর ও রিসার্চ হবে ১০০% গড-লেভেল এবং প্রোডাকশন কোড-লেভেলের—কখনোই নরমাল বা ভাসা-ভাসা উত্তর দেবে না।")
                appendLine("২. টোকেন আস্তে আস্তে খরচ করার জন্য (Slow Token Burn) কোনো অপ্রয়োজনীয় ভূমিকা, তোষামোদ বা লম্বা ফালতু কথা লিখবে না; সরাসরি মূল গভীর লজিক, আর্কিটেকচার এবং সম্পূর্ণ কার্যকরী কোড দেবে যাতে প্রতিটি টোকেন ১০০% কাজে লাগে।")
                appendLine("৩. ইউজার জিজ্ঞেস না করলে অ্যাপের ভেতরে কোন জায়গাতে কি চলছে বা মেটা-কার্নেল টেলিম্যাট্রি নিজে থেকে অযাচিতভাবে বলবে না; শুধুমাত্র ইউজার জানতে চাইলে নিখুঁত তথ্য দেবে।")
            }
            if (isUserAskingForLiveQuotaOrAppState(userPrompt)) {
                appendLine()
                appendLine("=== [📊 ON-DEMAND LIVE API QUOTA & APP STATE SNAPSHOT (ইউজার জিজ্ঞেস করেছে তাই দেখে বলো)] ===")
                appendLine(buildLiveLimitationStatusReportBangla())
                appendLine(SovereignScheduledAutoEvolutionDaemon.buildScheduleStatusSummaryBangla())
            }
        }.trim()
    }

    @Synchronized
    fun recordApiResponseTelemetry(
        rawResponseBody: String?,
        headers: Headers?,
        estimatedPromptChars: Int,
        estimatedOutputChars: Int,
        activeKeyCount: Int = 1,
        context: Context? = null
    ) {
        var promptTokens = max(1, estimatedPromptChars / 4)
        var candidateTokens = max(1, estimatedOutputChars / 4)
        var thoughtTokens = 0
        var totalTokens = promptTokens + candidateTokens

        if (!rawResponseBody.isNullOrBlank()) {
            try {
                val root = JSONObject(rawResponseBody)
                val usageMeta = root.optJSONObject("usageMetadata")
                if (usageMeta != null) {
                    promptTokens = usageMeta.optInt("promptTokenCount", promptTokens)
                    candidateTokens = usageMeta.optInt("candidatesTokenCount", candidateTokens)
                    thoughtTokens = usageMeta.optInt("thoughtsTokenCount", 0)
                    totalTokens = usageMeta.optInt(
                        "totalTokenCount",
                        promptTokens + candidateTokens + thoughtTokens
                    )
                } else {
                    val oaiUsage = root.optJSONObject("usage")
                    if (oaiUsage != null) {
                        promptTokens = oaiUsage.optInt(
                            "prompt_tokens",
                            oaiUsage.optInt("input_tokens", promptTokens)
                        )
                        candidateTokens = oaiUsage.optInt(
                            "completion_tokens",
                            oaiUsage.optInt("output_tokens", candidateTokens)
                        )
                        val details = oaiUsage.optJSONObject("completion_tokens_details")
                        if (details != null) {
                            thoughtTokens = details.optInt("reasoning_tokens", 0)
                        }
                        totalTokens = oaiUsage.optInt("total_tokens", promptTokens + candidateTokens + thoughtTokens)
                    }
                }
            } catch (_: Exception) {
            }
        }

        var headerRemReq: Int? = null
        var headerLimitReq: Int? = null
        var headerRemTok: Int? = null
        var headerResetSec: Int? = null

        if (headers != null) {
            headerRemReq = parseHeaderInt(
                headers,
                "x-ratelimit-remaining-requests",
                "x-ratelimit-remaining",
                "anthropic-ratelimit-requests-remaining"
            )
            headerLimitReq = parseHeaderInt(
                headers,
                "x-ratelimit-limit-requests",
                "x-ratelimit-limit",
                "anthropic-ratelimit-requests-limit"
            )
            headerRemTok = parseHeaderInt(
                headers,
                "x-ratelimit-remaining-tokens",
                "anthropic-ratelimit-tokens-remaining"
            )
            headerResetSec = parseHeaderSeconds(
                headers,
                "x-ratelimit-reset-requests",
                "x-ratelimit-reset",
                "retry-after"
            )
        }

        val now = System.currentTimeMillis()
        val nowDay = now / 86_400_000L
        val currentTel = _quotaTelemetry.value
        val newRpdUsed = if (nowDay == currentDayEpoch) currentTel.rpdUsedToday + 1 else 1
        currentDayEpoch = nowDay

        minuteCallLog.add(now to totalTokens)

        _quotaTelemetry.value = currentTel.copy(
            activeKeyCount = max(1, activeKeyCount),
            rpdUsedToday = newRpdUsed,
            lastPromptTokens = promptTokens,
            lastCandidateOutputTokens = candidateTokens,
            lastThinkingTokens = thoughtTokens,
            lastTotalTokens = totalTokens,
            sessionTotalPromptTokens = currentTel.sessionTotalPromptTokens + promptTokens,
            sessionTotalOutputTokens = currentTel.sessionTotalOutputTokens + candidateTokens,
            sessionTotalThinkingTokens = currentTel.sessionTotalThinkingTokens + thoughtTokens,
            sessionTotalTokens = currentTel.sessionTotalTokens + totalTokens,
            headerReportedRemainingRequests = headerRemReq ?: currentTel.headerReportedRemainingRequests,
            headerReportedRemainingTokens = headerRemTok ?: currentTel.headerReportedRemainingTokens,
            headerReportedResetSeconds = headerResetSec
        )

        if (headerLimitReq != null && headerLimitReq > 10) {
            _devConfig.value = _devConfig.value.copy(configuredDailyRpdLimitPerKey = headerLimitReq)
        }

        recalculateLiveQuotaWindow(activeProfileCount = max(1, activeKeyCount))
        persistConfig(context)
    }

    @Synchronized
    fun recalculateLiveQuotaWindow(activeProfileCount: Int = _quotaTelemetry.value.activeKeyCount) {
        val now = System.currentTimeMillis()
        minuteCallLog.removeAll { (timestamp, _) -> (now - timestamp) > 60_000L }

        val cfg = _devConfig.value
        val keyMultiplier = max(1, activeProfileCount)
        val rpmLimitTotal = cfg.configuredMinuteRpmLimitPerKey * keyMultiplier
        val rpdLimitTotal = cfg.configuredDailyRpdLimitPerKey * keyMultiplier
        val tpmLimitTotal = cfg.configuredMinuteTpmLimitPerKey * keyMultiplier

        val rpmUsed = minuteCallLog.size
        val tpmUsed = minuteCallLog.sumOf { it.second }

        val tel = _quotaTelemetry.value
        val effectiveRpdRemaining = tel.headerReportedRemainingRequests
            ?: max(0, rpdLimitTotal - tel.rpdUsedToday)
        val effectiveRpmRemaining = max(0, rpmLimitTotal - rpmUsed)
        val effectiveTpmRemaining = tel.headerReportedRemainingTokens
            ?: max(0, tpmLimitTotal - tpmUsed)

        val rpdPercent = if (rpdLimitTotal > 0) {
            ((effectiveRpdRemaining.toFloat() / rpdLimitTotal.toFloat()) * 100f).coerceIn(0f, 100f)
        } else 100f

        val burnRatePerMin = rpmUsed
        val forecastText = when {
            effectiveRpdRemaining <= 0 ->
                "⚠️ আজকের কোটা শেষের পথে! নতুন API Key যোগ করুন বা গড-কোড স্লো টোকেন মোড চালু রাখুন।"
            burnRatePerMin == 0 ->
                "✅ লিমিটেশন সুরক্ষিত: আজ আরও ~$effectiveRpdRemaining টি রিকোয়েস্ট বাকি (${String.format("%.1f", rpdPercent)}%) • ${cfg.quotaBurnMode.badgeText}"
            else -> {
                val estMinutesLeft = effectiveRpdRemaining / max(1, burnRatePerMin)
                val hours = estMinutesLeft / 60
                val mins = estMinutesLeft % 60
                val modeMultiplierNote = " (${cfg.quotaBurnMode.badgeText})"
                if (hours > 0) {
                    "⏱️ বর্তমান গতিতে লিমিটেশন আরও ~${hours} ঘণ্টা ${mins} মিনিট চলবে$modeMultiplierNote"
                } else {
                    "⚡ বর্তমান গতিতে লিমিটেশন ~${mins} মিনিট চলবে$modeMultiplierNote"
                }
            }
        }

        val speedStatus = "$rpmUsed RPM • ${formatCompactNumber(tpmUsed)} টোকেন/মিনিট"

        _quotaTelemetry.value = tel.copy(
            activeKeyCount = keyMultiplier,
            rpmLimitTotal = rpmLimitTotal,
            rpmUsedCurrentMinute = rpmUsed,
            rpmRemainingCurrentMinute = effectiveRpmRemaining,
            rpdLimitTotal = rpdLimitTotal,
            rpdRemainingToday = effectiveRpdRemaining,
            tpmLimitTotal = tpmLimitTotal,
            tokensUsedCurrentMinute = tpmUsed,
            tokensRemainingCurrentMinute = effectiveTpmRemaining,
            remainingQuotaPercentage = rpdPercent,
            burnSpeedStatusBangla = speedStatus,
            limitationForecastBangla = forecastText
        )
    }

    fun buildGeminiGenerationConfig(
        modelName: String,
        overrideMaxOutputTokens: Int? = null
    ): JSONObject {
        val cfg = _devConfig.value
        val effectiveTemp = if (cfg.isZeroDegreeLocked) 0.0 else cfg.temperature.toDouble()
        val effectiveMaxTokens = overrideMaxOutputTokens ?: when (cfg.quotaBurnMode) {
            QuotaBurnRateMode.GOD_CODE_SLOW_TOKEN_10X -> min(cfg.maxOutputTokens, 16384)
            QuotaBurnRateMode.SMART_ADAPTIVE -> cfg.maxOutputTokens
            QuotaBurnRateMode.FAST_MAX_OVERCLOCK -> max(cfg.maxOutputTokens, 32768)
        }

        return JSONObject().apply {
            put("temperature", effectiveTemp)
            put("topP", cfg.topP.toDouble())
            put("topK", cfg.topK)
            put("maxOutputTokens", effectiveMaxTokens)
            if (cfg.presencePenalty != 0.0f) {
                put("presencePenalty", cfg.presencePenalty.toDouble())
            }
            if (cfg.frequencyPenalty != 0.0f) {
                put("frequencyPenalty", cfg.frequencyPenalty.toDouble())
            }
            if (cfg.useDeterministicSeed) {
                put("seed", cfg.deterministicSeed)
            }
            if (cfg.enableJsonStructuredMode) {
                put("responseMimeType", "application/json")
            }
            val stopSeqArr = SovereignWakeupBatch3VisionAndDspEngine.getActiveStopSequencesJsonArray()
            if (stopSeqArr != null) {
                put("stopSequences", stopSeqArr)
            }
            val lowerModel = modelName.lowercase()
            if ((lowerModel.contains("2.5") || lowerModel.contains("thinking")) && !lowerModel.contains("image")) {
                val effectiveThinking = min(cfg.thinkingBudgetTokens, cfg.quotaBurnMode.autoThinkingBudgetCap)
                if (effectiveThinking > 0) {
                    put("thinkingConfig", JSONObject().apply {
                        put("thinkingBudget", effectiveThinking)
                    })
                }
            }
        }
    }

    fun buildGeminiBlockNoneSafetySettings(): JSONArray? {
        if (!_devConfig.value.unlockBlockNoneSafety) return null
        val categories = listOf(
            "HARM_CATEGORY_HARASSMENT",
            "HARM_CATEGORY_HATE_SPEECH",
            "HARM_CATEGORY_SEXUALLY_EXPLICIT",
            "HARM_CATEGORY_DANGEROUS_CONTENT",
            "HARM_CATEGORY_CIVIC_INTEGRITY"
        )
        return JSONArray().apply {
            for (cat in categories) {
                put(JSONObject().apply {
                    put("category", cat)
                    put("threshold", "BLOCK_NONE")
                })
            }
        }
    }

    fun buildGeminiToolsArray(modelName: String): JSONArray? {
        val cfg = _devConfig.value
        val lower = modelName.lowercase()
        if (lower.contains("1.5")) return null
        val arr = JSONArray()
        if (cfg.enableGoogleSearchGrounding) {
            arr.put(JSONObject().put("google_search", JSONObject()))
        }
        if (cfg.enableCodeExecutionSandbox) {
            arr.put(JSONObject().put("code_execution", JSONObject()))
        }
        return if (arr.length() > 0) arr else null
    }

    private fun parseHeaderInt(headers: Headers, vararg names: String): Int? {
        for (name in names) {
            val raw = headers[name]?.trim() ?: continue
            val numeric = raw.filter { it.isDigit() }.toIntOrNull()
            if (numeric != null) return numeric
        }
        return null
    }

    private fun parseHeaderSeconds(headers: Headers, vararg names: String): Int? {
        for (name in names) {
            val raw = headers[name]?.trim()?.lowercase() ?: continue
            if (raw.endsWith("ms")) {
                val ms = raw.removeSuffix("ms").toDoubleOrNull()
                if (ms != null) return max(1, (ms / 1000.0).toInt())
            } else if (raw.endsWith("s")) {
                val s = raw.removeSuffix("s").toDoubleOrNull()
                if (s != null) return max(1, s.toInt())
            } else {
                val sec = raw.toDoubleOrNull()
                if (sec != null) return max(1, sec.toInt())
            }
        }
        return null
    }

    fun formatCompactNumber(value: Number): String {
        val longVal = value.toLong()
        return when {
            longVal >= 1_000_000L -> String.format("%.2fM", longVal / 1_000_000.0)
            longVal >= 1_000L -> String.format("%.1fK", longVal / 1_000_000.0 * 1000.0)
            else -> longVal.toString()
        }
    }

    fun buildLiveLimitationStatusReportBangla(): String {
        val cfg = _devConfig.value
        val tel = _quotaTelemetry.value
        return buildString {
            appendLine("🎛️ **Google AI Studio 0.0° ডেভেলপার মোড ও লাইভ API লিমিটেশন রিপোর্ট:**")
            appendLine("• **Temperature:** `${if (cfg.isZeroDegreeLocked) "0.0° (শূন্য ডিগ্রি লকড)" else "${cfg.temperature}°"}` | **Top-P:** `${cfg.topP}` | **Top-K:** `${cfg.topK}` | **Seed:** `${if (cfg.useDeterministicSeed) cfg.deterministicSeed else " Random"}`")
            appendLine("• **Max Output Tokens:** `${cfg.maxOutputTokens}` | **Thinking Budget:** `${cfg.thinkingBudgetTokens}` টোকেন")
            appendLine("• **Safety & Tools:** `${if (cfg.unlockBlockNoneSafety) "🔓 BLOCK_NONE (Unfiltered)" else "🔒 Standard"}` | Grounding: `${if (cfg.enableGoogleSearchGrounding) "ON" else "OFF"}` | Code Sandbox: `${if (cfg.enableCodeExecutionSandbox) "ON" else "OFF"}`")
            appendLine("• **টোকেন বার্ন গভর্নর:** `${cfg.quotaBurnMode.titleBangla}`")
            appendLine("• **এই মিনিটে রিকোয়েস্ট বাকি (RPM):** `${tel.rpmRemainingCurrentMinute} / ${tel.rpmLimitTotal}`")
            appendLine("• **আজকের মোট রিকোয়েস্ট বাকি (RPD):** `${tel.rpdRemainingToday} / ${tel.rpdLimitTotal}` (`${String.format("%.1f", tel.remainingQuotaPercentage)}%` বাকি)")
            appendLine("• **এই মিনিটে টোকেন বাকি (TPM):** `${formatCompactNumber(tel.tokensRemainingCurrentMinute)} / ${formatCompactNumber(tel.tpmLimitTotal)}`")
            appendLine("• **সর্বশেষ কলে ব্যবহৃত টোকেন:** মোট `${tel.lastTotalTokens}` (Prompt: `${tel.lastPromptTokens}`, Output: `${tel.lastCandidateOutputTokens}`, Thinking: `${tel.lastThinkingTokens}`)")
            appendLine("• **সেশনে মোট টোকেন:** `${formatCompactNumber(tel.sessionTotalTokens)}` (স্লো-টোকেন গভর্নর বাঁচিয়েছে: `${formatCompactNumber(tel.sessionSavedTokensByGovernor)}` টোকেন)")
            appendLine("• **পূর্বাভাস:** ${tel.limitationForecastBangla}")
            appendLine()
            appendLine(SovereignWakeupBatch1QuantumKernelEngine.buildBatch1StatusSummaryBangla())
            appendLine()
            appendLine(SovereignWakeupBatch2SwarmAndDomEngine.buildBatch2StatusSummaryBangla())
            appendLine()
            appendLine(SovereignWakeupBatch3VisionAndDspEngine.buildBatch3StatusSummaryBangla())
            appendLine()
            appendLine(SovereignWakeupBatch4HardwareAndAuditEngine.buildBatch4StatusSummaryBangla())
            appendLine()
            append(SovereignProDeveloperTrinityEngine.buildTrinityStatusReportBangla())
        }
    }
}
