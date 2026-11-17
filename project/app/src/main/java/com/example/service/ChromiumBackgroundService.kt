package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.engine.QuadChatAndStrainerEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Quiet, Crash-Proof Background Service:
 * - Started via standard `startService()` (never `startForegroundService()`), completely preventing
 *   Android 12–16 (`targetSdk = 36`) `ForegroundServiceDidNotStartInTimeException` / `RemoteServiceException`
 *   that kicks the app to the background 5 seconds after launch.
 * - Zero audio, zero vibration, zero repetitive notification re-alerting noise.
 */
class ChromiumBackgroundService : Service() {

    companion object {
        private const val CHANNEL_ID = "skrl2_silent_daemon_channel"
        private const val NOTIFICATION_ID = 1001
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var heartbeatJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createSilentNotificationChannel()
        startQuietHeartbeatLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (heartbeatJob?.isActive != true) {
            startQuietHeartbeatLoop()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        heartbeatJob?.cancel()
        super.onDestroy()
    }

    private fun startQuietHeartbeatLoop() {
        heartbeatJob?.cancel()
        heartbeatJob = serviceScope.launch {
            while (isActive) {
                try {
                    QuadChatAndStrainerEngine.record24x7Heartbeat(applicationContext)
                } catch (_: Throwable) {
                }
                delay(30_000L)
            }
        }
    }

    private fun createSilentNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "SKRL 2 Silent Engine",
                    NotificationManager.IMPORTANCE_MIN
                ).apply {
                    description = "Silent background state sync"
                    setSound(null, null)
                    enableVibration(false)
                    setShowBadge(false)
                }
                val manager = getSystemService(NotificationManager::class.java)
                manager?.createNotificationChannel(channel)
            } catch (_: Throwable) {
            }
        }
    }

    @Suppress("unused")
    private fun buildSilentNotification(statusText: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, openAppIntent, pendingFlags)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("SKRL 2 Sovereign OS")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentIntent(pendingIntent)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }
}
