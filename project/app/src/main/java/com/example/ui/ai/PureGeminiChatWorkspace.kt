package com.example.ui.ai

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.engine.FileStreamHelper
import com.example.engine.NanobotSwarmOrchestrator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import java.util.Locale
import java.util.UUID
import com.example.engine.AiChromiumDevEngine
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.engine.AppCellularAnatomyEngine
import com.example.engine.AppTruthAndArchitectureVault
import com.example.ui.ai.AppTruthDossierSheet
import com.example.engine.AutonomousSelfEvolutionEngine
import com.example.engine.BuildJobStatus
import com.example.engine.ChatSessionManager
import com.example.engine.ChromiumOmniZeroPressureEngine
import com.example.engine.ParticleSwarmManager
import com.example.engine.SovereignDeepResearchAndIncrementalEngine
import com.example.engine.UniversalOmniControlEngine
import com.example.engine.MasterOmniControllerEngine
import com.example.engine.MicroPartHealthStatus
import com.example.engine.MultiMinuteAutonomousBuilderEngine
import com.example.engine.OmniAppControlBridge
import com.example.engine.SelfFeatureModule
import com.example.engine.SovereignLiveReviewAndXRayEngine
import com.example.engine.LiveIntelligenceMode
import com.example.engine.SovereignExecutionMode
import com.example.engine.SovereignGeminiLiveOmniEngine
import com.example.engine.SovereignMetaKernelEngine
import com.example.engine.UniversalAiManager
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRose
import com.example.ui.theme.CyberSlate
import com.example.ui.theme.cyberAmbientBackground
import com.example.voice.MicNetworkStatus
import com.example.voice.SovereignVoiceVaultEngine
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PureGeminiChatWorkspace(
    activeWebView: WebView?,
    isVoiceListening: Boolean = false,
    liveVoiceTranscript: String = "",
    pendingVoiceCommand: String = "",
    pendingVoiceTriggerId: Int = 0,
    onToggleVoiceMic: () -> Unit = {},
    onOpenAiKeySettings: () -> Unit = {},
    omniControlBridge: OmniAppControlBridge? = null,
    onSwitchToBrowserMode: () -> Unit = {},
    isImmersiveFullScreen: Boolean = false,
    onToggleImmersiveFullScreen: () -> Unit = {},
    onExitFullScreenChat: () -> Unit = {},
    onStartGeminiLiveShare: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val listState = rememberLazyListState()

    val activeSession by ChatSessionManager.activeSession.collectAsState()
    val sessionSummaries by ChatSessionManager.sessionSummaries.collectAsState()
    val profiles by UniversalAiManager.profiles.collectAsState()
    val activeProfileId by UniversalAiManager.activeProfileId.collectAsState()
    val activeProfile = remember(profiles, activeProfileId) {
        UniversalAiManager.getActiveProfile()
    }

    LaunchedEffect(Unit) {
        AutonomousSelfEvolutionEngine.initialize(context)
        SovereignLiveReviewAndXRayEngine.inspectAndBuildHumanReview(
            context = context,
            module = AutonomousSelfEvolutionEngine.activeLiveModule.value,
            activeBuildJob = MultiMinuteAutonomousBuilderEngine.activeJob.value
        )
    }

    val activeAutoModeBadge by UniversalAiManager.activeAutoModeBadge.collectAsState()
    val activeBuildJob by MultiMinuteAutonomousBuilderEngine.activeJob.collectAsState()
    val latestReviewSnapshot by SovereignLiveReviewAndXRayEngine.latestReviewSnapshot.collectAsState()
    val activeExecMode by SovereignMetaKernelEngine.activeExecutionMode.collectAsState()
    val bgTaskState by SovereignMetaKernelEngine.backgroundTaskState.collectAsState()
    val liveDebateBanner by SovereignMetaKernelEngine.liveDebateStatusBanner.collectAsState()
    val quotaTelemetry by FileStreamHelper.telemetry.collectAsState()

    // Classic Normal OS Voice Recognizer Launcher ("আবার নরমাল মাইক দিবে — একদম নিখুঁত শুনবে")
    val normalSystemMicLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            val spokenList = res.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val bestSpoken = spokenList?.firstOrNull { it.isNotBlank() }?.trim().orEmpty()
            if (bestSpoken.isNotBlank()) {
                val bnText = com.example.voice.IndianBengaliSpeechConverter.convertToIndianBengali(bestSpoken)
                userChatInput = if (userChatInput.isBlank()) bnText else "$userChatInput $bnText"
                SovereignMetaKernelEngine.triggerPredictiveShadowPrethink(context, userChatInput)
            }
        }
    }

    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    var isSpeakingTts by remember { mutableStateOf(false) }
    var speakingMsgId by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                ttsEngine?.stop()
                ttsEngine?.shutdown()
            } catch (_: Throwable) {}
        }
    }

    fun stopBanglaTts() {
        try {
            ttsEngine?.stop()
        } catch (_: Throwable) {}
        isSpeakingTts = false
        speakingMsgId = null
    }

    fun splitIntoNaturalSpeechChunks(text: String, maxChunkLen: Int = 300): List<String> {
        val rawSentences = text.split(Regex("(?<=[।.!?\\n])"))
        val chunks = mutableListOf<String>()
        val current = StringBuilder()
        for (s in rawSentences) {
            val trimmed = s.trim()
            if (trimmed.isEmpty()) continue
            if (current.length + trimmed.length + 1 <= maxChunkLen) {
                if (current.isNotEmpty()) current.append(" ")
                current.append(trimmed)
            } else {
                if (current.isNotEmpty()) {
                    chunks.add(current.toString())
                    current.clear()
                }
                if (trimmed.length <= maxChunkLen) {
                    current.append(trimmed)
                } else {
                    var idx = 0
                    while (idx < trimmed.length) {
                        val end = (idx + maxChunkLen).coerceAtMost(trimmed.length)
                        chunks.add(trimmed.substring(idx, end))
                        idx = end
                    }
                }
            }
        }
        if (current.isNotEmpty()) {
            chunks.add(current.toString())
        }
        return chunks
    }

    fun speakBanglaAloud(rawText: String, msgId: String? = null) {
        if (isSpeakingTts && (msgId == null || speakingMsgId == msgId)) {
            stopBanglaTts()
            Toast.makeText(context, "⏹️ ভয়েস পাঠ বন্ধ করা হয়েছে", Toast.LENGTH_SHORT).show()
            return
        }

        val cleanSpeech = rawText
            .replace(Regex("```[\\s\\S]*?```"), " কোড অংশ ")
            .replace(Regex("[#*`_~>•]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        if (cleanSpeech.isEmpty()) return

        val chunks = splitIntoNaturalSpeechChunks(cleanSpeech)
        if (chunks.isEmpty()) return

        fun enqueueAllChunks(engine: TextToSpeech) {
            engine.setSpeechRate(0.96f)
            engine.setPitch(1.0f)
            engine.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    isSpeakingTts = true
                }

                override fun onDone(utteranceId: String?) {
                    if (utteranceId?.endsWith("_last") == true) {
                        isSpeakingTts = false
                        speakingMsgId = null
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    isSpeakingTts = false
                    speakingMsgId = null
                }
            })
            isSpeakingTts = true
            speakingMsgId = msgId
            val baseId = "skrl_tts_${System.currentTimeMillis()}"
            chunks.forEachIndexed { index, chunk ->
                val mode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                val uId = if (index == chunks.lastIndex) "${baseId}_last" else "${baseId}_$index"
                engine.speak(chunk, mode, null, uId)
            }
        }

        val existing = ttsEngine
        if (existing != null) {
            enqueueAllChunks(existing)
            Toast.makeText(context, "🔊 বাংলা ভয়েসে পাঠ শুরু হয়েছে (থামাতে আবার ট্যাপ করুন)", Toast.LENGTH_SHORT).show()
        } else {
            var newTts: TextToSpeech? = null
            newTts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS && newTts != null) {
                    val bnIn = Locale("bn", "IN")
                    val res = newTts?.setLanguage(bnIn)
                    if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                        newTts?.language = Locale.getDefault()
                    }
                    enqueueAllChunks(newTts!!)
                }
            }
            ttsEngine = newTts
            Toast.makeText(context, "🔊 বাংলা ভয়েসে পাঠ শুরু হয়েছে (থামাতে আবার ট্যাপ করুন)", Toast.LENGTH_SHORT).show()
        }
    }

    val savedSelfFeatures by MasterOmniControllerEngine.savedSelfFeatures.collectAsState()
    val activeLiveModule by AutonomousSelfEvolutionEngine.activeLiveModule.collectAsState()
    val isFullScreenRunnerMode by AutonomousSelfEvolutionEngine.isFullScreenRunnerMode.collectAsState()
    val evolutionVersion by AutonomousSelfEvolutionEngine.evolutionVersion.collectAsState()
    val metaEngineState by AutonomousSelfEvolutionEngine.metaEngineState.collectAsState()
    val latestPredictiveBlueprint by com.example.engine.SovereignPredictiveArchitectEngine.latestBlueprint.collectAsState()
    val bugsPreemptedCount by com.example.engine.SovereignPredictiveArchitectEngine.totalBugsPreemptedCount.collectAsState()

    var userChatInput by remember { mutableStateOf("") }
    var isAiGenerating by remember { mutableStateOf(false) }
    var thinkingElapsedSeconds by remember { mutableStateOf(0) }
    var showHistoryDrawer by remember { mutableStateOf(false) }
    var showAnatomySheet by remember { mutableStateOf(false) }
    var showTruthDossierSheet by remember { mutableStateOf(false) }
    var showSelfUpgradeBar by remember { mutableStateOf(false) }
    var showBuiltAppsShowroom by remember { mutableStateOf(false) }
    var showHumanReviewPage by remember { mutableStateOf(false) }
    var showQuickApiKeyBar by remember { mutableStateOf(false) }
    var quickApiKeyInput by remember { mutableStateOf("") }
    var voiceAutoSendEnabled by remember { mutableStateOf(false) }
    var showCodeRemixDialog by remember { mutableStateOf(false) }
    var codeRemixInputText by remember { mutableStateOf("") }

    LaunchedEffect(isAiGenerating) {
        if (isAiGenerating) {
            thinkingElapsedSeconds = 0
            while (isAiGenerating) {
                kotlinx.coroutines.delay(1000L)
                thinkingElapsedSeconds += 1
            }
        } else {
            thinkingElapsedSeconds = 0
        }
    }

    val micStatus by SovereignVoiceVaultEngine.micStatus.collectAsState()
    val bufferedSpeech by SovereignVoiceVaultEngine.bufferedSpeechText.collectAsState()
    val liveRms by SovereignVoiceVaultEngine.liveRmsLevel.collectAsState()

    // Central dispatcher for both typed and spoken commands
    val executeChatOrEvolutionCommand: (String) -> Unit = { rawText ->
        val textToSend = rawText.trim()
        if (textToSend.isNotBlank() && !isAiGenerating) {
            userChatInput = ""
            isAiGenerating = true

            // Capture prior conversation history BEFORE adding the new message so the AI has full multi-turn memory
            val priorHistoryPairs = (activeSession?.messages ?: emptyList())
                .filter { it.sender == "USER" || it.sender == "AI" }
                .map { it.sender to it.text }

            // 1. Save user message to persistent disk history
            ChatSessionManager.addMessage(context, sender = "USER", text = textToSend)

            scope.launch {
                val isTruthMode = AppTruthAndArchitectureVault.isTruthTransparencyEnabled.value
                val lowerMsg = textToSend.lowercase()
                val isAskingForAppInside = lowerMsg.contains("ভেতরে কি আছে") ||
                    lowerMsg.contains("ভেতরে কি") ||
                    lowerMsg.contains("ভেতরে কি না আছে") ||
                    lowerMsg.contains("ভেতরে কি আছে না আছে") ||
                    lowerMsg.contains("অ্যাপটির ভেতরে") ||
                    lowerMsg.contains("অ্যাপের ভেতরে") ||
                    lowerMsg.contains("কোড দেখো") ||
                    lowerMsg.contains("সত্যি বলো") ||
                    lowerMsg.contains("আসল সত্য") ||
                    lowerMsg.contains("আপগ্রেড করা এটি কোথায় গেল") ||
                    lowerMsg.contains("নকশা বক্সা") ||
                    lowerMsg.contains("অ্যানাটমি")

                val isGeminiLiveCmd = (lowerMsg.contains("লাইভ শেয়ার") || lowerMsg.contains("লাইভ শেয়ার") || lowerMsg.contains("জেমিনি লাইভ") || lowerMsg.contains("লাইভ মোড") || lowerMsg.contains("gemini live") || lowerMsg.contains("live share")) &&
                    (lowerMsg.contains("চালু") || lowerMsg.contains("স্টার্ট") || lowerMsg.contains("ওপেন") || lowerMsg.contains("start") || lowerMsg.contains("on") || lowerMsg.length <= 28)

                if (isGeminiLiveCmd) {
                    isAiGenerating = false
                    onStartGeminiLiveShare()
                    ChatSessionManager.addMessage(
                        context = context,
                        sender = "AI",
                        text = "🔴 **[Google Gemini Live + Screen Share সক্রিয় হয়েছে]:**\nআমি এখন আপনার স্ক্রিন সরাসরি দেখছি। আপনি কথা না বললেও স্ক্রিনে পরিবর্তন হলে আমি নিজে থেকে কথা বলব এবং আপনার মুখের আদেশে অটো-টাচ ও স্ক্রল করব!",
                        isCode = false
                    )
                    return@launch
                }

                val isParticleCommand = lowerMsg.contains("পার্টিকল") || lowerMsg.contains("particle") ||
                    lowerMsg.contains("ক্লিক করো") || lowerMsg.contains("রান বাটন") ||
                    lowerMsg.contains("স্ক্রোল করো") || lowerMsg.contains("মিশন") ||
                    lowerMsg.contains("কোড আনো") || lowerMsg.contains("যতক্ষণ না")

                if (isParticleCommand) {
                    ParticleSwarmManager.dispatchParticleCommandByAi(context, activeWebView, textToSend) { reply ->
                        ChatSessionManager.addMessage(context, "AI", "✨ [পার্টিকলস একশন]:\n$reply", false)
                    }
                }

                // 1.5. Execute King/Raja Supreme Command + Original Universal Omni-Control Command (100% Same-to-Same + Extra)
                val omniInstantSummary = SovereignMetaKernelEngine.executeKingSupremeCommand(
                    context = context,
                    rawCommand = textToSend,
                    activeWebView = activeWebView
                ) ?: UniversalOmniControlEngine.processNaturalLanguageOrJsonCommand(
                    context = context,
                    rawText = textToSend
                )
                if (omniInstantSummary != null) {
                    ChatSessionManager.addMessage(context, "AI", omniInstantSummary, false)
                }

                // 1.8. Direct Website Creation check ("একটি ওয়েবসাইট ক্রিয়েট করা")
                val isWebsiteCreateCmd = (lowerMsg.contains("ওয়েবসাইট") || lowerMsg.contains("ওয়েবসাইট") || lowerMsg.contains("website") || lowerMsg.contains("সাইট তৈরি") || lowerMsg.contains("সাইট বানাও")) &&
                    (lowerMsg.contains("বানাও") || lowerMsg.contains("ক্রিয়েট") || lowerMsg.contains("ক্রিয়েট") || lowerMsg.contains("তৈরি") || lowerMsg.contains("বিল্ড") || lowerMsg.contains("অ্যাড") || lowerMsg.contains("এড"))
                if (isWebsiteCreateCmd) {
                    val (siteReport, _) = SovereignDeepResearchAndIncrementalEngine.createAndDeployCompleteWebsiteWithRetry(
                        context = context,
                        userRequest = textToSend,
                        activeWebView = activeWebView
                    )
                    isAiGenerating = false
                    ChatSessionManager.addMessage(context, "AI", siteReport, false)
                    Toast.makeText(context, "🌐 নতুন ওয়েবসাইট তৈরি ও ব্রাউজারে লোড হয়েছে!", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // 2. Execute Original Feature Addition / App Surgery / Game Build (100% Same-to-Same as Google AI Studio, while allowing normal coding/chat questions through to Gemini!)
                val wantsFullSystemAlongsideOmni = lowerMsg.contains("ফিচার") || lowerMsg.contains("গেম") || lowerMsg.contains("স্টুডিও") || lowerMsg.contains("স্লট") || lowerMsg.contains("ইঞ্জিন") || lowerMsg.contains("অ্যাপ") || lowerMsg.contains("সিস্টেম")
                val isCodingOrAnalyticalPrompt = textToSend.length > 140 || lowerMsg.contains("কোড দাও") || lowerMsg.contains("কেন ") || lowerMsg.contains("কিভাবে ") || lowerMsg.contains("বুঝিয়ে বলো")
                val evolutionRes = if (!isCodingOrAnalyticalPrompt && !isAskingForAppInside && !isParticleCommand && (omniInstantSummary == null || wantsFullSystemAlongsideOmni)) {
                    AutonomousSelfEvolutionEngine.processAndExecuteSovereignCommand(
                        context = context,
                        rawInput = textToSend,
                        bridge = omniControlBridge,
                        activeWebView = activeWebView,
                        onSwitchToBrowserMode = onSwitchToBrowserMode,
                        onOpenAnatomySheet = { showAnatomySheet = true },
                        onOpenAiKeySettings = onOpenAiKeySettings
                    )
                } else null

                if (evolutionRes != null && evolutionRes.wasHandledAsActionOrUpgrade) {
                    isAiGenerating = false
                    ChatSessionManager.addMessage(
                        context = context,
                        sender = "AI",
                        text = evolutionRes.detailedResponseBangla,
                        isCode = false
                    )
                    Toast.makeText(context, evolutionRes.actionTitleBangla, Toast.LENGTH_SHORT).show()
                } else {
                    // 3. Inject Focused Sovereign Cognitive Context (61-file map + live delta + Omni-Control + Dual Chat context with zero cognitive overload!)
                    val baseInstruction = if (lowerMsg.contains("নকশা বক্সা") || lowerMsg.contains("অ্যানাটমি")) {
                        AppCellularAnatomyEngine.generateComprehensiveSelfAnatomyPrompt(
                            context = context,
                            activeWebView = activeWebView,
                            activeScreen = "AI_CHAT",
                            isVoiceListening = isVoiceListening
                        )
                    } else {
                        AppTruthAndArchitectureVault.getFullSystemTruthInstruction(textToSend)
                    }
                    val activeCustomAis = SovereignDeepResearchAndIncrementalEngine.customAiAgents.value.filter { it.isEnabled }
                    val customAiPersonas = if (activeCustomAis.isNotEmpty()) {
                        "\n=== [ACTIVE CUSTOM AI BRAINS IN APP] ===\n" +
                            activeCustomAis.joinToString("\n") { "• ${it.name} (${it.specialtyBangla}): ${it.systemPrompt}" }
                    } else ""
                    val customSysInstruction = baseInstruction +
                        customAiPersonas + "\n" +
                        UniversalOmniControlEngine.getOmniControlSystemPrompt() + "\n" +
                        ParticleSwarmManager.getDualChatCombinedContext()

                    val wantsBackgroundExecution = activeExecMode == SovereignExecutionMode.BACKGROUND_AUTONOMOUS_ROBOT ||
                        ((lowerMsg.contains("ব্যাকগ্রাউন্ডে") || lowerMsg.contains("আমি ইউটিউব") || lowerMsg.contains("বাইরে থেকে")) && textToSend.length >= 12)

                    if (wantsBackgroundExecution) {
                        isAiGenerating = false
                        SovereignMetaKernelEngine.startBackgroundAutonomousOrder(
                            context = context,
                            userOrder = textToSend
                        )
                        if (activeExecMode == SovereignExecutionMode.BACKGROUND_AUTONOMOUS_ROBOT) {
                            SovereignMetaKernelEngine.setExecutionMode(context, SovereignExecutionMode.GOD_DEEP_THINK)
                        }
                        return@launch
                    }

                    val wantsGodDebate = activeExecMode == SovereignExecutionMode.GOD_MULTI_AI_DEBATE ||
                        lowerMsg.contains("ডিবেট করো") || lowerMsg.contains("তিনটি এআই") || lowerMsg.contains("সমালোচনা করে")

                    val cachedInstant = if (!wantsGodDebate) {
                        SovereignMetaKernelEngine.lookupSubMillisecondCache(textToSend)
                    } else null

                    val result = when {
                        cachedInstant != null -> Result.success(cachedInstant)
                        wantsGodDebate -> SovereignMetaKernelEngine.executeGodLevelMultiBrainDebate(
                            context = context,
                            userPrompt = textToSend,
                            baseSystemInstruction = customSysInstruction,
                            conversationHistory = priorHistoryPairs,
                            forLiveVoiceMode = false
                        )
                        else -> UniversalAiManager.executeAiPrompt(
                            prompt = textToSend,
                            systemInstruction = customSysInstruction,
                            conversationHistory = priorHistoryPairs,
                            includeStandingRules = true
                        )
                    }

                    isAiGenerating = false
                    val rawReplyText = result.getOrElse {
                        if (isAskingForAppInside) {
                            AppTruthAndArchitectureVault.getZeroHangSystemMapAndAnatomyForAi(textToSend)
                        } else if (omniInstantSummary != null) {
                            "✅ আপনার আদেশ অনুযায়ী মেইন এআই অ্যাপের কন্ট্রোল পরিবর্তন সম্পন্ন করেছে।"
                        } else {
                            "✅ **সার্বভৌম ডিপ-থিংক ইঞ্জিন রেসপন্স:**\nআপনার নির্দেশনা (\"$textToSend\") গৃহীত হয়েছে। উপরে '🔑 API Key' বাটনে ট্যাপ করে আপনার কী সক্রিয় করতে পারেন অথবা যেকোনো কন্ট্রোল আদেশ দিন।"
                        }
                    }

                    // Execute any [LIVE_ACTION: CLICK/SWIPE/TYPE/HOME/BACK] hardware gestures emitted by AI
                    if (rawReplyText.contains("[LIVE_ACTION:", ignoreCase = true)) {
                        com.example.engine.SovereignWakeupBatch3VisionAndDspEngine.parseAndExecuteLiveActionTags(context, rawReplyText)
                    }
                    // If Gemini returned an [OMNI_PATCH] JSON block, apply it live and strip the raw tag from display
                    if (rawReplyText.contains("[OMNI_PATCH]")) {
                        UniversalOmniControlEngine.processNaturalLanguageOrJsonCommand(context, rawReplyText)
                    }
                    // If Gemini returned a [GENESIS_BUILD] block for any novel/sky-high feature, synthesize and launch it automatically!
                    val genesisRegex = Regex("\\[GENESIS_BUILD\\](.*?)\\[/GENESIS_BUILD\\]", RegexOption.DOT_MATCHES_ALL)
                    val genesisMatch = genesisRegex.find(rawReplyText)
                    if (genesisMatch != null) {
                        val gSpec = genesisMatch.groupValues[1].trim()
                        AutonomousSelfEvolutionEngine.synthesizeAndExecuteLiveFeatureUpgrade(
                            context = context,
                            userOrder = gSpec.ifBlank { textToSend },
                            activeWebView = activeWebView
                        )
                    }

                    val godProcessedReply = com.example.engine.SovereignAutonomousGodBrainEngine.processAiResponseAndExecuteGodActions(
                        context = context,
                        userPrompt = textToSend,
                        rawAiResponse = rawReplyText,
                        activeWebView = activeWebView
                    )
                    val replyText = godProcessedReply
                        .replace(Regex("\\[OMNI_PATCH\\].*?\\[/OMNI_PATCH\\]", RegexOption.DOT_MATCHES_ALL), "")
                        .replace(genesisRegex, "")
                        .trim()

                    if (replyText.isNotBlank()) {
                        val isCodeOutput = replyText.contains("<div") || replyText.contains("<html") || replyText.contains("function") || replyText.contains("```")
                        ChatSessionManager.addMessage(
                            context = context,
                            sender = "AI",
                            text = replyText,
                            isCode = isCodeOutput
                        )
                    }
                }
            }
        }
    }

    // Mirror live speech into input box cleanly in real time (ignoring status prefixes)
    LaunchedEffect(liveVoiceTranscript) {
        val t = liveVoiceTranscript.trim()
        val isStatusNotice = t.startsWith("🎙️") ||
            t.startsWith("⏳") ||
            t.startsWith("⏸️") ||
            t.startsWith("⚡") ||
            t.startsWith("⚠️")
        if (t.isNotBlank() && !isStatusNotice) {
            userChatInput = t
        }
    }

    // Handle finished voice segment from in-app Hands-Free Auto-VAD MIC:
    // Places the accumulated Indian Bengali translated speech directly into userChatInput
    // (or auto-sends only if user explicitly enabled voiceAutoSendEnabled).
    var lastHandledVoiceTrigger by remember { mutableStateOf(0) }
    LaunchedEffect(pendingVoiceTriggerId) {
        if (pendingVoiceTriggerId > lastHandledVoiceTrigger && pendingVoiceCommand.isNotBlank()) {
            lastHandledVoiceTrigger = pendingVoiceTriggerId
            val cmdToRun = pendingVoiceCommand.trim()
            if (voiceAutoSendEnabled) {
                userChatInput = ""
                executeChatOrEvolutionCommand(cmdToRun)
            } else {
                userChatInput = cmdToRun
            }
        }
    }

    // Auto-scroll to bottom on new message
    val messages = activeSession?.messages ?: emptyList()
    LaunchedEffect(messages.size, isAiGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val omniVisual by UniversalOmniControlEngine.visualState.collectAsState()
    val isDefaultWhiteBg = omniVisual.mode == "SOLID" && omniVisual.primaryHex.equals("#FFFFFF", ignoreCase = true)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDefaultWhiteBg) Color.White else Color.Transparent)
    ) {
        // TOP FULL-SCREEN CHAT HEADER (Hidden when a Built App/3D Game is in 100% Full-Screen Runner Mode)
        if (!isFullScreenRunnerMode || activeLiveModule == null) {
            Surface(
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val omniAllNodes by UniversalOmniControlEngine.nodes.collectAsState()
                    val omniHeaderApplyEv by UniversalOmniControlEngine.pendingFeatureApplyEvent.collectAsState()
                    LaunchedEffect(omniHeaderApplyEv?.eventId) {
                        val ev = omniHeaderApplyEv ?: return@LaunchedEffect
                        val shouldOpen = ev.actionMode != "TURN_OFF"
                        when (ev.nodeId) {
                            "chat_hdr_api_key" -> showQuickApiKeyBar = shouldOpen
                            "chat_hdr_history" -> showHistoryDrawer = shouldOpen
                            "chat_hdr_review_page" -> {
                                if (shouldOpen) {
                                    SovereignLiveReviewAndXRayEngine.inspectAndBuildHumanReview(
                                        context = context,
                                        module = activeLiveModule,
                                        activeBuildJob = activeBuildJob
                                    )
                                }
                                showHumanReviewPage = shouldOpen
                            }
                            "chat_hdr_apps_showroom" -> showBuiltAppsShowroom = shouldOpen
                            "chat_hdr_power_modules" -> showSelfUpgradeBar = shouldOpen
                            "ALL_ACTIVE_FEATURES" -> {
                                if (!shouldOpen) {
                                    showQuickApiKeyBar = false
                                    showHistoryDrawer = false
                                    showHumanReviewPage = false
                                    showBuiltAppsShowroom = false
                                    showSelfUpgradeBar = false
                                }
                            }
                        }
                    }
                    val chatHeaderNodes = remember(omniAllNodes) {
                        omniAllNodes.filter { it.zone == "CHAT_HEADER" }.sortedBy { it.orderIndex }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .horizontalScroll(rememberScrollState())
                    ) {
                        chatHeaderNodes.forEach { hdrNode ->
                            val dispLabel = when (hdrNode.id) {
                                "chat_hdr_api_key" -> if (activeProfile != null) "🔑 API Key (চালু ✅)" else hdrNode.label
                                "chat_hdr_apps_showroom" -> "🚀 অ্যাপস (${savedSelfFeatures.size})"
                                else -> hdrNode.label
                            }
                            val isSelected = when (hdrNode.id) {
                                "chat_hdr_api_key" -> showQuickApiKeyBar
                                "chat_hdr_history" -> showHistoryDrawer
                                "chat_hdr_review_page" -> showHumanReviewPage
                                "chat_hdr_apps_showroom" -> showBuiltAppsShowroom
                                "chat_hdr_power_modules" -> showSelfUpgradeBar
                                else -> false
                            }
                            val tag = when (hdrNode.id) {
                                "chat_hdr_api_key" -> "first_position_api_key_btn"
                                "chat_hdr_new_chat" -> "gemini_new_chat_button"
                                "chat_hdr_history" -> "toggle_chat_history_drawer_btn"
                                "chat_hdr_review_page" -> "toggle_human_review_page_btn"
                                "chat_hdr_apps_showroom" -> "toggle_built_apps_showroom_btn"
                                "chat_hdr_power_modules" -> "toggle_power_modules_bar_btn"
                                "chat_hdr_code_import" -> "external_ai_code_harvester_btn"
                                else -> "omni_chat_hdr_${hdrNode.id}"
                            }
                            UniversalOmniControlEngine.OmniInteractiveButton(
                                node = hdrNode,
                                displayLabel = dispLabel,
                                isSelectedOrActive = isSelected,
                                baseHeightDp = 32f,
                                baseFontSp = 10f,
                                baseHorizPadDp = 8.5f,
                                testTagName = tag,
                                onClick = {
                                    when (hdrNode.id) {
                                        "chat_hdr_gemini_live" -> onStartGeminiLiveShare()
                                        "chat_hdr_api_key" -> showQuickApiKeyBar = !showQuickApiKeyBar
                                        "chat_hdr_new_chat" -> ChatSessionManager.startNewChat(context, "নতুন চ্যাট")
                                        "chat_hdr_history" -> showHistoryDrawer = !showHistoryDrawer
                                        "chat_hdr_review_page" -> {
                                            if (activeLiveModule == null) {
                                                val defaultFeature = savedSelfFeatures.firstOrNull()
                                                    ?: MasterOmniControllerEngine.savedSelfFeatures.value.firstOrNull()
                                                if (defaultFeature != null) {
                                                    AutonomousSelfEvolutionEngine.launchModuleInLiveRunner(defaultFeature, fullScreen = false)
                                                }
                                            }
                                            SovereignLiveReviewAndXRayEngine.inspectAndBuildHumanReview(
                                                context = context,
                                                module = activeLiveModule ?: savedSelfFeatures.firstOrNull(),
                                                activeBuildJob = activeBuildJob
                                            )
                                            showHumanReviewPage = !showHumanReviewPage
                                        }
                                        "chat_hdr_apps_showroom" -> showBuiltAppsShowroom = !showBuiltAppsShowroom
                                        "chat_hdr_power_modules" -> showSelfUpgradeBar = !showSelfUpgradeBar
                                        "chat_hdr_code_import" -> {
                                            val clip = clipboardManager.getText()?.text.orEmpty().ifBlank { userChatInput }
                                            codeRemixInputText = clip
                                            showCodeRemixDialog = true
                                        }
                                        else -> {
                                            // Full execution for any custom button, slot, website, or moved button placed in CHAT_HEADER!
                                            UniversalOmniControlEngine.controlFeatureStateAndPower(
                                                context = context,
                                                nodeId = hdrNode.id,
                                                enable = true,
                                                applyImmediately = true,
                                                extraCommand = hdrNode.customActionPayload
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // RIGHT SIDE: STOP TTS + AUTO-MODE BADGE + TOP-RIGHT [ ✕ ] (কাটার চিহ্ন) / FULL-SCREEN TOGGLE
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        if (isSpeakingTts) {
                            Surface(
                                color = CyberRose.copy(alpha = 0.22f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.9.dp, CyberRose),
                                modifier = Modifier.clickable { stopBanglaTts() }
                            ) {
                                Text(
                                    text = "⏹️ থামান",
                                    color = CyberRose,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Surface(
                            color = Color(0xFFECFDF5),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(0.9.dp, CyberEmerald.copy(alpha = 0.85f)),
                            modifier = Modifier
                                .clickable { onOpenAiKeySettings() }
                                .testTag("top_ai_key_status_badge")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = activeAutoModeBadge,
                                    color = Color(0xFF065F46),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // TOP-RIGHT [ ✕ কেটে সব বাটন আনুন ] (WHEN FULL-SCREEN) OR [ ⛶ শুধু চ্যাট বড় করুন ] (WHEN IN HUB)
                        Surface(
                            color = if (isImmersiveFullScreen) CyberRose else CyberCyan,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.2.dp, if (isImmersiveFullScreen) CyberRose else CyberCyan),
                            modifier = Modifier
                                .height(34.dp)
                                .clickable {
                                    if (isImmersiveFullScreen) {
                                        onExitFullScreenChat()
                                    } else {
                                        onToggleImmersiveFullScreen()
                                    }
                                }
                                .testTag("exit_fullscreen_chat_close_btn")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            ) {
                                Text(
                                    text = if (isImmersiveFullScreen) "✕ কেটে সব বাটন আনুন" else "⛶ চ্যাট বড় করুন",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 🎛️ GOOGLE AI STUDIO 0.0° DEVELOPER MODE, LIVE QUOTA REMAINING HUD & 5-WAKEUP (7x FEATURES) SCHEDULER BAR
        if (!isFullScreenRunnerMode || activeLiveModule == null) {
            AiStudioZeroDegreeLiveHudBar()
        }

        // 🔑 #1 FIRST-POSITION INSTANT API KEY PASTE & SAVE BAR (1-Tap Zero-Error Activation directly on Main Screen!)
        AnimatedVisibility(visible = showQuickApiKeyBar) {
            Surface(
                color = Color(0xFFECFDF5),
                border = BorderStroke(1.2.dp, CyberEmerald),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔑 ১-ট্যাপে API Key পেস্ট ও অটো চালু করুন (Gemini / Groq / OpenAI / DeepSeek):",
                            color = Color(0xFF065F46),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.weight(1f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.8.dp, CyberCyan),
                                modifier = Modifier.clickable {
                                    onOpenAiKeySettings()
                                }
                            ) {
                                Text(
                                    text = "⚙️ সব স্লট ও মডেল",
                                    color = CyberCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                color = Color(0xFFFFE4E6),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.clickable { showQuickApiKeyBar = false }
                            ) {
                                Text(
                                    text = "✕",
                                    color = CyberRose,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = quickApiKeyInput,
                            onValueChange = { quickApiKeyInput = it },
                            placeholder = {
                                Text(
                                    text = "এখানে আপনার API Key পেস্ট করুন (AIzaSy... / gsk_... / sk-...)...",
                                    color = CyberSlate,
                                    fontSize = 10.sp
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberEmerald,
                                unfocusedBorderColor = CyberCyan.copy(alpha = 0.6f),
                                focusedTextColor = Color(0xFF0F172A),
                                unfocusedTextColor = Color(0xFF0F172A),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quick_bar_api_key_input")
                        )
                        // 1-Tap Paste from Clipboard
                        Surface(
                            color = Color(0xFF0E2238),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(0.8.dp, CyberCyan),
                            modifier = Modifier
                                .height(46.dp)
                                .clickable {
                                    val clip = clipboardManager.getText()?.text.orEmpty().trim()
                                    if (clip.isNotBlank()) {
                                        quickApiKeyInput = clip
                                        val saved = UniversalAiManager.quickSaveAndActivateApiKey(context, clip)
                                        Toast.makeText(
                                            context,
                                            "⚡ ক্লিপবোর্ড থেকে API Key পেস্ট ও অটো চালু হয়েছে (${saved.provider.displayName})!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        showQuickApiKeyBar = false
                                    } else {
                                        Toast.makeText(context, "📋 ক্লিপবোর্ডে কোনো টেক্সট পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 9.dp)) {
                                Text("📋 পেস্ট", color = CyberCyan, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                        // 1-Tap Save & Activate
                        Surface(
                            color = CyberEmerald,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(46.dp)
                                .clickable {
                                    val keyToSave = quickApiKeyInput.trim().ifBlank {
                                        clipboardManager.getText()?.text.orEmpty().trim()
                                    }
                                    if (keyToSave.isNotBlank()) {
                                        val saved = UniversalAiManager.quickSaveAndActivateApiKey(context, keyToSave)
                                        quickApiKeyInput = ""
                                        showQuickApiKeyBar = false
                                        Toast.makeText(
                                            context,
                                            "⚡ API Key ১০০% সেভ ও সক্রিয় হয়েছে (${saved.provider.displayName})!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(context, "🔑 অনুগ্রহ করে আপনার API Key পেস্ট করুন", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .testTag("quick_bar_save_api_key_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 10.dp)) {
                                Text("⚡ সেভ ও চালু", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }
        }

        // 👁️ HUMAN-LANGUAGE VISUAL REVIEW & MICRO-FEATURE X-RAY PAGE ("মানুষের ভাষায় রিভিউ পেজ")
        AnimatedVisibility(visible = showHumanReviewPage) {
            SovereignHumanReviewPanel(
                snapshot = latestReviewSnapshot ?: SovereignLiveReviewAndXRayEngine.inspectAndBuildHumanReview(
                    context = context,
                    module = activeLiveModule,
                    activeBuildJob = activeBuildJob
                ),
                activeModule = activeLiveModule,
                activeBuildJob = activeBuildJob,
                activeWebView = activeWebView,
                onCloseReview = { showHumanReviewPage = false }
            )
        }

        // 🚀 MY BUILT APPS & 3D GAMES SHOWROOM DRAWER (Full-Screen App Launcher & .html Exporter)
        AnimatedVisibility(visible = showBuiltAppsShowroom && (!isFullScreenRunnerMode || activeLiveModule == null)) {
            Surface(
                color = Color(0xFF050A14),
                border = BorderStroke(0.9.dp, CyberPurple.copy(alpha = 0.55f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🚀 আপনার তৈরি প্রো অ্যাপস ও 3D গেমস শোরুম (${savedSelfFeatures.size}টি):",
                            color = CyberEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // 1-Tap Instant Sky-High 300% Predictive 3D WebGL Game Launcher (with SVG Logo, WebAudio, Shop & AutoBugGuard)
                            Surface(
                                color = CyberCyan.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.8.dp, CyberCyan),
                                modifier = Modifier.clickable {
                                    val gameBp = com.example.engine.SovereignPredictiveArchitectEngine.analyzeAndPredictBlueprint(
                                        context,
                                        "স্কাই-হাই 3D সাইবার-ভার্স এরিনা গেম"
                                    )
                                    val gameHtml = com.example.engine.SovereignPredictiveArchitectEngine.synthesizeSkyHighMasterpieceHtml(gameBp)
                                    val gameMod = SelfFeatureModule(
                                        id = "sky_3d_cyberverse_game_studio",
                                        title = "🎮 স্কাই-হাই 3D সাইবার-ভার্স গেম (লোগো + সাউন্ড + শপ + বাগ-শিল্ড)",
                                        description = "৩০০% স্বয়ংক্রিয় প্রিডিক্টিভ 3D গেম: কাস্টম SVG লোগো, ওয়েব-অডিও সিন্থ, আপগ্রেড শপ ও AutoBugGuard v4.0",
                                        categoryBadge = "SKY-HIGH 3D PRO 👑",
                                        jsPayload = gameHtml,
                                        isAutoRunOnPageLoad = true
                                    )
                                    MasterOmniControllerEngine.addOrUpdateSelfFeature(context, gameMod)
                                    AutonomousSelfEvolutionEngine.launchModuleInLiveRunner(gameMod, fullScreen = true)
                                    showBuiltAppsShowroom = false
                                }
                            ) {
                                Text(
                                    text = "🎮 +3D স্কাই-হাই গেম চালান",
                                    color = CyberCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                            Surface(
                                color = Color(0xFF1A0B12),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.clickable { showBuiltAppsShowroom = false }
                            ) {
                                Text(
                                    text = "✕ বন্ধ",
                                    color = CyberRose,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    if (savedSelfFeatures.isEmpty()) {
                        Text(
                            text = "এখনো কোনো কাস্টম অ্যাপ বা 3D গেম তৈরি করা হয়নি। নিচে চ্যাটবক্সে লিখুন বা '🎮 +3D গেম চালান' বাটনে ট্যাপ করুন!",
                            color = CyberSlate,
                            fontSize = 10.sp
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            savedSelfFeatures.forEach { feat ->
                                Surface(
                                    color = Color(0xFF0A1222),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(0.9.dp, CyberCyan.copy(alpha = 0.45f)),
                                    modifier = Modifier.width(210.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = feat.title,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = feat.categoryBadge,
                                            color = CyberEmerald,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Surface(
                                                color = CyberEmerald,
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.clickable {
                                                    AutonomousSelfEvolutionEngine.launchModuleInLiveRunner(feat, fullScreen = true)
                                                    showBuiltAppsShowroom = false
                                                }
                                            ) {
                                                Text(
                                                    text = "⛶ ফুল-স্ক্রিন খুলুন",
                                                    color = Color.Black,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                                )
                                            }
                                            Surface(
                                                color = Color(0xFF132238),
                                                shape = RoundedCornerShape(6.dp),
                                                border = BorderStroke(0.6.dp, CyberCyan),
                                                modifier = Modifier.clickable {
                                                    val path = MultiMinuteAutonomousBuilderEngine.exportStandaloneHtmlApp(context, feat)
                                                    if (path.isNotBlank()) {
                                                        Toast.makeText(context, "📦 এক্সপোর্ট সম্পন্ন: $path", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            ) {
                                                Text(
                                                    text = "📦 সেভ",
                                                    color = CyberCyan,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 📥 CODE REMIX, PASTE & INSTANT LIVE HARVEST PANEL
        AnimatedVisibility(visible = showCodeRemixDialog) {
            Surface(
                color = Color(0xFF060D1A),
                border = BorderStroke(1.2.dp, CyberCyan),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "📥 কোড ইমপোর্ট ও লাইভ রিমিক্স প্যানেল",
                                color = CyberCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "যেকোনো কোড পেস্ট করুন, ক্লিপবোর্ড থেকে নিন বা ব্রাউজার থেকে হারভেস্ট করে সাথে সাথে লাইভ অ্যাপ চালান",
                                color = CyberSlate,
                                fontSize = 9.sp
                            )
                        }
                        IconButton(
                            onClick = { showCodeRemixDialog = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = CyberRose, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = codeRemixInputText,
                        onValueChange = { codeRemixInputText = it },
                        placeholder = {
                            Text(
                                text = "এখানে আপনার HTML / JS / CSS বা সম্পূর্ণ কোড পেস্ট করুন...\nঅথবা নিচের '📋 ক্লিপবোর্ড পেস্ট' চাপুন",
                                color = CyberSlate,
                                fontSize = 11.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = Color(0xFF1E293B),
                            focusedContainerColor = Color(0xFF030712),
                            unfocusedContainerColor = Color(0xFF030712)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 90.dp, max = 150.dp),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = Color(0xFF1E293B),
                            shape = RoundedCornerShape(7.dp),
                            border = BorderStroke(0.8.dp, CyberCyan),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    val clip = clipboardManager.getText()?.text.orEmpty()
                                    if (clip.isNotBlank()) {
                                        codeRemixInputText = clip
                                        Toast.makeText(context, "📋 ক্লিপবোর্ড থেকে কোড নেওয়া হয়েছে!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "ক্লিপবোর্ডে কোনো লেখা বা কোড পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        ) {
                            Text(
                                text = "📋 ক্লিপবোর্ড পেস্ট",
                                color = CyberCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 7.dp)
                            )
                        }

                        Surface(
                            color = Color(0xFF132238),
                            shape = RoundedCornerShape(7.dp),
                            border = BorderStroke(0.8.dp, CyberPurple),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (activeWebView != null) {
                                        AiChromiumDevEngine.extractCodeBlocks(activeWebView) { extracted ->
                                            if (extracted.isNotBlank()) {
                                                codeRemixInputText = extracted
                                                Toast.makeText(context, "🌐 ব্রাউজার থেকে কোড সংগৃহীত হয়েছে!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "ব্রাউজারে কোনো কোড ব্লক পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, "ব্রাউজার ট্যাব সক্রিয় নেই", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        ) {
                            Text(
                                text = "🌐 ব্রাউজার হারভেস্ট",
                                color = CyberPurple,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 7.dp)
                            )
                        }

                        Surface(
                            color = CyberEmerald,
                            shape = RoundedCornerShape(7.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .clickable {
                                    val codeToRun = codeRemixInputText.trim().ifBlank {
                                        """
                                        <div style="background:#070E1C;color:#E2E8F0;padding:16px;border-radius:12px;border:1.5px solid #00F0FF;font-family:sans-serif;">
                                          <div style="color:#00F0FF;font-weight:bold;font-size:16px;">⚡ লাইভ রিমিক্স অ্যাপ প্রস্তুত!</div>
                                          <p style="color:#94A3B8;font-size:12px;margin-top:6px;">আপনার দেওয়া কোড ও প্রো অ্যাপটি এখানে সরাসরি রিয়েল-টাইমে চালু হয়েছে।</p>
                                          <div style="display:flex;gap:8px;margin-top:10px;">
                                            <button onclick="alert('✅ বাটন ১ কাজ করছে!')" style="background:#00F0FF;color:#000;border:none;padding:8px 12px;border-radius:6px;font-weight:bold;cursor:pointer;">বাটন ১ (সচল)</button>
                                            <button onclick="alert('🚀 বাটন ২ সচল!')" style="background:#10B981;color:#000;border:none;padding:8px 12px;border-radius:6px;font-weight:bold;cursor:pointer;">বাটন ২ (সচল)</button>
                                          </div>
                                        </div>
                                        """.trimIndent()
                                    }
                                    val sanitized = AutonomousSelfEvolutionEngine.sanitizeToPureExecutableHtmlOrJs(codeToRun, "লাইভ রিমিক্স মডিউল")
                                    val newModule = SelfFeatureModule(
                                        id = "sf_remix_" + UUID.randomUUID().toString().take(8),
                                        title = "⚡ লাইভ রিমিক্স মডিউল",
                                        description = "কোড ইমপোর্ট ও রিমিক্স প্যানেল থেকে নির্মিত সক্রিয় অ্যাপ",
                                        categoryBadge = "LIVE REMIX ⚡",
                                        jsPayload = sanitized,
                                        isAutoRunOnPageLoad = true
                                    )
                                    MasterOmniControllerEngine.addOrUpdateSelfFeature(context, newModule)
                                    AutonomousSelfEvolutionEngine.launchModuleInLiveRunner(newModule, fullScreen = false)
                                    SovereignLiveReviewAndXRayEngine.inspectAndBuildHumanReview(context, newModule, activeBuildJob)
                                    showCodeRemixDialog = false
                                    showHumanReviewPage = true
                                    Toast.makeText(context, "🚀 লাইভ অ্যাপ ও রিভিউ পেজ সক্রিয় হয়েছে!", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Text(
                                text = "🚀 রিমিক্স ও লাইভ চালান",
                                color = Color.Black,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }

        // 🔮 300% PREDICTIVE ARCHITECT, AUTO BUG-SHIELD & SMART HEAVY-PERMISSION PROPOSAL BAR
        AnimatedVisibility(visible = latestPredictiveBlueprint != null && (!isFullScreenRunnerMode || activeLiveModule == null)) {
            val bp = latestPredictiveBlueprint
            if (bp != null) {
                Surface(
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, CyberEmerald.copy(alpha = 0.65f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔮 ৩০০% প্রিডিক্টিভ আর্কিটেক্ট: ${bp.projectTitleBangla} • 🛡️ বাগ-শিল্ড ($bugsPreemptedCount ফিক্সড)",
                                color = Color(0xFF065F46),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = CyberEmerald,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.clickable {
                                    val msg = com.example.engine.SovereignPredictiveArchitectEngine.approveAllHeavyCapabilities(context, activeWebView)
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text(
                                    text = "👑 সব পারমিশন অন",
                                    color = Color.White,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            bp.heavyPermissionOptions.forEach { opt ->
                                Surface(
                                    color = if (opt.isApproved) Color(0xFFDCFCE7) else Color.White,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(
                                        0.8.dp,
                                        if (opt.isApproved) CyberEmerald else CyberAmber
                                    ),
                                    modifier = Modifier.clickable {
                                        val res = com.example.engine.SovereignPredictiveArchitectEngine.setHeavyCapabilityApproval(
                                            context = context,
                                            optionId = opt.optionId,
                                            approved = !opt.isApproved,
                                            activeWebView = activeWebView
                                        )
                                        Toast.makeText(context, res, Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Text(
                                        text = "${if (opt.isApproved) "✅" else "🔐"} ${opt.titleBangla}",
                                        color = Color(0xFF0F172A),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // SELF-UPGRADE & INSTALLED MODULES QUICK LAUNCHER BAR (1-Tap Add & Work Immediately)
        AnimatedVisibility(visible = showSelfUpgradeBar) {
            Surface(
                color = Color(0xFFF8FAFC),
                border = BorderStroke(0.8.dp, CyberEmerald.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡ স্বয়ংক্রিয় সেলফ-আপগ্রেড ও ১০টি আল্ট্রা-প্রো ফ্ল্যাগশিপ (${savedSelfFeatures.size}টি) — ট্যাপ বা মুখে বললেই সরাসরি চালু:",
                            color = CyberEmerald,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "META v${metaEngineState.metaLevel}.$evolutionVersion",
                            color = CyberCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // One-tap Self-Upgrade Presets (14 Ultra-Pro Flagships + Mega Apps + Meta-Engine + 12 Pro Laws + Utilities)
                        val quickUpgrades = listOf(
                            "⚡ এআই সুপারচার্জার ও টার্বো রাউটার" to "এআই সুপারচার্জার ও টার্বো বুস্টার অ্যাড করো",
                            "🧠 ডিপ-থিংক নিউরাল আর্কিটেক্ট" to "ডিপ থিংক নিউরাল আর্কিটেক্ট স্টুডিও অ্যাড করো",
                            "🛡️ জিরো-এরর কোয়ান্টাম কম্পাইলার" to "জিরো এরর কোয়ান্টাম কোড কম্পাইলার অ্যাড করো",
                            "📊 লাইভ ক্রিপ্টো ও মার্কেট রাডার" to "লাইভ ক্রিপ্টো ও মার্কেট রাডার প্রো অ্যাড করো",
                            "👑 অমনি ওএস (Zero-RAM)" to "ক্রোমিয়াম অমনি ওএস অ্যাড করো",
                            "🚀 ক্রোমিয়াম প্রো v4.0 আপডেট" to "ক্রোমিয়াম ইঞ্জিন আপডেট করো",
                            "🎯 স্ক্রিন অটো-টাচ" to "স্ক্রিন অটো টাচ ইঞ্জিন অ্যাড করো",
                            "⚡ পাওয়ার ও ৮জিবি MB" to "পাওয়ার ও মেগাবাইট রিঅ্যাক্টর অ্যাড করো",
                            "🛰️ ৩ডি সেন্সর রাডার" to "থ্রিডি স্যাটেলাইট রাডার ও সেন্সর ককপিট অ্যাড করো",
                            "🗄️ ডেটাবেস ও টাইম-মেশিন" to "ডেটাবেস ও টাইম মেশিন ভল্ট অ্যাড করো",
                            "📡 API ও QR মেকার" to "পোস্টম্যান এপিআই ও কিউআর মেকার অ্যাড করো",
                            "🩺 সেলফ-হিলিং ডক্টর" to "সেলফ হিলিং ডক্টর ও লাইভ বাগ স্ক্যানার অ্যাড করো",
                            "🌍 এআই মডেল রাডার" to "ইউনিভার্সাল এআই মডেল রাডার ও বেঞ্চমার্ক ল্যাব অ্যাড করো",
                            "🎙️ বাংলা ভয়েস ল্যাব" to "ইন্ডিয়ান বাংলা ভয়েস প্রি-অ্যাম্প ও ডিক্টেশন ল্যাব অ্যাড করো",
                            "🕸️ ডম এক্স-রে শিল্ড" to "সার্বভৌম ডম এক্স-রে ইন্সপেক্টর ও অ্যাড শিল্ড অ্যাড করো",
                            "⚡ ফাইল ও হেক্স বিল্ডার" to "কোয়ান্টাম ফাইল কনভার্টার ও হেক্স অ্যানালাইজার অ্যাড করো",
                            "🧠 মেটা-ইঞ্জিন আপগ্রেড" to "মেটা ইঞ্জিন আপগ্রেড করো",
                            "📜 ১২-প্রো নিয়ম" to "১২টি সার্বভৌম নিয়ম দেখাও",
                            "🎨 এআই জেনারেটর স্টুডিও" to "প্রো এআই জেনারেটর স্টুডিও অ্যাড করো",
                            "💻 ফুল-স্ট্যাক আইডিই" to "ফুল স্ট্যাক আইডিই কম্পাইলার অ্যাড করো",
                            "📊 চার্ট ও ফিন্যান্স" to "লাইভ চার্ট ও ফিন্যান্স ইএমআই স্টুডিও অ্যাড করো",
                            "🎬 অডিও ও ভয়েস স্টুডিও" to "মাল্টিমিডিয়া অডিও ও বাংলা ভয়েস স্টুডিও অ্যাড করো",
                            "🕵️ সিকিউরিটি ল্যাব" to "সাইবার সিকিউরিটি ও হ্যাশ ল্যাব অ্যাড করো",
                            "📋 প্রজেক্ট কানবান" to "প্রজেক্ট প্ল্যানার কানবান বোর্ড অ্যাড করো",
                            "🤖 অটোনমাস ওয়েব বট" to "অটোনমাস ওয়েব স্ক্র্যাপার বট অ্যাড করো",
                            "🧮 ক্যালকুলেটর" to "প্রো ক্যালকুলেটর অ্যাড করো",
                            "📝 নোটপ্যাড" to "অটো-সেভ নোটপ্যাড অ্যাড করো",
                            "🔐 পাসওয়ার্ড" to "পাসওয়ার্ড জেনারেটর অ্যাড করো",
                            "⏱️ স্টপওয়াচ" to "স্টপওয়াচ টাইমার অ্যাড করো",
                            "🌐 অনুবাদক" to "বাংলা ইংরেজি অনুবাদক অ্যাড করো",
                            "🛠️ JSON/Base64" to "জেসন ফরম্যাটার অ্যাড করো",
                            "👑 ওমেগা হাব" to "নিজেকে ওমেগা আপগ্রেড করো"
                        )
                        quickUpgrades.forEach { (chipLabel, orderCmd) ->
                            val isMetaChip = chipLabel.contains("মেটা-ইঞ্জিন")
                            Surface(
                                color = if (isMetaChip) Color(0xFFFFFBEB) else Color.White,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(
                                    0.9.dp,
                                    if (isMetaChip) CyberAmber else CyberCyan.copy(alpha = 0.65f)
                                ),
                                modifier = Modifier.clickable {
                                    executeChatOrEvolutionCommand(orderCmd)
                                }
                            ) {
                                Text(
                                    text = "+ $chipLabel",
                                    color = if (isMetaChip) CyberAmber else CyberCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Installed custom / active modules
                        savedSelfFeatures.forEach { feat ->
                            val isRunningNow = activeLiveModule?.id == feat.id
                            Surface(
                                color = if (isRunningNow) Color(0xFFECFDF5) else Color.White,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(
                                    0.9.dp,
                                    if (isRunningNow) CyberEmerald else CyberPurple.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.clickable {
                                    AutonomousSelfEvolutionEngine.launchModuleInLiveRunner(feat)
                                    if (activeWebView != null) {
                                        MasterOmniControllerEngine.runSelfFeatureOnWebView(context, feat, activeWebView)
                                    }
                                    Toast.makeText(context, "▶ চালু হয়েছে: ${feat.title}", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Run",
                                        tint = CyberEmerald,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = feat.title.take(22),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ITEM 2: MULTI-MINUTE AUTONOMOUS STEP-BY-STEP BUILDER LIVE TELEMETRY & DISK CHECKPOINT PANEL
        if (activeBuildJob != null) {
            val job = activeBuildJob!!
            val isNetworkPaused = job.status == BuildJobStatus.PAUSED_NETWORK_DROP
            val isUserPaused = job.status == BuildJobStatus.PAUSED_BY_USER
            val isRunning = job.status == BuildJobStatus.BLUEPRINTING ||
                job.status == BuildJobStatus.BUILDING_STEP ||
                job.status == BuildJobStatus.VERIFYING_AND_REPAIRING
            val isDone = job.status == BuildJobStatus.COMPLETED

            Surface(
                color = if (isNetworkPaused) Color(0xFF231604) else Color(0xFF071224),
                border = BorderStroke(
                    1.2.dp,
                    when {
                        isNetworkPaused -> CyberAmber
                        isDone -> CyberEmerald
                        else -> CyberCyan
                    }
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isRunning) {
                                CircularProgressIndicator(
                                    color = CyberCyan,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = "🛠️ মাল্টি-মিনিট বিল্ডার: ${job.featureTitle}",
                                color = if (isNetworkPaused) CyberAmber else if (isDone) CyberEmerald else CyberCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⏱️ ${MultiMinuteAutonomousBuilderEngine.formatDurationBangla(job.elapsedSeconds)} • 💾 ${job.diskCheckpointSizeKb}KB",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (isRunning) {
                                Surface(
                                    color = Color(0xFF271A04),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(0.7.dp, CyberAmber),
                                    modifier = Modifier.clickable {
                                        MultiMinuteAutonomousBuilderEngine.pauseBuild(context)
                                    }
                                ) {
                                    Text(
                                        text = "⏸️ পজ",
                                        color = CyberAmber,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            if (isNetworkPaused || isUserPaused) {
                                Surface(
                                    color = CyberEmerald.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(0.9.dp, CyberEmerald),
                                    modifier = Modifier.clickable {
                                        MultiMinuteAutonomousBuilderEngine.resumeBuild(
                                            context = context,
                                            activeWebView = activeWebView,
                                            onStepMessageToChat = { msg ->
                                                ChatSessionManager.addMessage(context, "AI", msg, false)
                                            }
                                        )
                                    }
                                ) {
                                    Text(
                                        text = "▶️ রিজিউম",
                                        color = CyberEmerald,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }

                                Surface(
                                    color = CyberCyan.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(0.9.dp, CyberCyan),
                                    modifier = Modifier.clickable {
                                        MultiMinuteAutonomousBuilderEngine.forceHealAndFinishPausedJob(
                                            context = context,
                                            activeWebView = activeWebView,
                                            onStepMessageToChat = { msg ->
                                                ChatSessionManager.addMessage(context, "AI", msg, false)
                                            }
                                        )
                                    }
                                ) {
                                    Text(
                                        text = "⚡ অটো-হিল ও ফিনিশ",
                                        color = CyberCyan,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Surface(
                                color = Color(0xFF2A0915),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.6.dp, CyberRose),
                                modifier = Modifier.clickable {
                                    if (isDone) {
                                        MultiMinuteAutonomousBuilderEngine.dismissCompletedBanner()
                                    } else {
                                        MultiMinuteAutonomousBuilderEngine.cancelAndClearJob(context)
                                    }
                                }
                            ) {
                                Text(
                                    text = if (isDone) "✖" else "❌ বাতিল",
                                    color = CyberRose,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = job.statusMessageBangla,
                        color = if (isNetworkPaused) CyberAmber else Color(0xFFE2E8F0),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Brain 1 Deep Thought Visualizer
                    if (job.deepThoughtBangla.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = Color(0xFF030710),
                            shape = RoundedCornerShape(7.dp),
                            border = BorderStroke(0.6.dp, CyberPurple.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "💭 ${job.deepThoughtBangla}",
                                color = Color(0xFFD8B4FE),
                                fontSize = 9.sp,
                                lineHeight = 13.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // Virtual Multi-File Project Disk Strip
                    if (job.virtualFilesList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📂 ডিস্ক ফাইল:", color = CyberEmerald, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                            job.virtualFilesList.forEach { vf ->
                                Surface(
                                    color = Color(0xFF051814),
                                    shape = RoundedCornerShape(5.dp),
                                    border = BorderStroke(0.5.dp, CyberEmerald.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = vf,
                                        color = CyberEmerald,
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (job.steps.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(5.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            job.steps.forEachIndexed { idx, step ->
                                val isCurr = idx == job.currentStepIndex && !step.isCompleted
                                Surface(
                                    color = when {
                                        step.isCompleted -> CyberEmerald.copy(alpha = 0.2f)
                                        isCurr -> CyberCyan.copy(alpha = 0.2f)
                                        else -> Color(0xFF0E1726)
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(
                                        0.7.dp,
                                        when {
                                            step.isCompleted -> CyberEmerald
                                            isCurr -> CyberCyan
                                            else -> Color(0xFF1E293B)
                                        }
                                    )
                                ) {
                                    Text(
                                        text = "${if (step.isCompleted) "✅" else if (isCurr) "⏳" else "•"} ধাপ ${idx + 1}: ${step.stepTitleBangla.take(24)}",
                                        color = if (step.isCompleted) CyberEmerald else if (isCurr) CyberCyan else CyberSlate,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // LIVE INTERACTIVE MODULE RUNNER PANEL (Supports both Compact 260dp & 100% Full-Screen Sovereign App Mode!)
        if (activeLiveModule != null) {
            val module = activeLiveModule!!
            Surface(
                color = Color(0xFF050B16),
                border = BorderStroke(1.2.dp, if (isFullScreenRunnerMode) CyberCyan else CyberEmerald),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                modifier = if (isFullScreenRunnerMode) {
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                }
            ) {
                Column(
                    modifier = if (isFullScreenRunnerMode) {
                        Modifier.fillMaxSize().padding(8.dp)
                    } else {
                        Modifier.fillMaxWidth().wrapContentHeight().padding(8.dp)
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(CyberEmerald)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${module.title}",
                                color = CyberEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 👁️ 1-Tap Human-Language Review Page Button for this running module
                            Surface(
                                color = if (showHumanReviewPage) CyberAmber.copy(alpha = 0.28f) else Color(0xFF271A04),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.8.dp, CyberAmber),
                                modifier = Modifier.clickable {
                                    SovereignLiveReviewAndXRayEngine.inspectAndBuildHumanReview(
                                        context = context,
                                        module = module,
                                        activeBuildJob = activeBuildJob
                                    )
                                    showHumanReviewPage = !showHumanReviewPage
                                }
                            ) {
                                Text(
                                    text = "👁️ রিভিউ পেজ",
                                    color = CyberAmber,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }

                            // Full-Screen / Compact Toggle Button
                            Surface(
                                color = if (isFullScreenRunnerMode) CyberCyan.copy(alpha = 0.25f) else Color(0xFF132238),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.8.dp, CyberCyan),
                                modifier = Modifier.clickable {
                                    AutonomousSelfEvolutionEngine.toggleFullScreenRunnerMode()
                                }
                            ) {
                                Text(
                                    text = if (isFullScreenRunnerMode) "🗕 ছোট করুন" else "⛶ ফুল-স্ক্রিন",
                                    color = CyberCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }

                            // Auto-Heal / Obstacle Fixer Button
                            Surface(
                                color = Color(0xFF271A04),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.6.dp, CyberAmber),
                                modifier = Modifier.clickable {
                                    val healRes = AutonomousSelfEvolutionEngine.diagnoseAndHealObstacle(
                                        context = context,
                                        rawProblemOrCode = module.jsPayload,
                                        activeWebView = activeWebView
                                    )
                                    SovereignLiveReviewAndXRayEngine.inspectAndBuildHumanReview(
                                        context = context,
                                        module = AutonomousSelfEvolutionEngine.activeLiveModule.value ?: module,
                                        activeBuildJob = activeBuildJob
                                    )
                                    Toast.makeText(context, healRes.actionTitleBangla, Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text(
                                    text = "🔧 অটো-হিল",
                                    color = CyberAmber,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }

                            // Send to Chromium Browser
                            Surface(
                                color = Color(0xFF132238),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.6.dp, CyberEmerald),
                                modifier = Modifier.clickable {
                                    if (activeWebView != null) {
                                        MasterOmniControllerEngine.runSelfFeatureOnWebView(context, module, activeWebView)
                                    }
                                    onSwitchToBrowserMode()
                                }
                            ) {
                                Text(
                                    text = "🌐 ব্রাউজারে",
                                    color = CyberEmerald,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }

                            IconButton(
                                onClick = { AutonomousSelfEvolutionEngine.dismissLiveRunner() },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Runner",
                                    tint = CyberRose,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Embedded Chromium Live Sandbox executing the Self-Feature Module 100% interactively
                    Box(
                        modifier = if (isFullScreenRunnerMode) {
                            Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .border(0.8.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
                        } else {
                            Modifier
                                .fillMaxWidth()
                                .height(255.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(0.8.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
                        }
                    ) {
                        val cleanHtmlDoc = remember(module.id, module.jsPayload) {
                            val omniBootstrapJs = ChromiumOmniZeroPressureEngine.buildOmniVirtualOsRuntimeBootstrapJs()
                            val bodyContent = AutonomousSelfEvolutionEngine.sanitizeToPureExecutableHtmlOrJs(
                                module.jsPayload,
                                module.title
                            )
                            """
                            <!DOCTYPE html>
                            <html>
                            <head>
                              <meta name="viewport" content="width=device-width, initial-scale=1.0">
                              <style>
                                body { margin: 0; padding: 6px; background: #040812; color: #E2E8F0; font-family: sans-serif; }
                                button { cursor: pointer; }
                              </style>
                              <script>
                                $omniBootstrapJs
                              </script>
                            </head>
                            <body>
                              $bodyContent
                            </body>
                            </html>
                            """.trimIndent()
                        }

                        var runnerWebViewRef by remember { mutableStateOf<WebView?>(null) }

                        DisposableEffect(module.id) {
                            onDispose {
                                // Zero-Pressure GPU & RAM Release when closing or switching module
                                try {
                                    runnerWebViewRef?.evaluateJavascript(
                                        "if(window.__releaseOmniWebGlGpu) window.__releaseOmniWebGlGpu(); if(window.OmniVirtualOS) window.OmniVirtualOS.purgeAllBlobMemory();",
                                        null
                                    )
                                    runnerWebViewRef?.clearCache(false)
                                    runnerWebViewRef?.freeMemory()
                                } catch (_: Throwable) {}
                            }
                        }

                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    runnerWebViewRef = this
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.databaseEnabled = true
                                    settings.allowFileAccess = true
                                    settings.allowContentAccess = true
                                    settings.mediaPlaybackRequiresUserGesture = false
                                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                    setBackgroundColor(android.graphics.Color.parseColor("#040812"))
                                    webViewClient = WebViewClient()
                                    webChromeClient = object : WebChromeClient() {
                                        override fun onPermissionRequest(request: PermissionRequest?) {
                                            request?.grant(request.resources)
                                        }
                                    }
                                    // PILLAR 3 & 4: Universal OmniNative Bridge (IndexedDB + Disk Vault + Live AI Brain + Hot-Patch)
                                    addJavascriptInterface(
                                        object {
                                            @JavascriptInterface
                                            fun saveToDiskVault(key: String, data: String): Boolean {
                                                return ChromiumOmniZeroPressureEngine.saveToNativeDiskVault(ctx, key, data)
                                            }

                                            @JavascriptInterface
                                            fun readFromDiskVault(key: String): String {
                                                return ChromiumOmniZeroPressureEngine.readFromNativeDiskVault(ctx, key)
                                            }

                                            @JavascriptInterface
                                            fun listDiskVaultKeys(): String {
                                                return ChromiumOmniZeroPressureEngine.listNativeDiskVaultKeys(ctx)
                                            }

                                            @JavascriptInterface
                                            fun purgeNativeRam() {
                                                post {
                                                    try {
                                                        clearCache(false)
                                                        freeMemory()
                                                        System.gc()
                                                        Toast.makeText(ctx, "🧹 জিরো-র‍্যাম ফ্লাশ সম্পন্ন! ফোনের র‍্যাম সম্পূর্ণ হালকা করা হয়েছে।", Toast.LENGTH_SHORT).show()
                                                    } catch (_: Throwable) {}
                                                }
                                            }

                                            @JavascriptInterface
                                            fun askAiFromJs(promptText: String, reqId: String) {
                                                scope.launch {
                                                    val res = UniversalAiManager.executeAiPrompt(
                                                        prompt = promptText,
                                                        includeStandingRules = false
                                                    )
                                                    val ans = res.getOrDefault("✅ সম্পন্ন হয়েছে")
                                                    val escapedAns = org.json.JSONObject.quote(ans)
                                                    post {
                                                        evaluateJavascript(
                                                            "if(window.__omniAiCallbacks && window.__omniAiCallbacks['$reqId']) window.__omniAiCallbacks['$reqId']($escapedAns); if(window.__onOmniAiResponse) window.__onOmniAiResponse('$reqId', $escapedAns);",
                                                            null
                                                        )
                                                    }
                                                }
                                            }

                                            @JavascriptInterface
                                            fun hotPatchCurrentModule(upgradeDirective: String) {
                                                post {
                                                    MultiMinuteAutonomousBuilderEngine.startMultiMinuteBuild(
                                                        context = ctx,
                                                        userOrder = upgradeDirective,
                                                        existingModuleToUpgrade = module,
                                                        activeWebView = activeWebView,
                                                        onStepMessageToChat = { msg ->
                                                            ChatSessionManager.addMessage(ctx, "AI", msg, false)
                                                        }
                                                    )
                                                    Toast.makeText(ctx, "🧬 হট-প্যাচ ও মাল্টি-মিনিট আপগ্রেড শুরু হয়েছে...", Toast.LENGTH_SHORT).show()
                                                }
                                            }

                                            @JavascriptInterface
                                            fun synthesizeNewOmniFeature(order: String) {
                                                post {
                                                    MultiMinuteAutonomousBuilderEngine.startMultiMinuteBuild(
                                                        context = ctx,
                                                        userOrder = order,
                                                        existingModuleToUpgrade = null,
                                                        activeWebView = activeWebView,
                                                        onStepMessageToChat = { msg ->
                                                            ChatSessionManager.addMessage(ctx, "AI", msg, false)
                                                        }
                                                    )
                                                    Toast.makeText(ctx, "🛠️ নতুন অমনি ফিচার বিল্ড শুরু হয়েছে...", Toast.LENGTH_SHORT).show()
                                                }
                                            }

                                            @JavascriptInterface
                                            fun evalInBrowserTab(jsCode: String) {
                                                post {
                                                    activeWebView?.evaluateJavascript(jsCode, null)
                                                }
                                            }
                                        },
                                        "OmniNative"
                                    )
                                    addJavascriptInterface(
                                        object {
                                            @JavascriptInterface
                                            fun showToast(msg: String) {
                                                post {
                                                    Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
                                                }
                                            }

                                            @JavascriptInterface
                                            fun speak(text: String) {
                                                post {
                                                    speakBanglaAloud(text)
                                                }
                                            }

                                            @JavascriptInterface
                                            fun vibrate(ms: Long) {
                                                try {
                                                    val vib = ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                        vib?.vibrate(VibrationEffect.createOneShot(ms.coerceIn(10L, 1000L), VibrationEffect.DEFAULT_AMPLITUDE))
                                                    } else {
                                                        @Suppress("DEPRECATION")
                                                        vib?.vibrate(ms.coerceIn(10L, 1000L))
                                                    }
                                                } catch (_: Exception) {}
                                            }

                                            @JavascriptInterface
                                            fun copyToClipboard(text: String) {
                                                post {
                                                    clipboardManager.setText(AnnotatedString(text))
                                                    Toast.makeText(ctx, "📋 ক্লিপবোর্ডে কপি হয়েছে!", Toast.LENGTH_SHORT).show()
                                                }
                                            }

                                            @JavascriptInterface
                                            fun setFullScreen(enabled: Boolean) {
                                                post {
                                                    AutonomousSelfEvolutionEngine.setFullScreenRunnerMode(enabled)
                                                }
                                            }

                                            @JavascriptInterface
                                            fun addDynamicTopButton(id: String, title: String, icon: String, zone: String, htmlPayload: String) {
                                                post {
                                                    UniversalOmniControlEngine.autoRegisterFeature(
                                                        context = ctx,
                                                        id = id,
                                                        title = title,
                                                        icon = icon,
                                                        zone = zone,
                                                        htmlCode = htmlPayload
                                                    )
                                                    Toast.makeText(ctx, "✅ নতুন বাটন যুক্ত হয়েছে: $title ($zone)", Toast.LENGTH_SHORT).show()
                                                }
                                            }

                                            @JavascriptInterface
                                            fun savePermanentFeature(title: String, category: String, htmlCode: String) {
                                                post {
                                                    val cleanId = "sov_" + title.lowercase().replace(Regex("[^a-z0-9]"), "_").take(24)
                                                    ChromiumOmniZeroPressureEngine.saveToNativeDiskVault(ctx, "feature_$cleanId", htmlCode)
                                                    val newMod = SelfFeatureModule(
                                                        id = "genesis_$cleanId",
                                                        title = title,
                                                        description = "Sovereign Auto Feature ($category)",
                                                        categoryBadge = category,
                                                        jsPayload = htmlCode,
                                                        isAutoRunOnPageLoad = true
                                                    )
                                                    MasterOmniControllerEngine.addOrUpdateSelfFeature(ctx, newMod)
                                                }
                                            }

                                            @JavascriptInterface
                                            fun spawnNewModule(title: String, htmlCode: String) {
                                                post {
                                                    val sanitized = AutonomousSelfEvolutionEngine.sanitizeToPureExecutableHtmlOrJs(htmlCode, title)
                                                    val newMod = SelfFeatureModule(
                                                        title = "⚡ $title",
                                                        description = "লাইভ আইডিই থেকে নির্মিত প্রো মডিউল",
                                                        categoryBadge = "OMNI ZERO-RAM 👑",
                                                        jsPayload = sanitized,
                                                        isAutoRunOnPageLoad = true
                                                    )
                                                    ChromiumOmniZeroPressureEngine.saveToNativeDiskVault(ctx, newMod.id, sanitized)
                                                    MasterOmniControllerEngine.addOrUpdateSelfFeature(ctx, newMod)
                                                    AutonomousSelfEvolutionEngine.launchModuleInLiveRunner(newMod)
                                                    Toast.makeText(ctx, "✅ নতুন অ্যাপ যুক্ত ও চালু হয়েছে: $title", Toast.LENGTH_SHORT).show()
                                                }
                                            }

                                            @JavascriptInterface
                                            fun upgradeMetaEngine(directive: String) {
                                                post {
                                                    val res = AutonomousSelfEvolutionEngine.upgradeMetaEngineItself(
                                                        context = ctx,
                                                        userDirective = directive,
                                                        activeWebView = activeWebView
                                                    )
                                                    ChatSessionManager.addMessage(ctx, "AI", res.detailedResponseBangla, false)
                                                    Toast.makeText(ctx, res.actionTitleBangla, Toast.LENGTH_SHORT).show()
                                                }
                                            }

                                            @JavascriptInterface
                                            fun startNativeAutoTouch(x: Float, y: Float, speedMs: Long) {
                                                post {
                                                    com.example.service.AutoTouchService.startAutoTouchLoop(
                                                        x = x,
                                                        y = y,
                                                        speedMs = speedMs
                                                    )
                                                    Toast.makeText(ctx, "🎯 স্ক্রিন অটো-টাচ চালু হয়েছে (${x.toInt()}, ${y.toInt()}) @ ${speedMs}ms", Toast.LENGTH_SHORT).show()
                                                }
                                            }

                                            @JavascriptInterface
                                            fun stopNativeAutoTouch() {
                                                post {
                                                    com.example.service.AutoTouchService.stopAutoTouchLoop()
                                                    Toast.makeText(ctx, "🛑 স্ক্রিন অটো-টাচ বন্ধ করা হয়েছে", Toast.LENGTH_SHORT).show()
                                                }
                                            }

                                            @JavascriptInterface
                                            fun setPowerAndMb(powerPct: Int, memoryMb: Int) {
                                                post {
                                                    com.example.service.AutoTouchService.setPowerAndMemoryAllocation(
                                                        powerPct = powerPct,
                                                        memoryMb = memoryMb
                                                    )
                                                    Toast.makeText(ctx, "⚡ পাওয়ার: ${powerPct}% | মেমোরি ভল্ট: ${memoryMb} MB সেট হয়েছে!", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        "SovereignOS"
                                    )
                                }
                            },
                            update = { wv ->
                                runnerWebViewRef = wv
                                if (wv.tag != module.id + "_" + module.jsPayload.hashCode()) {
                                    wv.tag = module.id + "_" + module.jsPayload.hashCode()
                                    wv.loadDataWithBaseURL(
                                        "https://skrl2.sovereign.local/",
                                        cleanHtmlDoc,
                                        "text/html",
                                        "UTF-8",
                                        null
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // HISTORY SESSIONS DRAWER (When toggled open)
        AnimatedVisibility(visible = showHistoryDrawer) {
            Surface(
                color = Color(0xFF070E1C),
                border = BorderStroke(0.8.dp, CyberCyan.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "📜 সংরক্ষিত চ্যাট সেশন (${sessionSummaries.size}টি):",
                        color = CyberCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        sessionSummaries.forEach { sum ->
                            val isCurrent = sum.id == activeSession?.id
                            Surface(
                                color = if (isCurrent) CyberCyan.copy(alpha = 0.22f) else CyberCardBg,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(
                                    0.8.dp,
                                    if (isCurrent) CyberCyan else Color(0xFF26334D)
                                ),
                                modifier = Modifier.clickable {
                                    ChatSessionManager.selectSession(context, sum.id)
                                    showHistoryDrawer = false
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${sum.title} (${sum.messageCount})",
                                        color = if (isCurrent) CyberCyan else Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (!isCurrent) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = CyberRose.copy(alpha = 0.8f),
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable {
                                                    ChatSessionManager.deleteSession(context, sum.id)
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // CHAT MESSAGES STREAM (Hidden when Full-Screen Sovereign App Mode is active so the App gets 100% space)
        if (!isFullScreenRunnerMode || activeLiveModule == null) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "👑 সার্বভৌম সেলফ-আপগ্রেডিং এআই প্রস্তুত",
                        color = CyberCyan,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "মুখে (MIC) বা লিখে যেকোনো আদেশ দিন — যেমন: 'ক্যালকুলেটর অ্যাড করো', 'নোটপ্যাড বানাও', 'নকশা বক্সা দেখাও' বা 'নিজেকে আপগ্রেড করো'। অ্যাড করার সাথে সাথে আপনার সামনেই সেটি ১০০% কাজ শুরু করবে!",
                        color = CyberSlate,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    if (activeProfile == null || activeProfile.lastValidatedStatus != "VALID") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onOpenAiKeySettings,
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("নতুন API Key যোগ করুন (+)", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        val isUser = msg.sender == "USER"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            // Crisp White Daylight Contrast Hierarchy:
                            // User bubble: Soft Sky-Blue Card (#E0F2FE) with Dark Navy Text (#0F172A)
                            // AI bubble: Clean Porcelain Card (#F8FAFC) with Dark Slate Text (#0F172A)
                            Surface(
                                color = if (isUser) Color(0xFFE0F2FE) else Color(0xFFF8FAFC),
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 3.dp,
                                    bottomEnd = if (isUser) 3.dp else 16.dp
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isUser) CyberCyan.copy(alpha = 0.65f) else Color(0xFFCBD5E1)
                                ),
                                modifier = Modifier.fillMaxWidth(if (isUser) 0.88f else 0.96f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isUser) {
                                                "👤 আপনি (You)"
                                            } else {
                                                "🤖 Gemini AI"
                                            },
                                            color = if (isUser) CyberCyan else CyberEmerald,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.sp
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val isSpeakingThisMsg = isSpeakingTts && speakingMsgId == msg.id
                                            Surface(
                                                color = if (isSpeakingThisMsg) CyberRose.copy(alpha = 0.18f) else Color.Transparent,
                                                shape = RoundedCornerShape(6.dp),
                                                border = if (isSpeakingThisMsg) BorderStroke(0.7.dp, CyberRose) else null,
                                                modifier = Modifier.clickable {
                                                    speakBanglaAloud(msg.text, msg.id)
                                                }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = if (isSpeakingThisMsg) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                                        contentDescription = "Speak or Stop in Indian Bengali",
                                                        tint = if (isSpeakingThisMsg) CyberRose else CyberCyan,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    if (isSpeakingThisMsg) {
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(
                                                            text = "থামান",
                                                            color = CyberRose,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.ExtraBold
                                                        )
                                                    }
                                                }
                                            }
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(msg.text))
                                                    Toast.makeText(context, "📋 মেসেজ কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.ContentCopy,
                                                    contentDescription = "Copy",
                                                    tint = CyberSlate,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = msg.text,
                                        color = Color(0xFF0F172A),
                                        fontSize = 13.sp,
                                        lineHeight = 19.sp,
                                        fontFamily = if (msg.isCode) FontFamily.Monospace else FontFamily.Default
                                    )

                                    // If AI response contains code/HTML/JS, provide 1-tap "Install & Run Live Now" button
                                    if (!isUser && (msg.text.contains("<script") || msg.text.contains("<div") || msg.text.contains("function") || msg.text.contains("```"))) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                val sanitizedPayload = AutonomousSelfEvolutionEngine.sanitizeToPureExecutableHtmlOrJs(
                                                    rawInput = msg.text,
                                                    fallbackTitle = "চ্যাট থেকে নির্মিত লাইভ ফিচার"
                                                )
                                                val module = SelfFeatureModule(
                                                    title = "⚡ চ্যাট লাইভ ফিচার",
                                                    description = "চ্যাট আউটপুট থেকে স্বয়ংক্রিয়ভাবে স্যানিটাইজড ও লাইভ কার্যকরী ফিচার",
                                                    categoryBadge = "LIVE CHAT MODULE",
                                                    jsPayload = sanitizedPayload,
                                                    isAutoRunOnPageLoad = true
                                                )
                                                MasterOmniControllerEngine.addOrUpdateSelfFeature(context, module)
                                                if (activeWebView != null) {
                                                    MasterOmniControllerEngine.runSelfFeatureOnWebView(context, module, activeWebView)
                                                }
                                                AutonomousSelfEvolutionEngine.launchModuleInLiveRunner(module)
                                                Toast.makeText(context, "🚀 ফিচারটি অ্যাপে যুক্ত হয়েছে এবং উপরে লাইভ চালু করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("🚀 এই ফিচারটি অ্যাপে যুক্ত ও এখনই লাইভ রান করুন", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isAiGenerating) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Surface(
                                    color = Color(0xFFECFDF5),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, CyberEmerald.copy(alpha = 0.6f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            color = CyberEmerald,
                                            strokeWidth = 2.dp,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (activeExecMode == SovereignExecutionMode.GOD_MULTI_AI_DEBATE) {
                                                "⚔️ গড-লেভেল ৩-ব্রেন ডিবেট চলছে (Proposer ∥ Critic ∥ Arbiter)... (${thinkingElapsedSeconds}s) ${liveDebateBanner}"
                                            } else {
                                                "🧠 গড-লেভেল ডিপ থিংকিং ও মেমোরি সিঙ্ক চলছে... (${thinkingElapsedSeconds} সেকেন্ড)"
                                            },
                                            color = Color(0xFF065F46),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        }

        // LIVE VOICE LISTENING STRIP (Acoustic Gain & Distant Speech Indicator — 100% In-App)
        AnimatedVisibility(visible = isVoiceListening) {
            Surface(
                color = Color(0xFFFFF1F2),
                border = BorderStroke(1.dp, CyberRose.copy(alpha = 0.75f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(CyberRose)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        val liveTranscript = userChatInput.ifBlank { bufferedSpeech }
                        Text(
                            text = if (liveTranscript.isNotBlank())
                                "🎙️ \"$liveTranscript\""
                            else
                                "🔴 MIC শুনছি (ইন্ডিয়ান বাংলা - bn-IN | লাইভ স্ট্রিমিং)... বলুন",
                            color = if (liveTranscript.isNotBlank()) Color.White else CyberRose,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Dynamic RMS Waveform Bars, Auto-Send Toggle & Finish/Send Action
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val h1 = (6.dp + (liveRms * 14).dp).coerceIn(4.dp, 20.dp)
                        val h2 = (9.dp + (liveRms * 20).dp).coerceIn(4.dp, 22.dp)
                        val h3 = (5.dp + (liveRms * 12).dp).coerceIn(4.dp, 18.dp)
                        Box(modifier = Modifier.width(3.dp).height(h1).clip(RoundedCornerShape(2.dp)).background(CyberCyan))
                        Box(modifier = Modifier.width(3.dp).height(h2).clip(RoundedCornerShape(2.dp)).background(CyberRose))
                        Box(modifier = Modifier.width(3.dp).height(h3).clip(RoundedCornerShape(2.dp)).background(CyberEmerald))

                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            color = if (voiceAutoSendEnabled) CyberAmber.copy(alpha = 0.25f) else Color(0xFF131D31),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(0.6.dp, if (voiceAutoSendEnabled) CyberAmber else CyberSlate),
                            modifier = Modifier.clickable { voiceAutoSendEnabled = !voiceAutoSendEnabled }
                        ) {
                            Text(
                                text = if (voiceAutoSendEnabled) "⚡ অটো-সেন্ড: অন" else "✍️ ইনপুটে রাখুন",
                                color = if (voiceAutoSendEnabled) CyberAmber else CyberCyan,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp)
                            )
                        }

                        val readyText = userChatInput.ifBlank { bufferedSpeech }
                        if (readyText.isNotBlank()) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Surface(
                                color = CyberEmerald.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.7.dp, CyberEmerald),
                                modifier = Modifier.clickable {
                                    onToggleVoiceMic()
                                    val cmd = readyText.trim()
                                    userChatInput = ""
                                    executeChatOrEvolutionCommand(cmd)
                                }
                            ) {
                                Text(
                                    text = "🚀 পাঠান",
                                    color = CyberEmerald,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            color = Color(0xFF131D31),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(0.6.dp, CyberRose.copy(alpha = 0.6f)),
                            modifier = Modifier.clickable { onToggleVoiceMic() }
                        ) {
                            Text(
                                text = "⏹️ সমাপ্ত",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        // OFFLINE VAULT SPEECH RECOVERY BANNER (When WiFi/Internet drops while speaking)
        AnimatedVisibility(visible = bufferedSpeech.isNotBlank() && micStatus == MicNetworkStatus.OFFLINE_BUFFERED_RETRY) {
            Surface(
                color = Color(0xFF271A04),
                border = BorderStroke(1.dp, CyberAmber.copy(alpha = 0.85f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.WifiOff,
                            contentDescription = "Offline",
                            tint = CyberAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(7.dp))
                        Column {
                            Text(
                                text = "⚠️ নেট ড্রপ করেছিল • আপনার কথা সুরক্ষিত আছে!",
                                color = CyberAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "\"${bufferedSpeech.take(55)}\"",
                                color = Color.White,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Button(
                        onClick = {
                            userChatInput = bufferedSpeech
                            SovereignVoiceVaultEngine.clearBuffer()
                            Toast.makeText(context, "✅ সুরক্ষিত কথা ইনপুটে আনা হয়েছে!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberAmber),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = Color.Black, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("🔄 ইনপুট করুন", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        // PERSISTENT CROSS-APP BACKGROUND ROBOT STATUS STRIP (When running while user watches YouTube / Chrome)
        AnimatedVisibility(visible = bgTaskState.isRunning) {
            Surface(
                color = Color(0xFF064E3B),
                border = BorderStroke(1.2.dp, CyberEmerald),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🤖 ${bgTaskState.currentPhaseTitleBangla} (YouTube/Chrome-এ গেলেও চলবে)",
                            color = CyberEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = bgTaskState.liveProgressDetailBangla,
                            color = Color.White,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Surface(
                        color = CyberRose,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable {
                            SovereignMetaKernelEngine.stopBackgroundAutonomousOrder(context)
                        }
                    ) {
                        Text(
                            text = "⏹️ থামান",
                            color = Color.White,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // BOTTOM INPUT BAR + GEMINI-STYLE LIVE & GOD-LEVEL DEBATE MODE BAR
        Surface(
            color = Color(0xFFF8FAFC),
            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Quick Mode & Live Selector Pill Row right above the input box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isDebateMode = activeExecMode == SovereignExecutionMode.GOD_MULTI_AI_DEBATE
                    val isBgRobotMode = activeExecMode == SovereignExecutionMode.BACKGROUND_AUTONOMOUS_ROBOT

                    // Option 1: Standard God-Level Deep-Thinking Mode
                    Surface(
                        color = if (!isDebateMode && !isBgRobotMode) Color(0xFFE0F2FE) else Color.White,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (!isDebateMode && !isBgRobotMode) CyberCyan else Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .clickable {
                                SovereignMetaKernelEngine.setExecutionMode(context, SovereignExecutionMode.GOD_DEEP_THINK)
                                Toast.makeText(context, "🧠 গড-লেভেল ডিপ-থিংক মোড সক্রিয়", Toast.LENGTH_SHORT).show()
                            }
                            .testTag("chat_mode_deep_think_pill")
                    ) {
                        Text(
                            text = "🧠 ডিপ-থিংক (1s Fast+Deep)",
                            color = Color(0xFF0F172A),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Option 2: God-Level 3-Brain Debate Mode
                    Surface(
                        color = if (isDebateMode) Color(0xFFFEF3C7) else Color.White,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.2.dp, if (isDebateMode) CyberAmber else Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .clickable {
                                val next = SovereignMetaKernelEngine.toggleDebateMode(context)
                                Toast.makeText(
                                    context,
                                    if (next == SovereignExecutionMode.GOD_MULTI_AI_DEBATE) "⚔️ গড-লেভেল ৩-ব্রেন ডিবেট মোড চালু!" else "🧠 ডিপ-থিংক মোড চালু!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .testTag("chat_mode_god_debate_pill")
                    ) {
                        Text(
                            text = if (isDebateMode) "⚔️ ডিবেট মোড: চালু ✅" else "⚔️ ডিবেট মোড (3-Brain)",
                            color = if (isDebateMode) Color(0xFF92400E) else Color(0xFF0F172A),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Option 3: Launch Gemini Live DIRECTLY in Live Debate Mode (Runs outside app too!)
                    Surface(
                        color = Color(0xFFFFF1F2),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CyberRose),
                        modifier = Modifier
                            .clickable {
                                SovereignGeminiLiveOmniEngine.setLiveIntelligenceMode(LiveIntelligenceMode.GOD_LEVEL_LIVE_DEBATE)
                                onStartGeminiLiveShare()
                            }
                            .testTag("chat_launch_live_debate_pill")
                    ) {
                        Text(
                            text = "🔴⚔️ লাইভ ডিবেট (বাইরেও চলবে)",
                            color = CyberRose,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Option 4: Background Autonomous Robot Mode (Give order & open YouTube/Chrome)
                    Surface(
                        color = if (isBgRobotMode) Color(0xFFDCFCE7) else Color.White,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CyberEmerald),
                        modifier = Modifier
                            .clickable {
                                if (userChatInput.isNotBlank()) {
                                    val ord = userChatInput.trim()
                                    userChatInput = ""
                                    ChatSessionManager.addMessage(context, "USER", "🤖 [ব্যাকগ্রাউন্ড অর্ডার]: $ord")
                                    SovereignMetaKernelEngine.startBackgroundAutonomousOrder(context, ord)
                                } else {
                                    val next = if (isBgRobotMode) SovereignExecutionMode.GOD_DEEP_THINK else SovereignExecutionMode.BACKGROUND_AUTONOMOUS_ROBOT
                                    SovereignMetaKernelEngine.setExecutionMode(context, next)
                                    Toast.makeText(
                                        context,
                                        "🤖 ব্যাকগ্রাউন্ড রোবট মোড নির্বাচিত — এবার যেকোনো অর্ডার লিখে পাঠান এবং নিশ্চিন্তে YouTube/Chrome চালান!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                            .testTag("chat_mode_bg_robot_pill")
                    ) {
                        Text(
                            text = if (isBgRobotMode) "🤖 ব্যাকগ্রাউন্ড অর্ডার: অন ✅" else "🤖 ব্যাকগ্রাউন্ডে কাজ দিন (YouTube মোড)",
                            color = Color(0xFF065F46),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Option 5: 1-Tap Exact AI Model Switcher (AUTO-RACE ⇄ 2.5-Pro ⇄ 2.5-Flash ⇄ 2.0-Flash)
                    val curModel = activeProfile?.modelName?.ifBlank { "AUTO" } ?: "AUTO"
                    Surface(
                        color = Color(0xFFF3E8FF),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CyberPurple),
                        modifier = Modifier
                            .clickable {
                                val available = (listOf("AUTO", "gemini-2.5-pro", "gemini-2.5-flash", "gemini-2.0-flash") + (activeProfile?.availableModels ?: emptyList())).distinct()
                                val idx = available.indexOfFirst { it.equals(curModel, ignoreCase = true) }
                                val nextMdl = available.getOrElse((idx + 1) % available.size) { "AUTO" }
                                UniversalAiManager.switchModelOnSameKeyInstant(context, activeProfile?.id, nextMdl)
                                Toast.makeText(context, "🤖 সক্রিয় মডেল এখন: $nextMdl", Toast.LENGTH_SHORT).show()
                            }
                            .testTag("chat_model_cycle_pill")
                    ) {
                        Text(
                            text = "🤖 মডেল: ${if (curModel == "AUTO") "AUTO-RACE (1s)" else curModel} 🔄",
                            color = Color(0xFF581C87),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Option 6: Hardware RAM & Storage Cap Governor (128MB -> 256MB -> 512MB -> 1GB -> 2GB)
                    Surface(
                        color = Color(0xFFECFDF5),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CyberEmerald),
                        modifier = Modifier
                            .clickable {
                                val nextRam = when (quotaTelemetry.userRamCapMb) {
                                    128L -> 256L
                                    256L -> 512L
                                    512L -> 1024L
                                    1024L -> 2048L
                                    else -> 128L
                                }
                                val report = FileStreamHelper.setRamAndStorageQuotaCap(context, nextRam, nextRam * 2L)
                                Toast.makeText(context, report, Toast.LENGTH_SHORT).show()
                            }
                            .testTag("chat_ram_storage_cap_pill")
                    ) {
                        val ramLabel = if (quotaTelemetry.userRamCapMb >= 1024L) "${quotaTelemetry.userRamCapMb / 1024}GB" else "${quotaTelemetry.userRamCapMb}MB"
                        val diskLabel = if (quotaTelemetry.userStorageCapMb >= 1024L) "${quotaTelemetry.userStorageCapMb / 1024}GB" else "${quotaTelemetry.userStorageCapMb}MB"
                        Text(
                            text = "💾 ক্যাপ: ${ramLabel} RAM / ${diskLabel} Disk 🔄",
                            color = Color(0xFF065F46),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Option 7: 1-Tap AI 8K Image / Photo Generator Studio ("যাতে ছবি করা যায়")
                    Surface(
                        color = Color(0xFFFCE7F3),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CyberRose),
                        modifier = Modifier
                            .clickable {
                                val imgOrder = userChatInput.trim().ifBlank { "Futuristic Sovereign King AI Core in 8K Ultra HD" }
                                userChatInput = ""
                                val report = com.example.engine.SovereignGodEngine.launchGodLevelAiImageGeneratorStudio(
                                    context = context,
                                    rawUserOrder = imgOrder,
                                    activeWebView = activeWebView
                                )
                                ChatSessionManager.addMessage(context, "USER", "🎨 [এআই ছবি তৈরি]: $imgOrder")
                                ChatSessionManager.addMessage(context, "AI", report, false)
                            }
                            .testTag("chat_ai_image_studio_pill")
                    ) {
                        Text(
                            text = "🎨 ছবি বানাও (8K AI)",
                            color = Color(0xFF9D174D),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Option 8: Set SKRL 2 as Default Home-Button Assistant (Replaces Google Gemini!)
                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.2.dp, CyberAmber),
                        modifier = Modifier
                            .clickable {
                                val msg = com.example.engine.SovereignGodEngine.openDefaultDigitalAssistantSettings(context)
                                ChatSessionManager.addMessage(context, "AI", msg, false)
                                Toast.makeText(context, "👑 Default Digital Assistant-এ SKRL 2 সিলেক্ট করুন!", Toast.LENGTH_LONG).show()
                            }
                            .testTag("chat_set_default_assistant_pill")
                    ) {
                        Text(
                            text = "👑 হোম বাটনে আমার অ্যাপ (Replace Gemini)",
                            color = Color(0xFF92400E),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Option 9: 1-Tap Phone Flashlight / Hardware Control
                    Surface(
                        color = Color(0xFFE0F2FE),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CyberCyan),
                        modifier = Modifier
                            .clickable {
                                val nextTorch = !com.example.engine.SovereignGodEngine.isTorchOn.value
                                val msg = com.example.engine.SovereignGodEngine.togglePhoneFlashlight(context, nextTorch)
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                            .testTag("chat_phone_torch_pill")
                    ) {
                        Text(
                            text = "🔦 ফোনের টর্চ অন/অফ",
                            color = Color(0xFF0369A1),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Main Input Row: [MIC] + [📋 Paste] + [TextField] + [⚔️ Debate] + [🔴 LIVE (Gemini Style)] + [Send]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Indian Bengali Hands-Free Auto-VAD Real-Time Live Translator MIC Button
                    val isMicAutoStandby = isVoiceListening && liveVoiceTranscript.startsWith("⏸️")
                    Surface(
                        color = when {
                            isMicAutoStandby -> Color(0xFFFFFBEB)
                            isVoiceListening -> Color(0xFFFFF1F2)
                            else -> Color.White
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(
                            width = 1.2.dp,
                            color = when {
                                isMicAutoStandby -> CyberAmber
                                isVoiceListening -> CyberRose
                                else -> CyberCyan
                            }
                        ),
                        modifier = Modifier
                            .height(44.dp)
                            .clickable { onToggleVoiceMic() }
                            .testTag("gemini_chat_voice_mic_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isVoiceListening) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = "Pro Voice MIC",
                                tint = when {
                                    isMicAutoStandby -> CyberAmber
                                    isVoiceListening -> CyberRose
                                    else -> CyberCyan
                                },
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = if (isVoiceListening) "প্রো..." else "প্রো",
                                color = Color(0xFF0F172A),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // 1B. Classic Normal OS Mic Button ("নরমাল মাইক" — 100% foolproof system speech recognizer)
                    Surface(
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.1.dp, CyberEmerald),
                        modifier = Modifier
                            .height(44.dp)
                            .clickable {
                                try {
                                    val intent = com.example.voice.NativeVoiceEngine.createSpeechIntent("bn-IN")
                                    normalSystemMicLauncher.launch(intent)
                                } catch (_: Throwable) {
                                    com.example.voice.NativeVoiceEngine.setNormalSingleShotMicMode(true)
                                    onToggleVoiceMic()
                                }
                            }
                            .testTag("gemini_chat_normal_mic_btn")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 6.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🎤", fontSize = 12.sp)
                            Text(
                                text = "নরমাল",
                                color = Color(0xFF065F46),
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // 2. 1-Tap Clipboard Paste Button right next to Input Box
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.7f)),
                        modifier = Modifier
                            .height(44.dp)
                            .clickable {
                                val clip = clipboardManager.getText()?.text.orEmpty()
                                if (clip.isNotBlank()) {
                                    userChatInput = if (userChatInput.isBlank()) clip else "$userChatInput
$clip"
                                    SovereignMetaKernelEngine.triggerPredictiveShadowPrethink(context, userChatInput)
                                    Toast.makeText(context, "📋 পেস্ট করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "📋 ক্লিপবোর্ড খালি", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .testTag("gemini_chat_paste_btn")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 7.dp)
                        ) {
                            Text(
                                text = "📋",
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(5.dp))

                    // 3. Text Input with Predictive Shadow Pre-Thinking on every keystroke
                    OutlinedTextField(
                        value = userChatInput,
                        onValueChange = {
                            userChatInput = it
                            SovereignMetaKernelEngine.triggerPredictiveShadowPrethink(context, it)
                        },
                        placeholder = {
                            val modeHint = when (activeExecMode) {
                                SovereignExecutionMode.GOD_MULTI_AI_DEBATE -> "⚔️ ৩-ব্রেন ডিবেটের জন্য লিখুন বা পেস্ট করুন..."
                                SovereignExecutionMode.BACKGROUND_AUTONOMOUS_ROBOT -> "🤖 ব্যাকগ্রাউন্ড অর্ডার লিখুন (YouTube ঘাঁটলেও চলবে)..."
                                else -> "বলুন বা লিখুন (পাশে 🔴 LIVE ও ⚔️ ডিবেট বাটন)..."
                            }
                            Text(modeHint, color = CyberSlate, fontSize = 10.5.sp)
                        },
                        maxLines = 4,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("gemini_chat_text_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (activeExecMode == SovereignExecutionMode.GOD_MULTI_AI_DEBATE) CyberAmber else CyberCyan,
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.width(5.dp))

                    // 3.8. Original 1-Tap Up-to-30-Minute Autonomous Step-by-Step Deep-Think Builder Button (100% Same-to-Same!)
                    if (userChatInput.isNotBlank() && !isAiGenerating) {
                        Surface(
                            color = Color(0xFFECFDF5),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.2.dp, CyberEmerald),
                            modifier = Modifier
                                .height(44.dp)
                                .clickable {
                                    val buildOrder = userChatInput.trim()
                                    userChatInput = ""
                                    ChatSessionManager.addMessage(context, "USER", "🛠️ [৩০-মিনিট ডিপ-থিংক অটোনোমাস বিল্ড অর্ডার]: $buildOrder")
                                    MultiMinuteAutonomousBuilderEngine.startMultiMinuteBuild(
                                        context = context,
                                        userOrder = buildOrder,
                                        existingModuleToUpgrade = activeLiveModule,
                                        activeWebView = activeWebView,
                                        targetDeepThinkMinutes = 30,
                                        onStepMessageToChat = { msg ->
                                            ChatSessionManager.addMessage(context, "AI", msg, false)
                                        }
                                    )
                                }
                                .testTag("gemini_multi_minute_build_btn")
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🛠️ ৩০মিঃ বিল্ড", color = CyberEmerald, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                                Text("ডিপ-থিংক", color = Color(0xFF0F172A), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.width(5.dp))
                    }

                    // 4. Side Button 1: God-Level Debate Mode Quick Toggle (⚔️)
                    val isDebateOn = activeExecMode == SovereignExecutionMode.GOD_MULTI_AI_DEBATE
                    Surface(
                        color = if (isDebateOn) CyberAmber else Color(0xFFFFFBEB),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.2.dp, CyberAmber),
                        modifier = Modifier
                            .height(44.dp)
                            .clickable {
                                val next = SovereignMetaKernelEngine.toggleDebateMode(context)
                                if (userChatInput.isNotBlank() && next == SovereignExecutionMode.GOD_MULTI_AI_DEBATE && !isAiGenerating) {
                                    executeChatOrEvolutionCommand(userChatInput)
                                } else {
                                    Toast.makeText(
                                        context,
                                        if (next == SovereignExecutionMode.GOD_MULTI_AI_DEBATE) "⚔️ গড-লেভেল ৩-ব্রেন ডিবেট মোড চালু!" else "🧠 ডিপ-থিংক মোড চালু!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            .testTag("gemini_input_side_debate_btn")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 7.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "⚔️",
                                fontSize = 12.sp
                            )
                            Text(
                                text = "ডিবেট",
                                color = Color(0xFF0F172A),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(5.dp))

                    // 5. Side Button 2: GEMINI-STYLE LIVE MODE BUTTON (🔴 LIVE) right beside Input/Send!
                    // Runs in-app AND outside in the background (Chrome, YouTube, anywhere) with both Deep-Think & Live Debate!
                    Surface(
                        color = Color(0xFFFFF1F2),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.4.dp, CyberRose),
                        modifier = Modifier
                            .height(44.dp)
                            .clickable {
                                if (activeExecMode == SovereignExecutionMode.GOD_MULTI_AI_DEBATE) {
                                    SovereignGeminiLiveOmniEngine.setLiveIntelligenceMode(LiveIntelligenceMode.GOD_LEVEL_LIVE_DEBATE)
                                } else {
                                    SovereignGeminiLiveOmniEngine.setLiveIntelligenceMode(LiveIntelligenceMode.DEEP_THINK_KNOWLEDGE)
                                }
                                onStartGeminiLiveShare()
                            }
                            .testTag("gemini_input_side_live_mode_btn")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🔴 LIVE",
                                color = CyberRose,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "বাইরেও চলে",
                                color = Color(0xFF0F172A),
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(5.dp))

                    // 6. Send Button
                    IconButton(
                        onClick = {
                            executeChatOrEvolutionCommand(userChatInput)
                        },
                        enabled = userChatInput.isNotBlank() && !isAiGenerating,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (userChatInput.isNotBlank() && !isAiGenerating) CyberCyan else Color(0xFFE2E8F0))
                            .testTag("gemini_chat_send_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (userChatInput.isNotBlank() && !isAiGenerating) Color.White else CyberSlate
                        )
                    }
                }
            }
        }

        // Living Cellular Anatomy & Button Matrix Modal Sheet
        if (showAnatomySheet) {
            AppCellularAnatomySheet(
                activeWebView = activeWebView,
                activeScreen = "AI_CHAT",
                onDismissRequest = { showAnatomySheet = false }
            )
        }

        if (showTruthDossierSheet) {
            AppTruthDossierSheet(onDismiss = { showTruthDossierSheet = false })
        }
    }
}

