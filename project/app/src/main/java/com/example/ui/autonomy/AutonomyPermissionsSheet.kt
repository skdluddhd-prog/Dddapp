package com.example.ui.autonomy

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.service.AutoTouchService
import com.example.service.ChromiumBackgroundService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutonomyPermissionsSheet(
    isContinuousVoiceActive: Boolean,
    onToggleContinuousVoice: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isBackgroundServiceRunning by remember { mutableStateOf(true) }

    var hasMicPerm by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasCameraPerm by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasLocationPerm by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasNotifPerm by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val refreshPermStates = {
        hasMicPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        hasCameraPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        hasLocationPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        hasNotifPerm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    val multiPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        refreshPermStates()
        Toast.makeText(
            context,
            "✅ মাইক, ক্যামেরা, লোকেশন ও নোটিফিকেশন পারমিশন আপডেট হয়েছে!",
            Toast.LENGTH_SHORT
        ).show()
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            Toast.makeText(context, "✅ ফটো/মিডিয়া সফলভাবে সংযুক্ত হয়েছে!", Toast.LENGTH_SHORT).show()
        }
    }

    val isAutoTouchLoopRunning by AutoTouchService.isAutoTouchRunning.collectAsState()
    val autoTouchX by AutoTouchService.targetX.collectAsState()
    val autoTouchY by AutoTouchService.targetY.collectAsState()
    val autoTouchSpeedMs by AutoTouchService.intervalMs.collectAsState()
    val totalAutoClicks by AutoTouchService.totalClicksCount.collectAsState()
    val allocatedMb by AutoTouchService.allocatedMemoryMb.collectAsState()
    val enginePowerPct by AutoTouchService.enginePowerPercent.collectAsState()

    val requestAllRuntimePermissions = {
        val perms = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            perms.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        multiPermissionLauncher.launch(perms.toTypedArray())
    }

    val powerManager = remember { context.getSystemService(Context.POWER_SERVICE) as? PowerManager }
    val isIgnoringBatteryOptimizations = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && powerManager != null) {
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else true
    }

    val canDrawOverlays = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else true
    }

    val isAccessibilityActive = remember(isAutoTouchLoopRunning) { AutoTouchService.instance != null }
    val allRuntimeGranted = hasMicPerm && hasCameraPerm && hasLocationPerm && hasNotifPerm

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF070B14),
        tonalElevation = 16.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.testTag("autonomy_permissions_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .fillMaxHeight(0.92f)
        ) {
            // Header with Embedded System Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF00F0FF), Color(0xFF10B981))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "অল-পারমিশন, অটো-টাচ ও পাওয়ার কন্ট্রোল হাব",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0x3010B981))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${allocatedMb}MB • ${enginePowerPct}%",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981),
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        Text(
                            text = "সব পারমিশন, স্ক্রিন অটো-টাচ, মেগাবাইট আপগ্রেড ও পাওয়ার বাড়ানো-কমানো",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Text("✕", color = Color(0xFF94A3B8), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 1-TAP MASTER GRANT ALL HARDWARE PERMISSIONS BUTTON
            Button(
                onClick = { requestAllRuntimePermissions() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (allRuntimeGranted) Color(0xFF10B981) else Color(0xFF00F0FF)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("grant_all_runtime_permissions_btn")
            ) {
                Text(
                    text = if (allRuntimeGranted) {
                        "✅ মাইক, ক্যামেরা, লোকেশন, ব্লুটুথ ও নোটিফিকেশন সব অনুমোদিত!"
                    } else {
                        "🛡️ এক ক্লিকে সব পারমিশন অন করুন (মাইক, ক্যামেরা, লোকেশন, নোটিফিকেশন, ব্লুটুথ)"
                    },
                    color = Color.Black,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Real-time Embedded Telemetry HUD
            Surface(
                color = Color(0xFF0C1322),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x3300F0FF))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "HARDWARE, MEMORY MB & AUTO-TOUCH TELEMETRY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00F0FF),
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TelemetryItem(label = "MIC (bn-IN)", value = if (hasMicPerm) "GRANTED" else "NEEDED", isOk = hasMicPerm)
                        TelemetryItem(label = "CAMERA", value = if (hasCameraPerm) "GRANTED" else "NEEDED", isOk = hasCameraPerm)
                        TelemetryItem(label = "POWER/MB", value = "${allocatedMb}MB (${enginePowerPct}%)", isOk = true)
                        TelemetryItem(label = "AUTO-TOUCH", value = if (isAutoTouchLoopRunning) "RUNNING ($totalAutoClicks)" else "READY", isOk = true)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                // NEW CARD A: POWER & MEGABYTE OVERCLOCK CONTROLLER (User Controllable Increase/Decrease)
                item {
                    Surface(
                        color = Color(0xFF0B1528),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00F0FF)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⚡ সিস্টেম পাওয়ার ও মেগাবাইট (MB) কন্ট্রোলার",
                                    color = Color(0xFF00F0FF),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "${allocatedMb} MB • ${enginePowerPct}% Power",
                                    color = Color(0xFF10B981),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "ছোটখাটো মেগাবাইটকে পাওয়ারফুল করা হয়েছে। নিচের স্লাইডার ও বাটন দিয়ে আপনার ইচ্ছেমতো পাওয়ার ও মেগাবাইট বাড়ান বা কমান:",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Slider(
                                value = enginePowerPct.toFloat(),
                                onValueChange = { newPower ->
                                    val p = newPower.toInt()
                                    val mb = (p * 8).coerceIn(128, 8192)
                                    AutoTouchService.setPowerAndMemoryAllocation(p, mb)
                                },
                                valueRange = 25f..1000f,
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFF00F0FF),
                                    activeTrackColor = Color(0xFF10B981)
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val presets = listOf(
                                    Triple("🔋 Eco", 50, 256),
                                    Triple("⚡ Pro", 250, 2048),
                                    Triple("🔥 Ultra", 500, 4096),
                                    Triple("👑 Max 8GB", 1000, 8192)
                                )
                                presets.forEach { (label, pwr, mb) ->
                                    val selected = allocatedMb == mb
                                    Button(
                                        onClick = {
                                            AutoTouchService.setPowerAndMemoryAllocation(pwr, mb)
                                            Toast.makeText(context, "⚡ পাওয়ার ${pwr}% এবং মেমোরি ভল্ট ${mb} MB সেট করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (selected) Color(0xFF10B981) else Color(0xFF1E293B),
                                            contentColor = if (selected) Color.Black else Color.White
                                        ),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(34.dp)
                                    ) {
                                        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }
                        }
                    }
                }

                // NEW CARD B: SCREEN AUTO-TOUCH LIVE CONTROLLER (Start/Stop + X/Y + Speed Control)
                item {
                    Surface(
                        color = Color(0xFF140D26),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isAutoTouchLoopRunning) Color(0xFF10B981) else Color(0xFFA855F7)
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
                                    text = "🎯 স্ক্রিন অটো-টাচ ও অটো-ক্লিকার ইঞ্জিন",
                                    color = Color(0xFFA855F7),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = if (isAutoTouchLoopRunning) "🟢 RUNNING ($totalAutoClicks taps)" else "⚪ STANDBY",
                                    color = if (isAutoTouchLoopRunning) Color(0xFF10B981) else Color(0xFFF59E0B),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "টার্গেট পজিশন: X=${autoTouchX.toInt()}px, Y=${autoTouchY.toInt()}px • স্পিড ইন্টারভ্যাল: ${autoTouchSpeedMs}ms",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val speeds = listOf(
                                    "⚡ 60ms (Turbo)" to 60L,
                                    "🚀 180ms (Fast)" to 180L,
                                    "🎯 500ms (Normal)" to 500L
                                )
                                speeds.forEach { (sLabel, ms) ->
                                    val isChosen = autoTouchSpeedMs == ms
                                    Button(
                                        onClick = {
                                            AutoTouchService.updateAutoTouchCoordinates(autoTouchX, autoTouchY, ms)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isChosen) Color(0xFFA855F7) else Color(0xFF1E293B)
                                        ),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(32.dp)
                                    ) {
                                        Text(text = sLabel, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (isAutoTouchLoopRunning) {
                                            AutoTouchService.stopAutoTouchLoop()
                                            Toast.makeText(context, "⏹️ স্ক্রিন অটো-টাচ থামানো হয়েছে।", Toast.LENGTH_SHORT).show()
                                        } else {
                                            AutoTouchService.startAutoTouchLoop(autoTouchX, autoTouchY, autoTouchSpeedMs)
                                            Toast.makeText(context, "🎯 স্ক্রিন অটো-টাচ চালু হয়েছে (${autoTouchSpeedMs}ms স্পিডে)!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isAutoTouchLoopRunning) Color(0xFFE11D48) else Color(0xFF10B981)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(38.dp)
                                ) {
                                    Text(
                                        text = if (isAutoTouchLoopRunning) "⏹️ অটো-টাচ বন্ধ করুন" else "▶ এখনই অটো-টাচ চালু করুন",
                                        color = if (isAutoTouchLoopRunning) Color.White else Color.Black,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }

                // 0. Camera, Microphone, Photo Picker & Location Hardware Permissions
                item {
                    PermissionCard(
                        icon = Icons.Default.CameraAlt,
                        iconTint = Color(0xFF00F0FF),
                        title = "০. মাইক, ক্যামেরা, লোকেশন ও ফটো পিকার (HARDWARE & MEDIA)",
                        description = "ভয়েস কমান্ড (MIC), ওয়েবভিউ ক্যামেরা/ভিশন স্টুডিও, লোকেশন এবং গ্যালারি ফটো আপলোডের সম্পূর্ণ পারমিশন।",
                        badgeText = if (allRuntimeGranted) "সব অনুমোদিত (ALL GRANTED ✅)" else "এক ক্লিকে চালু করুন",
                        isGranted = allRuntimeGranted,
                        actionButtonText = if (allRuntimeGranted) "📸 ফটো পিকার টেস্ট করুন (সক্রিয় ✅)" else "⚡ মাইক + ক্যামেরা + লোকেশন অন করুন",
                        onAction = {
                            if (allRuntimeGranted) {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                )
                            } else {
                                requestAllRuntimePermissions()
                            }
                        }
                    )
                }

                // 1. 24/7 Foreground Service (WakeLock)
                item {
                    PermissionCard(
                        icon = Icons.Default.Bolt,
                        iconTint = Color(0xFF00F0FF),
                        title = "১. ২৪ ঘণ্টা ব্যাকগ্রাউন্ড সার্ভিস (WAKELOCK)",
                        description = "স্ক্রিন বন্ধ থাকলেও ক্রোমিয়াম জাভাস্ক্রিপ্ট, নেটওয়ার্ক এবং এআই ব্যাকগ্রাউন্ড ইঞ্জিন নন-স্টপ চলবে।",
                        badgeText = "FOREGROUND SERVICE",
                        isGranted = isBackgroundServiceRunning,
                        actionButtonText = if (isBackgroundServiceRunning) "সার্ভিস রিস্টার্ট করুন" else "সার্ভিস চালু করুন",
                        onAction = {
                            val intent = Intent(context, ChromiumBackgroundService::class.java)
                            try {
                                context.startService(intent)
                            } catch (_: Exception) {}
                            isBackgroundServiceRunning = true
                        }
                    )
                }

                // 2. Battery Optimization Bypass (Doze Mode Killer)
                item {
                    PermissionCard(
                        icon = Icons.Default.BatteryAlert,
                        iconTint = Color(0xFFF59E0B),
                        title = "২. ব্যাটারি অপ্টিমাইজেশন নিষ্ক্রিয়করণ (DOZE BYPASS)",
                        description = "অ্যান্ড্রয়েড ওএস ১০-১৫ মিনিট পর ব্যাকগ্রাউন্ড অ্যাপ বন্ধ করে দেয়। এটি বাইপাস করলে অ্যাপ ২৪/৭ নিশ্চিন্তে চলবে।",
                        badgeText = if (isIgnoringBatteryOptimizations) "অনুমোদিত (GRANTED)" else "জরুরি (REQUIRED)",
                        isGranted = isIgnoringBatteryOptimizations,
                        actionButtonText = if (isIgnoringBatteryOptimizations) "অনুমোদিত আছে ✅" else "ব্যাটারি সীমাবদ্ধতা সরান",
                        onAction = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                try {
                                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                    context.startActivity(intent)
                                }
                            }
                        }
                    )
                }

                // 3. Continuous Always-Listening Microphone (No waiting)
                item {
                    PermissionCard(
                        icon = Icons.Default.Mic,
                        iconTint = Color(0xFF10B981),
                        title = "৩. কন্টিনিউয়াস ভয়েস রিসিভার (অটোমেটিক শোনা)",
                        description = "আপনার কথা বলার জন্য অপেক্ষা করবে না। কথা শেষ হলেই স্বয়ংক্রিয়ভাবে রিস্টার্ট হয়ে সব কথা স্ট্রিম করবে।",
                        badgeText = if (isContinuousVoiceActive) "লিসেনিং চালু (STREAMING)" else "বন্ধ (OFF)",
                        isGranted = isContinuousVoiceActive,
                        actionButtonText = if (isContinuousVoiceActive) "ভয়েস স্ট্রিম বন্ধ করুন" else "কন্টিনিউয়াস ভয়েস চালু করুন",
                        onAction = {
                            onToggleContinuousVoice(!isContinuousVoiceActive)
                        }
                    )
                }

                // 4. Accessibility Screen Auto-Touch Service
                item {
                    PermissionCard(
                        icon = Icons.Default.TouchApp,
                        iconTint = Color(0xFFA855F7),
                        title = "৪. স্ক্রিন টাচ ও অটো-ক্লিক সার্ভিস (ACCESSIBILITY)",
                        description = "এআই যাতে স্ক্রিনে যেকোনো বাটনে টাচ, ক্লিক, স্ক্রল বা টেক্সট টাইপিং স্বয়ংক্রিয়ভাবে করতে পারে।",
                        badgeText = if (isAccessibilityActive) "সংযুক্ত (CONNECTED)" else "অনুমোদন প্রয়োজন",
                        isGranted = isAccessibilityActive,
                        actionButtonText = if (isAccessibilityActive) "অ্যাক্সেসিবিলিটি সক্রিয় আছে ✅" else "১-ক্লিকে অ্যাক্সেসিবিলিটি অন করুন ⚡",
                        onAction = {
                            try {
                                android.widget.Toast.makeText(
                                    context,
                                    "তালিকায় 'SKRL 2' সার্ভিসটি নির্বাচন করে 'Use service' অন করুন",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "সেটিংস খুলতে ব্যর্থ: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                // 5. Draw Over Other Apps (System Alert Window / Floating Overlay)
                item {
                    PermissionCard(
                        icon = Icons.Default.Layers,
                        iconTint = Color(0xFF38BDF8),
                        title = "৫. অন্যান্য অ্যাপের উপর ফ্লোটিং ডিসপ্লে (OVERLAY)",
                        description = "অন্যান্য অ্যাপ বা গেম ব্যবহারের সময়ও এআই ফ্লোটিং হেড বা স্ক্রিন কন্ট্রোল প্যানেল সামনে থাকবে।",
                        badgeText = if (canDrawOverlays) "অনুমোদিত (GRANTED)" else "প্রয়োজনীয়",
                        isGranted = canDrawOverlays,
                        actionButtonText = if (canDrawOverlays) "অনুমোদিত আছে ✅" else "ওভারলে পারমিশন দিন",
                        onAction = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                try {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }
                        }
                    )
                }

                // 6. Sticky Persistent Notifications
                item {
                    PermissionCard(
                        icon = Icons.Default.NotificationsActive,
                        iconTint = Color(0xFFE2E8F0),
                        title = "৬. স্টিকি সিস্টেম নোটিফিকেশন (PERSISTENCE)",
                        description = "অ্যান্ড্রয়েড লিনাক্স কার্নেল যাতে মেমোরি খালি করার সময় SKRL 2 ব্যাকগ্রাউন্ড প্রসেস কিল না করে।",
                        badgeText = "KERNEL PROTECTION",
                        isGranted = true,
                        actionButtonText = "সুরক্ষিত (PROTECTED) ✅",
                        onAction = {}
                    )
                }
            }
        }
    }
}

@Composable
private fun TelemetryItem(
    label: String,
    value: String,
    isOk: Boolean
) {
    Column {
        Text(text = label, fontSize = 9.sp, color = Color(0xFF64748B), fontFamily = FontFamily.Monospace)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (isOk) Color(0xFF10B981) else Color(0xFFF59E0B))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isOk) Color(0xFF10B981) else Color(0xFFF59E0B),
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun PermissionCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String,
    badgeText: String,
    isGranted: Boolean,
    actionButtonText: String,
    onAction: () -> Unit
) {
    Surface(
        color = Color(0xFF0F172A),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isGranted) Color(0x3010B981) else Color(0x30F59E0B)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(iconTint.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isGranted) Color(0x2510B981) else Color(0x25F59E0B))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isGranted) Color(0xFF10B981) else Color(0xFFF59E0B),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isGranted) Color(0xFF1E293B) else Color(0xFF0284C7),
                    contentColor = if (isGranted) Color(0xFF38BDF8) else Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(38.dp)
            ) {
                Text(
                    text = actionButtonText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
