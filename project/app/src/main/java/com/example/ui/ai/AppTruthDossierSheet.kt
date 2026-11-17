package com.example.ui.ai

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.AppSourceModuleTruth
import com.example.engine.AppTruthAndArchitectureVault
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRose
import com.example.ui.theme.CyberSlate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTruthDossierSheet(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val isTruthModeEnabled by AppTruthAndArchitectureVault.isTruthTransparencyEnabled.collectAsState()
    val isDirectMicEnabled by AppTruthAndArchitectureVault.isDirectInAppMicVadEnabled.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val tabs = listOf("📂 কোড অ্যানাটমি", "🟢 বাস্তব বনাম সীমা", "🛠️ ফিক্স হিস্ট্রি")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier = Modifier.testTag("app_truth_dossier_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CyberCyan.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Truth",
                            tint = CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "🔍 সিস্টেম কোড ও অভ্যন্তরীণ সত্য ডসিয়ার",
                            color = Color(0xFF0F172A),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "১০০% স্বচ্ছতা • কোনো কাল্পনিক দাবি ছাড়া আসল স্থাপত্য",
                            color = CyberSlate,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF334155))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // TRUTH MODE TOGGLE CARD (THE BUTTON THE USER SPECIFICALLY REQUESTED!)
            Surface(
                color = if (isTruthModeEnabled) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.2.dp, if (isTruthModeEnabled) CyberEmerald else Color(0xFFCBD5E1)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🧠 AI সত্য উন্মোচন মোড",
                                color = if (isTruthModeEnabled) CyberEmerald else Color(0xFF0F172A),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = if (isTruthModeEnabled) CyberEmerald else Color(0xFF64748B),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (isTruthModeEnabled) "সক্রিয় (ON)" else "বন্ধ (OFF)",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "অন থাকলে AI অ্যাপের ভেতরের সমস্ত আসল কোড, ফাইল ও অ্যান্ড্রয়েড ওএস স্যান্ডবক্সের সত্য জেনে উত্তর দেয়।",
                            color = Color(0xFF334155),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                    Switch(
                        checked = isTruthModeEnabled,
                        onCheckedChange = { newState ->
                            AppTruthAndArchitectureVault.setTruthTransparencyEnabled(context, newState)
                            Toast.makeText(
                                context,
                                if (newState) "✅ AI সত্য উন্মোচন মোড চালু হয়েছে!" else "AI সাধারণ মোডে সেট হয়েছে",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CyberEmerald,
                            uncheckedThumbColor = Color(0xFF94A3B8),
                            uncheckedTrackColor = Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.testTag("toggle_truth_mode_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // TAB ROW
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color(0xFFF1F5F9),
                contentColor = CyberCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = CyberCyan,
                        height = 2.5.dp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTabIndex == index
                    Tab(
                        selected = isSelected,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                color = if (isSelected) CyberCyan else Color(0xFF64748B),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // TAB CONTENT
            when (selectedTabIndex) {
                0 -> {
                    // TAB 1: CODE MANIFEST
                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("ফাইলের নাম বা কাজ দিয়ে সার্চ করুন...", color = Color(0xFF94A3B8), fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = CyberSlate, modifier = Modifier.size(16.dp)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC),
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val filteredList = remember(searchQuery) {
                            if (searchQuery.isBlank()) {
                                AppTruthAndArchitectureVault.codeManifest
                            } else {
                                val q = searchQuery.lowercase()
                                AppTruthAndArchitectureVault.codeManifest.filter {
                                    it.fileName.lowercase().contains(q) ||
                                        it.exactRoleBangla.lowercase().contains(q) ||
                                        it.category.lowercase().contains(q)
                                }
                            }
                        }

                        val liveMutations by AppTruthAndArchitectureVault.liveMutationLedger.collectAsState()
                        val liveDeltaText = remember(liveMutations, searchQuery) {
                            AppTruthAndArchitectureVault.getLiveAddedAndModifiedFeaturesReport()
                        }

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (searchQuery.isBlank()) {
                                item {
                                    Surface(
                                        color = Color(0xFFF0FDF4),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, CyberEmerald),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = "🧭 জিরো-হ্যাং ৬১-ফাইল ম্যাপ ও লাইভ রানটাইম ডেল্টা (নতুন কী অ্যাড হয়েছে):",
                                                color = CyberEmerald,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = liveDeltaText,
                                                color = Color(0xFF0F172A),
                                                fontSize = 9.5.sp,
                                                lineHeight = 14.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                            items(filteredList) { item ->
                                ModuleTruthCard(item)
                            }
                        }
                    }
                }
                1 -> {
                    // TAB 2: REALITY VS LIMITATIONS (What works vs Android OS sandbox)
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Surface(
                                color = Color(0xFFF0FDF4),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, CyberEmerald),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "🟢 যা ১০০% আসল নেটিভভাবে কাজ করছে:",
                                        color = CyberEmerald,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "১. Google Gemini API ও কাস্টম AI মডেল জেনারেশন (UniversalAiManager.kt)\n" +
                                            "২. ডুয়াল ক্রোমিয়াম ব্রাউজার ট্যাব (Tab A ও Tab B) এবং অমনিবক্স সার্চ\n" +
                                            "৩. উচ্চ-কনট্রাস্ট পিওর হোয়াইট থিম ও ফুল-স্ক্রিন এক্সপ্যান্ড\n" +
                                            "৪. টপ-রাইট '[ ✕ কেটে সব বাটন আনুন ]' রিস্টোর কন্ট্রোল\n" +
                                            "৫. ইন-অ্যাপ সরাসরি সেফ অডিও VAD ইঞ্জিন (NativeVoiceEngine.kt)\n" +
                                            "৬. মাল্টি-ফাইল প্রজেক্ট এক্সপোর্ট ও জিপ ডাউনলোড ইঞ্জিন",
                                        color = Color(0xFF0F172A),
                                        fontSize = 10.sp,
                                        lineHeight = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        item {
                            Surface(
                                color = Color(0xFFFFFBEB),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, CyberAmber),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "🟡 'নিজেকে নিজে আপগ্রেড করা' সম্পর্কিত বাস্তব সত্য:",
                                        color = CyberAmber,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "ব্যবহারকারীর জিজ্ঞাসা: 'নিজেকে নিজে আপগ্রেড করা এটি কোথায় গেল? এটি কি হলো সত্যিকার?'\n\n" +
                                            "বাস্তব সত্য:\n" +
                                            "অ্যান্ড্রয়েড অপারেটিং সিস্টেমের সর্বোচ্চ নিরাপত্তা নীতি (SELinux ও APK Signature Sandbox)-এর কারণে কোনো অ্যান্ড্রয়েড অ্যাপ রানটাইমে ডিভাইসের ভেতর নিজের কম্পাইল করা কোড (Kotlin বাইটকোড বা classes.dex) পরিবর্তন করতে পারে না।\n\n" +
                                            "তাহলে অ্যাপে কী করা হয়েছিল?\n" +
                                            "পূর্বে তৈরি 'AutonomousSelfEvolutionEngine' আসলে একটি ডাইনামিক WebView HTML/JavaScript রানার। এটি ব্যবহারকারীর দেওয়া নির্দেশনা অনুযায়ী HTML/JS দিয়ে স্বয়ংসম্পূর্ণ ওয়েব অ্যাপ্লিকেশন বা টুল তৈরি করে SharedPreferences-এ জমা রাখত এবং ফুল-স্ক্রিনে চালাত। এটিকে নেটিভ কোটলিন সেলফ-রিকম্পাইলার বলা টেকনিক্যালি সঠিক নয়; এটি একটি ডাইনামিক ওয়েব রানার।",
                                        color = Color(0xFF0F172A),
                                        fontSize = 10.sp,
                                        lineHeight = 15.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        item {
                            Surface(
                                color = Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "🛡️ কেন কোনো অ্যাপ নিজে নিজে নেটিভ APK রিকম্পাইল করতে পারে না?",
                                        color = Color(0xFF0F172A),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "১. অ্যান্ড্রয়েডে ইনস্টল হওয়া APK ফাইলটি /data/app ডিরেক্টরিতে Read-Only মোডে থাকে।\n" +
                                            "২. অ্যাপের নিজস্ব প্রসেস কখনো ওই APK ফাইলে রাইট পারমিশন পায় না।\n" +
                                            "৩. নতুন নেটিভ কোড যুক্ত করার জন্য একটি বাহ্যিক কম্পাইলার (যেমন AI Studio বিল্ড সার্ভার বা Gradle) প্রয়োজন হয় যা নতুন APK তৈরি করে ব্যবহারকারীকে আপডেট হিসেবে প্রদান করে।",
                                        color = Color(0xFF334155),
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // TAB 3: FIXES & ISSUE RESOLUTION HISTORY
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            FixHistoryCard(
                                title = "🎙️ মাইক বাটনে ক্লিক করলে ব্যাকগ্রাউন্ডে চলে যাওয়া ফিক্স",
                                status = "স্থায়ী সমাধান সম্পন্ন",
                                statusColor = CyberEmerald,
                                cause = "অ্যান্ড্রয়েডের সিস্টেম SpeechRecognizer চালু করার সময় গুগল ভয়েস সার্চ ডায়ালগ MainActivity-কে পজ (onPause) করে ব্যাকগ্রাউন্ডে পাঠিয়ে দিচ্ছিল।",
                                fixApplied = "সিস্টেম ডায়ালগ বাইপাস করে ইন-প্রসেস Hardware AudioRecord + VAD ইঞ্জিন যুক্ত করা হয়েছে। এটি মেমরিতে কাজ করে এবং অ্যাপকে কখনো মিনিমাইজ বা ব্যাকগ্রাউন্ড করে না।"
                            )
                        }

                        item {
                            FixHistoryCard(
                                title = "⬜ স্ক্রিন ডার্ক থেকে পিওর হোয়াইট (Pure White) থিম রূপান্তর",
                                status = "সম্পন্ন",
                                statusColor = CyberEmerald,
                                cause = "ব্যবহারকারী সম্পূর্ণ সাদা ব্যাকগ্রাউন্ড ও পরিষ্কার কনট্রাস্ট চেয়েছিলেন।",
                                fixApplied = "MainActivity ও সমস্ত চ্যাট/ব্রাউজার কন্টেইনারে Color.White ব্যাকগ্রাউন্ড ও #0F172A ডিপ-স্লেট টেক্সট যুক্ত করা হয়েছে।"
                            )
                        }

                        item {
                            FixHistoryCard(
                                title = "⛶ চ্যাট ও ব্রাউজার ট্যাবের আলাদা ফুল-স্ক্রিন ও রিস্টোর বাটন",
                                status = "সম্পন্ন",
                                statusColor = CyberEmerald,
                                cause = "চ্যাট বা ব্রাউজারে ট্যাপ করলে বাকি অপশনগুলো ঢেকে বড় স্ক্রিন হওয়া এবং কেটে বের হওয়ার সহজ অপশন দরকার ছিল।",
                                fixApplied = "প্রতিটি ট্যাবে ক্লিক করলে সেটি স্বাধীন ফুল-স্ক্রিন হয়ে যায় এবং উপরে ডানে '[ ✕ কেটে সব বাটন আনুন ]' বাটন থাকে যা এক ট্যাপে আবার সব ট্যাব ও বাটন রিস্টোর করে।"
                            )
                        }

                        item {
                            FixHistoryCard(
                                title = "🛠️ 'Unresolved reference fillMaxHeight' বিল্ড এরর ফিক্স",
                                status = "সমাধান সম্পন্ন",
                                statusColor = CyberEmerald,
                                cause = "MainActivity.kt-তে ওজন-নির্দিষ্ট Row/Column চাইল্ডে ভুলভাবে fillMaxHeight কল করা হয়েছিল।",
                                fixApplied = "সঠিক Compose Modifier.fillMaxSize() দিয়ে সংশোধন করা হয়েছে।"
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // BOTTOM ACTION BAR: Copy Dossier & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val fullDossier = AppTruthAndArchitectureVault.getFullSystemTruthInstruction()
                        clipboardManager.setText(AnnotatedString(fullDossier))
                        Toast.makeText(context, "📋 পূর্ণাঙ্গ সিস্টেম ডসিয়ার ক্লিপবোর্ডে কপি হয়েছে!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFF0F172A), modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ডসিয়ার কপি করুন", color = Color(0xFF0F172A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("বন্ধ করুন", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
private fun ModuleTruthCard(module: AppSourceModuleTruth) {
    Surface(
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = module.fileName,
                        color = Color(0xFF0F172A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = Color(0xFFE2E8F0),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = module.category,
                            color = Color(0xFF475569),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "${module.linesApprox} lines",
                    color = Color(0xFF64748B),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = module.exactRoleBangla,
                color = Color(0xFF334155),
                fontSize = 10.sp,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "বাস্তবতা: ${module.implementationReality}",
                    color = if (module.implementationReality.contains("১০০%")) CyberEmerald else CyberAmber,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun FixHistoryCard(
    title: String,
    status: String,
    statusColor: Color,
    cause: String,
    fixApplied: String
) {
    Surface(
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color(0xFF0F172A),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = status,
                        color = statusColor,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "❌ কারণ: $cause",
                color = Color(0xFFDC2626),
                fontSize = 10.sp,
                lineHeight = 13.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "✅ সমাধান: $fixApplied",
                color = CyberEmerald,
                fontSize = 10.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
