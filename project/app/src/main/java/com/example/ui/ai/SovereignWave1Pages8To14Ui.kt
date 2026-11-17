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
import com.example.engine.SovereignProWave1CompilerAndSqlEngine

/**
 * ══════════════════════════════════════════════════════════════════════════════
 * 👑 WAVE-1 (03:45 PRO UPGRADE): 7 SEPARATE DEDICATED PRO-DEVELOPER PAGES (8–14)
 * ══════════════════════════════════════════════════════════════════════════════
 *
 * - Page 8: 🗄️ ইন-মেমোরি SQLite কোয়েরি ও স্কিমা ইঞ্জিন (0 Disk, < 8KB RAM)
 * - Page 9: 🔍 রেজেক্স ক্যাপচার গ্রুপ ও ReDoS অ্যানালাইজার
 * - Page 10: 📦 JSON থেকে Kotlin Data Class ও TypeScript কম্পাইলার
 * - Page 11: ⚡ প্রম্পট টোকেন এক্স-রে ও ডেনসিটি অপ্টিমাইজার
 * - Page 12: 🧵 লাইভ JVM থ্রেড ডাম্প ও ANR ডিটেক্টর
 * - Page 13: 🎨 WCAG 2.1 কনট্রাস্ট ও Compose কালার ল্যাব
 * - Page 14: 🛡️ ১০০° স্ট্যাটিক কোড লিক ও বাগ স্ক্যানার
 */
@Composable
fun SovereignWave1Pages8To14Ui(
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val activePage by SovereignProWave1CompilerAndSqlEngine.activeWave1Page.collectAsState()

    Surface(
        color = Color(0xFF070B14),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, Color(0xFF38BDF8)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag("wave1_pages_8_to_14_workstation")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🚀 ৩:৪৫ প্রো ওয়ার্কস্টেশন — পেজ ৮ থেকে ১৪ (Zero-RAM < 40KB)",
                        color = Color(0xFF38BDF8),
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

            // 7 SEPARATE PAGE SELECTOR TABS (PAGES 8 TO 14)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SovereignProWave1CompilerAndSqlEngine.Wave1ProPage.values().forEach { page ->
                    val isSelected = activePage == page
                    Surface(
                        color = if (isSelected) Color(0xFF0284C7) else Color(0xFF1E293B),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155)
                        ),
                        modifier = Modifier
                            .clickable {
                                SovereignProWave1CompilerAndSqlEngine.selectWave1Page(page)
                            }
                            .testTag("wave1_page_tab_${page.pageNumber}")
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
                        .testTag("wave1_real_deploy_runner_btn")
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
                        .testTag("wave1_real_send_chat_btn")
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
                        .testTag("wave1_real_sqlite_sync_btn")
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
                        .testTag("wave1_real_os_share_btn")
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
                SovereignProWave1CompilerAndSqlEngine.Wave1ProPage.PAGE_8_SQL_SANDBOX -> Page8InMemorySqlLab(context)
                SovereignProWave1CompilerAndSqlEngine.Wave1ProPage.PAGE_9_REGEX_DEBUGGER -> Page9RegexDebuggerLab()
                SovereignProWave1CompilerAndSqlEngine.Wave1ProPage.PAGE_10_JSON_CODEGEN -> Page10JsonToKotlinCompiler(context)
                SovereignProWave1CompilerAndSqlEngine.Wave1ProPage.PAGE_11_TOKEN_XRAY -> Page11PromptTokenXRayLab()
                SovereignProWave1CompilerAndSqlEngine.Wave1ProPage.PAGE_12_THREAD_PROFILER -> Page12JvmThreadProfilerLab()
                SovereignProWave1CompilerAndSqlEngine.Wave1ProPage.PAGE_13_WCAG_COLOR -> Page13WcagColorContrastLab(context)
                SovereignProWave1CompilerAndSqlEngine.Wave1ProPage.PAGE_14_STATIC_GATE -> Page14RuthlessStaticCodeAuditor()
            }
        }
    }
}

@Composable
private fun Page8InMemorySqlLab(context: Context) {
    var sqlInput by remember {
        mutableStateOf(
            "CREATE TABLE ai_models(id INT, name TEXT, speed_ms INT);\n" +
                "INSERT INTO ai_models VALUES (1, 'gemini-2.5-pro', 110), (2, 'gemini-2.5-flash', 45);\n" +
                "SELECT * FROM ai_models ORDER BY speed_ms ASC;"
        )
    }
    var sqlOutput by remember {
        mutableStateOf(SovereignProWave1CompilerAndSqlEngine.executeInMemorySqlScript(sqlInput))
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(
            value = sqlInput,
            onValueChange = { sqlInput = it },
            textStyle = TextStyle(color = Color(0xFF6EE7B7), fontSize = 10.sp, fontFamily = FontFamily.Monospace),
            maxLines = 4,
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(
                onClick = { sqlOutput = SovereignProWave1CompilerAndSqlEngine.executeInMemorySqlScript(sqlInput) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("▶️ রান SQL (0 Disk / <1ms)", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
            }
            Button(
                onClick = { copyWave1Clip(context, "SQL Output", sqlOutput) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("📋 ফলাফল কপি", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        Surface(
            color = Color(0xFF020617),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(0.8.dp, Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = sqlOutput,
                color = Color(0xFFE2E8F0),
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
private fun Page9RegexDebuggerLab() {
    var pattern by remember { mutableStateOf("(\\w+)=(\\d+)") }
    var sample by remember { mutableStateOf("temp=0, maxTokens=32768, seed=42") }
    var report by remember {
        mutableStateOf(SovereignProWave1CompilerAndSqlEngine.evaluateRegexAndCaptureGroups(pattern, sample))
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(
                value = pattern,
                onValueChange = { pattern = it },
                label = { Text("Regex Pattern", fontSize = 9.sp) },
                singleLine = true,
                textStyle = TextStyle(color = Color(0xFF38BDF8), fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = sample,
                onValueChange = { sample = it },
                label = { Text("Test Text", fontSize = 9.sp) },
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 10.sp),
                modifier = Modifier.weight(1f)
            )
        }
        Button(
            onClick = { report = SovereignProWave1CompilerAndSqlEngine.evaluateRegexAndCaptureGroups(pattern, sample) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("🔍 ক্যাপচার গ্রুপ ও ReDoS টেস্ট", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
        }
        Surface(
            color = Color(0xFF020617),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(0.8.dp, Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = report,
                color = Color(0xFFE2E8F0),
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
private fun Page10JsonToKotlinCompiler(context: Context) {
    var jsonText by remember { mutableStateOf("""{"model":"gemini-2.5-pro","temperature":0.0,"zeroDegree":true,"maxTokens":32768}""") }
    var compiledCode by remember {
        mutableStateOf(SovereignProWave1CompilerAndSqlEngine.compileJsonToKotlinAndTypeScript(jsonText, "AiStudioConfig"))
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(
            value = jsonText,
            onValueChange = { jsonText = it },
            textStyle = TextStyle(color = Color(0xFFFDE68A), fontSize = 10.sp, fontFamily = FontFamily.Monospace),
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(
                onClick = {
                    compiledCode = SovereignProWave1CompilerAndSqlEngine.compileJsonToKotlinAndTypeScript(jsonText, "AiStudioConfig")
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("⚡ Kotlin ও TS কম্পাইল", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
            }
            Button(
                onClick = { copyWave1Clip(context, "Kotlin & TS Code", compiledCode) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("📋 কোড কপি", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        Surface(
            color = Color(0xFF020617),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(0.8.dp, Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = compiledCode,
                color = Color(0xFF6EE7B7),
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
private fun Page11PromptTokenXRayLab() {
    var promptInput by remember { mutableStateOf("আমার প্রজেক্টের জন্য ১০০% প্রো-ডেভেলপার কোড দাও এবং টোকেন আস্তে আস্তে খরচ করো।") }
    var xrayOut by remember {
        mutableStateOf(SovereignProWave1CompilerAndSqlEngine.analyzeAndOptimizePromptTokens(promptInput))
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(
            value = promptInput,
            onValueChange = { promptInput = it },
            textStyle = TextStyle(color = Color.White, fontSize = 10.sp),
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { xrayOut = SovereignProWave1CompilerAndSqlEngine.analyzeAndOptimizePromptTokens(promptInput) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("⚡ টোকেন এক্স-রে ও মিনিফাই", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
        }
        Surface(
            color = Color(0xFF020617),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(0.8.dp, Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = xrayOut, color = Color(0xFFFEF3C7), fontSize = 9.5.sp, modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
private fun Page12JvmThreadProfilerLab() {
    var threadReport by remember {
        mutableStateOf(SovereignProWave1CompilerAndSqlEngine.inspectLiveJvmThreadsAndMainLooper())
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Button(
            onClick = { threadReport = SovereignProWave1CompilerAndSqlEngine.inspectLiveJvmThreadsAndMainLooper() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("🔄 লাইভ থ্রেড ও মেইন লুপার স্ক্যান", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
        }
        Surface(
            color = Color(0xFF020617),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(0.8.dp, Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = threadReport,
                color = Color(0xFFA7F3D0),
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
private fun Page13WcagColorContrastLab(context: Context) {
    var fgHex by remember { mutableStateOf("#38BDF8") }
    var bgHex by remember { mutableStateOf("#0F172A") }
    var contrastReport by remember {
        mutableStateOf(SovereignProWave1CompilerAndSqlEngine.analyzeColorContrastAndComposeCode(fgHex, bgHex))
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(
                value = fgHex,
                onValueChange = { fgHex = it },
                label = { Text("Foreground Hex", fontSize = 9.sp) },
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = bgHex,
                onValueChange = { bgHex = it },
                label = { Text("Background Hex", fontSize = 9.sp) },
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(
                onClick = { contrastReport = SovereignProWave1CompilerAndSqlEngine.analyzeColorContrastAndComposeCode(fgHex, bgHex) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("🎨 WCAG 2.1 চেক", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
            }
            Button(
                onClick = { copyWave1Clip(context, "Compose Color", contrastReport) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("📋 Compose কোড কপি", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        Surface(
            color = Color(0xFF020617),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(0.8.dp, Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = contrastReport,
                color = Color(0xFFE2E8F0),
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
private fun Page14RuthlessStaticCodeAuditor() {
    var sampleCode by remember {
        mutableStateOf("fun executeFast() {\n    val safeResult = computeValue()\n}")
    }
    var auditOut by remember {
        mutableStateOf(SovereignProWave1CompilerAndSqlEngine.runRuthless100DegreeCodeAudit(sampleCode))
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(
            value = sampleCode,
            onValueChange = { sampleCode = it },
            textStyle = TextStyle(color = Color(0xFF6EE7B7), fontSize = 10.sp, fontFamily = FontFamily.Monospace),
            maxLines = 4,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { auditOut = SovereignProWave1CompilerAndSqlEngine.runRuthless100DegreeCodeAudit(sampleCode) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("🛡️ ১০০° শত্রু-অডিট চালান", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
        }
        Surface(
            color = Color(0xFF020617),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(0.8.dp, Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = auditOut,
                color = Color(0xFFE2E8F0),
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

private fun copyWave1Clip(context: Context, label: String, text: String) {
    try {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        cm?.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, "📋 $label কপি হয়েছে!", Toast.LENGTH_SHORT).show()
    } catch (_: Exception) {
    }
}
