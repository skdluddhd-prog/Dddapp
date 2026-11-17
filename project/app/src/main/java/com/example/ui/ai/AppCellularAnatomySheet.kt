package com.example.ui.ai

import android.webkit.WebView
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.AppCellularAnatomyEngine
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRose
import com.example.ui.theme.CyberSlate

data class AppOrganCell(
    val titleBangla: String,
    val subtitleCode: String,
    val biologicalRole: String,
    val functionalDescription: String,
    val statusBadge: String = "🟢 ACTIVE"
)

data class AppButtonAnatomy(
    val name: String,
    val location: String,
    val purpose: String,
    val triggerTag: String,
    val isAvailable: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppCellularAnatomySheet(
    activeWebView: WebView?,
    activeScreen: String = "AI_CHAT",
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedTab by remember { mutableIntStateOf(0) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    val metrics = remember(refreshTrigger, activeScreen) {
        AppCellularAnatomyEngine.getCellularMetrics(
            context = context,
            activeWebView = activeWebView,
            activeScreen = activeScreen
        )
    }

    val organs = remember {
        listOf(
            AppOrganCell(
                titleBangla = "মস্তিষ্ক ও কেন্দ্রীয় স্নায়ুতন্ত্র",
                subtitleCode = "MainActivity.kt",
                biologicalRole = "কঙ্কাল ও স্নায়ু সমন্বয়ক",
                functionalDescription = "সম্পূর্ণ অ্যাপের স্ক্যাফোল্ড, শীর্ষ মোড সুইচার (চ্যাট vs ব্রাউজার), নিচের ৪-পিলার ডক ও লাইফসাইকেল নিয়ন্ত্রণ করে।"
            ),
            AppOrganCell(
                titleBangla = "বাচন ও সংবেদনশীল চ্যাট কেন্দ্র",
                subtitleCode = "PureGeminiChatWorkspace.kt",
                biologicalRole = "স্মৃতি ও কথোপকথন অঙ্গ",
                functionalDescription = "ক্লিন জেমিনি চ্যাট ইন্টারফেস, মাল্টি-সেশন হিস্ট্রি, লাইভ স্পিচ মাইক, কোড কপি ও ব্রাউজারে সেলফ-ফিচার রান ইঞ্জিন।"
            ),
            AppOrganCell(
                titleBangla = "দৃষ্টি ও সার্বভৌম ব্রাউজিং অঙ্গ",
                subtitleCode = "ChromiumWebViewContainer.kt & AiChromiumDevEngine.kt",
                biologicalRole = "দৃষ্টি ও তথ্য সংগ্রহ",
                functionalDescription = "দ্বৈত ক্রোমিয়াম ব্রাউজার (ট্যাব A/B), জিরো-র‍্যাম ইমেজ ব্লকার, ফোর্স সাইবার ডার্ক ও রিয়েলটাইম জাভাস্ক্রিপ্ট ইনজেকশন।"
            ),
            AppOrganCell(
                titleBangla = "মাল্টি-মডেল এআই হার্ট ও কিউ-রাউটার",
                subtitleCode = "UniversalAiManager.kt",
                biologicalRole = "রক্ত সংবহন ও প্রাণশক্তি",
                functionalDescription = "Gemini, OpenAI, Groq, DeepSeek, Claude কী পরিচালনা, ০.১ms লাইভ পিং ভেরিফিকেশন ও অটোমেটিক ফেইলওভার।"
            ),
            AppOrganCell(
                titleBangla = "স্বয়ংক্রিয় রিফ্লেক্স মোটর ও সার্জারি",
                subtitleCode = "MasterOmniControllerEngine.kt",
                biologicalRole = "অঙ্গ সঞ্চালন ও স্ব-সার্জারি",
                functionalDescription = "ভয়েস বা টেক্সট কমান্ডে তাৎক্ষণিক বাটন অন/অফ, ক্রোমিয়াম ফ্ল্যাগ পরিবর্তন ও জীবন্ত সেলফ-ফিচার ইনস্টলেশন।"
            ),
            AppOrganCell(
                titleBangla = "ছাঁকনি ও ফিল্টার প্রণালী",
                subtitleCode = "QuadChatAndStrainerEngine.kt",
                biologicalRole = "পরিপাক ও অনাক্রম্যতা",
                functionalDescription = "৪-চেম্বার চ্যাট এবং প্রতিটি কোড বা নির্দেশের ধাপে ধাপে ছাঁকনি ও কোয়ালিটি ভেরিফিকেশন চেকলিস্ট।"
            ),
            AppOrganCell(
                titleBangla = "নিরাপদ স্থায়ী সঞ্চয় ও ফাইল সিস্টেম",
                subtitleCode = "ChatSessionManager.kt & FileStreamHelper.kt",
                biologicalRole = "ডিএনএ ও কোষীয় মেমোরি",
                functionalDescription = "ডিস্কে ১০০% অটুট অ্যাটমিক সেশন সেভ, ০-বাইট ফাইল প্রতিরোধ ও মেমোরি ক্র্যাশ প্রোটেকশন।"
            )
        )
    }

    val buttons = remember {
        listOf(
            AppButtonAnatomy(
                name = "💬 চ্যাট মোড বাটন",
                location = "শীর্ষ বার (বামে)",
                purpose = "এক ট্যাপে পূর্ণাঙ্গ এআই চ্যাট ওয়ার্কস্পেস দৃশ্যমান করে",
                triggerTag = "top_toggle_ai_chat"
            ),
            AppButtonAnatomy(
                name = "🌐 ব্রাউজার মোড বাটন",
                location = "শীর্ষ বার (মাঝখানে)",
                purpose = "এক ট্যাপে দ্বৈত ক্রোমিয়াম ব্রাউজার ওপেন করে",
                triggerTag = "top_toggle_browser"
            ),
            AppButtonAnatomy(
                name = "🧬 সেলুলার অ্যানাটমি বাটন",
                location = "শীর্ষ বার (ডানে)",
                purpose = "অ্যাপের অভ্যন্তরীণ সকল সেলস, বাটন ও টেলিমেট্রি শিট খোলে",
                triggerTag = "top_anatomy_matrix_btn"
            ),
            AppButtonAnatomy(
                name = "⚙️ এআই সেটিংস ও কী বাটন",
                location = "শীর্ষ বার (ডান কোণে)",
                purpose = "ইউনিভার্সাল এআই কী ভল্ট, প্লাস বাটন ও লাইভ টেস্ট শিট খোলে",
                triggerTag = "top_ai_settings_btn"
            ),
            AppButtonAnatomy(
                name = "+ New Chat বাটন",
                location = "চ্যাট শীর্ষ বার (বামে)",
                purpose = "বর্তমান কথোপকথন সংরক্ষণ করে নতুন পরিচ্ছন্ন সেশন তৈরি করে",
                triggerTag = "gemini_new_chat_button"
            ),
            AppButtonAnatomy(
                name = "📜 ইতিহাস (History) বাটন",
                location = "চ্যাট শীর্ষ বার (বামে)",
                purpose = "পূর্ববর্তী সংরক্ষিত সকল চ্যাট সেশন প্রদর্শন ও পুনরুদ্ধারের ড্রয়ার খোলে",
                triggerTag = "gemini_chat_history_button"
            ),
            AppButtonAnatomy(
                name = "✅ এআই মডেল / + API Key চিপ",
                location = "চ্যাট শীর্ষ বার (ডানে)",
                purpose = "সক্রিয় মডেলের নাম দেখায় ও ট্যাপে কী যুক্ত করার পপআপ খোলে",
                triggerTag = "gemini_active_model_chip"
            ),
            AppButtonAnatomy(
                name = "🎙️ MIC • বাংলা (IN) বাটন",
                location = "চ্যাট ইনপুট বার (বামে)",
                purpose = "ইন্ডিয়ান বাংলা (bn-IN) ভাষায় দূরবর্তী ও মৃদু কণ্ঠ নিখুঁতভাবে টেক্সটে রূপান্তর এবং নেট ড্রপ করলে অফলাইন ভল্টে সুরক্ষিত রাখা",
                triggerTag = "gemini_chat_voice_mic_btn"
            ),
            AppButtonAnatomy(
                name = "➤ সেন্ড বাটন",
                location = "চ্যাট ইনপুট বার (ডানে)",
                purpose = "ব্যবহারকারীর লিখিত মেসেজ এআই মস্তিষ্কে তাৎক্ষণিক প্রেরণ করে",
                triggerTag = "gemini_chat_send_btn"
            ),
            AppButtonAnatomy(
                name = "পিলার ১: এআই চ্যাট",
                location = "নিচের ৪-পিলার ডক (১ম)",
                purpose = "প্রধান এআই চ্যাট কনসোলে সুইচ করে",
                triggerTag = "dock_btn_ai_chat"
            ),
            AppButtonAnatomy(
                name = "পিলার ২: ব্রাউজার",
                location = "নিচের ৪-পিলার ডক (২য়)",
                purpose = "সার্বভৌম ক্রোমিয়াম ব্রাউজারে সুইচ করে",
                triggerTag = "dock_btn_browser"
            ),
            AppButtonAnatomy(
                name = "পিলার ৩: সোয়ার্ম ও টুলস",
                location = "নিচের ৪-পিলার ডক (৩য়)",
                purpose = "ন্যানোবট অর্কেস্ট্রেশন ও ভিজ্যুয়াল নোড স্টুডিও খোলে",
                triggerTag = "dock_btn_swarm_tools"
            ),
            AppButtonAnatomy(
                name = "পিলার ৪: সেটিংস ও কী",
                location = "নিচের ৪-পিলার ডক (৪র্থ)",
                purpose = "সম্পূর্ণ ইউনিভার্সাল এআই সেটিংস ও প্রো ডেভ সেটিংস খোলে",
                triggerTag = "dock_btn_settings_keys"
            )
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = CyberDarkBg,
        dragHandle = null,
        modifier = Modifier.testTag("app_cellular_anatomy_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(CyberDarkBg)
        ) {
            // Header Bar
            Surface(
                color = Color(0xFF0C1427),
                border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyberCyan.copy(alpha = 0.15f))
                                .border(1.dp, CyberCyan, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🧬", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "অ্যাপের আত্ম-পরিদর্শন ও সেলুলার ম্যাট্রিক্স",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "মানবদেহের মতো অ্যাপের ভেতর-বাহির ও বাটন অবস্থান",
                                color = CyberCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = {
                                refreshTrigger++
                                Toast.makeText(context, "🔄 লাইভ সেলুলার টেলিমেট্রি রিফ্রেশ হয়েছে", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = CyberCyan)
                        }
                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = CyberSlate)
                        }
                    }
                }
            }

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF090F1C),
                contentColor = CyberCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = CyberCyan,
                        height = 2.5.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("🧬 কোষ ও অঙ্গ", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("🔘 বাটন ম্যাপ", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("🩸 লাইভ টেলিমেট্রি", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("🧠 এআই চেতনা", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                when (selectedTab) {
                    0 -> CellularOrgansTab(organs)
                    1 -> ButtonAnatomyTab(buttons)
                    2 -> CellularTelemetryTab(metrics)
                    3 -> AiConsciousnessTab(context, activeWebView, activeScreen)
                }
            }
        }
    }
}

@Composable
private fun CellularOrgansTab(organs: List<AppOrganCell>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D182E)),
                border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💡", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "একজন মানুষের শরীরে যেমন প্রতিটি কোষ ও অঙ্গ নির্দিষ্ট কাজ করে, এই অ্যাপটির প্রতিটি মডিউল তেমনি সুনির্দিষ্ট দায়িত্ব পালন করছে। এআই এর প্রতিটি অংশ নখদর্পণে জানে।",
                        color = Color.White,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        items(organs) { organ ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101C33)),
                border = BorderStroke(0.8.dp, Color(0xFF243657)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = organ.titleBangla,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberEmerald.copy(alpha = 0.15f))
                                .border(0.8.dp, CyberEmerald, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = organ.statusBadge,
                                color = CyberEmerald,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "ফাইল: ${organ.subtitleCode}",
                            color = CyberCyan,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text("•", color = CyberSlate, fontSize = 10.sp)
                        Text(
                            text = "ভূমিকা: ${organ.biologicalRole}",
                            color = CyberAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = organ.functionalDescription,
                        color = Color(0xFFD4E1F5),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ButtonAnatomyTab(buttons: List<AppButtonAnatomy>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1A30)),
                border = BorderStroke(1.dp, CyberPurple.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎯", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "অ্যাপের কোন বাটন কোথায় অবস্থান করছে এবং প্রেস করলে কি ঘটবে—নিচে তার পূর্ণাঙ্গ ম্যাপ তুলে ধরা হলো। এআই নিজেও প্রতিটি বাটনের অবস্থান নির্ভুলভাবে জানে।",
                        color = Color.White,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        items(buttons) { btn ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101C33)),
                border = BorderStroke(0.8.dp, Color(0xFF243657)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = btn.name,
                            color = CyberCyan,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberPurple.copy(alpha = 0.2f))
                                .border(0.8.dp, CyberPurple, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = btn.location,
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = btn.purpose,
                        color = Color.White,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "টেস্ট ট্যাগ: ${btn.triggerTag}",
                        color = CyberSlate,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun CellularTelemetryTab(metrics: com.example.engine.CellularSystemMetrics) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0E1A33)),
                border = BorderStroke(1.dp, CyberEmerald.copy(alpha = 0.45f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(CyberEmerald)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "লাইভ সেলুলার স্বাস্থ্য ও রক্তপ্রবাহ (System Telemetry)",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TelemetryRow("মেমোরি (RAM)", "${metrics.memoryUsageMb} MB ব্যবহৃত / ${metrics.memoryFreeMb} MB ফাঁকা (সর্বোচ্চ: ${metrics.maxMemoryMb} MB)")
                    TelemetryRow("ব্যাটারি ক্ষমতা", "${if (metrics.batteryPercent >= 0) "${metrics.batteryPercent}%" else "উপলব্ধ নয়"} ${if (metrics.isCharging) "⚡ চার্জ হচ্ছে" else ""}")
                    TelemetryRow("বর্তমান স্ক্রিন", if (metrics.activeScreen == "AI_CHAT") "💬 এআই চ্যাট ওয়ার্কস্পেস" else "🌐 ক্রোমিয়াম ব্রাউজার")
                    TelemetryRow("সক্রিয় এআই মডেল", "${metrics.activeAiProvider} [${metrics.activeAiModel}]")
                    TelemetryRow("ব্রাউজারের ওয়েবপেজ", "${metrics.activeBrowserTitle} (${metrics.activeBrowserUrl})")
                    TelemetryRow("ইনস্টল করা সেলফ-ফিচার", "${metrics.installedSelfFeaturesCount}টি জীবন্ত মডিউল")
                    TelemetryRow("স্থায়ী সংরক্ষিত রুল", "${metrics.activeRulesCount}টি নিয়ম কার্যকর")
                    TelemetryRow("বর্তমান সেশনে বার্তা", "${metrics.currentSessionMessagesCount}টি (মোট সেশন: ${metrics.totalChatSessions}টি)")
                    TelemetryRow("ডেস্কটপ ইঞ্জিন মোড", if (metrics.isDesktopMode) "চালু (Desktop UA)" else "মোবাইল ভিউ")
                    TelemetryRow("ডুয়াল স্প্লিট স্ক্রিন", if (metrics.isDualSplitMode) "চালু (Tab A & B)" else "সিঙ্গেল ভিউ")
                }
            }
        }
    }
}

@Composable
private fun TelemetryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = CyberSlate, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(
            text = value,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
    HorizontalDivider(color = Color(0xFF1E2E4A), thickness = 0.5.dp)
}

@Composable
private fun AiConsciousnessTab(
    context: android.content.Context,
    activeWebView: WebView?,
    activeScreen: String
) {
    val clipboardManager = LocalClipboardManager.current
    val fullPrompt = remember(activeScreen) {
        AppCellularAnatomyEngine.generateComprehensiveSelfAnatomyPrompt(
            context = context,
            activeWebView = activeWebView,
            activeScreen = activeScreen
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1A30)),
            border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🧠 এআই মস্তিষ্কে সার্বক্ষণিক ইনজেক্টেড চেতনা",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(fullPrompt))
                            Toast.makeText(context, "📋 চেতনা ব্লুপ্রিন্ট কপি হয়েছে", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = CyberCyan, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("কপি করুন", color = CyberCyan, fontSize = 10.sp)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "আপনি এআই-কে যে প্রশ্নই করুন না কেন, এআই প্রতি মুহূর্তে নিচের জীবন্ত অ্যানাটমি ও বাটন ম্যাপ নিজের সামনে রেখে উত্তর দেয়। তাই ও ভেতরের প্রতিটি সেল ও বাটনের অবস্থান নির্ভুলভাবে জানে।",
                    color = CyberSlate,
                    fontSize = 10.sp,
                    lineHeight = 15.sp
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF060B14))
                .border(1.dp, Color(0xFF1B2A45), RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) {
            LazyColumn {
                item {
                    Text(
                        text = fullPrompt,
                        color = Color(0xFFC0D5F6),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
