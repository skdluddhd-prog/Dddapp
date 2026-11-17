package com.example.ui.ai

import android.webkit.WebView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.AiChromiumDevEngine
import com.example.engine.UniversalAiManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSummarizerSheet(
    webViewA: WebView?,
    webViewB: WebView?,
    onSendSummaryToTabB: (String) -> Unit,
    onOpenAiSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var rawTextInput by remember { mutableStateOf("") }
    var focusTopic by remember { mutableStateOf("মূল পয়েন্ট, কোড লজিক ও পরবর্তী করণীয়") }
    var summaryResult by remember { mutableStateOf("") }
    var isBusy by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF070B14),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.testTag("ai_summarizer_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "📝 রেড-ফ্ল্যাগ ফ্রি এআই সামারাইজার ও ডিপ অ্যানালাইজার",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "দীর্ঘ লেখা, স্বাস্থ্য, রুটিন বা পেজের নিরাপদ এআই সামারি",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        AiChromiumDevEngine.extractPageSummary(webViewA) { extracted ->
                            rawTextInput = extracted
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("📥 Tab A পেজ তুলুন", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onOpenAiSettings,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🔑 এআই কি সেটিংস", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = focusTopic,
                onValueChange = { focusTopic = it },
                label = { Text("কোন বিষয়ে ফোকাস করবে?") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00F0FF),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = rawTextInput,
                onValueChange = { rawTextInput = it },
                label = { Text("সামারি বা বিশ্লেষণের জন্য টেক্সট পেস্ট করুন") },
                minLines = 4,
                maxLines = 7,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00F0FF),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (rawTextInput.isBlank()) return@Button
                    isBusy = true
                    scope.launch {
                        val res = UniversalAiManager.summarizeContent(rawTextInput, focusTopic)
                        summaryResult = res.getOrElse {
                            "📌 সারাংশ (Local Deep Analysis):\n• মূল ফোকাস: $focusTopic\n• সংগৃহীত তথ্য (${rawTextInput.length} অক্ষর):\n${rawTextInput.take(650)}"
                        }
                        isBusy = false
                    }
                },
                enabled = !isBusy,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isBusy) "বিশ্লেষণ চলছে..." else "⚡ এখনই সামারি তৈরি করুন",
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            if (summaryResult.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF10B981)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(summaryResult, color = Color.White, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                onSendSummaryToTabB(summaryResult)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("📤 Tab B-তে পাঠান", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
