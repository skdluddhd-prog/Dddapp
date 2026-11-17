package com.example.ui.workflow

import android.annotation.SuppressLint
import android.content.Intent
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.engine.WorkerEngine
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPurple

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveSandboxSheet(
    initialOutputOrCode: String,
    onSendToTabB: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var editableSource by remember(initialOutputOrCode) {
        mutableStateOf(
            initialOutputOrCode.ifBlank {
                """
                <!DOCTYPE html>
                <html>
                <head>
                  <style>
                    body { background:#0B1324; color:#00F0FF; font-family:sans-serif; padding:20px; }
                    .card { background:#131F38; border:1px solid #10B981; border-radius:12px; padding:16px; }
                  </style>
                </head>
                <body>
                  <div class="card">
                    <h2>⚡ SKRL 2 Live Nanobot Sandbox</h2>
                    <p>ন্যানোবট চেইনের তৈরি করা HTML/JS/Code বা রিপোর্ট এখানে সাথে সাথে রান ও টেস্ট করতে পারবেন।</p>
                  </div>
                </body>
                </html>
                """.trimIndent()
            }
        )
    }

    var viewMode by remember { mutableIntStateOf(0) } // 0 = Live Preview, 1 = Code Editor
    var sandboxWebView by remember { mutableStateOf<WebView?>(null) }

    val renderedHtml = remember(editableSource) {
        buildRenderableHtml(editableSource)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = CyberDarkBg,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.testTag("live_sandbox_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🧪 লাইভ আউটপুট ও কোড স্যান্ডবক্স (Live Preview & Export)",
                        color = CyberEmerald,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "বটদের তৈরি কোড/ওয়েব অ্যাপ সাথে সাথে রান করে দেখুন অথবা এক্সপোর্ট করুন",
                        color = CyberCyan,
                        fontSize = 11.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewMode = 0 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewMode == 0) CyberEmerald else CyberCardBg
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Preview",
                        tint = if (viewMode == 0) Color.Black else Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "লাইভ প্রিভিউ রান",
                        color = if (viewMode == 0) Color.Black else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { viewMode = 1 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewMode == 1) CyberPurple else CyberCardBg
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Code, contentDescription = "Code", tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "সোর্স কোড এডিটর",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberCardBg)
            ) {
                if (viewMode == 0) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                webViewClient = WebViewClient()
                                webChromeClient = WebChromeClient()
                                loadDataWithBaseURL(
                                    "https://localhost/",
                                    renderedHtml,
                                    "text/html",
                                    "UTF-8",
                                    null
                                )
                                sandboxWebView = this
                            }
                        },
                        update = { wv ->
                            wv.loadDataWithBaseURL(
                                "https://localhost/",
                                renderedHtml,
                                "text/html",
                                "UTF-8",
                                null
                            )
                        }
                    )
                } else {
                    OutlinedTextField(
                        value = editableSource,
                        onValueChange = { editableSource = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = Color.DarkGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = CyberDarkBg,
                            unfocusedContainerColor = CyberDarkBg
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color.White
                        ),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "SKRL 2 Nanobot Swarm Output")
                            putExtra(Intent.EXTRA_TEXT, editableSource)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "কোড বা আউটপুট শেয়ার/সেভ করুন"))
                    },
                    border = BorderStroke(1.dp, CyberCyan),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Export", tint = CyberCyan)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("এক্সপোর্ট / শেয়ার", color = CyberCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        onSendToTabB(editableSource)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Tab B কম্পাইলারে পাঠান", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

private fun buildRenderableHtml(raw: String): String {
    val extracted = WorkerEngine.extractCodeBlocks(raw)
    val candidate = if (extracted.contains("<html", ignoreCase = true) ||
        extracted.contains("<div", ignoreCase = true) ||
        extracted.contains("<script", ignoreCase = true)
    ) {
        extracted
    } else if (raw.contains("<html", ignoreCase = true) || raw.contains("<!DOCTYPE html", ignoreCase = true)) {
        raw
    } else {
        val escaped = raw
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\n", "<br/>")
        """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
          <style>
            body { background:#070B14; color:#E2E8F0; font-family:system-ui, sans-serif; padding:16px; line-height:1.6; }
            .box { background:#0F172A; border:1px solid #00F0FF55; border-radius:12px; padding:16px; white-space:pre-wrap; word-break:break-word; }
            h3 { color:#10B981; margin-top:0; }
          </style>
        </head>
        <body>
          <div class="box">
            <h3>🤖 ন্যানোবট সোয়ার্ম আউটপুট ও লাইভ প্রিভিউ</h3>
            <div>$escaped</div>
          </div>
        </body>
        </html>
        """.trimIndent()
    }
    return candidate
}
