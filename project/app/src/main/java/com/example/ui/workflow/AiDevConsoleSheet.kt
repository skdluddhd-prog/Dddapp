package com.example.ui.workflow

import android.webkit.WebView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.AiChromiumDevEngine
import com.example.engine.AiSelfAwarenessScope
import com.example.engine.ChromiumScript
import com.example.engine.MasterOmniControllerEngine
import com.example.ui.theme.cyberAmbientBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiDevConsoleSheet(
    activeWebView: WebView?,
    isDesktopMode: Boolean,
    onToggleDesktopMode: (Boolean) -> Unit,
    onTransferExtractedCodeToTabB: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val consoleLogs by AiChromiumDevEngine.consoleLogs.collectAsState()
    val scripts by AiChromiumDevEngine.scripts.collectAsState()
    val devFlags by AiChromiumDevEngine.devFlags.collectAsState()
    val uiButtons by AiChromiumDevEngine.uiButtons.collectAsState()
    val savedSelfFeatures by MasterOmniControllerEngine.savedSelfFeatures.collectAsState()
    val awarenessScope by MasterOmniControllerEngine.selfAwarenessScope.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var jsInput by remember { mutableStateOf("document.title") }
    var jsResultOutput by remember { mutableStateOf("") }
    var scriptNameInput by remember { mutableStateOf("") }
    var scriptCodeInput by remember { mutableStateOf("") }
    var scriptAutoInjectInput by remember { mutableStateOf(true) }
    var editingFeatureId by remember { mutableStateOf<String?>(null) }
    var domSelectorInput by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF070B14),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.testTag("ai_dev_console_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .cyberAmbientBackground()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "🛠️ নিজস্ব ক্রোমিয়াম ইঞ্জিন, ডেভেলপার ফ্ল্যাগ ও বাটন/কোড সার্জারি হাব",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "ক্রোমিয়ামের যেকোনো বাটন অন/অফ, ডেভেলপার ফ্ল্যাগ, কোড অ্যাড/এডিট/ডিলিট এবং এআই সেলফ-অ্যানাটমি স্কোপ",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "🎛️ বাটন ও ডেভ ফ্ল্যাগস",
                    "🧬 কোড অ্যাড/এডিট/মুছুন",
                    "💻 জেএস ও কনসোল",
                    "🧠 অ্যানাটমি ও অডিট"
                ).forEachIndexed { idx, title ->
                    val active = selectedTab == idx
                    Surface(
                        color = if (active) Color(0xFF0284C7) else Color(0xFF0F172A),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (active) Color(0xFF38BDF8) else Color(0xFF1E293B)),
                        modifier = Modifier.clickable { selectedTab = idx }
                    ) {
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (selectedTab) {
                // =========================================================================
                // TAB 0: AI SELF-AWARENESS SCOPE + APP BUTTON ON/OFF + CHROMIUM DEV FLAGS
                // =========================================================================
                0 -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // 1. AI Proprioceptive Self-Awareness Scope Card
                        item {
                            Surface(
                                color = Color(0xFF0F172A),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF00F0FF)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "🧠 নিজের শরীরের মতো অ্যাপকে জানা (AI Self-Anatomy Scope)",
                                        color = Color(0xFF00F0FF),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "কে জানবে অ্যাপের কোথায় কোন বাটন আছে, কী অন/অফ আছে এবং কী করা বাকি?",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    AiSelfAwarenessScope.entries.forEach { option ->
                                        val isSelected = awarenessScope == option
                                        Surface(
                                            color = if (isSelected) Color(0xFF064E3B) else Color(0xFF070B14),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(
                                                1.dp,
                                                if (isSelected) Color(0xFF10B981) else Color(0xFF334155)
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp)
                                                .clickable {
                                                    MasterOmniControllerEngine.setSelfAwarenessScope(context, option)
                                                }
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text(
                                                    text = "${if (isSelected) "✅ " else "⚪ "}${option.titleBangla}",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                                Text(
                                                    text = option.descriptionBangla,
                                                    color = Color(0xFFCBD5E1),
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 2. App & Chromium UI Button ON/OFF Switchboard
                        item {
                            Surface(
                                color = Color(0xFF0F172A),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF10B981)),
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
                                                text = "🎛️ অ্যাপ ও ক্রোমিয়ামের যেকোনো বাটন অন/অফ সুইচবোর্ড",
                                                color = Color(0xFF10B981),
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 12.sp
                                            )
                                            Text(
                                                text = "নিজে সুইচ করুন অথবা মেইন এআই-কে মুখে/চ্যাটে বলুন যেকোনো বাটন অন/অফ করতে",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 10.sp
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = { AiChromiumDevEngine.resetAllButtonsVisible(context, activeWebView) },
                                            border = BorderStroke(1.dp, Color(0xFF10B981))
                                        ) {
                                            Text("সব ON", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    FlagSwitchRow("💬 উপরের ৪-চেম্বার চ্যাট বাটন", uiButtons.showTop4ChatButton) { v ->
                                        AiChromiumDevEngine.updateUiButtonVisibility(context, activeWebView) { it.copy(showTop4ChatButton = v) }
                                    }
                                    FlagSwitchRow("🤖 উপরের সোয়ার্ম স্টুডিও বাটন", uiButtons.showTopSwarmButton) { v ->
                                        AiChromiumDevEngine.updateUiButtonVisibility(context, activeWebView) { it.copy(showTopSwarmButton = v) }
                                    }
                                    FlagSwitchRow("📌 উপরের পিন করা ছাঁকনি বার (✅/❌)", uiButtons.showTopStrainerBar) { v ->
                                        AiChromiumDevEngine.updateUiButtonVisibility(context, activeWebView) { it.copy(showTopStrainerBar = v) }
                                    }
                                    FlagSwitchRow("📺 লাইভ বট উত্তর ও রিলে ব্যানার", uiButtons.showLiveRelayBanner) { v ->
                                        AiChromiumDevEngine.updateUiButtonVisibility(context, activeWebView) { it.copy(showLiveRelayBanner = v) }
                                    }
                                    FlagSwitchRow("📋 ফ্লোটিং রুল পেস্ট বাটন", uiButtons.showFloatingRulePill) { v ->
                                        AiChromiumDevEngine.updateUiButtonVisibility(context, activeWebView) { it.copy(showFloatingRulePill = v) }
                                    }
                                    FlagSwitchRow("🔴 ফ্লোটিং ম্যাক্রো রেকর্ড বাটন", uiButtons.showFloatingMacroPill) { v ->
                                        AiChromiumDevEngine.updateUiButtonVisibility(context, activeWebView) { it.copy(showFloatingMacroPill = v) }
                                    }
                                    FlagSwitchRow("⚡ ফ্লোটিং ডুয়াল-গিয়ার ব্যাজ", uiButtons.showFloatingGearBadge) { v ->
                                        AiChromiumDevEngine.updateUiButtonVisibility(context, activeWebView) { it.copy(showFloatingGearBadge = v) }
                                    }
                                    FlagSwitchRow("👑 ফ্লোটিং মাস্টার অমনি বাটন", uiButtons.showFloatingOmniPill) { v ->
                                        AiChromiumDevEngine.updateUiButtonVisibility(context, activeWebView) { it.copy(showFloatingOmniPill = v) }
                                    }
                                    FlagSwitchRow("🧬 ক্রোমিয়ামের ভেতরের সেলফ-ফিচার ডক", uiButtons.showChromiumSelfFeatureDock) { v ->
                                        AiChromiumDevEngine.updateUiButtonVisibility(context, activeWebView) { it.copy(showChromiumSelfFeatureDock = v) }
                                    }
                                    FlagSwitchRow("⚓ নিচের ৪-পিলার মেইন ডক বার", uiButtons.showBottom4PillarDock) { v ->
                                        AiChromiumDevEngine.updateUiButtonVisibility(context, activeWebView) { it.copy(showBottom4PillarDock = v) }
                                    }
                                }
                            }
                        }

                        // 3. Sovereign Chromium Developer Flags Card
                        item {
                            Surface(
                                color = Color(0xFF0F172A),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFA855F7)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "⚙️ ক্রোমিয়াম ডেভেলপাররা যা যা অন/অফ করে (Sovereign Chromium Flags)",
                                        color = Color(0xFFA855F7),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "হার্ডওয়্যার GPU, জিরো-র‍্যাম ইমেজ ব্লকার, পপআপ ব্লকার, থার্ড-পার্টি কুকি ও ডার্ক ইঞ্জিন",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    FlagSwitchRow("🖥️ ডেস্কটপ ব্রাউজার মোড (Linux x86_64 User-Agent)", isDesktopMode || devFlags.desktopUserAgent) { v ->
                                        onToggleDesktopMode(v)
                                        AiChromiumDevEngine.updateDevFlags(context, activeWebView) { it.copy(desktopUserAgent = v) }
                                    }
                                    FlagSwitchRow("🛡️ অ্যাড, পপআপ ও ওভারলে ব্লকার (Ad/Popup Blocker)", devFlags.adAndPopupBlockerEnabled) { v ->
                                        AiChromiumDevEngine.updateDevFlags(context, activeWebView) { it.copy(adAndPopupBlockerEnabled = v) }
                                    }
                                    FlagSwitchRow("🌙 সব ওয়েবসাইটে ফোর্স সাইবার ডার্ক থিম (Force Cyber Dark)", devFlags.forceCyberDarkOnAllSites) { v ->
                                        AiChromiumDevEngine.updateDevFlags(context, activeWebView) { it.copy(forceCyberDarkOnAllSites = v) }
                                    }
                                    FlagSwitchRow("🪶 জিরো-র‍্যাম ইমেজ ব্লকার (ভারী ছবি বন্ধ রেখে আল্ট্রা-ফাস্ট ব্রাউজিং)", devFlags.blockNetworkImagesZeroRam) { v ->
                                        AiChromiumDevEngine.updateDevFlags(context, activeWebView) { it.copy(blockNetworkImagesZeroRam = v) }
                                    }
                                    FlagSwitchRow("⚡ হার্ডওয়্যার GPU অ্যাক্সিলারেশন (WebGL/Canvas Boost)", devFlags.hardwareAccelEnabled) { v ->
                                        AiChromiumDevEngine.updateDevFlags(context, activeWebView) { it.copy(hardwareAccelEnabled = v) }
                                    }
                                    FlagSwitchRow("🍪 থার্ড-পার্টি কুকি ও ক্রস-সাইট সেশন (Third-Party Cookies)", devFlags.thirdPartyCookiesEnabled) { v ->
                                        AiChromiumDevEngine.updateDevFlags(context, activeWebView) { it.copy(thirdPartyCookiesEnabled = v) }
                                    }
                                    FlagSwitchRow("💾 DOM Storage (localStorage / sessionStorage)", devFlags.domStorageEnabled) { v ->
                                        AiChromiumDevEngine.updateDevFlags(context, activeWebView) { it.copy(domStorageEnabled = v) }
                                    }
                                    FlagSwitchRow("🗄️ IndexedDB ও ওয়েব ডেটাবেস ইঞ্জিন", devFlags.indexedDbEnabled) { v ->
                                        AiChromiumDevEngine.updateDevFlags(context, activeWebView) { it.copy(indexedDbEnabled = v) }
                                    }
                                    FlagSwitchRow("🔊 মিডিয়া ও অডিও অটো-প্লে (Gesture ছাড়া প্লে)", devFlags.mediaAutoplayEnabled) { v ->
                                        AiChromiumDevEngine.updateDevFlags(context, activeWebView) { it.copy(mediaAutoplayEnabled = v) }
                                    }
                                    FlagSwitchRow("🔓 মিক্সড কন্টেন্ট অ্যালাউ (HTTP + HTTPS Mixed Mode)", devFlags.mixedContentAllowed) { v ->
                                        AiChromiumDevEngine.updateDevFlags(context, activeWebView) { it.copy(mixedContentAllowed = v) }
                                    }
                                    FlagSwitchRow("🔍 পিঞ্চ-টু-জুম কন্ট্রোল (Pinch Zoom)", devFlags.zoomControlsEnabled) { v ->
                                        AiChromiumDevEngine.updateDevFlags(context, activeWebView) { it.copy(zoomControlsEnabled = v) }
                                    }
                                    FlagSwitchRow("📜 জাভাস্ক্রিপ্ট V8 ইঞ্জিন (JavaScript Execution)", devFlags.javaScriptEnabled) { v ->
                                        AiChromiumDevEngine.updateDevFlags(context, activeWebView) { it.copy(javaScriptEnabled = v) }
                                    }
                                }
                            }
                        }
                    }
                }

                // =========================================================================
                // TAB 1: CODE & SELF-FEATURE ADD / EDIT / DELETE SURGERY
                // =========================================================================
                1 -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        // Add or Edit Self-Feature / Script Box
                        item {
                            Surface(
                                color = Color(0xFF0F172A),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF00F0FF)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = if (editingFeatureId != null) "✏️ বিদ্যমান ফিচার/কোড এডিট করুন" else "➕ নতুন কোড বা সেলফ-ফিচার ক্রোমিয়ামে অ্যাড করুন",
                                        color = Color(0xFF00F0FF),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = scriptNameInput,
                                        onValueChange = { scriptNameInput = it },
                                        label = { Text("ফিচার বা স্ক্রিপ্টের নাম") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF00F0FF),
                                            unfocusedBorderColor = Color(0xFF334155),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = scriptCodeInput,
                                        onValueChange = { scriptCodeInput = it },
                                        label = { Text("HTML/CSS/JS কোড অথবা এআই নির্দেশ (ছোট থেকে বিশাল যেকোনো কোড)") },
                                        minLines = 3,
                                        maxLines = 6,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF00F0FF),
                                            unfocusedBorderColor = Color(0xFF334155),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("পেজ লোড হলেই অটো-ইনজেক্ট", color = Color.White, fontSize = 11.sp)
                                        Switch(checked = scriptAutoInjectInput, onCheckedChange = { scriptAutoInjectInput = it })
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                if (scriptNameInput.isNotBlank() && scriptCodeInput.isNotBlank()) {
                                                    val editId = editingFeatureId
                                                    if (editId != null) {
                                                        MasterOmniControllerEngine.updateSelfFeatureCode(
                                                            context = context,
                                                            featureId = editId,
                                                            newTitle = scriptNameInput,
                                                            newCode = scriptCodeInput,
                                                            webView = activeWebView
                                                        )
                                                        editingFeatureId = null
                                                    } else {
                                                        MasterOmniControllerEngine.synthesizeAndInstallSelfFeatureZeroPressure(
                                                            context = context,
                                                            titleOrOrder = scriptNameInput,
                                                            rawCodeOrSpec = scriptCodeInput,
                                                            webView = activeWebView,
                                                            autoRunOnEveryPage = scriptAutoInjectInput
                                                        )
                                                    }
                                                    scriptNameInput = ""
                                                    scriptCodeInput = ""
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = if (editingFeatureId != null) "💾 এডিট সেভ ও মাউন্ট করুন" else "🧬 ক্রোমিয়ামে অ্যাড করুন (0% চাপ)",
                                                color = Color.Black,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 11.sp
                                            )
                                        }
                                        if (editingFeatureId != null) {
                                            OutlinedButton(
                                                onClick = {
                                                    editingFeatureId = null
                                                    scriptNameInput = ""
                                                    scriptCodeInput = ""
                                                }
                                            ) {
                                                Text("বাতিল", color = Color.White, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Live Webpage DOM Element Hider / Deleter
                        item {
                            Surface(
                                color = Color(0xFF0F172A),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "✂️ ওয়েবপেজের যেকোনো বাটন বা এলিমেন্ট হাইড / ডিলিট করুন",
                                        color = Color(0xFFF59E0B),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = domSelectorInput,
                                        onValueChange = { domSelectorInput = it },
                                        label = { Text("বাটনের নাম বা CSS Selector (যেমন: Sign up, .cookie-banner, #header)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFFF59E0B),
                                            unfocusedBorderColor = Color(0xFF334155),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                AiChromiumDevEngine.toggleOrDeleteDomElementInChromium(activeWebView, domSelectorInput, "HIDE") {
                                                    jsResultOutput = it
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("🙈 হাইড", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = {
                                                AiChromiumDevEngine.toggleOrDeleteDomElementInChromium(activeWebView, domSelectorInput, "SHOW") {
                                                    jsResultOutput = it
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("👁️ শো (Show)", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = {
                                                AiChromiumDevEngine.toggleOrDeleteDomElementInChromium(activeWebView, domSelectorInput, "DELETE") {
                                                    jsResultOutput = it
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("🗑️ ডিলিট", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // Installed Self-Features List with Run / Edit / Auto-Run / Delete
                        item {
                            Text(
                                text = "🧬 ইনস্টল করা সেলফ-ফিচার ও কোডসমূহ (${savedSelfFeatures.size}টি):",
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            )
                        }

                        items(savedSelfFeatures, key = { it.id }) { feat ->
                            Surface(
                                color = Color(0xFF0F172A),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(feat.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text(feat.description, color = Color(0xFF94A3B8), fontSize = 10.sp)
                                        }
                                        Switch(
                                            checked = feat.isAutoRunOnPageLoad,
                                            onCheckedChange = {
                                                MasterOmniControllerEngine.toggleSelfFeatureAutoRun(context, feat.id)
                                            }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                MasterOmniControllerEngine.runSelfFeatureOnWebView(context, feat, activeWebView)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("▶ রান", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                editingFeatureId = feat.id
                                                scriptNameInput = feat.title
                                                scriptCodeInput = feat.jsPayload
                                                scriptAutoInjectInput = feat.isAutoRunOnPageLoad
                                            },
                                            border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("✏️ এডিট কোড", color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = {
                                                MasterOmniControllerEngine.deleteSelfFeature(context, feat.id, activeWebView)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("🗑️ মুছুন", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // Custom Scripts List with Run & Delete
                        if (scripts.isNotEmpty()) {
                            item {
                                Text(
                                    text = "📜 কাস্টম ক্রোমিয়াম স্ক্রিপ্টসমূহ (${scripts.size}টি):",
                                    color = Color(0xFFA855F7),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp
                                )
                            }
                            items(scripts, key = { it.id }) { item ->
                                Surface(
                                    color = Color(0xFF0F172A),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text(item.code.take(80), color = Color(0xFF94A3B8), fontSize = 10.sp)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Button(
                                                onClick = { AiChromiumDevEngine.runSingleScript(activeWebView, item) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                                            ) {
                                                Text("Run", fontSize = 10.sp)
                                            }
                                            Button(
                                                onClick = { AiChromiumDevEngine.deleteScript(context, item.id, activeWebView) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E))
                                            ) {
                                                Text("🗑️", fontSize = 10.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // =========================================================================
                // TAB 2: LIVE CONSOLE & JS EVALUATOR
                // =========================================================================
                2 -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                AiChromiumDevEngine.extractCodeBlocks(activeWebView) { extracted ->
                                    jsResultOutput = extracted
                                    if (extracted.isNotBlank()) {
                                        onTransferExtractedCodeToTabB(extracted)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("⚡ Extract Code to Tab B", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                AiChromiumDevEngine.scanInteractiveElements(activeWebView) { domElements ->
                                    jsResultOutput = domElements
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🔍 DOM ইনপুট স্ক্যান", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = jsInput,
                        onValueChange = { jsInput = it },
                        label = { Text("Run Custom JavaScript on Active Page") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00F0FF),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            AiChromiumDevEngine.executeJavaScript(activeWebView, jsInput) { res ->
                                jsResultOutput = res
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("▶ জেএস রান করুন (Evaluate JS)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    if (jsResultOutput.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Color(0xFF0B1324),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Output:\n${jsResultOutput.take(800)}",
                                color = Color(0xFFA7F3D0),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Chromium Console Stream (${consoleLogs.size})", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Clear",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            modifier = Modifier.clickable { AiChromiumDevEngine.clearConsoleLogs() }
                        )
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(consoleLogs) { log ->
                            Surface(
                                color = Color(0xFF0F172A),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "[${log.level.name}] ${log.message} (${log.source}:${log.lineNumber})",
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                        }
                    }
                }

                // =========================================================================
                // TAB 3: LIVE APP SELF-ANATOMY MAP & SYSTEM AUDIT
                // =========================================================================
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        item {
                            Surface(
                                color = Color(0xFF0B1324),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF00F0FF)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "🧬 মেইন এআই-এর লাইভ সেলফ-অ্যানাটমি ম্যাপ (Proprioceptive Body Map)",
                                        color = Color(0xFF00F0FF),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = MasterOmniControllerEngine.buildLiveAppSelfAnatomyMap(),
                                        color = Color(0xFFA7F3D0),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        items(AiChromiumDevEngine.auditList) { audit ->
                            Surface(
                                color = Color(0xFF0F172A),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF10B981)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(audit.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(audit.description, color = Color(0xFF94A3B8), fontSize = 11.sp)
                                    Text("Impact: ${audit.impact}", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
private fun FlagSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            modifier = Modifier.weight(1f).padding(end = 8.dp)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
