package com.example.ui.workflow

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
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
import com.example.engine.AiMasterPlannerEngine
import com.example.engine.AutonomousSearchCrawlerEngine
import com.example.engine.GlobalRuleVaultEngine
import com.example.engine.NanobotSwarmOrchestrator
import com.example.engine.SwarmBlueprintResult
import com.example.engine.SwarmMemoryVaultEngine
import com.example.engine.UniversalAiManager
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRose
import com.example.ui.theme.CyberSlate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalRuleAndPlannerSheet(
    initialTab: Int = 0,
    activeWebView: WebView?,
    onApplySwarmBlueprint: (SwarmBlueprintResult, Boolean) -> Unit,
    onInjectTextToSwarm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(initialTab.coerceIn(0, 3)) }

    // Rule Vault states
    val rules by GlobalRuleVaultEngine.rules.collectAsState()
    val isMasterAutoRule by GlobalRuleVaultEngine.isMasterAutoRuleEnabled.collectAsState()
    val isAutoPasteOnLoad by GlobalRuleVaultEngine.isAutoPasteOnPageLoadEnabled.collectAsState()
    val lastRuleStatus by GlobalRuleVaultEngine.lastAutoPasteStatus.collectAsState()

    var newRuleTitle by remember { mutableStateOf("") }
    var newRuleInstruction by remember { mutableStateOf("") }
    var quickPasteExtraPrompt by remember { mutableStateOf("") }

    // AI Master Planner states
    val isPlanning by AiMasterPlannerEngine.isPlanning.collectAsState()
    val plannerStatus by AiMasterPlannerEngine.plannerStatus.collectAsState()
    val failoverStatus by UniversalAiManager.lastFailoverStatus.collectAsState()
    var masterGoalInput by remember {
        mutableStateOf("গিটহাব ও ওয়েব থেকে সেরা আর্কিটেকচার সার্চ করে ডিপ-থিংক করে সম্পূর্ণ বাগ-মুক্ত কোড বানিয়ে দাও।")
    }
    var preferredSearchOrSite by remember { mutableStateOf("") }

    // Autonomous Search Crawler states
    val isCrawling by AutonomousSearchCrawlerEngine.isCrawling.collectAsState()
    val crawlerStatus by AutonomousSearchCrawlerEngine.crawlerStatus.collectAsState()
    val crawledPages by AutonomousSearchCrawlerEngine.lastCrawledPages.collectAsState()
    var searchTopicInput by remember { mutableStateOf("Best Android Jetpack Compose Clean Architecture 2026") }
    var lastCrawlDossier by remember { mutableStateOf("") }

    // Shared Memory Vault states
    val memories by SwarmMemoryVaultEngine.memories.collectAsState()
    val isAutoMemoryRecall by SwarmMemoryVaultEngine.isAutoMemoryRecallEnabled.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = CyberDarkBg,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.testTag("global_rule_and_planner_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "⚡ অটো-রুল ভল্ট • AI মাস্টার প্ল্যানার • অটো-সার্চ ও মেমোরি",
                        color = CyberCyan,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = failoverStatus,
                        color = CyberEmerald,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4-Tab Selector
            val tabTitles = listOf(
                "📜 অটো-রুল ভল্ট (${rules.count { it.isEnabled }})",
                "🧠 AI মাস্টার প্ল্যানার",
                "🔍 অটো-সার্চ ক্রলার",
                "🗄️ যৌথ বট মেমোরি (${memories.size})"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tabTitles.forEachIndexed { idx, label ->
                    val active = selectedTab == idx
                    Surface(
                        color = if (active) CyberPurple.copy(alpha = 0.28f) else CyberCardBg,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (active) CyberPurple else Color.DarkGray),
                        modifier = Modifier
                            .clickable { selectedTab = idx }
                            .testTag("planner_sheet_tab_$idx")
                    ) {
                        Text(
                            text = label,
                            color = if (active) Color.White else CyberSlate,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (selectedTab) {
                // =========================================================================
                // TAB 0: GLOBAL RULE VAULT & AUTO-RULE PASTER
                // =========================================================================
                0 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Surface(
                                color = CyberCardBg,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CyberEmerald.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = lastRuleStatus,
                                        color = CyberEmerald,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "🔄 সব বট, API ও ওয়েবসাইটে অটো-রুল ইনজেক্ট",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = "চালু থাকলে প্রতিবার নতুন সার্চ বা বট চালানোর সময় রুল নিজে থেকেই যুক্ত হবে",
                                                color = CyberSlate,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Switch(
                                            checked = isMasterAutoRule,
                                            onCheckedChange = {
                                                GlobalRuleVaultEngine.setMasterAutoRuleEnabled(context, it)
                                            }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "🌐 যেকোনো ওয়েবসাইট লোড হলেই বক্সে রুল অটো-পেস্ট",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = "আপনি ব্রাউজারে অন্য সাইটে গেলেও নিজে থেকেই রুল বক্সে বসিয়ে রাখবে",
                                                color = CyberSlate,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Switch(
                                            checked = isAutoPasteOnLoad,
                                            onCheckedChange = {
                                                GlobalRuleVaultEngine.setAutoPasteOnPageLoad(context, it)
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Instant 1-Click Auto-Paste to Current Website
                        item {
                            Surface(
                                color = CyberCardBg,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.45f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "⚡ ১-ক্লিকে বর্তমান ওয়েবসাইটে সব স্থায়ী রুল পেস্ট করুন",
                                        color = CyberCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = quickPasteExtraPrompt,
                                        onValueChange = { quickPasteExtraPrompt = it },
                                        placeholder = {
                                            Text(
                                                "ঐচ্ছিক: রুলের সাথে আজকের প্রশ্ন বা টাস্ক লিখুন...",
                                                color = CyberSlate,
                                                fontSize = 11.sp
                                            )
                                        },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyberCyan,
                                            unfocusedBorderColor = Color.DarkGray,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                GlobalRuleVaultEngine.pasteActiveRulesIntoWebView(
                                                    webView = activeWebView,
                                                    optionalPrompt = quickPasteExtraPrompt,
                                                    autoSubmit = false
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("instant_paste_rules_button")
                                        ) {
                                            Text("📋 শুধু রুল পেস্ট করুন", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                        Button(
                                            onClick = {
                                                GlobalRuleVaultEngine.pasteActiveRulesIntoWebView(
                                                    webView = activeWebView,
                                                    optionalPrompt = quickPasteExtraPrompt,
                                                    autoSubmit = true
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("instant_paste_and_send_rules_button")
                                        ) {
                                            Text("🚀 রুল পেস্ট + সেন্ড", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                    }
                                }
                            }
                        }

                        // Add a new Permanent Standing Rule
                        item {
                            Surface(
                                color = CyberCardBg,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CyberPurple.copy(alpha = 0.45f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "➕ নতুন স্থায়ী রুল (Permanent Rule) যোগ করুন",
                                        color = CyberPurple,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = newRuleTitle,
                                        onValueChange = { newRuleTitle = it },
                                        placeholder = { Text("রুলের নাম (যেমন: সবসময় পূর্ণাঙ্গ কোড দেবে)", color = CyberSlate, fontSize = 11.sp) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyberPurple,
                                            unfocusedBorderColor = Color.DarkGray,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = newRuleInstruction,
                                        onValueChange = { newRuleInstruction = it },
                                        placeholder = {
                                            Text(
                                                "বিস্তারিত রুল লিখুন যা সব বট ও ওয়েবসাইটে বারবার স্বয়ংক্রিয়ভাবে প্রযুক্ত হবে...",
                                                color = CyberSlate,
                                                fontSize = 11.sp
                                            )
                                        },
                                        minLines = 2,
                                        maxLines = 4,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyberPurple,
                                            unfocusedBorderColor = Color.DarkGray,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            if (newRuleTitle.isNotBlank() && newRuleInstruction.isNotBlank()) {
                                                GlobalRuleVaultEngine.addRule(
                                                    context = context,
                                                    title = newRuleTitle,
                                                    ruleInstruction = newRuleInstruction
                                                )
                                                newRuleTitle = ""
                                                newRuleInstruction = ""
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("add_permanent_rule_button")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("স্থায়ী রুল ভল্টে সেভ করুন", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // List of saved Permanent Standing Rules
                        items(rules, key = { it.id }) { rule ->
                            Surface(
                                color = CyberCardBg,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (rule.isEnabled) CyberEmerald.copy(alpha = 0.5f) else Color.DarkGray
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = rule.title,
                                            color = if (rule.isEnabled) CyberEmerald else CyberSlate,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = rule.ruleInstruction,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            lineHeight = 16.sp
                                        )
                                    }
                                    Switch(
                                        checked = rule.isEnabled,
                                        onCheckedChange = {
                                            GlobalRuleVaultEngine.toggleRule(context, rule.id)
                                        }
                                    )
                                    IconButton(
                                        onClick = { GlobalRuleVaultEngine.deleteRule(context, rule.id) }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Rule", tint = CyberRose)
                                    }
                                }
                            }
                        }
                    }
                }

                // =========================================================================
                // TAB 1: AI MASTER PLANNER (1-PROMPT TO FULL SWARM + 1-TAP PRESETS)
                // =========================================================================
                1 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Surface(
                                color = CyberCardBg,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CyberPurple.copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "🧠 ১ লাইনে লক্ষ্য বলুন — API ব্রেন নিজে সব বট, রোল ও রুল সাজিয়ে দেবে",
                                        color = CyberCyan,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = plannerStatus,
                                        color = CyberEmerald,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = masterGoalInput,
                                        onValueChange = { masterGoalInput = it },
                                        label = { Text("আপনার মূল টাস্ক বা লক্ষ্য (Goal)", color = CyberCyan, fontSize = 11.sp) },
                                        minLines = 2,
                                        maxLines = 4,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyberCyan,
                                            unfocusedBorderColor = Color.DarkGray,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("master_planner_goal_input")
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    OutlinedTextField(
                                        value = preferredSearchOrSite,
                                        onValueChange = { preferredSearchOrSite = it },
                                        placeholder = {
                                            Text(
                                                "ঐচ্ছিক: নির্দিষ্ট ওয়েবসাইট বা সার্চ টপিক (খালি রাখলে নিজে সার্চ করবে)",
                                                color = CyberSlate,
                                                fontSize = 11.sp
                                            )
                                        },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyberPurple,
                                            unfocusedBorderColor = Color.DarkGray,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                if (!isPlanning) {
                                                    scope.launch {
                                                        val bp = AiMasterPlannerEngine.buildAutonomousSwarmFromGoal(
                                                            userGoal = masterGoalInput,
                                                            preferredSearchOrUrl = preferredSearchOrSite
                                                        )
                                                        onApplySwarmBlueprint(bp, false)
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("auto_build_swarm_button")
                                        ) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = "Build", tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = if (isPlanning) "তৈরি হচ্ছে..." else "বট ও রুল সাজান",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                if (!isPlanning) {
                                                    scope.launch {
                                                        val bp = AiMasterPlannerEngine.buildAutonomousSwarmFromGoal(
                                                            userGoal = masterGoalInput,
                                                            preferredSearchOrUrl = preferredSearchOrSite
                                                        )
                                                        onApplySwarmBlueprint(bp, true)
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("auto_build_and_start_swarm_button")
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = "Build & Run", tint = Color.Black, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = "সাজিয়ে অটো রান করুন",
                                                color = Color.Black,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "⚡ ১-ক্লিক রেডিমেড সোয়ার্ম টেমপ্লেট (1-Tap Swarm Presets):",
                                color = CyberAmber,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }

                        items(AiMasterPlannerEngine.presetTemplates, key = { it.id }) { preset ->
                            Surface(
                                color = CyberCardBg,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = preset.title,
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(CyberCyan.copy(alpha = 0.18f))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(preset.badge, color = CyberCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = preset.subtitle,
                                        color = CyberSlate,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                val bp = AiMasterPlannerEngine.buildPresetSwarm(
                                                    templateId = preset.id,
                                                    customTopicOrUrl = masterGoalInput
                                                )
                                                onApplySwarmBlueprint(bp, false)
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("ক্যানভাসে লোড করুন", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = {
                                                val bp = AiMasterPlannerEngine.buildPresetSwarm(
                                                    templateId = preset.id,
                                                    customTopicOrUrl = masterGoalInput
                                                )
                                                onApplySwarmBlueprint(bp, true)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("লোড + অটো চেইন রান", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // =========================================================================
                // TAB 2: AUTONOMOUS MULTI-SITE SEARCH & PARALLEL CRAWLER
                // =========================================================================
                2 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Surface(
                                color = CyberCardBg,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "🔍 ওয়েবসাইটে না ঢুকেই প্যারালাল অটো-সার্চ ও ডিপ লিংক ক্রলার",
                                        color = CyberCyan,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = crawlerStatus,
                                        color = CyberEmerald,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = searchTopicInput,
                                        onValueChange = { searchTopicInput = it },
                                        label = { Text("যেকোনো টপিক বা প্রশ্ন লিখুন (URL লাগবে না)", color = CyberCyan, fontSize = 11.sp) },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyberCyan,
                                            unfocusedBorderColor = Color.DarkGray,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("autonomous_search_query_input")
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                if (!isCrawling && searchTopicInput.isNotBlank()) {
                                                    scope.launch {
                                                        lastCrawlDossier =
                                                            AutonomousSearchCrawlerEngine.executeAutonomousSearchAndCrawl(
                                                                context = context,
                                                                rawQueryOrTarget = searchTopicInput,
                                                                optionalWebView = activeWebView
                                                            )
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("start_autonomous_crawl_button")
                                        ) {
                                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Black, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = if (isCrawling) "ক্রল হচ্ছে..." else "প্যারালাল সার্চ ও ক্রল",
                                                color = Color.Black,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }

                                        if (lastCrawlDossier.isNotBlank()) {
                                            Button(
                                                onClick = {
                                                    NanobotSwarmOrchestrator.updateMidFlightPayload(lastCrawlDossier)
                                                    onInjectTextToSwarm(lastCrawlDossier)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.Send, contentDescription = "Send to Swarm", tint = Color.Black, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Text(
                                                    text = "ডিপ-থিংক বটে পাঠান",
                                                    color = Color.Black,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        items(crawledPages, key = { it.url }) { page ->
                            Surface(
                                color = CyberCardBg,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, CyberEmerald.copy(alpha = 0.45f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "🌐 ${page.title}",
                                        color = CyberEmerald,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = page.url,
                                        color = CyberCyan,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = page.extractedText.take(450),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // =========================================================================
                // TAB 3: SHARED LONG-TERM SWARM MEMORY VAULT
                // =========================================================================
                3 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Surface(
                                color = CyberCardBg,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CyberAmber.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "🗄️ যৌথ বট মেমোরি অটো-রিকল (Shared Swarm Memory)",
                                            color = CyberAmber,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "এক বট যা শিখবে বা সংগ্রহ করবে, পরের বট ও ভবিষ্যৎ টাস্ক তা নিজে থেকেই মনে রাখবে",
                                            color = CyberSlate,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Switch(
                                        checked = isAutoMemoryRecall,
                                        onCheckedChange = {
                                            SwarmMemoryVaultEngine.setAutoMemoryRecall(context, it)
                                        }
                                    )
                                }
                            }
                        }

                        if (memories.isEmpty()) {
                            item {
                                Surface(
                                    color = CyberCardBg,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "এখনো কোনো বট মেমোরি জমা হয়নি। ন্যানোবট চেইন বা অটো-সার্চ ক্রলার চালালে স্বয়ংক্রিয়ভাবে এখানে অভিজ্ঞতা জমা হবে।",
                                        color = CyberSlate,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }

                        items(memories, key = { it.id }) { mem ->
                            Surface(
                                color = CyberCardBg,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (mem.isPinned) CyberAmber else Color.DarkGray
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
                                                text = mem.topicTitle,
                                                color = if (mem.isPinned) CyberAmber else CyberCyan,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = "উৎস: ${mem.sourceBotName} • ${mem.sourceUrlOrPlatform}",
                                                color = CyberSlate,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Row {
                                            IconButton(
                                                onClick = { SwarmMemoryVaultEngine.togglePin(context, mem.id) },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PushPin,
                                                    contentDescription = "Pin Memory",
                                                    tint = if (mem.isPinned) CyberAmber else Color.Gray,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            IconButton(
                                                onClick = { SwarmMemoryVaultEngine.deleteMemory(context, mem.id) },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete Memory",
                                                    tint = CyberRose,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = mem.keyInsightOrSolution.take(360),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedButton(
                                        onClick = {
                                            NanobotSwarmOrchestrator.updateMidFlightPayload(mem.keyInsightOrSolution)
                                            onInjectTextToSwarm(mem.keyInsightOrSolution)
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("এই মেমোরি বর্তমান বট চেইনে পাঠান", color = CyberEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
