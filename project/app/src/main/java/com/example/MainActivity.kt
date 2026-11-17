package com.example

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Message
import android.speech.RecognizerIntent
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import java.io.ByteArrayInputStream
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.bridge.GodBridge
import com.example.engine.AiChromiumDevEngine
import com.example.engine.AutonomousSelfEvolutionEngine
import com.example.engine.AutonomousTaskEngine
import com.example.engine.ChatSessionManager
import com.example.engine.FileStreamHelper
import com.example.engine.GhostMacroAndSnifferEngine
import com.example.engine.GlobalRuleVaultEngine
import com.example.engine.MultiMinuteAutonomousBuilderEngine
import com.example.engine.GoalHeuristics
import com.example.engine.GoalType
import com.example.engine.GodModeAutoSync
import com.example.engine.MasterOmniControllerEngine
import com.example.engine.NanobotSwarmOrchestrator
import com.example.engine.OmniAppControlBridge
import com.example.engine.QuadChatAndStrainerEngine
import com.example.engine.SmartContextCompressorEngine
import com.example.engine.StrainedStepStatus
import com.example.engine.SwarmMemoryVaultEngine
import com.example.engine.SovereignDeepResearchAndIncrementalEngine
import com.example.engine.TaskGearType
import com.example.engine.TaskStatus
import com.example.engine.UniversalAiManager
import com.example.engine.VoiceAiTranslator
import com.example.service.ChromiumBackgroundService
import com.example.ui.LiveVoiceWaveform
import com.example.ui.ai.AiSummarizerSheet
import com.example.ui.ai.AppCellularAnatomySheet
import com.example.ui.ai.AppTruthDossierSheet
import com.example.ui.ai.ChatHistoryDrawerSheet
import com.example.ui.ai.QuadChatWorkspaceSheet
import android.media.projection.MediaProjectionManager
import androidx.compose.ui.platform.LocalView
import com.example.engine.SovereignGeminiLiveOmniEngine
import com.example.ui.ai.SovereignGeminiLiveGlobalOverlay
import com.example.ui.ai.UniversalAiSettingsSheet
import com.example.engine.AppTruthAndArchitectureVault
import com.example.ui.autonomy.AutonomyPermissionsSheet
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberQuickActionChip
import com.example.ui.theme.CyberRose
import com.example.ui.theme.CyberSlate
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.cyberAmbientBackground
import com.example.ui.workflow.AiDevConsoleSheet
import com.example.ui.workflow.GlobalRuleAndPlannerSheet
import com.example.ui.ai.PureGeminiChatWorkspace
import com.example.ui.ai.ParticleChatWorkspace
import com.example.engine.ParticleSwarmManager
import com.example.engine.UniversalOmniControlEngine
import com.example.engine.OmniNode
import com.example.ui.workflow.LiveSandboxSheet
import com.example.ui.workflow.ProToolsHubSheet
import com.example.ui.workflow.WorkflowScreen
import com.example.voice.NativeVoiceEngine
import kotlinx.coroutines.launch

enum class MainScreenMode {
    AI_CHAT,         // 💬 এআই চ্যাট (Pure Gemini Master Chat)
    PARTICLES_CHAT,  // ✨ পার্টিকলস চ্যাট (Particle Swarm & Web Automation Chat)
    BROWSER          // 🌐 ক্রোমিয়াম ব্রাউজার (Chromium Engine Workspace)
}

data class ChromiumTabState(
    val id: Int,
    val title: String,
    val url: String
)

class WebAppInterface(
    private val context: Context,
    private val onStartListening: () -> Unit,
    private val onStopListening: () -> Unit,
    private val onNavigateUrl: (String) -> Unit,
    private val onOpenNanobotStudio: () -> Unit,
    private val onOpenQuadChat: (Int) -> Unit = {},
    private val onOpenDevConsole: () -> Unit = {},
    private val onOpenProLab: (Int) -> Unit = {},
    private val onExecuteOmniCommand: (String) -> Unit = {}
) {
    @JavascriptInterface
    fun startNativeVoice() {
        onStartListening()
    }

    @JavascriptInterface
    fun stopNativeVoice() {
        onStopListening()
    }

    @JavascriptInterface
    fun loadUrlInChromium(url: String) {
        onNavigateUrl(url)
    }

    @JavascriptInterface
    fun openNanobotSwarm() {
        onOpenNanobotStudio()
    }

    @JavascriptInterface
    fun openQuadChatChamber(chamberIndex: Int) {
        onOpenQuadChat(chamberIndex)
    }

    @JavascriptInterface
    fun openSovereignDevConsole() {
        onOpenDevConsole()
    }

    @JavascriptInterface
    fun openProLabTab(tabIndex: Int) {
        onOpenProLab(tabIndex)
    }

    @JavascriptInterface
    fun executeOmniAiCommand(command: String) {
        onExecuteOmniCommand(command)
    }

    @JavascriptInterface
    fun showToast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
}

class MainActivity : ComponentActivity() {

    private var nativeVoiceEngine: NativeVoiceEngine? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        com.example.engine.ResourceGuard.init(this)
        UniversalAiManager.init(this)
        ChatSessionManager.init(this)
        AiChromiumDevEngine.init(this)
        GlobalRuleVaultEngine.init(this)
        SwarmMemoryVaultEngine.init(this)
        GhostMacroAndSnifferEngine.init(this)
        MasterOmniControllerEngine.init(this)
        QuadChatAndStrainerEngine.init(this)
        AppTruthAndArchitectureVault.init(this)
        ParticleSwarmManager.init(this)
        UniversalOmniControlEngine.init(this)
        SovereignDeepResearchAndIncrementalEngine.init(this)
        com.example.engine.SovereignPredictiveArchitectEngine.init(this)
        AutonomousSelfEvolutionEngine.initialize(this)
        SovereignGeminiLiveOmniEngine.init(this)
        com.example.engine.SovereignMetaKernelEngine.init(this)

        try {
            val serviceIntent = Intent(this, ChromiumBackgroundService::class.java)
            startService(serviceIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            MyApplicationTheme {
                MainAutonomousWorkspace(
                    onInitVoiceEngine = { onPartial, onFinal, onRms, onState, onErr ->
                        if (nativeVoiceEngine == null) {
                            nativeVoiceEngine = NativeVoiceEngine(
                                context = this,
                                onPartialResult = onPartial,
                                onFinalCommand = onFinal,
                                onRmsChanged = onRms,
                                onStateChanged = onState,
                                onErrorNotice = onErr
                            )
                        }
                        nativeVoiceEngine
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val act = intent.action.orEmpty()
        if (act == Intent.ACTION_ASSIST || act == Intent.ACTION_VOICE_COMMAND || act == Intent.ACTION_SEARCH_LONG_PRESS ||
            intent.getBooleanExtra("launched_from_home_assist_button", false)
        ) {
            Toast.makeText(this, "👑 হোম বাটন থেকে আপনার সার্বভৌম এআই চালু হয়েছে!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val isLive = SovereignGeminiLiveOmniEngine.sessionState.value.isLiveActive
        val isBgTask = com.example.engine.SovereignMetaKernelEngine.backgroundTaskState.value.isRunning
        if (isLive || isBgTask) {
            if (isLive) {
                SovereignGeminiLiveOmniEngine.setMinimizedCapsuleMode(true)
            }
            com.example.service.SovereignMediaProjectionService.startPersistentLiveBackgroundService(applicationContext)
            com.example.service.SovereignMediaProjectionService.showSystemOverlayIfPermitted(applicationContext)
            if (isLive && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    val pipParams = android.app.PictureInPictureParams.Builder()
                        .setAspectRatio(android.util.Rational(16, 9))
                        .build()
                    enterPictureInPictureMode(pipParams)
                } catch (_: Exception) {
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        nativeVoiceEngine?.destroy()
    }
}

private fun formatOmniboxUrl(rawInput: String, searchMode: String = "all"): String {
    var trimmed = rawInput.trim()
    if (trimmed.isEmpty()) return "https://www.google.com"

    val googlePrefixes = listOf("https://www.google.com/", "https://www.google.com", "http://www.google.com/", "http://www.google.com")
    for (prefix in googlePrefixes) {
        if (trimmed.startsWith(prefix) && trimmed.length > prefix.length) {
            val remainder = trimmed.removePrefix(prefix).trim()
            if (!remainder.startsWith("search?") && !remainder.startsWith("url?") && !remainder.startsWith("imgres?")) {
                trimmed = remainder
                break
            }
        }
    }

    if (searchMode == "direct_site") {
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed
        if (trimmed.contains(".") && !trimmed.contains(" ")) return "https://$trimmed"
        val lowerName = trimmed.lowercase()
        return when {
            lowerName.contains("youtube") || lowerName.contains("ইউটিউব") -> "https://m.youtube.com"
            lowerName.contains("chatgpt") || lowerName.contains("চ্যাটজিপিটি") -> "https://chatgpt.com"
            lowerName.contains("gemini") || lowerName.contains("জেমিনি") -> "https://gemini.google.com"
            lowerName.contains("claude") || lowerName.contains("ক্লড") -> "https://claude.ai"
            lowerName.contains("deepseek") || lowerName.contains("ডিপসিক") -> "https://chat.deepseek.com"
            lowerName.contains("facebook") || lowerName.contains("ফেসবুক") -> "https://m.facebook.com"
            lowerName.contains("instagram") || lowerName.contains("ইনস্টাগ্রাম") -> "https://www.instagram.com"
            lowerName.contains("github") || lowerName.contains("গিটহাব") -> "https://github.com"
            lowerName.contains("google") || lowerName.contains("গুগল") -> "https://www.google.com"
            lowerName.contains("wikipedia") || lowerName.contains("উইকিপিডিয়া") -> "https://bn.m.wikipedia.org"
            else -> "https://www.google.com/search?q=${android.net.Uri.encode(trimmed)}&btnI=I"
        }
    }

    if (searchMode == "images") {
        val cleanQuery = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            android.net.Uri.parse(trimmed).getQueryParameter("q") ?: "HD Photos"
        } else {
            trimmed
        }
        return "https://www.google.com/search?tbm=isch&q=${android.net.Uri.encode(cleanQuery)}"
    } else if (searchMode == "videos") {
        val cleanQuery = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            android.net.Uri.parse(trimmed).getQueryParameter("q") ?: "Trending Videos"
        } else {
            trimmed
        }
        return "https://www.google.com/search?tbm=vid&q=${android.net.Uri.encode(cleanQuery)}"
    } else if (searchMode == "news") {
        val cleanQuery = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            android.net.Uri.parse(trimmed).getQueryParameter("q") ?: "Latest News"
        } else {
            trimmed
        }
        return "https://www.google.com/search?tbm=nws&q=${android.net.Uri.encode(cleanQuery)}"
    }

    if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("file://")) {
        return trimmed
    }
    return if (trimmed.contains(".") && !trimmed.contains(" ") && !trimmed.endsWith(".")) {
        "https://$trimmed"
    } else {
        "https://www.google.com/search?q=${android.net.Uri.encode(trimmed)}"
    }
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAutonomousWorkspace(
    onInitVoiceEngine: (
        onPartial: (String) -> Unit,
        onFinal: (String, String) -> Unit,
        onRms: (Float) -> Unit,
        onState: (Boolean) -> Unit,
        onErr: (String) -> Unit
    ) -> NativeVoiceEngine?
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    val tabs = remember {
        mutableStateListOf(
            ChromiumTabState(id = 1, title = "🌐 Tab A", url = "https://www.google.com"),
            ChromiumTabState(id = 2, title = "🌐 Tab B", url = "https://www.google.com")
        )
    }
    var activeTabIndex by remember { mutableIntStateOf(0) }
    var tabANavTriggerId by remember { mutableIntStateOf(1) }
    var tabBNavTriggerId by remember { mutableIntStateOf(1) }
    var splitScreenDualMode by remember { mutableStateOf(false) }
    var isDesktopMode by remember { mutableStateOf(false) }
    var isHeaderCompactMode by remember { mutableStateOf(false) }
    var isFloatingPillMinimized by remember { mutableStateOf(false) }
    var addressBarInput by remember { mutableStateOf("") }
    var mainScreenMode by remember { mutableStateOf(MainScreenMode.AI_CHAT) }
    var isImmersiveChatFullScreen by remember { mutableStateOf(false) }
    var pendingVoiceChatCommand by remember { mutableStateOf("") }
    var pendingVoiceChatTriggerId by remember { mutableIntStateOf(0) }

    var webViewTabA by remember { mutableStateOf<WebView?>(null) }
    var webViewTabB by remember { mutableStateOf<WebView?>(null) }

    val taskEngine = remember { AutonomousTaskEngine(context) }
    val checklistTasks by taskEngine.tasks.collectAsState()
    val currentTaskIdx by taskEngine.currentActiveTaskIndex.collectAsState()
    val engineLogText by taskEngine.engineLog.collectAsState()

    val relayFeed by NanobotSwarmOrchestrator.relayFeed.collectAsState()
    val isSwarmRunning by NanobotSwarmOrchestrator.isSwarmRunning.collectAsState()
    val swarmStatusBanner by NanobotSwarmOrchestrator.activeStatusBanner.collectAsState()

    // Sheet visibility states
    var showNanobotStudio by remember { mutableStateOf(false) }
    var showGlobalRuleAndPlannerSheet by remember { mutableStateOf(false) }
    var plannerSheetInitialTab by remember { mutableIntStateOf(0) }
    var showLiveSandboxSheet by remember { mutableStateOf(false) }
    var sandboxCustomHtml by remember { mutableStateOf("") }
    var showProToolsHubSheet by remember { mutableStateOf(false) }
    var proToolsInitialTab by remember { mutableIntStateOf(0) }
    var showTaskQueueSheet by remember { mutableStateOf(false) }
    var showAiSettingsSheet by remember { mutableStateOf(false) }
    var showOmniLayoutVisualEditor by remember { mutableStateOf(false) }
    var showSummarizerSheet by remember { mutableStateOf(false) }
    var showChatHistorySheet by remember { mutableStateOf(false) }
    var showQuadChatSheet by remember { mutableStateOf(false) }
    var quadChatInitialChamber by remember { mutableIntStateOf(0) }
    var showDevConsoleSheet by remember { mutableStateOf(false) }
    var showPermissionsSheet by remember { mutableStateOf(false) }
    var showOnScreenReplyModal by remember { mutableStateOf(false) }
    var showCellularAnatomySheet by remember { mutableStateOf(false) }
    var showTruthDossierSheet by remember { mutableStateOf(false) }

    val isMacroRecording by GhostMacroAndSnifferEngine.isRecordingMacro.collectAsState()
    val sniffedPackets by GhostMacroAndSnifferEngine.capturedPackets.collectAsState()
    val currentGear by SmartContextCompressorEngine.currentGear.collectAsState()
    val isAutoGear by SmartContextCompressorEngine.isAutoGearRouterEnabled.collectAsState()
    val strainedSteps by QuadChatAndStrainerEngine.strainedSteps.collectAsState()
    val stagingItems by QuadChatAndStrainerEngine.stagingItems.collectAsState()
    val uiButtons by AiChromiumDevEngine.uiButtons.collectAsState()

    // Voice & Live state
    var isVoiceListening by remember { mutableStateOf(false) }
    var isContinuousVoiceActive by remember { mutableStateOf(false) }
    var voiceRms by remember { mutableFloatStateOf(0f) }
    var liveVoiceTranscript by remember { mutableStateOf("") }
    var statusBannerText by remember {
        mutableStateOf("🤖 ন্যানোবট সোয়ার্ম ও সার্বভৌম ক্রোমিয়াম প্রস্তুত — যেকোনো আদেশ দিন")
    }

    val omniControlBridge = remember {
        OmniAppControlBridge(
            onSetSplitScreen = { enabled -> splitScreenDualMode = enabled },
            onSetDesktopMode = { enabled ->
                isDesktopMode = enabled
                webViewTabA?.let { AiChromiumDevEngine.applyDeveloperSettings(it, enabled) }
                webViewTabB?.let { AiChromiumDevEngine.applyDeveloperSettings(it, enabled) }
            },
            onSetContinuousVoice = { enabled ->
                isContinuousVoiceActive = enabled
            },
            onNavigateTabA = { rawUrl ->
                val url = formatOmniboxUrl(rawUrl)
                tabs[0] = tabs[0].copy(url = url)
                webViewTabA?.loadUrl(url)
            },
            onNavigateTabB = { rawUrl ->
                val url = formatOmniboxUrl(rawUrl)
                tabs[1] = tabs[1].copy(url = url)
                webViewTabB?.loadUrl(url)
            },
            onOpenSwarmStudio = { showNanobotStudio = true },
            onOpenRuleAndPlanner = { tabIdx ->
                plannerSheetInitialTab = tabIdx
                showGlobalRuleAndPlannerSheet = true
            },
            onOpenProLabTab = { tabIdx ->
                proToolsInitialTab = tabIdx
                showProToolsHubSheet = true
            },
            onOpenLiveSandbox = {
                sandboxCustomHtml = ""
                showLiveSandboxSheet = true
            },
            onOpenAiKeySettings = {
                showAiSettingsSheet = true
            },
            onDeployAndRunSwarmBlueprint = { blueprint ->
                showProToolsHubSheet = false
                showNanobotStudio = true
                NanobotSwarmOrchestrator.startContinuousRelay(
                    context = context,
                    nodes = blueprint.nodes,
                    connections = blueprint.connections,
                    webViewA = webViewTabA,
                    webViewB = webViewTabB ?: webViewTabA,
                    onCanvasInvalidate = {},
                    onCheckpointReached = {
                        showOnScreenReplyModal = true
                    }
                )
            },
            getActiveWebView = {
                if (activeTabIndex == 1 && webViewTabB != null) webViewTabB else webViewTabA
            }
        )
    }

    val currentComposeView = LocalView.current

    val onPartialCallback: (String) -> Unit = { partial ->
        liveVoiceTranscript = partial
        if (SovereignGeminiLiveOmniEngine.sessionState.value.isLiveActive) {
            SovereignGeminiLiveOmniEngine.onUserSpeechActivityDetected(partial, voiceRms)
        }
    }
    val onFinalCallback: (String, String) -> Unit = { finalSpeech, lang ->
        val cleanedSpeech = finalSpeech.trim()
        liveVoiceTranscript = cleanedSpeech
        if (cleanedSpeech.isNotBlank()) {
            if (SovereignGeminiLiveOmniEngine.sessionState.value.isLiveActive) {
                statusBannerText = "🔴 [Gemini Live]: $cleanedSpeech"
                val targetWv = if (activeTabIndex == 1 && webViewTabB != null) webViewTabB else webViewTabA
                SovereignGeminiLiveOmniEngine.processLiveUserVoiceCommand(
                    context = context,
                    spokenText = cleanedSpeech,
                    rootView = currentComposeView.rootView,
                    activeWebView = targetWv
                )
            } else if (mainScreenMode == MainScreenMode.BROWSER) {
                statusBannerText = "🎙️ ভয়েস ব্রাউজার সার্চ: $cleanedSpeech"
                val targetWv = if (activeTabIndex == 1) webViewTabB else webViewTabA
                val lower = cleanedSpeech.lowercase()
                val isDomNavigationCmd = lower.contains("নিচে") || lower.contains("উপরে") ||
                    lower.contains("স্ক্রল") || lower.contains("scroll") ||
                    lower.contains("পেছনে") || lower.contains("সামনে") ||
                    lower.contains("রিফ্রেশ") || lower.contains("বড় করো") || lower.contains("ছোট করো")

                if (isDomNavigationCmd) {
                    val jsCode = VoiceAiTranslator.translateVoiceToJs(cleanedSpeech, lang)
                    targetWv?.evaluateJavascript(jsCode, null)
                } else {
                    val searchUrl = formatOmniboxUrl(cleanedSpeech)
                    addressBarInput = cleanedSpeech
                    if (activeTabIndex == 1) {
                        tabs[1] = tabs[1].copy(url = searchUrl)
                        tabBNavTriggerId += 1
                        webViewTabB?.loadUrl(searchUrl)
                    } else {
                        tabs[0] = tabs[0].copy(url = searchUrl)
                        tabANavTriggerId += 1
                        webViewTabA?.loadUrl(searchUrl)
                    }
                    Toast.makeText(context, "🔍 সার্চ করা হচ্ছে: $cleanedSpeech", Toast.LENGTH_SHORT).show()
                }
            } else {
                statusBannerText = "🎙️ ভয়েস আদেশ গৃহীত: $cleanedSpeech"
                pendingVoiceChatCommand = cleanedSpeech
                pendingVoiceChatTriggerId += 1
            }
        }
    }
    val onRmsCallback: (Float) -> Unit = { rms ->
        voiceRms = rms
        if (SovereignGeminiLiveOmniEngine.sessionState.value.isLiveActive) {
            SovereignGeminiLiveOmniEngine.onUserSpeechActivityDetected(liveVoiceTranscript, rms)
        }
    }
    val onStateCallback: (Boolean) -> Unit = { active -> isVoiceListening = active }
    val onErrCallback: (String) -> Unit = { err -> statusBannerText = "🎙️ $err" }

    val voiceEngine = remember {
        onInitVoiceEngine(
            onPartialCallback,
            onFinalCallback,
            onRmsCallback,
            onStateCallback,
            onErrCallback
        )
    }

    // Keep voice engine callbacks updated with latest Compose state on every recomposition
    voiceEngine?.updateCallbacks(
        onPartial = onPartialCallback,
        onFinal = onFinalCallback,
        onRms = onRmsCallback,
        onState = onStateCallback,
        onErr = onErrCallback
    )

    DisposableEffect(Unit) {
        onDispose {
            voiceEngine?.stopListening()
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val seedText = if (mainScreenMode == MainScreenMode.BROWSER) addressBarInput else pendingVoiceChatCommand
                voiceEngine?.startListening("bn-IN", seedText)
                Toast.makeText(context, "🎙️ অটো-মাইক চালু — আপনি বললেই বাংলা হয়ে বসবে, চুপ করলে স্ট্যান্ডবাই থাকবে!", Toast.LENGTH_SHORT).show()
            } catch (_: Throwable) {
                isVoiceListening = false
                Toast.makeText(context, "🎙️ ভয়েস ইঞ্জিন স্ট্যান্ডবাই মোডে প্রস্তুত", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "🎙️ মাইক্রোফোন পারমিশন দিলে ভয়েস কমান্ড সরাসরি কাজ করবে", Toast.LENGTH_LONG).show()
        }
    }

    val toggleVoiceAction: () -> Unit = {
        try {
            if (isVoiceListening) {
                voiceEngine?.stopListening()
                isVoiceListening = false
                Toast.makeText(context, "⏹️ অটো-ভয়েস মাইক বন্ধ করা হয়েছে", Toast.LENGTH_SHORT).show()
            } else {
                if (!NativeVoiceEngine.hasRecordAudioPermission(context)) {
                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                } else {
                    val seedText = if (mainScreenMode == MainScreenMode.BROWSER) addressBarInput else ""
                    voiceEngine?.startListening("bn-IN", seedText)
                    Toast.makeText(context, "🎙️ অটো-মাইক চালু — আপনি বললেই বাংলা হয়ে বসবে, চুপ করলে স্ট্যান্ডবাই থাকবে!", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (_: Throwable) {
            isVoiceListening = false
            Toast.makeText(context, "🎙️ ভয়েস ইঞ্জিন স্ট্যান্ডবাই মোডে প্রস্তুত", Toast.LENGTH_SHORT).show()
        }
    }

    val mediaProjectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        if (result.resultCode == Activity.RESULT_OK && data != null) {
            SovereignGeminiLiveOmniEngine.attachExternalMediaProjection(
                context = context,
                resultCode = result.resultCode,
                data = data
            )
            Toast.makeText(context, "🌐 ফুল ফোন স্ক্রিন শেয়ার (অ্যাপের বাইরেও) চালু হয়েছে!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "📱 ইন-অ্যাপ লাইভ স্ক্রিন শেয়ার মোডে চলমান রয়েছে", Toast.LENGTH_SHORT).show()
        }
    }

    val requestExternalScreenShare: () -> Unit = {
        try {
            val mgr = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
            if (mgr != null) {
                mediaProjectionLauncher.launch(mgr.createScreenCaptureIntent())
            } else {
                Toast.makeText(context, "📱 ইন-অ্যাপ স্ক্রিন শেয়ার সক্রিয় রয়েছে", Toast.LENGTH_SHORT).show()
            }
        } catch (_: Throwable) {
            Toast.makeText(context, "📱 ইন-অ্যাপ স্ক্রিন শেয়ার সক্রিয় রয়েছে", Toast.LENGTH_SHORT).show()
        }
    }

    val launchGeminiLiveSession: () -> Unit = {
        SovereignGeminiLiveOmniEngine.startLiveSession(
            context = context,
            startMinimized = false,
            rootViewProvider = { currentComposeView.rootView },
            activeWebViewProvider = { if (activeTabIndex == 1 && webViewTabB != null) webViewTabB else webViewTabA },
            onEnsureVoiceMicListening = {
                if (!isVoiceListening) {
                    toggleVoiceAction()
                }
            }
        )
    }

    LaunchedEffect(activeTabIndex) {
        tabs.getOrNull(activeTabIndex)?.let { tab ->
            addressBarInput = if (tab.url == "https://www.google.com" || tab.url.startsWith("file:///")) "" else tab.url
        }
    }

    BackHandler(enabled = showNanobotStudio) {
        showNanobotStudio = false
    }

    if (showNanobotStudio) {
        WorkflowScreen(
            sourceWebView = webViewTabA,
            targetWebView = webViewTabB ?: webViewTabA,
            onRouteApprovedToTabB = { payloadText ->
                showNanobotStudio = false
                showOnScreenReplyModal = true
                statusBannerText = "✨ ন্যানোবট চেইন আউটপুট মেইন স্ক্রিনে ও Tab B-তে পাঠানো হয়েছে!"
                val targetWv = webViewTabB ?: webViewTabA
                if (targetWv != null && payloadText.isNotBlank()) {
                    scope.launch {
                        val file = FileStreamHelper.writeCodeToFile(context, payloadText, "swarm_output.txt")
                        FileStreamHelper.streamCodeToWebView(targetWv, file)
                        GoalHeuristics.executeGoalAction(targetWv, GoalType.RUN_CODE)
                    }
                }
            },
            onClose = { showNanobotStudio = false }
        )
        return
    }

    val latestRelayEvent = relayFeed.firstOrNull()
    val activeWebViewInstance = if (activeTabIndex == 1 && webViewTabB != null) webViewTabB else webViewTabA

    BackHandler(enabled = (mainScreenMode == MainScreenMode.BROWSER || mainScreenMode == MainScreenMode.PARTICLES_CHAT || (mainScreenMode == MainScreenMode.AI_CHAT && isImmersiveChatFullScreen))) {
        if (mainScreenMode == MainScreenMode.BROWSER && activeWebViewInstance?.canGoBack() == true) {
            activeWebViewInstance.goBack()
            Toast.makeText(context, "⬅️ পেছনে যাওয়া হয়েছে", Toast.LENGTH_SHORT).show()
        } else if (isImmersiveChatFullScreen) {
            isImmersiveChatFullScreen = false
            Toast.makeText(context, "🎛️ সব মেইন বাটন, চ্যাট, ট্যাব A ও ট্যাব B সামনে আনা হয়েছে", Toast.LENGTH_SHORT).show()
        } else if (mainScreenMode == MainScreenMode.PARTICLES_CHAT) {
            mainScreenMode = MainScreenMode.AI_CHAT
            Toast.makeText(context, "💬 মূল এআই চ্যাটে ফিরে আসা হয়েছে", Toast.LENGTH_SHORT).show()
        } else {
            mainScreenMode = MainScreenMode.AI_CHAT
            Toast.makeText(context, "💬 এআই চ্যাটে ফিরে আসা হয়েছে", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White
    ) { innerPadding ->
        // System Back handler for Full-Screen Chat & Live App Runner
        val activeLiveBuiltModule by AutonomousSelfEvolutionEngine.activeLiveModule.collectAsState()
        val isFullScreenRunner by AutonomousSelfEvolutionEngine.isFullScreenRunnerMode.collectAsState()
        BackHandler(enabled = (mainScreenMode == MainScreenMode.AI_CHAT && (activeLiveBuiltModule != null || isImmersiveChatFullScreen))) {
            if (isFullScreenRunner) {
                AutonomousSelfEvolutionEngine.setFullScreenRunnerMode(false)
            } else if (activeLiveBuiltModule != null) {
                AutonomousSelfEvolutionEngine.dismissLiveRunner()
                Toast.makeText(context, "⏹ লাইভ রানার বন্ধ করা হয়েছে", Toast.LENGTH_SHORT).show()
            } else if (isImmersiveChatFullScreen) {
                isImmersiveChatFullScreen = false
                Toast.makeText(context, "🎛️ সব মেইন বাটন, চ্যাট, ট্যাব A ও ট্যাব B সামনে আনা হয়েছে", Toast.LENGTH_SHORT).show()
            }
        }

        val omniNodes by UniversalOmniControlEngine.nodes.collectAsState()
        val omniVisual by UniversalOmniControlEngine.visualState.collectAsState()
        val omniAiLock by UniversalOmniControlEngine.aiLockState.collectAsState()
        val omniPendingApply by UniversalOmniControlEngine.pendingFeatureApplyEvent.collectAsState()
        val omniTouchEvent by UniversalOmniControlEngine.lastScreenTouchEvent.collectAsState()
        val omniZeroLoad by UniversalOmniControlEngine.zeroLoadState.collectAsState()

        val handleOmniNodeClick: (OmniNode) -> Unit = { node ->
            when (node.id) {
                "gemini_live_share", "chat_hdr_gemini_live" -> launchGeminiLiveSession()
                "api_key" -> showAiSettingsSheet = true
                "truth_dossier" -> showTruthDossierSheet = true
                "chat_1" -> {
                    mainScreenMode = MainScreenMode.AI_CHAT
                    isImmersiveChatFullScreen = true
                    Toast.makeText(context, "💬 শুধু এআই চ্যাট ১০০% ফুল-স্ক্রিন বড় হয়েছে — সব বাটনে ফিরতে উপরে [ ✕ ] চাপুন", Toast.LENGTH_SHORT).show()
                }
                "chat_2_particles" -> {
                    if (mainScreenMode != MainScreenMode.PARTICLES_CHAT) {
                        mainScreenMode = MainScreenMode.PARTICLES_CHAT
                        Toast.makeText(context, "✨ চ্যাট ২: পার্টিকলস ল্যাব চালু হয়েছে", Toast.LENGTH_SHORT).show()
                    } else {
                        isImmersiveChatFullScreen = !isImmersiveChatFullScreen
                    }
                }
                "tab_a" -> {
                    activeTabIndex = 0
                    splitScreenDualMode = false
                    mainScreenMode = MainScreenMode.BROWSER
                    isImmersiveChatFullScreen = true
                    val curUrl = tabs[0].url
                    addressBarInput = if (curUrl == "https://www.google.com" || curUrl == "https://www.google.com/") "" else curUrl
                    Toast.makeText(context, "🌐 শুধু ট্যাব A (অপশন A) ১০০% ফুল-স্ক্রিন বড় হয়েছে — সব বাটনে ফিরতে উপরে [ ✕ ] চাপুন", Toast.LENGTH_SHORT).show()
                }
                "tab_b" -> {
                    activeTabIndex = 1
                    splitScreenDualMode = false
                    mainScreenMode = MainScreenMode.BROWSER
                    isImmersiveChatFullScreen = true
                    val curUrl = tabs[1].url
                    addressBarInput = if (curUrl == "https://www.google.com" || curUrl == "https://www.google.com/") "" else curUrl
                    Toast.makeText(context, "🌐 শুধু ট্যাব B (অপশন B) ১০০% ফুল-স্ক্রিন বড় হয়েছে — সব বাটনে ফিরতে উপরে [ ✕ ] চাপুন", Toast.LENGTH_SHORT).show()
                }
                "dual_split" -> {
                    splitScreenDualMode = true
                    mainScreenMode = MainScreenMode.BROWSER
                    isImmersiveChatFullScreen = true
                    Toast.makeText(context, "⚔️ ডুয়াল (ট্যাব A + ট্যাব B) ফুল-স্ক্রিন চালু — সব বাটনে ফিরতে উপরে [ ✕ ] চাপুন", Toast.LENGTH_SHORT).show()
                }
                "auto_touch" -> {
                    proToolsInitialTab = 0
                    showProToolsHubSheet = true
                }
                "zero_load_turbo" -> {
                    val report = UniversalOmniControlEngine.runZeroLoadOptimizationPulse(context)
                    Toast.makeText(context, report, Toast.LENGTH_SHORT).show()
                }
                "ai_guardian_autopilot" -> {
                    if (omniZeroLoad.isAutoTouchLoopRunning) {
                        UniversalOmniControlEngine.stopRealAutoTouchLoop()
                        Toast.makeText(context, "⏹️ এআই অটো-টাচ লুপ বন্ধ করা হয়েছে", Toast.LENGTH_SHORT).show()
                    } else {
                        UniversalOmniControlEngine.startRealAutoTouchLoop(
                            context = context,
                            intervalMs = omniZeroLoad.autoTouchIntervalMs,
                            xFraction = 0.5f,
                            yFraction = 0.5f
                        )
                        Toast.makeText(context, "🎯 আসল এআই অটো-টাচ লুপ চালু (${omniZeroLoad.autoTouchIntervalMs}ms ইন্টারভ্যাল)", Toast.LENGTH_SHORT).show()
                    }
                }
                "anatomy" -> showCellularAnatomySheet = true
                "rules" -> {
                    plannerSheetInitialTab = 0
                    showGlobalRuleAndPlannerSheet = true
                }
                "planner" -> {
                    plannerSheetInitialTab = 1
                    showGlobalRuleAndPlannerSheet = true
                }
                "macro" -> {
                    if (isMacroRecording) {
                        GhostMacroAndSnifferEngine.stopAndSaveMacro(
                            context = context,
                            webView = activeWebViewInstance,
                            customTitle = "Auto_Macro_${System.currentTimeMillis() % 1000}"
                        )
                        Toast.makeText(context, "⏹️ ম্যাক্রো রেকর্ডিং সেভ হয়েছে!", Toast.LENGTH_SHORT).show()
                    } else {
                        GhostMacroAndSnifferEngine.startRecordingMacro(activeWebViewInstance)
                        Toast.makeText(context, "🔴 ঘোস্ট ম্যাক্রো রেকর্ডিং শুরু হয়েছে!", Toast.LENGTH_SHORT).show()
                    }
                }
                "sandbox" -> {
                    sandboxCustomHtml = ""
                    showLiveSandboxSheet = true
                }
                "bot_report" -> showOnScreenReplyModal = true
                "dock_nanobot" -> showNanobotStudio = true
                "dock_quad_chat" -> {
                    quadChatInitialChamber = 0
                    showQuadChatSheet = true
                }
                "dock_rule_planner" -> {
                    plannerSheetInitialTab = 0
                    showGlobalRuleAndPlannerSheet = true
                }
                "dock_pro_lab" -> {
                    proToolsInitialTab = 0
                    showProToolsHubSheet = true
                }
                "chat_hdr_api_key" -> showAiSettingsSheet = true
                "chat_hdr_new_chat" -> {
                    ChatSessionManager.startNewChat(context, "নতুন চ্যাট")
                    mainScreenMode = MainScreenMode.AI_CHAT
                    Toast.makeText(context, "➕ নতুন চ্যাট সেশন শুরু হয়েছে", Toast.LENGTH_SHORT).show()
                }
                "chat_hdr_history", "chat_hdr_review_page", "chat_hdr_apps_showroom", "chat_hdr_power_modules", "chat_hdr_code_import" -> {
                    mainScreenMode = MainScreenMode.AI_CHAT
                    Toast.makeText(context, "🎛️ চ্যাট হেডার ফিচার (${node.label}) সক্রিয় হয়েছে", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val payload = node.customActionPayload
                    when {
                        node.id == "incremental_master_project" || payload.startsWith("SLOT_CONTROL:") -> {
                            if (payload.startsWith("SLOT_CONTROL:")) {
                                val slotIdx = payload.removePrefix("SLOT_CONTROL:").substringBefore("|").toIntOrNull()
                                if (slotIdx != null) {
                                    SovereignDeepResearchAndIncrementalEngine.toggleSlot(context, slotIdx, true, activeWebViewInstance)
                                }
                            }
                            val masterMod = MasterOmniControllerEngine.savedSelfFeatures.value.find { it.id == "incremental_master_project" }
                            if (masterMod != null) {
                                AutonomousSelfEvolutionEngine.launchModuleInLiveRunner(masterMod, fullScreen = false)
                                Toast.makeText(context, "🏗️ ইনক্রিমেন্টাল মাস্টার প্রজেক্ট রানার খোলা হয়েছে (${node.label})", Toast.LENGTH_SHORT).show()
                            }
                        }
                        payload.startsWith("OPEN_WEBSITE:") -> {
                            val siteId = payload.removePrefix("OPEN_WEBSITE:").trim()
                            val site = SovereignDeepResearchAndIncrementalEngine.createdWebsites.value.find { it.siteId == siteId }
                            if (site != null) {
                                activeWebViewInstance?.loadDataWithBaseURL(
                                    "https://sovereign.local/${site.siteId}/",
                                    site.fullHtmlCode,
                                    "text/html",
                                    "UTF-8",
                                    null
                                )
                                val siteMod = MasterOmniControllerEngine.savedSelfFeatures.value.find { it.id == siteId }
                                if (siteMod != null) {
                                    AutonomousSelfEvolutionEngine.launchModuleInLiveRunner(siteMod, fullScreen = false)
                                }
                                Toast.makeText(context, "🌐 ওয়েবসাইট লোড হয়েছে: ${site.titleBangla}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        payload.startsWith("ACTIVATE_CUSTOM_AI:") -> {
                            val agentId = payload.removePrefix("ACTIVATE_CUSTOM_AI:").trim()
                            val customAi = SovereignDeepResearchAndIncrementalEngine.customAiAgents.value.find { it.agentId == agentId }
                            if (customAi != null) {
                                mainScreenMode = MainScreenMode.AI_CHAT
                                ChatSessionManager.addMessage(
                                    context = context,
                                    sender = "AI",
                                    text = "🧠 **কাস্টম এআই '${customAi.name}' সক্রিয় হয়েছে!**\n• **বিশেষত্ব:** ${customAi.specialtyBangla}\n• **অভ্যন্তরীণ ম্যাপ অ্যাক্সেস:** অ্যাপের ৬১টি ফাইল ও লাইভ যুক্ত হওয়া সকল ফিচারের ১০০% ম্যাপ এই এআই-এর মেমোরিতে যুক্ত আছে।",
                                    isCode = false
                                )
                                Toast.makeText(context, "🧠 কাস্টম এআই সক্রিয়: ${customAi.name}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        payload.startsWith("GENESIS_SYSTEM:") -> {
                            // Whole-Sky Living Subsystem: launches or synthesizes a full interactive system for this custom button!
                            val rawSpec = payload.removePrefix("GENESIS_SYSTEM:").substringAfter("|", node.label)
                            val existingMod = MasterOmniControllerEngine.savedSelfFeatures.value.find { it.id == "genesis_${node.id}" }
                            val modToRun = existingMod ?: AutonomousSelfEvolutionEngine.buildInstantProFoundationModule(rawSpec).copy(
                                id = "genesis_${node.id}",
                                title = node.label
                            ).also { created ->
                                MasterOmniControllerEngine.addOrUpdateSelfFeature(context, created)
                            }
                            mainScreenMode = MainScreenMode.AI_CHAT
                            AutonomousSelfEvolutionEngine.launchModuleInLiveRunner(modToRun, fullScreen = false)
                            Toast.makeText(context, "👑 পূর্ণাঙ্গ সিস্টেম চালু হয়েছে: ${node.label}", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            val existingMod = MasterOmniControllerEngine.savedSelfFeatures.value.find { it.id == "genesis_${node.id}" }
                            if (existingMod != null) {
                                mainScreenMode = MainScreenMode.AI_CHAT
                                AutonomousSelfEvolutionEngine.launchModuleInLiveRunner(existingMod, fullScreen = false)
                            } else {
                                Toast.makeText(context, "⚡ এআই ফিচার সক্রিয়: ${node.label}", Toast.LENGTH_SHORT).show()
                                ParticleSwarmManager.dispatchParticleCommandByAi(
                                    context = context,
                                    webView = activeWebViewInstance,
                                    commandText = payload.ifBlank { node.label }
                                ) { reply ->
                                    ChatSessionManager.addMessage(context, "AI", "⚡ [${node.label}]:\n$reply", false)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Automatically execute OR genuinely stop/close feature when AI turns it ON/OFF/APPLY via UniversalOmniControlEngine
        LaunchedEffect(omniPendingApply?.eventId) {
            val ev = omniPendingApply ?: return@LaunchedEffect
            if (ev.actionMode != "TURN_OFF") {
                val targetNode = omniNodes.find { it.id == ev.nodeId }
                if (targetNode != null) {
                    handleOmniNodeClick(targetNode)
                }
            } else {
                // REAL Feature TURN_OFF Execution: closes open sheets, stops active loops, and dismisses live runners!
                when (ev.nodeId) {
                    "sandbox" -> showLiveSandboxSheet = false
                    "truth_dossier" -> showTruthDossierSheet = false
                    "anatomy" -> showCellularAnatomySheet = false
                    "rules", "planner", "dock_rule_planner" -> showGlobalRuleAndPlannerSheet = false
                    "auto_touch", "dock_pro_lab" -> {
                        showProToolsHubSheet = false
                        UniversalOmniControlEngine.stopRealAutoTouchLoop()
                        UniversalOmniControlEngine.stopRealAutoScrollLoop()
                    }
                    "ai_guardian_autopilot" -> {
                        UniversalOmniControlEngine.stopRealAutoTouchLoop()
                        UniversalOmniControlEngine.stopRealAutoScrollLoop()
                    }
                    "dock_nanobot" -> showNanobotStudio = false
                    "dock_quad_chat" -> showQuadChatSheet = false
                    "api_key", "chat_hdr_api_key" -> showAiSettingsSheet = false
                    "bot_report" -> showOnScreenReplyModal = false
                    "macro" -> {
                        if (isMacroRecording) {
                            GhostMacroAndSnifferEngine.stopAndSaveMacro(
                                context = context,
                                webView = activeWebViewInstance,
                                customTitle = "Stopped_By_AI_${System.currentTimeMillis() % 1000}"
                            )
                        }
                    }
                    "chat_2_particles" -> {
                        ParticleSwarmManager.stopAutonomousMission(context)
                        ParticleSwarmManager.stopContinuousLoop(context)
                        ParticleSwarmManager.updateCanvasPhysicsState(context, isEnabled = false)
                        if (mainScreenMode == MainScreenMode.PARTICLES_CHAT) {
                            mainScreenMode = MainScreenMode.AI_CHAT
                        }
                    }
                    "incremental_master_project" -> {
                        AutonomousSelfEvolutionEngine.dismissLiveRunner()
                    }
                    "ALL_ACTIVE_FEATURES" -> {
                        showLiveSandboxSheet = false
                        showTruthDossierSheet = false
                        showCellularAnatomySheet = false
                        showGlobalRuleAndPlannerSheet = false
                        showProToolsHubSheet = false
                        showQuadChatSheet = false
                        showNanobotStudio = false
                        showAiSettingsSheet = false
                        showOnScreenReplyModal = false
                        UniversalOmniControlEngine.stopRealAutoTouchLoop()
                        UniversalOmniControlEngine.stopRealAutoScrollLoop()
                        ParticleSwarmManager.stopAutonomousMission(context)
                        ParticleSwarmManager.stopContinuousLoop(context)
                        AutonomousSelfEvolutionEngine.dismissLiveRunner()
                    }
                    else -> {
                        val targetNode = omniNodes.find { it.id == ev.nodeId }
                        val payload = targetNode?.customActionPayload.orEmpty()
                        if (payload.startsWith("SLOT_CONTROL:")) {
                            val slotIdx = payload.removePrefix("SLOT_CONTROL:").substringBefore("|").toIntOrNull()
                            if (slotIdx != null) {
                                SovereignDeepResearchAndIncrementalEngine.toggleSlot(context, slotIdx, false, activeWebViewInstance)
                            }
                        }
                        if (activeLiveBuiltModule?.id == "genesis_${ev.nodeId}" || activeLiveBuiltModule?.id == ev.nodeId) {
                            AutonomousSelfEvolutionEngine.dismissLiveRunner()
                        }
                    }
                }
                Toast.makeText(context, "⏹️ এআই ফিচার (${ev.nodeId}) এবং এর চলমান প্রসেস অফ করেছে", Toast.LENGTH_SHORT).show()
            }
        }

        // Automatically dispatch physical touch/scroll to active WebView when AI triggers Screen Touch
        LaunchedEffect(omniTouchEvent?.id) {
            val ev = omniTouchEvent ?: return@LaunchedEffect
            UniversalOmniControlEngine.performPhysicalTouchOnWebView(activeWebViewInstance, ev)
        }

        UniversalOmniControlEngine.OmniDynamicBackgroundBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val isPureChromeFullScreen = mainScreenMode == MainScreenMode.BROWSER && isImmersiveChatFullScreen

            // =========================================================================
            // MODE A: 100% AUTHENTIC FULL-SCREEN GOOGLE CHROME BAR (যখন ট্যাব A, ট্যাব B বা ডুয়াল ফুল-স্ক্রিনে বড় হয়)
            //         উপরে ডানদিকে লাল [ ✕ কেটে সব বাটন আনুন ] বাটন — টিপলেই আবার সব বাটন ও ৩টি স্ক্রিন সামনে চলে আসবে!
            // =========================================================================
            if (!isFullScreenRunner && isPureChromeFullScreen) {
                val currentTabWv = if (activeTabIndex == 1) webViewTabB else webViewTabA
                Surface(
                    color = Color(0xFFF8FAFC), // Crisp White/Light Chrome Toolbar Color
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.8.dp, Color(0xFFCBD5E1), RectangleShape)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Chrome Back Button
                        IconButton(
                            onClick = {
                                if (currentTabWv?.canGoBack() == true) {
                                    currentTabWv.goBack()
                                }
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(0.8.dp, Color(0xFFCBD5E1), CircleShape)
                                .testTag("browser_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Chrome Reload Button
                        IconButton(
                            onClick = { currentTabWv?.reload() },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(0.8.dp, Color(0xFFCBD5E1), CircleShape)
                                .testTag("browser_reload_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reload",
                                tint = CyberEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Chrome Home Button (Google.com)
                        IconButton(
                            onClick = {
                                focusManager.clearFocus()
                                addressBarInput = ""
                                val homeUrl = "https://www.google.com"
                                if (activeTabIndex == 1) {
                                    tabs[1] = tabs[1].copy(url = homeUrl)
                                    tabBNavTriggerId += 1
                                    webViewTabB?.loadUrl(homeUrl)
                                } else {
                                    tabs[0] = tabs[0].copy(url = homeUrl)
                                    tabANavTriggerId += 1
                                    webViewTabA?.loadUrl(homeUrl)
                                }
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(0.8.dp, Color(0xFFCBD5E1), CircleShape)
                        ) {
                            Text("🏠", fontSize = 13.sp)
                        }

                        // Authentic Chrome Pill Omnibox (White Daylight)
                        OutlinedTextField(
                            value = addressBarInput,
                            onValueChange = { addressBarInput = it },
                            placeholder = {
                                Text(
                                    text = if (activeTabIndex == 0) "🔒 ট্যাব A: Google এ সার্চ বা URL লিখুন" else "🔒 ট্যাব B: Google এ সার্চ বা URL লিখুন",
                                    color = Color(0xFF475569),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            trailingIcon = {
                                if (addressBarInput.isNotEmpty()) {
                                    IconButton(
                                        onClick = { addressBarInput = "" },
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear",
                                            tint = Color(0xFF475569),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedTextColor = Color(0xFF0F172A),
                                unfocusedTextColor = Color(0xFF0F172A),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    focusManager.clearFocus()
                                    val formatted = formatOmniboxUrl(addressBarInput, "all")
                                    if (activeTabIndex == 1) {
                                        tabs[1] = tabs[1].copy(url = formatted)
                                        tabBNavTriggerId += 1
                                        webViewTabB?.loadUrl(formatted)
                                    } else {
                                        tabs[0] = tabs[0].copy(url = formatted)
                                        tabANavTriggerId += 1
                                        webViewTabA?.loadUrl(formatted)
                                    }
                                },
                                onGo = {
                                    focusManager.clearFocus()
                                    val formatted = formatOmniboxUrl(addressBarInput, "all")
                                    if (activeTabIndex == 1) {
                                        tabs[1] = tabs[1].copy(url = formatted)
                                        tabBNavTriggerId += 1
                                        webViewTabB?.loadUrl(formatted)
                                    } else {
                                        tabs[0] = tabs[0].copy(url = formatted)
                                        tabANavTriggerId += 1
                                        webViewTabA?.loadUrl(formatted)
                                    }
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("omnibox_url_input")
                        )

                        // Chrome Search Go Button
                        Surface(
                            color = CyberCyan,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(34.dp)
                                .clickable {
                                    focusManager.clearFocus()
                                    val formatted = formatOmniboxUrl(addressBarInput, "all")
                                    if (activeTabIndex == 1) {
                                        tabs[1] = tabs[1].copy(url = formatted)
                                        tabBNavTriggerId += 1
                                        webViewTabB?.loadUrl(formatted)
                                    } else {
                                        tabs[0] = tabs[0].copy(url = formatted)
                                        tabANavTriggerId += 1
                                        webViewTabA?.loadUrl(formatted)
                                    }
                                }
                                .testTag("omnibox_go_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🔍", fontSize = 13.sp)
                            }
                        }

                        // Chrome Voice Search Mic
                        IconButton(
                            onClick = toggleVoiceAction,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isVoiceListening) CyberRose else Color(0xFFE0F2FE))
                                .border(0.8.dp, if (isVoiceListening) CyberRose else CyberCyan, CircleShape)
                                .testTag("voice_command_button")
                        ) {
                            Icon(
                                imageVector = if (isVoiceListening) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = "Voice Search",
                                tint = if (isVoiceListening) Color.White else CyberCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Authentic Chrome Square Tab Counter Badge ([A] / [B]) — 1-Tap switches between Option A & Option B
                        Surface(
                            color = if (activeTabIndex == 0) Color(0xFFECFDF5) else Color(0xFFEDE9FE),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (activeTabIndex == 0) CyberEmerald else CyberPurple
                            ),
                            modifier = Modifier
                                .size(32.dp)
                                .clickable {
                                    splitScreenDualMode = false
                                    activeTabIndex = if (activeTabIndex == 0) 1 else 0
                                    val curUrl = tabs[activeTabIndex].url
                                    addressBarInput = if (curUrl == "https://www.google.com" || curUrl == "https://www.google.com/") "" else curUrl
                                    Toast.makeText(
                                        context,
                                        if (activeTabIndex == 0) "🌐 Chrome ট্যাব A ফুল-স্ক্রিন" else "🌐 Chrome ট্যাব B ফুল-স্ক্রিন",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (activeTabIndex == 0) "A" else "B",
                                    color = if (activeTabIndex == 0) CyberEmerald else CyberPurple,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        // TOP-RIGHT [ ✕ কেটে সব বাটন আনুন ] BUTTON — Tapping this closes Full-Screen and brings back ALL buttons + Chat + Tab A + Tab B!
                        Surface(
                            color = Color(0xFFFFE4E6),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.4.dp, CyberRose),
                            modifier = Modifier
                                .height(34.dp)
                                .clickable {
                                    isImmersiveChatFullScreen = false
                                    Toast.makeText(context, "🎛️ ফুল-স্ক্রিন কেটে সব বাটন, চ্যাট, ট্যাব A ও ট্যাব B সামনে আনা হয়েছে!", Toast.LENGTH_SHORT).show()
                                }
                                .testTag("exit_chrome_fullscreen_btn")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            ) {
                                Text(
                                    text = "✕ কেটে সব বাটন আনুন",
                                    color = CyberRose,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // MODE B: ALL-BUTTONS MASTER COMMAND HEADER (যখন [ ✕ ] কাটার চিহ্ন টিপে সব বাটন ও ৩টি স্ক্রিন সামনে থাকে)
            // =========================================================================
            if (!isFullScreenRunner && !isImmersiveChatFullScreen) {
                val topRow1Nodes = remember(omniNodes) { omniNodes.filter { it.zone == "TOP_ROW_1" }.sortedBy { it.orderIndex } }
                val topRow2Nodes = remember(omniNodes) { omniNodes.filter { it.zone == "TOP_ROW_2" }.sortedBy { it.orderIndex } }
                val removedCount = remember(omniNodes) { omniNodes.count { it.zone == "REMOVED_TRASH" } }

                Surface(
                    color = UniversalOmniControlEngine.parseColorSafe(omniVisual.headerBgHex, Color(0xFFF8FAFC)).copy(alpha = 0.94f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.9.dp, Color(0xFFCBD5E1), RectangleShape)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
                        // ROW 1: Dynamic Omni-Controlled Mode & Feature Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                topRow1Nodes.forEach { node ->
                                    val isNodeActive = when (node.id) {
                                        "chat_1" -> mainScreenMode == MainScreenMode.AI_CHAT
                                        "chat_2_particles" -> mainScreenMode == MainScreenMode.PARTICLES_CHAT
                                        "tab_a" -> mainScreenMode == MainScreenMode.BROWSER && activeTabIndex == 0 && !splitScreenDualMode
                                        "tab_b" -> mainScreenMode == MainScreenMode.BROWSER && activeTabIndex == 1 && !splitScreenDualMode
                                        "dual_split" -> splitScreenDualMode && mainScreenMode == MainScreenMode.BROWSER
                                        else -> false
                                    }
                                    val tagName = when (node.id) {
                                        "api_key" -> "master_row_first_api_key_btn"
                                        "truth_dossier" -> "master_row_truth_dossier_btn"
                                        "chat_1" -> "top_toggle_ai_chat"
                                        "chat_2_particles" -> "top_toggle_particles_chat"
                                        "tab_a" -> "top_toggle_browser"
                                        "tab_b" -> "browser_tab_1"
                                        "dual_split" -> "toggle_dual_split_screen_btn"
                                        else -> "omni_node_${node.id}"
                                    }
                                    val displayLabel = if (node.id == "api_key" && omniAiLock.isPermanentlyLocked) {
                                        "🔒 ${node.label}"
                                    } else {
                                        node.label
                                    }

                                    UniversalOmniControlEngine.OmniInteractiveButton(
                                        node = node,
                                        displayLabel = displayLabel,
                                        isSelectedOrActive = isNodeActive,
                                        baseHeightDp = 33f,
                                        baseFontSp = 11f,
                                        baseHorizPadDp = 10f,
                                        testTagName = tagName,
                                        onClick = { handleOmniNodeClick(node) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(5.dp))

                            // Right: Emergency Restore/Reset + Settings + Full-Screen Focus Button
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (omniVisual.mode != "SOLID" || omniVisual.primaryHex != "#FFFFFF" || removedCount > 0) {
                                    Surface(
                                        color = Color(0xFFFEF3C7),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberAmber),
                                        modifier = Modifier
                                            .height(33.dp)
                                            .clickable {
                                                UniversalOmniControlEngine.resetAllToDefault(context)
                                                Toast.makeText(context, "🔄 সব বাটন ও ডিফল্ট সাদা থিম রিস্টোর করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                            }
                                            .testTag("omni_master_reset_btn")
                                    ) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 7.dp)) {
                                            Text("🔄 রিসেট", color = Color(0xFF92400E), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                    }
                                }

                                Surface(
                                    color = Color(0xFFECFDF5),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberEmerald),
                                    modifier = Modifier
                                        .height(33.dp)
                                        .clickable {
                                            proToolsInitialTab = 1
                                            showProToolsHubSheet = true
                                        }
                                        .testTag("open_gmail_zip_apk_hub_btn")
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(horizontal = 7.dp)
                                    ) {
                                        Text(
                                            text = "📦 .ZIP/APK",
                                            color = CyberEmerald,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { showOmniLayoutVisualEditor = true },
                                    modifier = Modifier
                                        .size(33.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(Color(0xFFEDE9FE))
                                        .border(0.9.dp, CyberPurple, RoundedCornerShape(9.dp))
                                        .testTag("open_omni_layout_editor_btn")
                                ) {
                                    Text("🎨", fontSize = 14.sp)
                                }

                                IconButton(
                                    onClick = { showAiSettingsSheet = true },
                                    modifier = Modifier
                                        .size(33.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(Color.White)
                                        .border(0.9.dp, CyberCyan, RoundedCornerShape(9.dp))
                                        .testTag("open_ai_settings_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "AI Settings",
                                        tint = CyberCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Surface(
                                    color = Color(0xFFE0F2FE),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan),
                                    modifier = Modifier
                                        .height(33.dp)
                                        .clickable {
                                            isImmersiveChatFullScreen = true
                                        }
                                        .testTag("enter_pure_fullscreen_btn")
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    ) {
                                        Text(
                                            text = "⛶ ফুল",
                                            color = CyberCyan,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }

                        if (topRow2Nodes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(5.dp))

                            // ROW 2: Dynamic Power & Custom AI Feature Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                topRow2Nodes.forEach { node ->
                                    val tagName = when (node.id) {
                                        "auto_touch" -> "power_row_auto_touch_btn"
                                        "anatomy" -> "power_row_anatomy_btn"
                                        "rules" -> "power_row_rules_btn"
                                        "planner" -> "power_row_planner_btn"
                                        "macro" -> "power_row_macro_btn"
                                        "sandbox" -> "power_row_sandbox_btn"
                                        else -> "omni_row2_${node.id}"
                                    }
                                    val baseLabel = if (node.id == "macro" && isMacroRecording) "⏹️ ম্যাক্রো থামান" else node.label
                                    val displayTxt = when {
                                        !node.isEnabled -> "$baseLabel [OFF]"
                                        node.id == "ai_guardian_autopilot" && omniZeroLoad.isAutoTouchLoopRunning -> "$baseLabel [অটো-টাচ চালু]"
                                        else -> baseLabel
                                    }

                                    UniversalOmniControlEngine.OmniInteractiveButton(
                                        node = node,
                                        displayLabel = displayTxt,
                                        isSelectedOrActive = false,
                                        baseHeightDp = 28f,
                                        baseFontSp = 10f,
                                        baseHorizPadDp = 8f,
                                        testTagName = tagName,
                                        onClick = { handleOmniNodeClick(node) }
                                    )
                                }
                            }
                        }

                        // Compact Chrome Omnibox Row (Always accessible in All-Buttons Hub so user can search/load URLs in Tab A or Tab B directly!)
                        Spacer(modifier = Modifier.height(5.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val currentTabWv = if (activeTabIndex == 1) webViewTabB else webViewTabA

                            // Active Target Tab Switcher Badge (ট্যাব A / ট্যাব B)
                            Surface(
                                color = if (activeTabIndex == 0) Color(0xFFECFDF5) else Color(0xFFEDE9FE),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (activeTabIndex == 0) CyberEmerald else CyberPurple
                                ),
                                modifier = Modifier
                                    .height(34.dp)
                                    .clickable {
                                        activeTabIndex = if (activeTabIndex == 0) 1 else 0
                                        val curUrl = tabs[activeTabIndex].url
                                        addressBarInput = if (curUrl == "https://www.google.com" || curUrl == "https://www.google.com/") "" else curUrl
                                    }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 7.dp)
                                ) {
                                    Text(
                                        text = if (activeTabIndex == 0) "🌐A" else "🌐B",
                                        color = if (activeTabIndex == 0) CyberEmerald else CyberPurple,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Back
                            IconButton(
                                onClick = {
                                    if (currentTabWv?.canGoBack() == true) {
                                        currentTabWv.goBack()
                                    }
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(0.8.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                    .testTag("browser_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            // Reload
                            IconButton(
                                onClick = { currentTabWv?.reload() },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(0.8.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                    .testTag("browser_reload_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reload",
                                    tint = CyberEmerald,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            // Chrome Address & Search Input Box
                            OutlinedTextField(
                                value = addressBarInput,
                                onValueChange = { addressBarInput = it },
                                placeholder = {
                                    Text(
                                        text = if (activeTabIndex == 0) "🔍 ট্যাব A-তে Google সার্চ বা URL..." else "🔍 ট্যাব B-তে Google সার্চ বা URL...",
                                        color = CyberSlate,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                trailingIcon = {
                                    if (addressBarInput.isNotEmpty()) {
                                        IconButton(
                                            onClick = { addressBarInput = "" },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear",
                                                tint = CyberSlate,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberCyan,
                                    unfocusedBorderColor = Color(0xFFCBD5E1),
                                    focusedTextColor = Color(0xFF0F172A),
                                    unfocusedTextColor = Color(0xFF0F172A),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        focusManager.clearFocus()
                                        val formatted = formatOmniboxUrl(addressBarInput, "all")
                                        if (activeTabIndex == 1) {
                                            tabs[1] = tabs[1].copy(url = formatted)
                                            tabBNavTriggerId += 1
                                            webViewTabB?.loadUrl(formatted)
                                        } else {
                                            tabs[0] = tabs[0].copy(url = formatted)
                                            tabANavTriggerId += 1
                                            webViewTabA?.loadUrl(formatted)
                                        }
                                    },
                                    onGo = {
                                        focusManager.clearFocus()
                                        val formatted = formatOmniboxUrl(addressBarInput, "all")
                                        if (activeTabIndex == 1) {
                                            tabs[1] = tabs[1].copy(url = formatted)
                                            tabBNavTriggerId += 1
                                            webViewTabB?.loadUrl(formatted)
                                        } else {
                                            tabs[0] = tabs[0].copy(url = formatted)
                                            tabANavTriggerId += 1
                                            webViewTabA?.loadUrl(formatted)
                                        }
                                    }
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("omnibox_url_input")
                            )

                            // Search (Google Results List) Button
                            Surface(
                                color = CyberCyan,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .height(36.dp)
                                    .clickable {
                                        focusManager.clearFocus()
                                        val formatted = formatOmniboxUrl(addressBarInput, "all")
                                        if (activeTabIndex == 1) {
                                            tabs[1] = tabs[1].copy(url = formatted)
                                            tabBNavTriggerId += 1
                                            webViewTabB?.loadUrl(formatted)
                                        } else {
                                            tabs[0] = tabs[0].copy(url = formatted)
                                            tabANavTriggerId += 1
                                            webViewTabA?.loadUrl(formatted)
                                        }
                                    }
                                    .testTag("omnibox_go_button")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 9.dp)
                                ) {
                                    Text(
                                        text = "🔍",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            // In-App Voice Search Button
                            IconButton(
                                onClick = toggleVoiceAction,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isVoiceListening) CyberRose else Color(0xFFE0F2FE))
                                    .border(0.8.dp, if (isVoiceListening) CyberRose else CyberCyan, RoundedCornerShape(8.dp))
                                    .testTag("voice_command_button")
                            ) {
                                Icon(
                                    imageVector = if (isVoiceListening) Icons.Default.Stop else Icons.Default.Mic,
                                    contentDescription = "Voice Search",
                                    tint = if (isVoiceListening) Color.White else CyberCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // 2. LIVE VOICE WAVEFORM BAR (WHEN VOICE MIC IS ACTIVE IN BROWSER MODE)
            // =========================================================================
            AnimatedVisibility(visible = (mainScreenMode == MainScreenMode.BROWSER && isVoiceListening)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFE4E6))
                        .border(0.8.dp, CyberRose.copy(alpha = 0.6f))
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LiveVoiceWaveform(
                        rmsLevel = voiceRms,
                        isActive = isVoiceListening,
                        modifier = Modifier
                            .width(70.dp)
                            .height(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = liveVoiceTranscript.ifBlank { "🔴 শুনছি (বাংলা bn-IN / English)... যা সার্চ করতে চান বলুন" },
                        color = Color(0xFF0F172A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        color = CyberRose,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable { toggleVoiceAction() }
                    ) {
                        Text(
                            text = "⏹️ সমাপ্ত",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // =========================================================================
            // 3. MAIN WORKSPACE AREA
            //    - যখন [ ✕ ] টিপে সব বাটন খোলা থাকে (!isImmersiveChatFullScreen):
            //      একই সাদা স্ক্রিনে উপরে "💬 এআই চ্যাট" এবং নিচে পাশাপাশি "🌐 ট্যাব A" ও "🌐 ট্যাব B" দেখা যাবে!
            //      যেকোনোটিতে ক্লিক করলেই শুধু সেটি ১০০% ফুল-স্ক্রিন বড় হয়ে যাবে এবং উপরে [ ✕ কেটে সব বাটন আনুন ] থাকবে!
            // =========================================================================
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val showChatInHub = !isImmersiveChatFullScreen
                val showChatFullScreenOnly = isImmersiveChatFullScreen && (mainScreenMode == MainScreenMode.AI_CHAT || mainScreenMode == MainScreenMode.PARTICLES_CHAT)
                val showBrowsersInHub = !isImmersiveChatFullScreen
                val showBrowserFullScreen = isImmersiveChatFullScreen && mainScreenMode == MainScreenMode.BROWSER

                // SECTION A: PERSISTENT AI CHAT & PARTICLES CHAT WORKSPACE
                Box(
                    modifier = when {
                        showChatFullScreenOnly -> Modifier.fillMaxSize()
                        showChatInHub -> Modifier
                            .weight(0.54f)
                            .fillMaxWidth()
                            .border(1.dp, if (mainScreenMode == MainScreenMode.PARTICLES_CHAT) CyberPurple.copy(alpha = 0.55f) else CyberCyan.copy(alpha = 0.55f))
                        else -> Modifier.size(0.dp)
                    }
                ) {
                    if (mainScreenMode == MainScreenMode.PARTICLES_CHAT) {
                        ParticleChatWorkspace(
                            activeWebView = activeWebViewInstance,
                            onSwitchToMainChat = {
                                mainScreenMode = MainScreenMode.AI_CHAT
                            },
                            onSwitchToBrowser = {
                                mainScreenMode = MainScreenMode.BROWSER
                                isImmersiveChatFullScreen = true
                            },
                            onToggleVoiceMic = toggleVoiceAction,
                            isVoiceListening = isVoiceListening,
                            liveVoiceTranscript = liveVoiceTranscript
                        )
                    } else {
                        PureGeminiChatWorkspace(
                            activeWebView = activeWebViewInstance,
                            isVoiceListening = isVoiceListening,
                            liveVoiceTranscript = liveVoiceTranscript,
                            pendingVoiceCommand = pendingVoiceChatCommand,
                            pendingVoiceTriggerId = pendingVoiceChatTriggerId,
                            onToggleVoiceMic = toggleVoiceAction,
                            onOpenAiKeySettings = {
                                showAiSettingsSheet = true
                            },
                            omniControlBridge = omniControlBridge,
                            onSwitchToBrowserMode = {
                                mainScreenMode = MainScreenMode.BROWSER
                                isImmersiveChatFullScreen = true
                            },
                            isImmersiveFullScreen = isImmersiveChatFullScreen,
                            onToggleImmersiveFullScreen = {
                                mainScreenMode = MainScreenMode.AI_CHAT
                                isImmersiveChatFullScreen = !isImmersiveChatFullScreen
                            },
                            onExitFullScreenChat = {
                                isImmersiveChatFullScreen = false
                            },
                            onStartGeminiLiveShare = launchGeminiLiveSession,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // SECTION B: PERSISTENT TAB A & TAB B CHROMIUM WORKSPACES (SIDE-BY-SIDE IN HUB OR FULL-SCREEN WHEN CLICKED)
                Row(
                    modifier = when {
                        showBrowserFullScreen -> Modifier.fillMaxSize()
                        showBrowsersInHub -> Modifier
                            .weight(0.46f)
                            .fillMaxWidth()
                        else -> Modifier.size(0.dp)
                    }
                ) {
                    val showTabA = showBrowsersInHub || (showBrowserFullScreen && (splitScreenDualMode || activeTabIndex == 0))
                    val showTabB = showBrowsersInHub || (showBrowserFullScreen && (splitScreenDualMode || activeTabIndex == 1))

                    // TAB A COLUMN (HEADER + LIVE CHROMIUM WEBVIEW + FLOATING EXPAND BUTTON)
                    Column(
                        modifier = if (showTabA) {
                            Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .border(
                                    1.2.dp,
                                    if (activeTabIndex == 0) CyberEmerald else Color(0xFFCBD5E1)
                                )
                        } else {
                            Modifier.size(0.dp)
                        }
                    ) {
                        // Clickable Header on Tab A when in All-Buttons Hub — Tapping expands ONLY Tab A to 100% Full-Screen!
                        if (!isImmersiveChatFullScreen) {
                            Surface(
                                color = Color(0xFFECFDF5),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        activeTabIndex = 0
                                        splitScreenDualMode = false
                                        mainScreenMode = MainScreenMode.BROWSER
                                        isImmersiveChatFullScreen = true
                                        val curUrl = tabs[0].url
                                        addressBarInput = if (curUrl == "https://www.google.com" || curUrl == "https://www.google.com/") "" else curUrl
                                        Toast.makeText(context, "🌐 ট্যাব A ১০০% ফুল-স্ক্রিন বড় হয়েছে — কাটার জন্য উপরে [ ✕ ] চাপুন", Toast.LENGTH_SHORT).show()
                                    }
                                    .testTag("hub_expand_tab_a_header")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🌐 ট্যাব A (Chrome)",
                                        color = CyberEmerald,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Surface(
                                        color = CyberEmerald,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "⛶ ট্যাব A বড় করুন",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            ChromiumWebViewContainer(
                                initialUrl = tabs[0].url,
                                navTriggerId = tabANavTriggerId,
                                isTabB = false,
                                isDesktopMode = isDesktopMode,
                                onWebViewCreated = { wv -> webViewTabA = wv },
                                onUrlChanged = { newUrl ->
                                    tabs[0] = tabs[0].copy(url = newUrl)
                                    if (activeTabIndex == 0) {
                                        addressBarInput = if (newUrl == "https://www.google.com/" || newUrl == "https://www.google.com") "" else newUrl
                                    }
                                },
                                onConsoleMessage = {},
                                onOpenNanobotStudio = { showNanobotStudio = true },
                                onOpenQuadChat = { ch ->
                                    quadChatInitialChamber = ch
                                    showQuadChatSheet = true
                                },
                                onOpenDevConsole = { showDevConsoleSheet = true },
                                onOpenProLab = { tb ->
                                    proToolsInitialTab = tb
                                    showProToolsHubSheet = true
                                },
                                onExecuteOmniCommand = { cmd ->
                                    MasterOmniControllerEngine.executeOmniCommand(context, cmd, omniControlBridge)
                                },
                                onStartVoice = toggleVoiceAction,
                                onStopVoice = toggleVoiceAction
                            )

                            // Floating 1-Tap Expand Overlay Badge on Tab A screen when in Hub mode
                            if (!isImmersiveChatFullScreen) {
                                Surface(
                                    color = CyberEmerald.copy(alpha = 0.92f),
                                    shape = RoundedCornerShape(8.dp),
                                    shadowElevation = 4.dp,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(6.dp)
                                        .clickable {
                                            activeTabIndex = 0
                                            splitScreenDualMode = false
                                            mainScreenMode = MainScreenMode.BROWSER
                                            isImmersiveChatFullScreen = true
                                        }
                                        .testTag("floating_expand_tab_a_btn")
                                ) {
                                    Text(
                                        text = "⛶ ট্যাব A ফুল-স্ক্রিন",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // TAB B COLUMN (HEADER + LIVE CHROMIUM WEBVIEW + FLOATING EXPAND BUTTON)
                    Column(
                        modifier = if (showTabB) {
                            Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .border(
                                    1.2.dp,
                                    if (activeTabIndex == 1) CyberPurple else Color(0xFFCBD5E1)
                                )
                        } else {
                            Modifier.size(0.dp)
                        }
                    ) {
                        // Clickable Header on Tab B when in All-Buttons Hub — Tapping expands ONLY Tab B to 100% Full-Screen!
                        if (!isImmersiveChatFullScreen) {
                            Surface(
                                color = Color(0xFFEDE9FE),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        activeTabIndex = 1
                                        splitScreenDualMode = false
                                        mainScreenMode = MainScreenMode.BROWSER
                                        isImmersiveChatFullScreen = true
                                        val curUrl = tabs[1].url
                                        addressBarInput = if (curUrl == "https://www.google.com" || curUrl == "https://www.google.com/") "" else curUrl
                                        Toast.makeText(context, "🌐 ট্যাব B ১০০% ফুল-স্ক্রিন বড় হয়েছে — কাটার জন্য উপরে [ ✕ ] চাপুন", Toast.LENGTH_SHORT).show()
                                    }
                                    .testTag("hub_expand_tab_b_header")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🌐 ট্যাব B (Chrome)",
                                        color = CyberPurple,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Surface(
                                        color = CyberPurple,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "⛶ ট্যাব B বড় করুন",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            ChromiumWebViewContainer(
                                initialUrl = tabs[1].url,
                                navTriggerId = tabBNavTriggerId,
                                isTabB = true,
                                isDesktopMode = isDesktopMode,
                                onWebViewCreated = { wv -> webViewTabB = wv },
                                onUrlChanged = { newUrl ->
                                    tabs[1] = tabs[1].copy(url = newUrl)
                                    if (activeTabIndex == 1) {
                                        addressBarInput = if (newUrl == "https://www.google.com/" || newUrl == "https://www.google.com") "" else newUrl
                                    }
                                },
                                onConsoleMessage = {},
                                onOpenNanobotStudio = { showNanobotStudio = true },
                                onOpenQuadChat = { ch ->
                                    quadChatInitialChamber = ch
                                    showQuadChatSheet = true
                                },
                                onOpenDevConsole = { showDevConsoleSheet = true },
                                onOpenProLab = { tb ->
                                    proToolsInitialTab = tb
                                    showProToolsHubSheet = true
                                },
                                onExecuteOmniCommand = { cmd ->
                                    MasterOmniControllerEngine.executeOmniCommand(context, cmd, omniControlBridge)
                                },
                                onStartVoice = toggleVoiceAction,
                                onStopVoice = toggleVoiceAction
                            )

                            // Floating 1-Tap Expand Overlay Badge on Tab B screen when in Hub mode
                            if (!isImmersiveChatFullScreen) {
                                Surface(
                                    color = CyberPurple.copy(alpha = 0.92f),
                                    shape = RoundedCornerShape(8.dp),
                                    shadowElevation = 4.dp,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(6.dp)
                                        .clickable {
                                            activeTabIndex = 1
                                            splitScreenDualMode = false
                                            mainScreenMode = MainScreenMode.BROWSER
                                            isImmersiveChatFullScreen = true
                                        }
                                        .testTag("floating_expand_tab_b_btn")
                                ) {
                                    Text(
                                        text = "⛶ ট্যাব B ফুল-স্ক্রিন",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // SECTION C: DYNAMIC FLOATING & BOTTOM DOCK (যখন মেইন এআই কোনো বাটনকে ভাসমান বা নিচে নিয়ে আসে বা নতুন থ্রিডি বাটন বসায়)
                val floatingNodes = remember(omniNodes) { omniNodes.filter { it.zone == "FLOATING" }.sortedBy { it.orderIndex } }
                if (floatingNodes.isNotEmpty() && !isImmersiveChatFullScreen) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🛸 ফ্লোটিং বাটন:",
                            color = CyberPurple,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        floatingNodes.forEach { node ->
                            UniversalOmniControlEngine.OmniInteractiveButton(
                                node = node,
                                displayLabel = node.label,
                                isSelectedOrActive = false,
                                baseHeightDp = 31f,
                                baseFontSp = 10.5f,
                                baseHorizPadDp = 10f,
                                testTagName = "omni_floating_${node.id}",
                                onClick = { handleOmniNodeClick(node) }
                            )
                        }
                    }
                }

                val bottomDockNodes = remember(omniNodes) { omniNodes.filter { it.zone == "BOTTOM_DOCK" }.sortedBy { it.orderIndex } }
                if (bottomDockNodes.isNotEmpty() && !isFullScreenRunner && !isImmersiveChatFullScreen) {
                    Surface(
                        color = Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(0.9.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 6.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            bottomDockNodes.forEach { node ->
                                val tag = when (node.id) {
                                    "dock_nanobot" -> "dock_nanobot_studio_button"
                                    "dock_quad_chat" -> "dock_quad_chat_button"
                                    "dock_rule_planner" -> "dock_rule_planner_button"
                                    "dock_pro_lab" -> "dock_pro_lab_button"
                                    else -> "omni_dock_${node.id}"
                                }
                                UniversalOmniControlEngine.OmniInteractiveButton(
                                    node = node,
                                    displayLabel = node.label,
                                    isSelectedOrActive = false,
                                    baseHeightDp = 34f,
                                    baseFontSp = 10f,
                                    baseHorizPadDp = 11f,
                                    testTagName = tag,
                                    onClick = { handleOmniNodeClick(node) }
                                )
                            }
                        }
                    }
                }
            }

            // GLOBAL GEMINI LIVE OMNI-SHARE STUDIO + FLOATING CAPSULE + NEON TARGET PULSE OVERLAY
            SovereignGeminiLiveGlobalOverlay(
                onRequestExternalScreenSharePermission = requestExternalScreenShare,
                onSendLiveVoiceOrTextCommand = { cmdText ->
                    SovereignGeminiLiveOmniEngine.processLiveUserVoiceCommand(
                        context = context,
                        spokenText = cmdText,
                        rootView = currentComposeView.rootView,
                        activeWebView = activeWebViewInstance
                    )
                },
                onToggleVoiceMic = toggleVoiceAction,
                isVoiceMicListening = isVoiceListening
            )
        }
        }
    }

    // MODAL 1: Live On-Screen Bot Reply & Relay Inspector
    if (showOnScreenReplyModal) {
        ModalBottomSheet(
            onDismissRequest = { showOnScreenReplyModal = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = CyberCardBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📺 স্ক্রিনে ন্যানোবটের সংগৃহীত উত্তর ও ডিপ-থিংক রিপোর্ট",
                        color = CyberEmerald,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                    IconButton(onClick = { showOnScreenReplyModal = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberDarkBg)
                        .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            val displayContent = latestRelayEvent?.let { ev ->
                                "🤖 বট: ${ev.botTitle} (${ev.roleLabel})\n" +
                                    "🌐 ওয়েবসাইট/টার্গেট: ${ev.targetWebsiteOrAi.ifBlank { "Internal Deep-Think" }}\n\n" +
                                    "📤 পাঠানো মেসেজ:\n${ev.messageSent}\n\n" +
                                    "📥 প্রাপ্ত উত্তর / ডিপ-থিংক ফলাফল:\n${ev.replyOrThoughtOutput}"
                            } ?: "এখনো কোনো বট উত্তর আনেনি। ন্যানোবট স্টুডিও থেকে ১ ধাপ বা অটো চেইন রান করুন।"

                            Text(
                                text = displayContent,
                                color = Color.White,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            showOnScreenReplyModal = false
                            showNanobotStudio = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("পরবর্তী বটকে দিন / এডিট করুন", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val reply = latestRelayEvent?.replyOrThoughtOutput.orEmpty()
                            val targetWv = webViewTabB ?: webViewTabA
                            if (reply.isNotBlank() && targetWv != null) {
                                scope.launch {
                                    val file = FileStreamHelper.writeCodeToFile(context, reply, "re_inject_reply.txt")
                                    FileStreamHelper.streamCodeToWebView(targetWv, file)
                                    GoalHeuristics.executeGoalAction(targetWv, GoalType.CUSTOM)
                                    statusBannerText = "🚀 উত্তরটি আবার ওয়েবসাইটে পাঠানো হয়েছে!"
                                }
                            }
                            showOnScreenReplyModal = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("ওয়েবসাইটে আবার পাঠান", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // MODAL 2: Autonomous Task Queue & God-Mode Auto Sync Sheet
    if (showTaskQueueSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTaskQueueSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = CyberCardBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "🤖 অটোনোমাস ৩-স্ট্রাইক টাস্ক কিউ ও Tab A ➔ Tab B অটো-সিঙ্ক",
                    color = CyberCyan,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = engineLogText,
                    color = CyberEmerald,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(checklistTasks) { idx, task ->
                        val isCurrent = idx == currentTaskIdx
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberDarkBg)
                                .border(
                                    1.dp,
                                    if (isCurrent) CyberEmerald else Color.DarkGray,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = task.description,
                                    color = CyberSlate,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = when (task.status) {
                                    TaskStatus.PENDING -> "⏳ Pending"
                                    TaskStatus.IN_PROGRESS -> "⚡ Running"
                                    TaskStatus.SUCCESS -> "✅ Done"
                                    TaskStatus.SKIPPED -> "⚠️ Skipped"
                                },
                                color = if (task.status == TaskStatus.SUCCESS) CyberEmerald else CyberCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val wvA = webViewTabA
                            val wvB = webViewTabB ?: webViewTabA
                            if (wvA != null && wvB != null) {
                                taskEngine.runAutonomousCrossTabLoop(
                                    webViewA = wvA,
                                    webViewB = wvB,
                                    onStatus = { st -> statusBannerText = st }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Start Loop", tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("অটো লুপ রান করুন", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val wvB = webViewTabB ?: webViewTabA
                            if (wvB != null) {
                                GodModeAutoSync.scanAndSyncManualFix(
                                    context = context,
                                    webViewB = wvB,
                                    taskEngine = taskEngine,
                                    onComplete = { _, summary ->
                                        statusBannerText = "⚡ God-Mode সিঙ্ক সম্পন্ন: $summary"
                                    }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Tab B ব্লুপ্রিন্ট সিঙ্ক",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // MODAL 3: Universal AI Settings Sheet
    if (showAiSettingsSheet) {
        UniversalAiSettingsSheet(onDismiss = { showAiSettingsSheet = false })
    }

    // MODAL 3.2: Omni Layout & Visual Button Editor Sheet
    if (showOmniLayoutVisualEditor) {
        com.example.ui.ai.OmniLayoutVisualEditorSheet(onDismiss = { showOmniLayoutVisualEditor = false })
    }

    // MODAL 3.5: App Truth & Code Anatomy Dossier Sheet
    if (showTruthDossierSheet) {
        AppTruthDossierSheet(onDismiss = { showTruthDossierSheet = false })
    }

    // MODAL 4: AI Summarizer Sheet
    if (showSummarizerSheet) {
        AiSummarizerSheet(
            webViewA = webViewTabA,
            webViewB = webViewTabB ?: webViewTabA,
            onSendSummaryToTabB = { summaryPayload ->
                showSummarizerSheet = false
                val targetWv = webViewTabB ?: webViewTabA
                if (targetWv != null && summaryPayload.isNotBlank()) {
                    scope.launch {
                        val file = FileStreamHelper.writeCodeToFile(context, summaryPayload, "summary_to_tab_b.txt")
                        FileStreamHelper.streamCodeToWebView(targetWv, file)
                        GoalHeuristics.executeGoalAction(targetWv, GoalType.CUSTOM)
                    }
                }
            },
            onOpenAiSettings = {
                showSummarizerSheet = false
                showAiSettingsSheet = true
            },
            onDismiss = { showSummarizerSheet = false }
        )
    }

    // MODAL 5: Chat History Drawer Sheet
    if (showChatHistorySheet) {
        ChatHistoryDrawerSheet(
            onNewBlankCanvas = {
                showChatHistorySheet = false
                NanobotSwarmOrchestrator.clearRelayFeed()
                statusBannerText = "🧹 নতুন ব্লাঙ্ক ক্যানভাস ও সেশন শুরু হয়েছে।"
            },
            onDismiss = { showChatHistorySheet = false },
            onSessionSelected = { sessionId ->
                statusBannerText = "📂 সেশন লোড হয়েছে: $sessionId"
                showChatHistorySheet = false
            },
            onStatusToast = { msg ->
                statusBannerText = msg
            }
        )
    }

    // MODAL 6: AI Chromium Dev Console Sheet
    if (showDevConsoleSheet) {
        AiDevConsoleSheet(
            activeWebView = activeWebViewInstance,
            isDesktopMode = isDesktopMode,
            onToggleDesktopMode = { enabled ->
                isDesktopMode = enabled
                webViewTabA?.let { AiChromiumDevEngine.applyDeveloperSettings(it, enabled) }
                webViewTabB?.let { AiChromiumDevEngine.applyDeveloperSettings(it, enabled) }
            },
            onTransferExtractedCodeToTabB = { extractedCode ->
                showDevConsoleSheet = false
                val targetWv = webViewTabB ?: webViewTabA
                if (targetWv != null && extractedCode.isNotBlank()) {
                    scope.launch {
                        val file = FileStreamHelper.writeCodeToFile(context, extractedCode, "extracted_dev_code.js")
                        FileStreamHelper.streamCodeToWebView(targetWv, file)
                        GoalHeuristics.executeGoalAction(targetWv, GoalType.RUN_CODE)
                    }
                }
            },
            onDismiss = { showDevConsoleSheet = false }
        )
    }

    // MODAL 7: Autonomy Permissions Sheet
    if (showPermissionsSheet) {
        AutonomyPermissionsSheet(
            isContinuousVoiceActive = isContinuousVoiceActive,
            onToggleContinuousVoice = { enabled ->
                isContinuousVoiceActive = enabled
                voiceEngine?.setContinuousMode(enabled)
            },
            onDismiss = { showPermissionsSheet = false }
        )
    }

    // MODAL 8: Global Rule Vault, AI Master Planner, Autonomous Search Crawler & Shared Memory
    if (showGlobalRuleAndPlannerSheet) {
        GlobalRuleAndPlannerSheet(
            initialTab = plannerSheetInitialTab,
            activeWebView = activeWebViewInstance,
            onApplySwarmBlueprint = { blueprint, autoStart ->
                showGlobalRuleAndPlannerSheet = false
                showNanobotStudio = true
                if (autoStart) {
                    NanobotSwarmOrchestrator.startContinuousRelay(
                        context = context,
                        nodes = blueprint.nodes,
                        connections = blueprint.connections,
                        webViewA = webViewTabA,
                        webViewB = webViewTabB ?: webViewTabA,
                        onCanvasInvalidate = {},
                        onCheckpointReached = {
                            showOnScreenReplyModal = true
                        }
                    )
                }
            },
            onInjectTextToSwarm = { injected ->
                statusBannerText = "✨ নতুন তথ্য/মেমোরি ন্যানোবট চেইনে যুক্ত হয়েছে (${injected.take(45)}...)"
                showGlobalRuleAndPlannerSheet = false
            },
            onDismiss = { showGlobalRuleAndPlannerSheet = false }
        )
    }

    // MODAL 9: Live Sandbox Preview & Exporter Sheet
    if (showLiveSandboxSheet) {
        LiveSandboxSheet(
            initialOutputOrCode = sandboxCustomHtml.ifBlank { latestRelayEvent?.replyOrThoughtOutput.orEmpty() },
            onSendToTabB = { codeToRoute ->
                val targetWv = webViewTabB ?: webViewTabA
                if (targetWv != null && codeToRoute.isNotBlank()) {
                    scope.launch {
                        val file = FileStreamHelper.writeCodeToFile(context, codeToRoute, "sandbox_to_tab_b.js")
                        FileStreamHelper.streamCodeToWebView(targetWv, file)
                        GoalHeuristics.executeGoalAction(targetWv, GoalType.RUN_CODE)
                    }
                }
            },
            onDismiss = {
                sandboxCustomHtml = ""
                showLiveSandboxSheet = false
            }
        )
    }

    // MODAL 10: Pro Lab Command Hub (All Pro Tools, Omni-Controller, Dual-Gear, Key Radar, .ZIP Builder, Ghost Macro & API Sniffer)
    if (showProToolsHubSheet) {
        ProToolsHubSheet(
            initialTab = proToolsInitialTab,
            activeWebView = activeWebViewInstance,
            omniControlBridge = omniControlBridge,
            onOpenNanobotSandbox = {
                sandboxCustomHtml = ""
                showProToolsHubSheet = false
                showLiveSandboxSheet = true
            },
            onOpenLiveSandboxWithHtml = { combinedHtml ->
                sandboxCustomHtml = combinedHtml
                showProToolsHubSheet = false
                showLiveSandboxSheet = true
            },
            onOpenTaskQueue = {
                showProToolsHubSheet = false
                showTaskQueueSheet = true
            },
            onOpenSummarizer = {
                showProToolsHubSheet = false
                showSummarizerSheet = true
            },
            onOpenDevConsole = {
                showProToolsHubSheet = false
                showDevConsoleSheet = true
            },
            onOpenChatHistory = {
                showProToolsHubSheet = false
                quadChatInitialChamber = 0
                showQuadChatSheet = true
            },
            onOpenPermissions = {
                showProToolsHubSheet = false
                showPermissionsSheet = true
            },
            onOpenAiSettings = {
                showProToolsHubSheet = false
                showAiSettingsSheet = true
            },
            onDismiss = { showProToolsHubSheet = false }
        )
    }

    // MODAL 11: 4-Chamber Omni Chat, Strainer (✅/❌), External AI Staging Vault & Auto-Debugger
    if (showQuadChatSheet) {
        QuadChatWorkspaceSheet(
            initialChamber = quadChatInitialChamber,
            activeWebView = activeWebViewInstance,
            liveVoiceTranscript = liveVoiceTranscript,
            isVoiceListening = isVoiceListening,
            onToggleVoiceMic = toggleVoiceAction,
            onOpenSandboxWithPayload = { payload ->
                sandboxCustomHtml = payload
                showQuadChatSheet = false
                showLiveSandboxSheet = true
            },
            onOpenClassicSessionDrawer = {
                showQuadChatSheet = false
                showChatHistorySheet = true
            },
            onOpenRemixedTaskLoop = {
                showQuadChatSheet = false
                showTaskQueueSheet = true
            },
            onOpenRemixedSummarizer = {
                showQuadChatSheet = false
                showSummarizerSheet = true
            },
            onOpenSovereignDevConsole = {
                showQuadChatSheet = false
                showDevConsoleSheet = true
            },
            onDismiss = { showQuadChatSheet = false }
        )
    }

    // MODAL 12: Living App Anatomy & Cellular Consciousness Matrix Sheet
    if (showCellularAnatomySheet) {
        AppCellularAnatomySheet(
            activeWebView = activeWebViewInstance,
            activeScreen = if (mainScreenMode == MainScreenMode.AI_CHAT) "AI_CHAT" else "BROWSER",
            onDismissRequest = { showCellularAnatomySheet = false }
        )
    }
}

@Composable
private fun ProPillarDockButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    badgeText: String,
    tint: Color,
    tag: String,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0E172A),
                        Color(0xFF070C17)
                    )
                )
            )
            .border(1.dp, tint.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 7.dp)
            .testTag(tag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = badgeText,
                color = tint,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ChromiumWebViewContainer(
    initialUrl: String,
    navTriggerId: Int = 0,
    isTabB: Boolean,
    isDesktopMode: Boolean,
    onWebViewCreated: (WebView) -> Unit,
    onUrlChanged: (String) -> Unit,
    onConsoleMessage: (ConsoleMessage) -> Unit,
    onOpenNanobotStudio: () -> Unit,
    onOpenQuadChat: (Int) -> Unit = {},
    onOpenDevConsole: () -> Unit = {},
    onOpenProLab: (Int) -> Unit = {},
    onExecuteOmniCommand: (String) -> Unit = {},
    onStartVoice: () -> Unit,
    onStopVoice: () -> Unit
) {
    // Support in-app OAuth / Social Login popup windows (e.g. "Continue with Google", GitHub, Apple, Facebook)
    // so login popups render cleanly inside the tab and auto-close when login completes!
    var oauthPopupWebView by remember { mutableStateOf<WebView?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    this.tag = navTriggerId
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        loadsImagesAutomatically = true
                        blockNetworkImage = false
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        setSupportZoom(true)
                        builtInZoomControls = true
                        displayZoomControls = false
                        mediaPlaybackRequiresUserGesture = false
                        setSupportMultipleWindows(true)
                        javaScriptCanOpenWindowsAutomatically = true
                        setGeolocationEnabled(true)
                        userAgentString = AiChromiumDevEngine.getGenuineHumanChromeUserAgent(ctx, isDesktopMode)
                    }

                    AiChromiumDevEngine.applyDeveloperSettings(this, isDesktopMode)

                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptCookie(true)
                    cookieManager.setAcceptThirdPartyCookies(this, true)

                    val godBridge = GodBridge(
                        context = ctx,
                        webViewProvider = { this },
                        onStartVoice = { onStartVoice() },
                        onRouteToCompiler = { onOpenProLab(0) }
                    )
                    addJavascriptInterface(godBridge, "GodBridge")

                    webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): WebResourceResponse? {
                            val reqUrl = request?.url?.toString().orEmpty()
                            if (reqUrl.isNotBlank() && AiChromiumDevEngine.isKnownThirdPartyTrackerUrl(reqUrl)) {
                                return WebResourceResponse(
                                    "text/plain",
                                    "UTF-8",
                                    ByteArrayInputStream(ByteArray(0))
                                )
                            }
                            return super.shouldInterceptRequest(view, request)
                        }

                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            url?.let { onUrlChanged(it) }
                            if (view != null) {
                                AiChromiumDevEngine.injectStealthHumanChromeShield(view)
                            }
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            url?.let { onUrlChanged(it) }
                            if (view != null) {
                                AiChromiumDevEngine.injectBuiltInFlagScripts(view)
                                runCatching { CookieManager.getInstance().flush() }
                            }
                        }

                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val reqUrl = request?.url?.toString().orEmpty()
                            if (reqUrl.isBlank()) return false

                            if (reqUrl.startsWith("http://") ||
                                reqUrl.startsWith("https://") ||
                                reqUrl.startsWith("file://") ||
                                reqUrl.startsWith("data:") ||
                                reqUrl.startsWith("about:") ||
                                reqUrl.startsWith("javascript:")
                            ) {
                                return false // Load directly inside this Chromium WebView!
                            }

                            // Handle intent:// or OAuth custom schemes in-app without kicking user out to external browser
                            if (reqUrl.startsWith("intent://")) {
                                try {
                                    val fallbackUrl = Regex("S\\.browser_fallback_url=([^;]+)").find(reqUrl)?.groupValues?.getOrNull(1)
                                    if (!fallbackUrl.isNullOrBlank()) {
                                        val decoded = android.net.Uri.decode(fallbackUrl)
                                        view?.loadUrl(decoded)
                                        return true
                                    }
                                } catch (_: Throwable) {}
                            }
                            return true // Never launch external browser intents so user is never kicked out
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onCreateWindow(
                            view: WebView?,
                            isDialog: Boolean,
                            isUserGesture: Boolean,
                            resultMsg: Message?
                        ): Boolean {
                            val ctxInner = view?.context ?: return false
                            val transport = resultMsg?.obj as? WebView.WebViewTransport ?: return false

                            val popupWv = WebView(ctxInner).apply {
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    databaseEnabled = true
                                    javaScriptCanOpenWindowsAutomatically = true
                                    setSupportMultipleWindows(true)
                                    useWideViewPort = true
                                    loadWithOverviewMode = true
                                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                    userAgentString = AiChromiumDevEngine.getGenuineHumanChromeUserAgent(ctxInner, isDesktopMode)
                                }
                                CookieManager.getInstance().setAcceptCookie(true)
                                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(v: WebView?, u: String?, f: Bitmap?) {
                                        super.onPageStarted(v, u, f)
                                        if (v != null) {
                                            AiChromiumDevEngine.injectStealthHumanChromeShield(v)
                                        }
                                    }

                                    override fun onPageFinished(v: WebView?, u: String?) {
                                        super.onPageFinished(v, u)
                                        runCatching { CookieManager.getInstance().flush() }
                                    }

                                    override fun shouldOverrideUrlLoading(
                                        v: WebView?,
                                        req: WebResourceRequest?
                                    ): Boolean {
                                        val u = req?.url?.toString().orEmpty()
                                        if (u.startsWith("http://") || u.startsWith("https://")) {
                                            return false
                                        }
                                        return true
                                    }
                                }

                                webChromeClient = object : WebChromeClient() {
                                    override fun onCloseWindow(window: WebView?) {
                                        super.onCloseWindow(window)
                                        runCatching { CookieManager.getInstance().flush() }
                                        oauthPopupWebView = null
                                        try {
                                            window?.destroy()
                                        } catch (_: Throwable) {}
                                    }

                                    override fun onPermissionRequest(request: PermissionRequest?) {
                                        request?.grant(request.resources)
                                    }
                                }
                            }

                            oauthPopupWebView = popupWv
                            transport.webView = popupWv
                            resultMsg.sendToTarget()
                            return true
                        }

                        override fun onCloseWindow(window: WebView?) {
                            super.onCloseWindow(window)
                            oauthPopupWebView = null
                        }

                        override fun onPermissionRequest(request: PermissionRequest?) {
                            request?.grant(request.resources)
                        }

                        override fun onGeolocationPermissionsShowPrompt(
                            origin: String?,
                            callback: GeolocationPermissions.Callback?
                        ) {
                            callback?.invoke(origin, true, false)
                        }

                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                            if (consoleMessage != null) {
                                AiChromiumDevEngine.recordConsoleMessage(consoleMessage)
                                if (isTabB) {
                                    onConsoleMessage(consoleMessage)
                                }
                            }
                            return super.onConsoleMessage(consoleMessage)
                        }
                    }

                    setDownloadListener { url, _, _, _, _ ->
                        if (url.startsWith("http://") || url.startsWith("https://")) {
                            Toast.makeText(ctx, "📥 ফাইল/ছবি ওপেন হচ্ছে...", Toast.LENGTH_SHORT).show()
                            post { loadUrl(url) }
                        }
                    }

                    loadUrl(initialUrl)
                    onWebViewCreated(this)
                }
            },
            update = { webView ->
                val lastTrigger = webView.tag as? Int ?: -1
                if (lastTrigger != navTriggerId && initialUrl.isNotBlank()) {
                    webView.tag = navTriggerId
                    webView.loadUrl(initialUrl)
                }
            }
        )

        // In-App OAuth / Login Popup Overlay (When "Continue with Google" or Social Login opens a popup window)
        if (oauthPopupWebView != null) {
            Surface(
                color = Color(0xFF040812),
                modifier = Modifier.fillMaxSize()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0C1526))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔐 নিরাপদ লগইন পপআপ (In-App Login)",
                            color = CyberCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Surface(
                            color = Color(0xFF280A14),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(0.7.dp, CyberRose),
                            modifier = Modifier.clickable {
                                val pop = oauthPopupWebView
                                oauthPopupWebView = null
                                runCatching {
                                    CookieManager.getInstance().flush()
                                    pop?.destroy()
                                }
                            }
                        ) {
                            Text(
                                text = "✕ বন্ধ করুন",
                                color = CyberRose,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    AndroidView(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        factory = { oauthPopupWebView!! }
                    )
                }
            }
        }
    }
}
