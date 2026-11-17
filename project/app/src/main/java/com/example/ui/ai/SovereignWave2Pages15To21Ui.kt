package com.example.ui.ai

import com.example.engine.SovereignProWave5LlmWasmAndMasterAuditEngine

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.SovereignProWave2NetworkAndSystemEngine

/**
 * ══════════════════════════════════════════════════════════════════════════════
 * 👑 WAVE-2 (04:45 PRO UPGRADE): 7 SEPARATE DEDICATED PRO-DEVELOPER PAGES (15–21)
 * ══════════════════════════════════════════════════════════════════════════════
 */
@Composable
fun SovereignWave2Pages15To21Ui(
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val activePage by SovereignProWave2NetworkAndSystemEngine.activeWave2Page.collectAsState()

    Surface(
        color = Color(0xFF080C18),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, Color(0xFFA855F7)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag("wave2_pages_15_to_21_workstation")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "⚡ ৪:৪৫ প্রো ওয়ার্কস্টেশন — পেজ ১৫ থেকে ২১ (Zero-RAM < 38KB)",
                        color = Color(0xFFC084FC),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "${activePage.fullTitleBangla} • RAM বাজেট: মাত্র ${activePage.ramKb} KB",
                        color = Color(0xFF94A3B8),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    color = Color(0xFF881337),
                    shape = RoundedCornerShape(7.dp),
                    modifier = Modifier.clickable { onClose() }
                ) {
                    Text(
                        text = "✕ বন্ধ",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(7.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SovereignProWave2NetworkAndSystemEngine.Wave2ProPage.values().forEach { page ->
                    val isSelected = activePage == page
                    Surface(
                        color = if (isSelected) Color(0xFF7E22CE) else Color(0xFF1E293B),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFFC084FC) else Color(0xFF334155)
                        ),
                        modifier = Modifier
                            .clickable {
                                SovereignProWave2NetworkAndSystemEngine.selectWave2Page(page)
                            }
                            .testTag("wave2_page_tab_${page.pageNumber}")
                    ) {
                        Text(
                            text = page.tabLabelBangla,
                            color = Color.White,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ⚡ REAL LIVE ACTION EXECUTION BAR (CONNECTS EVERY PAGE DIRECTLY TO REAL APP RUNNER, REAL AI CHAT, REAL DB & ANDROID OS)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    color = Color(0xFF065F46),
                    shape = RoundedCornerShape(7.dp),
                    border = BorderStroke(1.dp, Color(0xFF34D399)),
                    modifier = Modifier
                        .clickable {
                            val msg = SovereignProWave5LlmWasmAndMasterAuditEngine.deployToRealLiveAppRunner(
                                context = context,
                                title = activePage.tabLabelBangla,
                                contentOrHtml = activePage.fullTitleBangla
                            )
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                        .testTag("wave2_real_deploy_runner_btn")
                ) {
                    Text(
                        text = "🚀 অ্যাপে লাইভ রানার চালু",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = Color(0xFF1E3A8A),
                    shape = RoundedCornerShape(7.dp),
                    border = BorderStroke(1.dp, Color(0xFF60A5FA)),
                    modifier = Modifier
                        .clickable {
                            val msg = SovereignProWave5LlmWasmAndMasterAuditEngine.sendOutputToRealAiChat(
                                context = context,
                                title = activePage.tabLabelBangla,
                                content = activePage.fullTitleBangla
                            )
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                        .testTag("wave2_real_send_chat_btn")
                ) {
                    Text(
                        text = "🤖 সরাসরি AI চ্যাটে পাঠান",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = Color(0xFF581C87),
                    shape = RoundedCornerShape(7.dp),
                    border = BorderStroke(1.dp, Color(0xFFC084FC)),
                    modifier = Modifier
                        .clickable {
                            val dbDump = SovereignProWave5LlmWasmAndMasterAuditEngine.syncRealAppStateIntoInMemorySqlite(context)
                            SovereignProWave5LlmWasmAndMasterAuditEngine.sendOutputToRealAiChat(
                                context = context,
                                title = "Real App SQLite Sync",
                                content = dbDump
                            )
                            Toast.makeText(context, "📊 অ্যাপের আসল ডাটাবেস SQLite-এ সিঙ্ক ও চ্যাটে পাঠানো হয়েছে!", Toast.LENGTH_SHORT).show()
                        }
                        .testTag("wave2_real_sqlite_sync_btn")
                ) {
                    Text(
                        text = "📊 আসল অ্যাপ DB সিঙ্ক",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = Color(0xFF9A3412),
                    shape = RoundedCornerShape(7.dp),
                    border = BorderStroke(1.dp, Color(0xFFFB923C)),
                    modifier = Modifier
                        .clickable {
                            SovereignProWave5LlmWasmAndMasterAuditEngine.shareOrExportViaAndroidOs(
                                context = context,
                                title = activePage.fullTitleBangla,
                                content = activePage.fullTitleBangla
                            )
                        }
                        .testTag("wave2_real_os_share_btn")
                ) {
                    Text(
                        text = "📤 ফোনে শেয়ার / এক্সপোর্ট",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            when (activePage) {
                SovereignProWave2NetworkAndSystemEngine.Wave2ProPage.PAGE_15_APK_XRAY -> Page15ApkManifestXRayLab(context)
                SovereignProWave2NetworkAndSystemEngine.Wave2ProPage.PAGE_16_NET_DNS -> Page16NetworkAndDnsScannerLab(context)
                SovereignProWave2NetworkAndSystemEngine.Wave2ProPage.PAGE_17_R8_PROGUARD -> Page17ProGuardRuleCompilerLab(context)
                SovereignProWave2NetworkAndSystemEngine.Wave2ProPage.PAGE_18_CRON_EPOCH -> Page18CronAndEpochCalculatorLab()
                SovereignProWave2NetworkAndSystemEngine.Wave2ProPage.PAGE_19_SSE_STREAM -> Page19SseStreamParserLab()
                SovereignProWave2NetworkAndSystemEngine.Wave2ProPage.PAGE_20_DISK_TREE -> Page20InternalDiskTreeLab(context)
                SovereignProWave2NetworkAndSystemEngine.Wave2ProPage.PAGE_21_JUNIT_FUZZER -> Page21BoundaryFuzzerAndJUnitLab(context)
            }
        }
    }
}

@Composable
private fun Page15ApkManifestXRayLab(context: Context) {
    var pkgInput by remember { mutableStateOf("") }
    var xrayReport by remember {
        mutableStateOf(SovereignProWave2NetworkAndSystemEngine.inspectRunningPackageXRay(context))
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(
                value = pkgInput,
                onValueChange = { pkgInput = it },
                placeholder = { Text("খালি রাখলে এই অ্যাপের APK এক্স-রে করবে", fontSize = 9.sp) },
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    xrayReport = SovereignProWave2NetworkAndSystemEngine.inspectRunningPackageXRay(context, pkgInput)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E22CE)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("📦 APK এক্স-রে", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        Surface(
            color = Color(0xFF020617),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(0.8.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = xrayReport,
                color = Color(0xFFE2E8F0),
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
private fun Page16NetworkAndDnsScannerLab(context: Context) {
    var netReport by remember {
        mutableStateOf(SovereignProWave2NetworkAndSystemEngine.analyzeActiveNetworkAndDns(context))
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Button(
            onClick = { netReport = SovereignProWave2NetworkAndSystemEngine.analyzeActiveNetworkAndDns(context) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("🔄 লাইভ ব্যান্ডউইথ ও DNS স্ক্যান", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
        }
        Surface(
            color = Color(0xFF020617),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(0.8.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = netReport,
                color = Color(0xFF7DD3FC),
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
private fun Page17ProGuardRuleCompilerLab(context: Context) {
    var pkgPrefix by remember { mutableStateOf("com.example") }
    var rulesText by remember {
        mutableStateOf(SovereignProWave2NetworkAndSystemEngine.generateCrashProofProGuardRules("com.example"))
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(
                value = pkgPrefix,
                onValueChange = { pkgPrefix = it },
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    rulesText = SovereignProWave2NetworkAndSystemEngine.generateCrashProofProGuardRules(pkgPrefix)
                    copyWave2Clip(context, "ProGuard Rules", rulesText)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("📋 জেনারেট ও কপি", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        Surface(
            color = Color(0xFF020617),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(0.8.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = rulesText,
                color = Color(0xFF6EE7B7),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
private fun Page18CronAndEpochCalculatorLab() {
    var cronExpr by remember { mutableStateOf("45 4 * * *") }
    var cronOut by remember {
        mutableStateOf(SovereignProWave2NetworkAndSystemEngine.evaluateCronAndEpochSchedule("45 4 * * *"))
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(
                value = cronExpr,
                onValueChange = { cronExpr = it },
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = { cronOut = SovereignProWave2NetworkAndSystemEngine.evaluateCronAndEpochSchedule(cronExpr) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("⏱️ পরবর্তী ৫টি রান দেখুন", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        Surface(
            color = Color(0xFF020617),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(0.8.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = cronOut,
                color = Color(0xFFFEF3C7),
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
private fun Page19SseStreamParserLab() {
    var sseInput by remember {
        mutableStateOf(
            "data: {\"choices\":[{\"delta\":{\"content\":\"👑 SKRL 3 Pro \"}}]}\n" +
                "data: {\"choices\":[{\"delta\":{\"content\":\"0.0° Stream Active!\"}}]}\n" +
                "data: [DONE]"
        )
    }
    var sseReport by remember {
        mutableStateOf(SovereignProWave2NetworkAndSystemEngine.parseSseChunksAndBenchmarkTps(sseInput))
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(
            value = sseInput,
            onValueChange = { sseInput = it },
            textStyle = TextStyle(color = Color(0xFF7DD3FC), fontSize = 9.5.sp, fontFamily = FontFamily.Monospace),
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { sseReport = SovereignProWave2NetworkAndSystemEngine.parseSseChunksAndBenchmarkTps(sseInput) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("📡 SSE চাঙ্ক পার্স ও TPS টেস্ট", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
        }
        Surface(
            color = Color(0xFF020617),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(0.8.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = sseReport, color = Color(0xFFE2E8F0), fontSize = 9.5.sp, modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
private fun Page20InternalDiskTreeLab(context: Context) {
    var diskReport by remember {
        mutableStateOf(SovereignProWave2NetworkAndSystemEngine.inspectInternalSandboxDiskTree(context))
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Button(
            onClick = { diskReport = SovereignProWave2NetworkAndSystemEngine.inspectInternalSandboxDiskTree(context) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("🗂️ ইন্টারনাল ফাইল ট্রি রিফ্রেশ", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
        }
        Surface(
            color = Color(0xFF020617),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(0.8.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = diskReport, color = Color(0xFFA7F3D0), fontSize = 9.5.sp, modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
private fun Page21BoundaryFuzzerAndJUnitLab(context: Context) {
    var fnName by remember { mutableStateOf("executeKingSupremeCommand") }
    var junitCode by remember {
        mutableStateOf(SovereignProWave2NetworkAndSystemEngine.generateBoundaryFuzzSuiteAndJUnitCode("executeKingSupremeCommand"))
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(
                value = fnName,
                onValueChange = { fnName = it },
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    junitCode = SovereignProWave2NetworkAndSystemEngine.generateBoundaryFuzzSuiteAndJUnitCode(fnName)
                    copyWave2Clip(context, "JUnit4 Fuzz Suite", junitCode)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E22CE)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("🧪 JUnit4 ফাজ কোড কপি", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        Surface(
            color = Color(0xFF020617),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(0.8.dp, Color(0xFF334155)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = junitCode,
                color = Color(0xFFD8B4FE),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

private fun copyWave2Clip(context: Context, label: String, text: String) {
    try {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        cm?.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, "📋 $label কপি হয়েছে!", Toast.LENGTH_SHORT).show()
    } catch (_: Exception) {
    }
}
