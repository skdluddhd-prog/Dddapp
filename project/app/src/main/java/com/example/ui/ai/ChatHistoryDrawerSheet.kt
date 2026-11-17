package com.example.ui.ai

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.engine.ChatSessionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHistoryDrawerSheet(
    onNewBlankCanvas: () -> Unit,
    onDismiss: () -> Unit,
    onSessionSelected: (String) -> Unit = {},
    onStatusToast: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val summaries by ChatSessionManager.sessionSummaries.collectAsState()
    val activeSession by ChatSessionManager.activeSession.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF070B14),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.testTag("chat_history_drawer_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "🗂️ এআই চ্যাট হিস্ট্রি ও নিউ চ্যাট (০% ফোন মেমরি লোড)",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "গুগল ড্রাইভ ও জিমেইল ফ্রি পার্মানেন্ট সিঙ্ক • চারকোনা ফাঁকা সাদা ক্যানভাস",
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
                        ChatSessionManager.startNewChat(context)
                        onNewBlankCanvas()
                        onStatusToast("নতুন চ্যাট ও চারকোনা সাদা ক্যানভাস শুরু হয়েছে!")
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("➕ নিউ চ্যাট (Blank Canvas)", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }

                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val file = ChatSessionManager.exportForGoogleDrive(context)
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/json"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "গুগল ড্রাইভ বা জিমেইলে ব্যাকআপ করুন"))
                            } catch (e: Exception) {
                                onStatusToast("ব্যাকআপ ফাইল প্রস্তুত: ${e.message}")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("☁️ ড্রাইভ/জিমেইল সিঙ্ক", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(summaries, key = { it.id }) { item ->
                    val isSelected = activeSession?.id == item.id
                    Surface(
                        color = if (isSelected) Color(0xFF064E3B) else Color(0xFF0F172A),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFF10B981) else Color(0xFF334155)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                ChatSessionManager.selectSession(context, item.id)
                                onSessionSelected(item.id)
                                onStatusToast("সেশন নির্বাচন করা হয়েছে")
                                onDismiss()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    "${item.messageCount} messages • ${ChatSessionManager.formatTime(item.updatedAt)}",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            }
                            OutlinedButton(
                                onClick = { ChatSessionManager.deleteSession(context, item.id) }
                            ) {
                                Text("মুছুন", color = Color(0xFFF43F5E), fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
