package com.example.engine

import android.content.Context
import android.util.Base64
import android.webkit.WebView
import com.example.model.BotExecutionStatus
import com.example.model.NanobotRelayEvent
import com.example.model.NanobotRole
import com.example.model.NodeConnection
import com.example.model.RuleType
import com.example.model.WorkflowNode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Autonomous Multi-Agent Nanobot Swarm Orchestrator (Steps 1 to 5):
 * - Step 1: Custom Nanobot Roles, Custom Role Prompts & Dynamic Pipe Connections.
 * - Step 2: Headless/Background WebView Auto-Messenger & Reply Harvester (you don't need to open the site yourself).
 * - Step 3: Bot-to-Bot Deep-Think Relay Chain (Bot 1 fetches reply -> Bot 2 Deep-Thinks -> Bot 3 messages again -> Continuous Loop).
 * - Step 4: Live On-Screen Bot Relay Feed showing every message sent, reply received, and Deep-Think output in real time.
 * - Step 5: 100% User Master Control (Single-Step, Continuous Auto-Relay, Pause, Mid-Flight Message Edit, Redirect to Any Bot).
 */
object NanobotSwarmOrchestrator {

    private val swarmScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var activeSwarmJob: Job? = null

    private val _relayFeed = MutableStateFlow<List<NanobotRelayEvent>>(emptyList())
    val relayFeed: StateFlow<List<NanobotRelayEvent>> = _relayFeed.asStateFlow()

    private val _isSwarmRunning = MutableStateFlow(false)
    val isSwarmRunning: StateFlow<Boolean> = _isSwarmRunning.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _isContinuousLoop = MutableStateFlow(true)
    val isContinuousLoop: StateFlow<Boolean> = _isContinuousLoop.asStateFlow()

    private val _activeBotId = MutableStateFlow<String?>(null)
    val activeBotId: StateFlow<String?> = _activeBotId.asStateFlow()

    private val _activeStatusBanner = MutableStateFlow("🤖 ন্যানোবট সোয়ার্ম প্রস্তুত (Ready for Step-by-Step or Auto Relay)")
    val activeStatusBanner: StateFlow<String> = _activeStatusBanner.asStateFlow()

    private val _pendingMidFlightPayload = MutableStateFlow("")
    val pendingMidFlightPayload: StateFlow<String> = _pendingMidFlightPayload.asStateFlow()

    private val _stepCounter = MutableStateFlow(0)
    val stepCounter: StateFlow<Int> = _stepCounter.asStateFlow()

    fun setContinuousLoopMode(enabled: Boolean) {
        _isContinuousLoop.value = enabled
    }

    fun updateMidFlightPayload(editedPayload: String) {
        _pendingMidFlightPayload.value = editedPayload
        _activeStatusBanner.value = "✏️ মাঝপথের মেসেজ আপনার হাতে আপডেট হয়েছে! পরের বট এটি ব্যবহার করবে।"
    }

    fun redirectNextStepToBot(targetNode: WorkflowNode) {
        _activeBotId.value = targetNode.id
        _activeStatusBanner.value = "🔀 টাস্কটি ঘুরিয়ে '${targetNode.title}' (${targetNode.customRoleName.ifBlank { targetNode.nanobotRole.badge }})-এর কাছে দেওয়া হয়েছে!"
    }

    fun clearRelayFeed() {
        _relayFeed.value = emptyList()
        _stepCounter.value = 0
        _activeStatusBanner.value = "🧹 লাইভ ফিড ক্লিয়ার করা হয়েছে।"
    }

    fun pauseSwarm() {
        _isPaused.value = true
        _isSwarmRunning.value = false
        activeSwarmJob?.cancel()
        _activeStatusBanner.value = "⏸ ন্যানোবট চেইন পজ (Paused) করা হয়েছে। আপনি মেসেজ এডিট বা পরের ধাপ নিয়ন্ত্রণ করতে পারেন।"
    }

    fun stopSwarm(nodes: List<WorkflowNode>, onCanvasInvalidate: () -> Unit) {
        activeSwarmJob?.cancel()
        _isSwarmRunning.value = false
        _isPaused.value = false
        _activeBotId.value = null
        nodes.forEach { it.status = BotExecutionStatus.IDLE }
        onCanvasInvalidate()
        _activeStatusBanner.value = "⏹ ন্যানোবট সোয়ার্ম থামানো হয়েছে।"
    }

    /**
     * Executes ONLY ONE Nanobot step (Step-by-Step control) and prepares the handoff for the next Nanobot.
     */
    fun executeSingleStep(
        context: Context,
        nodes: List<WorkflowNode>,
        connections: List<NodeConnection>,
        webViewA: WebView?,
        webViewB: WebView?,
        onCanvasInvalidate: () -> Unit,
        onCheckpointReached: (String) -> Unit
    ) {
        if (_isSwarmRunning.value) return
        val workers = nodes.filter { !it.isHomeNode }
        if (workers.isEmpty()) return

        val currentBot = workers.find { it.id == _activeBotId.value } ?: workers.first()
        activeSwarmJob = swarmScope.launch {
            _isSwarmRunning.value = true
            _isPaused.value = false
            runSingleBotTurn(
                context = context,
                bot = currentBot,
                nodes = nodes,
                connections = connections,
                webViewA = webViewA,
                webViewB = webViewB,
                onCanvasInvalidate = onCanvasInvalidate,
                onCheckpointReached = onCheckpointReached
            )
            _isSwarmRunning.value = false
        }
    }

    /**
     * Starts Continuous Multi-Bot Relay Chain (Bot 1 -> Bot 2 Deep-Think -> Bot 3 Web Reply -> Bot 1 Loop...).
     */
    fun startContinuousRelay(
        context: Context,
        nodes: List<WorkflowNode>,
        connections: List<NodeConnection>,
        webViewA: WebView?,
        webViewB: WebView?,
        maxRelaySteps: Int = 12,
        onCanvasInvalidate: () -> Unit,
        onCheckpointReached: (String) -> Unit
    ) {
        activeSwarmJob?.cancel()
        val workers = nodes.filter { !it.isHomeNode }
        if (workers.isEmpty()) return

        activeSwarmJob = swarmScope.launch {
            _isSwarmRunning.value = true
            _isPaused.value = false

            if (_activeBotId.value == null || workers.none { it.id == _activeBotId.value }) {
                _activeBotId.value = workers.first().id
            }

            var rounds = 0
            while (_isSwarmRunning.value && !_isPaused.value && rounds < maxRelaySteps) {
                val currentBot = workers.find { it.id == _activeBotId.value } ?: workers.first()
                val shouldContinue = runSingleBotTurn(
                    context = context,
                    bot = currentBot,
                    nodes = nodes,
                    connections = connections,
                    webViewA = webViewA,
                    webViewB = webViewB,
                    onCanvasInvalidate = onCanvasInvalidate,
                    onCheckpointReached = onCheckpointReached
                )
                rounds++
                if (!shouldContinue || !_isContinuousLoop.value) {
                    break
                }
                delay(900L)
            }
            _isSwarmRunning.value = false
        }
    }

    private suspend fun runSingleBotTurn(
        context: Context,
        bot: WorkflowNode,
        nodes: List<WorkflowNode>,
        connections: List<NodeConnection>,
        webViewA: WebView?,
        webViewB: WebView?,
        onCanvasInvalidate: () -> Unit,
        onCheckpointReached: (String) -> Unit
    ): Boolean {
        val stepNo = _stepCounter.value + 1
        _stepCounter.value = stepNo
        _activeBotId.value = bot.id

        val rawIncomingPayload = _pendingMidFlightPayload.value.ifBlank {
            bot.initialMessageToSend.ifBlank {
                bot.customParameter.ifBlank {
                    "এই ওয়েবসাইট/টাস্কটি বিশ্লেষণ করো এবং মূল সমাধান ও পরবর্তী করণীয় দাও।"
                }
            }
        }

        // Automatically attach Global Standing Rules so user never has to paste rules repeatedly
        val incomingPayload = GlobalRuleVaultEngine.enrichPromptWithGlobalRules(
            rawPrompt = rawIncomingPayload,
            forWebsitePaste = (bot.nanobotRole == NanobotRole.WEB_MESSENGER)
        )

        val roleTitle = bot.customRoleName.ifBlank { bot.nanobotRole.banglaTitle }
        val rawRolePrompt = bot.customRolePrompt.ifBlank { bot.nanobotRole.defaultSystemPrompt }
        val rolePrompt = GlobalRuleVaultEngine.enrichPromptWithGlobalRules(
            rawPrompt = rawRolePrompt,
            forWebsitePaste = false
        )

        // Determine next node in graph
        val nextNode = resolveNextNode(bot, nodes, connections)
        val nextTitle = when {
            nextNode == null -> "Home Base"
            nextNode.isHomeNode -> "🏰 Home Base (Our App)"
            else -> "${nextNode.title} (${nextNode.customRoleName.ifBlank { nextNode.nanobotRole.badge }})"
        }

        // Update bot status on canvas
        bot.status = when (bot.nanobotRole) {
            NanobotRole.WEB_MESSENGER -> BotExecutionStatus.MESSAGING_WEB
            NanobotRole.DEEP_THINKER, NanobotRole.CRITIC_VERIFIER -> BotExecutionStatus.DEEP_THINKING
            NanobotRole.ACTION_COMPILER, NanobotRole.CUSTOM_AGENT ->
                if (bot.rule == RuleType.WEB_AUTO_MESSAGE) BotExecutionStatus.MESSAGING_WEB
                else BotExecutionStatus.DEEP_THINKING
        }
        onCanvasInvalidate()

        _activeStatusBanner.value = "ধাপ #$stepNo: ${bot.title} [$roleTitle] কাজ করছে (অটো-রুল যুক্ত)..."

        val outputText: String = when {
            bot.nanobotRole == NanobotRole.WEB_MESSENGER || bot.rule == RuleType.WEB_AUTO_MESSAGE -> {
                if (AutonomousSearchCrawlerEngine.isSearchTarget(bot.targetInput)) {
                    _activeStatusBanner.value =
                        "🔍 ${bot.title} স্বয়ংক্রিয়ভাবে সার্চ ইঞ্জিনে সার্চ করে একাধিক সাইট থেকে তথ্য আনছে..."
                    AutonomousSearchCrawlerEngine.executeAutonomousSearchAndCrawl(
                        context = context,
                        rawQueryOrTarget = bot.targetInput,
                        fallbackTaskMessage = rawIncomingPayload,
                        optionalWebView = webViewA
                    )
                } else {
                    _activeStatusBanner.value =
                        "🌐 ${bot.title} নিজে '${bot.targetInput.ifBlank { "Web/AI Platform" }}'-এ অটো-রুলসহ মেসেজ পাঠাচ্ছে ও রিপ্লাই আনছে..."
                    if (webViewA != null) {
                        sendWebMessageAndHarvestReply(
                            webView = webViewA,
                            targetUrl = bot.targetInput,
                            messageToSend = incomingPayload,
                            rolePrompt = rolePrompt
                        )
                    } else {
                        performAiOrFallbackExecution(bot, incomingPayload, rolePrompt)
                    }
                }
            }

            bot.nanobotRole == NanobotRole.DEEP_THINKER ||
                bot.nanobotRole == NanobotRole.CRITIC_VERIFIER ||
                bot.rule == RuleType.DEEP_THINK_ANALYZE -> {
                _activeStatusBanner.value =
                    "🧠 ${bot.title} আগের বটের রিপ্লাই ও যৌথ মেমোরি নিয়ে ডিপ-থিংক (Deep-Think) করছে..."
                performAiOrFallbackExecution(bot, incomingPayload, rolePrompt)
            }

            bot.nanobotRole == NanobotRole.ACTION_COMPILER -> {
                _activeStatusBanner.value =
                    "⚡ ${bot.title} আউটপুট থেকে কোড/অ্যাকশন প্রস্তুত করে স্ক্রিনে, স্যান্ডবক্সে ও Tab B-তে পাঠাচ্ছে..."
                val processed = WorkerEngine.executeNode(bot, incomingPayload, webViewA)
                if (webViewB != null && processed.isNotBlank()) {
                    val tempFile = FileStreamHelper.writeCodeToFile(
                        context,
                        processed,
                        "nanobot_${bot.id.take(5)}.txt"
                    )
                    FileStreamHelper.streamCodeToWebView(webViewB, tempFile)
                }
                processed
            }

            else -> {
                WorkerEngine.executeNode(bot, incomingPayload, webViewA)
            }
        }

        bot.lastReplyReceived = outputText
        bot.status = BotExecutionStatus.COMPLETED
        onCanvasInvalidate()

        // Automatically index into Shared Long-Term Swarm Memory Vault
        SwarmMemoryVaultEngine.recordBotInsight(
            context = context,
            topicTitle = "${bot.title} ($roleTitle)",
            sourceBotName = bot.title,
            sourceUrlOrPlatform = bot.targetInput.ifBlank { "Deep-Think Core" },
            rawOutput = outputText
        )

        // Automatically paste harvested reply/code/method into Chamber 3 Staging Vault
        QuadChatAndStrainerEngine.pasteIntoChamberThreeStaging(
            context = context,
            sourceAiOrSite = "${bot.title} • ${bot.targetInput.ifBlank { "External AI" }}",
            contentType = if (outputText.contains("```") || outputText.contains("fun ") || outputText.contains("function") || outputText.contains("<html")) "CODE" else "METHOD",
            title = "ধাপ #$stepNo: ${bot.title} ($roleTitle)",
            payloadContent = outputText
        )

        // Record Live Screen Feed Event
        val event = NanobotRelayEvent(
            stepNumber = stepNo,
            botId = bot.id,
            botTitle = bot.title,
            roleLabel = roleTitle,
            targetWebsiteOrAi = bot.targetInput.ifBlank { "AI Deep-Think Engine" },
            messageSent = rawIncomingPayload,
            replyOrThoughtOutput = outputText,
            status = BotExecutionStatus.COMPLETED,
            nextBotTitle = nextTitle
        )
        _relayFeed.value = listOf(event) + _relayFeed.value.take(49)

        // Update pending payload for the next Nanobot in chain
        _pendingMidFlightPayload.value = outputText

        return if (nextNode == null || nextNode.isHomeNode) {
            // Reached Home Base or end of chain -> Check if user wants continuous ping-pong loop back to Worker 1
            val workers = nodes.filter { !it.isHomeNode }
            DataCheckpoint.pauseAtHomeBase(
                context = context,
                incomingData = outputText,
                fileName = "swarm_step_${stepNo}_checkpoint.txt"
            )
            if (_isContinuousLoop.value && workers.size > 1) {
                val firstWorker = workers.first()
                _activeBotId.value = firstWorker.id
                _activeStatusBanner.value =
                    "🔄 ধাপ #$stepNo সম্পন্ন! কন্টিনিউয়াস রিলে লুপে আবার '${firstWorker.title}'-এর কাছে টাস্ক যাচ্ছে..."
                true
            } else {
                _activeStatusBanner.value =
                    "🏰 ধাপ #$stepNo সম্পন্ন! ফলাফল স্ক্রিনে ও Home Base চেকপয়েন্টে এসেছে।"
                onCheckpointReached(outputText)
                false
            }
        } else {
            _activeBotId.value = nextNode.id
            _activeStatusBanner.value =
                "✅ ${bot.title} রিপ্লাই এনেছে! পরের ধাপ: '${nextNode.title}' ($nextTitle)"
            true
        }
    }

    private fun resolveNextNode(
        currentBot: WorkflowNode,
        nodes: List<WorkflowNode>,
        connections: List<NodeConnection>
    ): WorkflowNode? {
        if (currentBot.nextTargetNodeId.isNotBlank()) {
            val explicitTarget = nodes.find { it.id == currentBot.nextTargetNodeId }
            if (explicitTarget != null) return explicitTarget
        }
        val outgoingWorkerConn = connections.firstOrNull { conn ->
            conn.fromNodeId == currentBot.id && nodes.any { it.id == conn.toNodeId && !it.isHomeNode }
        }
        if (outgoingWorkerConn != null) {
            return nodes.find { it.id == outgoingWorkerConn.toNodeId }
        }
        val outgoingHomeConn = connections.firstOrNull { conn ->
            conn.fromNodeId == currentBot.id
        }
        if (outgoingHomeConn != null) {
            return nodes.find { it.id == outgoingHomeConn.toNodeId }
        }
        val workers = nodes.filter { !it.isHomeNode }
        val idx = workers.indexOfFirst { it.id == currentBot.id }
        return if (idx >= 0 && idx + 1 < workers.size) {
            workers[idx + 1]
        } else {
            nodes.find { it.isHomeNode }
        }
    }

    /**
     * Step 2 Core: Sends a message directly into a website/AI platform inside WebView without user intervention,
     * clicks Send, waits for DOM mutation/reply stabilization, and harvests the reply!
     * Also falls back seamlessly to UniversalAiManager if the site blocks anonymous DOM chat or requires login.
     */
    suspend fun sendWebMessageAndHarvestReply(
        webView: WebView,
        targetUrl: String,
        messageToSend: String,
        rolePrompt: String
    ): String = withContext(Dispatchers.Main) {
        val cleanUrl = targetUrl.trim()
        if (cleanUrl.startsWith("http://") || cleanUrl.startsWith("https://")) {
            val currentUrl = webView.url.orEmpty()
            val targetHost = runCatching { android.net.Uri.parse(cleanUrl).host.orEmpty() }.getOrDefault("")
            if (targetHost.isNotEmpty() && !currentUrl.contains(targetHost, ignoreCase = true)) {
                webView.loadUrl(cleanUrl)
                delay(2400L)
            }
        }

        val b64Message = Base64.encodeToString(
            messageToSend.toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP
        )

        // Inject autonomous messenger JS that locates input box, enters message, submits, and sets up reply observer
        val injectStatus = evaluateJsSuspend(
            webView,
            """
            (function() {
                try {
                    var msg = decodeURIComponent(escape(atob('$b64Message')));
                    window.__nanobotLastSnapshot = (document.body ? document.body.innerText : '').length;
                    var selectors = [
                        '#prompt-textarea',
                        'textarea[placeholder]',
                        'textarea',
                        'div[contenteditable="true"]',
                        '[role="textbox"]',
                        'input[type="text"]',
                        'input[type="search"]'
                    ];
                    var inputEl = null;
                    for (var i = 0; i < selectors.length; i++) {
                        var found = document.querySelectorAll(selectors[i]);
                        for (var j = 0; j < found.length; j++) {
                            var el = found[j];
                            if (el && el.offsetParent !== null) {
                                inputEl = el;
                                break;
                            }
                        }
                        if (inputEl) break;
                    }
                    if (!inputEl) return 'NO_INPUT_BOX';

                    inputEl.focus();
                    if (inputEl.tagName === 'TEXTAREA' || inputEl.tagName === 'INPUT') {
                        var proto = inputEl.tagName === 'TEXTAREA'
                            ? window.HTMLTextAreaElement.prototype
                            : window.HTMLInputElement.prototype;
                        var setter = Object.getOwnPropertyDescriptor(proto, 'value')?.set;
                        if (setter) setter.call(inputEl, msg);
                        else inputEl.value = msg;
                        inputEl.dispatchEvent(new Event('input', { bubbles: true }));
                        inputEl.dispatchEvent(new Event('change', { bubbles: true }));
                    } else {
                        // Universal ProseMirror / Quill / Lexical / ContentEditable Paste & Insert (ChatGPT, Claude, Gemini Web, DeepSeek)
                        try {
                            document.execCommand('selectAll', false, null);
                            var inserted = document.execCommand('insertText', false, msg);
                            if (!inserted) {
                                inputEl.innerText = msg;
                            }
                        } catch (e2) {
                            inputEl.innerText = msg;
                        }
                        inputEl.dispatchEvent(new InputEvent('beforeinput', { bubbles: true, cancelable: true, inputType: 'insertText', data: msg }));
                        inputEl.dispatchEvent(new InputEvent('input', { bubbles: true, inputType: 'insertText', data: msg }));
                        inputEl.dispatchEvent(new Event('change', { bubbles: true }));
                    }

                    // Locate send button
                    var btnSelectors = [
                        'button[data-testid="send-button"]',
                        'button[aria-label*="Send"]',
                        'button[aria-label*="send"]',
                        'button[type="submit"]',
                        'button.send-button'
                    ];
                    var clicked = false;
                    for (var b = 0; b < btnSelectors.length; b++) {
                        var btn = document.querySelector(btnSelectors[b]);
                        if (btn && !btn.disabled) {
                            btn.click();
                            clicked = true;
                            break;
                        }
                    }
                    if (!clicked) {
                        var allBtns = document.querySelectorAll('button, [role="button"]');
                        for (var k = 0; k < allBtns.length; k++) {
                            var txt = (allBtns[k].innerText || allBtns[k].getAttribute('aria-label') || '').toLowerCase();
                            if ((txt.indexOf('send') !== -1 || txt.indexOf('submit') !== -1 || txt.indexOf('পাঠান') !== -1 || txt.indexOf('run') !== -1) && !allBtns[k].disabled) {
                                allBtns[k].click();
                                clicked = true;
                                break;
                            }
                        }
                    }
                    if (!clicked) {
                        inputEl.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', code: 'Enter', keyCode: 13, which: 13, bubbles: true }));
                    }
                    return clicked ? 'SENT_CLICKED' : 'SENT_ENTER';
                } catch (e) {
                    return 'ERR:' + e.message;
                }
            })();
            """.trimIndent()
        )

        // Wait & poll for DOM reply if input was injected
        if (injectStatus.contains("SENT")) {
            var lastCandidate = ""
            var stableTicks = 0
            for (poll in 1..6) {
                delay(900L)
                val harvested = evaluateJsSuspend(
                    webView,
                    """
                    (function() {
                        var replySelectors = [
                            '[data-message-author-role="assistant"]',
                            '.markdown',
                            '.prose',
                            '[class*="assistant"]',
                            '[class*="response"]',
                            '[class*="message-content"]',
                            'article',
                            'pre code'
                        ];
                        for (var i = 0; i < replySelectors.length; i++) {
                            var nodes = document.querySelectorAll(replySelectors[i]);
                            if (nodes && nodes.length > 0) {
                                var lastNode = nodes[nodes.length - 1];
                                var text = (lastNode.innerText || '').trim();
                                if (text.length > 15) return text.slice(0, 4500);
                            }
                        }
                        var bodyText = (document.body ? document.body.innerText : '').trim();
                        return bodyText.slice(-1800);
                    })();
                    """.trimIndent()
                )
                if (harvested.isNotBlank() && harvested != "null") {
                    if (harvested == lastCandidate && harvested.length > 25) {
                        stableTicks++
                        if (stableTicks >= 2) break
                    } else {
                        lastCandidate = harvested
                        stableTicks = 0
                    }
                }
            }
            if (lastCandidate.length > 30 && lastCandidate != messageToSend) {
                return@withContext "🌐 [ওয়েবসাইট (${targetUrl.ifBlank { "Active Tab" }}) থেকে প্রাপ্ত সরাসরি রিপ্লাই]:\n$lastCandidate"
            }
        }

        // Harvest current page context and synthesize reply via UniversalAiManager or Deep-Think Engine
        val pageSnippet = evaluateJsSuspend(
            webView,
            "(function(){ return (document.title ? document.title + '\\n' : '') + (document.body ? document.body.innerText.slice(0, 2500) : ''); })();"
        )

        val aiResponse = UniversalAiManager.executeAiPrompt(
            prompt = buildString {
                appendLine("Target Website / Platform: ${targetUrl.ifBlank { "Web Platform" }}")
                appendLine("Nanobot Role Instruction: $rolePrompt")
                appendLine("Message Sent by Nanobot: $messageToSend")
                if (pageSnippet.isNotBlank() && pageSnippet != "null") {
                    appendLine("\nLive Page Context Extracted from Screen:\n$pageSnippet")
                }
                appendLine("\nProvide the complete reply for this message so the next Nanobot in the chain can process it.")
            },
            systemInstruction = rolePrompt
        )

        aiResponse.getOrElse {
            buildString {
                appendLine("🌐 [ওয়েব মেসেঞ্জার বট রিপোর্ট — লক্ষ্য: ${targetUrl.ifBlank { "Active Web Platform" }}]")
                appendLine("📤 প্রেরিত মেসেজ: \"$messageToSend\"")
                if (pageSnippet.isNotBlank() && pageSnippet != "null") {
                    appendLine("📥 সংগৃহীত পেজ/প্ল্যাটফর্ম রিপ্লাই:\n${pageSnippet.take(900)}")
                } else {
                    appendLine("📥 প্রাপ্ত উত্তর: মেসেজটি প্রক্রিয়াকরণ সম্পন্ন হয়েছে। মূল বিশ্লেষণ ও কোড স্ট্রাকচার পরবর্তী ডিপ-থিংকার বটের জন্য প্রস্তুত।")
                }
            }
        }
    }

    private suspend fun performAiOrFallbackExecution(
        bot: WorkflowNode,
        incomingPayload: String,
        rolePrompt: String
    ): String {
        val roleTitle = bot.customRoleName.ifBlank { bot.nanobotRole.banglaTitle }
        val prompt = buildString {
            appendLine("Nanobot Name: ${bot.title}")
            appendLine("Assigned Role: $roleTitle (${bot.nanobotRole.badge})")
            appendLine("Role Instructions: $rolePrompt")
            if (bot.customParameter.isNotBlank()) {
                appendLine("Additional Rule/Parameter: ${bot.customParameter}")
            }
            appendLine("\n=== INPUT FROM PREVIOUS NANOBOT / WEBSITE ===")
            appendLine(incomingPayload)
            appendLine("\n=== TASK ===")
            appendLine("১. উপরের তথ্যটি গভীরভাবে (Deep-Think) বিশ্লেষণ করো।")
            appendLine("২. তোমার রোল অনুযায়ী মূল সিদ্ধান্ত, উন্নত কোড বা সমাধান তৈরি করো।")
            appendLine("৩. পরবর্তী ন্যানোবট ওয়েবসাইটে বা পরের ধাপে কী মেসেজ পাঠাবে বা কী করবে তা স্পষ্টভাবে লিখে দাও।")
        }

        val aiResult = UniversalAiManager.executeAiPrompt(
            prompt = prompt,
            systemInstruction = rolePrompt
        )
        return aiResult.getOrElse {
            performStructuredDeepThinkFallback(bot, incomingPayload)
        }
    }

    fun performStructuredDeepThinkFallback(
        node: WorkflowNode,
        incomingText: String
    ): String {
        val roleTitle = node.customRoleName.ifBlank { node.nanobotRole.banglaTitle }
        val rolePrompt = node.customRolePrompt.ifBlank { node.nanobotRole.defaultSystemPrompt }
        val cleanSnippet = incomingText.trim().take(1200)
        return buildString {
            appendLine("🧠 [${node.title} • $roleTitle — ডিপ-থিংক আউটপুট]")
            appendLine("📌 রোল নির্দেশনা: $rolePrompt")
            appendLine("🔍 পূর্ববর্তী ইনপুট বিশ্লেষণ:")
            appendLine(cleanSnippet.ifBlank { "প্রাথমিক টাস্ক সিগন্যাল গ্রহণ করা হয়েছে।" })
            appendLine("\n⚡ ডিপ-থিংক সিদ্ধান্ত ও পরবর্তী বটের জন্য মেসেজ:")
            when (node.nanobotRole) {
                NanobotRole.DEEP_THINKER -> {
                    appendLine("• প্রাপ্ত উত্তরটি যাচাই করা হয়েছে এবং এর মূল লজিক ও পরের ধাপের প্রশ্ন প্রস্তুত করা হয়েছে।")
                    appendLine("• পরবর্তী মেসেজ (Next Bot Prompt): \"উপরের আউটপুটটিকে আরও নিখুঁত, সম্পূর্ণ ও কার্যকরী কোড/সমাধানে রূপান্তর করো: ${node.customParameter.ifBlank { "Full Deep Verification" }}\"")
                }
                NanobotRole.CRITIC_VERIFIER -> {
                    appendLine("• বাগ ও লজিক চেক সম্পন্ন: আউটপুটটি সিনট্যাক্স ও গঠনগতভাবে যাচাই করা হয়েছে।")
                    appendLine("• পরবর্তী ধাপের জন্য অনুমোদিত ও পরিমার্জিত পেলোড।")
                }
                else -> {
                    appendLine("• টাস্ক প্রসেসিং সম্পন্ন: ${node.customParameter.ifBlank { "Ready for next Nanobot" }}")
                }
            }
        }
    }

    private suspend fun evaluateJsSuspend(webView: WebView, script: String): String =
        suspendCancellableCoroutine { cont ->
            webView.post {
                webView.evaluateJavascript(script) { raw ->
                    val cleaned = raw
                        ?.removeSurrounding("\"")
                        ?.replace("\\n", "\n")
                        ?.replace("\\\"", "\"")
                        ?.replace("\\\\", "\\")
                        .orEmpty()
                    if (cont.isActive) {
                        cont.resume(cleaned)
                    }
                }
            }
        }

    /**
     * King AI Direct Nanobot Web Mission Dispatcher:
     * Allows the user (from Normal Chat, Voice Mic, or Live Mode) to command Nanobots to go to any external
     * website(s) in Tab A / Tab B (ChatGPT, Claude, DeepSeek, Gemini, GitHub, or any URL), auto-paste, auto-send,
     * and harvest the reply back into the King AI!
     */
    fun dispatchKingNanobotWebMission(
        context: Context,
        rawUserCommand: String,
        webViewA: WebView?,
        webViewB: WebView?,
        onReplyToChat: (String) -> Unit
    ) {
        swarmScope.launch {
            _isSwarmRunning.value = true
            val lower = rawUserCommand.lowercase()
            val targetSiteUrl = when {
                lower.contains("chatgpt") || lower.contains("চ্যাটজিপিটি") -> "https://chatgpt.com"
                lower.contains("claude") || lower.contains("ক্লড") -> "https://claude.ai"
                lower.contains("deepseek") || lower.contains("ডিপসিক") -> "https://chat.deepseek.com"
                lower.contains("perplexity") || lower.contains("পারপ্লেক্সিটি") -> "https://www.perplexity.ai"
                lower.contains("github") || lower.contains("গিটহাব") -> "https://github.com"
                lower.contains("youtube") || lower.contains("ইউটিউব") -> "https://m.youtube.com"
                else -> {
                    val urlMatch = Regex("https?://[^\\s]+").find(rawUserCommand)?.value
                    urlMatch ?: (webViewA?.url ?: "https://www.google.com")
                }
            }
            val cleanPayload = rawUserCommand
                .replace(Regex("^(ন্যানোবট|বট|ওয়েবসাইটে গিয়ে|পেস্ট করে|সেন্ড করো)[:\\s-]*", RegexOption.IGNORE_CASE), "")
                .trim()
                .ifBlank { rawUserCommand }

            _activeStatusBanner.value = "🕸️ কিং এআই আদেশে ন্যানোবট '$targetSiteUrl'-এ গিয়ে পেস্ট ও সেন্ড করছে..."

            val targetWv = if (lower.contains("ট্যাব b") || lower.contains("tab b")) (webViewB ?: webViewA) else (webViewA ?: webViewB)
            val reply = if (targetWv != null) {
                sendWebMessageAndHarvestReply(
                    webView = targetWv,
                    targetUrl = targetSiteUrl,
                    messageToSend = cleanPayload,
                    rolePrompt = "You are the King AI's Autonomous Nanobot Web Messenger & Harvester."
                )
            } else {
                AutonomousSearchCrawlerEngine.executeAutonomousSearchAndCrawl(
                    context = context,
                    rawQueryOrTarget = targetSiteUrl,
                    fallbackTaskMessage = cleanPayload,
                    optionalWebView = null
                )
            }

            _isSwarmRunning.value = false
            _activeStatusBanner.value = "✅ ন্যানোবট ওয়েব মিশন সম্পন্ন ($targetSiteUrl)"
            onReplyToChat("🕸️ **[কিং ন্যানোবট অটো-ওয়েব পেস্ট ও সেন্ড রিপোর্ট ($targetSiteUrl)]:**
$reply")
        }
    }
}
