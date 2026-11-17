package com.example.ui.ai

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.*
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * ══════════════════════════════════════════════════════════════════════════════
 * 🎛️ GOOGLE AI STUDIO 0.0° DEVELOPER MODE, LIVE QUOTA HUD, PRO-DEVELOPER TRINITY
 *    (MULTI-FILE IDE, AGENTIC REACT LOOP, LIVE WEB CRAWLER) & 5x7 SCHEDULER UI
 * ══════════════════════════════════════════════════════════════════════════════
 */
@Composable
fun AiStudioZeroDegreeLiveHudBar(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        AiStudioZeroDegreeDevEngine.init(context)
        SovereignScheduledAutoEvolutionDaemon.initAndScheduleAllAlarms(context)
        SovereignProDeveloperTrinityEngine.seedDefaultProMultiFileWorkspace()
        SovereignUltimateGodPairEngine.synthesizeRealLifePredictiveTwinCards(context)
    }

    val devConfig by AiStudioZeroDegreeDevEngine.devConfig.collectAsState()
    val quotaTel by AiStudioZeroDegreeDevEngine.quotaTelemetry.collectAsState()
    val scheduleSlots by SovereignScheduledAutoEvolutionDaemon.scheduleSlots.collectAsState()
    val isWakeupRunning by SovereignScheduledAutoEvolutionDaemon.isSynthesisRunning.collectAsState()
    val wakeupProgress by SovereignScheduledAutoEvolutionDaemon.liveSynthesisProgressBangla.collectAsState()

    var activeProModalTab by remember { mutableStateOf("NONE") } // NONE, PRO_IDE, WEB_CRAWLER, LIFE_TWIN, HARDWARE_DECK

    Column(modifier = modifier.fillMaxWidth()) {
        // COMPACT ALWAYS-VISIBLE LIVE TELEMETRY, 0.0° LOCK & PRO TRINITY QUICK BAR
        Surface(
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 1. 0.0° Zero-Degree Instant Lock Pill
                val isZero = devConfig.isZeroDegreeLocked || devConfig.temperature == 0.0f
                Surface(
                    color = if (isZero) Color(0xFF0284C7) else Color(0xFF334155),
                    shape = RoundedCornerShape(7.dp),
                    border = BorderStroke(0.9.dp, if (isZero) Color(0xFF38BDF8) else Color(0xFF64748B)),
                    modifier = Modifier
                        .clickable {
                            AiStudioZeroDegreeDevEngine.lockZeroDegreeMode(!isZero, context)
                            Toast.makeText(
                                context,
                                if (!isZero) "❄️ 0.0° শূন্য ডিগ্রি (Zero-Degree) লক করা হয়েছে!" else "🌡️ কাস্টম টেম্পারেচার সক্রিয়",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .testTag("hud_zero_degree_pill")
                ) {
                    Text(
                        text = if (isZero) "❄️ 0.0° শূন্য ডিগ্রি লকড" else "🌡️ Temp: ${devConfig.temperature}°",
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // 2. Live Remaining Quota / Limitation Pill
                val quotaColor = when {
                    quotaTel.remainingQuotaPercentage > 50f -> Color(0xFF059669)
                    quotaTel.remainingQuotaPercentage > 20f -> Color(0xFFD97706)
                    else -> Color(0xFFE11D48)
                }
                Surface(
                    color = quotaColor.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(7.dp),
                    border = BorderStroke(0.9.dp, quotaColor),
                    modifier = Modifier
                        .clickable {
                            AiStudioZeroDegreeDevEngine.toggleDeveloperHudExpanded(context)
                        }
                        .testTag("hud_live_quota_remaining_pill")
                ) {
                    Text(
                        text = "📊 লিমিট বাকি: ${quotaTel.rpdRemainingToday}/${quotaTel.rpdLimitTotal} (${String.format("%.0f", quotaTel.remainingQuotaPercentage)}%) • RPM: ${quotaTel.rpmRemainingCurrentMinute}/${quotaTel.rpmLimitTotal}",
                        color = Color(0xFFA7F3D0),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // 3. 1-Tap Slow-Token God-Code Burn Mode Switcher Pill
                Surface(
                    color = Color(0xFF1E1B4B),
                    shape = RoundedCornerShape(7.dp),
                    border = BorderStroke(0.9.dp, Color(0xFF818CF8)),
                    modifier = Modifier
                        .clickable {
                            val nextMode = when (devConfig.quotaBurnMode) {
                                AiStudioZeroDegreeDevEngine.QuotaBurnRateMode.GOD_CODE_SLOW_TOKEN_10X ->
                                    AiStudioZeroDegreeDevEngine.QuotaBurnRateMode.SMART_ADAPTIVE
                                AiStudioZeroDegreeDevEngine.QuotaBurnRateMode.SMART_ADAPTIVE ->
                                    AiStudioZeroDegreeDevEngine.QuotaBurnRateMode.FAST_MAX_OVERCLOCK
                                AiStudioZeroDegreeDevEngine.QuotaBurnRateMode.FAST_MAX_OVERCLOCK ->
                                    AiStudioZeroDegreeDevEngine.QuotaBurnRateMode.GOD_CODE_SLOW_TOKEN_10X
                            }
                            AiStudioZeroDegreeDevEngine.setQuotaBurnMode(nextMode, context)
                            Toast.makeText(context, nextMode.titleBangla, Toast.LENGTH_SHORT).show()
                        }
                        .testTag("hud_burn_rate_mode_pill")
                ) {
                    Text(
                        text = devConfig.quotaBurnMode.badgeText,
                        color = Color(0xFFC7D2FE),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // 3.5. 7-Page Khatarnak Pro Workstation Button (Each Feature on a Separate Dedicated Page, <48KB RAM)
                Surface(
                    color = if (activeProModalTab == "KHATARNAK_7_PAGES") Color(0xFF059669) else Color(0xFF064E3B),
                    shape = RoundedCornerShape(7.dp),
                    border = BorderStroke(0.9.dp, Color(0xFF34D399)),
                    modifier = Modifier
                        .clickable {
                            activeProModalTab = if (activeProModalTab == "KHATARNAK_7_PAGES") "NONE" else "KHATARNAK_7_PAGES"
                        }
                        .testTag("hud_khatarnak_7_pages_btn")
                ) {
                    Text(
                        text = "🔥 ৭-পেজ প্রো ল্যাব (Zero-RAM)",
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // 3.8. Wave-1 (03:45) Pages 8-14 Pro Workstation Pill (<40KB RAM)
                Surface(
                    color = if (activeProModalTab == "WAVE1_PAGES_8_14") Color(0xFF0284C7) else Color(0xFF0C4A6E),
                    shape = RoundedCornerShape(7.dp),
                    border = BorderStroke(0.9.dp, Color(0xFF38BDF8)),
                    modifier = Modifier
                        .clickable {
                            activeProModalTab = if (activeProModalTab == "WAVE1_PAGES_8_14") "NONE" else "WAVE1_PAGES_8_14"
                        }
                        .testTag("hud_wave1_pages_8_14_btn")
                ) {
                    Text(
                        text = "🚀 ৩:৪৫ ল্যাব (পেজ ৮-১৪)",
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // 3.9. Wave-2 (04:45) Pages 15-21 Pro Workstation Pill (<38KB RAM)
                Surface(
                    color = if (activeProModalTab == "WAVE2_PAGES_15_21") Color(0xFF7E22CE) else Color(0xFF3B0764),
                    shape = RoundedCornerShape(7.dp),
                    border = BorderStroke(0.9.dp, Color(0xFFC084FC)),
                    modifier = Modifier
                        .clickable {
                            activeProModalTab = if (activeProModalTab == "WAVE2_PAGES_15_21") "NONE" else "WAVE2_PAGES_15_21"
                        }
                        .testTag("hud_wave2_pages_15_21_btn")
                ) {
                    Text(
                        text = "⚡ ৪:৪৫ ল্যাব (পেজ ১৫-২১)",
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // 3.95. Wave-3 (05:55) Pages 22-28 Pro Workstation Pill (<34KB RAM)
                Surface(
                    color = if (activeProModalTab == "WAVE3_PAGES_22_28") Color(0xFFD97706) else Color(0xFF451A03),
                    shape = RoundedCornerShape(7.dp),
                    border = BorderStroke(0.9.dp, Color(0xFFFBBF24)),
                    modifier = Modifier
                        .clickable {
                            activeProModalTab = if (activeProModalTab == "WAVE3_PAGES_22_28") "NONE" else "WAVE3_PAGES_22_28"
                        }
                        .testTag("hud_wave3_pages_22_28_btn")
                ) {
                    Text(
                        text = "💎 ৫:৫৫ ল্যাব (পেজ ২২-২৮)",
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // 3.98. Wave-4 (07:25) Pages 29-35 Pro Workstation Pill (<34KB RAM)
                Surface(
                    color = if (activeProModalTab == "WAVE4_PAGES_29_35") Color(0xFF059669) else Color(0xFF064E3B),
                    shape = RoundedCornerShape(7.dp),
                    border = BorderStroke(0.9.dp, Color(0xFF34D399)),
                    modifier = Modifier
                        .clickable {
                            activeProModalTab = if (activeProModalTab == "WAVE4_PAGES_29_35") "NONE" else "WAVE4_PAGES_29_35"
                        }
                        .testTag("hud_wave4_pages_29_35_btn")
                ) {
                    Text(
                        text = "👑 ৭:২৫ ল্যাব (পেজ ২৯-৩৫)",
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // 3.99. Wave-5 (08:30) Pages 36-42 Final Pro Workstation Pill (<34KB RAM)
                Surface(
                    color = if (activeProModalTab == "WAVE5_PAGES_36_42") Color(0xFFE11D48) else Color(0xFF4C0519),
                    shape = RoundedCornerShape(7.dp),
                    border = BorderStroke(0.9.dp, Color(0xFFFB7185)),
                    modifier = Modifier
                        .clickable {
                            activeProModalTab = if (activeProModalTab == "WAVE5_PAGES_36_42") "NONE" else "WAVE5_PAGES_36_42"
                        }
                        .testTag("hud_wave5_pages_36_42_btn")
                ) {
                    Text(
                        text = "🏆 ৮:৩০ ল্যাব (পেজ ৩৬-৪২)",
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // 4. Pro System #1: Multi-File IDE & Auto-Debugger Pill
                Surface(
                    color = if (activeProModalTab == "PRO_IDE") Color(0xFF4F46E5) else Color(0xFF1E1B4B),
                    shape = RoundedCornerShape(7.dp),
                    border = BorderStroke(0.9.dp, Color(0xFF6366F1)),
                    modifier = Modifier
                        .clickable {
                            activeProModalTab = if (activeProModalTab == "PRO_IDE") "NONE" else "PRO_IDE"
                        }
                        .testTag("hud_pro_ide_btn")
                ) {
                    Text(
                        text = "💻 প্রো মাল্টি-ফাইল IDE (99%)",
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // 5. Pro System #3: Live Multi-Source Web Research Crawler Pill
                Surface(
                    color = if (activeProModalTab == "WEB_CRAWLER") Color(0xFF0284C7) else Color(0xFF0C4A6E),
                    shape = RoundedCornerShape(7.dp),
                    border = BorderStroke(0.9.dp, Color(0xFF38BDF8)),
                    modifier = Modifier
                        .clickable {
                            activeProModalTab = if (activeProModalTab == "WEB_CRAWLER") "NONE" else "WEB_CRAWLER"
                        }
                        .testTag("hud_live_web_crawler_btn")
                ) {
                    Text(
                        text = "🌐 লাইভ ওয়েব ক্রলার",
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // 6. Normal-Life God Twin & Hardware OS Deck Pill
                Surface(
                    color = if (activeProModalTab == "LIFE_TWIN") Color(0xFFD97706) else Color(0xFF451A03),
                    shape = RoundedCornerShape(7.dp),
                    border = BorderStroke(0.9.dp, Color(0xFFF59E0B)),
                    modifier = Modifier
                        .clickable {
                            SovereignUltimateGodPairEngine.synthesizeRealLifePredictiveTwinCards(context)
                            activeProModalTab = if (activeProModalTab == "LIFE_TWIN") "NONE" else "LIFE_TWIN"
                        }
                        .testTag("hud_normal_life_twin_btn")
                ) {
                    Text(
                        text = "🧠 লাইফ টুইন ও হার্ডওয়্যার ওএস",
                        color = Color(0xFFFEF3C7),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // 7. Expand Full Google AI Studio 0.0° & 5x7 Schedule Deck Button
                Surface(
                    color = if (devConfig.isDeveloperHudExpanded) Color(0xFFE11D48) else Color(0xFF0D9488),
                    shape = RoundedCornerShape(7.dp),
                    modifier = Modifier
                        .clickable {
                            AiStudioZeroDegreeDevEngine.toggleDeveloperHudExpanded(context)
                        }
                        .testTag("toggle_ai_studio_dev_deck_btn")
                ) {
                    Text(
                        text = if (devConfig.isDeveloperHudExpanded) "▲ স্টুডিও গুটিয়ে রাখুন" else "🎛️ AI Studio 0° ও শিডিউল (৫x৭)",
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // INTERACTIVE 7-PAGE KHATARNAK PRO WORKSTATION (EACH FEATURE ON ITS OWN DEDICATED PAGE)
        AnimatedVisibility(visible = activeProModalTab == "KHATARNAK_7_PAGES") {
            SovereignKhatarnak7PagesDeckUi(
                onClose = { activeProModalTab = "NONE" }
            )
        }

        // INTERACTIVE WAVE-1 (03:45) PAGES 8-14 PRO WORKSTATION
        AnimatedVisibility(visible = activeProModalTab == "WAVE1_PAGES_8_14") {
            SovereignWave1Pages8To14Ui(
                onClose = { activeProModalTab = "NONE" }
            )
        }

        // INTERACTIVE WAVE-2 (04:45) PAGES 15-21 PRO WORKSTATION
        AnimatedVisibility(visible = activeProModalTab == "WAVE2_PAGES_15_21") {
            SovereignWave2Pages15To21Ui(
                onClose = { activeProModalTab = "NONE" }
            )
        }

        // INTERACTIVE WAVE-3 (05:55) PAGES 22-28 PRO WORKSTATION
        AnimatedVisibility(visible = activeProModalTab == "WAVE3_PAGES_22_28") {
            SovereignWave3Pages22To28Ui(
                onClose = { activeProModalTab = "NONE" }
            )
        }

        // INTERACTIVE WAVE-4 (07:25) PAGES 29-35 PRO WORKSTATION
        AnimatedVisibility(visible = activeProModalTab == "WAVE4_PAGES_29_35") {
            SovereignWave4Pages29To35Ui(
                onClose = { activeProModalTab = "NONE" }
            )
        }

        // INTERACTIVE WAVE-5 (08:30 FINAL) PAGES 36-42 PRO WORKSTATION
        AnimatedVisibility(visible = activeProModalTab == "WAVE5_PAGES_36_42") {
            SovereignWave5Pages36To42Ui(
                onClose = { activeProModalTab = "NONE" }
            )
        }

        // INTERACTIVE PRO MULTI-FILE IDE & AUTO-DEBUGGER PANEL
        AnimatedVisibility(visible = activeProModalTab == "PRO_IDE") {
            ProMultiFileIdeComposeCard(
                onClose = { activeProModalTab = "NONE" }
            )
        }

        // INTERACTIVE LIVE MULTI-SOURCE WEB RESEARCH CRAWLER PANEL
        AnimatedVisibility(visible = activeProModalTab == "WEB_CRAWLER") {
            ProLiveWebCrawlerComposeCard(
                onClose = { activeProModalTab = "NONE" }
            )
        }

        // INTERACTIVE NORMAL-LIFE GOD TWIN & PHONE HARDWARE OS PANEL
        AnimatedVisibility(visible = activeProModalTab == "LIFE_TWIN") {
            NormalLifeTwinAndHardwareOsCard(
                onClose = { activeProModalTab = "NONE" }
            )
        }

        // EXPANDABLE FULL GOOGLE AI STUDIO 0.0° + 10X DEVELOPER & 5-WAKEUP SCHEDULER DECK
        AnimatedVisibility(visible = devConfig.isDeveloperHudExpanded) {
            AiStudioFullDeveloperAndScheduleCard(
                devConfig = devConfig,
                quotaTel = quotaTel,
                scheduleSlots = scheduleSlots,
                isWakeupRunning = isWakeupRunning,
                wakeupProgress = wakeupProgress,
                onClose = { AiStudioZeroDegreeDevEngine.toggleDeveloperHudExpanded(context) }
            )
        }
    }
}

@Composable
private fun ProMultiFileIdeComposeCard(
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val vfsFiles by SovereignProDeveloperTrinityEngine.vfsFiles.collectAsState()
    val trinityTel by SovereignProDeveloperTrinityEngine.telemetry.collectAsState()
    var selectedPath by remember { mutableStateOf("index.html") }
    var editorText by remember(selectedPath, vfsFiles) {
        mutableStateOf(vfsFiles[selectedPath]?.content ?: "")
    }

    Surface(
        color = Color(0xFF0F172A),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.4.dp, Color(0xFF6366F1)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💻 প্রো মাল্টি-ফাইল IDE, VFS ও অটো-ডিবাগার (${trinityTel.vfsFilesCount} ফাইল)",
                    color = Color(0xFFC7D2FE),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Black
                )
                Surface(
                    color = Color(0xFF881337),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.clickable { onClose() }
                ) {
                    Text(
                        text = "✕ বন্ধ",
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // VFS File Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                vfsFiles.keys.forEach { filePath ->
                    val isSel = filePath == selectedPath
                    Surface(
                        color = if (isSel) Color(0xFF4F46E5) else Color(0xFF1E293B),
                        shape = RoundedCornerShape(7.dp),
                        border = BorderStroke(0.8.dp, if (isSel) Color(0xFF818CF8) else Color(0xFF475569)),
                        modifier = Modifier.clickable {
                            selectedPath = filePath
                            editorText = vfsFiles[filePath]?.content ?: ""
                        }
                    ) {
                        Text(
                            text = "📄 $filePath",
                            color = Color.White,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = editorText,
                onValueChange = { editorText = it },
                textStyle = TextStyle(
                    color = Color(0xFF6EE7B7),
                    fontSize = 10.5.sp,
                    fontFamily = FontFamily.Monospace
                ),
                maxLines = 8,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 110.dp, max = 165.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6366F1),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedContainerColor = Color(0xFF020617),
                    unfocusedContainerColor = Color(0xFF020617)
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = {
                        SovereignProDeveloperTrinityEngine.writeVfsFile(selectedPath, editorText)
                        val bundled = SovereignProDeveloperTrinityEngine.compileMultiFileVfsIntoSelfDebuggingBundle()
                        val mod = SelfFeatureModule(
                            id = "pro_vfs_live",
                            title = "💻 Pro Multi-File VFS App",
                            description = "Compiled from index.html + styles.css + app.js",
                            categoryBadge = "PRO VFS 99%",
                            jsPayload = bundled,
                            isAutoRunOnPageLoad = true
                        )
                        MasterOmniControllerEngine.addOrUpdateSelfFeature(context, mod)
                        AutonomousSelfEvolutionEngine.launchModuleInLiveRunner(mod, fullScreen = false)
                        Toast.makeText(context, "▶️ মাল্টি-ফাইল VFS কম্পাইল ও লাইভ রানারে চালু হয়েছে!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("▶️ সেভ, কম্পাইল ও লাইভ রান", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
                }

                Button(
                    onClick = {
                        SovereignProDeveloperTrinityEngine.writeVfsFile(selectedPath, editorText)
                        val zip = SovereignProDeveloperTrinityEngine.exportApkConverterReadyWebZip(context)
                        Toast.makeText(context, "📦 এক্সপোর্ট সম্পন্ন: ${zip.name}", Toast.LENGTH_LONG).show()
                        ChatSessionManager.addMessage(
                            context,
                            "AI",
                            "📦 **[প্রো মাল্টি-ফাইল ZIP বান্ডলার]:** আপনার প্রজেক্টটি (`index.html`, `styles.css`, `app.js`, `manifest.json`) `${zip.absolutePath}` ফাইলে সেভ করা হয়েছে!",
                            false
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("📦 মাল্টি-ফাইল .ZIP এক্সপোর্ট", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
private fun ProLiveWebCrawlerComposeCard(
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("Kotlin Multiplatform Jetpack Compose 2026") }
    var isSearching by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Wikipedia REST API + GitHub API + DuckDuckGo থেকে রিয়েল-টাইম তথ্য ও সোর্স লিংক ক্রল করুন।") }

    Surface(
        color = Color(0xFF0F172A),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.4.dp, Color(0xFF0284C7)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🌐 প্রো মাল্টি-সোর্স লাইভ ওয়েব রিসার্চ ও গ্রাউন্ডেড সাইটেশন ক্রলার",
                    color = Color(0xFF7DD3FC),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
                Surface(
                    color = Color(0xFF881337),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.clickable { onClose() }
                ) {
                    Text(
                        text = "✕ বন্ধ",
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 11.sp),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF020617),
                        unfocusedContainerColor = Color(0xFF020617)
                    )
                )
                Button(
                    enabled = !isSearching && searchQuery.isNotBlank(),
                    onClick = {
                        isSearching = true
                        statusText = "⏳ Wikipedia, GitHub ও DuckDuckGo API থেকে রিয়েল-টাইম তথ্য ক্রল হচ্ছে..."
                        scope.launch {
                            val items = SovereignProDeveloperTrinityEngine.searchLiveWebMultiSource(searchQuery, maxResults = 5)
                            isSearching = false
                            if (items.isNotEmpty()) {
                                statusText = "✅ ${items.size}টি ভেরিফায়েড সোর্স পাওয়া গেছে এবং চ্যাটে যুক্ত করা হয়েছে!"
                                val formatted = items.mapIndexed { idx, it ->
                                    "${idx + 1}. **[${it.title}](${it.url})** (${it.sourceEngine})\n   ${it.snippet}"
                                }.joinToString("\n\n")
                                ChatSessionManager.addMessage(
                                    context,
                                    "AI",
                                    "🌐 **[প্রো লাইভ ওয়েব রিসার্চ ও গ্রাউন্ডেড সাইটেশন রিপোর্ট (`$searchQuery`)]:**\n\n$formatted",
                                    false
                                )
                            } else {
                                statusText = "⚠️ কোনো ফলাফল পাওয়া যায়নি; অন্য কিওয়ার্ড দিন।"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (isSearching) "⏳..." else "🔍 ক্রল ও চ্যাটে আনুন", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = statusText, color = Color(0xFF94A3B8), fontSize = 9.5.sp)
        }
    }
}

@Composable
private fun NormalLifeTwinAndHardwareOsCard(
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifeCards by SovereignUltimateGodPairEngine.zeroClickLifeCards.collectAsState()

    Surface(
        color = Color(0xFF0F172A),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.4.dp, Color(0xFFF59E0B)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🧠 নরমাল-লাইফ গড টুইন (০-ক্লিক অটোপাইলট) ও ফোন হার্ডওয়্যার ডেক",
                    color = Color(0xFFFDE68A),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
                Surface(
                    color = Color(0xFF881337),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.clickable { onClose() }
                ) {
                    Text(
                        text = "✕ বন্ধ",
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 1-Tap Hardware OS & APK/ZIP Export Quick Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "🔦 ফ্ল্যাশলাইট টগল" to "ফ্ল্যাশলাইট অন করো",
                    "🔊 ভলিউম ম্যাক্স" to "ভলিউম ফুল করো",
                    "📸 স্ক্রিনশট" to "স্ক্রিনশট নাও",
                    "🏠 ডিফল্ট হোম অ্যাসিস্ট্যান্ট" to "ডিফল্ট অ্যাসিস্ট্যান্ট সেটিংস খোলো",
                    "🎨 8K AI ইমেজ স্টুডিও" to "একটি সাইবারপাঙ্ক ফিউচারিস্টিক শহরের ছবি বানাও",
                    "✅ ২৮-ইঞ্জিন ফুল অডিট" to "অডিট করো",
                    "💾 ফুল স্ন্যাপশট ব্যাকআপ" to "ফুল ব্যাকআপ করো"
                ).forEach { (label, cmd) ->
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(7.dp),
                        border = BorderStroke(0.8.dp, Color(0xFFF59E0B)),
                        modifier = Modifier.clickable {
                            val rep = SovereignMetaKernelEngine.executeKingSupremeCommand(context, cmd, null)
                            if (rep != null) {
                                ChatSessionManager.addMessage(context, "AI", rep, false)
                            }
                        }
                    ) {
                        Text(
                            text = label,
                            color = Color(0xFFFEF3C7),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Zero-Click Life Telepathy Cards
            lifeCards.forEach { card ->
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.8.dp, Color(0xFF38BDF8)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clickable {
                            val rep = SovereignMetaKernelEngine.executeKingSupremeCommand(context, card.actionCommandToExecute, null)
                            ChatSessionManager.addMessage(
                                context,
                                "AI",
                                rep ?: "🧠 **[নরমাল-লাইফ গড টুইন]:** `${card.titleBangla}` — আদেশ চ্যাটে প্রেরিত হয়েছে:\n`${card.actionCommandToExecute}`",
                                false
                            )
                            onClose()
                        }
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = card.badgeBangla, color = Color(0xFF38BDF8), fontSize = 9.sp, fontWeight = FontWeight.Black)
                        Text(text = card.titleBangla, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AiStudioFullDeveloperAndScheduleCard(
    devConfig: AiStudioZeroDegreeDevEngine.DeveloperStudioConfig,
    quotaTel: AiStudioZeroDegreeDevEngine.ApiQuotaLimitationTelemetry,
    scheduleSlots: List<SovereignScheduledAutoEvolutionDaemon.WakeupScheduleSlot>,
    isWakeupRunning: Boolean,
    wakeupProgress: String,
    onClose: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var boldInstructionDraft by remember(devConfig.boldHardenedSystemInstruction) {
        mutableStateOf(devConfig.boldHardenedSystemInstruction)
    }

    Surface(
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.4.dp, Color(0xFF0284C7)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎛️ Google AI Studio 0.0° + 10x গড-লেভেল ডেভেলপার ডেক",
                    color = Color(0xFF0F172A),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
                if (onClose != null) {
                    Surface(
                        color = Color(0xFFFFE4E6),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.clickable { onClose() }
                    ) {
                        Text(
                            text = "✕ বন্ধ",
                            color = Color(0xFFE11D48),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 1. LIVE API QUOTA / LIMITATION REMAINING TELEMETRY BOX
            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFF059669)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(9.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📊 API Key লিমিটেশন এখন কতটি বাকি আছে (Live Telemetry):",
                            color = Color(0xFF34D399),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${String.format("%.1f", quotaTel.remainingQuotaPercentage)}% বাকি",
                            color = Color(0xFF38BDF8),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    // Universal Box-based progress bar (100% compatible across all Compose Material3 versions)
                    val progressFraction = (quotaTel.remainingQuotaPercentage / 100f).coerceIn(0.02f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF334155))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressFraction)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF10B981))
                        )
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = "• এই মিনিটে বাকি (RPM): ${quotaTel.rpmRemainingCurrentMinute}/${quotaTel.rpmLimitTotal} | আজকের মোট বাকি (RPD): ${quotaTel.rpdRemainingToday}/${quotaTel.rpdLimitTotal}\n" +
                            "• শেষ কলের টোকেন: মোট ${quotaTel.lastTotalTokens} ( ইনপুট: ${quotaTel.lastPromptTokens}, আউটপুট: ${quotaTel.lastCandidateOutputTokens}, থিংকিং: ${quotaTel.lastThinkingTokens} )\n" +
                            "• সেশনে ব্যবহৃত টোকেন: ${AiStudioZeroDegreeDevEngine.formatCompactNumber(quotaTel.sessionTotalTokens)} | 🛡️ গভর্নর বাঁচিয়েছে: ${AiStudioZeroDegreeDevEngine.formatCompactNumber(quotaTel.sessionSavedTokensByGovernor)} টোকেন\n" +
                            "• ${quotaTel.limitationForecastBangla}",
                        color = Color(0xFFE2E8F0),
                        fontSize = 9.5.sp,
                        lineHeight = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. QUOTA BURN-RATE GOVERNOR (আস্তে আস্তে স্লো টোকেন বনাম জলদি ফাস্ট বার্ন)
            Text(
                text = "⚡ লিমিটেশন খরচের গতি (গড-লেভেল কোড কিন্তু স্লো টোকেন বনাম ফাস্ট বার্ন):",
                color = Color(0xFF0F172A),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AiStudioZeroDegreeDevEngine.QuotaBurnRateMode.values().forEach { mode ->
                    val selected = devConfig.quotaBurnMode == mode
                    Surface(
                        color = if (selected) Color(0xFF059669) else Color.White,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (selected) Color(0xFF059669) else Color(0xFF94A3B8)),
                        modifier = Modifier.clickable {
                            AiStudioZeroDegreeDevEngine.setQuotaBurnMode(mode, context)
                            Toast.makeText(context, mode.titleBangla, Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text(
                            text = mode.titleBangla,
                            color = if (selected) Color.White else Color(0xFF1E293B),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            Text(
                text = devConfig.quotaBurnMode.descriptionBangla,
                color = Color(0xFF475569),
                fontSize = 9.sp,
                modifier = Modifier.padding(top = 3.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. TEMPERATURE 0.0° (শূন্য ডিগ্রি) SLIDER & PRESETS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🌡️ Temperature ইনপুট কন্ট্রোল: ${if (devConfig.isZeroDegreeLocked) "0.0° (শূন্য ডিগ্রি লকড)" else "${devConfig.temperature}°"}",
                    color = Color(0xFF0284C7),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
                Surface(
                    color = if (devConfig.isZeroDegreeLocked) Color(0xFF0284C7) else Color(0xFFE2E8F0),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.clickable {
                        AiStudioZeroDegreeDevEngine.lockZeroDegreeMode(true, context)
                    }
                ) {
                    Text(
                        text = "❄️ 0.0° শূন্য ডিগ্রিতে নামান",
                        color = if (devConfig.isZeroDegreeLocked) Color.White else Color(0xFF0F172A),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Slider(
                value = if (devConfig.isZeroDegreeLocked) 0.0f else devConfig.temperature,
                onValueChange = { AiStudioZeroDegreeDevEngine.setTemperature(it, context) },
                valueRange = 0.0f..2.0f,
                steps = 19,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                listOf(
                    0.0f to "❄️ 0.0° (Zero)",
                    0.1f to "0.1° (Code)",
                    0.35f to "0.35° (Balanced)",
                    0.7f to "0.7° (Creative)",
                    1.0f to "1.0° (High)",
                    1.5f to "1.5° (Max)"
                ).forEach { (tempVal, label) ->
                    val active = (!devConfig.isZeroDegreeLocked && devConfig.temperature == tempVal) ||
                        (devConfig.isZeroDegreeLocked && tempVal == 0.0f)
                    Surface(
                        color = if (active) Color(0xFF0284C7) else Color.White,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.8.dp, Color(0xFF0284C7)),
                        modifier = Modifier.clickable {
                            AiStudioZeroDegreeDevEngine.setTemperature(tempVal, context)
                        }
                    ) {
                        Text(
                            text = label,
                            color = if (active) Color.White else Color(0xFF0284C7),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. THINKING BUDGET, MAX OUTPUT TOKENS, TOP-P, TOP-K, SEED & SAFETY TOGGLES
            Text(
                text = "🧠 ডিপ-থিংকিং বাজেট, আউটপুট টোকেন ও অ্যাডভান্সড AI Studio অপশন:",
                color = Color(0xFF0F172A),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                listOf(
                    0 to "Thinking: OFF",
                    2048 to "🧠 2K স্লো-টোকেন",
                    8192 to "🧠 8K ডিপ",
                    16384 to "🧠 16K প্রো",
                    32768 to "🧠 32K গড-ম্যাক্স"
                ).forEach { (budget, label) ->
                    val active = devConfig.thinkingBudgetTokens == budget
                    Surface(
                        color = if (active) Color(0xFF7C3AED) else Color.White,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.8.dp, Color(0xFF7C3AED)),
                        modifier = Modifier.clickable {
                            AiStudioZeroDegreeDevEngine.setThinkingBudgetTokens(budget, context)
                        }
                    ) {
                        Text(
                            text = label,
                            color = if (active) Color.White else Color(0xFF7C3AED),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                listOf(
                    4096 to "Out: 4K",
                    16384 to "Out: 16K",
                    32768 to "Out: 32K",
                    65536 to "Out: 65K (Max)"
                ).forEach { (maxTok, label) ->
                    val active = devConfig.maxOutputTokens == maxTok
                    Surface(
                        color = if (active) Color(0xFF0D9488) else Color.White,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.8.dp, Color(0xFF0D9488)),
                        modifier = Modifier.clickable {
                            AiStudioZeroDegreeDevEngine.setMaxOutputTokens(maxTok, context)
                        }
                    ) {
                        Text(
                            text = label,
                            color = if (active) Color.White else Color(0xFF0D9488),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                        )
                    }
                }

                // BLOCK_NONE Safety Toggle
                Surface(
                    color = if (devConfig.unlockBlockNoneSafety) Color(0xFFE11D48) else Color.White,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(0.8.dp, Color(0xFFE11D48)),
                    modifier = Modifier.clickable {
                        AiStudioZeroDegreeDevEngine.setUnlockBlockNoneSafety(!devConfig.unlockBlockNoneSafety, context)
                    }
                ) {
                    Text(
                        text = if (devConfig.unlockBlockNoneSafety) "🔓 Safety: BLOCK_NONE" else "🔒 Safety: Standard",
                        color = if (devConfig.unlockBlockNoneSafety) Color.White else Color(0xFFE11D48),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                    )
                }

                // Search Grounding Toggle
                Surface(
                    color = if (devConfig.enableGoogleSearchGrounding) Color(0xFF2563EB) else Color.White,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(0.8.dp, Color(0xFF2563EB)),
                    modifier = Modifier.clickable {
                        AiStudioZeroDegreeDevEngine.setGoogleSearchGrounding(!devConfig.enableGoogleSearchGrounding, context)
                    }
                ) {
                    Text(
                        text = if (devConfig.enableGoogleSearchGrounding) "🌐 Search Grounding: ON" else "🌐 Grounding: OFF",
                        color = if (devConfig.enableGoogleSearchGrounding) Color.White else Color(0xFF2563EB),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                    )
                }

                // Code Execution Tool Toggle
                Surface(
                    color = if (devConfig.enableCodeExecutionSandbox) Color(0xFF059669) else Color.White,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(0.8.dp, Color(0xFF059669)),
                    modifier = Modifier.clickable {
                        AiStudioZeroDegreeDevEngine.setCodeExecutionSandbox(!devConfig.enableCodeExecutionSandbox, context)
                    }
                ) {
                    Text(
                        text = if (devConfig.enableCodeExecutionSandbox) "💻 Code Sandbox: ON" else "💻 Code Sandbox: OFF",
                        color = if (devConfig.enableCodeExecutionSandbox) Color.White else Color(0xFF059669),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 5. BOLD HARDENED CODE-LEVEL SYSTEM INSTRUCTION
            Text(
                text = "🖤 গাঢ় (Bold Hardened) কোড-লেভেল কন্ট্রোল ইনস্ট্রাকশন:",
                color = Color(0xFF0F172A),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = boldInstructionDraft,
                    onValueChange = { boldInstructionDraft = it },
                    textStyle = TextStyle(
                        color = Color(0xFF0F172A),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    ),
                    maxLines = 2,
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0F172A),
                        unfocusedBorderColor = Color(0xFF475569),
                        focusedContainerColor = Color(0xFFFEF9C3),
                        unfocusedContainerColor = Color(0xFFFEF9C3)
                    )
                )
                Button(
                    onClick = {
                        AiStudioZeroDegreeDevEngine.setBoldHardenedSystemInstruction(boldInstructionDraft, context)
                        Toast.makeText(context, "🖤 গাঢ় কোড-লেভেল ইনস্ট্রাকশন লক করা হয়েছে!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text("🔒 সেভ", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 6. PERMANENT 5-TIME SCHEDULED AUTO-WAKEUP & 7x GOD-LEVEL FEATURE SYNTHESIZER
            Surface(
                color = Color(0xFFECFDF5),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.2.dp, Color(0xFF059669)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(9.dp)) {
                    Text(
                        text = "⏰ পার্মানেন্ট ৫-বার অটো-জাগরণ (৩:৪৫ • ৪:৪৫ • ৫:৫৫ • ৭:২৫ • ৮:৩০ — প্রতিবারে ৭টি গড-লেভেল কোড ফিচার):",
                        color = Color(0xFF065F46),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = wakeupProgress,
                        color = Color(0xFF047857),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    scheduleSlots.forEach { slot ->
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(0.8.dp, Color(0xFFA7F3D0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${slot.labelBangla} — ${slot.featuresPerWakeup}টি গড-লেভেল ফিচার",
                                        color = Color(0xFF0F172A),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = slot.focusDomainBangla,
                                        color = Color(0xFF475569),
                                        fontSize = 8.5.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Surface(
                                        color = if (isWakeupRunning) Color(0xFF94A3B8) else Color(0xFF059669),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.clickable(enabled = !isWakeupRunning) {
                                            SovereignScheduledAutoEvolutionDaemon.triggerWakeup7FeatureSynthesis(
                                                context = context,
                                                slotIndex = slot.slotIndex,
                                                isManualTrigger = true
                                            )
                                            Toast.makeText(
                                                context,
                                                "⚡ ${slot.labelBangla}-এর ৭টি গড-লেভেল ফিচার তৈরি শুরু হয়েছে!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    ) {
                                        Text(
                                            text = "⚡ এখনই ৭টি যোগ",
                                            color = Color.White,
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                        )
                                    }
                                    Switch(
                                        checked = slot.isEnabled,
                                        onCheckedChange = { enabled ->
                                            SovereignScheduledAutoEvolutionDaemon.toggleSlotEnabled(
                                                context,
                                                slot.slotIndex,
                                                enabled
                                            )
                                        },
                                        modifier = Modifier.height(24.dp)
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
