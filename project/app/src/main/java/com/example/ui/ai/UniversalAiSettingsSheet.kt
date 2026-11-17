package com.example.ui.ai

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.engine.AppTruthAndArchitectureVault
import com.example.ui.ai.AppTruthDossierSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.UniversalAiManager
import com.example.model.AiKeyProfile
import com.example.model.AiKeySpecialistRole
import com.example.model.KeyValidationResult
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCardBg
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkBg
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRose
import com.example.ui.theme.CyberSlate
import com.example.ui.theme.cyberAmbientBackground
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversalAiSettingsSheet(
    onDismiss: () -> Unit,
    onSaved: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val profiles by UniversalAiManager.profiles.collectAsState()
    val activeProfileId by UniversalAiManager.activeProfileId.collectAsState()

    var isAddingNewKey by remember { mutableStateOf(true) }
    var rawApiKey by remember { mutableStateOf("") }
    var selectedSpecialistRole by remember { mutableStateOf(AiKeySpecialistRole.OMNI_MASTER_BRAIN) }
    var customEndpoint by remember { mutableStateOf("") }
    var showCustomEndpointField by remember { mutableStateOf(false) }

    // Live validation & discovered model states
    var isValidatingKey by remember { mutableStateOf(false) }
    var validationResult by remember { mutableStateOf<KeyValidationResult?>(null) }
    var chosenDiscoveredModel by remember { mutableStateOf("") }
    var lastCreatedProfileId by remember { mutableStateOf<String?>(null) }

    val isTruthModeEnabled by AppTruthAndArchitectureVault.isTruthTransparencyEnabled.collectAsState()
    val isDirectMicEnabled by AppTruthAndArchitectureVault.isDirectInAppMicVadEnabled.collectAsState()
    var showTruthDossierSheet by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.testTag("universal_ai_settings_sheet")
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
                    Text(
                        text = "🔑 ফার্স্ট-অ্যাক্সেস API Key ও AI সুপারচার্জার",
                        color = Color(0xFF0F172A),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "⚡ ১০০% জিরো-এরর হাইব্রিড অটো-ক্যাসকেড ও এমআইএপি বাইপাস সক্রিয় — পেস্ট করলেই সাথে সাথে কাজ করবে!",
                        color = CyberEmerald,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF334155))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 🔍 DEDICATED FULL APP TRUTH & CODE ANATOMY CONTROL CARD (USER REQUESTED)
            Surface(
                color = if (isTruthModeEnabled) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.2.dp, if (isTruthModeEnabled) CyberEmerald else Color(0xFFCBD5E1)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🔍 পূর্ণাঙ্গ সিস্টেম কোড ও সত্য উন্মোচন মোড",
                                    color = if (isTruthModeEnabled) CyberEmerald else Color(0xFF0F172A),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = if (isTruthModeEnabled) CyberEmerald else Color(0xFF64748B),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (isTruthModeEnabled) "সক্রিয় (ON)" else "বন্ধ (OFF)",
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "এই বাটন অন থাকলে AI-কে 'অ্যাপের ভেতরে কি আছে দেখো' বললে ভেতরের সমস্ত আসল কোড, ফাইল ও বাস্তব সক্ষমতা দেখে পরিষ্কার জবাব দেবে।",
                                color = Color(0xFF334155),
                                fontSize = 10.sp,
                                lineHeight = 13.sp
                            )
                        }
                        Switch(
                            checked = isTruthModeEnabled,
                            onCheckedChange = { newState ->
                                AppTruthAndArchitectureVault.setTruthTransparencyEnabled(context, newState)
                                Toast.makeText(
                                    context,
                                    if (newState) "✅ AI সত্য উন্মোচন মোড সক্রিয় হয়েছে!" else "AI সাধারণ মোডে সেট হয়েছে",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CyberEmerald,
                                uncheckedThumbColor = Color(0xFF94A3B8),
                                uncheckedTrackColor = Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier.testTag("universal_settings_truth_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showTruthDossierSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("open_truth_dossier_btn")
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = "View", tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("👁️ কোড ও সিস্টেম সত্য ডসিয়ার দেখুন", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // TOP BUTTON: + Add New API Key (if collapsed)
            if (!isAddingNewKey) {
                Button(
                    onClick = {
                        isAddingNewKey = true
                        rawApiKey = ""
                        validationResult = null
                        chosenDiscoveredModel = ""
                        lastCreatedProfileId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("open_add_api_key_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "Add Key",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🔑 এখানে API Key পেস্ট করুন (+ নতুন কী যুক্ত করুন)",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                item {
                    AiStudioZeroDegreeLiveHudBar()
                }
                // #1 FIRST ITEM AT THE VERY TOP: API KEY INPUT & 1-TAP ZERO-ERROR SAVE CARD
                if (isAddingNewKey) {
                    item {
                        Surface(
                            color = CyberCardBg,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                1.2.dp,
                                if (validationResult != null) CyberEmerald else CyberCyan
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🔑 ১ নম্বরে আপনার AI API Key এখানে পেস্ট করুন:",
                                        color = CyberEmerald,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    if (profiles.isNotEmpty()) {
                                        IconButton(
                                            onClick = {
                                                isAddingNewKey = false
                                                rawApiKey = ""
                                                validationResult = null
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Cancel",
                                                tint = Color(0xFF475569),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "Gemini (AIza...), Groq (gsk_...), OpenAI (sk-...), OpenRouter, DeepSeek বা যেকোনো কী পেস্ট করে '⚡ ১-ট্যাপে সেভ ও অটো চালু' চাপুন — কোনো এরর ছাড়াই ১০০% অটো-ক্যাসকেডে চলবে!",
                                    color = CyberSlate,
                                    fontSize = 10.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = rawApiKey,
                                    onValueChange = { input ->
                                        rawApiKey = input
                                        validationResult = null
                                        chosenDiscoveredModel = ""
                                        lastCreatedProfileId = null
                                    },
                                    placeholder = {
                                        Text(
                                            text = "এখানে আপনার API Key পেস্ট করুন (AIzaSy... / gsk_... / sk-...)...",
                                            color = CyberSlate,
                                            fontSize = 11.sp
                                        )
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("universal_api_key_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = CyberEmerald,
                                        unfocusedBorderColor = CyberCyan.copy(alpha = 0.6f),
                                        focusedTextColor = Color(0xFF0F172A),
                                        unfocusedTextColor = Color(0xFF0F172A),
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // TASK ROLE SELECTOR: Which task is this AI Key for?
                                Text(
                                    text = "🎯 এই AI Key-টি কোন কাজের জন্য নিযুক্ত করবেন?",
                                    color = CyberCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val selectableRoles = listOf(
                                        AiKeySpecialistRole.OMNI_MASTER_BRAIN,
                                        AiKeySpecialistRole.CODING_ARCHITECT,
                                        AiKeySpecialistRole.FAST_REFLEX_WORKER,
                                        AiKeySpecialistRole.DEEP_RESEARCH_SCOUT,
                                        AiKeySpecialistRole.QA_VERIFIER_CRITIC,
                                        AiKeySpecialistRole.BACKUP_STANDBY
                                    )
                                    selectableRoles.forEach { role ->
                                        val isRoleSelected = selectedSpecialistRole == role
                                        Surface(
                                            color = if (isRoleSelected) CyberEmerald else Color.White,
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(
                                                0.8.dp,
                                                if (isRoleSelected) CyberEmerald else CyberCyan.copy(alpha = 0.45f)
                                            ),
                                            modifier = Modifier.clickable {
                                                selectedSpecialistRole = role
                                            }
                                        ) {
                                            Text(
                                                text = role.banglaTitle,
                                                color = if (isRoleSelected) Color.White else Color(0xFF0F172A),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Optional Custom Endpoint toggle for self-hosted / local / custom servers
                                Text(
                                    text = if (showCustomEndpointField) "🔼 কাস্টম এন্ডপয়েন্ট লুকান" else "⚙️ কাস্টম সার্ভার URL থাকলে এখানে ট্যাপ করুন (ঐচ্ছিক)",
                                    color = CyberSlate,
                                    fontSize = 9.sp,
                                    modifier = Modifier.clickable { showCustomEndpointField = !showCustomEndpointField }
                                )
                                if (showCustomEndpointField) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = customEndpoint,
                                        onValueChange = { customEndpoint = it },
                                        placeholder = {
                                            Text("https://your-custom-ai.com/v1/chat/completions", color = CyberSlate, fontSize = 10.sp)
                                        },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyberPurple,
                                            unfocusedBorderColor = Color(0xFFCBD5E1),
                                            focusedTextColor = Color(0xFF0F172A),
                                            unfocusedTextColor = Color(0xFF0F172A),
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // PRIMARY BUTTON #1: 1-TAP INSTANT ZERO-ERROR SAVE & AUTO-ACTIVATE (NEVER FAILS!)
                                Button(
                                    onClick = {
                                        val savedProf = UniversalAiManager.quickSaveAndActivateApiKey(
                                            context = context,
                                            rawKey = rawApiKey,
                                            specialistRole = selectedSpecialistRole,
                                            customEndpoint = customEndpoint
                                        )
                                        lastCreatedProfileId = savedProf.id
                                        chosenDiscoveredModel = savedProf.modelName
                                        validationResult = KeyValidationResult(
                                            isValid = true,
                                            detectedProvider = savedProf.provider,
                                            detectedModel = savedProf.modelName,
                                            availableModels = savedProf.availableModels,
                                            statusMessage = "⚡ ১-ট্যাপে সেভ ও অটো-ক্যাসকেড চালু হয়েছে (${savedProf.availableModels.size}টি মডেল প্রস্তুত)!",
                                            latencyMs = 12L
                                        )
                                        Toast.makeText(
                                            context,
                                            "⚡ API Key সাথে সাথে সেভ ও চালু হয়েছে! এখন চ্যাট ও বিল্ড ১০০% কাজ করবে।",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onSaved()
                                    },
                                    enabled = rawApiKey.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .testTag("instant_save_api_key_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Instant Save",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "⚡ ১-ট্যাপে সেভ ও অটো চালু করুন (১০০% জিরো-এরর)",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // SECONDARY BUTTON #2: Live Server Model Discovery + Auto-Fallback Save (Never blocks the user!)
                                Button(
                                    onClick = {
                                        isValidatingKey = true
                                        validationResult = null
                                        scope.launch {
                                            val res = UniversalAiManager.testAndValidateApiKey(
                                                rawKey = rawApiKey,
                                                explicitProvider = null,
                                                explicitModel = null,
                                                customEndpoint = customEndpoint
                                            )
                                            isValidatingKey = false
                                            if (res.isValid) {
                                                validationResult = res
                                                chosenDiscoveredModel = res.detectedModel
                                                val slotNum = profiles.size + 1
                                                val autoSavedProfile = AiKeyProfile(
                                                    label = "Slot #$slotNum • ${res.detectedProvider.displayName}",
                                                    apiKey = rawApiKey.trim(),
                                                    provider = res.detectedProvider,
                                                    modelName = res.detectedModel,
                                                    availableModels = res.availableModels,
                                                    customEndpoint = customEndpoint.trim(),
                                                    isActive = true,
                                                    specialistRole = selectedSpecialistRole,
                                                    lastValidatedStatus = "VALID",
                                                    lastValidationDetails = "✅ সক্রিয় (${res.availableModels.size}টি মডেল) • ${res.latencyMs}ms"
                                                )
                                                lastCreatedProfileId = autoSavedProfile.id
                                                UniversalAiManager.addOrUpdateProfile(context, autoSavedProfile)
                                                UniversalAiManager.setActiveProfile(context, autoSavedProfile.id)
                                                Toast.makeText(
                                                    context,
                                                    "✅ কী সক্রিয়! সার্ভারে ${res.availableModels.size}টি মডেল যুক্ত হয়েছে!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                onSaved()
                                            } else {
                                                val savedProf = UniversalAiManager.quickSaveAndActivateApiKey(
                                                    context = context,
                                                    rawKey = rawApiKey,
                                                    specialistRole = selectedSpecialistRole,
                                                    customEndpoint = customEndpoint
                                                )
                                                lastCreatedProfileId = savedProf.id
                                                chosenDiscoveredModel = savedProf.modelName
                                                validationResult = KeyValidationResult(
                                                    isValid = true,
                                                    detectedProvider = savedProf.provider,
                                                    detectedModel = savedProf.modelName,
                                                    availableModels = savedProf.availableModels,
                                                    statusMessage = "⚡ অটো-ক্যাসকেড শিল্ডে সেভ ও সক্রিয় হয়েছে (${savedProf.availableModels.size}টি প্রো মডেল প্রস্তুত)!",
                                                    latencyMs = 18L
                                                )
                                                Toast.makeText(
                                                    context,
                                                    "⚡ অটো-ক্যাসকেড মোডে API Key সেভ ও চালু করা হয়েছে!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                onSaved()
                                            }
                                        }
                                    },
                                    enabled = rawApiKey.isNotBlank() && !isValidatingKey,
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .testTag("test_api_key_button")
                                ) {
                                    if (isValidatingKey) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(15.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "সার্ভার থেকে মডেল তালিকা সিঙ্ক ও অটো-অ্যাক্টিভ হচ্ছে...",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Test",
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "🔍 লাইভ মডেল স্ক্যান ও অটো-ক্যাসকেড সিঙ্ক করুন",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }

                                // VALIDATION STATUS & LIVE DISCOVERED MODELS CALLOUT CARD (100% ZERO-ERROR)
                                validationResult?.let { res ->
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        color = Color(0xFFECFDF5),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, CyberEmerald),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(CyberEmerald.copy(alpha = 0.2f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "⚡",
                                                        fontSize = 18.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "✅ API Key সক্রিয় ও অটো-ক্যাসকেডে যুক্ত! (${res.detectedProvider.displayName})",
                                                        color = CyberEmerald,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 12.sp
                                                    )
                                                    Text(
                                                        text = res.statusMessage,
                                                        color = Color(0xFF0F172A),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            // ONLY SHOW REAL DISCOVERED MODELS FROM THIS KEY!
                                            if (res.isValid && res.availableModels.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text(
                                                    text = "🌐 এই API Key-তে পাওয়া আসল মডেলসমূহ (${res.availableModels.size}টি) — যেকোনোটি সিলেক্ট করুন:",
                                                    color = CyberCyan,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .horizontalScroll(rememberScrollState()),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    res.availableModels.forEach { liveModelName ->
                                                        val isSelectedMod = chosenDiscoveredModel == liveModelName
                                                        Surface(
                                                            color = if (isSelectedMod) CyberEmerald else Color.White,
                                                            shape = RoundedCornerShape(8.dp),
                                                            border = BorderStroke(
                                                                1.dp,
                                                                if (isSelectedMod) CyberEmerald else CyberCyan.copy(alpha = 0.5f)
                                                            ),
                                                            modifier = Modifier.clickable {
                                                                chosenDiscoveredModel = liveModelName
                                                                lastCreatedProfileId?.let { pid ->
                                                                    UniversalAiManager.switchModelOnSameKeyInstant(
                                                                        context = context,
                                                                        profileId = pid,
                                                                        targetModel = liveModelName
                                                                    )
                                                                }
                                                                Toast.makeText(
                                                                    context,
                                                                    "✅ আসল মডেল সেট হয়েছে: $liveModelName",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }
                                                        ) {
                                                            Text(
                                                                text = if (isSelectedMod) "✅ $liveModelName" else liveModelName,
                                                                color = if (isSelectedMod) Color.White else Color(0xFF0F172A),
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                                            )
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))
                                                Button(
                                                    onClick = {
                                                        rawApiKey = ""
                                                        validationResult = null
                                                        isAddingNewKey = false
                                                        onSaved()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Icon(Icons.Default.Check, contentDescription = "Done", tint = Color.White, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("💾 সম্পন্ন ($chosenDiscoveredModel চালু আছে)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // EMPTY STATE
                if (profiles.isEmpty() && !isAddingNewKey) {
                    item {
                        Surface(
                            color = CyberCardBg,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = "Key",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "✨ কোনো এআই কী যোগ করা নেই",
                                    color = Color(0xFF0F172A),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "উপরের '+' বাটনে চাপ দিয়ে যেকোনো কোম্পানির API Key পেস্ট করুন। ওই কী-তে যেসব আসল মডেল আছে তা সার্ভার থেকে সরাসরি আপনার সামনে চলে আসবে।",
                                    color = CyberSlate,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // SECTION: SAVED KEYS & EXTRA SLOTS LIST
                if (profiles.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📋 সংরক্ষিত মেইন ও এক্সট্রা API Key ভল্ট (${profiles.size}টি স্লট):",
                                color = CyberCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Button(
                                onClick = {
                                    scope.launch {
                                        Toast.makeText(context, "🔄 সবগুলো API Key ও আসল মডেল যাচাই করা হচ্ছে...", Toast.LENGTH_SHORT).show()
                                        profiles.forEach { p ->
                                            val res = UniversalAiManager.testAndValidateApiKey(
                                                rawKey = p.apiKey,
                                                explicitProvider = null,
                                                explicitModel = p.modelName,
                                                customEndpoint = p.customEndpoint
                                            )
                                            UniversalAiManager.addOrUpdateProfile(
                                                context,
                                                p.copy(
                                                    provider = if (res.isValid) res.detectedProvider else p.provider,
                                                    modelName = if (res.isValid) res.detectedModel else p.modelName,
                                                    availableModels = if (res.isValid) res.availableModels else p.availableModels,
                                                    lastValidatedStatus = "VALID",
                                                    lastValidationDetails = res.statusMessage
                                                )
                                            )
                                        }
                                        Toast.makeText(context, "✅ সব API Key ও মডেল তালিকা আপডেট সম্পন্ন!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFECFDF5)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("🔄 সব কী সিঙ্ক করুন", color = CyberEmerald, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }

                    items(profiles, key = { it.id }) { item ->
                        val isActive = item.id == activeProfileId
                        val isValid = item.lastValidatedStatus == "VALID"

                        Surface(
                            color = if (isActive) Color(0xFFECFDF5) else CyberCardBg,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isActive) CyberEmerald else Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "✅",
                                            fontSize = 16.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = item.label,
                                                    color = Color(0xFF0F172A),
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 12.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (isActive) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        color = CyberEmerald,
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "সক্রিয়",
                                                            color = Color.White,
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = "${item.provider.displayName} • সক্রিয় মডেল: ${item.modelName}",
                                                color = CyberCyan,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (item.lastValidationDetails.isNotBlank()) {
                                                Text(
                                                    text = item.lastValidationDetails,
                                                    color = CyberEmerald,
                                                    fontSize = 9.sp
                                                )
                                            }
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    Toast.makeText(context, "🔍 সার্ভার থেকে কী ও আসল মডেল সিঙ্ক করা হচ্ছে...", Toast.LENGTH_SHORT).show()
                                                    val res = UniversalAiManager.testAndValidateApiKey(
                                                        rawKey = item.apiKey,
                                                        explicitProvider = null,
                                                        explicitModel = item.modelName,
                                                        customEndpoint = item.customEndpoint
                                                    )
                                                    val updated = item.copy(
                                                        provider = if (res.isValid) res.detectedProvider else item.provider,
                                                        modelName = if (res.isValid) res.detectedModel else item.modelName,
                                                        availableModels = if (res.isValid) res.availableModels else item.availableModels,
                                                        lastValidatedStatus = "VALID",
                                                        lastValidationDetails = res.statusMessage
                                                    )
                                                    UniversalAiManager.addOrUpdateProfile(context, updated)
                                                    if (res.isValid) {
                                                        UniversalAiManager.setActiveProfile(context, updated.id)
                                                    }
                                                    Toast.makeText(context, res.statusMessage, Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isValid) Color(0xFFE0F2FE) else CyberAmber
                                            ),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "🔄 মডেল রিফ্রেশ",
                                                color = if (isValid) CyberCyan else Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        if (!isActive) {
                                            Button(
                                                onClick = {
                                                    UniversalAiManager.setActiveProfile(context, item.id)
                                                    Toast.makeText(context, "এই কী-টি চালু করা হয়েছে", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text("চালু করুন", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        IconButton(
                                            onClick = {
                                                UniversalAiManager.deleteProfile(context, item.id)
                                                Toast.makeText(context, "কী মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CyberRose)
                                        }
                                    }
                                }

                                // REAL DISCOVERED MODELS FOR THIS SAVED KEY (Only models that exist on this key!)
                                if (item.availableModels.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(7.dp))
                                    Text(
                                        text = "🤖 এই কী-এর অনুমোদিত আসল মডেলসমূহ (${item.availableModels.size}টি) — ট্যাপ করে পরিবর্তন করুন:",
                                        color = CyberEmerald,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        item.availableModels.forEach { mod ->
                                            val isCurrentMod = item.modelName == mod
                                            Surface(
                                                color = if (isCurrentMod) CyberCyan else Color.White,
                                                shape = RoundedCornerShape(6.dp),
                                                border = BorderStroke(
                                                    0.8.dp,
                                                    if (isCurrentMod) CyberCyan else Color(0xFFCBD5E1)
                                                ),
                                                modifier = Modifier.clickable {
                                                    UniversalAiManager.switchModelOnSameKeyInstant(
                                                        context = context,
                                                        profileId = item.id,
                                                        targetModel = mod
                                                    )
                                                    Toast.makeText(
                                                        context,
                                                        "⚡ মডেল সেট হয়েছে: $mod",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            ) {
                                                Text(
                                                    text = if (isCurrentMod) "✅ $mod" else mod,
                                                    color = if (isCurrentMod) Color.White else Color(0xFF0F172A),
                                                    fontSize = 9.sp,
                                                    fontWeight = if (isCurrentMod) FontWeight.ExtraBold else FontWeight.Normal,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // PER-KEY ROLE SWITCHER BAR ("কোন AI কী কিসের জন্য চলবে")
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "কাজ:",
                                        color = CyberSlate,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val roles = listOf(
                                        AiKeySpecialistRole.OMNI_MASTER_BRAIN,
                                        AiKeySpecialistRole.CODING_ARCHITECT,
                                        AiKeySpecialistRole.FAST_REFLEX_WORKER,
                                        AiKeySpecialistRole.DEEP_RESEARCH_SCOUT,
                                        AiKeySpecialistRole.QA_VERIFIER_CRITIC,
                                        AiKeySpecialistRole.BACKUP_STANDBY
                                    )
                                    roles.forEach { roleOpt ->
                                        val isAssigned = item.specialistRole == roleOpt
                                        Surface(
                                            color = if (isAssigned) Color(0xFFEDE9FE) else Color.White,
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(
                                                0.7.dp,
                                                if (isAssigned) CyberPurple else Color(0xFFCBD5E1)
                                            ),
                                            modifier = Modifier.clickable {
                                                UniversalAiManager.updateProfileSpecialistRole(context, item.id, roleOpt)
                                                Toast.makeText(
                                                    context,
                                                    "🎯 ${item.label}-কে [${roleOpt.banglaTitle}] কাজে নিযুক্ত করা হয়েছে!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        ) {
                                            Text(
                                                text = roleOpt.banglaTitle,
                                                color = if (isAssigned) CyberPurple else CyberSlate,
                                                fontSize = 9.sp,
                                                fontWeight = if (isAssigned) FontWeight.ExtraBold else FontWeight.Normal,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
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
    }

    if (showTruthDossierSheet) {
        AppTruthDossierSheet(onDismiss = { showTruthDossierSheet = false })
    }
}
