package com.example.ui.ai

import android.webkit.WebView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.AiChromiumDevEngine
import com.example.engine.AiSelfAwarenessScope
import com.example.engine.FileStreamHelper
import com.example.engine.MasterOmniControllerEngine
import com.example.engine.QuadChatAndStrainerEngine
import com.example.engine.StrainedStepStatus
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRose
import com.example.ui.theme.CyberSlate
import com.example.ui.theme.cyberAmbientBackground
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuadChatWorkspaceSheet(
    initialChamber: Int = 0,
    activeWebView: WebView?,
    liveVoiceTranscript: String = "",
    isVoiceListening: Boolean = false,
    onToggleVoiceMic: () -> Unit = {},
    onOpenSandboxWithPayload: (String) -> Unit,
    onOpenClassicSessionDrawer: () -> Unit,
    onOpenRemixedTaskLoop: () -> Unit = {},
    onOpenRemixedSummarizer: () -> Unit = {},
    onOpenSovereignDevConsole: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedChamber by remember { mutableIntStateOf(initialChamber.coerceIn(0, 3)) }

    val directMessages by QuadChatAndStrainerEngine.directMessages.collectAsState()
    val strainedSteps by QuadChatAndStrainerEngine.strainedSteps.collectAsState()
    val activeProjectTitle by QuadChatAndStrainerEngine.activeProjectTitle.collectAsState()
    val stagingItems by QuadChatAndStrainerEngine.stagingItems.collectAsState()
    val chamberFourItems by QuadChatAndStrainerEngine.chamberFourItems.collectAsState()
    val isStrainingOrRunning by QuadChatAndStrainerEngine.isStrainingOrRunning.collectAsState()
    val is24x7DaemonActive by QuadChatAndStrainerEngine.is24x7DaemonActive.collectAsState()
    val heartbeatCount by QuadChatAndStrainerEngine.heartbeatCount.collectAsState()
    val statusBanner by QuadChatAndStrainerEngine.statusBanner.collectAsState()
    val zeroPressureTelemetry by FileStreamHelper.telemetry.collectAsState()
    val savedSelfFeatures by MasterOmniControllerEngine.savedSelfFeatures.collectAsState()
    val awarenessScope by MasterOmniControllerEngine.selfAwarenessScope.collectAsState()

    var chatOneInput by remember { mutableStateOf("") }
    var manualPasteTitle by remember { mutableStateOf("") }
    var manualPasteSource by remember { mutableStateOf("ChatGPT / Claude") }
    var manualPasteContent by remember { mutableStateOf("") }

    // If user speaks while inside the 4-Chamber Chat, automatically mirror the speech into chatOneInput!
    androidx.compose.runtime.LaunchedEffect(liveVoiceTranscript) {
        if (liveVoiceTranscript.isNotBlank()) {
            chatOneInput = liveVoiceTranscript
        }
    }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        FileStreamHelper.refreshTelemetry(context)
    }

    val completedCount = strainedSteps.count { it.status == StrainedStepStatus.COMPLETED }
    val failedOrRemainingCount = strainedSteps.size - completedCount

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = CyberDarkBg,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.testTag("quad_chat_workspace_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.94f)
                .cyberAmbientBackground()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            // Top Title + Classic Session Drawer Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "💬 ৪-চেম্বার অমনি চ্যাট, ছাঁকনি (✅/❌) ও পেস্ট ভল্ট",
                        color = CyberCyan,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = statusBanner,
                        color = CyberEmerald,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(
                        onClick = onOpenSovereignDevConsole,
                        border = BorderStroke(1.dp, CyberPurple.copy(alpha = 0.7f))
                    ) {
                        Text("🛠️ ডেভ ও বাটন", color = CyberPurple, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            // COMPACT HERITAGE 3-CHAT LAUNCHER + PROPRIOCEPTIVE AI SCOPE SELECTOR BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // AI Self-Anatomy Scope Toggle Chip
                Surface(
                    color = CyberCardBg,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, CyberCyan),
                    modifier = Modifier.clickable {
                        val nextScope = when (awarenessScope) {
                            AiSelfAwarenessScope.MAIN_AI_ONLY -> AiSelfAwarenessScope.ALL_AI_AND_NANOBOTS
                            AiSelfAwarenessScope.ALL_AI_AND_NANOBOTS -> AiSelfAwarenessScope.MANUAL_OFF
                            AiSelfAwarenessScope.MANUAL_OFF -> AiSelfAwarenessScope.MAIN_AI_ONLY
                        }
                        MasterOmniControllerEngine.setSelfAwarenessScope(context, nextScope)
                    }
                ) {
                    Text(
                        text = "🧠 অ্যাপ ম্যাপ: ${awarenessScope.titleBangla}",
                        color = CyberCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }

                // Earlier Remix Chat 1: Blank Canvas & Drive/Gmail Backup
                Surface(
                    color = CyberCardBg,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, CyberEmerald.copy(alpha = 0.7f)),
                    modifier = Modifier.clickable { onOpenClassicSessionDrawer() }
                ) {
                    Text(
                        text = "☁️ রিমিক্স চ্যাট ১: ব্লাঙ্ক ক্যানভাস ও ড্রাইভ ব্যাকআপ",
                        color = CyberEmerald,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }

                // Earlier Remix Chat 2: Tab A -> Tab B 3-Strike Auto-Loop
                Surface(
                    color = CyberCardBg,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, CyberPurple.copy(alpha = 0.7f)),
                    modifier = Modifier.clickable { onOpenRemixedTaskLoop() }
                ) {
                    Text(
                        text = "🔄 রিমিক্স চ্যাট ২: Tab A➔B ৩-স্ট্রাইক অটো-লুপ",
                        color = CyberPurple,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }

                // Earlier Remix Chat 3: Anti-Red-Flag Deep Analyzer & Summarizer
                Surface(
                    color = CyberCardBg,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, CyberAmber.copy(alpha = 0.7f)),
                    modifier = Modifier.clickable { onOpenRemixedSummarizer() }
                ) {
                    Text(
                        text = "📑 রিমিক্স চ্যাট ৩: রেড-ফ্ল্যাগ-মুক্ত ডিপ সামারাইজার",
                        color = CyberAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ALWAYS-VISIBLE TOP PINNED STRAINER BAR (Chamber 2 Summary strip inside Chat too!)
            Surface(
                color = CyberCardBg,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, CyberEmerald.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedChamber = 1 }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "📌 ছাঁকনি প্রোগ্রেস: $activeProjectTitle",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "✅ সম্পন্ন: $completedCount/${strainedSteps.size} • ❌ বাকি: $failedOrRemainingCount • 24/7 বিট: #$heartbeatCount",
                            color = CyberEmerald,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "🛡️ ক্রোমিয়াম জিরো-চাপ: RAM ${zeroPressureTelemetry.jvmUsedMb}/${zeroPressureTelemetry.jvmMaxMb}MB • ডিস্ক বাফার: ${zeroPressureTelemetry.tempCacheKb}KB • সেলফ-ফিচার: ${savedSelfFeatures.size}টি",
                            color = CyberCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = { QuadChatAndStrainerEngine.runAllStrainedStepsSequentially(context) },
                        enabled = !isStrainingOrRunning && failedOrRemainingCount > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald)
                    ) {
                        Text(
                            text = if (isStrainingOrRunning) "⏳ চলছে..." else "▶️ সব ধাপ রান",
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4-Chamber Selector Tabs
            val chamberTabs = listOf(
                "💬 ১. সরাসরি চ্যাট (${directMessages.size})",
                "🎯 ২. ছাঁকনি ও ধাপ ($completedCount/${strainedSteps.size})",
                "📥 ৩. অন্য AI পেস্ট (${stagingItems.size})",
                "🛡️ ৪. অটো-ডিবাগ ও ২৪/৭ (${chamberFourItems.size})"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                chamberTabs.forEachIndexed { idx, label ->
                    val selected = selectedChamber == idx
                    val accent = when (idx) {
                        0 -> CyberCyan
                        1 -> CyberEmerald
                        2 -> CyberPurple
                        else -> CyberAmber
                    }
                    Surface(
                        color = if (selected) accent.copy(alpha = 0.24f) else CyberCardBg,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (selected) accent else Color.DarkGray),
                        modifier = Modifier
                            .clickable { selectedChamber = idx }
                            .testTag("quad_chat_tab_$idx")
                    ) {
                        Text(
                            text = label,
                            color = if (selected) Color.White else CyberSlate,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (selectedChamber) {
                // =========================================================================
                // CHAMBER 1: DIRECT CONVERSATIONAL & NANO-ORDER CHAT
                // =========================================================================
                0 -> {
                    Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(directMessages, key = { it.id }) { msg ->
                                val isUser = msg.sender == "USER"
                                Surface(
                                    color = if (isUser) Color(0xFF0F2942) else CyberCardBg,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isUser) CyberCyan.copy(alpha = 0.6f) else CyberEmerald.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = msg.senderBadge,
                                            color = if (isUser) CyberCyan else CyberEmerald,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = msg.content,
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // LIVE INSTALLED SELF-FEATURE QUICK-DOCK & SOVEREIGN COMMAND CHIPS
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            savedSelfFeatures.take(4).forEach { feat ->
                                Surface(
                                    color = CyberCardBg,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(0.8.dp, CyberEmerald.copy(alpha = 0.65f)),
                                    modifier = Modifier.clickable {
                                        MasterOmniControllerEngine.runSelfFeatureOnWebView(feat, activeWebView)
                                    }
                                ) {
                                    Text(
                                        text = "▶️ ${feat.title.take(22)}",
                                        color = CyberEmerald,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Surface(
                                color = CyberCardBg,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(0.8.dp, CyberRose.copy(alpha = 0.65f)),
                                modifier = Modifier.clickable {
                                    AiChromiumDevEngine.removeMountedSelfFeatureFromDom(activeWebView, "")
                                }
                            ) {
                                Text(
                                    text = "🧹 পেজ ফিচার মুছুন",
                                    color = CyberRose,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = chatOneInput,
                            onValueChange = { chatOneInput = it },
                            label = {
                                Text("ন্যানোবট বা মেইন এআই-কে যেকোনো বড় অর্ডার বা অ্যাপের ডিটেইলস লিখুন (স্বয়ংক্রিয়ভাবে ২ নম্বর ছাঁকনিতে ধাপে ধাপে যাবে)")
                            },
                            minLines = 2,
                            maxLines = 5,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("chamber_one_chat_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = onToggleVoiceMic,
                                border = BorderStroke(1.dp, if (isVoiceListening) CyberRose else CyberCyan),
                                modifier = Modifier.testTag("chamber_one_voice_mic_btn")
                            ) {
                                Text(
                                    text = if (isVoiceListening) "⏹ থামান" else "🎙️ মুখে বলুন",
                                    color = if (isVoiceListening) CyberRose else CyberCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Button(
                                onClick = {
                                    val order = chatOneInput
                                    chatOneInput = ""
                                    QuadChatAndStrainerEngine.sendDirectOrderInChatOne(
                                        context = context,
                                        userText = order,
                                        autoStrainToChecklist = true,
                                        activeWebView = activeWebView
                                    )
                                },
                                enabled = chatOneInput.isNotBlank() && !isStrainingOrRunning,
                                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.Black, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("পাঠান ও ছাঁকনিতে সাজান", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = {
                                    val order = chatOneInput
                                    chatOneInput = ""
                                    QuadChatAndStrainerEngine.installIntoSelfViaChromiumZeroPressure(
                                        context = context,
                                        webView = activeWebView,
                                        title = order.take(40),
                                        codeOrSpec = order
                                    )
                                },
                                enabled = chatOneInput.isNotBlank() && !isStrainingOrRunning,
                                colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chamber_one_self_add_chromium_btn")
                            ) {
                                Text(
                                    text = "🧬 নিজের মধ্যে অ্যাড (Chromium 0% চাপ)",
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    val raw = chatOneInput
                                    chatOneInput = ""
                                    scope.launch {
                                        QuadChatAndStrainerEngine.strainOrderIntoStepByStepChecklist(context, raw)
                                        selectedChamber = 1
                                    }
                                },
                                enabled = chatOneInput.isNotBlank() && !isStrainingOrRunning,
                                border = BorderStroke(1.dp, CyberEmerald)
                            ) {
                                Text("🎯 শুধু ছাঁকনি", color = CyberEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // =========================================================================
                // CHAMBER 2: THE STRAINER (ছাঁকনি — STEP-BY-STEP BUILDER WITH ✅ AND ❌)
                // =========================================================================
                1 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Surface(
                                color = CyberCardBg,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, CyberEmerald.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "🎯 চ্যাট ২ (ছাঁকনি): ১ নং চ্যাটের বেকার কথা ছেঁকে সাজানো ধাপসমূহ",
                                        color = CyberEmerald,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "প্রথম থেকে শেষ পর্যন্ত প্রতিটি ধাপ (App Create ➔ Color/Design ➔ Button Placement ➔ Features) নিখুঁতভাবে হয়েছে কি না তা রাইট (✅) ও কাটা (❌) চিহ্ন দিয়ে দেখানো হচ্ছে।",
                                        color = CyberSlate,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        items(strainedSteps, key = { it.id }) { step ->
                            val isDone = step.status == StrainedStepStatus.COMPLETED
                            val isRunning = step.status == StrainedStepStatus.IN_PROGRESS
                            val borderColor = when {
                                isDone -> CyberEmerald
                                isRunning -> CyberCyan
                                else -> CyberRose
                            }

                            Surface(
                                color = CyberCardBg,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, borderColor.copy(alpha = 0.7f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            // Big ✅ or ❌ Status Badge (Clickable to toggle manually if desired)
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(borderColor.copy(alpha = 0.2f))
                                                    .clickable {
                                                        QuadChatAndStrainerEngine.toggleStepStatusManually(context, step.id)
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = step.status.icon,
                                                    fontSize = 15.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "[${step.phaseCategory}] ${step.title}",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 12.sp
                                                )
                                                Text(
                                                    text = "${step.status.icon} ${step.status.banglaLabel} ${if (step.verificationNote.isNotBlank()) "• ${step.verificationNote}" else ""}",
                                                    color = borderColor,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                QuadChatAndStrainerEngine.executeSingleStrainedStep(context, step.id)
                                            },
                                            enabled = !isStrainingOrRunning,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isDone) CyberSlate else CyberCyan
                                            )
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = "Run Step", tint = Color.Black, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = if (isDone) "পুনরায়" else "চালান",
                                                color = Color.Black,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = step.detailedSpec,
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 11.sp
                                    )

                                    if (step.generatedOutputPreview.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Surface(
                                            color = CyberDarkBg,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text(
                                                    text = step.generatedOutputPreview,
                                                    color = CyberEmerald,
                                                    fontSize = 10.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    maxLines = 5,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = "🧬 নিজের মধ্যে অ্যাড (Chromium)",
                                                        color = CyberEmerald,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        modifier = Modifier.clickable {
                                                            QuadChatAndStrainerEngine.installIntoSelfViaChromiumZeroPressure(
                                                                context = context,
                                                                webView = activeWebView,
                                                                title = step.title,
                                                                codeOrSpec = step.generatedOutputPreview.ifBlank { step.detailedSpec }
                                                            )
                                                        }
                                                    )
                                                    Text(
                                                        text = "স্যান্ডবক্সে রান করুন ➔",
                                                        color = CyberCyan,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        modifier = Modifier.clickable {
                                                            onDismiss()
                                                            onOpenSandboxWithPayload(step.generatedOutputPreview)
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
                }

                // =========================================================================
                // CHAMBER 3: EXTERNAL AI HARVEST & STAGING PASTE VAULT
                // =========================================================================
                2 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1-Tap Harvest from Active Browser Tab + Manual Paste Box
                        item {
                            Surface(
                                color = CyberCardBg,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CyberPurple.copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "📥 চ্যাট ৩: অন্য AI থেকে আনা কোড, পদ্ধতি ও ইমেজ পেস্ট ভল্ট",
                                        color = CyberPurple,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "অন্য যেকোনো AI (ChatGPT, Claude, DeepSeek) বা ওয়েবসাইট থেকে ন্যানোবট যা আনবে (যত বড় কোড, পদ্ধতি বা ছবি হোক) তা এখানে পেস্ট করে জমা রাখবে।",
                                        color = CyberSlate,
                                        fontSize = 10.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = {
                                            if (activeWebView != null) {
                                                val harvestJs = """
                                                    (function() {
                                                        var codes = [];
                                                        document.querySelectorAll('pre, code').forEach(function(el) {
                                                            var t = (el.innerText || '').trim();
                                                            if (t.length > 20) codes.push(t);
                                                        });
                                                        var imgs = [];
                                                        document.querySelectorAll('img').forEach(function(im) {
                                                            if (im.src && im.src.startsWith('http')) imgs.push(im.src);
                                                        });
                                                        var bodyText = (document.body ? document.body.innerText : '').trim().slice(0, 6000);
                                                        return JSON.stringify({
                                                            url: window.location.href,
                                                            title: document.title || 'External AI Page',
                                                            codeJoined: codes.join('\n\n/* --- NEXT BLOCK --- */\n\n'),
                                                            firstImg: imgs[0] || '',
                                                            bodyText: bodyText
                                                        });
                                                    })();
                                                """.trimIndent()
                                                activeWebView.evaluateJavascript(harvestJs) { raw ->
                                                    try {
                                                        val unescaped = raw.trim().removeSurrounding("\"")
                                                            .replace("\\\"", "\"")
                                                            .replace("\\\\", "\\")
                                                            .replace("\\n", "\n")
                                                        val obj = org.json.JSONObject(unescaped)
                                                        val codeBlock = obj.optString("codeJoined", "")
                                                        val bodyTxt = obj.optString("bodyText", "")
                                                        val imgUrl = obj.optString("firstImg", "")
                                                        val pageTitle = obj.optString("title", "ব্রাউজার ট্যাব থেকে সংগৃহীত")
                                                        QuadChatAndStrainerEngine.pasteIntoChamberThreeStaging(
                                                            context = context,
                                                            sourceAiOrSite = obj.optString("url", "Active Tab").take(35),
                                                            contentType = if (codeBlock.isNotBlank()) "CODE" else if (imgUrl.isNotBlank()) "IMAGE_DATA" else "METHOD",
                                                            title = pageTitle,
                                                            payloadContent = codeBlock.ifBlank { bodyTxt },
                                                            imageOrSourceUrl = imgUrl
                                                        )
                                                    } catch (_: Exception) {
                                                    }
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "🌐 বর্তমান ওয়েবসাইট/AI ট্যাব থেকে কোড, পদ্ধতি ও ছবি ছেঁকে এখানে পেস্ট করুন",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = manualPasteSource,
                                            onValueChange = { manualPasteSource = it },
                                            label = { Text("উৎস (যেমন: ChatGPT)") },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = CyberPurple,
                                                unfocusedBorderColor = Color.DarkGray,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            )
                                        )
                                        OutlinedTextField(
                                            value = manualPasteTitle,
                                            onValueChange = { manualPasteTitle = it },
                                            label = { Text("শিরোনাম") },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = CyberPurple,
                                                unfocusedBorderColor = Color.DarkGray,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    OutlinedTextField(
                                        value = manualPasteContent,
                                        onValueChange = { manualPasteContent = it },
                                        label = { Text("যত বড় কোড, পদ্ধতি বা ইমেজ লিংক হোক এখানে পেস্ট করে রাখুন") },
                                        minLines = 2,
                                        maxLines = 4,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyberPurple,
                                            unfocusedBorderColor = Color.DarkGray,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    OutlinedButton(
                                        onClick = {
                                            QuadChatAndStrainerEngine.pasteIntoChamberThreeStaging(
                                                context = context,
                                                sourceAiOrSite = manualPasteSource,
                                                contentType = if (manualPasteContent.contains("fun ") || manualPasteContent.contains("<html") || manualPasteContent.contains("function")) "CODE" else "METHOD",
                                                title = manualPasteTitle.ifBlank { "সংরক্ষিত কোড / পদ্ধতি" },
                                                payloadContent = manualPasteContent
                                            )
                                            manualPasteContent = ""
                                            manualPasteTitle = ""
                                        },
                                        enabled = manualPasteContent.isNotBlank(),
                                        border = BorderStroke(1.dp, CyberCyan),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("➕ চ্যাট ৩ হোল্ডিং ভল্টে পেস্ট করে রাখুন", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        items(stagingItems, key = { it.id }) { item ->
                            Surface(
                                color = CyberCardBg,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (item.hasLazyPlaceholders) CyberRose else CyberPurple.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "[${item.contentType}] ${item.title}",
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = "উৎস: ${item.sourceAiOrSite} • কোয়ালিটি: ${item.qualityScorePercent}% ${if (item.hasLazyPlaceholders) "⚠️ (অসম্পূর্ণ/TODO ধরা পড়েছে)" else "✅ (সম্পূর্ণ)"}",
                                                color = if (item.hasLazyPlaceholders) CyberRose else CyberEmerald,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    if (item.imageOrSourceUrl.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "🖼️ ইমেজ/মিডিয়া সোর্স: ${item.imageOrSourceUrl}",
                                            color = CyberCyan,
                                            fontSize = 10.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        color = CyberDarkBg,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = item.payloadContent.take(650),
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                QuadChatAndStrainerEngine.installIntoSelfViaChromiumZeroPressure(
                                                    context = context,
                                                    webView = activeWebView,
                                                    title = item.title,
                                                    codeOrSpec = item.payloadContent
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("🧬 নিজের মধ্যে অ্যাড", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                        Button(
                                            onClick = {
                                                QuadChatAndStrainerEngine.triggerManualChamberFourHealForStagingItem(context, item)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberAmber),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("🛡️ চ্যাট ৪ স্টিচ", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                onDismiss()
                                                onOpenSandboxWithPayload(item.payloadContent)
                                            },
                                            border = BorderStroke(1.dp, CyberCyan),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("▶️ স্যান্ডবক্স", color = CyberCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // =========================================================================
                // CHAMBER 4: AUTO-DEBUGGER, ANTI-LAZY CODE STITCHER & 24/7 GUARDIAN
                // =========================================================================
                3 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 24/7 Immortal Background Daemon Control Card
                        item {
                            Surface(
                                color = CyberCardBg,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CyberAmber.copy(alpha = 0.65f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "🛡️ চ্যাট ৪: অটো-ডিবাগার, কোড স্টিচার ও ২৪/৭ গার্ডিয়ান",
                                                color = CyberAmber,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = "অন্য AI নরমাল বা অর্ধেক কোড দিলে এখানে তা ধরা পড়ে এবং নিজে থেকে ১০০% পূর্ণ করা হয়। পাশাপাশি ২৪/৭ ব্যাকগ্রাউন্ড হার্টবিট চালু থাকে।",
                                                color = CyberSlate,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Switch(
                                            checked = is24x7DaemonActive,
                                            onCheckedChange = {
                                                QuadChatAndStrainerEngine.set24x7DaemonActive(context, it)
                                            }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "⚡ ২৪/৭ হার্টবিট পালস: #$heartbeatCount • WakeLock + WifiLock High-Perf সক্রিয়",
                                        color = CyberEmerald,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (chamberFourItems.isEmpty()) {
                            item {
                                Surface(
                                    color = CyberCardBg,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "চ্যাট ২ বা চ্যাট ৩-এ কোনো ধাপ রান বা কোড পেস্ট হলেই এখানে অটো-ডিবাগিং, শর্টকাট চেক ও স্টিচিং রিপোর্ট দেখা যাবে।",
                                        color = CyberSlate,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            }
                        }

                        items(chamberFourItems, key = { it.id }) { resItem ->
                            Surface(
                                color = CyberCardBg,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, CyberEmerald.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "🎯 লক্ষ্য: ${resItem.targetStepOrItemTitle}",
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "🔍 শনাক্তকৃত বাধা: ${resItem.detectedIssue}",
                                        color = CyberAmber,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = resItem.resolutionAction,
                                        color = CyberEmerald,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (resItem.healedFullPayload.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Surface(
                                            color = CyberDarkBg,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = resItem.healedFullPayload.take(450),
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Button(
                                            onClick = {
                                                QuadChatAndStrainerEngine.installIntoSelfViaChromiumZeroPressure(
                                                    context = context,
                                                    webView = activeWebView,
                                                    title = resItem.targetStepOrItemTitle,
                                                    codeOrSpec = resItem.healedFullPayload
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "🧬 এই পূর্ণ কোডটি ক্রোমিয়ামের মাধ্যমে নিজের মধ্যে অ্যাড করুন (0% চাপ)",
                                                color = Color.Black,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold
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
}
