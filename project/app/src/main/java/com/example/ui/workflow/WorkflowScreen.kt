package com.example.ui.workflow

import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.engine.DataCheckpoint
import com.example.engine.NanobotSwarmOrchestrator
import com.example.model.NanobotRelayEvent
import com.example.model.NanobotRole
import com.example.model.WorkflowNode
import kotlinx.coroutines.launch

@Composable
fun WorkflowScreen(
    sourceWebView: WebView?,
    targetWebView: WebView?,
    onRouteApprovedToTabB: (String) -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var canvasViewRef by remember { mutableStateOf<NodeCanvasView?>(null) }
    var selectedNodeForSheet by remember { mutableStateOf<WorkflowNode?>(null) }
    var canvasVersionTrigger by remember { mutableIntStateOf(0) }
    var showCheckpointApprovalDialog by remember { mutableStateOf(false) }
    var checkpointPreviewText by remember { mutableStateOf("") }
    var showMidFlightEditor by remember { mutableStateOf(false) }
    var showRuleAndPlannerSheet by remember { mutableStateOf(false) }
    var ruleAndPlannerInitialTab by remember { mutableIntStateOf(0) }
    var showLiveSandboxSheet by remember { mutableStateOf(false) }
    var taskPromptDraft by remember {
        mutableStateOf("এই প্রজেক্ট বা বিষয়ের মূল কোড ও আর্কিটেকচার তৈরি করো এবং কোনো বাগ থাকলে ডিপ-থিংক করে সমাধান দাও।")
    }

    val relayFeed by NanobotSwarmOrchestrator.relayFeed.collectAsState()
    val isSwarmRunning by NanobotSwarmOrchestrator.isSwarmRunning.collectAsState()
    val isContinuousLoop by NanobotSwarmOrchestrator.isContinuousLoop.collectAsState()
    val activeBotId by NanobotSwarmOrchestrator.activeBotId.collectAsState()
    val statusBanner by NanobotSwarmOrchestrator.activeStatusBanner.collectAsState()
    val pendingPayload by NanobotSwarmOrchestrator.pendingMidFlightPayload.collectAsState()
    val stepCounter by NanobotSwarmOrchestrator.stepCounter.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050811))
            .testTag("workflow_screen_root")
    ) {
        // 1. Top Bar: Back + Title + Continuous Loop Switch
        Surface(
            color = Color(0xFF0B1120),
            border = BorderStroke(1.dp, Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(36.dp).testTag("workflow_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF00F0FF)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "🤖 ন্যানোবট সোয়ার্ম ও ডিপ-থিংক চেইন স্টুডিও",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "ধাপ #$stepCounter • যেকোনো বটকে ধরুন, রোল দিন, ঘোরান ও অটো-রিলে চালান",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isContinuousLoop) "🔄 লুপ ON" else "১-রাউন্ড",
                            color = if (isContinuousLoop) Color(0xFF10B981) else Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(
                            checked = isContinuousLoop,
                            onCheckedChange = { NanobotSwarmOrchestrator.setContinuousLoopMode(it) },
                            modifier = Modifier.testTag("continuous_loop_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Quick Spawn Role Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SpawnBotChip(
                        label = "+ 🌐 ওয়েব মেসেঞ্জার বট",
                        color = Color(0xFF00F0FF),
                        onClick = {
                            canvasViewRef?.spawnNode(NanobotRole.WEB_MESSENGER)
                            canvasVersionTrigger++
                        }
                    )
                    SpawnBotChip(
                        label = "+ 🧠 ডিপ-থিংকার বট",
                        color = Color(0xFFA855F7),
                        onClick = {
                            canvasViewRef?.spawnNode(NanobotRole.DEEP_THINKER)
                            canvasVersionTrigger++
                        }
                    )
                    SpawnBotChip(
                        label = "+ 🔍 ভেরিফায়ার বট",
                        color = Color(0xFFF59E0B),
                        onClick = {
                            canvasViewRef?.spawnNode(NanobotRole.CRITIC_VERIFIER)
                            canvasVersionTrigger++
                        }
                    )
                    SpawnBotChip(
                        label = "+ ⚡ অ্যাকশন বট",
                        color = Color(0xFF10B981),
                        onClick = {
                            canvasViewRef?.spawnNode(NanobotRole.ACTION_COMPILER)
                            canvasVersionTrigger++
                        }
                    )
                    SpawnBotChip(
                        label = "+ 🤖 নিজস্ব কাস্টম বট",
                        color = Color(0xFFEC4899),
                        onClick = {
                            canvasViewRef?.spawnNode(NanobotRole.CUSTOM_AGENT)
                            canvasVersionTrigger++
                        }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Master Power-Tools Bar: Global Rule Vault, 1-Prompt AI Planner, Auto-Search Crawler, Shared Memory & Sandbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SpawnBotChip(
                        label = "📜 অটো-রুল ভল্ট (Auto-Paste)",
                        color = Color(0xFF10B981),
                        onClick = {
                            ruleAndPlannerInitialTab = 0
                            showRuleAndPlannerSheet = true
                        }
                    )
                    SpawnBotChip(
                        label = "🧠 ১-ক্লিক AI মাস্টার প্ল্যানার",
                        color = Color(0xFFA855F7),
                        onClick = {
                            ruleAndPlannerInitialTab = 1
                            showRuleAndPlannerSheet = true
                        }
                    )
                    SpawnBotChip(
                        label = "🔍 প্যারালাল অটো-সার্চ ক্রলার",
                        color = Color(0xFF00F0FF),
                        onClick = {
                            ruleAndPlannerInitialTab = 2
                            showRuleAndPlannerSheet = true
                        }
                    )
                    SpawnBotChip(
                        label = "🗄️ যৌথ বট মেমোরি ভল্ট",
                        color = Color(0xFFF59E0B),
                        onClick = {
                            ruleAndPlannerInitialTab = 3
                            showRuleAndPlannerSheet = true
                        }
                    )
                    SpawnBotChip(
                        label = "🧪 লাইভ কোড স্যান্ডবক্স",
                        color = Color(0xFF38BDF8),
                        onClick = {
                            showLiveSandboxSheet = true
                        }
                    )
                }
            }
        }

        // 2. Live Status Banner
        Surface(
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0x3300F0FF)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isSwarmRunning) Color(0xFF10B981) else Color(0xFF00F0FF))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = statusBanner,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = if (showMidFlightEditor) "ক্যানভাস দেখুন" else "✏️ টাস্ক/মেসেজ কন্ট্রোল",
                    color = Color(0xFF00F0FF),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E293B))
                        .clickable { showMidFlightEditor = !showMidFlightEditor }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("toggle_midflight_editor_button")
                )
            }
        }

        // 3. Optional Expandable Task / Mid-Flight Message Editor & Bot Redirector
        if (showMidFlightEditor) {
            Surface(
                color = Color(0xFF0B1324),
                border = BorderStroke(1.dp, Color(0xFFA855F7)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "🎯 সরাসরি টাস্ক দিন বা মাঝপথে বটের মেসেজ নিজের হাতে পরিবর্তন করুন (ওয়েবসাইটে না ঢুকেই):",
                        color = Color(0xFFE9D5FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = if (pendingPayload.isNotBlank()) pendingPayload else taskPromptDraft,
                        onValueChange = {
                            taskPromptDraft = it
                            NanobotSwarmOrchestrator.updateMidFlightPayload(it)
                        },
                        label = { Text("ন্যানোবটকে টাস্ক / মেসেজ দিন (সে নিজে ওয়েবসাইটে পাঠাবে বা ডিপ-থিংক করবে)") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth().testTag("swarm_task_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00F0FF),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Bot Redirection Row (Choose which Bot acts next)
                    val currentNodes = remember(canvasVersionTrigger, canvasViewRef) {
                        canvasViewRef?.nodes?.filter { !it.isHomeNode }.orEmpty()
                    }
                    if (currentNodes.isNotEmpty()) {
                        Text(
                            text = "🔀 এখন কোন ন্যানোবটকে দিয়ে কাজ শুরু/রিডাইরেক্ট করবেন? (ট্যাপ করুন):",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            currentNodes.forEach { bot ->
                                val isCurrent = activeBotId == bot.id
                                Surface(
                                    color = if (isCurrent) Color(0xFF0284C7) else Color(0xFF1E293B),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isCurrent) Color(0xFF00F0FF) else Color(0xFF334155)
                                    ),
                                    modifier = Modifier.clickable {
                                        NanobotSwarmOrchestrator.redirectNextStepToBot(bot)
                                    }
                                ) {
                                    Text(
                                        text = "🤖 ${bot.title} (${bot.customRoleName.ifBlank { bot.nanobotRole.badge }})",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. 2D Visual Node Canvas (Drag & Drop Nanobots, Tap to Assign Custom Role)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.46f)
                .padding(horizontal = 10.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            AndroidView(
                factory = { ctx ->
                    NodeCanvasView(ctx).apply {
                        onNodeSelectedListener = { clickedNode ->
                            selectedNodeForSheet = clickedNode
                        }
                        canvasViewRef = this
                        post { canvasVersionTrigger++ }
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("node_canvas_view")
            )

            // Hint overlay at top-left of canvas
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xCC0F172A))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "💡 যেকোনো বট টেনে ঘোরান • বটের ওপর ট্যাপ করে নিজস্ব রোল ও সাইট দিন",
                    color = Color(0xFF38BDF8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 5. Master Execution Control Bar (Step-by-Step, Continuous Auto Relay, Pause/Stop)
        Surface(
            color = Color(0xFF0B1120),
            border = BorderStroke(1.dp, Color(0xFF1E293B)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Single Step Button (এক ধাপ করে আগান)
                Button(
                    onClick = {
                        val cv = canvasViewRef ?: return@Button
                        if (pendingPayload.isBlank() && taskPromptDraft.isNotBlank()) {
                            NanobotSwarmOrchestrator.updateMidFlightPayload(taskPromptDraft)
                        }
                        NanobotSwarmOrchestrator.executeSingleStep(
                            context = context,
                            nodes = cv.nodes,
                            connections = cv.connections,
                            webViewA = sourceWebView,
                            webViewB = targetWebView,
                            onCanvasInvalidate = { cv.invalidate() },
                            onCheckpointReached = { preview ->
                                checkpointPreviewText = preview.take(350)
                                showCheckpointApprovalDialog = true
                            }
                        )
                    },
                    enabled = !isSwarmRunning,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(40.dp).testTag("single_step_button")
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("১ ধাপ আগান", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }

                // Continuous Auto-Relay Button (অবিরাম চেইন রান)
                Button(
                    onClick = {
                        val cv = canvasViewRef ?: return@Button
                        if (pendingPayload.isBlank() && taskPromptDraft.isNotBlank()) {
                            NanobotSwarmOrchestrator.updateMidFlightPayload(taskPromptDraft)
                        }
                        NanobotSwarmOrchestrator.startContinuousRelay(
                            context = context,
                            nodes = cv.nodes,
                            connections = cv.connections,
                            webViewA = sourceWebView,
                            webViewB = targetWebView,
                            maxRelaySteps = 12,
                            onCanvasInvalidate = { cv.invalidate() },
                            onCheckpointReached = { preview ->
                                checkpointPreviewText = preview.take(350)
                                showCheckpointApprovalDialog = true
                            }
                        )
                    },
                    enabled = !isSwarmRunning,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1.2f).height(40.dp).testTag("run_continuous_swarm_button")
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSwarmRunning) "চলছে..." else "অটো চেইন রান",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Pause / Stop Button
                OutlinedButton(
                    onClick = {
                        val cv = canvasViewRef
                        if (isSwarmRunning) {
                            NanobotSwarmOrchestrator.pauseSwarm()
                        } else if (cv != null) {
                            NanobotSwarmOrchestrator.stopSwarm(cv.nodes) { cv.invalidate() }
                        }
                    },
                    border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(40.dp).testTag("pause_stop_swarm_button")
                ) {
                    Icon(
                        imageVector = if (isSwarmRunning) Icons.Default.Pause else Icons.Default.Refresh,
                        contentDescription = "Pause or Reset",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSwarmRunning) "পজ" else "রিসেট",
                        color = Color(0xFFF59E0B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 6. Live On-Screen Bot Relay Feed (স্ক্রিনে সরাসরি প্রতিটি বটের মেসেজ, রিপ্লাই ও ডিপ-থিংক ফলাফল)
        Surface(
            color = Color(0xFF090E1A),
            border = BorderStroke(1.dp, Color(0xFF1E293B)),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.54f)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📺 লাইভ ন্যানোবট স্ক্রিন ফিড (কে কী আনল ও কী ডিপ-থিংক করল)",
                        color = Color(0xFF00F0FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (relayFeed.isNotEmpty()) {
                        Text(
                            text = "ক্লিয়ার",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            modifier = Modifier.clickable {
                                NanobotSwarmOrchestrator.clearRelayFeed()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (relayFeed.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "উপরে '১ ধাপ আগান' বা 'অটো চেইন রান' চাপুন।\nBot 1 ওয়েবসাইটে মেসেজ করে রিপ্লাই আনবে -> স্ক্রিনে দেখাবে -> Bot 2 সেটি ডিপ-থিংক করে Bot 3-কে দেবে!",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize().testTag("nanobot_relay_feed_list")
                    ) {
                        items(relayFeed, key = { it.id }) { event ->
                            RelayEventCard(
                                event = event,
                                onEditAndPassToNext = { textToEdit ->
                                    taskPromptDraft = textToEdit
                                    NanobotSwarmOrchestrator.updateMidFlightPayload(textToEdit)
                                    showMidFlightEditor = true
                                },
                                onSendToMainScreen = { output ->
                                    onRouteApprovedToTabB(output)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Node Role & Config Sheet
    selectedNodeForSheet?.let { node ->
        val allNodes = canvasViewRef?.nodes?.toList().orEmpty()
        WorkflowBottomSheet(
            node = node,
            allNodes = allNodes,
            onDismiss = { selectedNodeForSheet = null },
            onDeleteNode = { id ->
                canvasViewRef?.deleteNode(id)
                canvasVersionTrigger++
            },
            onRunThisBotNow = { botToRun ->
                val cv = canvasViewRef ?: return@WorkflowBottomSheet
                NanobotSwarmOrchestrator.redirectNextStepToBot(botToRun)
                NanobotSwarmOrchestrator.executeSingleStep(
                    context = context,
                    nodes = cv.nodes,
                    connections = cv.connections,
                    webViewA = sourceWebView,
                    webViewB = targetWebView,
                    onCanvasInvalidate = { cv.invalidate() },
                    onCheckpointReached = { preview ->
                        checkpointPreviewText = preview.take(350)
                        showCheckpointApprovalDialog = true
                    }
                )
            },
            onSaveConfig = { title, role, roleName, rolePrompt, initMsg, nextId, rule, param, power, isCloud, webhook, target, loop, interval, flush ->
                canvasViewRef?.updateSelectedNodeConfig(
                    title = title,
                    nanobotRole = role,
                    customRoleName = roleName,
                    customRolePrompt = rolePrompt,
                    initialMessageToSend = initMsg,
                    nextTargetNodeId = nextId,
                    rule = rule,
                    customParam = param,
                    brainPower = power,
                    isCloud = isCloud,
                    webhookUrl = webhook,
                    targetInput = target,
                    is24x7Loop = loop,
                    loopIntervalMinutes = interval,
                    autoFlushMemory = flush
                )
                canvasVersionTrigger++
            }
        )
    }

    // Home Base Checkpoint Approval Dialog
    if (showCheckpointApprovalDialog) {
        AlertDialog(
            onDismissRequest = { showCheckpointApprovalDialog = false },
            containerColor = Color(0xFF0F172A),
            title = {
                Text(
                    "🏰 Home Base Checkpoint (Paused)",
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        "ন্যানোবট চেইনের চূড়ান্ত আউটপুট ডিস্কে প্রস্তুত। আপনি কি এটি মূল স্ক্রিনে / Tab B-তে পাঠাতে চান?",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFF070B14),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Text(
                            text = checkpointPreviewText.ifBlank { DataCheckpoint.currentState.previewSnippet },
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCheckpointApprovalDialog = false
                        val fullText = DataCheckpoint.currentState.checkpointFile?.takeIf { it.exists() }?.readText()
                            ?: pendingPayload
                        if (targetWebView != null) {
                            scope.launch {
                                DataCheckpoint.approveAndRoute(context, targetWebView) {
                                    onRouteApprovedToTabB(fullText)
                                }
                            }
                        } else {
                            onRouteApprovedToTabB(fullText)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Approve & Route to Tab B", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        DataCheckpoint.rejectCheckpoint()
                        showCheckpointApprovalDialog = false
                    }
                ) {
                    Text("Reject / Clear", color = Color(0xFFF43F5E))
                }
            }
        )
    }

    if (showRuleAndPlannerSheet) {
        GlobalRuleAndPlannerSheet(
            initialTab = ruleAndPlannerInitialTab,
            activeWebView = sourceWebView ?: targetWebView,
            onApplySwarmBlueprint = { blueprint, autoStart ->
                val cv = canvasViewRef
                if (cv != null) {
                    cv.applyBlueprint(blueprint.nodes, blueprint.connections)
                    canvasVersionTrigger++
                    val firstWorker = cv.nodes.firstOrNull { !it.isHomeNode }
                    if (firstWorker != null) {
                        taskPromptDraft = firstWorker.initialMessageToSend.ifBlank { taskPromptDraft }
                        NanobotSwarmOrchestrator.updateMidFlightPayload(taskPromptDraft)
                    }
                    if (autoStart) {
                        NanobotSwarmOrchestrator.startContinuousRelay(
                            context = context,
                            nodes = cv.nodes,
                            connections = cv.connections,
                            webViewA = sourceWebView,
                            webViewB = targetWebView,
                            onCanvasInvalidate = {
                                cv.postInvalidate()
                                canvasVersionTrigger++
                            },
                            onCheckpointReached = { finalOut ->
                                checkpointPreviewText = finalOut.take(300)
                                showCheckpointApprovalDialog = true
                            }
                        )
                    }
                }
                showRuleAndPlannerSheet = false
            },
            onInjectTextToSwarm = { injected ->
                taskPromptDraft = injected
                showRuleAndPlannerSheet = false
            },
            onDismiss = { showRuleAndPlannerSheet = false }
        )
    }

    if (showLiveSandboxSheet) {
        val latestCodeOrReply = relayFeed.firstOrNull()?.replyOrThoughtOutput
            ?.takeIf { it.isNotBlank() }
            ?: pendingPayload
        LiveSandboxSheet(
            initialOutputOrCode = latestCodeOrReply,
            onSendToTabB = { codeToRoute ->
                onRouteApprovedToTabB(codeToRoute)
            },
            onDismiss = { showLiveSandboxSheet = false }
        )
    }
}

@Composable
private fun SpawnBotChip(
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        color = color.copy(alpha = 0.14f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun RelayEventCard(
    event: NanobotRelayEvent,
    onEditAndPassToNext: (String) -> Unit,
    onSendToMainScreen: (String) -> Unit
) {
    Surface(
        color = Color(0xFF11192C),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF1E3A8A)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0284C7))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ধাপ #${event.stepNumber}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${event.botTitle} • ${event.roleLabel}",
                        color = Color(0xFF00F0FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Text(
                    text = "➡️ ${event.nextBotTitle}",
                    color = Color(0xFF10B981),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "🌐 টার্গেট: ${event.targetWebsiteOrAi}",
                color = Color(0xFF94A3B8),
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "📤 পাঠানো মেসেজ/ইনপুট: ${event.messageSent.take(180)}",
                color = Color(0xFFCBD5E1),
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = Color(0xFF060A12),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0x3310B981)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = event.replyOrThoughtOutput.take(900),
                    color = Color(0xFFA7F3D0),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✏️ এডিট করে পরের বটকে দিন",
                    color = Color(0xFFF59E0B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E293B))
                        .clickable { onEditAndPassToNext(event.replyOrThoughtOutput) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "📺 মূল স্ক্রিনে দেখান / Tab B",
                    color = Color(0xFF00F0FF),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E293B))
                        .clickable { onSendToMainScreen(event.replyOrThoughtOutput) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
