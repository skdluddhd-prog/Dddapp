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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.WifiTethering
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Settings
import com.example.engine.AiChromiumDevEngine
import com.example.engine.GhostMacroAndSnifferEngine
import com.example.engine.MasterOmniControllerEngine
import com.example.engine.MultiFileProjectZipEngine
import com.example.engine.OmniAppControlBridge
import com.example.engine.SmartContextCompressorEngine
import com.example.engine.TaskGearType
import com.example.engine.UniversalAiManager
import com.example.model.AiKeySpecialistRole
import com.example.model.AiProviderType
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
fun ProToolsHubSheet(
    initialTab: Int = 0,
    activeWebView: WebView?,
    omniControlBridge: OmniAppControlBridge,
    onOpenNanobotSandbox: () -> Unit,
    onOpenLiveSandboxWithHtml: (String) -> Unit,
    onOpenTaskQueue: () -> Unit,
    onOpenSummarizer: () -> Unit,
    onOpenDevConsole: () -> Unit,
    onOpenChatHistory: () -> Unit,
    onOpenPermissions: () -> Unit,
    onOpenAiSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(initialTab.coerceIn(0, 4)) }

    // Master Omni-Controller & Self-Feature states
    val isProcessingOmni by MasterOmniControllerEngine.isProcessingOmniCommand.collectAsState()
    val omniStatusBanner by MasterOmniControllerEngine.omniStatusBanner.collectAsState()
    val savedSelfFeatures by MasterOmniControllerEngine.savedSelfFeatures.collectAsState()
    val omniLogs by MasterOmniControllerEngine.actionLogs.collectAsState()
    var omniCommandInput by remember { mutableStateOf("") }

    // Dual-Gear & Smart Context Compressor states
    val isAutoGear by SmartContextCompressorEngine.isAutoGearRouterEnabled.collectAsState()
    val currentGear by SmartContextCompressorEngine.currentGear.collectAsState()
    val isAutoCompress by SmartContextCompressorEngine.isAutoCompressionEnabled.collectAsState()
    val totalTokensSaved by SmartContextCompressorEngine.totalTokensSaved.collectAsState()
    val lastCompressStats by SmartContextCompressorEngine.lastStats.collectAsState()
    val keyTelemetryMap by UniversalAiManager.keyTelemetryMap.collectAsState()
    val profiles by UniversalAiManager.profiles.collectAsState()

    // Full Project & ZIP Builder states
    val isBuildingProject by MultiFileProjectZipEngine.isBuildingProject.collectAsState()
    val projectBuildStatus by MultiFileProjectZipEngine.buildStatus.collectAsState()
    val projectFiles by MultiFileProjectZipEngine.projectFiles.collectAsState()
    var projectGoalInput by remember {
        mutableStateOf("একটি প্রো-লেভেল টাস্ক ড্যাশবোর্ড ও অটোমেশন ওয়েব অ্যাপ তৈরি করো।")
    }
    var selectedVfsFileIndex by remember { mutableIntStateOf(0) }

    // Ghost Macro & Network Sniffer states
    val isRecordingMacro by GhostMacroAndSnifferEngine.isRecordingMacro.collectAsState()
    val isReplayingMacro by GhostMacroAndSnifferEngine.isReplayingMacro.collectAsState()
    val savedMacros by GhostMacroAndSnifferEngine.savedMacros.collectAsState()
    val capturedPackets by GhostMacroAndSnifferEngine.capturedPackets.collectAsState()
    val macroSnifferStatus by GhostMacroAndSnifferEngine.statusBanner.collectAsState()
    var customMacroName by remember { mutableStateOf("") }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 3) {
            GhostMacroAndSnifferEngine.refreshCapturedApiPackets(activeWebView)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = CyberDarkBg,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.testTag("pro_tools_hub_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .cyberAmbientBackground()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Clean Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🛠️ PRO LAB • LAST-STAGE COMMAND CENTER",
                        color = CyberCyan,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${currentGear.badge} • Tokens Saved: $totalTokensSaved",
                        color = CyberEmerald,
                        fontSize = 11.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 5 Clean Pro Tabs
            val tabs = listOf(
                "🎛️ প্রো হাব ও রাডার",
                "📦 .ZIP প্রজেক্ট বিল্ডার",
                "👻 ঘোস্ট ম্যাক্রো (${savedMacros.size})",
                "📡 API স্নিফার (${capturedPackets.size})",
                "👑 অমনি-এআই ও সেলফ-ফিচার (${savedSelfFeatures.size})"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tabs.forEachIndexed { idx, title ->
                    val selected = selectedTab == idx
                    Surface(
                        color = if (selected) CyberCyan.copy(alpha = 0.22f) else CyberCardBg,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (selected) CyberCyan else Color.DarkGray),
                        modifier = Modifier
                            .clickable { selectedTab = idx }
                            .testTag("pro_lab_tab_$idx")
                    ) {
                        Text(
                            text = title,
                            color = if (selected) Color.White else CyberSlate,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (selectedTab) {
                // =====================================================================
                // TAB 0: PRO TOOLS GRID + DUAL-GEAR ROUTER + KEY HEALTH & COMPRESSOR
                // =====================================================================
                0 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Clean 2x4 Pro Launch Grid (Zero Clutter)
                        item {
                            Text(
                                text = "⚡ কুইক প্রো মডিউল লঞ্চার (Zero-Clutter Grid)",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ProGridCard(
                                        icon = Icons.Default.AutoAwesome,
                                        title = "অমনি-এআই ও সেলফ-ফিচার",
                                        subtitle = "অ্যাপ কন্ট্রোল ও নতুন ফিচার",
                                        accent = CyberEmerald,
                                        modifier = Modifier.weight(1f),
                                        onClick = { selectedTab = 4 }
                                    )
                                    ProGridCard(
                                        icon = Icons.Default.Settings,
                                        title = "API Key (+) ও রোল ম্যাট্রিক্স",
                                        subtitle = "রোল, ইগনোর ও সেম-কী সুইচ",
                                        accent = CyberCyan,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            onDismiss()
                                            onOpenAiSettings()
                                        }
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ProGridCard(
                                        icon = Icons.Default.PlayArrow,
                                        title = "লাইভ স্যান্ডবক্স",
                                        subtitle = "ন্যানোবট কোড ও UI রান",
                                        accent = CyberEmerald,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            onDismiss()
                                            onOpenNanobotSandbox()
                                        }
                                    )
                                    ProGridCard(
                                        icon = Icons.Default.SmartToy,
                                        title = "৩-স্ট্রাইক টাস্ক কিউ",
                                        subtitle = "অটো সেলফ-হিলিং লুপ",
                                        accent = CyberCyan,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            onDismiss()
                                            onOpenTaskQueue()
                                        }
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ProGridCard(
                                        icon = Icons.Default.AutoAwesome,
                                        title = "AI সামারাইজার",
                                        subtitle = "পেজ ও ডক সারাংশ",
                                        accent = CyberPurple,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            onDismiss()
                                            onOpenSummarizer()
                                        }
                                    )
                                    ProGridCard(
                                        icon = Icons.Default.Code,
                                        title = "Chromium Dev ইঞ্জিন",
                                        subtitle = "DOM ও ইউজারস্ক্রিপ্ট",
                                        accent = CyberAmber,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            onDismiss()
                                            onOpenDevConsole()
                                        }
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ProGridCard(
                                        icon = Icons.Default.History,
                                        title = "চ্যাট ও সেশন হিস্ট্রি",
                                        subtitle = "পুরনো সেশন রিস্টোর",
                                        accent = CyberCyan,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            onDismiss()
                                            onOpenChatHistory()
                                        }
                                    )
                                    ProGridCard(
                                        icon = Icons.Default.Security,
                                        title = "অটোনমি পারমিশন",
                                        subtitle = "অটো-টাচ ও ভয়েস শেল",
                                        accent = CyberEmerald,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            onDismiss()
                                            onOpenPermissions()
                                        }
                                    )
                                }
                            }
                        }

                        // Dual-Gear Adaptive Router (Normal Fast vs Hard Deep)
                        item {
                            Surface(
                                color = CyberCardBg,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.45f)),
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
                                                text = "⚙️ ডুয়াল-গিয়ার ব্রেন (নরমাল বনাম কঠিন কাজ)",
                                                color = CyberCyan,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = currentGear.banglaLabel,
                                                color = CyberEmerald,
                                                fontSize = 11.sp
                                            )
                                        }
                                        Switch(
                                            checked = isAutoGear,
                                            onCheckedChange = { SmartContextCompressorEngine.setAutoGearRouter(it) }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        TaskGearType.entries.forEach { gear ->
                                            val active = currentGear == gear
                                            OutlinedButton(
                                                onClick = { SmartContextCompressorEngine.setManualGear(gear) },
                                                border = BorderStroke(1.dp, if (active) CyberEmerald else Color.DarkGray),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = gear.badge,
                                                    color = if (active) CyberEmerald else CyberSlate,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Smart Context Compressor & Token Saver
                        item {
                            Surface(
                                color = CyberCardBg,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CyberPurple.copy(alpha = 0.45f)),
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
                                            text = "🧠 স্মার্ট কনটেক্সট কম্প্রেসর (API কোটা ও স্পিড সেভার)",
                                            color = CyberPurple,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "মোট সেভ হয়েছে: $totalTokensSaved টোকেন • সর্বশেষ কম্প্রেশন: ${lastCompressStats.savedPercent}%",
                                            color = Color.White,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Switch(
                                        checked = isAutoCompress,
                                        onCheckedChange = { SmartContextCompressorEngine.setAutoCompression(it) }
                                    )
                                }
                            }
                        }

                        // Live API Key Health, Speed, Role Matrix & Same-Key Model Fallback Radar
                        item {
                            Surface(
                                color = CyberCardBg,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CyberEmerald.copy(alpha = 0.45f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "🛡️ API Key হেলথ, রোল ম্যাট্রিক্স ও সেম-কী সুইচার",
                                            color = CyberEmerald,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedButton(
                                            onClick = {
                                                onDismiss()
                                                onOpenAiSettings()
                                            },
                                            border = BorderStroke(1.dp, CyberCyan)
                                        ) {
                                            Text("➕ কী / রোল সেটিংস", color = CyberCyan, fontSize = 10.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    profiles.forEach { prof ->
                                        val tel = keyTelemetryMap[prof.id]
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 5.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "${prof.label} • [${prof.specialistRole.shortBadge}]",
                                                        color = if (prof.specialistRole == AiKeySpecialistRole.IGNORED_PAUSED) CyberRose else Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp
                                                    )
                                                    Text(
                                                        text = "মডেল: ${prof.modelName} • কল: ${tel?.totalCalls ?: 0} • ফলব্যাক: ${tel?.activeFallbackModel?.ifBlank { "Auto Flash" } ?: "Auto Flash"}",
                                                        color = CyberSlate,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                                Text(
                                                    text = if (prof.specialistRole == AiKeySpecialistRole.IGNORED_PAUSED) "🚫 IGNORED" else (tel?.statusBadge ?: "🟢 Ready"),
                                                    color = if (prof.specialistRole == AiKeySpecialistRole.IGNORED_PAUSED) CyberRose else CyberCyan,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            // 1-Tap Role & Ignore quick selector right inside Radar
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                AiKeySpecialistRole.entries.forEach { rOpt ->
                                                    val isSel = prof.specialistRole == rOpt
                                                    val c = when (rOpt) {
                                                        AiKeySpecialistRole.IGNORED_PAUSED -> CyberRose
                                                        AiKeySpecialistRole.BACKUP_STANDBY -> CyberAmber
                                                        else -> CyberEmerald
                                                    }
                                                    Surface(
                                                        color = if (isSel) c.copy(alpha = 0.25f) else CyberDarkBg,
                                                        shape = RoundedCornerShape(6.dp),
                                                        border = BorderStroke(0.7.dp, if (isSel) c else Color.DarkGray),
                                                        modifier = Modifier.clickable {
                                                            UniversalAiManager.updateProfileSpecialistRole(context, prof.id, rOpt)
                                                        }
                                                    ) {
                                                        Text(
                                                            text = rOpt.shortBadge,
                                                            color = if (isSel) c else CyberSlate,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                                        )
                                                    }
                                                }
                                                if (prof.provider == AiProviderType.GEMINI || prof.provider == AiProviderType.GEMINI_FLASH) {
                                                    listOf("gemini-3.1-flash-lite-preview", "gemini-3.5-flash-lite", "gemini-3-flash-preview").forEach { mId ->
                                                        val isM = prof.modelName.equals(mId, ignoreCase = true)
                                                        Surface(
                                                            color = if (isM) CyberCyan.copy(alpha = 0.25f) else CyberDarkBg,
                                                            shape = RoundedCornerShape(6.dp),
                                                            border = BorderStroke(0.7.dp, if (isM) CyberCyan else Color.DarkGray),
                                                            modifier = Modifier.clickable {
                                                                UniversalAiManager.switchModelOnSameKeyInstant(context, prof.id, mId)
                                                            }
                                                        ) {
                                                            Text(
                                                                text = "⚡${mId.removePrefix("gemini-")}",
                                                                color = if (isM) CyberCyan else Color.White,
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
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

                // =====================================================================
                // TAB 1: PARALLEL MULTI-KEY SPLIT + FULL MULTI-FILE .ZIP PROJECT BUILDER + GMAIL & APK HUB
                // =====================================================================
                1 -> {
                    var restoreInputJson by remember { mutableStateOf("") }
                    var showPasteRestoreBox by remember { mutableStateOf(false) }
                    val restoreFileLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
                    ) { uri ->
                        if (uri != null) {
                            scope.launch {
                                val ok = MultiFileProjectZipEngine.restoreFromUri(context, uri)
                                android.widget.Toast.makeText(
                                    context,
                                    if (ok) "✅ ১০০% অ্যাপ স্টেট সফলভাবে রিস্টোর হয়েছে — ঠিক যেখান থেকে স্টপ হয়েছিল!" else "⚠️ ফাইল থেকে রিস্টোর করা যায়নি",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Surface(
                                color = CyberCardBg,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.5.dp, CyberEmerald),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "👑 ১০০% ফুল অ্যাপ .ZIP, আসল .APK, জিমেইল ও যেখান থেকে স্টপ হয়েছিল কন্টিনিউ হাব",
                                        color = CyberEmerald,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = projectBuildStatus,
                                        color = CyberCyan,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // 1. Direct Gmail & Full Continuation Pack Card
                                    Surface(
                                        color = Color(0xFF040A18),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.5.dp, CyberCyan),
                                        modifier = Modifier.fillMaxWidth().testTag("native_apk_option_card")
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Download, contentDescription = "APK & ZIP", tint = CyberCyan, modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "📧 Gmail (skdluddhd@gmail.com) • ফুল .ZIP ও .APK",
                                                        color = Color.White,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.ExtraBold
                                                    )
                                                }
                                                Surface(
                                                    color = CyberEmerald.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(6.dp),
                                                    border = BorderStroke(1.dp, CyberEmerald)
                                                ) {
                                                    Text(
                                                        text = "✅ 100% RESUME READY",
                                                        color = CyberEmerald,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "• সরাসরি আপনার জিমেইলে (skdluddhd@gmail.com) বা ফোনের Downloads ফোল্ডারে পুরো অ্যাপের ১০০% স্টেট .ZIP এবং আসল রানিং .APK পাঠান।\n• অন্য যেকোনো ওয়েবসাইট বা অ্যাপে শেয়ার করলে ১০০% যেখান থেকে স্টপ হয়েছিল ঠিক সেখান থেকেই কন্টিনিউ হবে!",
                                                color = Color(0xFFCBD5E1),
                                                fontSize = 11.sp,
                                                lineHeight = 16.sp
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            // Row A: Direct Gmail (.ZIP + Links) & Direct Real APK Download/Share
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        scope.launch {
                                                            MultiFileProjectZipEngine.sendFullContinuationZipViaGmail(context)
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .testTag("send_gmail_full_zip_button")
                                                ) {
                                                    Icon(Icons.Default.FolderZip, contentDescription = "Gmail ZIP", tint = Color.Black, modifier = Modifier.size(15.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "📧 জিমেইলে ফুল .ZIP পাঠান",
                                                        color = Color.Black,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.ExtraBold
                                                    )
                                                }

                                                Button(
                                                    onClick = {
                                                        scope.launch {
                                                            MultiFileProjectZipEngine.exportAndShareInstalledApk(
                                                                context = context,
                                                                apkFileName = "SKRL2_100_PERCENT_FULL_APP.apk",
                                                                sendDirectlyToGmail = false
                                                            )
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .testTag("share_apk_info_button")
                                                ) {
                                                    Icon(Icons.Default.Download, contentDescription = "Real APK", tint = Color.Black, modifier = Modifier.size(15.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "📱 আসল .APK ডাউনলোড/শেয়ার",
                                                        color = Color.Black,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.ExtraBold
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Row B: Copy 100% Continuation Link + State & Restore 100% State
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedButton(
                                                    onClick = {
                                                        MultiFileProjectZipEngine.copyFullContinuationPackToClipboard(context)
                                                    },
                                                    border = BorderStroke(1.dp, CyberAmber),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .testTag("copy_apk_path_button")
                                                ) {
                                                    Icon(Icons.Default.Security, contentDescription = "Copy Link", tint = CyberAmber, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "🔗 ১০০% কন্টিনিউ লিংক কপি",
                                                        color = CyberAmber,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.ExtraBold
                                                    )
                                                }

                                                OutlinedButton(
                                                    onClick = {
                                                        restoreFileLauncher.launch("*/*")
                                                    },
                                                    border = BorderStroke(1.dp, CyberPurple),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .testTag("restore_from_zip_file_button")
                                                ) {
                                                    Icon(Icons.Default.Refresh, contentDescription = "Restore File", tint = CyberPurple, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "🔄 .ZIP ফাইল থেকে রিস্টোর",
                                                        color = CyberPurple,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.ExtraBold
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))

                                            OutlinedButton(
                                                onClick = { showPasteRestoreBox = !showPasteRestoreBox },
                                                border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.6f)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = if (showPasteRestoreBox) "▲ পেস্ট রিস্টোর বক্স লুকান" else "📋 অন্য ওয়েবসাইট/অ্যাপ থেকে কপি করা স্টেট পেস্ট করে ১০০% কন্টিনিউ করুন",
                                                    color = CyberCyan,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            if (showPasteRestoreBox) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                OutlinedTextField(
                                                    value = restoreInputJson,
                                                    onValueChange = { restoreInputJson = it },
                                                    label = { Text("এখানে 100% State Snapshot JSON পেস্ট করুন", color = CyberCyan, fontSize = 10.sp) },
                                                    minLines = 3,
                                                    maxLines = 6,
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = CyberEmerald,
                                                        unfocusedBorderColor = Color.DarkGray,
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White
                                                    ),
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Button(
                                                    onClick = {
                                                        val ok = MultiFileProjectZipEngine.restoreFullAppContinuationStateFromJson(context, restoreInputJson)
                                                        android.widget.Toast.makeText(
                                                            context,
                                                            if (ok) "✅ ১০০% স্টেট রিস্টোর সম্পন্ন — যেখান থেকে থেমেছিল সেখান থেকেই প্রস্তুত!" else "⚠️ সঠিক স্টেট JSON দিন",
                                                            android.widget.Toast.LENGTH_LONG
                                                        ).show()
                                                        if (ok) showPasteRestoreBox = false
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text("✅ এখনই ১০০% রিস্টোর ও কন্টিনিউ করুন", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = projectGoalInput,
                                        onValueChange = { projectGoalInput = it },
                                        label = { Text("প্রজেক্টের বিবরণ লিখুন (UI + Logic + Config একসাথে তৈরি হবে)", color = CyberCyan, fontSize = 11.sp) },
                                        minLines = 2,
                                        maxLines = 4,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyberEmerald,
                                            unfocusedBorderColor = Color.DarkGray,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("zip_project_goal_input")
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                if (!isBuildingProject) {
                                                    scope.launch {
                                                        MultiFileProjectZipEngine.synthesizeFullProjectParallel(projectGoalInput)
                                                    }
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("parallel_synthesize_project_button")
                                        ) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = "Build", tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = if (isBuildingProject) "তৈরি হচ্ছে..." else "প্যারালাল বিল্ড করুন",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    MultiFileProjectZipEngine.exportProjectAsZip(context)
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("export_project_zip_button")
                                        ) {
                                            Icon(Icons.Default.FolderZip, contentDescription = "ZIP", tint = Color.Black, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = ".ZIP ডাউনলোড / শেয়ার",
                                                color = Color.Black,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    OutlinedButton(
                                        onClick = {
                                            val combined = MultiFileProjectZipEngine.buildCombinedSingleHtmlPreview()
                                            onDismiss()
                                            onOpenLiveSandboxWithHtml(combined)
                                        },
                                        border = BorderStroke(1.dp, CyberCyan),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Run", tint = CyberCyan, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("পুরো মাল্টি-ফাইল প্রজেক্ট লাইভ স্যান্ডবক্সে রান করে দেখুন", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Virtual File System Tabs
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                projectFiles.forEachIndexed { idx, vf ->
                                    val selected = selectedVfsFileIndex == idx
                                    Surface(
                                        color = if (selected) CyberEmerald.copy(alpha = 0.22f) else CyberCardBg,
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, if (selected) CyberEmerald else Color.DarkGray),
                                        modifier = Modifier.clickable { selectedVfsFileIndex = idx }
                                    ) {
                                        Text(
                                            text = "${vf.fileName} (${vf.darwinianScore}%)",
                                            color = if (selected) CyberEmerald else Color.LightGray,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            val activeFile = projectFiles.getOrNull(selectedVfsFileIndex) ?: projectFiles.firstOrNull()
                            if (activeFile != null) {
                                OutlinedTextField(
                                    value = activeFile.content,
                                    onValueChange = { newCode ->
                                        MultiFileProjectZipEngine.updateFileContent(activeFile.fileName, newCode)
                                    },
                                    label = { Text("ফাইল এডিটর: ${activeFile.fileName}", color = CyberEmerald, fontSize = 11.sp) },
                                    minLines = 10,
                                    maxLines = 16,
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = Color.White
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyberEmerald,
                                        unfocusedBorderColor = Color.DarkGray,
                                        focusedContainerColor = CyberCardBg,
                                        unfocusedContainerColor = CyberCardBg
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // =====================================================================
                // TAB 2: HUMAN ACTION GHOST MACRO RECORDER & INFINITE REPLAYER
                // =====================================================================
                2 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Surface(
                                color = CyberCardBg,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (isRecordingMacro) CyberRose else CyberCyan.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "👻 হিউম্যান অ্যাকশন রেকর্ডার ও অটো-রিপ্লেয়ার (Ghost Macro)",
                                        color = CyberCyan,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = macroSnifferStatus,
                                        color = if (isRecordingMacro) CyberRose else CyberEmerald,
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = customMacroName,
                                        onValueChange = { customMacroName = it },
                                        placeholder = { Text("ম্যাক্রোর নাম দিন (যেমন: Auto Login & Submit)...", color = CyberSlate, fontSize = 11.sp) },
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
                                        if (!isRecordingMacro) {
                                            Button(
                                                onClick = {
                                                    GhostMacroAndSnifferEngine.startRecordingMacro(activeWebView)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = CyberRose),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("start_ghost_macro_record_button")
                                            ) {
                                                Icon(Icons.Default.FiberManualRecord, contentDescription = "Rec", tint = Color.White, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Text("🔴 অ্যাকশন রেকর্ড শুরু করুন", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            Button(
                                                onClick = {
                                                    GhostMacroAndSnifferEngine.stopAndSaveMacro(
                                                        context = context,
                                                        webView = activeWebView,
                                                        customTitle = customMacroName
                                                    )
                                                    customMacroName = ""
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("stop_and_save_ghost_macro_button")
                                            ) {
                                                Icon(Icons.Default.Stop, contentDescription = "Save", tint = Color.Black, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Text("⏹ থামান ও ম্যাক্রো সেভ করুন", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                            }
                                        }

                                        if (isReplayingMacro) {
                                            OutlinedButton(
                                                onClick = { GhostMacroAndSnifferEngine.stopMacroReplay() },
                                                border = BorderStroke(1.dp, CyberRose)
                                            ) {
                                                Text("রিপ্লে থামান", color = CyberRose, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        items(savedMacros, key = { it.id }) { macro ->
                            Surface(
                                color = CyberCardBg,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, CyberEmerald.copy(alpha = 0.45f)),
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
                                                text = macro.title,
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = "${macro.steps.size}টি ধাপ • ${macro.targetDomainOrUrl.take(50)}",
                                                color = CyberSlate,
                                                fontSize = 10.sp
                                            )
                                        }
                                        IconButton(
                                            onClick = { GhostMacroAndSnifferEngine.deleteMacro(context, macro.id) }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CyberRose)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                GhostMacroAndSnifferEngine.replayMacro(activeWebView, macro, repeatRounds = 1)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = "1x", tint = Color.Black, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("১ বার চালান", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                        Button(
                                            onClick = {
                                                GhostMacroAndSnifferEngine.replayMacro(activeWebView, macro, repeatRounds = 10)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Repeat, contentDescription = "Loop", tint = Color.White, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("১০ বার লুপ চালান", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // =====================================================================
                // TAB 3: DEEP NETWORK FETCH / XHR HIDDEN API SNIFFER
                // =====================================================================
                3 -> {
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
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "📡 ডিপ নেটওয়ার্ক Fetch / XHR API ট্রাফিক স্নিফার",
                                            color = CyberCyan,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = macroSnifferStatus,
                                            color = CyberEmerald,
                                            fontSize = 11.sp
                                        )
                                    }
                                    IconButton(
                                        onClick = { GhostMacroAndSnifferEngine.refreshCapturedApiPackets(activeWebView) }
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = CyberCyan)
                                    }
                                }
                            }
                        }

                        if (capturedPackets.isEmpty()) {
                            item {
                                Surface(
                                    color = CyberCardBg,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "ওয়েবসাইটের ভেতরে যেকোনো বাটনে ক্লিক বা সার্চ করলে পর্দার আড়ালের গোপন JSON ও API কলগুলো এখানে রিয়েল-টাইমে দেখা যাবে।",
                                        color = CyberSlate,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }

                        items(capturedPackets, key = { it.id }) { pkt ->
                            Surface(
                                color = CyberCardBg,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, CyberPurple.copy(alpha = 0.45f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "[${pkt.method}] HTTP ${pkt.status}",
                                            color = CyberEmerald,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "স্যান্ডবক্সে পাঠান ➔",
                                            color = CyberCyan,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.clickable {
                                                onDismiss()
                                                onOpenLiveSandboxWithHtml(pkt.responsePreview)
                                            }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = pkt.url,
                                        color = CyberCyan,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (pkt.responsePreview.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = pkt.responsePreview.take(360),
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // =====================================================================
                // TAB 4: MASTER APP OMNI-CONTROLLER AI & SELF-FEATURE BUILDER
                // =====================================================================
                4 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Omni Command Input & Quick Presets
                        item {
                            Surface(
                                color = CyberCardBg,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CyberEmerald.copy(alpha = 0.65f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "👑 মাস্টার অ্যাপ অমনি-কন্ট্রোলার এআই (১০০% ফুল অ্যাপ ও ফিচার কন্ট্রোল)",
                                        color = CyberEmerald,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = omniStatusBanner,
                                        color = CyberCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = omniCommandInput,
                                        onValueChange = { omniCommandInput = it },
                                        label = {
                                            Text("মেইন এআই-কে আদেশ দিন (যেমন: ডুয়াল স্ক্রিন অন করো / নতুন সেলফ-ফিচার বানাও যা পেজের সব লিংক বের করবে / একই কী-তে ফ্ল্যাশ মডেল করো)")
                                        },
                                        minLines = 2,
                                        maxLines = 4,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("omni_command_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyberEmerald,
                                            unfocusedBorderColor = Color.DarkGray,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // 1-Tap Quick Omni Command Chips (Including Button ON/OFF, Chromium Flags & Self-Features)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val presets = listOf(
                                            "🎛️ ফ্লোটিং পিল OFF করো" to "ফ্লোটিং রুল পিল এবং ম্যাক্রো পিল অফ করো",
                                            "🎛️ সব বাটন ON করো" to "সব বাটন অন করো",
                                            "🔓 CORS ও DevFlags আনলক" to "ক্রোমিয়াম ফ্ল্যাগ cors এবং popup আনলক করো",
                                            "⚡ একই কী-তে Flash মডেল" to "একই কী-তে ফ্ল্যাশ মডেল করো",
                                            "🧠 একই কী-তে Pro মডেল" to "একই কী-তে প্রো মডেল করো",
                                            "🖥️ ডুয়াল স্প্লিট ভিউ ON" to "ডুয়াল স্প্লিট স্ক্রিন অন করো",
                                            "✨ নতুন সেলফ-ফিচার অ্যাড" to "নতুন একটি সেলফ-ফিচার বানাও যা বর্তমান ওয়েবপেজের সব ছবি এবং গুরুত্বপূর্ণ লিংক হাইলাইট করে কাউন্ট দেখাবে",
                                            "🗑️ মাউন্ট করা ফিচার মুছো" to "বর্তমান পেজ থেকে মাউন্ট করা সব সেলফ ফিচার ডিলিট করো"
                                        )
                                        presets.forEach { (label, cmdText) ->
                                            Surface(
                                                color = CyberDarkBg,
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(0.8.dp, CyberCyan.copy(alpha = 0.6f)),
                                                modifier = Modifier.clickable {
                                                    omniCommandInput = cmdText
                                                    MasterOmniControllerEngine.executeOmniCommand(
                                                        context = context,
                                                        rawCommand = cmdText,
                                                        bridge = omniControlBridge
                                                    )
                                                }
                                            ) {
                                                Text(
                                                    text = label,
                                                    color = CyberCyan,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = {
                                            MasterOmniControllerEngine.executeOmniCommand(
                                                context = context,
                                                rawCommand = omniCommandInput,
                                                bridge = omniControlBridge
                                            )
                                        },
                                        enabled = !isProcessingOmni && omniCommandInput.isNotBlank(),
                                        colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("execute_omni_command_button")
                                    ) {
                                        Text(
                                            text = if (isProcessingOmni) "⏳ মেইন অমনি-এআই কার্যকর করছে..." else "🚀 অমনি-কমান্ড রান করুন (অ্যাপ / বাটন / ফ্ল্যাগ / সেলফ-ফিচার)",
                                            color = Color.Black,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }

                        // Self-Feature Vault Header
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🧩 সেলফ-ফিচার ভল্ট (${savedSelfFeatures.size}টি সক্রিয় মডিউল):",
                                    color = CyberPurple,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "🧹 পেজ থেকে সব আনমাউন্ট",
                                    color = CyberRose,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.clickable {
                                        AiChromiumDevEngine.removeMountedSelfFeatureFromDom(activeWebView, "")
                                    }
                                )
                            }
                        }

                        items(savedSelfFeatures, key = { it.id }) { feat ->
                            Surface(
                                color = CyberCardBg,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, CyberPurple.copy(alpha = 0.55f)),
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
                                                text = feat.title,
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = feat.description,
                                                color = CyberSlate,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Surface(
                                            color = CyberPurple.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = feat.categoryBadge,
                                                color = CyberPurple,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = {
                                                MasterOmniControllerEngine.runSelfFeatureOnWebView(feat, activeWebView)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("▶️ পেজে রান", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                MasterOmniControllerEngine.toggleSelfFeatureAutoRun(context, feat.id)
                                            },
                                            border = BorderStroke(
                                                1.dp,
                                                if (feat.isAutoRunOnPageLoad) CyberEmerald else Color.DarkGray
                                            )
                                        ) {
                                            Text(
                                                text = if (feat.isAutoRunOnPageLoad) "🟢 অটো ON" else "⚪ অটো OFF",
                                                color = if (feat.isAutoRunOnPageLoad) CyberEmerald else CyberSlate,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                AiChromiumDevEngine.removeMountedSelfFeatureFromDom(activeWebView, feat.title)
                                                MasterOmniControllerEngine.deleteSelfFeature(context, feat.id)
                                            }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Self Feature", tint = CyberRose)
                                        }
                                    }
                                }
                            }
                        }

                        // Omni Execution History Logs
                        if (omniLogs.isNotEmpty()) {
                            item {
                                Text(
                                    text = "📜 অমনি-এআই কমান্ড লগ:",
                                    color = CyberCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            items(omniLogs, key = { it.id }) { log ->
                                Surface(
                                    color = CyberDarkBg,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(0.5.dp, Color.DarkGray),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("🗣️ আদেশ: ${log.command}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text("✅ অ্যাকশন: ${log.executedActionsSummary}", color = CyberEmerald, fontSize = 10.sp)
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

@Composable
private fun ProGridCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = CyberCardBg,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f)),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = accent, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    color = CyberSlate,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
