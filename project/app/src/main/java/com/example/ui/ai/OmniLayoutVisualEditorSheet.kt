package com.example.ui.ai

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.engine.OmniNode
import com.example.engine.UniversalOmniControlEngine
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRose

/**
 * Visual Layout & Touch Editor for Universal Omni Control:
 * Allows the user to physically touch, drag, move, resize, recolor,
 * and toggle 3D tactile styling on ANY button in the app without typing in chat!
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmniLayoutVisualEditorSheet(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val nodes by UniversalOmniControlEngine.nodes.collectAsState()
    val visualState by UniversalOmniControlEngine.visualState.collectAsState()
    var selectedNodeId by remember { mutableStateOf(nodes.firstOrNull()?.id ?: "") }

    val activeSelectedNode = nodes.find { it.id == selectedNodeId } ?: nodes.firstOrNull()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.testTag("omni_layout_visual_editor_sheet")
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
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🎨 সার্বজনীন বাটন ও লেআউট এডিটর",
                            color = Color(0xFF0F172A),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = Color(0xFFE0F2FE),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "হাতে টাচ কন্ট্রোল",
                                color = CyberCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "যেকোনো বাটন সিলেক্ট করে ডানে/বামে সরান, ছোট/বড় করুন, ৩ডি করুন বা জোন পরিবর্তন করুন।",
                        color = Color(0xFF64748B),
                        fontSize = 10.sp
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF334155))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Global Controls (All 3D, Reset All)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val new3D = !visualState.global3DButtons
                        UniversalOmniControlEngine.processNaturalLanguageOrJsonCommand(
                            context,
                            if (new3D) "সব বাটন থ্রিডি করো" else "সব বাটন নরমাল ২ডি করো"
                        )
                        Toast.makeText(context, if (new3D) "🧊 সব বাটন ৩ডি করা হয়েছে" else "সব বাটন ২ডি করা হয়েছে", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (visualState.global3DButtons) CyberPurple else Color(0xFFEDE9FE)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Text(
                        text = if (visualState.global3DButtons) "🧊 ৩ডি অ্যাক্টিভ (সব)" else "🧊 সব বাটন ৩ডি করুন",
                        color = if (visualState.global3DButtons) Color.White else CyberPurple,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        UniversalOmniControlEngine.resetAllToDefault(context)
                        Toast.makeText(context, "🔄 সব বাটন ও থিম ডিফল্ট অবস্থায় ফিরিয়ে আনা হয়েছে", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEF3C7)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(36.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color(0xFF92400E), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ডিফল্ট রিসেট",
                        color = Color(0xFF92400E),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Button Selector Horizontal Carousel
            Text(
                text = "১. কোন বাটনটি এডিট করবেন সিলেক্ট করুন:",
                color = Color(0xFF0F172A),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                nodes.forEach { node ->
                    val isSel = node.id == activeSelectedNode?.id
                    Surface(
                        color = if (isSel) CyberCyan else Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (isSel) CyberCyan else Color(0xFFCBD5E1)),
                        modifier = Modifier.clickable { selectedNodeId = node.id }
                    ) {
                        Text(
                            text = node.label,
                            color = if (isSel) Color.White else Color(0xFF1E293B),
                            fontSize = 10.sp,
                            fontWeight = if (isSel) FontWeight.ExtraBold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Active Selected Button Editor Card
            activeSelectedNode?.let { node ->
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.2.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "নির্বাচিত: ${node.label}",
                                    color = Color(0xFF0F172A),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "বর্তমান অবস্থান: ${node.zone} • স্কেল: ${String.format("%.2f", node.scaleFactor)}x",
                                    color = CyberCyan,
                                    fontSize = 10.sp
                                )
                            }

                            // 3D Toggle Switch for this button
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("৩ডি স্টাইল", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                Spacer(modifier = Modifier.width(6.dp))
                                Switch(
                                    checked = node.is3DStyle,
                                    onCheckedChange = { checked ->
                                        UniversalOmniControlEngine.processNaturalLanguageOrJsonCommand(
                                            context,
                                            if (checked) "${node.label} থ্রিডি করো" else "${node.label} নরমাল ২ডি করো"
                                        )
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = CyberPurple
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Move Left / Right Buttons
                        Text("বাটনের ক্রম ও অবস্থান (আগে/পিছে সরাতে ট্যাপ করুন):", fontSize = 10.sp, color = Color(0xFF475569), fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    UniversalOmniControlEngine.processNaturalLanguageOrJsonCommand(context, "${node.label} বামে প্রথমে আনো")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(34.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Move Left", tint = Color.White)
                                Text("⬅️ বামে সরান", color = Color.White, fontSize = 10.sp)
                            }

                            Button(
                                onClick = {
                                    UniversalOmniControlEngine.processNaturalLanguageOrJsonCommand(context, "${node.label} ডানে শেষে পাঠাও")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(34.dp)
                            ) {
                                Text("ডানে সরান ➡️", color = Color.White, fontSize = 10.sp)
                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Move Right", tint = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Move Zone (Top Row 1, Top Row 2, Bottom Dock, Floating)
                        Text("স্ক্রিনের কোন জায়গায় বসাবেন:", fontSize = 10.sp, color = Color(0xFF475569), fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            val zones = listOf(
                                "TOP_ROW_1" to "১ম সারি (উপরে)",
                                "TOP_ROW_2" to "২য় সারি",
                                "BOTTOM_DOCK" to "নিচের ডক",
                                "FLOATING" to "ভাসমান"
                            )
                            zones.forEach { (zCode, zLabel) ->
                                val isCur = node.zone == zCode
                                Surface(
                                    color = if (isCur) CyberEmerald else Color.White,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, if (isCur) CyberEmerald else Color(0xFFCBD5E1)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            UniversalOmniControlEngine.processNaturalLanguageOrJsonCommand(
                                                context,
                                                "${node.label} ${if (zCode == "FLOATING") "ভাসমান ফ্লোটিং করো" else if (zCode == "BOTTOM_DOCK") "নিচে ডকে নাও" else if (zCode == "TOP_ROW_1") "উপরের প্রথম সারিতে নাও" else "দ্বিতীয় সারিতে নাও"}"
                                            )
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                                        Text(
                                            text = zLabel,
                                            color = if (isCur) Color.White else Color(0xFF1E293B),
                                            fontSize = 9.sp,
                                            fontWeight = if (isCur) FontWeight.ExtraBold else FontWeight.Normal,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Size Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("বাটনের সাইজ (বড় বা ছোট করুন):", fontSize = 10.sp, color = Color(0xFF475569), fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "${(node.scaleFactor * 100).toInt()}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CyberCyan
                            )
                        }
                        Slider(
                            value = node.scaleFactor,
                            onValueChange = { newVal ->
                                val updated = nodes.map { if (it.id == node.id) it.copy(scaleFactor = newVal) else it }
                                UniversalOmniControlEngine.setNodesDirect(context, updated)
                            },
                            valueRange = 0.65f..1.70f,
                            colors = SliderDefaults.colors(
                                thumbColor = CyberCyan,
                                activeTrackColor = CyberCyan
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Color Presets
                        Text("বাটনের কালার প্যালেট:", fontSize = 10.sp, color = Color(0xFF475569), fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val colors = listOf(
                                "#0284C7" to "নীল",
                                "#10B981" to "সবুজ",
                                "#E11D48" to "লাল",
                                "#7C3AED" to "বেগুনি",
                                "#D97706" to "সোনালী",
                                "#0F172A" to "কালো"
                            )
                            colors.forEach { (cHex, cName) ->
                                Surface(
                                    color = UniversalOmniControlEngine.parseColorSafe(cHex),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(28.dp)
                                        .clickable {
                                            UniversalOmniControlEngine.processNaturalLanguageOrJsonCommand(
                                                context,
                                                "${node.label} কালার $cName করো"
                                            )
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(cName, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Remove Button
                        Button(
                            onClick = {
                                UniversalOmniControlEngine.processNaturalLanguageOrJsonCommand(context, "${node.label} রিমুভ করো")
                                Toast.makeText(context, "🗑️ ${node.label} রিমুভ করা হয়েছে", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFE4E6)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().height(32.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove", tint = CyberRose, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("স্ক্রিন থেকে এই বাটনটি রিমুভ (হাইড) করুন", color = CyberRose, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Global Canvas Background Controls
            Text(
                text = "২. পুরো অ্যাপের ব্যাকগ্রাউন্ড ও শেডার কাস্টমাইজেশন:",
                color = Color(0xFF0F172A),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val bgStyles = listOf(
                    "সাদা ব্যাকগ্রাউন্ড করো" to "⚪ পিওর হোয়াইট",
                    "কালো ব্যাকগ্রাউন্ড করো" to "⚫ পিওর ব্ল্যাক",
                    "মাঝখানে লাল ডট ব্যাকগ্রাউন্ড করো" to "🔴 রাডিয়াল ডট",
                    "উপরে কালো নিচে নীল গ্রেডিয়েন্ট ব্যাকগ্রাউন্ড করো" to "🌌 গ্রেডিয়েন্ট",
                    "ডার্ক রেইনবো ব্যাকগ্রাউন্ড করো" to "🌈 ডার্ক রেইনবো"
                )
                bgStyles.forEach { (cmd, label) ->
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(0.8.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier.clickable {
                            UniversalOmniControlEngine.processNaturalLanguageOrJsonCommand(context, cmd)
                            Toast.makeText(context, "🎨 ব্যাকগ্রাউন্ড পরিবর্তিত হয়েছে!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text(
                            text = label,
                            color = Color(0xFF1E293B),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}
