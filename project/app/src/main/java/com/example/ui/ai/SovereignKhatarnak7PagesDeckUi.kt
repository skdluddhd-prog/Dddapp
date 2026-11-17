package com.example.ui.ai

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.*

/**
 * ══════════════════════════════════════════════════════════════════════════════
 * 👑 7 SEPARATE DEDICATED PRO-DEVELOPER PAGES UI (ZERO-RAM WORKSTATION DECK)
 * ══════════════════════════════════════════════════════════════════════════════
 *
 * ইউজারের নির্দেশ অনুযায়ী প্রতিটি ফিচার সম্পূর্ণ আলাদা আলাদা পেজে (Separate Dedicated Pages)
 * এবং আল্ট্রা-লো র‍্যাম ও স্টোরেজ ফুটপ্রিন্টে তৈরি:
 * - Page 1: 🌐 API নেটওয়ার্ক স্নিফার ও cURL / Kotlin জেনারেটর
 * - Page 2: 🧬 লাইভ কোড ডিফ (+/-) ও AST সার্জন
 * - Page 3: 🕸️ ডম অটোমেশন ও অটো-ক্লিক JS ম্যাক্রো ল্যাব
 * - Page 4: 🔐 ০-টোকেন JWT ডিকোডার, SHA/HMAC ও সিকিউরিটি স্ক্যানার
 * - Page 5: ⚡ মাল্টি-মডেল কোড বেঞ্চমার্ক ও স্পিড এরিনা
 * - Page 6: 📉 জিরো-র‍্যাম ও স্টোরেজ সার্জিক্যাল কম্প্যাক্টর
 * - Page 7: 🎭 ১০০° শত্রু-অডিট ও প্রো কগনিটিভ পার্সোনা ম্যাট্রিক্স
 */
@Composable
fun SovereignKhatarnak7PagesDeckUi(
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val activePage by SovereignZeroRamKhatarnakProEngine.activePage.collectAsState()
    val apiTraces by SovereignZeroRamKhatarnakProEngine.apiTraces.collectAsState()
    val diffReport by SovereignZeroRamKhatarnakProEngine.latestDiffReport.collectAsState()
    val activePersona by SovereignZeroRamKhatarnakProEngine.activePersona.collectAsState()

    Surface(
        color = Color(0xFF090D16),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.5.dp, Color(0xFF10B981)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag("khatarnak_7_pages_workstation")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // TOP HEADER WITH RAM FOOTPRINT BADGE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🔥 খতরনাক প্রো-ডেভেলপার ৭-পেজ ওয়ার্কস্টেশন (Zero-RAM < 48KB)",
                        color = Color(0xFF34D399),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = activePage.fullTitleBangla + " • RAM বাজেট: মাত্র ${activePage.ramFootprintKb} KB",
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

            // 7 SEPARATE PAGE SELECTOR TABS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SovereignZeroRamKhatarnakProEngine.ProWorkstationPage.values().forEach { page ->
                    val isSelected = activePage == page
                    Surface(
                        color = if (isSelected) Color(0xFF059669) else Color(0xFF1E293B),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF34D399) else Color(0xFF334155)
                        ),
                        modifier = Modifier
                            .clickable {
                                SovereignZeroRamKhatarnakProEngine.selectWorkstationPage(page)
                            }
                            .testTag("pro_page_tab_${page.pageIndex}")
                    ) {
                        Text(
                            text = page.shortTabBangla,
                            color = Color.White,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // DEDICATED VIEW FOR EACH OF THE 7 PAGES
            when (activePage) {
                SovereignZeroRamKhatarnakProEngine.ProWorkstationPage.PAGE_1_NET_SNIFFER -> {
                    Page1ApiSnifferAndCurlGenerator(context = context, traces = apiTraces)
                }
                SovereignZeroRamKhatarnakProEngine.ProWorkstationPage.PAGE_2_AST_DIFF -> {
                    Page2AstDiffAndCodeSurgeon(context = context, diffReport = diffReport)
                }
                SovereignZeroRamKhatarnakProEngine.ProWorkstationPage.PAGE_3_DOM_MACRO -> {
                    Page3DomAutomationMacroBuilder(context = context)
                }
                SovereignZeroRamKhatarnakProEngine.ProWorkstationPage.PAGE_4_CRYPTO_SEC -> {
                    Page4CryptoAndJwtSecurityLab(context = context)
                }
                SovereignZeroRamKhatarnakProEngine.ProWorkstationPage.PAGE_5_BRAIN_ARENA -> {
                    Page5MultiModelBrainBenchmarkArena(context = context)
                }
                SovereignZeroRamKhatarnakProEngine.ProWorkstationPage.PAGE_6_RAM_PURGER -> {
                    Page6ZeroRamAndStorageCompactor(context = context)
                }
                SovereignZeroRamKhatarnakProEngine.ProWorkstationPage.PAGE_7_PERSONA_MATRIX -> {
                    Page7RuthlessPersonaMatrix(context = context, activePersona = activePersona)
                }
            }
        }
    }
}

@Composable
private fun Page1ApiSnifferAndCurlGenerator(
    context: Context,
    traces: List<SovereignZeroRamKhatarnakProEngine.CapturedApiTrace>
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "🌐 সর্বশেষ API কল ট্রেস ও ১-ট্যাপ cURL / Kotlin OkHttp কোড জেনারেটর:",
            color = Color(0xFF38BDF8),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.ExtraBold
        )
        if (traces.isEmpty()) {
            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.8.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "চ্যাটে যেকোনো মেসেজ পাঠালেই তার লাইভ HTTP স্ট্যাটাস, ল্যাটেন্সি, এবং রেডিমেড cURL ও Kotlin কোড এখানে ক্যাপচার হবে (র‍্যাম ব্যবহার: < 8KB)।",
                    color = Color(0xFF94A3B8),
                    fontSize = 9.5.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }
        } else {
            traces.take(3).forEach { tr ->
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.8.dp, Color(0xFF0284C7)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "#${tr.id} • ${tr.providerAndModel} • HTTP ${tr.statusCode} • ${tr.latencyMs}ms (In:${tr.promptChars}c / Out:${tr.responseChars}c)",
                            color = Color(0xFF34D399),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                color = Color(0xFF0284C7),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.clickable {
                                    copyToClip(context, "cURL", tr.curlSnippet)
                                }
                            ) {
                                Text("📋 cURL কপি", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                            Surface(
                                color = Color(0xFF4F46E5),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.clickable {
                                    copyToClip(context, "Kotlin OkHttp", tr.kotlinOkHttpSnippet)
                                }
                            ) {
                                Text("📋 Kotlin কোড কপি", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Page2AstDiffAndCodeSurgeon(
    context: Context,
    diffReport: SovereignZeroRamKhatarnakProEngine.CodeDiffReport
) {
    var codeA by remember { mutableStateOf("fun compute(x: Int): Int {\n    return x * 2\n}") }
    var codeB by remember { mutableStateOf("fun compute(x: Int): Int {\n    val safe = x.coerceAtLeast(0)\n    return safe * 1024\n}") }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "🧬 লাইভ কোড ডিফ (+${diffReport.addedLinesCount} Added / -${diffReport.removedLinesCount} Removed) ও ফাংশন ইনডেক্সার:",
            color = Color(0xFFA78BFA),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(
                value = codeA,
                onValueChange = { codeA = it },
                label = { Text("আগের কোড (Old)", fontSize = 9.sp) },
                textStyle = TextStyle(color = Color(0xFFFCA5A5), fontSize = 9.5.sp, fontFamily = FontFamily.Monospace),
                maxLines = 4,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = codeB,
                onValueChange = { codeB = it },
                label = { Text("নতুন কোড (New)", fontSize = 9.sp) },
                textStyle = TextStyle(color = Color(0xFF6EE7B7), fontSize = 9.5.sp, fontFamily = FontFamily.Monospace),
                maxLines = 4,
                modifier = Modifier.weight(1f)
            )
        }
        Button(
            onClick = {
                SovereignZeroRamKhatarnakProEngine.computeZeroRamCodeDiff(codeA, codeB)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text("🔬 ০-র‍্যাম সার্জিক্যাল ডিফ বের করুন", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
        }
        Surface(
            color = Color(0xFF020617),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(0.8.dp, Color(0xFF4C1D95)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = diffReport.unifiedDiffPreview,
                color = Color(0xFFE2E8F0),
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
private fun Page3DomAutomationMacroBuilder(context: Context) {
    var selectorInput by remember { mutableStateOf("Send") }
    var fillValueInput by remember { mutableStateOf("") }
    var generatedMacro by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "🕸️ ওয়েব অটোমেশন ও ১-ট্যাপ অটো-ক্লিক/অটো-ফিল JS ম্যাক্রো জেনারেটর:",
            color = Color(0xFF38BDF8),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(
                value = selectorInput,
                onValueChange = { selectorInput = it },
                placeholder = { Text("বাটনের নাম বা CSS Selector", fontSize = 9.sp) },
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 10.sp),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = fillValueInput,
                onValueChange = { fillValueInput = it },
                placeholder = { Text("ইনপুট টেক্সট (ঐচ্ছিক)", fontSize = 9.sp) },
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 10.sp),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(
                onClick = {
                    generatedMacro = SovereignZeroRamKhatarnakProEngine.buildDomAutoClickAndFillMacroJs(selectorInput, fillValueInput)
                    copyToClip(context, "DOM Macro JS", generatedMacro)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("⚡ JS ম্যাক্রো তৈরি ও কপি", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        if (generatedMacro.isNotBlank()) {
            Text(
                text = generatedMacro.take(260) + "...",
                color = Color(0xFF6EE7B7),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun Page4CryptoAndJwtSecurityLab(context: Context) {
    var rawText by remember { mutableStateOf("SKRL2-Sovereign-Zero-Degree-2026") }
    var outputReport by remember {
        mutableStateOf(SovereignZeroRamKhatarnakProEngine.inspectJwtOrComputeHashes("SKRL2-Sovereign-Zero-Degree-2026"))
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "🔐 ০-টোকেন JWT ডিকোডার, SHA-256/512, HMAC ও API Key লিক স্ক্যানার:",
            color = Color(0xFFFBBF24),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = rawText,
                onValueChange = { rawText = it },
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    outputReport = SovereignZeroRamKhatarnakProEngine.inspectJwtOrComputeHashes(rawText)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("🔍 স্ক্যান", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        Surface(
            color = Color(0xFF020617),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(0.8.dp, Color(0xFFB45309)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = outputReport,
                color = Color(0xFFFEF3C7),
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
private fun Page5MultiModelBrainBenchmarkArena(context: Context) {
    val profiles by UniversalAiManager.profiles.collectAsState()
    val telemetryMap by UniversalAiManager.keyTelemetryMap.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "⚡ মাল্টি-কী ও মাল্টি-মডেল স্পেকুলেটিভ রেস স্কোরবোর্ড (${profiles.size}টি প্রোফাইল):",
            color = Color(0xFF34D399),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.ExtraBold
        )
        profiles.take(5).forEach { prof ->
            val tel = telemetryMap[prof.id]
            Surface(
                color = Color(0xFF0F172A),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.8.dp, if (prof.isActive) Color(0xFF10B981) else Color(0xFF334155)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        UniversalAiManager.setActiveProfile(context, prof.id)
                        Toast.makeText(context, "⚡ '${prof.label}' সক্রিয় করা হয়েছে!", Toast.LENGTH_SHORT).show()
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${if (prof.isActive) "🟢 " else "⚪ "}${prof.label} (${prof.modelName.ifBlank { "AUTO" }})",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "রোল: ${prof.specialistRole.shortBadge} • ল্যাটেন্সি: ${tel?.lastLatencyMs ?: 0}ms • কল: ${tel?.totalCalls ?: 0}",
                            color = Color(0xFF94A3B8),
                            fontSize = 8.5.sp
                        )
                    }
                    Text(
                        text = "স্কোর: ${UniversalAiManager.scoreModelCapabilityPower(prof.modelName)}",
                        color = Color(0xFF38BDF8),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun Page6ZeroRamAndStorageCompactor(context: Context) {
    var purgeReport by remember {
        mutableStateOf(SovereignWakeupBatch4HardwareAndAuditEngine.inspectAndRelieveJvmMemoryPressure())
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "📉 আল্ট্রা-লো র‍্যাম ও স্টোরেজ সার্জিক্যাল কম্প্যাক্টর (Zero-Lag Memory Guard):",
            color = Color(0xFF34D399),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Surface(
            color = Color(0xFF020617),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(0.8.dp, Color(0xFF059669)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = purgeReport,
                color = Color(0xFFA7F3D0),
                fontSize = 9.5.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Button(
                onClick = {
                    purgeReport = SovereignZeroRamKhatarnakProEngine.executeSurgicalZeroRamAndStoragePurge(context, null)
                    Toast.makeText(context, "🧹 র‍্যাম ও ক্যাশ সার্জিক্যাল ক্লিন সম্পন্ন!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("🧹 ১-ট্যাপে র‍্যাম ও ক্যাশ ফ্রি করুন", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
            }
            Button(
                onClick = {
                    purgeReport = FileStreamHelper.setRamAndStorageQuotaCap(context, 128L, 256L)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("🔒 আল্ট্রা-লো ক্যাপ (128MB RAM)", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun Page7RuthlessPersonaMatrix(
    context: Context,
    activePersona: SovereignZeroRamKhatarnakProEngine.DeveloperPersonaMode
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "🎭 ১০০° শত্রু-অডিট ও প্রো-ডেভেলপার কগনিটিভ মোড সুইচার:",
            color = Color(0xFFF43F5E),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.ExtraBold
        )
        SovereignZeroRamKhatarnakProEngine.DeveloperPersonaMode.values().forEach { mode ->
            val isSel = activePersona == mode
            Surface(
                color = if (isSel) Color(0xFF881337) else Color(0xFF0F172A),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (isSel) Color(0xFFF43F5E) else Color(0xFF334155)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        SovereignZeroRamKhatarnakProEngine.selectDeveloperPersona(mode, context)
                        Toast.makeText(context, "✅ সক্রিয় মোড: ${mode.titleBangla}", Toast.LENGTH_SHORT).show()
                    }
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = mode.titleBangla,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = mode.systemDirective,
                        color = Color(0xFFE2E8F0),
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

private fun copyToClip(context: Context, label: String, text: String) {
    try {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        cm?.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, "📋 $label কপি হয়েছে!", Toast.LENGTH_SHORT).show()
    } catch (_: Exception) {
    }
}
