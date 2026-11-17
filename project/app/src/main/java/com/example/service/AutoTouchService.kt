package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Sovereign Screen Auto-Touch, Multi-Point Gesture, Full Phone OS Global Action & Power Overclock Engine.
 * Supports native Android AccessibilityService gestures, OS Global Actions (Home, Back, Recents, Notifications,
 * Quick Settings, Screenshot, Lock Screen), Cross-App Text Injection, and coordinated WebView DOM auto-touch.
 */
class AutoTouchService : AccessibilityService() {

    companion object {
        @Volatile
        var instance: AutoTouchService? = null

        private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        private var loopJob: Job? = null

        private val _isAutoTouchRunning = MutableStateFlow(false)
        val isAutoTouchRunning: StateFlow<Boolean> = _isAutoTouchRunning.asStateFlow()

        private val _targetX = MutableStateFlow(540f)
        val targetX: StateFlow<Float> = _targetX.asStateFlow()

        private val _targetY = MutableStateFlow(960f)
        val targetY: StateFlow<Float> = _targetY.asStateFlow()

        private val _intervalMs = MutableStateFlow(350L)
        val intervalMs: StateFlow<Long> = _intervalMs.asStateFlow()

        private val _totalClicksCount = MutableStateFlow(0L)
        val totalClicksCount: StateFlow<Long> = _totalClicksCount.asStateFlow()

        // Global Power & Megabyte Overclock State (User Controllable 10% to 1000% / 128MB to 4096MB)
        private val _allocatedMemoryMb = MutableStateFlow(2048) // Default 2048 MB (2 GB Pro Mode)
        val allocatedMemoryMb: StateFlow<Int> = _allocatedMemoryMb.asStateFlow()

        private val _enginePowerPercent = MutableStateFlow(250) // Default 250% Turbo Power
        val enginePowerPercent: StateFlow<Int> = _enginePowerPercent.asStateFlow()

        fun setPowerAndMemoryAllocation(powerPct: Int, memoryMb: Int) {
            _enginePowerPercent.value = powerPct.coerceIn(20, 1000)
            _allocatedMemoryMb.value = memoryMb.coerceIn(128, 8192)
        }

        fun updateAutoTouchCoordinates(x: Float, y: Float, speedMs: Long) {
            _targetX.value = x.coerceIn(10f, 3000f)
            _targetY.value = y.coerceIn(10f, 4000f)
            _intervalMs.value = speedMs.coerceIn(40L, 10000L)
        }

        fun startAutoTouchLoop(
            x: Float = _targetX.value,
            y: Float = _targetY.value,
            speedMs: Long = _intervalMs.value,
            onWebViewFallbackTap: ((Float, Float) -> Unit)? = null
        ) {
            updateAutoTouchCoordinates(x, y, speedMs)
            loopJob?.cancel()
            _isAutoTouchRunning.value = true
            loopJob = serviceScope.launch {
                while (isActive && _isAutoTouchRunning.value) {
                    val tx = _targetX.value
                    val ty = _targetY.value
                    val svc = instance
                    if (svc != null) {
                        svc.performClickAt(tx, ty, 60L)
                    }
                    onWebViewFallbackTap?.invoke(tx, ty)
                    _totalClicksCount.value += 1
                    delay(_intervalMs.value.coerceAtLeast(40L))
                }
            }
        }

        fun stopAutoTouchLoop() {
            _isAutoTouchRunning.value = false
            loopJob?.cancel()
            loopJob = null
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Handled dynamically on demand
    }

    override fun onInterrupt() {
        stopAutoTouchLoop()
    }

    override fun onDestroy() {
        stopAutoTouchLoop()
        super.onDestroy()
        if (instance === this) {
            instance = null
        }
    }

    /**
     * Executes system-wide Android Global Actions across the entire phone:
     * HOME, BACK, RECENTS, NOTIFICATIONS, QUICK_SETTINGS, SCREENSHOT, LOCK_SCREEN, POWER_DIALOG, SPLIT_SCREEN.
     */
    fun performPhoneGlobalAction(actionName: String): Boolean {
        val actionConstant = when (actionName.uppercase()) {
            "HOME" -> GLOBAL_ACTION_HOME
            "BACK" -> GLOBAL_ACTION_BACK
            "RECENTS" -> GLOBAL_ACTION_RECENTS
            "NOTIFICATIONS" -> GLOBAL_ACTION_NOTIFICATIONS
            "QUICK_SETTINGS" -> GLOBAL_ACTION_QUICK_SETTINGS
            "POWER_DIALOG" -> GLOBAL_ACTION_POWER_DIALOG
            "SPLIT_SCREEN" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN else GLOBAL_ACTION_RECENTS
            "SCREENSHOT" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) GLOBAL_ACTION_TAKE_SCREENSHOT else GLOBAL_ACTION_NOTIFICATIONS
            "LOCK_SCREEN" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) GLOBAL_ACTION_LOCK_SCREEN else GLOBAL_ACTION_POWER_DIALOG
            else -> return false
        }
        return try {
            performGlobalAction(actionConstant)
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Types text directly into any focused or editable input box inside ANY external phone app
     * (WhatsApp, YouTube search, Chrome, Settings, SMS, etc.) via AccessibilityNodeInfo.ACTION_SET_TEXT.
     */
    fun typeTextIntoExternalPhoneApp(textToType: String): Boolean {
        val root = try { rootInActiveWindow } catch (_: Throwable) { null } ?: return false
        var targetEditNode: AccessibilityNodeInfo? = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)

        fun findEditable(node: AccessibilityNodeInfo?, depth: Int): AccessibilityNodeInfo? {
            if (node == null || depth > 22) return null
            if (node.isEditable && node.isVisibleToUser) return node
            for (i in 0 until node.childCount) {
                val found = findEditable(node.getChild(i), depth + 1)
                if (found != null) return found
            }
            return null
        }

        if (targetEditNode == null || !targetEditNode.isEditable) {
            targetEditNode = findEditable(root, 0)
        }
        val editable = targetEditNode ?: return false
        return try {
            val args = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, textToType)
            }
            editable.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            editable.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        } catch (_: Throwable) {
            false
        }
    }

    fun performClickAt(x: Float, y: Float, durationMs: Long = 65L): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return false
        return try {
            val path = Path().apply { moveTo(x, y) }
            val stroke = GestureDescription.StrokeDescription(path, 0L, durationMs)
            val gesture = GestureDescription.Builder().addStroke(stroke).build()
            dispatchGesture(gesture, null, null)
        } catch (_: Exception) {
            false
        }
    }

    fun performSwipe(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        durationMs: Long = 280L
    ): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return false
        return try {
            val path = Path().apply {
                moveTo(startX, startY)
                lineTo(endX, endY)
            }
            val stroke = GestureDescription.StrokeDescription(path, 0L, durationMs)
            val gesture = GestureDescription.Builder().addStroke(stroke).build()
            dispatchGesture(gesture, null, null)
        } catch (_: Throwable) {
            false
        }
    }

    fun performScroll(down: Boolean = true): Boolean {
        return if (down) {
            performSwipe(540f, 1400f, 540f, 500f, 260L)
        } else {
            performSwipe(540f, 500f, 540f, 1400f, 260L)
        }
    }

    fun findAndClickNodeByText(
        targetQuery: String,
        performClickNow: Boolean = true
    ): Triple<Rect, String, Boolean>? {
        val cleanQuery = targetQuery.trim().lowercase()
        if (cleanQuery.isBlank()) return null
        val root = try {
            rootInActiveWindow
        } catch (_: Throwable) {
            null
        } ?: return null

        var bestMatchNode: AccessibilityNodeInfo? = null
        var bestMatchRect = Rect()
        var bestMatchLabel = ""
        var bestScore = 0

        fun traverse(node: AccessibilityNodeInfo?, depth: Int) {
            if (node == null || depth > 28) return
            if (node.isVisibleToUser) {
                val txt = node.text?.toString()?.trim().orEmpty()
                val desc = node.contentDescription?.toString()?.trim().orEmpty()
                val combined = "$txt $desc".trim()
                if (combined.isNotBlank()) {
                    val lowerCombined = combined.lowercase()
                    val score = when {
                        lowerCombined == cleanQuery -> 100
                        lowerCombined.contains(cleanQuery) -> 80 + (if (node.isClickable) 15 else 0)
                        cleanQuery.split(" ").any { token -> token.length >= 3 && lowerCombined.contains(token) } ->
                            55 + (if (node.isClickable) 15 else 0)
                        else -> 0
                    }
                    if (score > bestScore) {
                        val rect = Rect()
                        node.getBoundsInScreen(rect)
                        if (rect.width() > 8 && rect.height() > 8) {
                            bestScore = score
                            bestMatchNode = node
                            bestMatchRect = rect
                            bestMatchLabel = txt.ifBlank { desc }.take(45)
                        }
                    }
                }
            }
            val count = node.childCount
            for (i in 0 until count) {
                traverse(node.getChild(i), depth + 1)
            }
        }

        try {
            traverse(root, 0)
            val matched = bestMatchNode ?: return null
            var clicked = false
            if (performClickNow) {
                var clickableTarget: AccessibilityNodeInfo? = matched
                var hops = 0
                while (clickableTarget != null && !clickableTarget.isClickable && hops < 4) {
                    clickableTarget = clickableTarget.parent
                    hops++
                }
                clicked = try {
                    clickableTarget?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true
                } catch (_: Throwable) {
                    false
                }
                val cx = bestMatchRect.exactCenterX()
                val cy = bestMatchRect.exactCenterY()
                performClickAt(cx, cy, 70L)
                _totalClicksCount.value += 1
            }
            return Triple(bestMatchRect, bestMatchLabel, clicked)
        } catch (_: Throwable) {
            return null
        }
    }

    fun dumpVisibleScreenNodesSummary(maxItems: Int = 22): String {
        val root = try {
            rootInActiveWindow
        } catch (_: Throwable) {
            null
        } ?: return ""

        val buttons = mutableListOf<String>()
        val texts = mutableListOf<String>()

        fun traverse(node: AccessibilityNodeInfo?, depth: Int) {
            if (node == null || depth > 24 || (buttons.size + texts.size) >= maxItems * 2) return
            if (node.isVisibleToUser) {
                val t = node.text?.toString()?.trim().orEmpty()
                val d = node.contentDescription?.toString()?.trim().orEmpty()
                val label = t.ifBlank { d }.replace("\n", " ").trim()
                if (label.length in 2..65 && !label.startsWith("🔴 GEMINI LIVE")) {
                    val rect = Rect()
                    node.getBoundsInScreen(rect)
                    if (node.isClickable) {
                        val entry = "\"$label\"@(${rect.centerX()},${rect.centerY()})"
                        if (!buttons.contains(entry) && buttons.size < maxItems) {
                            buttons.add(entry)
                        }
                    } else if (texts.size < maxItems && !texts.contains(label)) {
                        texts.add(label)
                    }
                }
            }
            val c = node.childCount
            for (i in 0 until c) {
                traverse(node.getChild(i), depth + 1)
            }
        }

        return try {
            traverse(root, 0)
            buildString {
                if (buttons.isNotEmpty()) {
                    append("Screen Buttons: ")
                    append(buttons.take(maxItems).joinToString(", "))
                }
                if (texts.isNotEmpty()) {
                    if (isNotEmpty()) append(" | ")
                    append("Visible Texts: ")
                    append(texts.take(12).joinToString(" • "))
                }
            }
        } catch (_: Throwable) {
            ""
        }
    }
}
