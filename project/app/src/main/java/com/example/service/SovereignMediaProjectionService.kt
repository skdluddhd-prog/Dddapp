package com.example.service

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.engine.LiveIntelligenceMode
import com.example.engine.SovereignGeminiLiveOmniEngine
import com.example.engine.SovereignMetaKernelEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * SovereignMediaProjectionService (v3.0 — Unstoppable Cross-App Background Live + Autonomous Worker Service):
 *
 * 1. Runs BOTH with MediaProjection (`ACTION_START_PROJECTION`) AND without MediaProjection (`ACTION_START_PERSISTENT_LIVE_BG`)
 *    using `FOREGROUND_SERVICE_TYPE_DATA_SYNC` / `SPECIAL_USE` / `MICROPHONE` + `PARTIAL_WAKE_LOCK` + `WifiLock` + `START_STICKY`.
 *    Guarantees that Gemini Live Mode and Background Autonomous Orders NEVER stop when the user switches to
 *    YouTube, Chrome, WhatsApp, Home Screen, or locks the device!
 * 2. Renders an interactive System-Wide Floating Live Capsule (`TYPE_APPLICATION_OVERLAY`) with 1-tap toggle
 *    between `🧠 ডিপ-থিংক` and `⚔️ ডিবেট মোড` right on top of YouTube or Chrome!
 */
class SovereignMediaProjectionService : Service() {

    companion object {
        private const val CHANNEL_ID = "skrl2_gemini_live_projection_channel"
        private const val NOTIFICATION_ID = 2049

        const val ACTION_START_PROJECTION = "com.example.action.START_PROJECTION"
        const val ACTION_START_PERSISTENT_LIVE_BG = "com.example.action.START_PERSISTENT_LIVE_BG"
        const val ACTION_SHOW_SYSTEM_OVERLAY = "com.example.action.SHOW_SYSTEM_OVERLAY"
        const val ACTION_UPDATE_OVERLAY_TEXT = "com.example.action.UPDATE_OVERLAY_TEXT"
        const val ACTION_STOP_PROJECTION = "com.example.action.STOP_PROJECTION"

        const val EXTRA_RESULT_CODE = "extra_result_code"
        const val EXTRA_RESULT_DATA = "extra_result_data"
        const val EXTRA_STATUS_TITLE = "extra_status_title"
        const val EXTRA_OVERLAY_BADGE = "extra_overlay_badge"
        const val EXTRA_OVERLAY_SUBTITLE = "extra_overlay_subtitle"

        @Volatile
        private var currentFloatingBadge: String = "🔴 GEMINI LIVE (OS)"
        @Volatile
        private var currentFloatingSubtitle: String = "ব্যাকগ্রাউন্ড ও স্ক্রিন পর্যবেক্ষণ সক্রিয়..."

        fun startPersistentLiveBackgroundService(
            context: Context,
            statusTitle: String = "🔴 Gemini Live ও ব্যাকগ্রাউন্ড এআই রোবট সক্রিয় (অন্য অ্যাপেও চলমান)"
        ) {
            val intent = Intent(context, SovereignMediaProjectionService::class.java).apply {
                action = ACTION_START_PERSISTENT_LIVE_BG
                putExtra(EXTRA_STATUS_TITLE, statusTitle)
            }
            try {
                ContextCompat.startForegroundService(context, intent)
            } catch (_: Throwable) {
                try {
                    context.startService(intent)
                } catch (_: Throwable) {}
            }
        }

        fun startWithProjectionPermission(context: Context, resultCode: Int, resultData: Intent) {
            val intent = Intent(context, SovereignMediaProjectionService::class.java).apply {
                action = ACTION_START_PROJECTION
                putExtra(EXTRA_RESULT_CODE, resultCode)
                putExtra(EXTRA_RESULT_DATA, resultData)
            }
            try {
                ContextCompat.startForegroundService(context, intent)
            } catch (_: Throwable) {
                try {
                    context.startService(intent)
                } catch (_: Throwable) {}
            }
        }

        fun showSystemOverlayIfPermitted(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                return
            }
            val intent = Intent(context, SovereignMediaProjectionService::class.java).apply {
                action = ACTION_SHOW_SYSTEM_OVERLAY
            }
            try {
                context.startService(intent)
            } catch (_: Throwable) {}
        }

        fun updateFloatingOverlayText(badge: String, subtitle: String) {
            currentFloatingBadge = badge
            currentFloatingSubtitle = subtitle
        }

        fun stopProjectionService(context: Context) {
            try {
                context.stopService(Intent(context, SovereignMediaProjectionService::class.java))
            } catch (_: Throwable) {}
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var stateCollectJob: Job? = null
    private var bgTaskCollectJob: Job? = null
    private var windowManager: WindowManager? = null
    private var floatingOverlayView: View? = null
    private var overlaySubtitleText: TextView? = null
    private var overlayStatusBadge: TextView? = null
    private var overlayModeBtn: TextView? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var wifiLock: WifiManager.WifiLock? = null

    override fun onCreate() {
        super.onCreate()
        createProjectionNotificationChannel()
        acquirePersistentLocks()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action.orEmpty()
        acquirePersistentLocks()

        when (action) {
            ACTION_START_PERSISTENT_LIVE_BG -> {
                val statusTitle = intent?.getStringExtra(EXTRA_STATUS_TITLE)
                    ?: "🔴 Gemini Live ও ব্যাকগ্রাউন্ড এআই রোবট সক্রিয়"
                val notif = buildProjectionNotification(statusTitle)
                startSafeForeground(notif, isMediaProjection = false)
                ensureSystemFloatingOverlayAttached()
            }

            ACTION_START_PROJECTION -> {
                val notif = buildProjectionNotification("🔴 Gemini Live Full OS Screen Share ও ব্যাকগ্রাউন্ড সক্রিয়")
                startSafeForeground(notif, isMediaProjection = true)

                val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
                    ?: Activity.RESULT_CANCELED
                @Suppress("DEPRECATION")
                val resultData = intent?.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)

                if (resultCode == Activity.RESULT_OK && resultData != null) {
                    SovereignGeminiLiveOmniEngine.onForegroundServiceReadyForProjection(
                        context = applicationContext,
                        resultCode = resultCode,
                        data = resultData
                    )
                }
                ensureSystemFloatingOverlayAttached()
            }

            ACTION_SHOW_SYSTEM_OVERLAY -> {
                val notif = buildProjectionNotification("🔴 Gemini Live ফ্লোটিং ওভারলে সক্রিয়")
                startSafeForeground(notif, isMediaProjection = false)
                ensureSystemFloatingOverlayAttached()
            }

            ACTION_UPDATE_OVERLAY_TEXT -> {
                val b = intent?.getStringExtra(EXTRA_OVERLAY_BADGE).orEmpty()
                val s = intent?.getStringExtra(EXTRA_OVERLAY_SUBTITLE).orEmpty()
                if (b.isNotBlank()) overlayStatusBadge?.text = b
                if (s.isNotBlank()) overlaySubtitleText?.text = s
            }

            ACTION_STOP_PROJECTION -> {
                if (!SovereignMetaKernelEngine.backgroundTaskState.value.isRunning) {
                    releasePersistentLocks()
                    removeSystemFloatingOverlay()
                    stopForeground(true)
                    stopSelf()
                }
            }

            else -> {
                // Service restarted by Android OS (START_STICKY) while user is in YouTube/Chrome!
                val notif = buildProjectionNotification("🔴 SKRL 2 Live ও ব্যাকগ্রাউন্ড এআই সচল আছে")
                startSafeForeground(notif, isMediaProjection = false)
                ensureSystemFloatingOverlayAttached()
            }
        }
        return START_STICKY
    }

    private fun startSafeForeground(notif: Notification, isMediaProjection: Boolean) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val fgsType = if (isMediaProjection) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                } else {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                }
                startForeground(NOTIFICATION_ID, notif, fgsType)
            } else {
                startForeground(NOTIFICATION_ID, notif)
            }
        } catch (_: Throwable) {
            try {
                startForeground(NOTIFICATION_ID, notif)
            } catch (_: Throwable) {}
        }
    }

    private fun acquirePersistentLocks() {
        try {
            if (wakeLock?.isHeld != true) {
                val pm = applicationContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
                wakeLock = pm?.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "SKRL2:LiveAndBackgroundDaemonLock"
                )?.apply {
                    setReferenceCounted(false)
                    acquire(60 * 60 * 1000L)
                }
            }
            if (wifiLock?.isHeld != true) {
                val wm = applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                @Suppress("DEPRECATION")
                wifiLock = wm?.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "SKRL2:LiveWifiLock")?.apply {
                    setReferenceCounted(false)
                    acquire()
                }
            }
        } catch (_: Throwable) {
        }
    }

    private fun releasePersistentLocks() {
        try {
            if (wakeLock?.isHeld == true) wakeLock?.release()
        } catch (_: Throwable) {}
        try {
            if (wifiLock?.isHeld == true) wifiLock?.release()
        } catch (_: Throwable) {}
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stateCollectJob?.cancel()
        bgTaskCollectJob?.cancel()
        releasePersistentLocks()
        removeSystemFloatingOverlay()
        super.onDestroy()
    }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    private fun ensureSystemFloatingOverlayAttached() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            return
        }
        if (floatingOverlayView != null) return

        try {
            val wm = getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return
            windowManager = wm

            val density = resources.displayMetrics.density
            val container = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(
                    (12 * density).toInt(),
                    (8 * density).toInt(),
                    (12 * density).toInt(),
                    (8 * density).toInt()
                )
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 18f * density
                    setColor(Color.parseColor("#F2060B16"))
                    setStroke((1.6f * density).toInt(), Color.parseColor("#00F0FF"))
                }
            }

            val topRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val badgeView = TextView(this).apply {
                text = currentFloatingBadge
                setTextColor(Color.parseColor("#00F0FF"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f)
                paint.isFakeBoldText = true
            }
            overlayStatusBadge = badgeView

            // 1-Tap Live Mode Switcher (Deep-Think <-> Debate Mode) right over YouTube/Chrome!
            val modeSwitchBtn = TextView(this).apply {
                val curMode = SovereignGeminiLiveOmniEngine.sessionState.value.intelligenceMode
                text = if (curMode == LiveIntelligenceMode.GOD_LEVEL_LIVE_DEBATE) "  [⚔️ ডিবেট]  " else "  [🧠 ডিপ-থিংক]  "
                setTextColor(Color.parseColor("#F59E0B"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                paint.isFakeBoldText = true
                setOnClickListener {
                    val nextMode = SovereignGeminiLiveOmniEngine.toggleLiveIntelligenceMode()
                    text = if (nextMode == LiveIntelligenceMode.GOD_LEVEL_LIVE_DEBATE) "  [⚔️ ডিবেট]  " else "  [🧠 ডিপ-থিংক]  "
                }
            }
            overlayModeBtn = modeSwitchBtn

            val openAppBtn = TextView(this).apply {
                text = " [⛶ অ্যাপ] "
                setTextColor(Color.parseColor("#10B981"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                paint.isFakeBoldText = true
                setOnClickListener {
                    val openIntent = Intent(this@SovereignMediaProjectionService, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }
                    startActivity(openIntent)
                }
            }

            val closeBtn = TextView(this).apply {
                text = " ✕ "
                setTextColor(Color.parseColor("#F43F5E"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                paint.isFakeBoldText = true
                setOnClickListener {
                    SovereignGeminiLiveOmniEngine.stopLiveSession(applicationContext)
                    SovereignMetaKernelEngine.stopBackgroundAutonomousOrder(applicationContext)
                    removeSystemFloatingOverlay()
                    stopSelf()
                }
            }

            topRow.addView(badgeView)
            topRow.addView(modeSwitchBtn)
            topRow.addView(openAppBtn)
            topRow.addView(closeBtn)

            val subView = TextView(this).apply {
                text = currentFloatingSubtitle
                setTextColor(Color.WHITE)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f)
                maxLines = 2
                ellipsize = TextUtils.TruncateAt.END
            }
            overlaySubtitleText = subView

            container.addView(topRow)
            container.addView(subView)

            val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val lp = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                overlayType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                y = (42 * density).toInt()
            }

            // Allow dragging the floating capsule anywhere on screen over YouTube/Chrome
            var initialX = 0
            var initialY = 0
            var initialTouchX = 0f
            var initialTouchY = 0f
            container.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = lp.x
                        initialY = lp.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        false
                    }
                    MotionEvent.ACTION_MOVE -> {
                        lp.x = initialX + (event.rawX - initialTouchX).toInt()
                        lp.y = initialY + (event.rawY - initialTouchY).toInt()
                        try {
                            wm.updateViewLayout(container, lp)
                        } catch (_: Throwable) {}
                        true
                    }
                    else -> false
                }
            }

            wm.addView(container, lp)
            floatingOverlayView = container

            stateCollectJob?.cancel()
            stateCollectJob = serviceScope.launch {
                SovereignGeminiLiveOmniEngine.sessionState.collectLatest { st ->
                    val modeTag = if (st.intelligenceMode == LiveIntelligenceMode.GOD_LEVEL_LIVE_DEBATE) "⚔️ DEBATE" else "🧠 DEEP"
                    overlayStatusBadge?.text = "🔴 LIVE ($modeTag) • ${st.phase.labelBangla.take(16)}"
                    overlayModeBtn?.text = if (st.intelligenceMode == LiveIntelligenceMode.GOD_LEVEL_LIVE_DEBATE) "  [⚔️ ডিবেট]  " else "  [🧠 ডিপ-থিংক]  "
                    overlaySubtitleText?.text = st.liveAiSubtitle
                }
            }

            bgTaskCollectJob?.cancel()
            bgTaskCollectJob = serviceScope.launch {
                SovereignMetaKernelEngine.backgroundTaskState.collectLatest { bg ->
                    if (bg.isRunning) {
                        overlayStatusBadge?.text = "🤖 BG ROBOT (${bg.currentPhaseIndex}/${bg.totalPhases})"
                        overlaySubtitleText?.text = "${bg.currentPhaseTitleBangla}: ${bg.liveProgressDetailBangla}"
                        val notif = buildProjectionNotification("🤖 ${bg.currentPhaseTitleBangla} — ${bg.userOrder.take(35)}")
                        val nm = getSystemService(NotificationManager::class.java)
                        nm?.notify(NOTIFICATION_ID, notif)
                    }
                }
            }
        } catch (_: Throwable) {
        }
    }

    private fun removeSystemFloatingOverlay() {
        try {
            val view = floatingOverlayView
            if (view != null) {
                windowManager?.removeView(view)
            }
        } catch (_: Throwable) {
        }
        floatingOverlayView = null
        overlaySubtitleText = null
        overlayStatusBadge = null
        overlayModeBtn = null
    }

    private fun createProjectionNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "SKRL 2 Gemini Live & Background AI Robot",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Keeps Gemini Live and Background Autonomous AI Tasks active across all apps (YouTube, Chrome, etc.)"
                    setSound(null, null)
                    enableVibration(false)
                }
                val manager = getSystemService(NotificationManager::class.java)
                manager?.createNotificationChannel(channel)
            } catch (_: Throwable) {
            }
        }
    }

    private fun buildProjectionNotification(statusText: String): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val stopIntent = Intent(this, SovereignMediaProjectionService::class.java).apply {
            action = ACTION_STOP_PROJECTION
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val contentPi = PendingIntent.getActivity(this, 0, openIntent, flags)
        val stopPi = PendingIntent.getService(this, 1, stopIntent, flags)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SKRL 2 • সার্বভৌম লাইভ ও ব্যাকগ্রাউন্ড রোবট")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(contentPi)
            .setOngoing(true)
            .setSilent(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "বন্ধ করুন", stopPi)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
