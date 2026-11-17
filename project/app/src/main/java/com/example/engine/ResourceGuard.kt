package com.example.engine

import android.content.ComponentCallbacks2
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import java.util.concurrent.Executor

/**
 * Keeps SKRL 2 light on the phone. Tracks three real system signals and tells background
 * workers when to hold back: memory pressure (onTrimMemory), thermal status (Android 10+)
 * and low battery / battery saver. Costs no polling and no extra memory.
 */
object ResourceGuard {
    private const val MEMORY_WINDOW_MS = 120_000L
    private const val LOW_BATTERY_PERCENT = 15

    @Volatile private var lastTrimLevel = 0
    @Volatile private var lastTrimAt = 0L
    @Volatile private var thermalStatus = 0
    @Volatile private var registered = false

    fun init(context: Context) {
        if (registered) return
        registered = true
        val app = context.applicationContext
        try {
            app.registerComponentCallbacks(object : ComponentCallbacks2 {
                override fun onTrimMemory(level: Int) {
                    lastTrimLevel = level
                    lastTrimAt = SystemClock.elapsedRealtime()
                }

                override fun onLowMemory() {
                    lastTrimLevel = ComponentCallbacks2.TRIM_MEMORY_COMPLETE
                    lastTrimAt = SystemClock.elapsedRealtime()
                }

                override fun onConfigurationChanged(newConfig: Configuration) {}
            })
        } catch (_: Throwable) {
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val pm = app.getSystemService(Context.POWER_SERVICE) as PowerManager
                thermalStatus = pm.currentThermalStatus
                pm.addThermalStatusListener(Executor { it.run() }) { status ->
                    thermalStatus = status
                }
            } catch (_: Throwable) {
            }
        }
    }

    /** Returns a short reason when heavy background work should wait, or null when it is safe. */
    fun constraintReason(context: Context): String? {
        val now = SystemClock.elapsedRealtime()
        val level = lastTrimLevel
        val memoryPressure = now - lastTrimAt < MEMORY_WINDOW_MS && lastTrimAt != 0L &&
            level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW &&
            level != ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN
        if (memoryPressure) return "memory"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            thermalStatus >= PowerManager.THERMAL_STATUS_SEVERE
        ) return "thermal"
        return try {
            val app = context.applicationContext
            val pm = app.getSystemService(Context.POWER_SERVICE) as PowerManager
            val battery: Intent? =
                app.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level2 = battery?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = battery?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val status = battery?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
            val percent = if (level2 >= 0 && scale > 0) level2 * 100 / scale else 100
            when {
                pm.isPowerSaveMode && !charging -> "battery_saver"
                percent < LOW_BATTERY_PERCENT && !charging -> "low_battery"
                else -> null
            }
        } catch (_: Throwable) {
            null
        }
    }
}
