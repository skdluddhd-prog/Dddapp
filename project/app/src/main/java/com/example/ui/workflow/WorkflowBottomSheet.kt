package com.example.ui.workflow

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NanobotRole
import com.example.model.RuleType
import com.example.model.WorkflowNode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkflowBottomSheet(
    node: WorkflowNode,
    allNodes: List<WorkflowNode>,
    onDismiss: () -> Unit,
    onDeleteNode: (String) -> Unit,
    onRunThisBotNow: (WorkflowNode) -> Unit,
    onSaveConfig: (
        title: String,
        nanobotRole: NanobotRole,
        customRoleName: String,
        customRolePrompt: String,
        initialMessageToSend: String,
        nextTargetNodeId: String,
        rule: RuleType,
        customParam: String,
        brainPower: Int,
        isCloud: Boolean,
        webhookUrl: String,
        targetInput: String,
        is24x7Loop: Boolean,
        loopIntervalMinutes: Int,
        autoFlushMemory: Boolean
    ) -> Unit
) {
    var botTitle by remember(node.id) { mutableStateOf(node.title) }
    var selectedRole by remember(node.id) { mutableStateOf(node.nanobotRole) }
    var customRoleName by remember(node.id) {
        mutableStateOf(node.customRoleName.ifBlank { node.nanobotRole.banglaTitle })
    }
    var customRolePrompt by remember(node.id) {
        mutableStateOf(node.customRolePrompt.ifBlank { node.nanobotRole.defaultSystemPrompt })
    }
    var initialMessageToSend by remember(node.id) { mutableStateOf(node.initialMessageToSend) }
    var nextTargetNodeId by remember(node.id) { mutableStateOf(node.nextTargetNodeId) }
    var selectedRule by remember(node.id) { mutableStateOf(node.rule) }
    var customParam by remember(node.id) { mutableStateOf(node.customParameter) }
    var brainPower by remember(node.id) { mutableFloatStateOf(node.brainPower.toFloat()) }
    var isCloudExecution by remember(node.id) { mutableStateOf(node.isCloudExecution) }
    var cloudWebhookUrl by remember(node.id) { mutableStateOf(node.cloudWebhookUrl) }
    var targetInput by remember(node.id) { mutableStateOf(node.targetInput) }
    var is24x7LoopState by remember(node.id) { mutableStateOf(node.is24x7Loop) }
    var loopIntervalText by remember(node.id) { mutableStateOf(node.loopIntervalMinutes.toString()) }
    var autoFlushMemory by remember(node.id) { mutableStateOf(node.autoFlushMemory) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF070B14),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.testTag("workflow_node_config_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (node.isHomeNode) "🏰 Home Base (Master Screen)"
                        else "🤖 ন্যানোবট রোল ও ১০০% কন্ট্রোল স্টুডিও",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "নিজস্ব রোল, ওয়েব মেসেঞ্জার টার্গেট, ডিপ-থিংক প্রম্পট এবং পরবর্তী বট কানেকশন",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
                if (!node.isHomeNode) {
                    OutlinedButton(
                        onClick = {
                            onDeleteNode(node.id)
                            onDismiss()
                        },
                        border = BorderStroke(1.dp, Color(0xFFF43F5E)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("মুছুন", color = Color(0xFFF43F5E), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (node.isHomeNode) {
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFF10B981))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🏰 Home Base হলো আপনার অ্যাপের মূল কমান্ড সেন্টার।",
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "সব ন্যানোবট চেইন শেষ করে এখানে চূড়ান্ত রিপ্লাই জমা দেয় অথবা কন্টিনিউয়াস রিলে লুপে আবার প্রথম বটের কাছে পাঠায়।",
                            color = Color(0xFFE2E8F0),
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                return@ModalBottomSheet
            }

            // 1. Nanobot Name & Preset Role Selector
            Text(
                text = "১. ন্যানোবটের নাম ও রোল (Role Preset বা নিজস্ব রোল)",
                color = Color(0xFF00F0FF),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = botTitle,
                onValueChange = { botTitle = it },
                label = { Text("ন্যানোবটের নাম (যেমন: Bot 1 - Messenger)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("bot_title_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00F0FF),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NanobotRole.entries.forEach { role ->
                    val isSelected = selectedRole == role
                    Surface(
                        color = if (isSelected) Color(0xFF0284C7) else Color(0xFF0F172A),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF00F0FF) else Color(0xFF334155)
                        ),
                        modifier = Modifier
                            .clickable {
                                selectedRole = role
                                customRoleName = role.banglaTitle
                                customRolePrompt = role.defaultSystemPrompt
                                selectedRule = when (role) {
                                    NanobotRole.WEB_MESSENGER -> RuleType.WEB_AUTO_MESSAGE
                                    NanobotRole.DEEP_THINKER, NanobotRole.CRITIC_VERIFIER, NanobotRole.CUSTOM_AGENT ->
                                        RuleType.DEEP_THINK_ANALYZE
                                    NanobotRole.ACTION_COMPILER -> RuleType.EXTRACT_CODE
                                }
                            }
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text(
                                text = role.badge,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = role.banglaTitle,
                                color = Color(0xFFCBD5E1),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = customRoleName,
                onValueChange = { customRoleName = it },
                label = { Text("নিজস্ব রোলের নাম (ইচ্ছেমতো যেকোনো রোল লিখুন)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("custom_role_name_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFA855F7),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = customRolePrompt,
                onValueChange = { customRolePrompt = it },
                label = { Text("রোল নির্দেশনা / ডিটেইলড প্রম্পট (এই বট ঠিক কী কাজ ও কীভাবে ডিপ-থিংক করবে)") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth().testTag("custom_role_prompt_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFA855F7),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Website / AI Platform Target & Autonomous Message
            Text(
                text = "২. ওয়েবসাইট / প্ল্যাটফর্ম টার্গেট ও স্বয়ংক্রিয় মেসেজ (আপনি না ঢুকলেও বট নিজে যাবে)",
                color = Color(0xFF10B981),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = targetInput,
                onValueChange = { targetInput = it },
                label = { Text("Target Website URL / AI Platform (যেমন: https://chatgpt.com বা যেকোনো সাইট)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("target_url_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF10B981),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Quick Website Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "ChatGPT" to "https://chatgpt.com",
                    "DeepSeek" to "https://chat.deepseek.com",
                    "Claude" to "https://claude.ai",
                    "Gemini Web" to "https://gemini.google.com",
                    "DuckDuckGo AI" to "https://duckduckgo.com/?q=DuckDuckGo+AI+Chat&ia=chat",
                    "AI Deep-Think Brain" to "AI Deep-Think Brain"
                ).forEach { (label, url) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1E293B))
                            .clickable { targetInput = url }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(label, color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = initialMessageToSend,
                onValueChange = { initialMessageToSend = it },
                label = { Text("ওয়েবসাইটে/AI-কে কী মেসেজ পাঠাবে? (খালি থাকলে আগের বটের দেওয়া মেসেজ পাঠাবে)") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth().testTag("initial_message_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF10B981),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Step-by-Step Handoff: Who gets the task after this bot?
            Text(
                text = "৩. এই বটের কাজ শেষ হলে টাস্ক কার কাছে যাবে? (Step-by-Step Handoff)",
                color = Color(0xFFF59E0B),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allNodes.filter { it.id != node.id }.forEach { candidate ->
                    val isChosen = nextTargetNodeId == candidate.id
                    Surface(
                        color = if (isChosen) Color(0xFF065F46) else Color(0xFF0F172A),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isChosen) Color(0xFF10B981) else Color(0xFF334155)
                        ),
                        modifier = Modifier.clickable { nextTargetNodeId = candidate.id }
                    ) {
                        Text(
                            text = if (candidate.isHomeNode) "🏰 Home Base"
                            else "➡️ ${candidate.title} (${candidate.customRoleName.ifBlank { candidate.nanobotRole.badge }})",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Execution Engine Rule & Brain Power
            Text(
                text = "৪. ইঞ্জিন মোড ও ব্রেইন পাওয়ার (${brainPower.toInt()}%)",
                color = Color(0xFF38BDF8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                RuleType.entries.forEach { rule ->
                    val active = selectedRule == rule
                    Surface(
                        color = if (active) Color(0xFF1E3A8A) else Color(0xFF0F172A),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (active) Color(0xFF38BDF8) else Color(0xFF334155)),
                        modifier = Modifier.clickable { selectedRule = rule }
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Text(rule.title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(rule.description.take(28), color = Color(0xFF94A3B8), fontSize = 9.sp)
                        }
                    }
                }
            }

            Slider(
                value = brainPower,
                onValueChange = { brainPower = it },
                valueRange = 10f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFF00F0FF),
                    activeTrackColor = Color(0xFF00F0FF)
                )
            )

            OutlinedTextField(
                value = customParam,
                onValueChange = { customParam = it },
                label = { Text("অতিরিক্ত প্যারামিটার / ফিল্টার রুল (ঐচ্ছিক)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF38BDF8),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Cloud & 24/7 Loop switches
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("☁️ ক্লাউড ওয়েবহুক এক্সিকিউশন", color = Color.White, fontSize = 12.sp)
                Switch(checked = isCloudExecution, onCheckedChange = { isCloudExecution = it })
            }
            if (isCloudExecution) {
                OutlinedTextField(
                    value = cloudWebhookUrl,
                    onValueChange = { cloudWebhookUrl = it },
                    label = { Text("Cloud Webhook URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔄 ২৪/৭ অটো-লুপ সিডিউলার", color = Color.White, fontSize = 12.sp)
                Switch(checked = is24x7LoopState, onCheckedChange = { is24x7LoopState = it })
            }
            if (is24x7LoopState) {
                OutlinedTextField(
                    value = loopIntervalText,
                    onValueChange = { loopIntervalText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("লুপ বিরতি (মিনিট)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Show Last Reply Received by this Bot if available
            if (node.lastReplyReceived.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFF0B1324),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFF10B981))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "📥 এই ন্যানোবটের সর্বশেষ প্রাপ্ত রিপ্লাই / ডিপ-থিংক আউটপুট:",
                            color = Color(0xFF10B981),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = node.lastReplyReceived.take(600),
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Save & Run Immediately
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val interval = loopIntervalText.toIntOrNull()?.coerceAtLeast(1) ?: 15
                        onSaveConfig(
                            botTitle,
                            selectedRole,
                            customRoleName,
                            customRolePrompt,
                            initialMessageToSend,
                            nextTargetNodeId,
                            selectedRule,
                            customParam,
                            brainPower.toInt(),
                            isCloudExecution,
                            cloudWebhookUrl,
                            targetInput,
                            is24x7LoopState,
                            interval,
                            autoFlushMemory
                        )
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("save_bot_config_button")
                ) {
                    Text("💾 রোল সেভ করুন", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        val interval = loopIntervalText.toIntOrNull()?.coerceAtLeast(1) ?: 15
                        onSaveConfig(
                            botTitle,
                            selectedRole,
                            customRoleName,
                            customRolePrompt,
                            initialMessageToSend,
                            nextTargetNodeId,
                            selectedRule,
                            customParam,
                            brainPower.toInt(),
                            isCloudExecution,
                            cloudWebhookUrl,
                            targetInput,
                            is24x7LoopState,
                            interval,
                            autoFlushMemory
                        )
                        onRunThisBotNow(node)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("run_this_bot_now_button")
                ) {
                    Text("▶ এখনই এই বট চালান", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
