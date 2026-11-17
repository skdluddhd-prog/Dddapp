package com.example.ui.ai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.service.SovereignMediaProjectionService
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.GeminiLivePhase
import com.example.engine.GeminiLiveSessionState
import com.example.engine.LiveIntelligenceMode
import com.example.engine.LiveTargetPulse
import com.example.engine.LiveVideoStreamSpeed
import com.example.engine.LiveVisionSource
import com.example.engine.SovereignGeminiLiveOmniEngine
import com.example.engine.SovereignMetaKernelEngine
import com.example.engine.UniversalAiManager
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRose
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Global Overlay Layer for Google Gemini Live Mode + Screen Share + Proactive Autonomous Speech + Vision Auto-Touch.
 *
 * Renders:
 * 1. Full-Screen Neon Target Pulse Ring (`🎯`) at exact `(xFraction, yFraction)` screen coordinates whenever Gemini touches or highlights a button.
 * 2. Floating Compact Live Capsule when minimized (`isMinimizedCapsule == true`), allowing the user to freely use Browser Tabs A/B or Apps while Gemini watches & talks proactively.
 * 3. Full Gemini Live Plasma Studio when expanded (`isMinimizedCapsule == false`).
 */
@Composable
fun SovereignGeminiLiveGlobalOverlay(
    onRequestExternalScreenSharePermission: () -> Unit,
    onSendLiveVoiceOrTextCommand: (String) -> Unit,
    onToggleVoiceMic: () -> Unit,
    isVoiceMicListening: Boolean
) {
    val context = LocalContext.current
    val sessionState by SovereignGeminiLiveOmniEngine.sessionState.collectAsState()
    val latestBitmap by SovereignGeminiLiveOmniEngine.latestFrameBitmap.collectAsState()

    if (!sessionState.isLiveActive) return

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val maxW = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val maxH = constraints.maxHeight.toFloat().coerceAtLeast(1f)

        // 1. If Full Studio Mode (not minimized), show the Full Gemini Live Omni-Share Studio
        if (!sessionState.isMinimizedCapsule) {
            GeminiLiveFullStudioScreen(
                state = sessionState,
                latestBitmap = latestBitmap,
                isVoiceMicListening = isVoiceMicListening,
                onRequestExternalScreenSharePermission = onRequestExternalScreenSharePermission,
                onSendLiveVoiceOrTextCommand = onSendLiveVoiceOrTextCommand,
                onToggleVoiceMic = onToggleVoiceMic,
                onMinimizeToCapsule = {
                    SovereignGeminiLiveOmniEngine.setMinimizedCapsuleMode(true)
                },
                onStopLive = {
                    SovereignGeminiLiveOmniEngine.stopLiveSession(context)
                }
            )
        } else {
            // 2. Minimized Floating Live Capsule at Top of Screen (User can interact with everything underneath!)
            GeminiLiveFloatingCapsuleBar(
                state = sessionState,
                latestBitmap = latestBitmap,
                onRequestExternalScreenSharePermission = onRequestExternalScreenSharePermission,
                onExpandStudio = {
                    SovereignGeminiLiveOmniEngine.setMinimizedCapsuleMode(false)
                },
                onStopLive = {
                    SovereignGeminiLiveOmniEngine.stopLiveSession(context)
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 6.dp, start = 8.dp, end = 8.dp)
            )
        }

        // 3. Always-On Neon Target Pulse Ring (`🎯`) whenever Gemini Live touches or highlights a screen coordinate
        val pulse = sessionState.activeTargetPulse
        if (pulse != null) {
            GeminiLiveTargetPulseOverlay(
                pulse = pulse,
                screenWidthPx = maxW,
                screenHeightPx = maxH,
                onConfirm = {
                    SovereignGeminiLiveOmniEngine.confirmAndExecutePendingPulse(context)
                },
                onCancel = {
                    SovereignGeminiLiveOmniEngine.cancelPendingPulse(context)
                }
            )
        }
    }
}

@Composable
private fun GeminiLiveFloatingCapsuleBar(
    state: GeminiLiveSessionState,
    latestBitmap: Bitmap?,
    onRequestExternalScreenSharePermission: () -> Unit,
    onExpandStudio: () -> Unit,
    onStopLive: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Surface(
        color = Color(0xF2060B16),
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 12.dp,
        border = BorderStroke(
            width = 1.5.dp,
            brush = Brush.horizontalGradient(
                listOf(CyberCyan, CyberPurple, CyberEmerald, CyberAmber)
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("gemini_live_floating_capsule")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Mini Reactive Plasma Orb
                    GeminiLivePlasmaOrb(
                        phase = state.phase,
                        rmsLevel = state.audioRmsLevel,
                        sizeDp = 34f
                    )

                    // Mini Live Thumbnail Preview
                    if (latestBitmap != null) {
                        Image(
                            bitmap = latestBitmap.asImageBitmap(),
                            contentDescription = "Live Screen Share Thumbnail",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(width = 28.dp, height = 36.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, CyberCyan, RoundedCornerShape(6.dp))
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "🔴 GEMINI LIVE",
                                color = CyberCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Δ ${state.visualDeltaPercent.toInt()}%",
                                color = CyberEmerald,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = state.phase.labelBangla,
                                color = Color(0xFFE2E8F0),
                                fontSize = 9.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = state.liveAiSubtitle,
                            color = Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Quick Action Buttons on Floating Capsule
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 1-Tap Live Deep-Think <-> Live Debate Mode Switcher on Floating Capsule
                    val isCapsuleDebate = state.intelligenceMode == LiveIntelligenceMode.GOD_LEVEL_LIVE_DEBATE
                    Surface(
                        color = if (isCapsuleDebate) CyberAmber.copy(alpha = 0.28f) else CyberCyan.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (isCapsuleDebate) CyberAmber else CyberCyan),
                        modifier = Modifier
                            .clickable {
                                SovereignGeminiLiveOmniEngine.toggleLiveIntelligenceMode()
                            }
                            .testTag("capsule_toggle_live_debate_btn")
                    ) {
                        Text(
                            text = if (isCapsuleDebate) "⚔️ ডিবেট" else "🧠 ডিপ-থিংক",
                            color = if (isCapsuleDebate) CyberAmber else CyberCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }

                    // Toggle Proactive Speaking
                    Surface(
                        color = if (state.isProactiveAutoSpeakEnabled) CyberEmerald.copy(alpha = 0.25f) else Color(0xFF1E293B),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (state.isProactiveAutoSpeakEnabled) CyberEmerald else Color(0xFF475569)),
                        modifier = Modifier.clickable {
                            SovereignGeminiLiveOmniEngine.toggleProactiveAutoSpeak()
                        }
                    ) {
                        Text(
                            text = if (state.isProactiveAutoSpeakEnabled) "🧠 স্বয়ংক্রিয়" else "🔇 ম্যানুয়াল",
                            color = if (state.isProactiveAutoSpeakEnabled) CyberEmerald else Color(0xFF94A3B8),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }

                    // External OS Screen Share trigger
                    Surface(
                        color = if (state.visionSource == LiveVisionSource.SCREEN_SHARE_EXTERNAL_OS) CyberPurple.copy(alpha = 0.3f) else Color(0xFF1E293B),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CyberPurple),
                        modifier = Modifier.clickable {
                            onRequestExternalScreenSharePermission()
                        }
                    ) {
                        Text(
                            text = "🌐 বাইরেও",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }

                    // 1-Tap Side Clipboard Paste & Send Button on Floating Capsule
                    val capsuleCtx = LocalContext.current
                    Surface(
                        color = CyberEmerald.copy(alpha = 0.28f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CyberEmerald),
                        modifier = Modifier
                            .clickable {
                                val clipText = com.example.engine.SovereignAutonomousGodBrainEngine.readClipboardForLiveSidePaste(capsuleCtx)
                                if (clipText.isNotBlank()) {
                                    onSendLiveVoiceOrTextCommand(clipText)
                                } else {
                                    onExpandStudio()
                                }
                            }
                            .testTag("capsule_side_paste_btn")
                    ) {
                        Text(
                            text = "📋 পেস্ট",
                            color = CyberEmerald,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }

                    // Expand to Full Studio
                    Surface(
                        color = CyberCyan.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CyberCyan),
                        modifier = Modifier
                            .clickable { onExpandStudio() }
                            .testTag("expand_gemini_live_studio_btn")
                    ) {
                        Text(
                            text = "⛶ বড়",
                            color = CyberCyan,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                        )
                    }

                    // Stop Live Session
                    Surface(
                        color = CyberRose.copy(alpha = 0.28f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CyberRose),
                        modifier = Modifier
                            .clickable { onStopLive() }
                            .testTag("stop_gemini_live_capsule_btn")
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
        }
    }
}

@Composable
private fun GeminiLiveFullStudioScreen(
    state: GeminiLiveSessionState,
    latestBitmap: Bitmap?,
    isVoiceMicListening: Boolean,
    onRequestExternalScreenSharePermission: () -> Unit,
    onSendLiveVoiceOrTextCommand: (String) -> Unit,
    onToggleVoiceMic: () -> Unit,
    onMinimizeToCapsule: () -> Unit,
    onStopLive: () -> Unit
) {
    val context = LocalContext.current
    var liveCommandInput by remember { mutableStateOf("") }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            SovereignGeminiLiveOmniEngine.setVisionSource(
                context = context,
                source = LiveVisionSource.CAMERA_LIVE_VIEW
            )
        }
    }

    Surface(
        color = Color(0xFA050914),
        modifier = Modifier
            .fillMaxSize()
            .testTag("gemini_live_full_studio")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // 1. TOP HEADER BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = CyberRose.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, CyberRose)
                        ) {
                            Text(
                                text = "🔴 LIVE OMNI-SHARE",
                                color = CyberRose,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                        Text(
                            text = "Google Gemini Live + অটো-টাচ + প্রোঅ্যাক্টিভ মাইন্ড",
                            color = CyberCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = state.phase.labelBangla,
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Minimize to Floating Capsule so user can use browser/apps underneath
                    Surface(
                        color = CyberCyan.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.2.dp, CyberCyan),
                        modifier = Modifier
                            .clickable { onMinimizeToCapsule() }
                            .testTag("minimize_gemini_live_btn")
                    ) {
                        Text(
                            text = "🗕 মিনি ক্যাপসুল (স্ক্রিনে কাজ করুন)",
                            color = CyberCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                        )
                    }

                    // Close Live Mode
                    Surface(
                        color = CyberRose,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .clickable { onStopLive() }
                            .testTag("close_gemini_live_btn")
                    ) {
                        Text(
                            text = "✕ বন্ধ",
                            color = Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 1.5 GOD-LEVEL LIVE INTELLIGENCE MODE SWITCHER (🧠 লাইভ ডিপ-থিংক জ্ঞান মোড ⇄ ⚔️ লাইভ ডিবেট মোড ⇄ 🤖 ব্যাকগ্রাউন্ড অর্ডার)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isDeepThink = state.intelligenceMode == LiveIntelligenceMode.DEEP_THINK_KNOWLEDGE
                val isLiveDebate = state.intelligenceMode == LiveIntelligenceMode.GOD_LEVEL_LIVE_DEBATE

                Surface(
                    color = if (isDeepThink) CyberCyan.copy(alpha = 0.28f) else Color(0xFF0F172A),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.4.dp, if (isDeepThink) CyberCyan else Color(0xFF334155)),
                    modifier = Modifier
                        .clickable {
                            SovereignGeminiLiveOmniEngine.setLiveIntelligenceMode(LiveIntelligenceMode.DEEP_THINK_KNOWLEDGE)
                        }
                        .testTag("live_mode_deep_think_btn")
                ) {
                    Text(
                        text = "🧠 লাইভ ডিপ-থিংক (গভীর জ্ঞান মোড)",
                        color = if (isDeepThink) Color.White else Color(0xFF94A3B8),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
                    )
                }

                Surface(
                    color = if (isLiveDebate) CyberAmber.copy(alpha = 0.30f) else Color(0xFF0F172A),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.4.dp, if (isLiveDebate) CyberAmber else Color(0xFF334155)),
                    modifier = Modifier
                        .clickable {
                            SovereignGeminiLiveOmniEngine.setLiveIntelligenceMode(LiveIntelligenceMode.GOD_LEVEL_LIVE_DEBATE)
                        }
                        .testTag("live_mode_debate_btn")
                ) {
                    Text(
                        text = "⚔️ লাইভ ডিবেট মোড (৩-ব্রেন গড লেভেল)",
                        color = if (isLiveDebate) CyberAmber else Color(0xFF94A3B8),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
                    )
                }

                Surface(
                    color = CyberEmerald.copy(alpha = 0.24f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.2.dp, CyberEmerald),
                    modifier = Modifier
                        .clickable {
                            val orderText = liveCommandInput.trim().ifBlank {
                                state.liveUserTranscript.trim().ifBlank { "স্ক্রিন ও প্রজেক্ট পর্যবেক্ষণ করে ব্যাকগ্রাউন্ডে ডিপ-থিংক অপটিমাইজেশন সম্পন্ন করো" }
                            }
                            liveCommandInput = ""
                            SovereignMediaProjectionService.showSystemOverlayIfPermitted(context)
                            SovereignGeminiLiveOmniEngine.setMinimizedCapsuleMode(true)
                            SovereignMetaKernelEngine.startBackgroundAutonomousOrder(context, orderText)
                        }
                        .testTag("live_mode_bg_order_btn")
                ) {
                    Text(
                        text = "🤖 ব্যাকগ্রাউন্ডে কাজ দিয়ে YouTube/Chrome চালান",
                        color = CyberEmerald,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 2. VISION SOURCE SWITCHER ROW (In-App Screen, Full Phone OS Screen Share, Camera, Lens Flip, System Overlay, Pause)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VisionSourceChip(
                    label = "📱 অ্যাপ ও ব্রাউজার স্ক্রিন",
                    isSelected = state.visionSource == LiveVisionSource.SCREEN_SHARE_IN_APP,
                    accent = CyberCyan,
                    onClick = {
                        SovereignGeminiLiveOmniEngine.setVisionSource(
                            context = context,
                            source = LiveVisionSource.SCREEN_SHARE_IN_APP
                        )
                    }
                )
                VisionSourceChip(
                    label = "🌐 ফুল ফোন স্ক্রিন শেয়ার (অ্যাপের বাইরেও)",
                    isSelected = state.visionSource == LiveVisionSource.SCREEN_SHARE_EXTERNAL_OS,
                    accent = CyberPurple,
                    onClick = {
                        onRequestExternalScreenSharePermission()
                    }
                )
                VisionSourceChip(
                    label = if (state.visionSource == LiveVisionSource.CAMERA_LIVE_VIEW) {
                        if (state.isUsingFrontCamera) "🤳 ফ্রন্ট ক্যামেরা লাইভ" else "📷 ব্যাক ক্যামেরা লাইভ"
                    } else {
                        "📷 রিয়েল ক্যামেরা ভিউ (Camera2)"
                    },
                    isSelected = state.visionSource == LiveVisionSource.CAMERA_LIVE_VIEW,
                    accent = CyberEmerald,
                    onClick = {
                        val hasCamPerm = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasCamPerm) {
                            SovereignGeminiLiveOmniEngine.setVisionSource(
                                context = context,
                                source = LiveVisionSource.CAMERA_LIVE_VIEW
                            )
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                )
                if (state.visionSource == LiveVisionSource.CAMERA_LIVE_VIEW) {
                    VisionSourceChip(
                        label = if (state.isUsingFrontCamera) "🔄 পেছনের ক্যামেরায় যান" else "🤳 সামনের ক্যামেরায় যান",
                        isSelected = true,
                        accent = CyberCyan,
                        onClick = {
                            SovereignGeminiLiveOmniEngine.toggleCameraLensFacing(context)
                        }
                    )
                }
                val canDrawSysOverlay = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)
                VisionSourceChip(
                    label = if (canDrawSysOverlay) "🪟 সিস্টেম ওভারলে পিল চালু করুন" else "🪟 অন্য অ্যাপের ওপর ভাসমান পিল (পারমিশন)",
                    isSelected = canDrawSysOverlay && state.isMinimizedCapsule,
                    accent = CyberCyan,
                    onClick = {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)) {
                            SovereignMediaProjectionService.showSystemOverlayIfPermitted(context)
                            SovereignGeminiLiveOmniEngine.setMinimizedCapsuleMode(true)
                        } else {
                            try {
                                val permIntent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                ).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(permIntent)
                            } catch (_: Exception) {}
                        }
                    }
                )
                VisionSourceChip(
                    label = "⏸️ স্ক্রিন দেখা পজ",
                    isSelected = state.visionSource == LiveVisionSource.VISION_PAUSED,
                    accent = CyberAmber,
                    onClick = {
                        SovereignGeminiLiveOmniEngine.setVisionSource(
                            context = context,
                            source = LiveVisionSource.VISION_PAUSED
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 2.5 LIVE VIDEO STREAM SPEED (FPS), VOICE SPEED & EXACT AI MODEL CHOOSER ROW
            val profiles by UniversalAiManager.profiles.collectAsState()
            val activeProfileId by UniversalAiManager.activeProfileId.collectAsState()
            val activeProf = remember(profiles, activeProfileId) { UniversalAiManager.getActiveProfile() }
            val currentModelLabel = activeProf?.modelName?.ifBlank { "AUTO" } ?: "AUTO"
            val modelCandidates = remember(activeProf) {
                (listOf("AUTO", "gemini-2.5-pro", "gemini-2.5-flash", "gemini-2.0-flash") + (activeProf?.availableModels ?: emptyList())).distinct().take(7)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Video Stream Speed Cycle Chip
                VisionSourceChip(
                    label = "📹 ${state.videoStreamSpeed.labelBangla}",
                    isSelected = true,
                    accent = CyberEmerald,
                    onClick = {
                        SovereignGeminiLiveOmniEngine.cycleVideoStreamSpeed()
                    }
                )

                // Voice TTS Speed Cycle Chip
                VisionSourceChip(
                    label = "🔊 স্পিড: ${String.format(java.util.Locale.US, "%.2fx", state.ttsSpeechRate)}",
                    isSelected = state.ttsSpeechRate > 1.1f,
                    accent = CyberAmber,
                    onClick = {
                        val nextRate = when {
                            state.ttsSpeechRate < 1.05f -> 1.22f
                            state.ttsSpeechRate < 1.30f -> 1.45f
                            state.ttsSpeechRate < 1.50f -> 0.90f
                            else -> 1.05f
                        }
                        SovereignGeminiLiveOmniEngine.setLiveVoiceSpeechRate(nextRate)
                    }
                )

                // Active AI Model Chooser Chips inside Live Mode
                modelCandidates.forEach { mdl ->
                    val isSelectedMdl = currentModelLabel.equals(mdl, ignoreCase = true)
                    VisionSourceChip(
                        label = if (mdl == "AUTO") "⚡ মডেল: AUTO-RACE" else "🤖 $mdl",
                        isSelected = isSelectedMdl,
                        accent = if (isSelectedMdl) CyberCyan else CyberPurple,
                        onClick = {
                            UniversalAiManager.switchModelOnSameKeyInstant(context, activeProf?.id, mdl)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 3. CENTER HERO: GEMINI LIVE PLASMA ORB + LIVE SCREEN SHARE PIP MONITOR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Plasma Audio Orb + Barge-in Control
                Surface(
                    color = Color(0xFF0A1122),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF1E293B)),
                    modifier = Modifier
                        .weight(1f)
                        .height(175.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        GeminiLivePlasmaOrb(
                            phase = state.phase,
                            rmsLevel = state.audioRmsLevel,
                            sizeDp = 96f
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = when (state.phase) {
                                GeminiLivePhase.SPEAKING_AI -> "🔊 জেমিনি কথা বলছে (বললেই থেমে শুনবে)"
                                GeminiLivePhase.LISTENING_USER -> "🎙️ সরাসরি আপনার কথা শুনছি..."
                                GeminiLivePhase.THINKING_VISION -> "🧠 স্ক্রিনের ছবি ও কথা বিশ্লেষণ হচ্ছে..."
                                GeminiLivePhase.EXECUTING_TOUCH -> "🎯 স্ক্রিনে অটো-টাচ করছে..."
                                GeminiLivePhase.AWAITING_CONFIRMATION -> "🛡️ আপনার 'হ্যাঁ' বা কনফার্মের অপেক্ষায়..."
                                else -> "👁️ লাইভ স্ক্রিন ওয়াচার সক্রিয়"
                            },
                            color = CyberCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                }

                // Right: Live Screen Capture Preview Monitor & Telemetry
                Surface(
                    color = Color(0xFF0A1122),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.55f)),
                    modifier = Modifier
                        .weight(1.05f)
                        .height(175.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "👁️ লাইভ স্ক্রিন ভিউ",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Δ ${state.visualDeltaPercent.toInt()}% • ${state.framesAnalyzedCount} ফ্রেম",
                                color = CyberEmerald,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF050811))
                                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (latestBitmap != null) {
                                Image(
                                    bitmap = latestBitmap.asImageBitmap(),
                                    contentDescription = "Live Shared Screen Preview",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = "স্ক্রিন ফ্রেম ক্যাপচার হচ্ছে...",
                                    color = Color(0xFF64748B),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Text(
                            text = "নিজে বলেছে: ${state.proactiveInterventionsCount} বার • অটো-টাচ: ${state.autonomousTouchesCount} বার",
                            color = CyberAmber,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. THREE CORE AUTONOMOUS CAPABILITY TOGGLES (Proactive Mind, Vision Auto-Touch, Safety Gate)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CapabilityToggleCard(
                    title = if (state.isProactiveAutoSpeakEnabled) "🧠 নিজে থেকে কথা: চালু" else "🔇 নিজে থেকে কথা: বন্ধ",
                    subtitle = "আপনি না বললেও স্ক্রিন দেখে কথা বলবে",
                    isEnabled = state.isProactiveAutoSpeakEnabled,
                    accent = CyberEmerald,
                    testTag = "toggle_proactive_speak_btn",
                    onClick = { SovereignGeminiLiveOmniEngine.toggleProactiveAutoSpeak() }
                )

                CapabilityToggleCard(
                    title = if (state.isVisionAutoTouchEnabled) "🎯 ভিশন অটো-টাচ: চালু" else "✋ ভিশন অটো-টাচ: বন্ধ",
                    subtitle = "মুখে বললেই বাটনে ক্লিক ও স্ক্রল করবে",
                    isEnabled = state.isVisionAutoTouchEnabled,
                    accent = CyberCyan,
                    testTag = "toggle_vision_autotouch_btn",
                    onClick = { SovereignGeminiLiveOmniEngine.toggleVisionAutoTouch() }
                )

                CapabilityToggleCard(
                    title = if (state.isSafetyPermissionGateEnabled) "🛡️ সেফটি পারমিশন: চালু" else "⚡ ডিরেক্ট ফাস্ট টাচ",
                    subtitle = "সেনসিটিভ বাটনে আগে অনুমতি চাইবে",
                    isEnabled = state.isSafetyPermissionGateEnabled,
                    accent = CyberAmber,
                    testTag = "toggle_safety_gate_btn",
                    onClick = { SovereignGeminiLiveOmniEngine.toggleSafetyPermissionGate() }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 5. LIVE FULL-DUPLEX SUBTITLE & BARGE-IN TRANSCRIPT CARD
            Surface(
                color = Color(0xFF0B1325),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    if (state.liveUserTranscript.isNotBlank()) {
                        Text(
                            text = "🎙️ আপনি বলেছেন: \"${state.liveUserTranscript}\"",
                            color = CyberEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🔊 ${state.liveAiSubtitle}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )

                        if (state.phase == GeminiLivePhase.SPEAKING_AI) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = CyberRose.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, CyberRose),
                                modifier = Modifier.clickable {
                                    SovereignGeminiLiveOmniEngine.stopSpeakingForBargeIn()
                                }
                            ) {
                                Text(
                                    text = "⏹️ থামান",
                                    color = CyberRose,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    // Quick Instant Voice-Action Demo Chips
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        QuickLiveCommandChip("👁️ স্ক্রিনে এখন কী আছে বলো?") {
                            onSendLiveVoiceOrTextCommand("স্ক্রিনে এখন কী কী দেখা যাচ্ছে এবং আমি কী করতে পারি আমাকে বাংলায় বলো")
                        }
                        QuickLiveCommandChip("⬇️ নিচে স্ক্রল করো") {
                            onSendLiveVoiceOrTextCommand("স্ক্রিন নিচে স্ক্রল করো")
                        }
                        QuickLiveCommandChip("⬆️ উপরে স্ক্রল করো") {
                            onSendLiveVoiceOrTextCommand("স্ক্রিন উপরে স্ক্রল করো")
                        }
                        QuickLiveCommandChip("🎯 মাঝখানে ক্লিক করো") {
                            onSendLiveVoiceOrTextCommand("স্ক্রিনের মাঝখানে ক্লিক করো")
                        }
                        QuickLiveCommandChip("🛡️ ডিলিট বাটনে ক্লিক (সেফটি টেস্ট)") {
                            onSendLiveVoiceOrTextCommand("উপরে ডানদিকের ডিলিট বাটনে ক্লিক করো")
                        }
                    }

                    // Live Harvested Real Screen Buttons & DOM Nodes Strip (Fix #6 & #7)
                    if (state.liveDetectedButtonsList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "🎯 স্ক্রিনে শনাক্তকৃত রিয়েল বাটন/লিংক (ট্যাপ করলে বা মুখে বললেই ক্লিক করবে):",
                            color = CyberAmber,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            state.liveDetectedButtonsList.forEach { btnLabel ->
                                QuickLiveCommandChip("🔘 $btnLabel") {
                                    onSendLiveVoiceOrTextCommand("$btnLabel বাটনে ক্লিক করো")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 6. REAL-TIME ACTION & PROACTIVE INTERVENTION LOGS
            Text(
                text = "📜 লাইভ ভিশন, প্রোঅ্যাক্টিভ স্পিচ ও অটো-টাচ লগ:",
                color = Color(0xFF94A3B8),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(5.dp),
                contentPadding = PaddingValues(bottom = 6.dp)
            ) {
                items(state.actionLogs, key = { it.id }) { log ->
                    Surface(
                        color = Color(0xFF0F172A),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(0.8.dp, Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = log.titleBangla,
                                color = when (log.category) {
                                    "PROACTIVE_SPEECH" -> CyberEmerald
                                    "AUTO_TOUCH" -> CyberCyan
                                    "SAFETY_GATE" -> CyberAmber
                                    "BARGE_IN" -> CyberRose
                                    else -> Color.White
                                },
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = log.detailBangla,
                                color = Color(0xFFCBD5E1),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // 7. BOTTOM LIVE VOICE / TEXT COMMAND BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Hands-Free Live Mic Toggle
                Surface(
                    color = if (isVoiceMicListening) CyberRose else CyberEmerald,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onToggleVoiceMic() }
                        .testTag("gemini_live_mic_toggle_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isVoiceMicListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = "Live Microphone Toggle",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = liveCommandInput,
                    onValueChange = { liveCommandInput = it },
                    placeholder = {
                        Text(
                            text = "মুখে বলুন বা এখানে লাইভ আদেশ লিখুন (যেমন: নিচে নামাও / এখানে ক্লিক করো)...",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A),
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = Color(0xFF334155)
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (liveCommandInput.isNotBlank()) {
                                onSendLiveVoiceOrTextCommand(liveCommandInput)
                                liveCommandInput = ""
                            }
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("gemini_live_command_input")
                )

                // Side 1-Tap Clipboard Paste Button in Live Mode ("লাইভ মোডে সাইডে পেস্ট করার বাটন")
                Surface(
                    color = Color(0xFF0E2238),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.2.dp, CyberEmerald),
                    modifier = Modifier
                        .height(48.dp)
                        .clickable {
                            val clip = com.example.engine.SovereignAutonomousGodBrainEngine.readClipboardForLiveSidePaste(context)
                            if (clip.isNotBlank()) {
                                liveCommandInput = if (liveCommandInput.isBlank()) clip else "$liveCommandInput\n$clip"
                            }
                        }
                        .testTag("gemini_live_side_paste_btn")
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 10.dp)) {
                        Text(
                            text = "📋 পেস্ট",
                            color = CyberEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Button(
                    onClick = {
                        if (liveCommandInput.isNotBlank()) {
                            onSendLiveVoiceOrTextCommand(liveCommandInput)
                            liveCommandInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("gemini_live_send_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Live Command",
                        tint = Color(0xFF040812)
                    )
                }
            }
        }
    }
}

/**
 * Animated Google Gemini Live Plasma Audio Orb.
 * Dynamically shifts color and wave amplitude based on voice RMS and live phase.
 */
@Composable
fun GeminiLivePlasmaOrb(
    phase: GeminiLivePhase,
    rmsLevel: Float,
    sizeDp: Float = 92f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gemini_live_orb")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orb_wave_phase"
    )
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 950, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_breath"
    )

    val (primaryColor, secondaryColor) = when (phase) {
        GeminiLivePhase.SPEAKING_AI -> Pair(CyberAmber, CyberCyan)
        GeminiLivePhase.LISTENING_USER -> Pair(CyberEmerald, CyberCyan)
        GeminiLivePhase.THINKING_VISION -> Pair(CyberPurple, CyberCyan)
        GeminiLivePhase.EXECUTING_TOUCH -> Pair(CyberRose, CyberAmber)
        GeminiLivePhase.AWAITING_CONFIRMATION -> Pair(CyberAmber, CyberRose)
        else -> Pair(CyberCyan, CyberPurple)
    }

    Canvas(
        modifier = Modifier.size(sizeDp.dp)
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val baseRadius = (size.minDimension / 2f) * 0.56f
        val dynamicBoost = (rmsLevel.coerceIn(0.06f, 1.0f) * 0.38f) + (breathScale - 0.88f)

        // Outer glowing halo rings
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = 0.42f),
                    secondaryColor.copy(alpha = 0.16f),
                    Color.Transparent
                ),
                center = center,
                radius = baseRadius * (1.55f + dynamicBoost)
            ),
            radius = baseRadius * (1.55f + dynamicBoost),
            center = center
        )

        // Concentric harmonic plasma ring
        drawCircle(
            color = secondaryColor.copy(alpha = 0.65f),
            radius = baseRadius * (1.18f + dynamicBoost * 0.6f),
            center = center,
            style = Stroke(width = 3.5f)
        )

        // Core Plasma Sphere
        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(primaryColor, secondaryColor),
                start = Offset(
                    center.x + cos(wavePhase) * baseRadius,
                    center.y + sin(wavePhase) * baseRadius
                ),
                end = Offset(
                    center.x - cos(wavePhase) * baseRadius,
                    center.y - sin(wavePhase) * baseRadius
                )
            ),
            radius = baseRadius * (0.92f + dynamicBoost * 0.35f),
            center = center
        )
    }
}

/**
 * Full-Screen Neon Target Pulse Ring (`🎯`) with Safety Confirmation Gate buttons.
 */
@Composable
private fun GeminiLiveTargetPulseOverlay(
    pulse: LiveTargetPulse,
    screenWidthPx: Float,
    screenHeightPx: Float,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "target_pulse_transition")
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "target_ring_scale"
    )

    val targetX = (pulse.xFraction * screenWidthPx).roundToInt()
    val targetY = (pulse.yFraction * screenHeightPx).roundToInt()
    val ringColor = if (pulse.requiresConfirmation) CyberAmber else CyberRose

    Box(modifier = Modifier.fillMaxSize()) {
        // Pulsing Crosshair Ring at exact (X, Y)
        Canvas(
            modifier = Modifier
                .size(76.dp)
                .offset {
                    IntOffset(
                        x = (targetX - 38.dp.roundToPx()).coerceAtLeast(0),
                        y = (targetY - 38.dp.roundToPx()).coerceAtLeast(0)
                    )
                }
        ) {
            val c = Offset(size.width / 2f, size.height / 2f)
            val r = (size.minDimension / 2f) * 0.65f * ringScale
            drawCircle(
                color = ringColor.copy(alpha = 0.28f),
                radius = r
            )
            drawCircle(
                color = ringColor,
                radius = r,
                style = Stroke(width = 4f)
            )
            drawCircle(
                color = Color.White,
                radius = 6f,
                center = c
            )
        }

        // Confirmation Gate Banner or Target Label Pill
        Surface(
            color = Color(0xF5070D19),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.5.dp, ringColor),
            shadowElevation = 10.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 86.dp, start = 16.dp, end = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (pulse.requiresConfirmation) {
                        "🛡️ সেনসিটিভ টাচ পারমিশন: '${pulse.targetLabel}'-এ ক্লিক করব?"
                    } else {
                        "🎯 এআই অটো-টাচ: ${pulse.targetLabel} (${(pulse.xFraction * 100).toInt()}%, ${(pulse.yFraction * 100).toInt()}%)"
                    },
                    color = Color.White,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                if (pulse.requiresConfirmation) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onConfirm,
                            colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "✅ হ্যাঁ, টাচ করুন",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Button(
                            onClick = onCancel,
                            colors = ButtonDefaults.buttonColors(containerColor = CyberRose),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "✕ বাতিল",
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
}

@Composable
private fun VisionSourceChip(
    label: String,
    isSelected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) accent.copy(alpha = 0.26f) else Color(0xFF0F172A),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.2.dp, if (isSelected) accent else Color(0xFF334155)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else Color(0xFF94A3B8),
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun CapabilityToggleCard(
    title: String,
    subtitle: String,
    isEnabled: Boolean,
    accent: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        color = if (isEnabled) accent.copy(alpha = 0.16f) else Color(0xFF0F172A),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.2.dp, if (isEnabled) accent else Color(0xFF334155)),
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
        ) {
            Text(
                text = title,
                color = if (isEnabled) accent else Color(0xFF94A3B8),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = subtitle,
                color = Color(0xFFCBD5E1),
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun QuickLiveCommandChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.8.dp, CyberCyan.copy(alpha = 0.6f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            color = CyberCyan,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
        )
    }
}
