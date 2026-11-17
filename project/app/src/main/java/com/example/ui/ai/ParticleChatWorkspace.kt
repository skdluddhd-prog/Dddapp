package com.example.ui.ai

import android.webkit.WebView
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.ChatSessionManager
import com.example.engine.ParticleChatMessage
import com.example.engine.ParticleSwarmManager
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRose
import com.example.ui.theme.CyberSlate
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// DISTINCT COLOR PALETTE FOR CHAT 2 (PARTICLES DECK)
private val Chat2DarkBg = Color(0xFF090D1A)        // Distinct Cosmic Midnight Indigo
private val Chat2CardBg = Color(0xFF131B2E)        // Elevated Navy Card
private val Chat2Surface = Color(0xFF1E293B)       // Elevated Border Surface
private val Chat2TextPrimary = Color(0xFFF8FAFC)   // Crisp High-Contrast White Text
private val Chat2TextSecondary = Color(0xFF94A3B8) // Muted Slate Blue Text

@Composable
fun ParticleChatWorkspace(
    activeWebView: WebView?,
    onSwitchToMainChat: () -> Unit = {},
    onSwitchToBrowser: () -> Unit = {},
    onToggleVoiceMic: () -> Unit = {},
    isVoiceListening: Boolean = false,
    liveVoiceTranscript: String = ""
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val messages by ParticleSwarmManager.particleMessages.collectAsState()
    val particles by ParticleSwarmManager.particlesList.collectAsState()
    val isLoopRunning by ParticleSwarmManager.isLoopRunning.collectAsState()
    val statusBanner by ParticleSwarmManager.activeParticleStatus.collectAsState()
    val autonomousMission by ParticleSwarmManager.autonomousMissionState.collectAsState()
    val canvasPhysics by ParticleSwarmManager.canvasPhysicsState.collectAsState()

    var userParticleInput by remember { mutableStateOf("") }
    var showAddParticleDialog by remember { mutableStateOf(false) }
    var newParticleName by remember { mutableStateOf("") }
    var newParticleRole by remember { mutableStateOf("") }
    var newParticleDesc by remember { mutableStateOf("") }

    var showStartMissionDialog by remember { mutableStateOf(false) }
    var missionGoalInput by remember { mutableStateOf("") }

    var physicsTick by remember { mutableStateOf(0f) }
    LaunchedEffect(canvasPhysics.isEnabled, canvasPhysics.speedMultiplier) {
        while (canvasPhysics.isEnabled) {
            kotlinx.coroutines.delay(32L)
            physicsTick += 0.045f * canvasPhysics.speedMultiplier
        }
    }

    // Auto scroll to latest message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Voice transcript mirror
    LaunchedEffect(liveVoiceTranscript) {
        val t = liveVoiceTranscript.trim()
        if (t.isNotBlank() && !t.startsWith("🎙️") && !t.startsWith("⏸️") && !t.startsWith("⚡")) {
            userParticleInput = t
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Chat2DarkBg)
            .testTag("particle_chat_workspace")
    ) {
        if (canvasPhysics.isEnabled) {
            val pColor = com.example.engine.UniversalOmniControlEngine.parseColorSafe(canvasPhysics.colorHex, CyberCyan)
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val count = canvasPhysics.particleCount.coerceIn(6, 160)
                val radiusPx = canvasPhysics.particleSizeDp.dp.toPx()
                val w = size.width.coerceAtLeast(100f)
                val h = size.height.coerceAtLeast(100f)
                val pts = ArrayList<androidx.compose.ui.geometry.Offset>(count)
                for (i in 0 until count) {
                    val seed = (i + 1) * 1.37f
                    val pos = when (canvasPhysics.physicsMode) {
                        "VORTEX" -> {
                            val angle = physicsTick * (1.2f + (i % 5) * 0.25f) + seed * 3f
                            val r = ((i.toFloat() / count) * (kotlin.math.min(w, h) * 0.44f))
                            androidx.compose.ui.geometry.Offset(
                                x = w * 0.5f + kotlin.math.cos(angle) * r,
                                y = h * 0.5f + kotlin.math.sin(angle) * r
                            )
                        }
                        "MATRIX_RAIN" -> {
                            val colX = ((i * 73) % 100) / 100f * w
                            val fallY = ((physicsTick * 140f * (0.6f + (i % 4) * 0.3f) + i * 55f) % h)
                            androidx.compose.ui.geometry.Offset(colX, fallY)
                        }
                        "ORBIT" -> {
                            val angle = physicsTick + (i.toFloat() / count) * 6.283f
                            val rx = w * 0.36f
                            val ry = h * 0.28f
                            androidx.compose.ui.geometry.Offset(
                                x = w * 0.5f + kotlin.math.cos(angle) * rx,
                                y = h * 0.5f + kotlin.math.sin(angle) * ry
                            )
                        }
                        else -> {
                            val px = ((kotlin.math.sin(physicsTick * 0.7f + seed) * 0.45f + 0.5f) * w)
                            val py = ((kotlin.math.cos(physicsTick * 0.55f + seed * 1.7f) * 0.45f + 0.5f) * h)
                            androidx.compose.ui.geometry.Offset(px, py)
                        }
                    }
                    pts.add(pos)
                    drawCircle(
                        color = pColor.copy(alpha = 0.35f),
                        radius = radiusPx,
                        center = pos
                    )
                }
                if (canvasPhysics.physicsMode == "NEURAL_WEB" || canvasPhysics.physicsMode == "HYPER_SWARM") {
                    for (i in 0 until pts.size) {
                        val a = pts[i]
                        val b = pts[(i + 1) % pts.size]
                        val dx = a.x - b.x
                        val dy = a.y - b.y
                        if (dx * dx + dy * dy < 42000f) {
                            drawLine(
                                color = pColor.copy(alpha = 0.14f),
                                start = a,
                                end = b,
                                strokeWidth = 1.2f
                            )
                        }
                    }
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
        // TOP HEADER BAR FOR CHAT 2 (DISTINCT DARK CYBER THEME)
        Surface(
            color = Chat2CardBg,
            border = BorderStroke(1.dp, Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(CyberPurple.copy(alpha = 0.25f))
                                .border(1.dp, CyberPurple, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✨", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "✨ চ্যাট ২: পার্টিকলস ল্যাব",
                                    color = Chat2TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                // Distinct color badge
                                Surface(
                                    color = Color(0xFF7C3AED),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "🟣 আলাদা কালার ডেক",
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🔗 চ্যাট ১ এর সাথে যুক্ত • ${particles.size}টি পার্টিকল কণা",
                                    color = CyberCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Action buttons in Header
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Add particle button
                        IconButton(
                            onClick = { showAddParticleDialog = true },
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF2E1065))
                                .border(0.8.dp, CyberPurple, RoundedCornerShape(6.dp))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Particle", tint = Color.White, modifier = Modifier.size(16.dp))
                        }

                        // Clear Chat (keeps memory lean)
                        IconButton(
                            onClick = {
                                ParticleSwarmManager.clearAllMessages(context)
                                Toast.makeText(context, "🧹 চ্যাট ২ পরিষ্কার করা হয়েছে", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF1E293B))
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear Chat", tint = Color(0xFF94A3B8), modifier = Modifier.size(15.dp))
                        }

                        // Switch to Chat 1 (Pure White)
                        Button(
                            onClick = onSwitchToMainChat,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("💬 চ্যাট ১ এ যান", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Status Banner
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = Color(0xFF0B132B),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(0.6.dp, Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = statusBanner,
                        color = Color(0xFF38BDF8),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // AUTONOMOUS MISSION ORCHESTRATION CARD (WHEN ACTIVE)
        if (autonomousMission.isRunning) {
            Surface(
                color = Color(0xFF1E1B4B),
                border = BorderStroke(1.2.dp, Color(0xFFA855F7)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🧠", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "এআই ব্রেন অটো-মিশন সক্রিয় (ধাপ #${autonomousMission.iteration}/12)",
                                color = Color(0xFFE9D5FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "লক্ষ্য: ${autonomousMission.missionGoal}",
                            color = Color(0xFFC084FC),
                            fontSize = 9.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = autonomousMission.statusText,
                            color = Color(0xFFF3E8FF),
                            fontSize = 9.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Button(
                        onClick = {
                            ParticleSwarmManager.stopAutonomousMission(context)
                            Toast.makeText(context, "⏹️ মিশন থামানো হয়েছে", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberRose),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("⏹️ থামান", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // QUICK ACTION CHIPS FOR CHAT 2
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mission Loop Trigger Chip
            ParticleQuickChip(
                label = if (autonomousMission.isRunning) "⏹️ মিশন থামান" else "🧠 এআই অটো-মিশন",
                color = if (autonomousMission.isRunning) CyberRose else Color(0xFFA855F7)
            ) {
                if (autonomousMission.isRunning) {
                    ParticleSwarmManager.stopAutonomousMission(context)
                } else {
                    showStartMissionDialog = true
                }
            }

            ParticleQuickChip(
                label = "🌐 সার্চ ও সার্ফ",
                color = CyberCyan
            ) {
                userParticleInput = "গুগলে সার্চ করো: "
            }

            ParticleQuickChip(
                label = "🖱️ রান বাটনে ক্লিক",
                color = CyberEmerald
            ) {
                ParticleSwarmManager.particleClickButton(context, activeWebView, "Run") { ok, res ->
                    Toast.makeText(context, res, Toast.LENGTH_SHORT).show()
                }
            }

            ParticleQuickChip(
                label = "📜 নিচে স্ক্রোল",
                color = CyberPurple
            ) {
                ParticleSwarmManager.particleScrollWebPage(context, activeWebView, "DOWN", 800)
            }

            ParticleQuickChip(
                label = "📥 পেজ থেকে চ্যাটে পেস্ট",
                color = CyberAmber
            ) {
                ParticleSwarmManager.particleExtractAndPipeToChat(context, activeWebView, "কুইক পেস্ট")
            }

            ParticleQuickChip(
                label = if (isLoopRunning) "⏹️ ২৪ঘ লুপ থামান" else "🔄 ২৪ঘ লুপ শুরু",
                color = if (isLoopRunning) CyberRose else CyberEmerald
            ) {
                ParticleSwarmManager.toggleContinuousLoop(context, activeWebView)
            }

            ParticleQuickChip(
                label = "🎮 স্কাই-হাই 3D গেম (লোগো+শপ+বাগ শিল্ড)",
                color = CyberCyan
            ) {
                val bp = com.example.engine.SovereignPredictiveArchitectEngine.analyzeAndPredictBlueprint(
                    context,
                    "স্কাই-হাই 3D সাইবার-ভার্স এরিনা গেম"
                )
                val html = com.example.engine.SovereignPredictiveArchitectEngine.synthesizeSkyHighMasterpieceHtml(bp)
                val mod = com.example.engine.SelfFeatureModule(
                    id = "sky_3d_cyberverse_game_studio",
                    title = "🎮 স্কাই-হাই 3D সাইবার-ভার্স গেম (লোগো + সাউন্ড + শপ + বাগ-শিল্ড)",
                    description = "৩০০% স্বয়ংক্রিয় প্রিডিক্টিভ 3D গেম",
                    categoryBadge = "SKY-HIGH 3D PRO 👑",
                    jsPayload = html,
                    isAutoRunOnPageLoad = true
                )
                com.example.engine.MasterOmniControllerEngine.addOrUpdateSelfFeature(context, mod)
                if (activeWebView != null) {
                    com.example.engine.MasterOmniControllerEngine.runSelfFeatureOnWebView(context, mod, activeWebView)
                }
                com.example.engine.AutonomousSelfEvolutionEngine.launchModuleInLiveRunner(mod, fullScreen = true)
                ParticleSwarmManager.addParticleMessage(
                    context = context,
                    senderName = "🔮 প্রিডিক্টিভ আর্কিটেক্ট",
                    actionType = "AI_BRAIN",
                    text = com.example.engine.SovereignPredictiveArchitectEngine.formatPredictiveBlueprintReportBangla(bp)
                )
            }

            ParticleQuickChip(
                label = "👑 সব হেভি-পাওয়ার পারমিশন অন",
                color = CyberPurple
            ) {
                val msg = com.example.engine.SovereignPredictiveArchitectEngine.approveAllHeavyCapabilities(context, activeWebView)
                ParticleSwarmManager.addParticleMessage(
                    context = context,
                    senderName = "🔐 স্মার্ট পারমিশন ইঞ্জিন",
                    actionType = "AI_BRAIN",
                    text = msg
                )
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }

        // CHAT MESSAGES LIST (DISTINCT DARK TILES WITH HIGH CONTRAST)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ParticleMessageCard(
                    message = msg,
                    onDelete = {
                        ParticleSwarmManager.deleteMessage(context, msg.id)
                        Toast.makeText(context, "মেসেজ মোছা হয়েছে", Toast.LENGTH_SHORT).show()
                    },
                    onForwardToMainChat = {
                        ChatSessionManager.addMessage(
                            context = context,
                            sender = "AI",
                            text = "✨ [পার্টিকলস চ্যাট ২ থেকে প্রাপ্ত তথ্য]:\n\n${msg.text}",
                            isCode = msg.isCode
                        )
                        Toast.makeText(context, "📤 মূল এআই চ্যাট ১-এ পাঠানো হয়েছে!", Toast.LENGTH_SHORT).show()
                    },
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(msg.text))
                        Toast.makeText(context, "📋 কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                    },
                    onEvaluateWithAiBrain = {
                        scope.launch {
                            Toast.makeText(context, "🧠 এআই ব্রেন দিয়ে যাচাই শুরু...", Toast.LENGTH_SHORT).show()
                            ParticleSwarmManager.startAutonomousMission(
                                context = context,
                                webView = activeWebView,
                                userGoal = "এই প্রাপ্ত কোডটি যাচাই ও রান করে নিশ্চিত করো: ${msg.text.take(100)}"
                            )
                        }
                    }
                )
            }
        }

        // BOTTOM INPUT CONSOLE (HIGH CONTRAST DARK THEME)
        Surface(
            color = Chat2CardBg,
            border = BorderStroke(1.dp, Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Safe in-app Mic button
                    IconButton(
                        onClick = onToggleVoiceMic,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isVoiceListening) CyberRose.copy(alpha = 0.3f) else Color(0xFF1E293B))
                            .border(1.dp, if (isVoiceListening) CyberRose else Color(0xFF38BDF8), RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = if (isVoiceListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = "Mic",
                            tint = if (isVoiceListening) CyberRose else CyberCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    OutlinedTextField(
                        value = userParticleInput,
                        onValueChange = { userParticleInput = it },
                        placeholder = {
                            Text(
                                "পার্টিকল ও এআই ব্রেনকে আদেশ দিন (যেমন: 'কোড আনো ঠিক না হওয়া পর্যন্ত')...",
                                color = Chat2TextSecondary,
                                fontSize = 11.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A),
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("particle_chat_input_field")
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            val cmd = userParticleInput.trim()
                            if (cmd.isNotBlank()) {
                                userParticleInput = ""
                                ParticleSwarmManager.addParticleMessage(
                                    context = context,
                                    senderName = "YOU (ইউজার)",
                                    actionType = "USER",
                                    text = cmd
                                )
                                scope.launch {
                                    val omniSummary = com.example.engine.UniversalOmniControlEngine.processNaturalLanguageOrJsonCommand(
                                        context = context,
                                        rawText = cmd
                                    )
                                    if (omniSummary != null) {
                                        ParticleSwarmManager.addParticleMessage(
                                            context = context,
                                            senderName = "👑 মেইন অমনি কন্ট্রোলার",
                                            actionType = "AI_BRAIN",
                                            text = omniSummary
                                        )
                                    }
                                    ParticleSwarmManager.dispatchParticleCommandByAi(
                                        context = context,
                                        webView = activeWebView,
                                        commandText = cmd
                                    ) { reply ->
                                        ParticleSwarmManager.addParticleMessage(
                                            context = context,
                                            senderName = "✨ পার্টিকল কন্ট্রোলার",
                                            actionType = "AI_BRAIN",
                                            text = reply
                                        )
                                    }
                                }
                            }
                        },
                        enabled = userParticleInput.isNotBlank(),
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (userParticleInput.isNotBlank()) CyberCyan else Color(0xFF1E293B))
                            .testTag("particle_chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (userParticleInput.isNotBlank()) Color(0xFF0F172A) else Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
        }
    }

    // DIALOG 1: START AUTONOMOUS AI-PARTICLE MISSION
    if (showStartMissionDialog) {
        AlertDialog(
            onDismissRequest = { showStartMissionDialog = false },
            title = {
                Text(
                    text = "🧠 এআই ব্রেন অটো-মিশন শুরু করুন",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "এআই ব্রেন পার্টিকলসদের সাইটে পাঠাবে, কোড আনবে এবং নিজে যাচাই করে যতক্ষণ পারফেক্ট না হয় ততক্ষণ স্বয়ংক্রিয় লুপ চালাবে।",
                        color = Color(0xFFCBD5E1),
                        fontSize = 10.sp
                    )
                    OutlinedTextField(
                        value = missionGoalInput,
                        onValueChange = { missionGoalInput = it },
                        label = { Text("মিশন বা কাঙ্ক্ষিত কোড", color = Color(0xFF38BDF8)) },
                        placeholder = { Text("যেমন: Python Leetcode TwoSum কোড আনো এবং রান করো", color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedContainerColor = Color(0xFF1E293B),
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = Color(0xFF475569)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val g = missionGoalInput.trim().ifBlank { "ওয়েবসাইট থেকে সঠিক ও সম্পূর্ণ কোড সংগ্রহ ও যাচাই" }
                        ParticleSwarmManager.startAutonomousMission(context, activeWebView, g)
                        Toast.makeText(context, "🚀 অটো-মিশন শুরু হয়েছে!", Toast.LENGTH_SHORT).show()
                        showStartMissionDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                ) {
                    Text("মিশন শুরু করুন", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartMissionDialog = false }) {
                    Text("বাতিল", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF131B2E)
        )
    }

    // DIALOG 2: CREATE EXTRA CUSTOM PARTICLE
    if (showAddParticleDialog) {
        AlertDialog(
            onDismissRequest = { showAddParticleDialog = false },
            title = {
                Text(
                    text = "➕ নতুন পার্টিকল তৈরি করুন",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "নতুন পার্টিকল কণা তৈরি করে যেকোনো নির্দিষ্ট ওয়েবসাইট বা অটোমেশন টাস্কের দায়িত্ব দিন।",
                        color = Color(0xFFCBD5E1),
                        fontSize = 10.sp
                    )
                    OutlinedTextField(
                        value = newParticleName,
                        onValueChange = { newParticleName = it },
                        label = { Text("পার্টিকলের নাম", color = Color(0xFF38BDF8)) },
                        placeholder = { Text("যেমন: পার্টিকল ৬ (গিটহাব ক্লোনার)", color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedContainerColor = Color(0xFF1E293B)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newParticleRole,
                        onValueChange = { newParticleRole = it },
                        label = { Text("রোল ব্যাজ", color = Color(0xFF38BDF8)) },
                        placeholder = { Text("যেমন: ⚡ CLONER", color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedContainerColor = Color(0xFF1E293B)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newParticleDesc,
                        onValueChange = { newParticleDesc = it },
                        label = { Text("কাজের বিবরণ", color = Color(0xFF38BDF8)) },
                        placeholder = { Text("যেমন: পেজ স্ক্রোল করে নির্দিষ্ট কোড সংগ্রহ করবে", color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedContainerColor = Color(0xFF1E293B)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newParticleName.isNotBlank()) {
                            ParticleSwarmManager.createExtraCustomParticle(
                                context = context,
                                name = newParticleName.trim(),
                                roleBadge = newParticleRole.trim(),
                                description = newParticleDesc.trim()
                            )
                            Toast.makeText(context, "✅ নতুন পার্টিকল যুক্ত হয়েছে!", Toast.LENGTH_SHORT).show()
                            newParticleName = ""
                            newParticleRole = ""
                            newParticleDesc = ""
                            showAddParticleDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPurple)
                ) {
                    Text("তৈরি করুন", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddParticleDialog = false }) {
                    Text("বাতিল", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF131B2E)
        )
    }
}

@Composable
private fun ParticleQuickChip(
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        color = color.copy(alpha = 0.18f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(0.8.dp, color.copy(alpha = 0.6f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ParticleMessageCard(
    message: ParticleChatMessage,
    onDelete: () -> Unit,
    onForwardToMainChat: () -> Unit,
    onCopy: () -> Unit,
    onEvaluateWithAiBrain: () -> Unit = {}
) {
    val isUser = message.actionType == "USER"
    val isAiBrain = message.actionType == "AI_BRAIN"
    val isSuccessVerified = message.text.contains("কোড ভেরিফাইড") || message.text.contains("মিশন সফল")
    val isRetryRequired = message.text.contains("রিট্রাই প্রয়োজন") || message.text.contains("কোড অসম্পূর্ণ")

    val timeStr = remember(message.timestamp) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))
    }

    val cardBg = when {
        isUser -> Color(0xFF1E3A8A) // Vibrant Cobalt Blue for User
        isSuccessVerified -> Color(0xFF064E3B) // Dark Emerald for Success
        isRetryRequired -> Color(0xFF451A03) // Dark Amber for Retry
        isAiBrain -> Color(0xFF2E1065) // Dark Purple for AI Brain
        else -> Chat2CardBg // Dark Navy for Particles
    }

    val cardBorder = when {
        isUser -> BorderStroke(1.dp, Color(0xFF3B82F6))
        isSuccessVerified -> BorderStroke(1.2.dp, CyberEmerald)
        isRetryRequired -> BorderStroke(1.2.dp, CyberAmber)
        isAiBrain -> BorderStroke(1.dp, CyberPurple)
        else -> BorderStroke(0.8.dp, Color(0xFF334155))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = cardBorder,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header Row: Sender + Action + Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = message.senderName,
                        color = when {
                            isUser -> Color.White
                            isSuccessVerified -> Color(0xFF6EE7B7)
                            isRetryRequired -> Color(0xFFFDE68A)
                            isAiBrain -> Color(0xFFE9D5FF)
                            else -> CyberCyan
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = when (message.actionType) {
                            "SEARCH" -> CyberCyan.copy(alpha = 0.25f)
                            "CLICK" -> CyberEmerald.copy(alpha = 0.25f)
                            "SCROLL" -> CyberPurple.copy(alpha = 0.25f)
                            "EXTRACT" -> CyberAmber.copy(alpha = 0.25f)
                            "AI_BRAIN" -> Color(0xFF7C3AED)
                            "LOOP_TICK" -> CyberRose.copy(alpha = 0.25f)
                            else -> Color(0xFF334155)
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = message.actionType,
                            color = when (message.actionType) {
                                "SEARCH" -> CyberCyan
                                "CLICK" -> CyberEmerald
                                "SCROLL" -> CyberPurple
                                "EXTRACT" -> CyberAmber
                                "AI_BRAIN" -> Color.White
                                "LOOP_TICK" -> CyberRose
                                else -> Color(0xFFCBD5E1)
                            },
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = timeStr,
                    color = Color(0xFF94A3B8),
                    fontSize = 9.sp
                )
            }

            // Target URL if present
            if (message.targetUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🌐 ${message.targetUrl}",
                    color = CyberCyan,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Body text / code
            if (message.isCode) {
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(0.8.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = message.text,
                        color = Color(0xFFF8FAFC),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            } else {
                Text(
                    text = message.text,
                    color = Color(0xFFF1F5F9),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // If it's an extracted text or code, offer 1-click evaluation by AI Brain
                if (!isUser && !isAiBrain && message.text.length > 30) {
                    Surface(
                        color = Color(0xFF7C3AED),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.clickable { onEvaluateWithAiBrain() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text("🤖 এআই দিয়ে যাচাই", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                // Forward to Main Chat button (Chat 1)
                if (!isUser) {
                    Surface(
                        color = Color(0xFF0284C7),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.clickable { onForwardToMainChat() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text("📤 চ্যাট ১ এ পাঠান", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                // Copy
                IconButton(onClick = onCopy, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFF94A3B8), modifier = Modifier.size(13.dp))
                }

                // Delete message to free memory
                IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CyberRose, modifier = Modifier.size(13.dp))
                }
            }
        }
    }
}
