package com.example.ui.ai

import android.webkit.WebView
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.AutonomousSelfEvolutionEngine
import com.example.engine.BuildJobStatus
import com.example.engine.ChatSessionManager
import com.example.engine.HumanReviewSnapshot
import com.example.engine.MasterOmniControllerEngine
import com.example.engine.MicroPartHealthStatus
import com.example.engine.MultiMinuteAutonomousBuilderEngine
import com.example.engine.MultiMinuteBuildJob
import com.example.engine.SelfFeatureModule
import com.example.engine.SovereignLiveReviewAndXRayEngine
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRose
import com.example.ui.theme.CyberSlate

@Composable
fun SovereignHumanReviewPanel(
    snapshot: HumanReviewSnapshot,
    activeModule: SelfFeatureModule?,
    activeBuildJob: MultiMinuteBuildJob?,
    activeWebView: WebView?,
    onCloseReview: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Surface(
        color = Color(0xFF060C18),
        border = BorderStroke(1.2.dp, CyberAmber.copy(alpha = 0.85f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .heightIn(max = 370.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .verticalScroll(scrollState)
        ) {
            // 1. TOP HEADER OF REVIEW PAGE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "👁️ মানুষের ভাষায় লাইভ রিভিউ পেজ (কী কাজ করছে ও কী বাকি)",
                        color = CyberAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "টার্গেট: ${snapshot.targetTitle} • ✅ ${snapshot.workingScorePercent}% সচল • 💾 ${snapshot.diskSizeKb} KB",
                        color = CyberEmerald,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    // 1-Tap Demo Button: User's Exact Example ("মাইকের অপশন আছে কিন্তু মাইক অন করার অপশন নেই")
                    Surface(
                        color = CyberPurple.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.8.dp, CyberPurple),
                        modifier = Modifier
                            .clickable {
                                val demoIncompleteMicHtml = """
                                    <div style="background:#040812;color:#E2E8F0;padding:12px;border-radius:12px;border:1px solid #00F0FF55;font-family:sans-serif;">
                                      <div style="color:#00F0FF;font-weight:900;font-size:14px;">🎙️ স্মার্ট ভয়েস ও কমান্ড হাব (উদাহরণ টেস্ট)</div>
                                      <div style="color:#94A3B8;font-size:11px;margin-top:4px;">এই ফিচারটিতে মাইকের অপশন/আইকন দেখা যাচ্ছে, কিন্তু মাইক অন করার বাটন এখনো দেওয়া হয়নি:</div>
                                      <div style="background:#090E18;padding:10px;border-radius:8px;border:1px solid #1E293B;margin-top:8px;display:flex;justify-content:space-between;align-items:center;">
                                        <span>🎙️ মাইক অপশন স্ট্যাটাস: (অন করার বাটন মিসিং)</span>
                                        <span style="color:#F59E0B;font-size:11px;font-weight:bold;">⚠️ শুধু ডিজাইন</span>
                                      </div>
                                      <div style="margin-top:8px;display:flex;gap:6px;">
                                        <button onclick="alert('নোট কাজ করছে!')" style="background:#00F0FF;color:#000;border:none;border-radius:6px;padding:6px 10px;font-weight:bold;font-size:11px;">📝 নোট টেস্ট (সচল)</button>
                                        <button style="background:#334155;color:#fff;border:none;border-radius:6px;padding:6px 10px;font-weight:bold;font-size:11px;">⚙️ ডেড বাটন (লজিক ছাড়া)</button>
                                      </div>
                                    </div>
                                """.trimIndent()
                                val demoMod = SelfFeatureModule(
                                    id = "sf_demo_mic_review_test",
                                    title = "🎙️ উদাহরণ ফিচার (মাইক আছে কিন্তু অন বাটন নেই)",
                                    description = "রিভিউ পেজ কিভাবে সূক্ষ্ম ঘাটতি মানুষের ভাষায় ধরে এবং ১ ট্যাপে ঠিক করে তার লাইভ উদাহরণ",
                                    categoryBadge = "REVIEW TEST 👁️",
                                    jsPayload = demoIncompleteMicHtml,
                                    isAutoRunOnPageLoad = true
                                )
                                MasterOmniControllerEngine.addOrUpdateSelfFeature(context, demoMod)
                                AutonomousSelfEvolutionEngine.launchModuleInLiveRunner(demoMod, fullScreen = false)
                                SovereignLiveReviewAndXRayEngine.inspectAndBuildHumanReview(context, demoMod, activeBuildJob)
                                Toast.makeText(context, "👁️ রিভিউ পেজে দেখুন: মাইক অন করার অপশন যে নেই তা পরিষ্কার ধরা পড়েছে!", Toast.LENGTH_LONG).show()
                            }
                            .testTag("review_page_demo_mic_test_btn")
                    ) {
                        Text(
                            text = "🧪 মাইক উদাহরণ টেস্ট",
                            color = Color(0xFFD8B4FE),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        color = Color(0xFF230A14),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.8.dp, CyberRose),
                        modifier = Modifier.clickable { onCloseReview() }
                    ) {
                        Text(
                            text = "✕ বন্ধ",
                            color = CyberRose,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(7.dp))

            // 2. PLAIN HUMAN SUMMARY & WHAT CHANGED IN THE LAST EDIT
            Surface(
                color = Color(0xFF0A1324),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.8.dp, CyberCyan.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "🗣️ মানুষের ভাষায় বর্তমান অবস্থা:",
                        color = CyberCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = snapshot.plainHumanSummaryBangla,
                        color = Color.White,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = snapshot.whatChangedLastBangla,
                        color = CyberEmerald,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(7.dp))

            // 3. PAST ERRORS & INCOMPLETE BUILDS BROUGHT TO FRONT (If any exist)
            val hasPausedBuild = activeBuildJob != null &&
                (activeBuildJob.status == BuildJobStatus.PAUSED_NETWORK_DROP || activeBuildJob.status == BuildJobStatus.PAUSED_BY_USER)
            if (snapshot.pastErrorsAndFixes.isNotEmpty() || hasPausedBuild) {
                Surface(
                    color = Color(0xFF221206),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.9.dp, CyberAmber),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🛠️ আগের এরর ও অসমাপ্ত বিল্ড (সব সামনে আনা হয়েছে):",
                                color = CyberAmber,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            if (hasPausedBuild) {
                                Surface(
                                    color = CyberEmerald,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.clickable {
                                        MultiMinuteAutonomousBuilderEngine.forceHealAndFinishPausedJob(
                                            context = context,
                                            activeWebView = activeWebView,
                                            onStepMessageToChat = { msg ->
                                                ChatSessionManager.addMessage(context, "AI", msg, false)
                                            }
                                        )
                                    }
                                ) {
                                    Text(
                                        text = "⚡ এখনই বিল্ড সম্পূর্ণ করুন",
                                        color = Color.Black,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        snapshot.pastErrorsAndFixes.take(4).forEach { errItem ->
                            Text(
                                text = "• $errItem",
                                color = Color(0xFFFDE68A),
                                fontSize = 9.sp,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(7.dp))
            }

            // 4. MICRO-FEATURE X-RAY CARDS (Shows every small option: Working vs Missing Option like Mic without Mic-ON)
            Text(
                text = "🔬 ছোট থেকে ছোট অংশের মানব-ভাষায় রিপোর্ট (কোনটা আছে আর কোনটা মিসিং):",
                color = CyberEmerald,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(5.dp))

            snapshot.microParts.forEach { part ->
                val borderCol = when (part.status) {
                    MicroPartHealthStatus.WORKING_PERFECT -> CyberEmerald.copy(alpha = 0.65f)
                    MicroPartHealthStatus.PARTIAL_MISSING -> CyberAmber
                    MicroPartHealthStatus.NOT_ADDED_YET -> CyberCyan.copy(alpha = 0.45f)
                }
                val bgCol = when (part.status) {
                    MicroPartHealthStatus.WORKING_PERFECT -> Color(0xFF061914)
                    MicroPartHealthStatus.PARTIAL_MISSING -> Color(0xFF241605)
                    MicroPartHealthStatus.NOT_ADDED_YET -> Color(0xFF091220)
                }
                val statusTag = when (part.status) {
                    MicroPartHealthStatus.WORKING_PERFECT -> "✅ ১০০% সচল"
                    MicroPartHealthStatus.PARTIAL_MISSING -> "⚠️ অপশন অসম্পূর্ণ / মিসিং"
                    MicroPartHealthStatus.NOT_ADDED_YET -> "➕ যুক্ত করা যাবে"
                }

                Surface(
                    color = bgCol,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.8.dp, borderCol),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${part.categoryIcon} ${part.partNameBangla}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = statusTag,
                                color = when (part.status) {
                                    MicroPartHealthStatus.WORKING_PERFECT -> CyberEmerald
                                    MicroPartHealthStatus.PARTIAL_MISSING -> CyberAmber
                                    MicroPartHealthStatus.NOT_ADDED_YET -> CyberCyan
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = part.humanExplanationBangla,
                            color = Color(0xFFE2E8F0),
                            fontSize = 9.sp,
                            lineHeight = 13.sp
                        )

                        if (part.whatIsMissingBangla.isNotBlank() && part.oneTapFixCommand.isNotBlank()) {
                            Spacer(modifier = Modifier.height(5.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "👉 ${part.whatIsMissingBangla}",
                                    color = CyberAmber,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = CyberEmerald,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.clickable {
                                        val targetMod = activeModule
                                            ?: MasterOmniControllerEngine.savedSelfFeatures.value.firstOrNull()
                                        if (targetMod != null) {
                                            val (_, msg) = SovereignLiveReviewAndXRayEngine.applySurgicalMicroFix(
                                                context = context,
                                                module = targetMod,
                                                fixCommand = part.oneTapFixCommand,
                                                activeWebView = activeWebView
                                            )
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "প্রথমে একটি ফিচার বা '🧪 মাইক উদাহরণ টেস্ট' চালু করুন!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Text(
                                        text = "⚡ এখনই সচল করুন",
                                        color = Color.Black,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. BUTTON-BY-BUTTON HUMAN AUDIT STRIP
            if (snapshot.buttonAudits.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "🔘 ফিচারের ভেতরে থাকা প্রতিটি বাটনের আলাদা স্ট্যাটাস (${snapshot.buttonAudits.size}টি):",
                    color = CyberCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    snapshot.buttonAudits.forEach { btn ->
                        Surface(
                            color = if (btn.hasWorkingHandler) Color(0xFF071B16) else Color(0xFF281207),
                            shape = RoundedCornerShape(7.dp),
                            border = BorderStroke(
                                0.8.dp,
                                if (btn.hasWorkingHandler) CyberEmerald else CyberAmber
                            )
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
                                Text(
                                    text = "${if (btn.hasWorkingHandler) "✅" else "⚠️"} ${btn.buttonLabel}",
                                    color = if (btn.hasWorkingHandler) CyberEmerald else CyberAmber,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = btn.humanRoleBangla,
                                    color = Color.LightGray,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
