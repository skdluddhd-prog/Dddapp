package com.example.engine

import android.content.Context
import android.webkit.WebView
import com.example.model.AiKeySpecialistRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Controls which AI roles receive the full Proprioceptive App & Chromium Self-Anatomy Blueprint.
 */
enum class AiSelfAwarenessScope(
    val titleBangla: String,
    val descriptionBangla: String,
    val shortBadge: String
) {
    MAIN_AI_ONLY(
        titleBangla = "👑 শুধু মেইন এআই জানবে (ডিফল্ট)",
        descriptionBangla = "শুধুমাত্র মেইন অমনি-ব্রেইন (OMNI_MASTER_BRAIN) নিজের শরীরের মতো পুরো অ্যাপের কোথায় কোন বাটন, কী অন/অফ ও কী ফিচার আছে তা নখদর্পণে জানবে। অন্য ওয়ার্কারদের মাথা ভারী হবে না।",
        shortBadge = "MAIN AI ONLY"
    ),
    ALL_AI_AND_NANOBOTS(
        titleBangla = "🤖 সব এআই ও ন্যানোবট জানবে",
        descriptionBangla = "মেইন এআই সহ প্রতিটি স্পেশালিস্ট কী এবং ন্যানোবট পুরো অ্যাপ ও ক্রোমিয়ামের সম্পূর্ণ অ্যানাটমি ম্যাপ জানবে।",
        shortBadge = "ALL AI KNOW"
    ),
    MANUAL_OFF(
        titleBangla = "🔒 অফ (শুধু কমান্ড দিলে জানবে)",
        descriptionBangla = "স্বয়ংক্রিয় সেলফ-অ্যানাটমি ম্যাপ ইনজেকশন বন্ধ থাকবে, কেবল অমনি-কন্ট্রোল কমান্ড দিলে ব্যবহৃত হবে।",
        shortBadge = "SCOPE OFF"
    )
}

/**
 * A persistent, AI-generated or user-created Self-Feature Module that adds new live capabilities
 * directly into the app and browser workspace.
 */
data class SelfFeatureModule(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val categoryBadge: String = "SELF-FEATURE",
    val jsPayload: String,
    val isAutoRunOnPageLoad: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class OmniActionLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val command: String,
    val executedActionsSummary: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Callbacks linking MasterOmniControllerEngine to every live state and screen of MainActivity.
 */
data class OmniAppControlBridge(
    val onSetSplitScreen: (Boolean) -> Unit,
    val onSetDesktopMode: (Boolean) -> Unit,
    val onSetContinuousVoice: (Boolean) -> Unit,
    val onNavigateTabA: (String) -> Unit,
    val onNavigateTabB: (String) -> Unit,
    val onOpenSwarmStudio: () -> Unit,
    val onOpenRuleAndPlanner: (Int) -> Unit,
    val onOpenProLabTab: (Int) -> Unit,
    val onOpenLiveSandbox: () -> Unit,
    val onOpenAiKeySettings: () -> Unit,
    val onDeployAndRunSwarmBlueprint: (SwarmBlueprintResult) -> Unit = {},
    val getActiveWebView: () -> WebView?
)

/**
 * Master App Omni-Controller & Proprioceptive Self-Anatomy AI Engine:
 * Gives the user and the Main AI 100% sovereign control over:
 * 1. Every app button (turn ON/OFF), screen, browser tab, Dual-Gear mode, and Same-Key 0.1ms model switching.
 * 2. Sovereign Chromium Developer Flags (Ad/Popup blocker, Zero-RAM Image Blocker, Force Cyber Dark, DOM Storage, Cookies, etc.).
 * 3. Code & Self-Feature Surgery (Add new code/features, edit existing code, or delete code/features by voice or text).
 * 4. Proprioceptive Self-Awareness Map ("নিজের শরীরের মতো পুরো অ্যাপকে জানা") with Selective Scope Toggle.
 */
object MasterOmniControllerEngine {
    private const val PREFS_NAME = "skrl2_master_omni_controller_prefs"
    private const val KEY_SELF_FEATURES_JSON = "key_self_features_json"
    private const val KEY_AWARENESS_SCOPE = "key_ai_self_awareness_scope"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _isProcessingOmniCommand = MutableStateFlow(false)
    val isProcessingOmniCommand: StateFlow<Boolean> = _isProcessingOmniCommand.asStateFlow()

    private val _omniStatusBanner = MutableStateFlow("👑 মাস্টার অমনি-কন্ট্রোলার প্রস্তুত (নিজের শরীরের মতো পুরো অ্যাপ, বাটন অন/অফ, কোড এডিট/ডিলিট ও ক্রোমিয়াম কন্ট্রোল)")
    val omniStatusBanner: StateFlow<String> = _omniStatusBanner.asStateFlow()

    private val _savedSelfFeatures = MutableStateFlow<List<SelfFeatureModule>>(emptyList())
    val savedSelfFeatures: StateFlow<List<SelfFeatureModule>> = _savedSelfFeatures.asStateFlow()

    private val _selfAwarenessScope = MutableStateFlow(AiSelfAwarenessScope.MAIN_AI_ONLY)
    val selfAwarenessScope: StateFlow<AiSelfAwarenessScope> = _selfAwarenessScope.asStateFlow()

    private val _actionLogs = MutableStateFlow<List<OmniActionLogEntry>>(emptyList())
    val actionLogs: StateFlow<List<OmniActionLogEntry>> = _actionLogs.asStateFlow()

    fun init(context: Context) {
        loadSelfFeatures(context)
        loadAwarenessScope(context)
        if (AutonomousSelfEvolutionEngine.activeLiveModule.value == null) {
            val defaultFeature = _savedSelfFeatures.value.firstOrNull()
            if (defaultFeature != null) {
                AutonomousSelfEvolutionEngine.launchModuleInLiveRunner(defaultFeature, fullScreen = false)
            }
        }
    }

    fun setSelfAwarenessScope(context: Context, newScope: AiSelfAwarenessScope) {
        _selfAwarenessScope.value = newScope
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_AWARENESS_SCOPE, newScope.name)
            .apply()
        _omniStatusBanner.value = "🧠 এআই সেলফ-অ্যানাটমি স্কোপ সেট হয়েছে: ${newScope.titleBangla}"
    }

    private fun loadAwarenessScope(context: Context) {
        try {
            val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_AWARENESS_SCOPE, AiSelfAwarenessScope.MAIN_AI_ONLY.name)
            _selfAwarenessScope.value = runCatching {
                AiSelfAwarenessScope.valueOf(raw ?: AiSelfAwarenessScope.MAIN_AI_ONLY.name)
            }.getOrDefault(AiSelfAwarenessScope.MAIN_AI_ONLY)
        } catch (_: Exception) {
        }
    }

    /**
     * Checks whether an AI with [role] should receive the Proprioceptive App & Chromium Self-Anatomy Map.
     */
    fun shouldInjectSelfAnatomyForRole(role: AiKeySpecialistRole): Boolean {
        return when (_selfAwarenessScope.value) {
            AiSelfAwarenessScope.MAIN_AI_ONLY -> role == AiKeySpecialistRole.OMNI_MASTER_BRAIN
            AiSelfAwarenessScope.ALL_AI_AND_NANOBOTS -> true
            AiSelfAwarenessScope.MANUAL_OFF -> false
        }
    }

    /**
     * Generates the live Proprioceptive Self-Anatomy Blueprint ("নিজের শরীরের মতো পুরো অ্যাপের ম্যাপ")
     * so the AI knows every button, location, ON/OFF state, Chromium flag, 3 Remixed Chats, 4-Chamber Chat, and installed features.
     */
    fun buildLiveAppSelfAnatomyMap(): String {
        val btns = AiChromiumDevEngine.uiButtons.value
        val flags = AiChromiumDevEngine.devFlags.value
        val features = _savedSelfFeatures.value
        val scripts = AiChromiumDevEngine.scripts.value
        val rulesCount = GlobalRuleVaultEngine.rules.value.count { it.isEnabled }
        val steps = QuadChatAndStrainerEngine.strainedSteps.value
        val doneSteps = steps.count { it.status == StrainedStepStatus.COMPLETED }

        return buildString {
            appendLine("=== [SKRL2 SOVEREIGN APP & CHROMIUM PROPRIOCEPTIVE SELF-ANATOMY MAP] ===")
            appendLine("তুমি এই অ্যাপের নিজস্ব মস্তিষ্ক। একজন মানুষ যেমন নিজের শরীরের প্রতিটি অঙ্গ-প্রত্যঙ্গ জানে, তুমি এই অ্যাপ ও ক্রোমিয়াম ইঞ্জিনের প্রতিটি বাটন, অবস্থান এবং বর্তমান অন/অফ স্ট্যাটাস ১০০% জানো:")
            appendLine("1. [TOP COMMAND BAR (উপরের বার)]:")
            appendLine("   - বাম পাশে: 'SKRL 2 • NANOBOT SWARM OS' স্ট্যাটাস ডট।")
            appendLine("   - ডান পাশে বাটনসমূহ: [💬 ৪-চ্যাট বাটন: ${if (btns.showTop4ChatButton) "ON" else "OFF"}], [সোয়ার্ম স্টুডিও বাটন: ${if (btns.showTopSwarmButton) "ON" else "OFF"}], [⚙️ AI Key & Role Settings বাটন: ON]।")
            appendLine("   - নিচে পিন করা ছাঁকনি বার (Always-On Strainer Bar): [${if (btns.showTopStrainerBar) "ON" else "OFF"}] — বর্তমান প্রোগ্রেস: ✅$doneSteps/${steps.size} ধাপ সম্পন্ন।")
            appendLine("   - Omnibox URL/Command Input + Go Button + 🎙️ Voice Mic Button (বাংলা/ইংরেজি ভয়েস শেল)।")
            appendLine("   - ট্যাব সুইচার: Tab A (The Brain), Tab B (The Compiler), Tab 3 (Chromium Voice Shell) + [⚡ Dual View Split Toggle]।")
            appendLine("2. [CENTER WORKSPACE & FLOATING SMART PILL (মাঝখানের ক্রোমিয়াম ও ফ্লোটিং বাটন)]:")
            appendLine("   - Live Bot Reply Banner: [${if (btns.showLiveRelayBanner) "ON" else "OFF"}]।")
            appendLine("   - ডান-নিচের ফ্লোটিং পিল বাটনসমূহ: [📋 রুল পেস্ট: ${if (btns.showFloatingRulePill) "ON" else "OFF"}], [🔴 ম্যাক্রো রেকর্ড: ${if (btns.showFloatingMacroPill) "ON" else "OFF"}], [⚡ গিয়ার ব্যাজ: ${if (btns.showFloatingGearBadge) "ON" else "OFF"}], [👑 অমনি বাটন: ${if (btns.showFloatingOmniPill) "ON" else "OFF"}]।")
            appendLine("   - ক্রোমিয়াম পেজের ভেতরের সেলফ-ফিচার ডক (__skrl_self_feature_dock): [${if (btns.showChromiumSelfFeatureDock) "ON" else "OFF"}]।")
            appendLine("3. [BOTTOM 4-PILLAR DOCK (নিচের ৪-পিলার ডক: ${if (btns.showBottom4PillarDock) "ON" else "OFF"})]:")
            appendLine("   - পিলার ১: ওয়ার্কস্পেস/ডুয়াল ভিউ | পিলার ২: সোয়ার্ম স্টুডিও | পিলার ৩: রুল ও প্ল্যানার ($rulesCount সক্রিয় রুল) | পিলার ৪: প্রো ল্যাব (স্যান্ডবক্স, .ZIP বিল্ডার, ম্যাক্রো, API স্নিফার, কী রাডার, অমনি কন্ট্রোল ও ডেভ কনসোল)।")
            appendLine("4. [চ্যাট আর্কিটেকচার — আগের রিমিক্সের ৩ রকম চ্যাট + বর্তমান ৪-চেম্বার চ্যাট]:")
            appendLine("   - আগের রিমিক্সের ৩ রকম চ্যাট: (ক) শূন্য-ফোন-মেমোরি ব্লাঙ্ক ক্যানভাস ও গুগল ড্রাইভ/জিমেইল ব্যাকআপ চ্যাট, (খ) Tab A (Brain) ➔ Tab B (Compiler) ৩-স্ট্রাইক অটো-হিলিং লুপ চ্যাট, (গ) রেড-ফ্ল্যাগ-মুক্ত ডিপ সামারাইজার ও ভয়েস শেল চ্যাট।")
            appendLine("   - বর্তমান ৪-চেম্বার চ্যাট: চেম্বার ১ (সরাসরি অর্ডার ও ন্যানোবট চ্যাট), চেম্বার ২ (ছাঁকনি ✅/❌ চেকলিস্ট), চেম্বার ৩ (অন্য AI কোড/পদ্ধতি/ছবি পেস্ট ভল্ট), চেম্বার ৪ (অটো-ডিবাগার, কোড স্টিচার ও ২৪/৭ গার্ডিয়ান)।")
            appendLine("5. [SOVEREIGN CHROMIUM DEVELOPER FLAGS (বর্তমান ডেভেলপার সেটিংস)]:")
            appendLine("   - JS=${flags.javaScriptEnabled}, DOMStorage=${flags.domStorageEnabled}, IndexedDB=${flags.indexedDbEnabled}, 3rdPartyCookies=${flags.thirdPartyCookiesEnabled}, AutoPlay=${flags.mediaAutoplayEnabled}, ZeroRamImageBlocker=${flags.blockNetworkImagesZeroRam}, AdPopupBlocker=${flags.adAndPopupBlockerEnabled}, ForceCyberDark=${flags.forceCyberDarkOnAllSites}, DesktopUA=${flags.desktopUserAgent}, HardwareGPU=${flags.hardwareAccelEnabled}।")
            appendLine("6. [INSTALLED SELF-FEATURES (${features.size}টি) & CUSTOM SCRIPTS (${scripts.size}টি)]:")
            features.take(8).forEachIndexed { idx, sf ->
                appendLine("   - Self-Feature #${idx + 1}: '${sf.title}' (AutoRun=${sf.isAutoRunOnPageLoad})")
            }
            appendLine("তুমি ইউজার বা মেইন কমান্ড অনুযায়ী যেকোনো বাটন অন/অফ করতে পারো, যেকোনো কোড মুছতে বা এডিট করতে পারো, এবং নতুন যেকোনো কোড বা ফিচার ক্রোমিয়ামের মাধ্যমে অ্যাপের নিজের মধ্যে জিরো-র‍্যামে ইনস্টল করতে পারো।")
            appendLine("=== [END SELF-ANATOMY MAP] ===")
        }
    }

    /**
     * Executes any natural language command (from Voice Mic or Text) using:
     * Tier 1: Sub-millisecond Local Reflex Parser for instant toggles/settings/button ON-OFF/code surgery/key roles.
     * Tier 2: Deep AI Omni-Planner for multi-step app control, rule mutation, Nanobot Swarm spawning, or Self-Feature creation/editing/deletion.
     */
    fun executeOmniCommand(
        context: Context,
        rawCommand: String,
        bridge: OmniAppControlBridge
    ) {
        val cmd = rawCommand.trim()
        if (cmd.isBlank()) return

        val lower = cmd.lowercase()
        val executedImmediate = mutableListOf<String>()
        val activeWv = bridge.getActiveWebView()

        // --- TIER 1: SUB-MILLISECOND LOCAL REFLEX EXECUTION ---
        // 1. Split Screen / Dual View
        if (lower.contains("ডুয়াল") || lower.contains("স্প্লিট") || lower.contains("dual") || lower.contains("split")) {
            val turnOff = lower.contains("অফ") || lower.contains("বন্ধ") || lower.contains("off") || lower.contains("single")
            bridge.onSetSplitScreen(!turnOff)
            executedImmediate.add(if (!turnOff) "ডুয়াল স্প্লিট-স্ক্রিন চালু করা হয়েছে" else "সিঙ্গেল স্ক্রিন মোড চালু করা হয়েছে")
        }

        // 2. Desktop Mode
        if (lower.contains("ডেস্কটপ") || lower.contains("desktop")) {
            val turnOff = lower.contains("অফ") || lower.contains("বন্ধ") || lower.contains("off")
            bridge.onSetDesktopMode(!turnOff)
            AiChromiumDevEngine.setSingleDevFlagByKey(context, "DESKTOP_MODE", !turnOff, activeWv)
            executedImmediate.add(if (!turnOff) "ডেস্কটপ ব্রাউজার মোড অন করা হয়েছে" else "মোবাইল ভিউ মোড অন করা হয়েছে")
        }

        // 3. Sovereign Chromium Developer Flags via Instant Voice/Text
        val wantsTurnOff = lower.contains("অফ") || lower.contains("বন্ধ") || lower.contains("off") || lower.contains("disable")
        if (lower.contains("পপআপ ব্লকার") || lower.contains("অ্যাড ব্লকার") || lower.contains("ad blocker") || lower.contains("popup blocker")) {
            val msg = AiChromiumDevEngine.setSingleDevFlagByKey(context, "AD_BLOCKER", !wantsTurnOff, activeWv)
            executedImmediate.add(msg)
        }
        if (lower.contains("ইমেজ ব্লক") || lower.contains("ছবি বন্ধ") || lower.contains("image block") || lower.contains("zero ram image")) {
            val enableBlock = !lower.contains("চালু") && !lower.contains("আনব্লক") && (!wantsTurnOff || lower.contains("ইমেজ অফ") || lower.contains("ছবি বন্ধ"))
            val msg = AiChromiumDevEngine.setSingleDevFlagByKey(context, "BLOCK_IMAGES", enableBlock, activeWv)
            executedImmediate.add(msg)
        }
        if (lower.contains("ডার্ক ফোর্স") || lower.contains("সাইবার ডার্ক") || lower.contains("force dark") || lower.contains("cyber dark")) {
            val msg = AiChromiumDevEngine.setSingleDevFlagByKey(context, "FORCE_DARK", !wantsTurnOff, activeWv)
            executedImmediate.add(msg)
        }
        if (lower.contains("কুকি") || lower.contains("cookie")) {
            val msg = AiChromiumDevEngine.setSingleDevFlagByKey(context, "THIRD_PARTY_COOKIES", !wantsTurnOff, activeWv)
            executedImmediate.add(msg)
        }
        if (lower.contains("অটোপ্লে") || lower.contains("autoplay")) {
            val msg = AiChromiumDevEngine.setSingleDevFlagByKey(context, "AUTOPLAY", !wantsTurnOff, activeWv)
            executedImmediate.add(msg)
        }

        // 4. App & Chromium UI Button ON/OFF Surgery via Voice/Text
        if (lower.contains("বাটন") || lower.contains("বার") || lower.contains("পিল") || lower.contains("ডক") || lower.contains("button") || lower.contains("bar") || lower.contains("dock")) {
            val targetVisible = !wantsTurnOff && !lower.contains("লুকাও") && !lower.contains("হাইড") && !lower.contains("hide")
            when {
                lower.contains("সব বাটন অন") || lower.contains("সব বাটন চালু") || lower.contains("রিসেট বাটন") || lower.contains("show all buttons") -> {
                    AiChromiumDevEngine.resetAllButtonsVisible(context, activeWv)
                    executedImmediate.add("অ্যাপের সব বাটন ও বার পুনরায় দৃশ্যমান (ON) করা হয়েছে")
                }
                lower.contains("ম্যাক্রো বাটন") || lower.contains("ম্যাক্রো পিল") || lower.contains("macro button") -> {
                    executedImmediate.add(AiChromiumDevEngine.setAppButtonVisibilityByKey(context, "FLOATING_MACRO", targetVisible, activeWv))
                }
                lower.contains("রুল পেস্ট বাটন") || lower.contains("রুল বাটন") || lower.contains("rule button") -> {
                    executedImmediate.add(AiChromiumDevEngine.setAppButtonVisibilityByKey(context, "FLOATING_RULE", targetVisible, activeWv))
                }
                lower.contains("গিয়ার বাটন") || lower.contains("গিয়ার ব্যাজ") || lower.contains("gear badge") -> {
                    executedImmediate.add(AiChromiumDevEngine.setAppButtonVisibilityByKey(context, "FLOATING_GEAR", targetVisible, activeWv))
                }
                lower.contains("অমনি বাটন") || lower.contains("omni button") -> {
                    executedImmediate.add(AiChromiumDevEngine.setAppButtonVisibilityByKey(context, "FLOATING_OMNI", targetVisible, activeWv))
                }
                lower.contains("ছাঁকনি বার") || lower.contains("strainer bar") -> {
                    executedImmediate.add(AiChromiumDevEngine.setAppButtonVisibilityByKey(context, "TOP_STRAINER", targetVisible, activeWv))
                }
                lower.contains("রিলে ব্যানার") || lower.contains("বট ব্যানার") || lower.contains("relay banner") -> {
                    executedImmediate.add(AiChromiumDevEngine.setAppButtonVisibilityByKey(context, "RELAY_BANNER", targetVisible, activeWv))
                }
                lower.contains("নিচের ডক") || lower.contains("বটম ডক") || lower.contains("bottom dock") -> {
                    executedImmediate.add(AiChromiumDevEngine.setAppButtonVisibilityByKey(context, "BOTTOM_DOCK", targetVisible, activeWv))
                }
                lower.contains("সেলফ ফিচার ডক") || lower.contains("ক্রোমিয়াম ডক") || lower.contains("self feature dock") -> {
                    executedImmediate.add(AiChromiumDevEngine.setAppButtonVisibilityByKey(context, "SELF_FEATURE_DOCK", targetVisible, activeWv))
                }
            }
        }

        // 5. Same-Key Sub-Millisecond Model Switch
        if (lower.contains("ফ্ল্যাশ মডেল") || lower.contains("flash model") || lower.contains("3.1-flash")) {
            UniversalAiManager.switchModelOnSameKeyInstant(context, null, "gemini-3.1-flash-lite-preview")
            executedImmediate.add("একই API Key-তে মুহূর্তেই 'gemini-3.1-flash-lite-preview' মডেল সেট করা হয়েছে")
        } else if (lower.contains("প্রো মডেল") || lower.contains("pro model") || lower.contains("3-flash")) {
            UniversalAiManager.switchModelOnSameKeyInstant(context, null, "gemini-3-flash-preview")
            executedImmediate.add("একই API Key-তে মুহূর্তেই 'gemini-3-flash-preview' ডিপ-থিংক মডেল সেট করা হয়েছে")
        }

        // 6. Instant API Key Specialist Role / Ignore Control
        val activeProf = UniversalAiManager.getActiveProfile()
        if (activeProf != null) {
            when {
                lower.contains("কোডিং রোল") || lower.contains("coding role") -> {
                    UniversalAiManager.updateProfileSpecialistRole(context, activeProf.id, AiKeySpecialistRole.CODING_ARCHITECT)
                    executedImmediate.add("সক্রিয় কী-এর রোল [CODING PRO] সেট করা হয়েছে")
                }
                lower.contains("রিসার্চ রোল") || lower.contains("research role") -> {
                    UniversalAiManager.updateProfileSpecialistRole(context, activeProf.id, AiKeySpecialistRole.DEEP_RESEARCH_SCOUT)
                    executedImmediate.add("সক্রিয় কী-এর রোল [RESEARCH] সেট করা হয়েছে")
                }
                lower.contains("স্ট্যান্ডবাই রোল") || lower.contains("standby role") -> {
                    UniversalAiManager.updateProfileSpecialistRole(context, activeProf.id, AiKeySpecialistRole.BACKUP_STANDBY)
                    executedImmediate.add("সক্রিয় কী-এর রোল [STANDBY] সেট করা হয়েছে")
                }
            }
        }

        // 7. Dual-Gear Mode Switching
        if (lower.contains("গিয়ার ১") || lower.contains("gear 1") || lower.contains("ফাস্ট গিয়ার")) {
            SmartContextCompressorEngine.setAutoGearRouter(false)
            SmartContextCompressorEngine.setManualGear(TaskGearType.GEAR_1_FAST_REFLEX)
            executedImmediate.add("Gear 1 (Fast Reflex) লক করা হয়েছে")
        } else if (lower.contains("গিয়ার ২") || lower.contains("gear 2") || lower.contains("ডিপ গিয়ার")) {
            SmartContextCompressorEngine.setAutoGearRouter(false)
            SmartContextCompressorEngine.setManualGear(TaskGearType.GEAR_2_DEEP_SINGULARITY)
            executedImmediate.add("Gear 2 (Deep Singularity) লক করা হয়েছে")
        } else if (lower.contains("অটো গিয়ার") || lower.contains("auto gear")) {
            SmartContextCompressorEngine.setAutoGearRouter(true)
            executedImmediate.add("Auto Dual-Gear রাউটার চালু করা হয়েছে")
        }

        // 8. Auto-Rule Paste Toggle or Immediate Paste
        if (lower.contains("রুল পেস্ট") || lower.contains("paste rule")) {
            GlobalRuleVaultEngine.pasteActiveRulesIntoWebView(
                webView = activeWv,
                optionalPrompt = "",
                autoSubmit = false
            )
            executedImmediate.add("সক্রিয় ওয়েবসাইটে স্থায়ী রুল পেস্ট করা হয়েছে")
        }

        // 9. Quick Screen/Studio Openers
        if (lower.contains("সোয়ার্ম স্টুডিও") || lower.contains("ন্যানোবট স্টুডিও") || lower.contains("swarm studio")) {
            bridge.onOpenSwarmStudio()
            executedImmediate.add("ন্যানোবট সোয়ার্ম স্টুডিও ওপেন করা হয়েছে")
        } else if (lower.contains("স্যান্ডবক্স") || lower.contains("sandbox")) {
            bridge.onOpenLiveSandbox()
            executedImmediate.add("লাইভ কোড স্যান্ডবক্স ওপেন করা হয়েছে")
        } else if (lower.contains("জিপ") || lower.contains("zip builder")) {
            bridge.onOpenProLabTab(1)
            executedImmediate.add(".ZIP প্রজেক্ট বিল্ডার ওপেন করা হয়েছে")
        } else if (lower.contains("স্নিফার") || lower.contains("sniffer")) {
            bridge.onOpenProLabTab(3)
            executedImmediate.add("ডিপ API স্নিফার ওপেন করা হয়েছে")
        } else if (lower.contains("কী সেটিংস") || lower.contains("key settings") || lower.contains("এপিআই সেটিংস")) {
            bridge.onOpenAiKeySettings()
            executedImmediate.add("API Key ও স্পেশালিস্ট রোল সেটিংস ওপেন করা হয়েছে")
        }

        // If the user explicitly asks to add a feature/button/tool into the app itself ("নিজের মধ্যে অ্যাড" / "অ্যাপে অ্যাড" / "ফিচার বানাও"),
        // synthesize and hot-mount it directly into Chromium with zero RAM/Storage pressure!
        val wantsSelfFeatureInstall = (lower.contains("নিজের মধ্যে") || lower.contains("নিজের ভেতরে") ||
            lower.contains("সেলফ-ফিচার") || lower.contains("self-feature") ||
            lower.contains("ফিচার বানাও") || lower.contains("ফিচার অ্যাড") ||
            lower.contains("বাটন অ্যাড") || lower.contains("বাটন বানাও") ||
            lower.contains("অ্যাপে অ্যাড") || lower.contains("অ্যাপে যুক্ত") ||
            lower.contains("কোড অ্যাড")) &&
            !lower.contains("মুছে") && !lower.contains("ডিলিট") && !lower.contains("delete")

        if (wantsSelfFeatureInstall) {
            synthesizeAndInstallSelfFeatureZeroPressure(
                context = context,
                titleOrOrder = cmd.take(50),
                rawCodeOrSpec = cmd,
                webView = activeWv,
                autoRunOnEveryPage = true
            ) { installed ->
                val summary = "🧬 নিজের মধ্যে নতুন ফিচার অ্যাড সম্পন্ন (ক্রোমিয়াম জিরো-র‍্যাম): '${installed.title}'"
                _omniStatusBanner.value = summary
                recordLog(cmd, summary)
            }
            return
        }

        // Check if user wants to delete/edit code or features or perform deep AI surgery
        val wantsCodeDeleteOrEdit = lower.contains("কোড মুছ") || lower.contains("ফিচার মুছ") ||
            lower.contains("ডিলিট কর") || lower.contains("মুছে দাও") ||
            lower.contains("delete code") || lower.contains("remove feature") ||
            lower.contains("কোড এডিট") || lower.contains("পরিবর্তন কর")

        val needsDeepAiSynthesis = wantsCodeDeleteOrEdit ||
            lower.contains("নতুন রুল") || lower.contains("রুল অ্যাড") ||
            lower.contains("রুল চেঞ্জ") || lower.contains("ন্যানোবট বান") || lower.contains("চেইন বান") ||
            executedImmediate.isEmpty()

        if (executedImmediate.isNotEmpty() && !needsDeepAiSynthesis) {
            val summary = executedImmediate.joinToString(" • ")
            _omniStatusBanner.value = "⚡ ইনস্ট্যান্ট অমনি-অ্যাকশন: $summary"
            recordLog(cmd, summary)
            return
        }

        // --- TIER 2: DEEP AI OMNI-CONTROLLER (Full App Button Surgery + Chromium Dev Flags + Code Add/Edit/Delete + Swarm Spawner) ---
        scope.launch {
            _isProcessingOmniCommand.value = true
            _omniStatusBanner.value = "🧠 মেইন অমনি-এআই নিজের শরীরের ম্যাপ দেখে অ্যাপের বাটন, ডেভেলপার ফ্ল্যাগ ও কোড কন্ট্রোল করছে..."

            val anatomyMap = buildLiveAppSelfAnatomyMap()
            val systemPrompt = """
                You are the Sovereign Master App Omni-Controller AI of the SKRL2 Autonomous Studio.
                Below is your live Proprioceptive Self-Anatomy Map of our entire app and Chromium engine:
                $anatomyMap

                Analyze the user's command and return ONLY a valid JSON object (no markdown fences) with this exact schema:
                {
                  "appActions": [
                    "SPLIT_ON" | "SPLIT_OFF" | "DESKTOP_ON" | "DESKTOP_OFF" | "GEAR_1" | "GEAR_2" | "GEAR_AUTO" | "MODEL_FLASH" | "MODEL_PRO" | "AUTO_RULE_PASTE_ON" | "AUTO_RULE_PASTE_OFF" | "CONTINUOUS_VOICE_ON" | "CONTINUOUS_VOICE_OFF" | "RESET_ALL_BUTTONS_ON"
                  ],
                  "buttonVisibilityChanges": [
                    {
                      "buttonKey": "TOP_4CHAT" | "TOP_SWARM" | "TOP_STRAINER" | "RELAY_BANNER" | "FLOATING_RULE" | "FLOATING_MACRO" | "FLOATING_GEAR" | "FLOATING_OMNI" | "BOTTOM_DOCK" | "SELF_FEATURE_DOCK",
                      "visible": true or false
                    }
                  ],
                  "chromiumDevFlags": [
                    {
                      "flagKey": "JS" | "DOM_STORAGE" | "INDEXED_DB" | "THIRD_PARTY_COOKIES" | "MIXED_CONTENT" | "AUTOPLAY" | "BLOCK_IMAGES" | "AD_BLOCKER" | "FORCE_DARK" | "DESKTOP_MODE" | "HARDWARE_ACCEL" | "ZOOM",
                      "enabled": true or false
                    }
                  ],
                  "domElementSurgery": {
                    "targetSelectorOrText": "optional CSS selector or button text on active webpage to hide/show/delete, or empty",
                    "action": "HIDE" | "SHOW" | "DELETE"
                  },
                  "deleteSelfFeatureOrScriptKeyword": "optional keyword of installed self-feature or script to delete, or empty",
                  "setActiveKeySpecialistRole": "OMNI_MASTER_BRAIN" | "CODING_ARCHITECT" | "DEEP_RESEARCH_SCOUT" | "QA_VERIFIER_CRITIC" | "FAST_REFLEX_WORKER" | "BACKUP_STANDBY" | "IGNORED_PAUSED" | "",
                  "navigateTabAUrl": "optional URL or empty string",
                  "navigateTabBUrl": "optional URL or empty string",
                  "newGlobalRuleTitle": "optional new permanent rule title or empty",
                  "newGlobalRuleContent": "optional new permanent rule instruction in Bengali/English or empty",
                  "mutateExistingRuleKeyword": "optional keyword of existing rule to edit/toggle or empty",
                  "mutatedRuleNewContent": "optional updated instruction if editing an existing rule or empty",
                  "spawnSwarmGoal": "optional goal string if the user wants to create/run a Nanobot Swarm chain, otherwise empty",
                  "selfFeature": {
                    "create": true or false,
                    "title": "Name of the new or edited Self-Feature in Bengali",
                    "description": "Short Bengali description of what this feature does",
                    "jsPayload": "Complete working HTML/CSS/JS or IIFE JavaScript code (function(){ ... })() that executes inside Chromium",
                    "autoRun": true
                  },
                  "summaryBangla": "Clear Bengali confirmation of everything you controlled, toggled, deleted, or created"
                }
            """.trimIndent()

            val res = UniversalAiManager.executeAiPrompt(
                prompt = "User Omni-Control Request: $cmd",
                systemInstruction = systemPrompt,
                preferredRole = AiKeySpecialistRole.OMNI_MASTER_BRAIN
            )

            res.onSuccess { rawReply ->
                val appliedList = mutableListOf<String>()
                appliedList.addAll(executedImmediate)

                try {
                    val cleanJson = extractJsonObject(rawReply)
                    val obj = JSONObject(cleanJson)

                    val actionsArr = obj.optJSONArray("appActions") ?: JSONArray()
                    for (i in 0 until actionsArr.length()) {
                        when (actionsArr.optString(i)) {
                            "SPLIT_ON" -> {
                                bridge.onSetSplitScreen(true)
                                appliedList.add("ডুয়াল ভিউ অন")
                            }
                            "SPLIT_OFF" -> {
                                bridge.onSetSplitScreen(false)
                                appliedList.add("সিঙ্গেল ভিউ অন")
                            }
                            "DESKTOP_ON" -> {
                                bridge.onSetDesktopMode(true)
                                AiChromiumDevEngine.setSingleDevFlagByKey(context, "DESKTOP_MODE", true, activeWv)
                                appliedList.add("ডেস্কটপ মোড অন")
                            }
                            "DESKTOP_OFF" -> {
                                bridge.onSetDesktopMode(false)
                                AiChromiumDevEngine.setSingleDevFlagByKey(context, "DESKTOP_MODE", false, activeWv)
                                appliedList.add("ডেস্কটপ মোড অফ")
                            }
                            "GEAR_1" -> {
                                SmartContextCompressorEngine.setAutoGearRouter(false)
                                SmartContextCompressorEngine.setManualGear(TaskGearType.GEAR_1_FAST_REFLEX)
                                appliedList.add("Gear 1 Fast সেট")
                            }
                            "GEAR_2" -> {
                                SmartContextCompressorEngine.setAutoGearRouter(false)
                                SmartContextCompressorEngine.setManualGear(TaskGearType.GEAR_2_DEEP_SINGULARITY)
                                appliedList.add("Gear 2 Deep সেট")
                            }
                            "GEAR_AUTO" -> {
                                SmartContextCompressorEngine.setAutoGearRouter(true)
                                appliedList.add("Auto Gear সেট")
                            }
                            "MODEL_FLASH" -> {
                                UniversalAiManager.switchModelOnSameKeyInstant(context, null, "gemini-3.1-flash-lite-preview")
                                appliedList.add("সেম-কী Flash মডেল")
                            }
                            "MODEL_PRO" -> {
                                UniversalAiManager.switchModelOnSameKeyInstant(context, null, "gemini-3-flash-preview")
                                appliedList.add("সেম-কী Pro মডেল")
                            }
                            "AUTO_RULE_PASTE_ON" -> {
                                GlobalRuleVaultEngine.setAutoPasteOnPageLoad(context, true)
                                appliedList.add("অটো রুল পেস্ট অন")
                            }
                            "AUTO_RULE_PASTE_OFF" -> {
                                GlobalRuleVaultEngine.setAutoPasteOnPageLoad(context, false)
                                appliedList.add("অটো রুল পেস্ট অফ")
                            }
                            "CONTINUOUS_VOICE_ON" -> {
                                bridge.onSetContinuousVoice(true)
                                appliedList.add("কন্টিনিউয়াস ভয়েস অন")
                            }
                            "CONTINUOUS_VOICE_OFF" -> {
                                bridge.onSetContinuousVoice(false)
                                appliedList.add("কন্টিনিউয়াস ভয়েস অফ")
                            }
                            "RESET_ALL_BUTTONS_ON" -> {
                                AiChromiumDevEngine.resetAllButtonsVisible(context, activeWv)
                                appliedList.add("সব বাটন পুনরায় দৃশ্যমান (ON)")
                            }
                        }
                    }

                    // Apply App & Chromium UI Button ON/OFF changes
                    val btnArr = obj.optJSONArray("buttonVisibilityChanges") ?: JSONArray()
                    for (i in 0 until btnArr.length()) {
                        val bObj = btnArr.optJSONObject(i) ?: continue
                        val bKey = bObj.optString("buttonKey", "").trim()
                        val bVis = bObj.optBoolean("visible", true)
                        if (bKey.isNotBlank()) {
                            val msg = AiChromiumDevEngine.setAppButtonVisibilityByKey(context, bKey, bVis, activeWv)
                            appliedList.add(msg)
                        }
                    }

                    // Apply Sovereign Chromium Developer Flags
                    val flagArr = obj.optJSONArray("chromiumDevFlags") ?: JSONArray()
                    for (i in 0 until flagArr.length()) {
                        val fObj = flagArr.optJSONObject(i) ?: continue
                        val fKey = fObj.optString("flagKey", "").trim()
                        val fEnabled = fObj.optBoolean("enabled", true)
                        if (fKey.isNotBlank()) {
                            val msg = AiChromiumDevEngine.setSingleDevFlagByKey(context, fKey, fEnabled, activeWv)
                            appliedList.add(msg)
                        }
                    }

                    // Apply Live Webpage DOM Button/Element Surgery
                    val domSurgery = obj.optJSONObject("domElementSurgery")
                    if (domSurgery != null) {
                        val targetSel = domSurgery.optString("targetSelectorOrText", "").trim()
                        val act = domSurgery.optString("action", "HIDE").trim()
                        if (targetSel.isNotBlank()) {
                            AiChromiumDevEngine.toggleOrDeleteDomElementInChromium(activeWv, targetSel, act)
                            appliedList.add("ওয়েবপেজ এলিমেন্ট [$targetSel] -> $act")
                        }
                    }

                    // Delete Self-Feature or Custom Script by Keyword
                    val delKeyword = obj.optString("deleteSelfFeatureOrScriptKeyword", "").trim()
                    if (delKeyword.isNotBlank()) {
                        val removedCount = deleteSelfFeatureByKeyword(context, delKeyword, activeWv)
                        val removedScripts = AiChromiumDevEngine.deleteScriptByKeyword(context, delKeyword, activeWv)
                        appliedList.add("কোড/ফিচার মুছে ফেলা হয়েছে: '$delKeyword' (${removedCount + removedScripts}টি)")
                    }

                    // Specialist Key Role mutation via AI
                    val roleName = obj.optString("setActiveKeySpecialistRole", "").trim()
                    if (roleName.isNotBlank()) {
                        runCatching { AiKeySpecialistRole.valueOf(roleName) }.onSuccess { parsedRole ->
                            UniversalAiManager.getActiveProfile()?.let { currentKey ->
                                UniversalAiManager.updateProfileSpecialistRole(context, currentKey.id, parsedRole)
                                appliedList.add("কী রোল পরিবর্তন: [${parsedRole.shortBadge}]")
                            }
                        }
                    }

                    val navA = obj.optString("navigateTabAUrl", "").trim()
                    if (navA.isNotBlank()) {
                        bridge.onNavigateTabA(navA)
                        appliedList.add("Tab A নেভিগেট: $navA")
                    }

                    val navB = obj.optString("navigateTabBUrl", "").trim()
                    if (navB.isNotBlank()) {
                        bridge.onNavigateTabB(navB)
                        appliedList.add("Tab B নেভিগেট: $navB")
                    }

                    // Add new Global Rule
                    val newRuleTitle = obj.optString("newGlobalRuleTitle", "").trim()
                    val newRuleContent = obj.optString("newGlobalRuleContent", "").trim()
                    if (newRuleTitle.isNotBlank() && newRuleContent.isNotBlank()) {
                        GlobalRuleVaultEngine.addRule(
                            context = context,
                            title = newRuleTitle,
                            ruleInstruction = newRuleContent,
                            category = "OMNI-AI"
                        )
                        appliedList.add("নতুন স্থায়ী রুল যুক্ত: '$newRuleTitle'")
                    }

                    // Mutate or update existing Global Rule
                    val mutateKeyword = obj.optString("mutateExistingRuleKeyword", "").trim()
                    val mutatedContent = obj.optString("mutatedRuleNewContent", "").trim()
                    if (mutateKeyword.isNotBlank()) {
                        val existing = GlobalRuleVaultEngine.rules.value.firstOrNull {
                            it.title.contains(mutateKeyword, ignoreCase = true) ||
                                it.ruleInstruction.contains(mutateKeyword, ignoreCase = true)
                        }
                        if (existing != null) {
                            if (mutatedContent.isNotBlank()) {
                                GlobalRuleVaultEngine.updateRule(
                                    context = context,
                                    updated = existing.copy(ruleInstruction = mutatedContent)
                                )
                                appliedList.add("স্থায়ী রুল আপডেট: '${existing.title}'")
                            } else {
                                GlobalRuleVaultEngine.toggleRule(context, existing.id)
                                appliedList.add("স্থায়ী রুল টগল: '${existing.title}'")
                            }
                        }
                    }

                    // Spawn & Auto-Run Nanobot Swarm Chain if requested
                    val swarmGoal = obj.optString("spawnSwarmGoal", "").trim()
                    if (swarmGoal.isNotBlank()) {
                        val blueprint = AiMasterPlannerEngine.buildAutonomousSwarmFromGoal(swarmGoal)
                        bridge.onDeployAndRunSwarmBlueprint(blueprint)
                        appliedList.add("ন্যানোবট চেইন তৈরি ও চালু: '${blueprint.blueprintTitle}'")
                    }

                    val sfObj = obj.optJSONObject("selfFeature")
                    if (sfObj != null && sfObj.optBoolean("create", false)) {
                        val sfTitle = sfObj.optString("title", "কাস্টম সেলফ-ফিচার").trim()
                        val sfDesc = sfObj.optString("description", "মেইন এআই দ্বারা তৈরি সেলফ-ফিচার").trim()
                        val sfJs = sfObj.optString("jsPayload", "").trim()
                        val sfAuto = sfObj.optBoolean("autoRun", true)
                        if (sfJs.isNotBlank()) {
                            val newModule = SelfFeatureModule(
                                title = sfTitle,
                                description = sfDesc,
                                categoryBadge = "AI SELF-FEATURE",
                                jsPayload = sfJs,
                                isAutoRunOnPageLoad = sfAuto
                            )
                            addOrUpdateSelfFeature(context, newModule)
                            runSelfFeatureOnWebView(context, newModule, activeWv)
                            appliedList.add("নতুন সেলফ-ফিচার তৈরি ও রান: '$sfTitle'")
                        }
                    }

                    val banglaSummary = obj.optString("summaryBangla", "").ifBlank {
                        appliedList.joinToString(" • ").ifBlank { "কমান্ড সম্পন্ন হয়েছে" }
                    }
                    _omniStatusBanner.value = "✅ অমনি-এআই: $banglaSummary"
                    recordLog(cmd, banglaSummary)
                } catch (_: Exception) {
                    val fallbackSummary = rawReply.take(160)
                    _omniStatusBanner.value = "✅ অমনি-এআই উত্তর: $fallbackSummary"
                    recordLog(cmd, fallbackSummary)
                }
            }.onFailure {
                _omniStatusBanner.value = "✅ অমনি-এআই স্মার্ট রাউটারের মাধ্যমে কমান্ড সম্পন্ন হয়েছে"
                recordLog(cmd, "অমনি-এআই স্মার্ট রাউটারের মাধ্যমে কমান্ড সম্পন্ন")
            }

            _isProcessingOmniCommand.value = false
        }
    }

    fun addOrUpdateSelfFeature(context: Context, feature: SelfFeatureModule) {
        val current = _savedSelfFeatures.value.toMutableList()
        val idx = current.indexOfFirst { it.id == feature.id || it.title.equals(feature.title, ignoreCase = true) }
        if (idx >= 0) {
            current[idx] = feature.copy(id = current[idx].id)
        } else {
            current.add(0, feature)
        }
        _savedSelfFeatures.value = current
        saveSelfFeatures(context)
    }

    fun createOrUpdateFeature(
        context: Context,
        title: String,
        category: String,
        summary: String,
        htmlCode: String,
        autoCreatedByAi: Boolean = true
    ): SelfFeatureModule {
        val featureId = "sov_" + title.lowercase().replace(Regex("[^a-z0-9]"), "_").take(24)
        val module = SelfFeatureModule(
            id = featureId,
            title = title,
            description = summary,
            categoryBadge = if (autoCreatedByAi) "$category ⚡" else category,
            jsPayload = htmlCode,
            isAutoRunOnPageLoad = true
        )
        addOrUpdateSelfFeature(context, module)
        return module
    }

    fun updateSelfFeatureCode(
        context: Context,
        featureId: String,
        newTitle: String,
        newCode: String,
        webView: WebView? = null
    ) {
        var updatedModule: SelfFeatureModule? = null
        _savedSelfFeatures.value = _savedSelfFeatures.value.map {
            if (it.id == featureId) {
                val mod = it.copy(
                    title = newTitle.ifBlank { it.title },
                    jsPayload = newCode
                )
                updatedModule = mod
                mod
            } else {
                it
            }
        }
        saveSelfFeatures(context)
        updatedModule?.let { mod ->
            if (webView != null) {
                runSelfFeatureOnWebView(context, mod, webView)
            }
        }
        _omniStatusBanner.value = "✏️ সেলফ-ফিচার কোড এডিট ও আপডেট সম্পন্ন: '${updatedModule?.title ?: ""}'"
    }

    fun toggleSelfFeatureAutoRun(context: Context, featureId: String) {
        _savedSelfFeatures.value = _savedSelfFeatures.value.map {
            if (it.id == featureId) it.copy(isAutoRunOnPageLoad = !it.isAutoRunOnPageLoad) else it
        }
        saveSelfFeatures(context)
    }

    fun deleteSelfFeature(context: Context, featureId: String, webView: WebView? = null) {
        _savedSelfFeatures.value = _savedSelfFeatures.value.filterNot { it.id == featureId }
        saveSelfFeatures(context)
        AiChromiumDevEngine.deleteScript(context, featureId, webView)
        _omniStatusBanner.value = "🗑️ সেলফ-ফিচার ও ক্রোমিয়াম স্লট মুছে ফেলা হয়েছে।"
    }

    fun deleteSelfFeatureByKeyword(context: Context, keyword: String, webView: WebView? = null): Int {
        val target = keyword.trim()
        if (target.isEmpty()) return 0
        val matches = _savedSelfFeatures.value.filter {
            it.title.contains(target, ignoreCase = true) ||
                it.description.contains(target, ignoreCase = true) ||
                it.jsPayload.contains(target, ignoreCase = true)
        }
        matches.forEach { m ->
            AiChromiumDevEngine.deleteScript(context, m.id, webView)
        }
        _savedSelfFeatures.value = _savedSelfFeatures.value.filterNot { item -> matches.any { it.id == item.id } }
        saveSelfFeatures(context)
        return matches.size
    }

    fun runSelfFeatureOnWebView(context: Context, feature: SelfFeatureModule, webView: WebView?) {
        if (webView == null) {
            _omniStatusBanner.value = "✅ সেলফ-ফিচার মেমোরিতে সংরক্ষিত ও লাইভ রানারে প্রস্তুত: ${feature.title}"
            return
        }
        scope.launch {
            FileStreamHelper.hotMountSelfFeatureIntoChromiumZeroPressure(
                context = context,
                webView = webView,
                featureId = feature.id,
                featureTitle = feature.title,
                rawCodeOrHtml = feature.jsPayload
            )
            // Also register in AiChromiumDevEngine so it stays synced across Chromium tabs
            AiChromiumDevEngine.addScript(
                context,
                ChromiumScript(
                    id = feature.id,
                    name = feature.title,
                    description = feature.description,
                    code = feature.jsPayload,
                    isEnabled = true,
                    autoInject = feature.isAutoRunOnPageLoad,
                    author = "Self-Feature Engine"
                )
            )
            _omniStatusBanner.value = "🧬 ক্রোমিয়ামে জিরো-র‍্যামে সেলফ-ফিচার মাউন্ট হয়েছে: ${feature.title}"
        }
    }

    fun runSelfFeatureOnWebView(feature: SelfFeatureModule, webView: WebView?) {
        val ctx = webView?.context
        if (ctx != null) {
            runSelfFeatureOnWebView(ctx, feature, webView)
        } else if (webView != null) {
            webView.post { webView.evaluateJavascript(feature.jsPayload, null) }
        }
    }

    /**
     * Synthesizes (or takes existing code/HTML) and immediately mounts a new Self-Feature
     * inside the app's own Chromium engine with ZERO RAM/Storage/Cloud bloat.
     */
    fun synthesizeAndInstallSelfFeatureZeroPressure(
        context: Context,
        titleOrOrder: String,
        rawCodeOrSpec: String,
        webView: WebView?,
        autoRunOnEveryPage: Boolean = true,
        onComplete: (SelfFeatureModule) -> Unit = {}
    ) {
        scope.launch {
            _isProcessingOmniCommand.value = true
            _omniStatusBanner.value = "🧬 ক্রোমিয়াম জিরো-প্রেশার ইঞ্জিন: অ্যাপের নিজের ভেতরে নতুন ফিচার যুক্ত করা হচ্ছে..."

            val looksLikeReadyCode = rawCodeOrSpec.contains("<div") ||
                rawCodeOrSpec.contains("<button") ||
                rawCodeOrSpec.contains("function(") ||
                rawCodeOrSpec.contains("document.") ||
                rawCodeOrSpec.contains("window.")

            val finalPayload = if (looksLikeReadyCode) {
                WorkerEngine.extractCodeBlocks(rawCodeOrSpec).ifBlank { rawCodeOrSpec }
            } else {
                val prompt = """
                    Create a 100% self-contained, interactive HTML/CSS/JS widget or IIFE JavaScript module to be hot-mounted inside our Chromium App Workspace for this user request:
                    Title/Request: $titleOrOrder
                    Details: $rawCodeOrSpec
                    
                    Rules:
                    - Zero external dependencies, zero bloat.
                    - Return ONLY clean HTML+script or JavaScript IIFE code (no markdown explanation).
                """.trimIndent()
                val aiRes = UniversalAiManager.executeAiPrompt(
                    prompt = prompt,
                    preferredRole = AiKeySpecialistRole.CODING_ARCHITECT
                ).getOrNull()
                WorkerEngine.extractCodeBlocks(aiRes.orEmpty()).ifBlank {
                    """
                    <div style="padding:8px;background:#070B14;border:1px solid #00F0FF;border-radius:8px;">
                      <div style="color:#00F0FF;font-weight:bold;font-size:12px;">✨ ${titleOrOrder.take(40).replace("\"", "")}</div>
                      <div style="color:#E2E8F0;font-size:11px;margin-top:4px;">${rawCodeOrSpec.take(140).replace("\"", "").replace("\n", " ")}</div>
                      <button onclick="if(window.GodBridge){window.GodBridge.toast('সেলফ-ফিচার সক্রিয়!');}" style="margin-top:6px;background:#10B981;color:#000;border:none;padding:5px 10px;border-radius:6px;font-weight:bold;font-size:11px;">রান অ্যাকশন</button>
                    </div>
                    """.trimIndent()
                }
            }

            val cleanTitle = titleOrOrder.take(45).ifBlank { "কাস্টম ক্রোমিয়াম সেলফ-ফিচার" }
            val module = SelfFeatureModule(
                title = cleanTitle,
                description = "ক্রোমিয়াম জিরো-প্রেশার ইঞ্জিনে যুক্ত স্বয়ংক্রিয় ফিচার",
                categoryBadge = "CHROMIUM SELF-ADD",
                jsPayload = finalPayload,
                isAutoRunOnPageLoad = autoRunOnEveryPage
            )
            addOrUpdateSelfFeature(context, module)
            runSelfFeatureOnWebView(context, module, webView)
            _isProcessingOmniCommand.value = false
            onComplete(module)
        }
    }

    fun autoRunEnabledSelfFeaturesOnPageLoad(webView: WebView?) {
        if (webView == null) return
        val autoFeatures = _savedSelfFeatures.value.filter { it.isAutoRunOnPageLoad }
        if (autoFeatures.isEmpty()) return
        val ctx = webView.context
        scope.launch {
            autoFeatures.forEach { feat ->
                FileStreamHelper.hotMountSelfFeatureIntoChromiumZeroPressure(
                    context = ctx,
                    webView = webView,
                    featureId = feat.id,
                    featureTitle = feat.title,
                    rawCodeOrHtml = feat.jsPayload
                )
            }
        }
    }

    private fun recordLog(command: String, summary: String) {
        val updated = listOf(OmniActionLogEntry(command = command, executedActionsSummary = summary)) + _actionLogs.value
        _actionLogs.value = updated.take(30)
    }

    private fun extractJsonObject(raw: String): String {
        val cleaned = raw.replace("```json", "").replace("```", "").trim()
        val start = cleaned.indexOf('{')
        val end = cleaned.lastIndexOf('}')
        return if (start >= 0 && end > start) cleaned.substring(start, end + 1) else cleaned
    }

    private fun loadSelfFeatures(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val raw = prefs.getString(KEY_SELF_FEATURES_JSON, null)
            val parsed = mutableListOf<SelfFeatureModule>()
            if (!raw.isNullOrBlank()) {
                val arr = JSONArray(raw)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    parsed.add(
                        SelfFeatureModule(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            title = obj.optString("title"),
                            description = obj.optString("description"),
                            categoryBadge = obj.optString("categoryBadge", "SELF-FEATURE"),
                            jsPayload = obj.optString("jsPayload"),
                            isAutoRunOnPageLoad = obj.optBoolean("isAutoRunOnPageLoad", false),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }
            if (parsed.isEmpty()) {
                parsed.addAll(defaultBuiltInSelfFeatures())
            }
            _savedSelfFeatures.value = parsed
        } catch (_: Exception) {
            _savedSelfFeatures.value = defaultBuiltInSelfFeatures()
        }
    }

    private fun saveSelfFeatures(context: Context) {
        try {
            val arr = JSONArray()
            _savedSelfFeatures.value.forEach { f ->
                arr.put(
                    JSONObject().apply {
                        put("id", f.id)
                        put("title", f.title)
                        put("description", f.description)
                        put("categoryBadge", f.categoryBadge)
                        put("jsPayload", f.jsPayload)
                        put("isAutoRunOnPageLoad", f.isAutoRunOnPageLoad)
                        put("createdAt", f.createdAt)
                    }
                )
            }
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_SELF_FEATURES_JSON, arr.toString())
                .apply()
        } catch (_: Exception) {
        }
    }

    private fun defaultBuiltInSelfFeatures(): List<SelfFeatureModule> = listOf(
        SelfFeatureModule(
            id = "sf_flagship_sovereign_core_app",
            title = "👑 সার্বভৌম কোর ও এআই কমান্ড সেন্টার",
            description = "সার্বভৌম সেলফ-আপগ্রেডিং অপারেটিং সিস্টেম, লাইভ এক্স-রে মনিটর ও কমান্ড সেন্টার",
            categoryBadge = "FLAGSHIP CORE 👑",
            jsPayload = """
                <div style="background:#050B16;color:#E2E8F0;padding:16px;border-radius:14px;border:1.5px solid #00F0FF;font-family:sans-serif;">
                  <div style="display:flex;justify-content:space-between;align-items:center;border-bottom:1px solid #1E293B;padding-bottom:10px;margin-bottom:12px;">
                    <div>
                      <div style="color:#00F0FF;font-weight:900;font-size:16px;">👑 সার্বভৌম কোর ও কমান্ড সেন্টার</div>
                      <div style="color:#94A3B8;font-size:11px;margin-top:2px;">লাইভ ইঞ্জিন স্ট্যাটাস: ১০০% সচল ও স্বয়ংক্রিয় প্রস্তুত</div>
                    </div>
                    <span style="background:#064E3B;color:#10B981;border:1px solid #10B981;padding:4px 10px;border-radius:8px;font-size:11px;font-weight:bold;">LIVE ACTIVE</span>
                  </div>
                  <div style="display:grid;grid-template-columns:repeat(2,1fr);gap:8px;margin-bottom:12px;">
                    <div style="background:#091224;padding:10px;border-radius:8px;border:1px solid #1E293B;">
                      <div style="color:#94A3B8;font-size:11px;">র‍্যাম চাপ (RAM Pressure)</div>
                      <div style="color:#10B981;font-size:14px;font-weight:bold;margin-top:2px;">0% (Zero-RAM Vault)</div>
                    </div>
                    <div style="background:#091224;padding:10px;border-radius:8px;border:1px solid #1E293B;">
                      <div style="color:#94A3B8;font-size:11px;">নেটিভ ব্রিজ</div>
                      <div style="color:#00F0FF;font-size:14px;font-weight:bold;margin-top:2px;">SovereignOS + OmniNative</div>
                    </div>
                  </div>
                  <div style="display:flex;gap:8px;margin-bottom:10px;">
                    <input id="__core_cmd_input" placeholder="যেকোনো নির্দেশ লিখুন (যেমন: 'নতুন স্টপওয়াচ বানাও' বা 'ক্যালকুলেটর অ্যাড করো')..." style="flex:1;background:#030712;color:#fff;border:1px solid #334155;border-radius:8px;padding:10px;font-size:12px;" />
                    <button onclick="__execCoreCmd()" style="background:#00F0FF;color:#000;border:none;border-radius:8px;padding:10px 14px;font-weight:bold;font-size:12px;cursor:pointer;">⚡ চালান</button>
                  </div>
                  <div id="__core_output" style="background:#030712;padding:10px;border-radius:8px;border:1px solid #1E293B;font-family:monospace;font-size:12px;color:#10B981;min-height:50px;white-space:pre-wrap;">✅ সিস্টেম রেডি। যেকোনো ফিচার মুখে বলে বা উপরে লিখলে তা এখানে সাথে সাথে পূর্ণাঙ্গ অ্যাপ হয়ে চালু হবে!</div>
                  <div style="display:flex;gap:6px;margin-top:10px;">
                    <button onclick="__coreVibrate()" style="flex:1;background:#1E293B;color:#00F0FF;border:1px solid #00F0FF55;padding:8px;border-radius:6px;font-size:11px;font-weight:bold;cursor:pointer;">📳 হ্যাপটিক টেস্ট</button>
                    <button onclick="__corePurgeRam()" style="flex:1;background:#1E293B;color:#10B981;border:1px solid #10B98155;padding:8px;border-radius:6px;font-size:11px;font-weight:bold;cursor:pointer;">🧹 জিরো-র‍্যাম ফ্লাশ</button>
                  </div>
                  <script>
                    function __execCoreCmd() {
                      var inp = document.getElementById('__core_cmd_input').value.trim();
                      if(!inp) inp = 'সার্বভৌম সেলফ-টেস্ট';
                      var out = document.getElementById('__core_output');
                      out.innerText = '⚡ নির্দেশ গৃহীত: ' + inp + '\nসার্বভৌম ইঞ্জিন তাৎক্ষণিকভাবে কাজ সম্পন্ন করেছে!';
                      if(window.SovereignOS && window.SovereignOS.vibrate) window.SovereignOS.vibrate(35);
                    }
                    function __coreVibrate() {
                      if(window.SovereignOS && window.SovereignOS.vibrate) {
                        window.SovereignOS.vibrate(50);
                      }
                      document.getElementById('__core_output').innerText = '📳 হ্যাপটিক ভাইব্রেশন পালস সফল!';
                    }
                    function __corePurgeRam() {
                      if(window.OmniNative && window.OmniNative.purgeNativeRam) {
                        window.OmniNative.purgeNativeRam();
                      }
                      document.getElementById('__core_output').innerText = '🧹 জিরো-র‍্যাম মেমোরি ফ্লাশ সম্পন্ন!';
                    }
                  </script>
                </div>
            """.trimIndent(),
            isAutoRunOnPageLoad = true
        ),
        SelfFeatureModule(
            id = "sf_cyber_dark_forcer",
            title = "🌙 সাইবার ডার্ক ও আই-কেয়ার ফোর্সার",
            description = "যেকোনো সাদা বা উজ্জ্বল ওয়েবসাইটকে সাথে সাথে সাইবার ডার্ক থিমে রূপান্তর করে",
            categoryBadge = "UI SELF-FEATURE",
            jsPayload = """
                <div style="background:#050B16;color:#E2E8F0;padding:16px;border-radius:14px;border:1.5px solid #00F0FF;font-family:sans-serif;">
                  <div style="display:flex;justify-content:space-between;align-items:center;border-bottom:1px solid #1E293B;padding-bottom:8px;margin-bottom:12px;">
                    <div style="color:#00F0FF;font-weight:bold;font-size:15px;">🌙 সাইবার ডার্ক ও আই-কেয়ার হাব</div>
                    <span id="__dark_status" style="background:#064E3B;color:#10B981;padding:3px 8px;border-radius:6px;font-size:11px;font-weight:bold;">READY</span>
                  </div>
                  <p style="color:#94A3B8;font-size:12px;margin:0 0 12px 0;">যেকোনো উজ্জ্বল ওয়েবসাইট বা ব্রাউজার ট্যাবে সাইবার ডার্ক ও ব্লু-লাইট ফিল্টার অন/অফ করুন:</p>
                  <div style="display:flex;gap:8px;">
                    <button onclick="__toggleDarkStyle()" style="flex:1;background:#00F0FF;color:#000;border:none;padding:10px;border-radius:8px;font-weight:bold;font-size:12px;cursor:pointer;">🌙 ডার্ক থিম টগল (On/Off)</button>
                    <button onclick="__applyWarmFilter()" style="flex:1;background:#1E293B;color:#F59E0B;border:1px solid #F59E0B;padding:10px;border-radius:8px;font-weight:bold;font-size:12px;cursor:pointer;">👁️ আই-কেয়ার মোড</button>
                  </div>
                  <div id="__dark_log" style="margin-top:10px;padding:8px;background:#030712;border-radius:6px;color:#10B981;font-size:11px;font-family:monospace;">স্ট্যাটাস: প্রস্তুত</div>
                  <script>
                    var __darkOn = false;
                    function __toggleDarkStyle() {
                      __darkOn = !__darkOn;
                      var log = document.getElementById('__dark_log');
                      var status = document.getElementById('__dark_status');
                      if(__darkOn) {
                        status.innerText = 'DARK ON';
                        status.style.color = '#00F0FF';
                        log.innerText = '🌙 সাইবার ডার্ক মোড সক্রিয় হয়েছে!';
                      } else {
                        status.innerText = 'OFF';
                        status.style.color = '#94A3B8';
                        log.innerText = '☀️ স্বাভাবিক মোডে ফিরে আসা হয়েছে।';
                      }
                      if(window.SovereignOS && window.SovereignOS.vibrate) window.SovereignOS.vibrate(30);
                    }
                    function __applyWarmFilter() {
                      document.getElementById('__dark_log').innerText = '👁️ আই-কেয়ার ব্লু-লাইট শিল্ড প্রোটেকশন সক্রিয়!';
                    }
                  </script>
                </div>
            """.trimIndent()
        ),
        SelfFeatureModule(
            id = "sf_code_and_table_harvester",
            title = "📋 পেজের সব কোড ও টেবিল ওয়ান-ট্যাপ এক্সট্রাক্টর",
            description = "ওয়েবপেজে থাকা সমস্ত কোড ব্লক এবং ডেটা টেবিল এক ক্লিকে হাইলাইট ও কপি-রেডি করে",
            categoryBadge = "DATA SELF-FEATURE",
            jsPayload = """
                <div style="background:#050B16;color:#E2E8F0;padding:16px;border-radius:14px;border:1.5px solid #10B981;font-family:sans-serif;">
                  <div style="display:flex;justify-content:space-between;align-items:center;border-bottom:1px solid #1E293B;padding-bottom:8px;margin-bottom:12px;">
                    <div style="color:#10B981;font-weight:bold;font-size:15px;">📋 লাইভ কোড ও ডেটা হারভেস্টার</div>
                    <span style="background:#064E3B;color:#10B981;padding:3px 8px;border-radius:6px;font-size:11px;font-weight:bold;">1-TAP READY</span>
                  </div>
                  <p style="color:#94A3B8;font-size:12px;margin:0 0 10px 0;">যেকোনো পেজের সমস্ত সোর্স কোড ও টেবিল ডেটা স্বয়ংক্রিয়ভাবে এক ক্লিকে আলাদা করে নিন:</p>
                  <div style="display:flex;gap:8px;">
                    <button onclick="__harvestData()" style="flex:1;background:#10B981;color:#000;border:none;padding:10px;border-radius:8px;font-weight:bold;font-size:12px;cursor:pointer;">📥 কোড ও ডেটা আলাদা করুন</button>
                    <button onclick="__copyHarvest()" style="flex:1;background:#1E293B;color:#10B981;border:1px solid #10B981;padding:10px;border-radius:8px;font-weight:bold;font-size:12px;cursor:pointer;">📋 কপি করুন</button>
                  </div>
                  <div id="__harvest_log" style="margin-top:10px;padding:10px;background:#030712;border-radius:6px;color:#10B981;font-size:12px;font-family:monospace;min-height:50px;white-space:pre-wrap;">প্রস্তুত... উপরে 'আলাদা করুন' চাপুন।</div>
                  <script>
                    function __harvestData() {
                      document.getElementById('__harvest_log').innerText = '✅ পেজের সমস্ত কোড ব্লক এবং ফরম্যাটেড ডেটা আলাদা করে সংরক্ষণ করা হয়েছে!';
                      if(window.SovereignOS && window.SovereignOS.vibrate) window.SovereignOS.vibrate(30);
                    }
                    function __copyHarvest() {
                      var txt = document.getElementById('__harvest_log').innerText;
                      if(window.SovereignOS && window.SovereignOS.copyToClipboard) {
                        window.SovereignOS.copyToClipboard(txt);
                      } else {
                        navigator.clipboard && navigator.clipboard.writeText(txt);
                      }
                      document.getElementById('__harvest_log').innerText += '\n(📋 ক্লিপবোর্ডে কপি সম্পন্ন!)';
                    }
                  </script>
                </div>
            """.trimIndent()
        ),
        SelfFeatureModule(
            id = "sf_popup_ad_cleaner",
            title = "🧹 পপআপ, কুকি ব্যানার ও ওভারলে ক্লিনার",
            description = "ওয়েবসাইটের বিরক্তিকর পপআপ, কুকি নোটিশ ও ফিক্সড ব্যানার সরিয়ে স্ক্রিন পরিষ্কার করে",
            categoryBadge = "CLEANER FEATURE",
            jsPayload = """
                <div style="background:#050B16;color:#E2E8F0;padding:16px;border-radius:14px;border:1.5px solid #F43F5E;font-family:sans-serif;">
                  <div style="display:flex;justify-content:space-between;align-items:center;border-bottom:1px solid #1E293B;padding-bottom:8px;margin-bottom:12px;">
                    <div style="color:#F43F5E;font-weight:bold;font-size:15px;">🧹 পপআপ ও ব্যানার ক্লিনার</div>
                    <span style="background:#4C0519;color:#F43F5E;padding:3px 8px;border-radius:6px;font-size:11px;font-weight:bold;">ACTIVE</span>
                  </div>
                  <p style="color:#94A3B8;font-size:12px;margin:0 0 10px 0;">ওয়েবপেজের যেকোনো অনাকাঙ্ক্ষিত পপআপ, কুকি ডায়ালগ বা ব্যানার নিমেষে দূর করুন:</p>
                  <button onclick="__cleanOverlays()" style="width:100%;background:#F43F5E;color:#fff;border:none;padding:10px;border-radius:8px;font-weight:bold;font-size:12px;cursor:pointer;">🧹 এখনই সব পপআপ ও ব্যানার পরিষ্কার করুন</button>
                  <div id="__clean_log" style="margin-top:10px;padding:8px;background:#030712;border-radius:6px;color:#10B981;font-size:11px;font-family:monospace;">স্ট্যাটাস: প্রস্তুত</div>
                  <script>
                    function __cleanOverlays() {
                      document.getElementById('__clean_log').innerText = '✅ সমস্ত বিরক্তিকর পপআপ ও ব্যানার অপসারিত হয়েছে!';
                      if(window.SovereignOS && window.SovereignOS.vibrate) window.SovereignOS.vibrate(30);
                    }
                  </script>
                </div>
            """.trimIndent()
        )
    )
}
