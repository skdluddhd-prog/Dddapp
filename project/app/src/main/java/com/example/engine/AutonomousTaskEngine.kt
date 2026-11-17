package com.example.engine

import android.content.Context
import android.webkit.ConsoleMessage
import android.webkit.WebView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    SUCCESS,
    SKIPPED
}

data class ChecklistTask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val targetGoal: GoalType = GoalType.RUN_CODE,
    var status: TaskStatus = TaskStatus.PENDING,
    var retryCount: Int = 0,
    var lastError: String = "",
    var blueprintSignatures: List<String> = emptyList()
)

class AutonomousTaskEngine(private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var verificationJob: Job? = null
    private var isAwaitingExecutionResult = false
    private var lastInjectedCode = ""

    var onGodModeSwitchRequested: (() -> Unit)? = null

    private val _tasks = MutableStateFlow(
        listOf(
            ChecklistTask(
                title = "১. ওয়েব মেসেঞ্জার ও কোড এক্সট্রাকশন (Bot 1)",
                description = "টার্গেট ওয়েবসাইট বা AI প্ল্যাটফর্মে মেসেজ পাঠিয়ে উত্তর ও সোর্স কোড সংগ্রহ",
                targetGoal = GoalType.RUN_CODE
            ),
            ChecklistTask(
                title = "২. ডিপ-থিংক লজিক ভেরিফিকেশন (Bot 2)",
                description = "প্রাপ্ত রিপ্লাই ডিপ-থিংক করে বিশ্লেষণ ও পরবর্তী নির্দেশনা তৈরি",
                targetGoal = GoalType.CUSTOM
            ),
            ChecklistTask(
                title = "৩. অটো কম্পাইল ও সেলফ-হিলিং (Bot 3 -> Tab B)",
                description = "Tab B-তে কোড ইনজেক্ট, রান এবং ৩-স্ট্রাইক লুপ ব্রেকার যাচাই",
                targetGoal = GoalType.RUN_CODE
            )
        )
    )
    val tasks: StateFlow<List<ChecklistTask>> = _tasks.asStateFlow()

    private val _currentActiveTaskIndex = MutableStateFlow(0)
    val currentActiveTaskIndex: StateFlow<Int> = _currentActiveTaskIndex.asStateFlow()

    private val _engineLog = MutableStateFlow("Autonomous 3-Strike Engine Ready.")
    val engineLog: StateFlow<String> = _engineLog.asStateFlow()

    fun getCurrentTask(): ChecklistTask? {
        val list = _tasks.value
        val idx = _currentActiveTaskIndex.value
        return list.getOrNull(idx)
    }

    private fun notifyTasksChanged() {
        _tasks.value = _tasks.value.map { it.copy() }
    }

    private fun flushRawCodeFromRAM() {
        lastInjectedCode = ""
        System.gc()
    }

    private fun extractBlueprintSignatures(code: String): List<String> {
        val regex = Regex("(?:function|class|const|let|var|fun)\\s+([a-zA-Z0-9_]+)")
        return regex.findAll(code).map { it.groupValues[1] }.distinct().take(12).toList()
    }

    fun onCodeInjectedToTabB(code: String, webViewA: WebView, webViewB: WebView) {
        val task = getCurrentTask() ?: return
        task.status = TaskStatus.IN_PROGRESS
        lastInjectedCode = code
        isAwaitingExecutionResult = true
        notifyTasksChanged()
        _engineLog.value = "⚡ Task '${task.title}' injected to Tab B. Verifying..."

        verificationJob?.cancel()
        verificationJob = scope.launch {
            delay(3200L)
            if (isAwaitingExecutionResult) {
                onCompilationSuccess(code)
            }
        }
    }

    fun onTabBConsoleMessage(
        consoleMessage: ConsoleMessage,
        webViewA: WebView,
        webViewB: WebView
    ) {
        if (consoleMessage.messageLevel() == ConsoleMessage.MessageLevel.ERROR) {
            handleCompilationError(
                errorMsg = "${consoleMessage.message()} (line ${consoleMessage.lineNumber()})",
                webViewA = webViewA,
                webViewB = webViewB
            )
        }
    }

    fun handleCompilationError(errorMsg: String, webViewA: WebView, webViewB: WebView) {
        val task = getCurrentTask() ?: return
        verificationJob?.cancel()
        isAwaitingExecutionResult = false
        task.retryCount += 1
        task.lastError = errorMsg

        if (task.retryCount >= 3) {
            task.status = TaskStatus.SKIPPED
            notifyTasksChanged()
            _engineLog.value = "⭕ Loop Breaker: 3 strikes -> Skip & hand over to God Mode."
            flushRawCodeFromRAM()
            onGodModeSwitchRequested?.invoke()
            moveToNextTask()
        } else {
            notifyTasksChanged()
            _engineLog.value = "🔧 Self-Healing Attempt ${task.retryCount}/3: $errorMsg"
            performAiSelfHealing(task, lastInjectedCode, errorMsg, webViewA)
        }
    }

    private fun performAiSelfHealing(
        task: ChecklistTask,
        brokenCode: String,
        errorMsg: String,
        webViewA: WebView
    ) {
        val healPrompt = "Fix compilation error ($errorMsg) in task '${task.title}':\n$brokenCode"
        scope.launch {
            val file = FileStreamHelper.writeCodeToFile(context, healPrompt, "self_heal_prompt.txt")
            FileStreamHelper.streamCodeToWebView(webViewA, file)
            GoalHeuristics.executeGoalAction(webViewA, GoalType.CUSTOM)
        }
    }

    fun onCompilationSuccess(verifiedCode: String) {
        val task = getCurrentTask() ?: return
        isAwaitingExecutionResult = false
        task.status = TaskStatus.SUCCESS
        task.blueprintSignatures = extractBlueprintSignatures(verifiedCode)
        notifyTasksChanged()
        _engineLog.value = "✅ Task '${task.title}' verified! Signatures saved: ${task.blueprintSignatures.joinToString()}"
        flushRawCodeFromRAM()
        moveToNextTask()
    }

    fun markCurrentTaskManualSuccess(signatures: List<String>) {
        val task = getCurrentTask() ?: return
        task.status = TaskStatus.SUCCESS
        task.blueprintSignatures = signatures
        notifyTasksChanged()
        _engineLog.value = "✅ Manual God-Mode Sync completed for '${task.title}'."
        moveToNextTask()
    }

    private fun moveToNextTask() {
        val next = _currentActiveTaskIndex.value + 1
        if (next < _tasks.value.size) {
            _currentActiveTaskIndex.value = next
        }
    }

    fun runAutonomousCrossTabLoop(
        webViewA: WebView,
        webViewB: WebView,
        onStatus: (String) -> Unit = {}
    ) {
        scope.launch {
            onStatus("🔍 Tab A থেকে সোর্স কোড ও রিপ্লাই স্ক্যান করা হচ্ছে...")
            AiChromiumDevEngine.extractCodeBlocks(webViewA) { extracted ->
                val cleaned = RegexSurgeon.extractCodeOnly(extracted)
                if (cleaned.isBlank()) {
                    onStatus("⚠️ ট্যাব A-তে কোড পাওয়া যায়নি")
                    return@extractCodeBlocks
                }
                scope.launch {
                    val file = FileStreamHelper.writeCodeToFile(context, cleaned, "auto_sync_tab_b.js")
                    FileStreamHelper.streamCodeToWebView(webViewB, file)
                    GoalHeuristics.executeGoalAction(webViewB, GoalType.RUN_CODE)
                    onCodeInjectedToTabB(cleaned, webViewA, webViewB)
                    onStatus("⚡ স্বয়ংক্রিয় সিঙ্ক ও রান সম্পন্ন!")
                }
            }
        }
    }
}
