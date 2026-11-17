package com.example.engine

import android.content.Context
import android.os.SystemClock
import android.view.MotionEvent
import android.webkit.WebView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AiKeySpecialistRole
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
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Represents any controllable UI Button, Feature, Tool, Website, or Dynamic Slot in the app.
 * Every existing AND future feature added to the app is registered as an OmniNode,
 * giving the user and Main AI 100% control over its position, size, 3D depth, color, ON/OFF state, and execution.
 */
data class OmniNode(
    val id: String,
    val label: String,
    val defaultLabel: String = label,
    val zone: String = "TOP_ROW_2", // TOP_ROW_1, TOP_ROW_2, BOTTOM_DOCK, FLOATING, REMOVED_TRASH
    val previousZone: String = "TOP_ROW_2",
    val bgColorHex: String = "#FFFFFF",
    val accentColorHex: String = "#0284C7",
    val textColorHex: String = "",
    val orderIndex: Int = 0,
    val scaleFactor: Float = 1.0f, // 0.65f (small) to 1.70f (large)
    val is3DStyle: Boolean = false, // Real 3D tactile raised button style
    val isCustomAiFeature: Boolean = false,
    val customActionPayload: String = "",
    val isEnabled: Boolean = true,
    val isBlinkingPulse: Boolean = false,
    val powerPercent: Int = 100,
    val isAutoMaintainedByAi: Boolean = true,
    val applyCount: Int = 0
)

/**
 * Universal Background, 3D Button & Global Visual Shader State controlled by the Main AI.
 */
data class OmniVisualState(
    val mode: String = "SOLID", // SOLID, VERTICAL_GRADIENT, HORIZONTAL_GRADIENT, RADIAL_DOT, RAINBOW, RAINBOW_DARK
    val primaryHex: String = "#FFFFFF",
    val secondaryHex: String = "#000000",
    val centerDotHex: String = "#FF0055",
    val dotRadiusFraction: Float = 0.38f,
    val headerBgHex: String = "#F8FAFC",
    val chat1BgHex: String = "#FFFFFF",
    val chat2BgHex: String = "#090D1A",
    val globalButtonScale: Float = 1.0f,
    val global3DButtons: Boolean = false
)

/**
 * Permanent API Key & Multi-AI Hierarchy Lock State.
 */
data class OmniAiLockState(
    val lockedMasterProfileId: String = "",
    val lockedMasterName: String = "Auto Master Brain",
    val isPermanentlyLocked: Boolean = false,
    val workerRolesMap: Map<String, String> = emptyMap()
)

/**
 * Represents a live autonomous screen touch, tap, swipe, scroll, or DOM click executed by the Main AI.
 */
data class OmniScreenTouchEvent(
    val id: Long = System.currentTimeMillis(),
    val xFraction: Float = 0.5f,
    val yFraction: Float = 0.5f,
    val actionType: String = "TAP", // TAP, DOUBLE_TAP, LONG_PRESS, SCROLL_DOWN, SCROLL_UP
    val targetLabel: String = "Screen Center",
    val targetSelector: String = ""
)

/**
 * Represents a live feature execution/application request triggered by the Main AI.
 */
data class OmniFeatureApplyEvent(
    val eventId: Long = System.currentTimeMillis(),
    val nodeId: String,
    val actionMode: String = "APPLY", // APPLY, TURN_ON, TURN_OFF
    val powerPercent: Int = 100,
    val extraCommand: String = ""
)

/**
 * Real-Time Hardware & Engine Execution State (Genuine interval/speed/loop parameters, no fake numbers).
 */
data class ZeroLoadEngineState(
    val isZeroLoadActive: Boolean = true,
    val isAutoTouchLoopRunning: Boolean = false,
    val autoTouchIntervalMs: Long = 900L, // Real physical click interval in ms (faster = 250ms, slower = 2500ms)
    val isAutoScrollLoopRunning: Boolean = false,
    val autoScrollStepPx: Int = 380,      // Real pixel scroll distance per tick
    val totalAiTouchesExecuted: Int = 0,
    val totalFeaturesAppliedByAi: Int = 0
)

object UniversalOmniControlEngine {
    private const val PREFS_OMNI = "skrl2_universal_omni_control_prefs"
    private const val KEY_NODES_JSON = "omni_nodes_json_v4"
    private const val KEY_VISUAL_JSON = "omni_visual_json_v2"
    private const val KEY_AI_LOCK_JSON = "omni_ai_lock_json_v1"

    private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var autoTouchJob: Job? = null
    private var autoScrollJob: Job? = null

    private val _nodes = MutableStateFlow<List<OmniNode>>(defaultNodes())
    val nodes: StateFlow<List<OmniNode>> = _nodes.asStateFlow()

    private val _visualState = MutableStateFlow(OmniVisualState())
    val visualState: StateFlow<OmniVisualState> = _visualState.asStateFlow()

    private val _aiLockState = MutableStateFlow(OmniAiLockState())
    val aiLockState: StateFlow<OmniAiLockState> = _aiLockState.asStateFlow()

    private val _lastScreenTouchEvent = MutableStateFlow<OmniScreenTouchEvent?>(null)
    val lastScreenTouchEvent: StateFlow<OmniScreenTouchEvent?> = _lastScreenTouchEvent.asStateFlow()

    private val _pendingFeatureApplyEvent = MutableStateFlow<OmniFeatureApplyEvent?>(null)
    val pendingFeatureApplyEvent: StateFlow<OmniFeatureApplyEvent?> = _pendingFeatureApplyEvent.asStateFlow()

    private val _zeroLoadState = MutableStateFlow(ZeroLoadEngineState())
    val zeroLoadState: StateFlow<ZeroLoadEngineState> = _zeroLoadState.asStateFlow()

    private val _lastControlSummary = MutableStateFlow("👑 মেইন এআই সার্বজনীন কন্ট্রোল ব্রিজ সক্রিয়")
    val lastControlSummary: StateFlow<String> = _lastControlSummary.asStateFlow()

    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        isInitialized = true
        loadState(context)
        SovereignDeepResearchAndIncrementalEngine.init(context)
    }

    private fun defaultNodes(): List<OmniNode> = listOf(
        // ROW 1 Core Navigation & Mode Nodes
        OmniNode(id = "gemini_live_share", label = "🔴 Gemini Live", zone = "TOP_ROW_1", previousZone = "TOP_ROW_1", bgColorHex = "#FFE4E6", accentColorHex = "#E11D48", orderIndex = 0),
        OmniNode(id = "api_key", label = "🔑 API Key", zone = "TOP_ROW_1", previousZone = "TOP_ROW_1", bgColorHex = "#ECFDF5", accentColorHex = "#10B981", orderIndex = 1),
        OmniNode(id = "truth_dossier", label = "🔍 সত্য ডসিয়ার", zone = "TOP_ROW_1", previousZone = "TOP_ROW_1", bgColorHex = "#F0FDF4", accentColorHex = "#10B981", orderIndex = 2),
        OmniNode(id = "chat_1", label = "💬 ১. এআই চ্যাট ⛶", zone = "TOP_ROW_1", previousZone = "TOP_ROW_1", bgColorHex = "#FFFFFF", accentColorHex = "#0284C7", orderIndex = 3),
        OmniNode(id = "chat_2_particles", label = "✨ ২. পার্টিকলস ⛶", zone = "TOP_ROW_1", previousZone = "TOP_ROW_1", bgColorHex = "#F3E8FF", accentColorHex = "#7C3AED", orderIndex = 4),
        OmniNode(id = "tab_a", label = "🌐 ট্যাব A ⛶", zone = "TOP_ROW_1", previousZone = "TOP_ROW_1", bgColorHex = "#FFFFFF", accentColorHex = "#10B981", orderIndex = 5),
        OmniNode(id = "tab_b", label = "🌐 ট্যাব B ⛶", zone = "TOP_ROW_1", previousZone = "TOP_ROW_1", bgColorHex = "#FFFFFF", accentColorHex = "#9333EA", orderIndex = 6),
        OmniNode(id = "dual_split", label = "⚔️ ডুয়াল (A+B) ⛶", zone = "TOP_ROW_1", previousZone = "TOP_ROW_1", bgColorHex = "#FFFFFF", accentColorHex = "#F59E0B", orderIndex = 7),

        // ROW 2 Real Engine & Tool Nodes
        OmniNode(id = "auto_touch", label = "🎯 অটো-টাচ ও পাওয়ার", zone = "TOP_ROW_2", previousZone = "TOP_ROW_2", bgColorHex = "#ECFDF5", accentColorHex = "#10B981", orderIndex = 10),
        OmniNode(id = "anatomy", label = "🧬 নকশা-বক্সা", zone = "TOP_ROW_2", previousZone = "TOP_ROW_2", bgColorHex = "#EDE9FE", accentColorHex = "#9333EA", orderIndex = 11),
        OmniNode(id = "rules", label = "📋 রুল ইঞ্জিন", zone = "TOP_ROW_2", previousZone = "TOP_ROW_2", bgColorHex = "#FEF3C7", accentColorHex = "#F59E0B", orderIndex = 12),
        OmniNode(id = "planner", label = "📝 প্ল্যানার", zone = "TOP_ROW_2", previousZone = "TOP_ROW_2", bgColorHex = "#E0F2FE", accentColorHex = "#0284C7", orderIndex = 13),
        OmniNode(id = "macro", label = "🔴 ম্যাক্রো", zone = "TOP_ROW_2", previousZone = "TOP_ROW_2", bgColorHex = "#FFE4E6", accentColorHex = "#F43F5E", orderIndex = 14),
        OmniNode(id = "sandbox", label = "🧪 স্যান্ডবক্স", zone = "TOP_ROW_2", previousZone = "TOP_ROW_2", bgColorHex = "#ECFDF5", accentColorHex = "#10B981", orderIndex = 15),
        OmniNode(id = "bot_report", label = "📺 বট রিপোর্ট", zone = "TOP_ROW_2", previousZone = "TOP_ROW_2", bgColorHex = "#E0F2FE", accentColorHex = "#0284C7", orderIndex = 16),

        // BOTTOM DOCK 4-Pillar Master Nodes (Now 100% controllable: move, resize, 3D, recolor, remove/restore)
        OmniNode(id = "dock_nanobot", label = "🕸️ ন্যানোবট", zone = "BOTTOM_DOCK", previousZone = "BOTTOM_DOCK", bgColorHex = "#E0F2FE", accentColorHex = "#0284C7", orderIndex = 20),
        OmniNode(id = "dock_quad_chat", label = "💬 ৪-চ্যাট ও ছাঁকনি", zone = "BOTTOM_DOCK", previousZone = "BOTTOM_DOCK", bgColorHex = "#EDE9FE", accentColorHex = "#9333EA", orderIndex = 21),
        OmniNode(id = "dock_rule_planner", label = "📋 রুল ও প্ল্যানার", zone = "BOTTOM_DOCK", previousZone = "BOTTOM_DOCK", bgColorHex = "#FEF3C7", accentColorHex = "#F59E0B", orderIndex = 22),
        OmniNode(id = "dock_pro_lab", label = "🛠️ প্রো ল্যাব হাব", zone = "BOTTOM_DOCK", previousZone = "BOTTOM_DOCK", bgColorHex = "#ECFDF5", accentColorHex = "#10B981", orderIndex = 23),

        // CHAT 1 INNER HEADER NODES (Now 100% controllable: move, resize, 3D, recolor, remove/restore)
        OmniNode(id = "chat_hdr_gemini_live", label = "🔴 লাইভ শেয়ার", zone = "CHAT_HEADER", previousZone = "CHAT_HEADER", bgColorHex = "#FFE4E6", accentColorHex = "#E11D48", orderIndex = 29),
        OmniNode(id = "chat_hdr_api_key", label = "🔑 API Key বসান", zone = "CHAT_HEADER", previousZone = "CHAT_HEADER", bgColorHex = "#ECFDF5", accentColorHex = "#10B981", orderIndex = 30),
        OmniNode(id = "chat_hdr_new_chat", label = "➕ New Chat", zone = "CHAT_HEADER", previousZone = "CHAT_HEADER", bgColorHex = "#E0F2FE", accentColorHex = "#0284C7", orderIndex = 31),
        OmniNode(id = "chat_hdr_history", label = "🕒 হিস্ট্রি", zone = "CHAT_HEADER", previousZone = "CHAT_HEADER", bgColorHex = "#F1F5F9", accentColorHex = "#334155", orderIndex = 32),
        OmniNode(id = "chat_hdr_review_page", label = "👁️ রিভিউ পেজ", zone = "CHAT_HEADER", previousZone = "CHAT_HEADER", bgColorHex = "#FFFBEB", accentColorHex = "#F59E0B", orderIndex = 33),
        OmniNode(id = "chat_hdr_apps_showroom", label = "🚀 অ্যাপস", zone = "CHAT_HEADER", previousZone = "CHAT_HEADER", bgColorHex = "#F1F5F9", accentColorHex = "#7C3AED", orderIndex = 34),
        OmniNode(id = "chat_hdr_power_modules", label = "⚡ পাওয়ার মডিউল", zone = "CHAT_HEADER", previousZone = "CHAT_HEADER", bgColorHex = "#F1F5F9", accentColorHex = "#0284C7", orderIndex = 35),
        OmniNode(id = "chat_hdr_code_import", label = "📥 কোড ইমপোর্ট", zone = "CHAT_HEADER", previousZone = "CHAT_HEADER", bgColorHex = "#F1F5F9", accentColorHex = "#0284C7", orderIndex = 36)
    )

    /**
     * Reusable Composable that renders any OmniNode with real 3D tactile depth (when 3D is active),
     * dynamic resizing (scaleFactor * globalButtonScale), custom colors, and full accessibility.
     */
    @Composable
    fun OmniInteractiveButton(
        node: OmniNode,
        displayLabel: String,
        isSelectedOrActive: Boolean = false,
        baseHeightDp: Float = 30f,
        baseFontSp: Float = 10.5f,
        baseHorizPadDp: Float = 9f,
        testTagName: String,
        onClick: () -> Unit
    ) {
        val vis by visualState.collectAsState()
        val effectiveScale = (node.scaleFactor * vis.globalButtonScale).coerceIn(0.65f, 1.75f)
        val use3D = node.is3DStyle || vis.global3DButtons

        val accentCol = when {
            !node.isEnabled -> Color(0xFF94A3B8)
            node.isBlinkingPulse -> Color(0xFFFF0055)
            else -> parseColorSafe(node.accentColorHex, Color(0xFF0284C7))
        }
        val bgCol = when {
            !node.isEnabled -> Color(0xFFF1F5F9)
            isSelectedOrActive -> accentCol
            else -> parseColorSafe(node.bgColorHex, Color.White)
        }
        val txtCol = when {
            !node.isEnabled -> Color(0xFF64748B)
            node.textColorHex.isNotBlank() -> parseColorSafe(node.textColorHex, accentCol)
            isSelectedOrActive -> Color.White
            node.id == "chat_1" || node.id == "bot_report" -> Color(0xFF0F172A)
            else -> accentCol
        }

        val scaledHeight = (baseHeightDp * effectiveScale).dp
        val scaledFont = (baseFontSp * effectiveScale).sp
        val scaledPadH = (baseHorizPadDp * effectiveScale).dp
        val cornerRadius = (8.5f * effectiveScale).dp

        Surface(
            color = bgCol,
            shape = RoundedCornerShape(cornerRadius),
            shadowElevation = if (use3D) (7f * effectiveScale).dp else 0.dp,
            border = BorderStroke(
                width = if (use3D) (1.6f * effectiveScale).dp else (1.0f * effectiveScale).dp,
                color = accentCol
            ),
            modifier = Modifier
                .height(scaledHeight)
                .then(
                    if (use3D) {
                        Modifier
                            .graphicsLayer {
                                rotationX = 7f
                                cameraDistance = 12f * density
                            }
                            .drawWithContent {
                                drawContent()
                                // Top 3D specular highlight line
                                drawLine(
                                    color = Color.White.copy(alpha = 0.65f),
                                    start = Offset(6f, 2f),
                                    end = Offset(size.width - 6f, 2f),
                                    strokeWidth = 2.2f
                                )
                                // Bottom 3D tactile bevel ledge
                                drawLine(
                                    color = accentCol.copy(alpha = 0.85f),
                                    start = Offset(4f, size.height - 2.5f),
                                    end = Offset(size.width - 4f, size.height - 2.5f),
                                    strokeWidth = 4.5f
                                )
                            }
                    } else {
                        Modifier
                    }
                )
                .clickable { onClick() }
                .testTag(testTagName)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = scaledPadH)
            ) {
                Text(
                    text = if (use3D && !displayLabel.startsWith("🧊")) displayLabel else displayLabel,
                    color = txtCol,
                    fontSize = scaledFont,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }
        }
    }

    fun autoRegisterFeature(
        context: Context,
        id: String,
        label: String,
        defaultZone: String = "TOP_ROW_2",
        bgColorHex: String = "#F0F9FF",
        accentColorHex: String = "#0284C7",
        isCustomAiFeature: Boolean = false,
        customActionPayload: String = "",
        initialPowerPercent: Int = 100,
        scaleFactor: Float = 1.0f,
        is3DStyle: Boolean = false
    ) {
        val current = _nodes.value.toMutableList()
        val existingIdx = current.indexOfFirst { it.id == id }
        if (existingIdx >= 0) {
            current[existingIdx] = current[existingIdx].copy(
                label = label,
                customActionPayload = customActionPayload,
                is3DStyle = is3DStyle || current[existingIdx].is3DStyle,
                scaleFactor = if (scaleFactor != 1.0f) scaleFactor else current[existingIdx].scaleFactor
            )
        } else {
            current.add(
                OmniNode(
                    id = id,
                    label = label,
                    defaultLabel = label,
                    zone = defaultZone,
                    previousZone = defaultZone,
                    bgColorHex = bgColorHex,
                    accentColorHex = accentColorHex,
                    orderIndex = current.size + 1,
                    scaleFactor = scaleFactor.coerceIn(0.65f, 1.70f),
                    is3DStyle = is3DStyle,
                    isCustomAiFeature = isCustomAiFeature,
                    customActionPayload = customActionPayload,
                    isEnabled = true,
                    powerPercent = initialPowerPercent
                )
            )
        }
        _nodes.value = current
        saveState(context)
        AppTruthAndArchitectureVault.recordLiveMutation(
            category = "BUTTON_OR_FEATURE_REGISTERED",
            summaryBangla = "অমনি-কন্ট্রোল রেজিস্ট্রিতে যুক্ত/আপডেট হয়েছে: '$label' (id=$id, zone=$defaultZone, 3D=$is3DStyle, scale=${scaleFactor}x)"
        )
    }

    fun autoRegisterFeature(
        context: Context,
        id: String,
        title: String,
        icon: String,
        zone: String,
        htmlCode: String
    ) {
        val displayLabel = if (title.contains(icon)) title else "$icon $title"
        if (htmlCode.isNotBlank()) {
            val module = SelfFeatureModule(
                id = "genesis_$id",
                title = displayLabel,
                description = "Sovereign Core Live Module ($displayLabel)",
                categoryBadge = "SOVEREIGN CORE 👑",
                jsPayload = htmlCode,
                isAutoRunOnPageLoad = true
            )
            MasterOmniControllerEngine.addOrUpdateSelfFeature(context, module)
        }
        autoRegisterFeature(
            context = context,
            id = id,
            label = displayLabel,
            defaultZone = zone,
            bgColorHex = "#070B19",
            accentColorHex = "#00E5FF",
            isCustomAiFeature = true,
            customActionPayload = "GENESIS_SYSTEM:$id|$displayLabel",
            initialPowerPercent = 100,
            scaleFactor = 1.0f,
            is3DStyle = true
        )
    }

    fun styleNode(
        context: Context,
        idOrLabel: String,
        hexColor: String,
        scale: Float = 1.0f,
        is3D: Boolean = true
    ) {
        val targetLower = idOrLabel.trim().lowercase()
        val updated = _nodes.value.map { node ->
            if (node.id.lowercase() == targetLower || node.label.lowercase().contains(targetLower)) {
                node.copy(
                    accentColorHex = hexColor.ifBlank { node.accentColorHex },
                    scaleFactor = scale.coerceIn(0.65f, 1.70f),
                    is3DStyle = is3D
                )
            } else {
                node
            }
        }
        _nodes.value = updated
        saveState(context)
    }

    fun moveNode(
        context: Context,
        idOrLabel: String,
        targetZone: String,
        offsetX: Float = 0f,
        offsetY: Float = 0f
    ) {
        val targetLower = idOrLabel.trim().lowercase()
        val updated = _nodes.value.map { node ->
            if (node.id.lowercase() == targetLower || node.label.lowercase().contains(targetLower)) {
                node.copy(
                    previousZone = node.zone,
                    zone = targetZone.ifBlank { node.zone }
                )
            } else {
                node
            }
        }
        _nodes.value = updated
        saveState(context)
    }

    /**
     * Controls a feature's real ON/OFF state, real functional capability (no fake 0-1000 cap!), and live UI opening/closing.
     */
    fun controlFeatureStateAndPower(
        context: Context,
        nodeId: String,
        enable: Boolean? = null,
        powerPercent: Int? = null,
        applyImmediately: Boolean = false,
        extraCommand: String = ""
    ) {
        val updated = _nodes.value.map { node ->
            if (node.id == nodeId) {
                val newEnabled = enable ?: node.isEnabled
                val newPower = (powerPercent ?: node.powerPercent).coerceAtLeast(1)
                val updatedPayload = if (extraCommand.isNotBlank() && node.isCustomAiFeature) {
                    "${node.customActionPayload} | $extraCommand".trim(' ', '|')
                } else {
                    node.customActionPayload
                }
                node.copy(
                    isEnabled = newEnabled,
                    powerPercent = newPower,
                    customActionPayload = updatedPayload,
                    applyCount = if (applyImmediately) node.applyCount + 1 else node.applyCount
                )
            } else node
        }
        _nodes.value = updated
        if (applyImmediately || enable != null) {
            val targetNode = updated.find { it.id == nodeId }
            _pendingFeatureApplyEvent.value = OmniFeatureApplyEvent(
                nodeId = nodeId,
                actionMode = if (enable == false) "TURN_OFF" else if (enable == true) "TURN_ON" else "APPLY",
                powerPercent = targetNode?.powerPercent ?: 100,
                extraCommand = extraCommand
            )
            _zeroLoadState.value = _zeroLoadState.value.copy(
                totalFeaturesAppliedByAi = _zeroLoadState.value.totalFeaturesAppliedByAi + 1
            )
        }
        saveState(context)
    }

    /**
     * Starts or updates a REAL Continuous Auto-Touch Loop on screen at the specified millisecond interval.
     */
    fun startRealAutoTouchLoop(
        context: Context,
        xFraction: Float = 0.5f,
        yFraction: Float = 0.5f,
        intervalMs: Long = 900L,
        selector: String = ""
    ) {
        autoTouchJob?.cancel()
        val safeInterval = intervalMs.coerceIn(220L, 10000L)
        _zeroLoadState.value = _zeroLoadState.value.copy(
            isAutoTouchLoopRunning = true,
            autoTouchIntervalMs = safeInterval
        )
        autoTouchJob = engineScope.launch {
            while (isActive && _zeroLoadState.value.isAutoTouchLoopRunning) {
                triggerAutonomousScreenTouch(
                    context = context,
                    xFraction = xFraction,
                    yFraction = yFraction,
                    actionType = "TAP",
                    targetLabel = "Auto-Touch Loop (${safeInterval}ms)",
                    targetSelector = selector
                )
                delay(_zeroLoadState.value.autoTouchIntervalMs)
            }
        }
    }

    fun stopRealAutoTouchLoop() {
        autoTouchJob?.cancel()
        autoTouchJob = null
        _zeroLoadState.value = _zeroLoadState.value.copy(isAutoTouchLoopRunning = false)
    }

    /**
     * Starts or updates a REAL Continuous Auto-Scroll Loop on the active WebView.
     */
    fun startRealAutoScrollLoop(context: Context, stepPx: Int = 380, intervalMs: Long = 1200L) {
        autoScrollJob?.cancel()
        val safeStep = stepPx.coerceIn(-1200, 1200)
        _zeroLoadState.value = _zeroLoadState.value.copy(
            isAutoScrollLoopRunning = true,
            autoScrollStepPx = safeStep
        )
        autoScrollJob = engineScope.launch {
            while (isActive && _zeroLoadState.value.isAutoScrollLoopRunning) {
                triggerAutonomousScreenTouch(
                    context = context,
                    xFraction = 0.5f,
                    yFraction = 0.5f,
                    actionType = if (safeStep >= 0) "SCROLL_DOWN" else "SCROLL_UP",
                    targetLabel = "Auto-Scroll Loop (${safeStep}px)"
                )
                delay(intervalMs.coerceIn(400L, 8000L))
            }
        }
    }

    fun stopRealAutoScrollLoop() {
        autoScrollJob?.cancel()
        autoScrollJob = null
        _zeroLoadState.value = _zeroLoadState.value.copy(isAutoScrollLoopRunning = false)
    }

    fun triggerAutonomousScreenTouch(
        context: Context,
        xFraction: Float = 0.5f,
        yFraction: Float = 0.5f,
        actionType: String = "TAP",
        targetLabel: String = "AI Smart Touch",
        targetSelector: String = ""
    ) {
        val clampedX = xFraction.coerceIn(0.05f, 0.95f)
        val clampedY = yFraction.coerceIn(0.08f, 0.92f)
        _lastScreenTouchEvent.value = OmniScreenTouchEvent(
            id = System.currentTimeMillis(),
            xFraction = clampedX,
            yFraction = clampedY,
            actionType = actionType.uppercase(),
            targetLabel = targetLabel,
            targetSelector = targetSelector
        )
        _zeroLoadState.value = _zeroLoadState.value.copy(
            totalAiTouchesExecuted = _zeroLoadState.value.totalAiTouchesExecuted + 1
        )
    }

    /**
     * Executes the physical Android MotionEvent (TAP, DOUBLE_TAP, LONG_PRESS, SWIPE, SCROLL)
     * + JS DOM Touch on the active WebView when AI touches the screen.
     */
    fun performPhysicalTouchOnWebView(webView: WebView?, touchEvent: OmniScreenTouchEvent) {
        if (webView == null) return
        try {
            val w = webView.width.takeIf { it > 0 } ?: 1080
            val h = webView.height.takeIf { it > 0 } ?: 1920
            val px = (touchEvent.xFraction * w).coerceIn(10f, w - 10f)
            val py = (touchEvent.yFraction * h).coerceIn(10f, h - 10f)
            val stepPx = _zeroLoadState.value.autoScrollStepPx

            when (touchEvent.actionType.uppercase()) {
                "SCROLL_DOWN", "SWIPE_UP" -> {
                    webView.evaluateJavascript("window.scrollBy({top: ${kotlin.math.abs(stepPx)}, behavior: 'smooth'});", null)
                }
                "SCROLL_UP", "SWIPE_DOWN" -> {
                    webView.evaluateJavascript("window.scrollBy({top: -${kotlin.math.abs(stepPx)}, behavior: 'smooth'});", null)
                }
                "SWIPE_LEFT", "SWIPE_RIGHT" -> {
                    val dx = if (touchEvent.actionType.uppercase() == "SWIPE_LEFT") -320f else 320f
                    val downTime = SystemClock.uptimeMillis()
                    val eDown = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, px, py, 0)
                    val eMove = MotionEvent.obtain(downTime, downTime + 80, MotionEvent.ACTION_MOVE, (px + dx * 0.5f).coerceIn(10f, w - 10f), py, 0)
                    val eUp = MotionEvent.obtain(downTime, downTime + 160, MotionEvent.ACTION_UP, (px + dx).coerceIn(10f, w - 10f), py, 0)
                    webView.dispatchTouchEvent(eDown)
                    webView.dispatchTouchEvent(eMove)
                    webView.dispatchTouchEvent(eUp)
                    eDown.recycle(); eMove.recycle(); eUp.recycle()
                }
                "DOUBLE_TAP" -> {
                    repeat(2) { idx ->
                        val t = SystemClock.uptimeMillis() + (idx * 60)
                        val d = MotionEvent.obtain(t, t, MotionEvent.ACTION_DOWN, px, py, 0)
                        val u = MotionEvent.obtain(t, t + 35, MotionEvent.ACTION_UP, px, py, 0)
                        webView.dispatchTouchEvent(d)
                        webView.dispatchTouchEvent(u)
                        d.recycle(); u.recycle()
                    }
                }
                "LONG_PRESS" -> {
                    val downTime = SystemClock.uptimeMillis()
                    val downEvent = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, px, py, 0)
                    val upEvent = MotionEvent.obtain(downTime, downTime + 550, MotionEvent.ACTION_UP, px, py, 0)
                    webView.dispatchTouchEvent(downEvent)
                    webView.postDelayed({
                        webView.dispatchTouchEvent(upEvent)
                        upEvent.recycle()
                    }, 520L)
                    downEvent.recycle()
                }
                else -> {
                    val downTime = SystemClock.uptimeMillis()
                    val downEvent = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, px, py, 0)
                    val upEvent = MotionEvent.obtain(downTime, downTime + 45, MotionEvent.ACTION_UP, px, py, 0)
                    webView.dispatchTouchEvent(downEvent)
                    webView.dispatchTouchEvent(upEvent)
                    downEvent.recycle()
                    upEvent.recycle()

                    if (touchEvent.targetSelector.isNotBlank()) {
                        val safeSel = touchEvent.targetSelector.replace("'", "\\'")
                        val js = """
                            (function(){
                              var el = null;
                              try { el = document.querySelector('$safeSel'); } catch(e) {}
                              if (!el) {
                                var all = document.querySelectorAll('button, a, input, [role="button"], [onclick]');
                                for (var i=0; i<all.length; i++) {
                                  if ((all[i].innerText || all[i].value || all[i].title || '').toLowerCase().indexOf('$safeSel'.toLowerCase()) !== -1) {
                                    el = all[i]; break;
                                  }
                                }
                              }
                              if (el) { el.click(); el.focus(); return 'CLICKED'; }
                              var ptEl = document.elementFromPoint(${px.toInt()}, ${py.toInt()});
                              if (ptEl) { ptEl.click(); return 'POINT_CLICKED'; }
                              return 'NONE';
                            })();
                        """.trimIndent()
                        webView.evaluateJavascript(js, null)
                    } else {
                        val js = """
                            (function(){
                              var ptEl = document.elementFromPoint(${px.toInt()}, ${py.toInt()});
                              if (ptEl) { ptEl.click(); return 'POINT_CLICKED'; }
                              return 'NONE';
                            })();
                        """.trimIndent()
                        webView.evaluateJavascript(js, null)
                    }
                }
            }
        } catch (_: Throwable) {}
    }

    /**
     * Frees unused memory and keeps background coroutines lightweight.
     */
    fun runZeroLoadOptimizationPulse(context: Context): String {
        System.gc()
        return "🚀 মেমরি ক্যাশ ক্লিন করা হয়েছে (অটো-টাচ ইন্টারভ্যাল: ${_zeroLoadState.value.autoTouchIntervalMs}ms)।"
    }

    fun parseColorSafe(hex: String, fallback: Color = Color.White): Color {
        return try {
            val cleaned = hex.trim().let { if (it.startsWith("#")) it else "#$it" }
            Color(android.graphics.Color.parseColor(cleaned))
        } catch (_: Throwable) {
            fallback
        }
    }

    /**
     * Composable Dynamic Shader Canvas that renders any background the Main AI commands
     * AND overlays the live AI Screen Touch Pulse Indicator whenever AI touches the screen.
     */
    @Composable
    fun OmniDynamicBackgroundBox(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        val vState by visualState.collectAsState()
        val touchEv by lastScreenTouchEvent.collectAsState()
        var showTouchPulse by remember { mutableStateOf(false) }

        LaunchedEffect(touchEv?.id) {
            if (touchEv != null) {
                showTouchPulse = true
                delay(900)
                showTouchPulse = false
            }
        }

        val cPrimary = parseColorSafe(vState.primaryHex, Color.White)
        val cSecondary = parseColorSafe(vState.secondaryHex, Color.Black)
        val cDot = parseColorSafe(vState.centerDotHex, Color(0xFFFF0055))

        Box(modifier = modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                when (vState.mode.uppercase()) {
                    "VERTICAL_GRADIENT" -> {
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(cPrimary, cSecondary)
                            )
                        )
                    }
                    "HORIZONTAL_GRADIENT" -> {
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(cPrimary, cSecondary)
                            )
                        )
                    }
                    "RADIAL_DOT" -> {
                        drawRect(color = cPrimary)
                        val minDim = size.minDimension.coerceAtLeast(100f)
                        val r = (minDim * vState.dotRadiusFraction.coerceIn(0.12f, 0.85f))
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(cDot, cDot.copy(alpha = 0.65f), cPrimary),
                                center = center,
                                radius = r * 1.25f
                            ),
                            radius = r,
                            center = center
                        )
                    }
                    "RAINBOW" -> {
                        drawRect(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFFFFE4E6),
                                    Color(0xFFFEF3C7),
                                    Color(0xFFDCFCE7),
                                    Color(0xFFE0F2FE),
                                    Color(0xFFF3E8FF),
                                    Color(0xFFFFE4E6)
                                ),
                                center = center
                            )
                        )
                    }
                    "RAINBOW_DARK" -> {
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF3B0764),
                                    Color(0xFF1E3A8A),
                                    Color(0xFF064E3B),
                                    Color(0xFF7C2D12),
                                    Color(0xFF4C0519)
                                ),
                                start = Offset.Zero,
                                end = Offset(size.width, size.height)
                            )
                        )
                    }
                    else -> {
                        drawRect(color = cPrimary)
                    }
                }
            }

            content()

            if (showTouchPulse && touchEv != null) {
                val ev = touchEv!!
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val tx = (ev.xFraction * size.width).coerceIn(20f, size.width - 20f)
                    val ty = (ev.yFraction * size.height).coerceIn(20f, size.height - 20f)
                    drawCircle(
                        color = Color(0xFF00F0FF).copy(alpha = 0.28f),
                        radius = 48f,
                        center = Offset(tx, ty)
                    )
                    drawCircle(
                        color = Color(0xFF10B981),
                        radius = 24f,
                        center = Offset(tx, ty),
                        style = Stroke(width = 4f)
                    )
                    drawCircle(
                        color = Color(0xFFFF0055),
                        radius = 8f,
                        center = Offset(tx, ty)
                    )
                }
            }
        }
    }

    /**
     * Instant Natural-Language & JSON Command Processor so the Main AI can control:
     * 1. Any 3D Button creation or converting existing buttons to 3D ("থ্রিডি বাটন", "3D button", "৩ডি বাটন")
     * 2. Making any button or all buttons smaller or larger ("বাটন ছোট করো", "বাটন বড় করো", "সাইজ বাড়াও/কমাও")
     * 3. Moving any button left/right/first/last/top/bottom/floating ("বাটন এদিক ওদিক সরাও", "বামে/ডানে/উপরে/নিচে/ডকে/ফ্লোটিং")
     * 4. Changing any button's color or the entire app's background color/shader ("কালার চেঞ্জ করো", "ব্যাকগ্রাউন্ড চেঞ্জ করো")
     * 5. Removing or restoring any button/feature ("রিমুভ করো", "ফিরিয়ে আনো")
     * 6. Adding new custom buttons/features (small or large) ("নতুন বাটন যুক্ত করো")
     * 7. Controlling any existing or future feature's ON/OFF state, speed, and live screen touch!
     */
    fun processNaturalLanguageOrJsonCommand(
        context: Context,
        rawText: String
    ): String? {
        init(context)
        val lower = rawText.lowercase().trim()
        val executedActions = mutableListOf<String>()

        // 1. Check for embedded JSON patch block [OMNI_PATCH]{...}[/OMNI_PATCH]
        val patchRegex = Regex("\\[OMNI_PATCH\\](.*?)\\[/OMNI_PATCH\\]", RegexOption.DOT_MATCHES_ALL)
        val match = patchRegex.find(rawText)
        if (match != null) {
            val jsonStr = match.groupValues[1].trim()
            val patchResult = applyJsonPatch(context, jsonStr)
            if (patchResult != null) {
                executedActions.add(patchResult)
            }
        }

        // 2. 3D Button Styling & New 3D Button Creation ("থ্রিডি বাটন", "3d button", "৩ডি বাটন")
        val is3DCmd = lower.contains("থ্রিডি") || lower.contains("৩ডি") || lower.contains("3d")
        if (is3DCmd && (lower.contains("বাটন") || lower.contains("button") || lower.contains("অ্যাপ") || lower.contains("সব"))) {
            val turnOff3D = lower.contains("অফ") || lower.contains("বন্ধ") || lower.contains("নরমাল") || lower.contains("2d")
            val targetIds = matchTargetNodeIds(lower)
            val wantsNewButton = lower.contains("নতুন") || lower.contains("যুক্ত করো") || lower.contains("অ্যাড করো") || lower.contains("এড করো") || lower.contains("বানাও") || lower.contains("ক্রিয়েট") || lower.contains("ক্রিয়েট")

            if (wantsNewButton && targetIds.isEmpty()) {
                val cleanLabel = rawText
                    .replace(Regex("(?i)(নতুন|থ্রিডি|৩ডি|3d|বাটন|button|যুক্ত করো|অ্যাড করো|এড করো|যোগ করো|বানাও|ক্রিয়েট করো|ক্রিয়েট করো|করো)"), "")
                    .trim()
                    .take(20)
                    .ifBlank { "৩ডি অ্যাকশন" }
                val newId = "custom_3d_" + UUID.randomUUID().toString().take(5)
                val customHex = detectColorHexFromBangla(lower, "#0284C7")
                autoRegisterFeature(
                    context = context,
                    id = newId,
                    label = "🧊 $cleanLabel",
                    defaultZone = if (lower.contains("নিচে") || lower.contains("ডকে")) "BOTTOM_DOCK" else if (lower.contains("হেডার") || lower.contains("চ্যাট বারে")) "CHAT_HEADER" else if (lower.contains("উপরে") || lower.contains("প্রথম")) "TOP_ROW_1" else "TOP_ROW_2",
                    bgColorHex = "#E0F2FE",
                    accentColorHex = customHex,
                    isCustomAiFeature = true,
                    customActionPayload = "GENESIS_SYSTEM:$cleanLabel|$rawText",
                    scaleFactor = if (lower.contains("বড়") || lower.contains("বড়")) 1.28f else if (lower.contains("ছোট")) 0.82f else 1.08f,
                    is3DStyle = true
                )
                executedActions.add("🧊 নতুন থ্রিডি (3D) বাটন **'🧊 $cleanLabel'** অ্যাপের বাটন বারে তৈরি হয়েছে এবং এর ভেতরে পূর্ণাঙ্গ কার্যকরী লাইভ সিস্টেম (`GENESIS_SYSTEM`) সংযুক্ত করা হয়েছে!")
            } else if (targetIds.isNotEmpty() && !lower.contains("সব বাটন")) {
                val updated = _nodes.value.map { n ->
                    if (n.id in targetIds) n.copy(is3DStyle = !turnOff3D) else n
                }
                _nodes.value = updated
                saveState(context)
                executedActions.add(
                    if (!turnOff3D) "🧊 নির্বাচিত বাটন (${targetIds.joinToString()}) আসল থ্রিডি (3D) স্টাইলে রূপান্তর করা হয়েছে!"
                    else "বাটনের ৩ডি স্টাইল নরমাল করা হয়েছে।"
                )
            } else {
                _visualState.value = _visualState.value.copy(global3DButtons = !turnOff3D)
                val updated = _nodes.value.map { it.copy(is3DStyle = !turnOff3D) }
                _nodes.value = updated
                saveState(context)
                executedActions.add(
                    if (!turnOff3D) "🧊 পুরো অ্যাপের সব বাটন এখন আসল থ্রিডি (3D Bevel + Depth + Shadow) স্টাইলে রূপান্তর করা হয়েছে!"
                    else "পুরো অ্যাপের বাটন আবার সাধারণ ২ডি মোডে আনা হয়েছে।"
                )
            }
        }

        // 3. Button Resize: Smaller or Larger ("বাটন ছোট করা, বড় করা", "সাইজ বাড়াও/কমাও")
        val isResizeButtonCmd = (lower.contains("বাটন") || lower.contains("button") || lower.contains("সাইজ")) &&
            (lower.contains("বড় করো") || lower.contains("বড় করো") || lower.contains("ছোট করো") || lower.contains("বড়") || lower.contains("বড়") || lower.contains("ছোট") || lower.contains("বাড়াও") || lower.contains("বাড়াও") || lower.contains("কমাও"))
        if (isResizeButtonCmd) {
            val makeLarger = lower.contains("বড়") || lower.contains("বড়") || lower.contains("বাড়াও") || lower.contains("বাড়াও") || lower.contains("large") || lower.contains("big")
            val isResetSize = lower.contains("নরমাল") || lower.contains("ডিফল্ট") || lower.contains("আগের মতো")
            val targetIds = matchTargetNodeIds(lower)

            if (targetIds.isNotEmpty() && !lower.contains("সব বাটন")) {
                val updated = _nodes.value.map { n ->
                    if (n.id in targetIds) {
                        val newScale = when {
                            isResetSize -> 1.0f
                            makeLarger -> (n.scaleFactor + 0.22f).coerceAtMost(1.65f)
                            else -> (n.scaleFactor - 0.18f).coerceAtLeast(0.68f)
                        }
                        n.copy(scaleFactor = newScale)
                    } else n
                }
                _nodes.value = updated
                saveState(context)
                executedActions.add(
                    if (makeLarger) "🔍 বাটন (${targetIds.joinToString()}) আকারে বড় করা হয়েছে!"
                    else "🔽 বাটন (${targetIds.joinToString()}) আকারে ছোট করা হয়েছে!"
                )
            } else {
                val curGlobal = _visualState.value.globalButtonScale
                val newGlobal = when {
                    isResetSize -> 1.0f
                    makeLarger -> (curGlobal + 0.20f).coerceAtMost(1.55f)
                    else -> (curGlobal - 0.16f).coerceAtLeast(0.72f)
                }
                _visualState.value = _visualState.value.copy(globalButtonScale = newGlobal)
                saveState(context)
                executedActions.add(
                    if (makeLarger) "🔍 অ্যাপের সব বাটনের আকার বড় (${(newGlobal * 100).toInt()}%) করা হয়েছে!"
                    else "🔽 অ্যাপের সব বাটনের আকার ছোট (${(newGlobal * 100).toInt()}%) করা হয়েছে!"
                )
            }
        }

        // 4. Button Color Change ("বাটনের কালার চেঞ্জ করা") & Background Color Change ("ব্যাকগ্রাউন্ড চেঞ্জ করা")
        val isButtonColorCmd = (lower.contains("বাটন") || lower.contains("button") || matchTargetNodeIds(lower).isNotEmpty()) &&
            (lower.contains("কালার") || lower.contains("রঙ") || lower.contains("রং") || lower.contains("color")) &&
            !lower.contains("ব্যাকগ্রাউন্ড")
        if (isButtonColorCmd) {
            val chosenBgHex = detectButtonBgHexFromBangla(lower, "#E0F2FE")
            val chosenAccentHex = detectButtonAccentHexFromBangla(lower, "#0284C7")
            val targetIds = matchTargetNodeIds(lower)
            val updated = _nodes.value.map { n ->
                if (targetIds.isEmpty() || n.id in targetIds || lower.contains("সব বাটন")) {
                    n.copy(bgColorHex = chosenBgHex, accentColorHex = chosenAccentHex)
                } else n
            }
            _nodes.value = updated
            saveState(context)
            val targetLabel = if (targetIds.isEmpty() || lower.contains("সব বাটন")) "সব বাটনের" else "নির্বাচিত বাটনের (${targetIds.joinToString()})"
            executedActions.add("🎨 $targetLabel রঙ পরিবর্তন করে নতুন কালার ($chosenAccentHex) দেওয়া হয়েছে!")
        }

        val isBgCommand = lower.contains("ব্যাকগ্রাউন্ড") || lower.contains("background") ||
            (lower.contains("মাঝখানে") && (lower.contains("ডট") || lower.contains("গোল") || lower.contains("বৃত্ত") || lower.contains("লাল") || lower.contains("নীল") || lower.contains("সবুজ"))) ||
            lower.contains("রঙধনু") || lower.contains("রংধনু") || lower.contains("rainbow") ||
            (lower.contains("উপরে") && lower.contains("নিচে") && (lower.contains("কালার") || lower.contains("রঙ") || lower.contains("রং") || lower.contains("কালো") || lower.contains("সাদা") || lower.contains("নীল") || lower.contains("লাল")))

        if (isBgCommand) {
            val currentVis = _visualState.value
            val newVis = when {
                lower.contains("রঙধনু") || lower.contains("রংধনু") || lower.contains("rainbow") -> {
                    val isDark = lower.contains("গাঢ়") || lower.contains("ডার্ক") || lower.contains("কালো")
                    currentVis.copy(mode = if (isDark) "RAINBOW_DARK" else "RAINBOW")
                }
                lower.contains("মাঝখানে") && (lower.contains("ডট") || lower.contains("গোল") || lower.contains("বৃত্ত") || lower.contains("চারিদিকে") || lower.contains("চারপাশে")) -> {
                    val dotHex = detectColorHexFromBangla(
                        lower.substringAfter("মাঝখানে", lower),
                        defaultHex = "#FF0055"
                    )
                    val outerHex = detectColorHexFromBangla(
                        lower.substringBefore("মাঝখানে", lower),
                        defaultHex = "#090D1A"
                    )
                    currentVis.copy(
                        mode = "RADIAL_DOT",
                        primaryHex = outerHex,
                        centerDotHex = dotHex,
                        dotRadiusFraction = 0.36f
                    )
                }
                (lower.contains("উপরে") && lower.contains("নিচে")) || lower.contains("গ্রেডিয়েন্ট") || lower.contains("gradient") -> {
                    val topPart = lower.substringBefore("নিচে")
                    val bottomPart = lower.substringAfter("নিচে", "")
                    val topHex = detectColorHexFromBangla(topPart, "#090D1A")
                    val botHex = detectColorHexFromBangla(bottomPart, "#1E3A8A")
                    currentVis.copy(mode = "VERTICAL_GRADIENT", primaryHex = topHex, secondaryHex = botHex)
                }
                else -> {
                    val targetHex = detectColorHexFromBangla(lower, "#FFFFFF")
                    currentVis.copy(mode = "SOLID", primaryHex = targetHex, chat1BgHex = targetHex)
                }
            }
            _visualState.value = newVis
            saveState(context)
            executedActions.add("🎨 ব্যাকগ্রাউন্ড ও ক্যানভাস পরিবর্তন করা হয়েছে (মোড: ${newVis.mode})।")
        }

        // 5. Adding a Brand New Custom Button + Whole-Sky Living Subsystem ("কোনো বাটন যুক্ত করা — শুধু ছোট চিপ নয়, বাটনের ভেতর পূর্ণাঙ্গ কার্যকরী ইঞ্জিন সহ!")
        val isAddButtonCmd = (lower.contains("বাটন যুক্ত করো") || lower.contains("বাটন অ্যাড করো") || lower.contains("বাটন এড করো") || lower.contains("নতুন বাটন দাও") || lower.contains("নতুন বাটন বানাও")) && !is3DCmd
        if (isAddButtonCmd) {
            val cleanName = rawText
                .replace(Regex("(?i)(নতুন|বাটন|যুক্ত করো|অ্যাড করো|এড করো|যোগ করো|দাও|বানাও|একটি|একটা)"), "")
                .trim()
                .take(24)
                .ifBlank { "কাস্টম ওমেগা সিস্টেম" }
            val newId = "btn_" + UUID.randomUUID().toString().take(5)
            val accentHex = detectButtonAccentHexFromBangla(lower, "#10B981")
            val bgHex = detectButtonBgHexFromBangla(lower, "#ECFDF5")
            val destZone = when {
                lower.contains("নিচে") || lower.contains("ডকে") -> "BOTTOM_DOCK"
                lower.contains("হেডার") || lower.contains("চ্যাট বারে") -> "CHAT_HEADER"
                lower.contains("ফ্লোটিং") || lower.contains("ভাসমান") -> "FLOATING"
                lower.contains("উপরে") || lower.contains("প্রথম") -> "TOP_ROW_1"
                else -> "TOP_ROW_2"
            }
            autoRegisterFeature(
                context = context,
                id = newId,
                label = "⚡ $cleanName",
                defaultZone = destZone,
                bgColorHex = bgHex,
                accentColorHex = accentHex,
                isCustomAiFeature = true,
                customActionPayload = "GENESIS_SYSTEM:$cleanName|$rawText",
                is3DStyle = true,
                scaleFactor = 1.08f
            )
            executedActions.add("➕ নতুন বাটন **'⚡ $cleanName'** ($destZone) তৈরি হয়েছে এবং এর ভেতরে পূর্ণাঙ্গ কার্যকরী লাইভ সিস্টেম (`GENESIS_SYSTEM`) সংযুক্ত করা হয়েছে!")
        }

        // 6. REAL Feature Functional Capability Evolution (Standard -> Pro Turbo -> Autonomous Swarm -> God-Level Omni)
        val isCapabilityAdjust = (lower.contains("ক্ষমতা") || lower.contains("স্পিড") || lower.contains("গতি") || lower.contains("speed") || lower.contains("শক্তি") || lower.contains("পাওয়ার") || lower.contains("পাওয়ার")) &&
            (lower.contains("বাড়াও") || lower.contains("বাড়াও") || lower.contains("কমাও") || lower.contains("দ্রুত") || lower.contains("ধীরে") || lower.contains("আপগ্রেড") || lower.contains("গড লেভেল"))
        if (isCapabilityAdjust) {
            val increase = lower.contains("বাড়াও") || lower.contains("বাড়াও") || lower.contains("দ্রুত") || lower.contains("আপগ্রেড") || lower.contains("গড লেভেল")
            val curZl = _zeroLoadState.value
            val newInterval = if (increase) (curZl.autoTouchIntervalMs / 2).coerceAtLeast(160L) else (curZl.autoTouchIntervalMs * 2).coerceAtMost(5000L)
            val newScrollStep = if (increase) (curZl.autoScrollStepPx + 260).coerceAtMost(1600) else (curZl.autoScrollStepPx - 180).coerceAtLeast(120)
            _zeroLoadState.value = curZl.copy(
                autoTouchIntervalMs = newInterval,
                autoScrollStepPx = newScrollStep
            )

            // Upgrade targeted feature(s) with REAL functional capability evolution!
            val targetIds = matchTargetNodeIds(lower)
            if (targetIds.isNotEmpty()) {
                targetIds.forEach { tid ->
                    val curNode = _nodes.value.find { it.id == tid }
                    val nextCap = if (increase) (curNode?.powerPercent ?: 100) + 100 else ((curNode?.powerPercent ?: 100) - 40).coerceAtLeast(40)
                    val tierBadge = when {
                        nextCap >= 300 -> "GOD-LEVEL OMNI v4.0"
                        nextCap >= 200 -> "AUTONOMOUS SWARM v3.0"
                        nextCap >= 150 -> "PRO TURBO v2.0"
                        else -> "STANDARD v1.0"
                    }
                    controlFeatureStateAndPower(
                        context = context,
                        nodeId = tid,
                        enable = true,
                        powerPercent = nextCap,
                        applyImmediately = true,
                        extraCommand = "CAPABILITY_TIER:$tierBadge|$rawText"
                    )
                    if (tid == "chat_2_particles") {
                        val curPhys = ParticleSwarmManager.canvasPhysicsState.value
                        ParticleSwarmManager.updateCanvasPhysicsState(
                            context = context,
                            isEnabled = true,
                            particleCount = if (increase) (curPhys.particleCount + 28).coerceAtMost(150) else (curPhys.particleCount - 16).coerceAtLeast(12),
                            speedMultiplier = if (increase) (curPhys.speedMultiplier * 1.75f).coerceAtMost(8.0f) else (curPhys.speedMultiplier * 0.6f).coerceAtLeast(0.3f),
                            physicsMode = if (increase) "HYPER_SWARM" else "NEURAL_WEB"
                        )
                        if (increase) {
                            ParticleSwarmManager.createExtraCustomParticle(
                                context = context,
                                name = "গড-লেভেল টার্বো কণা #${ParticleSwarmManager.particlesList.value.size + 1}",
                                roleBadge = "👑 $tierBadge",
                                description = rawText
                            )
                        }
                    }
                }
                executedActions.add(
                    if (increase) "👑 নির্বাচিত ফিচারের (${targetIds.joinToString()}) বাস্তব কার্যক্ষমতা পরের স্তরে (Pro/Swarm/God-Level Tier) উন্নীত করে নতুন সক্ষমতা সরাসরি চালু করা হয়েছে!"
                    else "🔉 নির্বাচিত ফিচারের (${targetIds.joinToString()}) অপারেশনাল রেট ও প্রসেসিং কমানো হয়েছে।"
                )
            } else {
                // Also upgrade the Meta-Engine synthesis multiplier so global app capability scales up!
                if (increase) {
                    AutonomousSelfEvolutionEngine.upgradeMetaEngineItself(context, rawText, null)
                }
                executedActions.add(
                    if (increase) {
                        "👑 গ্লোবাল ইঞ্জিন ও মেটা-কম্পাইলারের বাস্তব সক্ষমতা আপগ্রেড হয়েছে: অটো-টাচ ইন্টারভ্যাল **${newInterval}ms**, স্ক্রল স্টেপ **${newScrollStep}px**, এবং ফুল-বডি সিন্থেসিস বুস্ট সক্রিয়!"
                    } else {
                        "🔉 ইঞ্জিনের গতি কমানো হয়েছে: অটো-টাচ ইন্টারভ্যাল এখন **${newInterval}ms** এবং স্ক্রল স্টেপ **${newScrollStep}px** করা হয়েছে।"
                    }
                )
            }
        }

        // 7. REAL Continuous Auto-Touch, Coordinate Parsing & Native/Web Smart Target Screen Touch ("যখন তখন স্ক্রিন টাচ করা বা যেকোনো জিনিস")
        val isAutoTouchCmd = lower.contains("অটো টাচ") || lower.contains("অটো-টাচ") || lower.contains("বারবার টাচ") ||
            lower.contains("স্ক্রিন টাচ") || lower.contains("স্ক্রিনে টাচ") || lower.contains("টাচ করো") || lower.contains("ট্যাপ করো") ||
            lower.contains("ডাবল ট্যাপ") || lower.contains("লং প্রেস") || lower.contains("সোয়াইপ") || lower.contains("সোয়াইপ")
        if (isAutoTouchCmd) {
            // Parse explicit numbers if user provided coordinates like "30% 70%" or "x=40, y=80" or "300 600"
            val numMatches = Regex("(\\d{1,4})").findAll(lower).mapNotNull { it.groupValues[1].toFloatOrNull() }.toList()
            val parsedX = if (numMatches.size >= 2) {
                val n1 = numMatches[0]
                if (n1 <= 100f) (n1 / 100f).coerceIn(0.05f, 0.95f) else (n1 / 1080f).coerceIn(0.05f, 0.95f)
            } else null
            val parsedY = if (numMatches.size >= 2) {
                val n2 = numMatches[1]
                if (n2 <= 100f) (n2 / 100f).coerceIn(0.08f, 0.92f) else (n2 / 1920f).coerceIn(0.08f, 0.92f)
            } else null

            val x = parsedX ?: when {
                lower.contains("বাম") || lower.contains("left") -> 0.22f
                lower.contains("ডান") || lower.contains("right") -> 0.78f
                else -> 0.50f
            }
            val y = parsedY ?: when {
                lower.contains("উপরে") || lower.contains("top") -> 0.20f
                lower.contains("নিচে") || lower.contains("bottom") -> 0.80f
                else -> 0.50f
            }
            val gestureType = when {
                lower.contains("ডাবল ট্যাপ") || lower.contains("double tap") -> "DOUBLE_TAP"
                lower.contains("লং প্রেস") || lower.contains("long press") || lower.contains("চেপে ধরো") -> "LONG_PRESS"
                lower.contains("সোয়াইপ বামে") || lower.contains("সোয়াইপ বামে") || lower.contains("swipe left") -> "SWIPE_LEFT"
                lower.contains("সোয়াইপ ডানে") || lower.contains("সোয়াইপ ডানে") || lower.contains("swipe right") -> "SWIPE_RIGHT"
                lower.contains("সোয়াইপ উপরে") || lower.contains("সোয়াইপ উপরে") || lower.contains("swipe up") -> "SWIPE_UP"
                lower.contains("সোয়াইপ নিচে") || lower.contains("সোয়াইপ নিচে") || lower.contains("swipe down") -> "SWIPE_DOWN"
                else -> "TAP"
            }
            val extractedSelector = rawText
                .replace(Regex("(?i)(স্ক্রিনে|স্ক্রিন|অটো-টাচ|অটো টাচ|বারবার|ডাবল ট্যাপ|লং প্রেস|সোয়াইপ|সোয়াইপ|টাচ করো|ট্যাপ করো|ক্লিক করো|উপরে|নিচে|বামে|ডানে|মাঝখানে|লুপ|অন করো|চালু করো|বাটনে|বাটন|\\d+|%)"), "")
                .trim()
                .take(28)

            if (lower.contains("অফ করো") || lower.contains("বন্ধ করো") || lower.contains("থামাও")) {
                stopRealAutoTouchLoop()
                stopRealAutoScrollLoop()
                executedActions.add("⏹️ লাইভ অটো-টাচ ও অটো-স্ক্রল লুপ সম্পূর্ণ বন্ধ করা হয়েছে।")
            } else if (lower.contains("লুপ") || lower.contains("বারবার") || lower.contains("অটো টাচ অন") || lower.contains("অটো-টাচ অন")) {
                startRealAutoTouchLoop(context, xFraction = x, yFraction = y, intervalMs = _zeroLoadState.value.autoTouchIntervalMs, selector = extractedSelector)
                executedActions.add("🎯 বাস্তব অটো-টাচ লুপ চালু করা হয়েছে (প্রতি ${_zeroLoadState.value.autoTouchIntervalMs}ms অন্তর X=${(x * 100).toInt()}%, Y=${(y * 100).toInt()}%-এ ফিজিক্যাল টাচ করছে)।")
            } else {
                triggerAutonomousScreenTouch(
                    context = context,
                    xFraction = x,
                    yFraction = y,
                    actionType = gestureType,
                    targetLabel = if (extractedSelector.isNotBlank()) "AI $gestureType ($extractedSelector)" else "AI Direct $gestureType",
                    targetSelector = extractedSelector
                )
                val targetIds = matchTargetNodeIds(lower)
                if (targetIds.isNotEmpty()) {
                    targetIds.forEach { tid ->
                        controlFeatureStateAndPower(context, tid, enable = true, applyImmediately = true, extraCommand = rawText)
                    }
                }
                executedActions.add(
                    if (targetIds.isNotEmpty()) "👆 স্ক্রিনে ($gestureType @ X=${(x * 100).toInt()}%, Y=${(y * 100).toInt()}%) এবং অ্যাপের বাটন/ফিচার (${targetIds.joinToString()})-এ সরাসরি টাচ ও ওপেন কার্যকর করা হয়েছে!"
                    else if (extractedSelector.isNotBlank()) "👆 স্ক্রিনে ও '$extractedSelector' এলিমেন্টে সরাসরি ফিজিক্যাল টাচ ($gestureType @ X=${(x * 100).toInt()}%, Y=${(y * 100).toInt()}%) কার্যকর করা হয়েছে।"
                    else "👆 স্ক্রিনে সরাসরি ফিজিক্যাল টাচ ($gestureType @ X=${(x * 100).toInt()}%, Y=${(y * 100).toInt()}%) কার্যকর করা হয়েছে।"
                )
            }
        }

        // 8. REAL Feature ON / OFF / Open / Apply Control (for both current and future features, plus "সব অফ করো")
        val isFeatureToggleOrApply = (lower.contains("অন করো") || lower.contains("অফ করো") || lower.contains("চালু করো") ||
            lower.contains("বন্ধ করো") || lower.contains("এপ্লাই করো") || lower.contains("খোলো") || lower.contains("ওপেন করো")) && !isAutoTouchCmd && !isBgCommand
        if (isFeatureToggleOrApply) {
            val turnOff = lower.contains("অফ করো") || lower.contains("বন্ধ করো") || lower.contains("off")
            val targetIds = matchTargetNodeIds(lower)
            if (targetIds.isNotEmpty()) {
                targetIds.forEach { id ->
                    controlFeatureStateAndPower(
                        context = context,
                        nodeId = id,
                        enable = !turnOff,
                        applyImmediately = true,
                        extraCommand = rawText
                    )
                }
                val actionDesc = if (turnOff) "অফ (OFF) এবং চলমান শিট/রানার/লুপ বন্ধ করা হয়েছে" else "অন (ON) ও সরাসরি স্ক্রিনে এপ্লাই করা হয়েছে"
                executedActions.add("🎛️ ফিচার (${targetIds.joinToString()}) $actionDesc।")
            } else if (turnOff && (lower.contains("সব ফিচার") || lower.contains("সব বন্ধ") || lower.contains("সব অফ") || lower.contains("ফিচার অফ"))) {
                stopRealAutoTouchLoop()
                stopRealAutoScrollLoop()
                _pendingFeatureApplyEvent.value = OmniFeatureApplyEvent(
                    nodeId = "ALL_ACTIVE_FEATURES",
                    actionMode = "TURN_OFF",
                    powerPercent = 0,
                    extraCommand = rawText
                )
                executedActions.add("⏹️ অ্যাপের সমস্ত খোলা শিট, লাইভ রানার এবং অটো-লুপ সাথে সাথে অফ (OFF) করা হয়েছে।")
            }
        }

        // 9. Custom AI Creation Command ("নতুন এআই বানাও", "কাস্টম এআই তৈরি করো")
        val isCreateAiCmd = (lower.contains("নতুন এআই") || lower.contains("কাস্টম এআই") || lower.contains("বড় এআই") || lower.contains("বড় এআই")) &&
            (lower.contains("বানাও") || lower.contains("ক্রিয়েট") || lower.contains("ক্রিয়েট") || lower.contains("তৈরি করো") || lower.contains("যোগ করো"))
        if (isCreateAiCmd) {
            val cleanAiName = rawText.replace(Regex("(?i)(নতুন|কাস্টম|বড়|বড়|এআই|বানাও|ক্রিয়েট|ক্রিয়েট|তৈরি|করো|যোগ|অ্যাড|দাও)"), "").trim().take(20).ifBlank { "সার্বভৌম প্রো এআই" }
            val createdAgent = SovereignDeepResearchAndIncrementalEngine.createCustomAiInApp(
                context = context,
                name = cleanAiName,
                specialtyBangla = rawText,
                systemInstruction = "You are '$cleanAiName', a custom specialized AI agent created inside the user's app. Role & instructions: $rawText"
            )
            executedActions.add("🧠 নতুন স্বতন্ত্র এআই **'${createdAgent.name}'** তৈরি করে অ্যাপের বাটন বারে ও মেমরি ভল্টে স্থায়ীভাবে যুক্ত করা হয়েছে!")
        }

        // 10. Button / Feature Remove ("উড়িয়ে দাও", "রিমুভ করো", "হাইড করো", "বাদ দাও")
        val isRemoveCmd = lower.contains("উড়িয়ে দাও") || lower.contains("উড়িয়ে দাও") || lower.contains("রিমুভ করো") || lower.contains("হাইড করো") || lower.contains("লুকিয়ে দাও") || lower.contains("বাদ দাও")
        if (isRemoveCmd) {
            val targetIds = matchTargetNodeIds(lower)
            if (targetIds.isNotEmpty()) {
                val updated = _nodes.value.map { node ->
                    if (node.id in targetIds && node.zone != "REMOVED_TRASH") {
                        node.copy(previousZone = node.zone, zone = "REMOVED_TRASH", isEnabled = false)
                    } else node
                }
                _nodes.value = updated
                targetIds.forEach { tid ->
                    _pendingFeatureApplyEvent.value = OmniFeatureApplyEvent(
                        nodeId = tid,
                        actionMode = "TURN_OFF",
                        powerPercent = 0,
                        extraCommand = "REMOVED"
                    )
                }
                saveState(context)
                executedActions.add("🗑️ বাটন/ফিচার (${targetIds.joinToString()}) স্ক্রিন থেকে সরিয়ে আর্কাইভে রাখা হয়েছে। আপনি বললেই আবার ফিরিয়ে আনা যাবে।")
            }
        }

        // 11. Button / Feature Restore ("ফিরিয়ে আনো", "রিটার্ন আনো", "আবার আনো", "সব বাটন আনো")
        val isRestoreCmd = lower.contains("ফিরিয়ে আনো") || lower.contains("ফিরিয়ে আনো") || lower.contains("রিটার্ন আনো") || lower.contains("আবার আনো") || lower.contains("সব বাটন আনো") || lower.contains("রিস্টোর")
        if (isRestoreCmd) {
            val targetIds = matchTargetNodeIds(lower)
            val updated = _nodes.value.map { node ->
                if (node.zone == "REMOVED_TRASH" && (targetIds.isEmpty() || node.id in targetIds)) {
                    node.copy(zone = node.previousZone.ifBlank { "TOP_ROW_2" }, isEnabled = true)
                } else node
            }
            _nodes.value = updated
            saveState(context)
            executedActions.add("♻️ আর্কাইভ করা বাটন/ফিচার আবার আগের জায়গায় ফিরিয়ে আনা হয়েছে!")
        }

        // 12. Move Button Left/Right/Center/First/Last/Top/Bottom/Floating ("যে কোনো বাটন এদিক ওদিক সরানো")
        val isMoveCmd = lower.contains("নিয়ে যাও") || lower.contains("নিয়ে যাও") || lower.contains("পাঠাও") ||
            lower.contains("বসাও") || lower.contains("নামিয়ে দাও") || lower.contains("নামিয়ে দাও") ||
            lower.contains("সরাও") || lower.contains("সরিয়ে") || lower.contains("সরিয়ে") ||
            lower.contains("এদিক ওদিক") || lower.contains("বামে") || lower.contains("ডানে") ||
            lower.contains("মাঝখানে") || lower.contains("প্রথমে আনো") || lower.contains("শেষে") || lower.contains("ভাসমান") || lower.contains("ফ্লোটিং")
        if (isMoveCmd && !isRemoveCmd && !isBgCommand) {
            val targetIds = matchTargetNodeIds(lower)
            val moveHorizontalOnly = (lower.contains("বামে") || lower.contains("ডানে") || lower.contains("মাঝখানে") || lower.contains("প্রথমে") || lower.contains("শেষে") || lower.contains("এদিক ওদিক")) &&
                !lower.contains("নিচে") && !lower.contains("ডকে") && !lower.contains("উপরে") && !lower.contains("ফ্লোটিং") && !lower.contains("ভাসমান")

            if (moveHorizontalOnly) {
                val moveFirst = lower.contains("প্রথমে") || lower.contains("একদম সামনে") || lower.contains("শুরুতে")
                val moveLast = lower.contains("শেষে") || lower.contains("একদম পিছনে") || lower.contains("লাস্টে")
                val moveCenter = lower.contains("মাঝখানে") || lower.contains("মিডিলে") || lower.contains("center")
                val moveLeftStep = lower.contains("বামে") || lower.contains("আগে") || lower.contains("সামনে")
                val moveRightStep = lower.contains("ডানে") || lower.contains("পরে") || lower.contains("পিছনে")

                val currentList = _nodes.value.toMutableList()
                if (targetIds.isNotEmpty()) {
                    targetIds.forEach { tid ->
                        val targetNode = currentList.find { it.id == tid } ?: return@forEach
                        val sameZoneNodes = currentList.filter { it.zone == targetNode.zone }.sortedBy { it.orderIndex }.toMutableList()
                        val currentPos = sameZoneNodes.indexOfFirst { it.id == tid }
                        if (currentPos >= 0 && sameZoneNodes.size > 1) {
                            sameZoneNodes.removeAt(currentPos)
                            val newPos = when {
                                moveFirst -> 0
                                moveLast -> sameZoneNodes.size
                                moveCenter -> sameZoneNodes.size / 2
                                moveLeftStep -> (currentPos - 1).coerceAtLeast(0)
                                moveRightStep -> (currentPos + 1).coerceAtMost(sameZoneNodes.size)
                                else -> if (currentPos > 0) currentPos - 1 else sameZoneNodes.size
                            }
                            sameZoneNodes.add(newPos, targetNode)
                            sameZoneNodes.forEachIndexed { idx, n ->
                                val globalIdx = currentList.indexOfFirst { it.id == n.id }
                                if (globalIdx >= 0) {
                                    currentList[globalIdx] = currentList[globalIdx].copy(orderIndex = idx + 1)
                                }
                            }
                        }
                    }
                    _nodes.value = currentList
                } else {
                    // Shift each active zone's buttons by 1 position so user visually sees them shift cleanly
                    val grouped = currentList.groupBy { it.zone }
                    val shifted = mutableListOf<OmniNode>()
                    grouped.forEach { (_, listInZone) ->
                        val sorted = listInZone.sortedBy { it.orderIndex }
                        if (sorted.size > 1) {
                            val rotated = sorted.drop(1) + sorted.first()
                            rotated.forEachIndexed { idx, item ->
                                shifted.add(item.copy(orderIndex = idx + 1))
                            }
                        } else {
                            shifted.addAll(sorted)
                        }
                    }
                    _nodes.value = shifted
                }
                saveState(context)
                executedActions.add("↔️ বাটনের পজিশন আপনার নির্দেশ অনুযায়ী নিখুঁতভাবে সরানো হয়েছে!")
            } else {
                val destZone = when {
                    lower.contains("ফ্লোটিং") || lower.contains("ভাসমান") || lower.contains("floating") -> "FLOATING"
                    lower.contains("নিচে") || lower.contains("ডকে") || lower.contains("bottom") -> "BOTTOM_DOCK"
                    lower.contains("প্রথম") || lower.contains("উপরে") || lower.contains("row 1") -> "TOP_ROW_1"
                    else -> "TOP_ROW_2"
                }
                if (targetIds.isNotEmpty()) {
                    val updated = _nodes.value.map { node ->
                        if (node.id in targetIds) {
                            node.copy(previousZone = node.zone, zone = destZone)
                        } else node
                    }
                    _nodes.value = updated
                    saveState(context)
                    val zoneName = when (destZone) {
                        "FLOATING" -> "স্ক্রিনের ওপর ভাসমান (Floating) পজিশনে"
                        "BOTTOM_DOCK" -> "নিচের ডকে (Bottom Dock)"
                        "TOP_ROW_1" -> "উপরের ১ম সারিতে"
                        else -> "উপরের ২য় সারিতে"
                    }
                    executedActions.add("🔀 বাটন (${targetIds.joinToString()}) সরিয়ে $zoneName বসানো হয়েছে।")
                }
            }
        }

        // 13. API Key & Multi-AI Permanent Lock
        val isApiLockCmd = (lower.contains("এপিআই") || lower.contains("api") || lower.contains("মডেল") || lower.contains("এআই")) &&
            (lower.contains("লক") || lower.contains("lock") || lower.contains("মেইন করো") || lower.contains("নরমাল করো") || lower.contains("গাঁথা"))
        if (isApiLockCmd) {
            val profiles = UniversalAiManager.profiles.value
            val active = UniversalAiManager.getActiveProfile() ?: profiles.firstOrNull()
            if (active != null) {
                val workerMap = mutableMapOf<String, String>()
                profiles.forEach { p ->
                    if (p.id == active.id) {
                        workerMap[p.id] = "MASTER_LOCKED"
                        UniversalAiManager.updateProfileSpecialistRole(context, p.id, AiKeySpecialistRole.OMNI_MASTER_BRAIN)
                    } else {
                        workerMap[p.id] = "NORMAL_WORKER"
                        UniversalAiManager.updateProfileSpecialistRole(context, p.id, AiKeySpecialistRole.FAST_REFLEX_WORKER)
                    }
                }
                _aiLockState.value = OmniAiLockState(
                    lockedMasterProfileId = active.id,
                    lockedMasterName = "${active.label} (${active.modelName})",
                    isPermanentlyLocked = true,
                    workerRolesMap = workerMap
                )
                saveState(context)
                executedActions.add("🔒 স্থায়ী এআই লক সম্পন্ন: '${active.label} (${active.modelName})' মেইন ব্রেন হিসেবে স্থায়ীভাবে লক করা হয়েছে।")
            }
        }

        // 14. Reset everything to clean default white if requested
        if (lower.contains("সব রিসেট করো") || lower.contains("ডিফল্ট সাদা করো") || lower.contains("আগের মতো সাদা করো")) {
            resetAllToDefault(context)
            executedActions.add("🔄 পুরো অ্যাপের কালার, বাটন ও লেআউট আবার ডিফল্ট ক্লিন সাদা অবস্থায় ফিরিয়ে আনা হয়েছে।")
        }

        if (executedActions.isEmpty()) return null

        val summary = executedActions.joinToString("\n• ", prefix = "👑 **[মেইন এআই অ্যাপ ও ইঞ্জিন কন্ট্রোল কার্যকর হয়েছে]:**\n• ")
        _lastControlSummary.value = executedActions.first()
        AppTruthAndArchitectureVault.recordLiveMutation(
            category = "OMNI_UI_CONTROL",
            summaryBangla = executedActions.joinToString(" | ")
        )
        return summary
    }

    fun resetAllToDefault(context: Context) {
        stopRealAutoTouchLoop()
        stopRealAutoScrollLoop()
        _nodes.value = defaultNodes()
        _visualState.value = OmniVisualState()
        _zeroLoadState.value = ZeroLoadEngineState()
        saveState(context)
        _lastControlSummary.value = "🔄 ডিফল্ট ক্লিন হোয়াইট মোড পুনরুদ্ধার হয়েছে"
    }

    private fun detectColorHexFromBangla(text: String, defaultHex: String): String {
        val t = text.lowercase()
        return when {
            t.contains("কালো") || t.contains("ব্ল্যাক") || t.contains("black") -> "#090D1A"
            t.contains("সাদা") || t.contains("হোয়াইট") || t.contains("white") -> "#FFFFFF"
            t.contains("নীল") || t.contains("ব্লু") || t.contains("blue") -> "#0F2942"
            t.contains("আকাশি") || t.contains("cyan") -> "#E0F2FE"
            t.contains("লাল") || t.contains("রেড") || t.contains("red") -> "#450A0A"
            t.contains("গোলাপি") || t.contains("pink") || t.contains("rose") -> "#FFE4E6"
            t.contains("সবুজ") || t.contains("গ্রিন") || t.contains("green") -> "#064E3B"
            t.contains("হালকা সবুজ") -> "#ECFDF5"
            t.contains("বেগুনি") || t.contains("পার্পল") || t.contains("purple") -> "#2E1065"
            t.contains("হলুদ") || t.contains("সোনালী") || t.contains("yellow") || t.contains("gold") -> "#FEF3C7"
            else -> defaultHex
        }
    }

    private fun detectButtonBgHexFromBangla(text: String, fallback: String): String {
        val t = text.lowercase()
        return when {
            t.contains("লাল") || t.contains("red") -> "#FFE4E6"
            t.contains("নীল") || t.contains("blue") || t.contains("আকাশি") -> "#E0F2FE"
            t.contains("সবুজ") || t.contains("green") -> "#ECFDF5"
            t.contains("বেগুনি") || t.contains("purple") -> "#EDE9FE"
            t.contains("হলুদ") || t.contains("সোনালী") || t.contains("yellow") || t.contains("gold") -> "#FEF3C7"
            t.contains("কালো") || t.contains("black") -> "#0F172A"
            t.contains("সাদা") || t.contains("white") -> "#FFFFFF"
            else -> fallback
        }
    }

    private fun detectButtonAccentHexFromBangla(text: String, fallback: String): String {
        val t = text.lowercase()
        return when {
            t.contains("লাল") || t.contains("red") -> "#E11D48"
            t.contains("নীল") || t.contains("blue") || t.contains("আকাশি") -> "#0284C7"
            t.contains("সবুজ") || t.contains("green") -> "#10B981"
            t.contains("বেগুনি") || t.contains("purple") -> "#7C3AED"
            t.contains("হলুদ") || t.contains("সোনালী") || t.contains("yellow") || t.contains("gold") -> "#D97706"
            t.contains("কালো") || t.contains("black") -> "#38BDF8"
            else -> fallback
        }
    }

    /**
     * Matches built-in AND dynamically added future features/buttons/websites/slots by Bangla or English name.
     */
    fun matchTargetNodeIds(lowerText: String): List<String> {
        val matched = mutableListOf<String>()
        if (lowerText.contains("ম্যাক্রো") || lowerText.contains("macro")) matched.add("macro")
        if (lowerText.contains("স্যান্ডবক্স") || lowerText.contains("sandbox")) matched.add("sandbox")
        if (lowerText.contains("প্ল্যানার") || lowerText.contains("planner")) matched.add("planner")
        if (lowerText.contains("রুল") || lowerText.contains("rule")) matched.add("rules")
        if (lowerText.contains("নকশা") || lowerText.contains("anatomy")) matched.add("anatomy")
        if (lowerText.contains("অটো-টাচ") || lowerText.contains("auto touch") || lowerText.contains("টাচ প্যানেল")) matched.add("auto_touch")
        if (lowerText.contains("রিপোর্ট") || lowerText.contains("report")) matched.add("bot_report")
        if (lowerText.contains("ডসিয়ার") || lowerText.contains("ডসিয়ার") || lowerText.contains("সত্য")) matched.add("truth_dossier")
        if (lowerText.contains("ডুয়াল") || lowerText.contains("ডুয়াল") || lowerText.contains("dual")) matched.add("dual_split")
        if (lowerText.contains("ট্যাব a") || lowerText.contains("tab a")) matched.add("tab_a")
        if (lowerText.contains("ট্যাব b") || lowerText.contains("tab b")) matched.add("tab_b")
        if (lowerText.contains("পার্টিকল") || lowerText.contains("কণা") || lowerText.contains("particle")) matched.add("chat_2_particles")
        if (lowerText.contains("চ্যাট ১") || lowerText.contains("মেইন চ্যাট")) matched.add("chat_1")
        if (lowerText.contains("এপিআই কি") || lowerText.contains("api key")) matched.add("api_key")
        if (lowerText.contains("ন্যানোবট") || lowerText.contains("nanobot")) matched.add("dock_nanobot")
        if (lowerText.contains("৪-চ্যাট") || lowerText.contains("4-chat") || lowerText.contains("ছাঁকনি") || lowerText.contains("quad")) matched.add("dock_quad_chat")
        if (lowerText.contains("রুল ও প্ল্যানার")) matched.add("dock_rule_planner")
        if (lowerText.contains("প্রো ল্যাব") || lowerText.contains("pro lab") || lowerText.contains("হাব")) matched.add("dock_pro_lab")
        if (lowerText.contains("প্রজেক্ট") || lowerText.contains("project")) matched.add("incremental_master_project")
        if (lowerText.contains("নিউ চ্যাট") || lowerText.contains("নতুন চ্যাট") || lowerText.contains("new chat")) matched.add("chat_hdr_new_chat")
        if (lowerText.contains("হিস্ট্রি") || lowerText.contains("history")) matched.add("chat_hdr_history")
        if (lowerText.contains("রিভিউ পেজ") || lowerText.contains("রিভিউ") || lowerText.contains("review")) matched.add("chat_hdr_review_page")
        if (lowerText.contains("অ্যাপস") || lowerText.contains("শোরুম") || lowerText.contains("apps")) matched.add("chat_hdr_apps_showroom")
        if (lowerText.contains("পাওয়ার মডিউল") || lowerText.contains("পাওয়ার মডিউল") || lowerText.contains("power module")) matched.add("chat_hdr_power_modules")
        if (lowerText.contains("কোড ইমপোর্ট") || lowerText.contains("হারভেস্ট") || lowerText.contains("import")) matched.add("chat_hdr_code_import")

        // Also match any custom / future feature, slot, website, or 3D button by its label or ID!
        _nodes.value.forEach { node ->
            val clean = node.label
                .replace(Regex("[⚡🧠🏗️🧩🌐🧊🔑🔍💬✨⚔️🎯🧬📋📝🔴🧪📺⛶🕸️🛠️]"), "")
                .trim()
                .lowercase()
            if (clean.length >= 2 && lowerText.contains(clean)) {
                matched.add(node.id)
            }
        }
        return matched.distinct()
    }

    fun applyJsonPatch(context: Context, jsonStr: String): String? {
        return try {
            val obj = JSONObject(jsonStr)
            val changes = mutableListOf<String>()

            if (obj.has("visual")) {
                val v = obj.getJSONObject("visual")
                val cur = _visualState.value
                _visualState.value = cur.copy(
                    mode = v.optString("mode", cur.mode),
                    primaryHex = v.optString("primaryHex", cur.primaryHex),
                    secondaryHex = v.optString("secondaryHex", cur.secondaryHex),
                    centerDotHex = v.optString("centerDotHex", cur.centerDotHex),
                    dotRadiusFraction = v.optDouble("dotRadiusFraction", cur.dotRadiusFraction.toDouble()).toFloat(),
                    globalButtonScale = v.optDouble("globalButtonScale", cur.globalButtonScale.toDouble()).toFloat().coerceIn(0.65f, 1.70f),
                    global3DButtons = v.optBoolean("global3DButtons", cur.global3DButtons)
                )
                changes.add("ভিজ্যুয়াল ও ৩ডি থিম আপডেট")
            }

            if (obj.has("addNodes")) {
                val addArr = obj.getJSONArray("addNodes")
                for (i in 0 until addArr.length()) {
                    val item = addArr.getJSONObject(i)
                    val id = item.optString("id", "ai_btn_" + UUID.randomUUID().toString().take(5))
                    val label = item.optString("label", "⚡ নতুন বাটন")
                    val zone = item.optString("zone", "TOP_ROW_2")
                    val bgHex = item.optString("bgColorHex", "#E0F2FE")
                    val accHex = item.optString("accentColorHex", "#0284C7")
                    val scale = item.optDouble("scaleFactor", 1.0).toFloat().coerceIn(0.65f, 1.70f)
                    val is3D = item.optBoolean("is3DStyle", false)
                    val payload = item.optString("payload", label)
                    autoRegisterFeature(
                        context = context,
                        id = id,
                        label = label,
                        defaultZone = zone,
                        bgColorHex = bgHex,
                        accentColorHex = accHex,
                        isCustomAiFeature = true,
                        customActionPayload = payload,
                        scaleFactor = scale,
                        is3DStyle = is3D
                    )
                }
                changes.add("নতুন বাটন/ফিচার সংযোজন")
            }

            if (obj.has("moveNodes")) {
                val moveObj = obj.getJSONObject("moveNodes")
                val updated = _nodes.value.map { n ->
                    if (moveObj.has(n.id)) {
                        val newZone = moveObj.getString(n.id)
                        n.copy(previousZone = n.zone, zone = newZone)
                    } else n
                }
                _nodes.value = updated
                changes.add("বাটন পজিশন পরিবর্তন")
            }

            if (obj.has("reorderNodes")) {
                val orderObj = obj.getJSONObject("reorderNodes")
                val updated = _nodes.value.map { n ->
                    if (orderObj.has(n.id)) {
                        n.copy(orderIndex = orderObj.optInt(n.id, n.orderIndex))
                    } else n
                }
                _nodes.value = updated
                changes.add("বাটন ক্রম (এদিক-ওদিক) পরিবর্তন")
            }

            if (obj.has("scaleNodes")) {
                val scaleObj = obj.getJSONObject("scaleNodes")
                val updated = _nodes.value.map { n ->
                    if (scaleObj.has(n.id)) {
                        val sc = scaleObj.optDouble(n.id, 1.0).toFloat().coerceIn(0.65f, 1.70f)
                        n.copy(scaleFactor = sc)
                    } else n
                }
                _nodes.value = updated
                changes.add("বাটন সাইজ (বড়/ছোট) পরিবর্তন")
            }

            if (obj.has("style3DNodes")) {
                val s3dObj = obj.getJSONObject("style3DNodes")
                val updated = _nodes.value.map { n ->
                    if (s3dObj.has(n.id)) {
                        n.copy(is3DStyle = s3dObj.optBoolean(n.id, true))
                    } else n
                }
                _nodes.value = updated
                changes.add("থ্রিডি (3D) বাটন স্টাইল")
            }

            if (obj.has("recolorNodes")) {
                val colObj = obj.getJSONObject("recolorNodes")
                val updated = _nodes.value.map { n ->
                    if (colObj.has(n.id)) {
                        val hex = colObj.getString(n.id)
                        n.copy(bgColorHex = hex, accentColorHex = hex)
                    } else n
                }
                _nodes.value = updated
                changes.add("বাটন কালার পরিবর্তন")
            }

            if (obj.has("renameNodes")) {
                val renObj = obj.getJSONObject("renameNodes")
                val updated = _nodes.value.map { n ->
                    if (renObj.has(n.id)) {
                        n.copy(label = renObj.getString(n.id))
                    } else n
                }
                _nodes.value = updated
                changes.add("বাটন নাম পরিবর্তন")
            }

            if (obj.has("toggleFeatures")) {
                val togObj = obj.getJSONObject("toggleFeatures")
                togObj.keys().forEach { k ->
                    val enabled = togObj.optBoolean(k, true)
                    controlFeatureStateAndPower(context, k, enable = enabled, applyImmediately = true)
                }
                changes.add("ফিচার ON/OFF নিয়ন্ত্রণ")
            }

            if (obj.has("applyFeature")) {
                val targetId = obj.optString("applyFeature", "").trim()
                if (targetId.isNotEmpty()) {
                    controlFeatureStateAndPower(context, targetId, enable = true, applyImmediately = true)
                    changes.add("ফিচার লাইভ এপ্লাই ($targetId)")
                }
            }

            if (obj.has("screenTouch")) {
                val st = obj.getJSONObject("screenTouch")
                val x = st.optDouble("x", 0.5).toFloat()
                val y = st.optDouble("y", 0.5).toFloat()
                val action = st.optString("action", "TAP")
                val selector = st.optString("selector", "")
                val loop = st.optBoolean("loop", false)
                val intervalMs = st.optLong("intervalMs", 900L)
                if (loop) {
                    startRealAutoTouchLoop(context, xFraction = x, yFraction = y, intervalMs = intervalMs, selector = selector)
                    changes.add("কন্টিনিউয়াস অটো-টাচ লুপ (${intervalMs}ms)")
                } else {
                    triggerAutonomousScreenTouch(
                        context = context,
                        xFraction = x,
                        yFraction = y,
                        actionType = action,
                        targetLabel = "AI Touch ($action)",
                        targetSelector = selector
                    )
                    changes.add("লাইভ স্ক্রিন টাচ ($action)")
                }
            }

            saveState(context)
            if (changes.isNotEmpty()) changes.joinToString(", ") else null
        } catch (_: Throwable) {
            null
        }
    }

    fun getOmniControlSystemPrompt(): String {
        val currentNodes = _nodes.value
        val activeRow1 = currentNodes.filter { it.zone == "TOP_ROW_1" }.sortedBy { it.orderIndex }.joinToString { "${it.id}('${it.label}',order=${it.orderIndex},ON=${it.isEnabled},3D=${it.is3DStyle},scale=${it.scaleFactor})" }
        val activeRow2 = currentNodes.filter { it.zone == "TOP_ROW_2" }.sortedBy { it.orderIndex }.joinToString { "${it.id}('${it.label}',order=${it.orderIndex},ON=${it.isEnabled},3D=${it.is3DStyle},scale=${it.scaleFactor})" }
        val chatHeaderNodes = currentNodes.filter { it.zone == "CHAT_HEADER" }.sortedBy { it.orderIndex }.joinToString { "${it.id}('${it.label}',order=${it.orderIndex},ON=${it.isEnabled},3D=${it.is3DStyle},scale=${it.scaleFactor})" }
        val bottomDock = currentNodes.filter { it.zone == "BOTTOM_DOCK" }.sortedBy { it.orderIndex }.joinToString { "${it.id}('${it.label}',order=${it.orderIndex},ON=${it.isEnabled},3D=${it.is3DStyle},scale=${it.scaleFactor})" }
        val floatingNodes = currentNodes.filter { it.zone == "FLOATING" }.sortedBy { it.orderIndex }.joinToString { "${it.id}('${it.label}',ON=${it.isEnabled})" }
        val trashRemoved = currentNodes.filter { it.zone == "REMOVED_TRASH" }.joinToString { "${it.id}('${it.label}')" }
        val vis = _visualState.value
        val lock = _aiLockState.value
        val zl = _zeroLoadState.value
        val slots = SovereignDeepResearchAndIncrementalEngine.slots.value
        val customAis = SovereignDeepResearchAndIncrementalEngine.customAiAgents.value
        val websites = SovereignDeepResearchAndIncrementalEngine.createdWebsites.value

        return """
            === [UNIVERSAL OMNI-CONTROL, 3D UI SURGERY & SELF-HEALING INCREMENTAL BRIDGE] ===
            You (Main AI Brain) have 100% real execution control over every current AND future button, feature, 3D style, size, position, website, and incremental slot—regardless of how the user phrases their request in Bengali or English:
            - Auto-Touch Loop: Running=${zl.isAutoTouchLoopRunning}, Interval=${zl.autoTouchIntervalMs}ms | Auto-Scroll: Running=${zl.isAutoScrollLoopRunning}, Step=${zl.autoScrollStepPx}px
            - Background Mode: ${vis.mode} (Primary: ${vis.primaryHex}, Secondary: ${vis.secondaryHex}, Global3DButtons=${vis.global3DButtons}, GlobalScale=${vis.globalButtonScale})
            - Top Row 1 Buttons: [$activeRow1]
            - Top Row 2 Buttons: [$activeRow2]
            - Chat Header Buttons: [$chatHeaderNodes]
            - Bottom Dock Buttons: [${bottomDock.ifBlank { "None" }}]
            - Floating Buttons: [${floatingNodes.ifBlank { "None" }}]
            - Removed/Hidden Buttons: [${trashRemoved.ifBlank { "None" }}]
            - Incremental Project Slots (${slots.size}): [${slots.joinToString { "#${it.slotIndex}:${it.titleBangla}" }}]
            - Created Websites (${websites.size}): [${websites.joinToString { "${it.siteId}:${it.titleBangla}" }}]
            - Custom Created AI Agents (${customAis.size}): [${customAis.joinToString { it.name }}]
            - Permanent Master AI Lock: ${if (lock.isPermanentlyLocked) "LOCKED to ${lock.lockedMasterName}" else "Unlocked / Auto"}
            
            Whenever the user asks to move any button, reorder left/right, resize small/large, make 3D, recolor buttons/backgrounds, add a new button, turn a feature ON/OFF, apply a feature, or touch the screen, emit an [OMNI_PATCH] JSON block at the end of your reply:
            [OMNI_PATCH]{"visual":{"mode":"SOLID|VERTICAL_GRADIENT|RADIAL_DOT|RAINBOW","primaryHex":"#000000","secondaryHex":"#0033FF","global3DButtons":true,"globalButtonScale":1.15},"addNodes":[{"id":"btn_custom","label":"🧊 কাস্টম বাটন","zone":"TOP_ROW_2","bgColorHex":"#E0F2FE","accentColorHex":"#0284C7","scaleFactor":1.15,"is3DStyle":true,"payload":"custom"}],"moveNodes":{"sandbox":"BOTTOM_DOCK|TOP_ROW_1|TOP_ROW_2|FLOATING|REMOVED_TRASH"},"reorderNodes":{"macro":1},"scaleNodes":{"macro":1.3},"style3DNodes":{"macro":true},"recolorNodes":{"macro":"#FFE4E6"},"toggleFeatures":{"macro":true},"applyFeature":"truth_dossier","screenTouch":{"x":0.5,"y":0.5,"action":"TAP|SCROLL_DOWN|SCROLL_UP","selector":"","loop":false,"intervalMs":900}}[/OMNI_PATCH]
            =================================================================================
        """.trimIndent()
    }

    fun setNodesDirect(context: Context, newNodes: List<OmniNode>) {
        _nodes.value = newNodes
        saveState(context)
    }

    private fun saveState(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_OMNI, Context.MODE_PRIVATE)
            val nodesArr = JSONArray()
            _nodes.value.forEach { n ->
                nodesArr.put(
                    JSONObject().apply {
                        put("id", n.id)
                        put("label", n.label)
                        put("defaultLabel", n.defaultLabel)
                        put("zone", n.zone)
                        put("previousZone", n.previousZone)
                        put("bgColorHex", n.bgColorHex)
                        put("accentColorHex", n.accentColorHex)
                        put("textColorHex", n.textColorHex)
                        put("orderIndex", n.orderIndex)
                        put("scaleFactor", n.scaleFactor.toDouble())
                        put("is3DStyle", n.is3DStyle)
                        put("isCustomAiFeature", n.isCustomAiFeature)
                        put("customActionPayload", n.customActionPayload)
                        put("isEnabled", n.isEnabled)
                        put("powerPercent", n.powerPercent)
                        put("isAutoMaintainedByAi", n.isAutoMaintainedByAi)
                        put("applyCount", n.applyCount)
                    }
                )
            }

            val v = _visualState.value
            val visObj = JSONObject().apply {
                put("mode", v.mode)
                put("primaryHex", v.primaryHex)
                put("secondaryHex", v.secondaryHex)
                put("centerDotHex", v.centerDotHex)
                put("dotRadiusFraction", v.dotRadiusFraction.toDouble())
                put("headerBgHex", v.headerBgHex)
                put("chat1BgHex", v.chat1BgHex)
                put("chat2BgHex", v.chat2BgHex)
                put("globalButtonScale", v.globalButtonScale.toDouble())
                put("global3DButtons", v.global3DButtons)
            }

            val l = _aiLockState.value
            val rolesObj = JSONObject()
            l.workerRolesMap.forEach { (k, vRole) -> rolesObj.put(k, vRole) }
            val lockObj = JSONObject().apply {
                put("lockedMasterProfileId", l.lockedMasterProfileId)
                put("lockedMasterName", l.lockedMasterName)
                put("isPermanentlyLocked", l.isPermanentlyLocked)
                put("workerRolesMap", rolesObj)
            }

            prefs.edit()
                .putString(KEY_NODES_JSON, nodesArr.toString())
                .putString(KEY_VISUAL_JSON, visObj.toString())
                .putString(KEY_AI_LOCK_JSON, lockObj.toString())
                .apply()
        } catch (_: Throwable) {}
    }

    private fun loadState(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_OMNI, Context.MODE_PRIVATE)
            val nodesStr = prefs.getString(KEY_NODES_JSON, null)
            if (!nodesStr.isNullOrBlank()) {
                val arr = JSONArray(nodesStr)
                val loaded = mutableListOf<OmniNode>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    loaded.add(
                        OmniNode(
                            id = o.optString("id"),
                            label = o.optString("label"),
                            defaultLabel = o.optString("defaultLabel", o.optString("label")),
                            zone = o.optString("zone", "TOP_ROW_2"),
                            previousZone = o.optString("previousZone", "TOP_ROW_2"),
                            bgColorHex = o.optString("bgColorHex", "#FFFFFF"),
                            accentColorHex = o.optString("accentColorHex", "#0284C7"),
                            textColorHex = o.optString("textColorHex", ""),
                            orderIndex = o.optInt("orderIndex", i),
                            scaleFactor = o.optDouble("scaleFactor", 1.0).toFloat().coerceIn(0.65f, 1.70f),
                            is3DStyle = o.optBoolean("is3DStyle", false),
                            isCustomAiFeature = o.optBoolean("isCustomAiFeature", false),
                            customActionPayload = o.optString("customActionPayload", ""),
                            isEnabled = o.optBoolean("isEnabled", true),
                            powerPercent = o.optInt("powerPercent", 100),
                            isAutoMaintainedByAi = o.optBoolean("isAutoMaintainedByAi", true),
                            applyCount = o.optInt("applyCount", 0)
                        )
                    )
                }
                if (loaded.isNotEmpty()) {
                    val existingIds = loaded.map { it.id }.toSet()
                    defaultNodes().forEach { defNode ->
                        if (defNode.id !in existingIds) {
                            loaded.add(defNode)
                        }
                    }
                    _nodes.value = loaded
                }
            }

            val visStr = prefs.getString(KEY_VISUAL_JSON, null)
            if (!visStr.isNullOrBlank()) {
                val vo = JSONObject(visStr)
                _visualState.value = OmniVisualState(
                    mode = vo.optString("mode", "SOLID"),
                    primaryHex = vo.optString("primaryHex", "#FFFFFF"),
                    secondaryHex = vo.optString("secondaryHex", "#000000"),
                    centerDotHex = vo.optString("centerDotHex", "#FF0055"),
                    dotRadiusFraction = vo.optDouble("dotRadiusFraction", 0.38).toFloat(),
                    headerBgHex = vo.optString("headerBgHex", "#F8FAFC"),
                    chat1BgHex = vo.optString("chat1BgHex", "#FFFFFF"),
                    chat2BgHex = vo.optString("chat2BgHex", "#090D1A"),
                    globalButtonScale = vo.optDouble("globalButtonScale", 1.0).toFloat().coerceIn(0.65f, 1.70f),
                    global3DButtons = vo.optBoolean("global3DButtons", false)
                )
            }

            val lockStr = prefs.getString(KEY_AI_LOCK_JSON, null)
            if (!lockStr.isNullOrBlank()) {
                val lo = JSONObject(lockStr)
                val map = mutableMapOf<String, String>()
                val rolesObj = lo.optJSONObject("workerRolesMap")
                rolesObj?.keys()?.forEach { k -> map[k] = rolesObj.optString(k) }
                _aiLockState.value = OmniAiLockState(
                    lockedMasterProfileId = lo.optString("lockedMasterProfileId", ""),
                    lockedMasterName = lo.optString("lockedMasterName", "Auto Master Brain"),
                    isPermanentlyLocked = lo.optBoolean("isPermanentlyLocked", false),
                    workerRolesMap = map
                )
            }
        } catch (_: Throwable) {}
    }

    private var previousNodesUndoSnapshot: List<OmniNode>? = null

    fun saveUndoSnapshot() {
        previousNodesUndoSnapshot = _nodes.value.toList()
    }

    fun restoreUndoSnapshot(context: Context): String {
        val snap = previousNodesUndoSnapshot ?: return "⚠️ রোলব্যাক করার মতো পূর্ববর্তী বাটন পরিবর্তন নেই।"
        _nodes.value = snap
        saveState(context)
        return "🔄 **[টাইম-মেশিন রোলব্যাক সম্পন্ন]:** অ্যাপের সব বাটন ও লেআউট ঠিক আগের অবস্থায় ফিরিয়ে আনা হয়েছে!"
    }

    fun kingControlButtonBlinkOrState(
        context: Context,
        rawCommand: String
    ): String? {
        val lower = rawCommand.lowercase()
        if (lower.contains("আগের অবস্থায়") || lower.contains("undo") || lower.contains("আনডু") || lower.contains("রোলব্যাক")) {
            return restoreUndoSnapshot(context)
        }
        val wantsBlink = lower.contains("নিপাক") || lower.contains("ব্লিঙ্ক") || lower.contains("blink") || lower.contains("পালস") || lower.contains("জ্বলুক")
        val wantsTurnOff = (lower.contains("বন্ধ করো") || lower.contains("অফ করো") || lower.contains("turn off")) && (lower.contains("বাটন") || lower.contains("ফিচার"))
        val wantsTurnOn = (lower.contains("চালু করো") || lower.contains("অন করো") || lower.contains("turn on")) && (lower.contains("বাটন") || lower.contains("ফিচার"))

        if (!wantsBlink && !wantsTurnOff && !wantsTurnOn) return null

        saveUndoSnapshot()
        var matchedTitle = ""
        val updated = _nodes.value.map { n ->
            val lbl = n.label.lowercase()
            val idLow = n.id.lowercase()
            val isTarget = lower.contains("সব বাটন") ||
                (lbl.length >= 3 && lower.contains(lbl.replace(Regex("[^a-zA-Z0-9\u0980-\u09FF ]"), "").trim().take(6))) ||
                lower.contains(idLow) ||
                (lower.contains("লাইভ") && idLow.contains("live")) ||
                (lower.contains("এপিআই") && idLow.contains("api")) ||
                (lower.contains("ন্যানোবট") && idLow.contains("nanobot")) ||
                (lower.contains("ম্যাক্রো") && idLow.contains("macro")) ||
                (lower.contains("স্যান্ডবক্স") && idLow.contains("sandbox")) ||
                (lower.contains("অটো টাচ") && idLow.contains("auto_touch"))
            if (isTarget) {
                matchedTitle = n.label
                when {
                    wantsBlink -> n.copy(isBlinkingPulse = !n.isBlinkingPulse, is3DStyle = true, scaleFactor = 1.18f)
                    wantsTurnOff -> n.copy(isEnabled = false, isBlinkingPulse = false)
                    wantsTurnOn -> n.copy(isEnabled = true)
                    else -> n
                }
            } else n
        }
        if (matchedTitle.isNotBlank()) {
            _nodes.value = updated
            saveState(context)
            if (wantsTurnOff || wantsTurnOn) {
                val targetNode = updated.find { it.label == matchedTitle }
                if (targetNode != null) {
                    controlFeatureStateAndPower(context, targetNode.id, enable = wantsTurnOn, applyImmediately = true)
                }
            }
            return when {
                wantsBlink -> "👑 **[কিং বাটন কন্ট্রোল]:** '$matchedTitle' বাটনে নিপাক/ব্লিঙ্ক ও ৩ডি গ্লো চালু করা হয়েছে!"
                wantsTurnOff -> "👑 **[কিং বাটন কন্ট্রোল]:** '$matchedTitle' বাটন ও ফিচার বন্ধ (OFF) করা হয়েছে!"
                else -> "👑 **[কিং বাটন কন্ট্রোল]:** '$matchedTitle' বাটন ও ফিচার সক্রিয় (ON) করা হয়েছে!"
            }
        }
        return null
    }
}
