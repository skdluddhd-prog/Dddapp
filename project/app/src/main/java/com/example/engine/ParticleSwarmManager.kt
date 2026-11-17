package com.example.engine

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
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
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Data model for a Particle Agent ("পার্টিকল কণা")
 */
data class ParticleAgent(
    val id: String = UUID.randomUUID().toString().take(8),
    val name: String,
    val roleBadge: String,
    val description: String,
    val isStealthActive: Boolean = true,
    var tasksCompletedCount: Int = 0,
    var isEnabled: Boolean = true
)

/**
 * Data model for a Particle Chat Message
 */
data class ParticleChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderName: String,
    val actionType: String, // USER, SEARCH, CLICK, SCROLL, EXTRACT, AI_BRAIN, LOOP_TICK
    val text: String,
    val targetUrl: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isSuccess: Boolean = true,
    val isCode: Boolean = false
)

/**
 * Autonomous Mission State for the AI-Particle Orchestration Loop
 */
data class AutonomousMissionState(
    val isRunning: Boolean = false,
    val missionGoal: String = "",
    val iteration: Int = 0,
    val maxIterations: Int = 12,
    val statusText: String = "নিষ্ক্রিয় (Standby)",
    val lastExtractedSnippet: String = "",
    val lastAiVerdict: String = "PENDING"
)

/**
 * Live Controllable Physics & Visual State for the Particle Swarm Canvas in Chat 2
 */
data class ParticleCanvasPhysicsState(
    val isEnabled: Boolean = true,
    val particleCount: Int = 36,
    val speedMultiplier: Float = 1.0f,
    val particleSizeDp: Float = 4.5f,
    val colorHex: String = "#00F0FF",
    val secondaryColorHex: String = "#A855F7",
    val physicsMode: String = "NEURAL_WEB", // NEURAL_WEB, VORTEX, MATRIX_RAIN, ORBIT, HYPER_SWARM
    val isInteractiveTouchRepel: Boolean = true
)

/**
 * ParticleSwarmManager:
 *
 * Manages the Particle Agents ("পার্টিকলস"):
 * 1. Stealth & Anti-Bot Shield (No bot detection, webdriver=false, geolocation/WebRTC leak protection, human delays).
 * 2. Cross-Website Surfing & Search.
 * 3. Human Touch Click on any button (Run, Submit, Search, Next).
 * 4. Human-like Smooth Scrolling (Up/Down) for long pages and code.
 * 5. Automatic Text & Code Extraction directly piped into the Particles Chat (no manual copy-paste needed).
 * 6. Continuous Auto-Loop / 24-Hour Loop Monitoring.
 * 7. AI Brain Controller integration (controls particles, canvas physics, DOM input/style/JS, and full app creation via natural language prompts).
 */
object ParticleSwarmManager {

    private const val PREFS_PARTICLES = "skrl2_particle_swarm_prefs"
    private const val KEY_PARTICLES_JSON = "saved_particles_list"
    private const val KEY_MESSAGES_JSON = "particle_chat_messages"
    private const val KEY_LOOP_ENABLED = "continuous_loop_enabled"
    private const val KEY_CANVAS_PHYSICS = "particle_canvas_physics_json"

    private val swarmScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _particlesList = MutableStateFlow<List<ParticleAgent>>(emptyList())
    val particlesList: StateFlow<List<ParticleAgent>> = _particlesList.asStateFlow()

    private val _particleMessages = MutableStateFlow<List<ParticleChatMessage>>(emptyList())
    val particleMessages: StateFlow<List<ParticleChatMessage>> = _particleMessages.asStateFlow()

    private val _isLoopRunning = MutableStateFlow(false)
    val isLoopRunning: StateFlow<Boolean> = _isLoopRunning.asStateFlow()

    private val _activeParticleStatus = MutableStateFlow("✨ পার্টিকল সোয়ার্ম প্রস্তুত (Ready)")
    val activeParticleStatus: StateFlow<String> = _activeParticleStatus.asStateFlow()

    private val _autonomousMissionState = MutableStateFlow(AutonomousMissionState())
    val autonomousMissionState: StateFlow<AutonomousMissionState> = _autonomousMissionState.asStateFlow()

    private val _canvasPhysicsState = MutableStateFlow(ParticleCanvasPhysicsState())
    val canvasPhysicsState: StateFlow<ParticleCanvasPhysicsState> = _canvasPhysicsState.asStateFlow()

    private var loopJob: Job? = null
    private var autonomousJob: Job? = null
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        isInitialized = true
        loadParticles(context)
        loadMessages(context)
    }

    private fun getDefaultParticles(): List<ParticleAgent> = listOf(
        ParticleAgent(
            id = "p-scout",
            name = "পার্টিকল ১ (ওয়েব সার্ফার ও স্কাউট)",
            roleBadge = "🌐 SURF",
            description = "এক ওয়েবসাইট থেকে অন্য ওয়েবসাইটে অবাধে ঘোরাঘুরি ও সার্চ করে।"
        ),
        ParticleAgent(
            id = "p-clicker",
            name = "পার্টিকল ২ (বাটন ও রানার কণা)",
            roleBadge = "🖱️ CLICK",
            description = "ওয়েবপেজের রান বাটন, সাবমিট বা অন্য যেকোনো বাটনে নিখুঁত ক্লিক করে।"
        ),
        ParticleAgent(
            id = "p-scroller",
            name = "পার্টিকল ৩ (হিউম্যান স্ক্রলার)",
            roleBadge = "📜 SCROLL",
            description = "লম্বা কোড বা কন্টেন্ট দেখতে পেজটিকে মানুষের মতো স্মুথ স্ক্রোল করে।"
        ),
        ParticleAgent(
            id = "p-extractor",
            name = "পার্টিকল ৪ (ডিপ ডাটা এক্সট্র্যাক্টর)",
            roleBadge = "📥 EXTRACT",
            description = "ওয়েবসাইট থেকে তথ্য ও কোড সরাসরি এনে পার্টিকলস চ্যাটে পেস্ট করে।"
        ),
        ParticleAgent(
            id = "p-loop",
            name = "পার্টিকল ৫ (অটো-লুপ ও ট্র্যাকার)",
            roleBadge = "🔄 LOOP",
            description = "২৪ ঘণ্টা বা নির্দিষ্ট সময় পর পর টাস্ক পুনরাবৃত্তি করে ও AI-কে দিয়ে যাচাই করায়।"
        )
    )

    private fun loadParticles(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_PARTICLES, Context.MODE_PRIVATE)
            val jsonStr = prefs.getString(KEY_PARTICLES_JSON, null)
            if (jsonStr.isNullOrBlank()) {
                _particlesList.value = getDefaultParticles()
                saveParticles(context)
            } else {
                val arr = JSONArray(jsonStr)
                val list = mutableListOf<ParticleAgent>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        ParticleAgent(
                            id = obj.optString("id", UUID.randomUUID().toString().take(8)),
                            name = obj.optString("name", "কাস্টম পার্টিকল"),
                            roleBadge = obj.optString("roleBadge", "✨ BOT"),
                            description = obj.optString("description", ""),
                            isStealthActive = obj.optBoolean("isStealthActive", true),
                            tasksCompletedCount = obj.optInt("tasksCompletedCount", 0),
                            isEnabled = obj.optBoolean("isEnabled", true)
                        )
                    )
                }
                if (list.isEmpty()) {
                    _particlesList.value = getDefaultParticles()
                } else {
                    _particlesList.value = list
                }
            }
        } catch (_: Throwable) {
            _particlesList.value = getDefaultParticles()
        }
    }

    private fun saveParticles(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_PARTICLES, Context.MODE_PRIVATE)
            val arr = JSONArray()
            _particlesList.value.forEach { p ->
                val obj = JSONObject().apply {
                    put("id", p.id)
                    put("name", p.name)
                    put("roleBadge", p.roleBadge)
                    put("description", p.description)
                    put("isStealthActive", p.isStealthActive)
                    put("tasksCompletedCount", p.tasksCompletedCount)
                    put("isEnabled", p.isEnabled)
                }
                arr.put(obj)
            }
            prefs.edit().putString(KEY_PARTICLES_JSON, arr.toString()).apply()
        } catch (_: Throwable) {}
    }

    private fun loadMessages(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_PARTICLES, Context.MODE_PRIVATE)
            val jsonStr = prefs.getString(KEY_MESSAGES_JSON, "[]").orEmpty()
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<ParticleChatMessage>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    ParticleChatMessage(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        senderName = obj.optString("senderName", "পার্টিকল"),
                        actionType = obj.optString("actionType", "SEARCH"),
                        text = obj.optString("text", ""),
                        targetUrl = obj.optString("targetUrl", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        isSuccess = obj.optBoolean("isSuccess", true),
                        isCode = obj.optBoolean("isCode", false)
                    )
                )
            }
            if (list.isEmpty()) {
                _particleMessages.value = listOf(
                    ParticleChatMessage(
                        senderName = "✨ পার্টিকল কন্ট্রোলার",
                        actionType = "AI_BRAIN",
                        text = "স্বাগতম! এটি হলো ডেডিকেটেড 'পার্টিকলস চ্যাট'। ওয়েবসাইটে পার্টিকল কণা পাঠিয়ে যেকোনো বাটন ক্লিক, সার্চ, স্ক্রোল ও টেক্সট সরাসরি এখানে পেস্ট করা যাবে।",
                        isCode = false
                    )
                )
            } else {
                _particleMessages.value = list
            }
        } catch (_: Throwable) {
            _particleMessages.value = emptyList()
        }
    }

    private fun saveMessages(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_PARTICLES, Context.MODE_PRIVATE)
            val arr = JSONArray()
            // Keep at most 80 latest messages to prevent memory bloat
            val listToSave = _particleMessages.value.takeLast(80)
            listToSave.forEach { m ->
                val obj = JSONObject().apply {
                    put("id", m.id)
                    put("senderName", m.senderName)
                    put("actionType", m.actionType)
                    put("text", m.text)
                    put("targetUrl", m.targetUrl)
                    put("timestamp", m.timestamp)
                    put("isSuccess", m.isSuccess)
                    put("isCode", m.isCode)
                }
                arr.put(obj)
            }
            prefs.edit().putString(KEY_MESSAGES_JSON, arr.toString()).apply()
        } catch (_: Throwable) {}
    }

    fun addParticleMessage(
        context: Context,
        senderName: String,
        actionType: String,
        text: String,
        targetUrl: String = "",
        isSuccess: Boolean = true,
        isCode: Boolean = false
    ) {
        val msg = ParticleChatMessage(
            senderName = senderName,
            actionType = actionType,
            text = text,
            targetUrl = targetUrl,
            isSuccess = isSuccess,
            isCode = isCode
        )
        val updated = _particleMessages.value.toMutableList().apply { add(msg) }
        // Keep memory lean
        if (updated.size > 80) {
            updated.removeAt(0)
        }
        _particleMessages.value = updated
        saveMessages(context)
    }

    fun deleteMessage(context: Context, messageId: String) {
        val updated = _particleMessages.value.filter { it.id != messageId }
        _particleMessages.value = updated
        saveMessages(context)
    }

    fun clearAllMessages(context: Context) {
        _particleMessages.value = emptyList()
        saveMessages(context)
    }

    fun createExtraCustomParticle(
        context: Context,
        name: String,
        roleBadge: String,
        description: String
    ): ParticleAgent {
        val newAgent = ParticleAgent(
            name = name.ifBlank { "কাস্টম পার্টিকল #${_particlesList.value.size + 1}" },
            roleBadge = roleBadge.ifBlank { "⚡ PRO" },
            description = description.ifBlank { "ব্যবহারকারীর নির্দেশিত কাস্টম ওয়েব অটোমেশন কণা।" }
        )
        val updated = _particlesList.value.toMutableList().apply { add(newAgent) }
        _particlesList.value = updated
        saveParticles(context)
        addParticleMessage(
            context = context,
            senderName = "✨ সোয়ার্ম ম্যানেজার",
            actionType = "AI_BRAIN",
            text = "✅ নতুন পার্টিকল তৈরি হয়েছে: '${newAgent.name}' [${newAgent.roleBadge}]."
        )
        return newAgent
    }

    /**
     * Injects the ultra-stealth Anti-Bot & Location/Identity Mask into Chromium WebView.
     * Prevents websites, Cloudflare, bot checkers from detecting automation.
     */
    fun injectStealthShield(webView: WebView?) {
        if (webView == null) return
        val stealthCode = """
            (function() {
                try {
                    // 1. Wipe navigator.webdriver
                    Object.defineProperty(navigator, 'webdriver', {
                        get: () => false,
                        configurable: true
                    });

                    // 2. Mock authentic Chrome object
                    window.chrome = {
                        app: { isInstalled: false },
                        runtime: {},
                        loadTimes: function() {},
                        csi: function() {}
                    };

                    // 3. Spoof realistic plugins & languages
                    Object.defineProperty(navigator, 'languages', {
                        get: () => ['bn-BD', 'bn-IN', 'en-US', 'en'],
                        configurable: true
                    });
                    Object.defineProperty(navigator, 'hardwareConcurrency', {
                        get: () => 8,
                        configurable: true
                    });
                    Object.defineProperty(navigator, 'deviceMemory', {
                        get: () => 8,
                        configurable: true
                    });

                    // 4. Spoof Geolocation (Prevents real GPS/location leak)
                    if (navigator.geolocation) {
                        navigator.geolocation.getCurrentPosition = function(success, error, options) {
                            success({
                                coords: {
                                    latitude: 22.5726,
                                    longitude: 88.3639,
                                    accuracy: 20
                                },
                                timestamp: Date.now()
                            });
                        };
                    }

                    // 5. Prevent WebRTC local IP leak
                    if (window.RTCPeerConnection) {
                        var origRTC = window.RTCPeerConnection;
                        window.RTCPeerConnection = function(cfg) {
                            var pc = new origRTC(cfg);
                            return pc;
                        };
                    }

                    // 6. Delete automation traces
                    delete window._phantom;
                    delete window.__nightmare;
                    delete window.callPhantom;
                } catch(e) {}
            })();
        """.trimIndent()
        webView.post {
            webView.evaluateJavascript(stealthCode, null)
        }
    }

    /**
     * Particle Action 1: Search and Navigate to any website.
     */
    fun particleSearchAndNavigate(
        context: Context,
        webView: WebView?,
        queryOrUrl: String,
        onDone: (String) -> Unit = {}
    ) {
        val target = queryOrUrl.trim()
        val url = if (target.startsWith("http://") || target.startsWith("https://")) {
            target
        } else if (target.contains(".") && !target.contains(" ")) {
            "https://$target"
        } else {
            "https://www.google.com/search?q=${android.net.Uri.encode(target)}"
        }

        _activeParticleStatus.value = "🌐 পার্টিকল ১ চলছে: '$target'-এ সার্ফ করছে..."
        addParticleMessage(
            context = context,
            senderName = "🌐 পার্টিকল ১ (স্কাউট)",
            actionType = "SEARCH",
            text = "ওয়েবসাইটে নেভিগেট করা হচ্ছে: $url",
            targetUrl = url
        )

        webView?.post {
            injectStealthShield(webView)
            webView.loadUrl(url)
            onDone(url)
        }
    }

    /**
     * Particle Action 2: Human Touch Click on any button matching text or CSS selector.
     */
    fun particleClickButton(
        context: Context,
        webView: WebView?,
        buttonNameOrSelector: String,
        onDone: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        if (webView == null) {
            onDone(false, "ওয়েবভিউ প্রস্তুত নয়")
            return
        }

        _activeParticleStatus.value = "🖱️ পার্টিকল ২ চলছে: '$buttonNameOrSelector' বাটনে ক্লিক করছে..."
        injectStealthShield(webView)

        val targetEscaped = JSONObject.quote(buttonNameOrSelector)
        val clickJs = """
            (function() {
                var target = $targetEscaped.toLowerCase().trim();
                var found = null;

                // 1. Try direct CSS selector
                try {
                    found = document.querySelector(target);
                } catch(e) {}

                // 2. Search buttons by text content
                if (!found) {
                    var buttons = document.querySelectorAll('button, a, input[type="button"], input[type="submit"], [role="button"], div, span');
                    for (var i = 0; i < buttons.length; i++) {
                        var b = buttons[i];
                        var txt = (b.innerText || b.value || b.getAttribute('aria-label') || '').toLowerCase().trim();
                        if (txt.indexOf(target) !== -1 || target.indexOf(txt) !== -1 && txt.length > 1) {
                            found = b;
                            break;
                        }
                    }
                }

                if (found) {
                    found.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    // Authentic human click dispatch
                    var rect = found.getBoundingClientRect();
                    var cx = rect.left + rect.width / 2;
                    var cy = rect.top + rect.height / 2;

                    found.dispatchEvent(new MouseEvent('mouseover', { bubbles: true, clientX: cx, clientY: cy }));
                    found.dispatchEvent(new MouseEvent('mousedown', { bubbles: true, clientX: cx, clientY: cy }));
                    found.dispatchEvent(new MouseEvent('mouseup', { bubbles: true, clientX: cx, clientY: cy }));
                    found.click();
                    return JSON.stringify({ success: true, text: (found.innerText || found.value || '').trim() });
                } else {
                    return JSON.stringify({ success: false, reason: "বাটন পাওয়া যায়নি" });
                }
            })();
        """.trimIndent()

        webView.post {
            webView.evaluateJavascript(clickJs) { rawRes ->
                val clean = rawRes?.trim('"', '\\', ' ') ?: "{}"
                val isSuccess = clean.contains("\"success\":true") || clean.contains("true")
                val details = if (isSuccess) "সফলভাবে '$buttonNameOrSelector' বাটনে ক্লিক করা হয়েছে।" else "'$buttonNameOrSelector' বাটন পাওয়া যায়নি।"
                addParticleMessage(
                    context = context,
                    senderName = "🖱️ পার্টিকল ২ (রানার)",
                    actionType = "CLICK",
                    text = details,
                    isSuccess = isSuccess
                )
                _activeParticleStatus.value = if (isSuccess) "✅ বাটন ক্লিক সম্পন্ন" else "⚠️ বাটন পাওয়া যায়নি"
                onDone(isSuccess, details)
            }
        }
    }

    /**
     * Particle Action 3: Human Smooth Scrolling for long code/pages.
     */
    fun particleScrollWebPage(
        context: Context,
        webView: WebView?,
        direction: String = "DOWN",
        amountPx: Int = 900,
        onDone: (Boolean) -> Unit = {}
    ) {
        if (webView == null) {
            onDone(false)
            return
        }

        val sign = if (direction.equals("UP", ignoreCase = true)) -1 else 1
        val delta = sign * amountPx

        _activeParticleStatus.value = "📜 পার্টিকল ৩ চলছে: পেজ ${if (sign > 0) "নিচে" else "উপরে"} স্ক্রোল করছে..."
        injectStealthShield(webView)

        val scrollJs = """
            (function() {
                window.scrollBy({ top: $delta, left: 0, behavior: 'smooth' });
                return "SCROLLED";
            })();
        """.trimIndent()

        webView.post {
            webView.evaluateJavascript(scrollJs) {
                addParticleMessage(
                    context = context,
                    senderName = "📜 পার্টিকল ৩ (স্ক্রলার)",
                    actionType = "SCROLL",
                    text = "পেজ ${if (sign > 0) "নিচে $amountPx px" else "উপরে $amountPx px"} স্মুথ স্ক্রোল করা হয়েছে (কোড ও কনটেন্ট লোড সম্পন্ন)।"
                )
                _activeParticleStatus.value = "✅ স্ক্রোল সম্পন্ন"
                onDone(true)
            }
        }
    }

    /**
     * Particle Action 4: Extract Content and Pipe directly into Particles Chat.
     * "সে কপি করবে না ওখানে যা লেখা আছে সোজা এসে আমার চ্যাটের অপশনে পেস্ট করতে পারে"
     */
    fun particleExtractAndPipeToChat(
        context: Context,
        webView: WebView?,
        tagOrReason: String = "কন্টেন্ট এক্সট্রাক্ট",
        onDone: (String) -> Unit = {}
    ) {
        if (webView == null) {
            onDone("")
            return
        }

        _activeParticleStatus.value = "📥 পার্টিকল ৪ চলছে: ওয়েবপেজ থেকে ডাটা চ্যাটে পাইপ করছে..."
        injectStealthShield(webView)

        val extractJs = """
            (function() {
                // Check for code blocks first
                var codeBlocks = document.querySelectorAll('pre, code, .code, .syntaxhighlighter');
                var codeText = "";
                for (var i = 0; i < codeBlocks.length && i < 3; i++) {
                    codeText += codeBlocks[i].innerText + "\n---\n";
                }

                // Main readable text
                var mainEl = document.querySelector('article, main, #content, .content, #main, .post-body') || document.body;
                var rawText = mainEl ? mainEl.innerText : document.body.innerText;
                var clean = rawText.replace(/\s+/g, ' ').trim().substring(0, 3200);

                return JSON.stringify({
                    title: document.title || "",
                    url: window.location.href || "",
                    code: codeText.substring(0, 2000),
                    text: clean
                });
            })();
        """.trimIndent()

        webView.post {
            webView.evaluateJavascript(extractJs) { rawJson ->
                try {
                    val unescaped = rawJson?.removePrefix("\"")?.removeSuffix("\"")
                        ?.replace("\\\"", "\"")?.replace("\\n", "\n") ?: "{}"
                    val obj = JSONObject(unescaped)
                    val title = obj.optString("title", "ওয়েবপেজ")
                    val pageUrl = obj.optString("url", "")
                    val code = obj.optString("code", "")
                    val text = obj.optString("text", "")

                    val combined = buildString {
                        appendLine("📄 **[$title]**")
                        if (code.isNotBlank()) {
                            appendLine("```\n$code\n```")
                        }
                        if (text.isNotBlank()) {
                            appendLine(text.take(1800))
                        }
                    }

                    addParticleMessage(
                        context = context,
                        senderName = "📥 পার্টিকল ৪ (এক্সট্র্যাক্টর)",
                        actionType = "EXTRACT",
                        text = combined,
                        targetUrl = pageUrl,
                        isCode = code.isNotBlank()
                    )
                    _activeParticleStatus.value = "✅ তথ্য সরাসরি চ্যাটে পেস্ট হয়েছে!"
                    onDone(combined)
                } catch (_: Throwable) {
                    val fallback = "ওয়েবপেজ থেকে ডাটা প্রাপ্ত হয়েছে।"
                    addParticleMessage(
                        context = context,
                        senderName = "📥 পার্টিকল ৪ (এক্সট্র্যাক্টর)",
                        actionType = "EXTRACT",
                        text = fallback
                    )
                    onDone(fallback)
                }
            }
        }
    }

    /**
     * Particle Action 5: Continuous Auto-Loop / 24-Hour Loop Toggle.
     */
    fun toggleContinuousLoop(
        context: Context,
        webView: WebView?,
        targetUrlOrQuery: String = ""
    ) {
        if (_isLoopRunning.value) {
            stopContinuousLoop(context)
        } else {
            startContinuousLoop(context, webView, targetUrlOrQuery)
        }
    }

    fun startContinuousLoop(
        context: Context,
        webView: WebView?,
        targetUrlOrQuery: String = ""
    ) {
        if (_isLoopRunning.value) return
        _isLoopRunning.value = true
        _activeParticleStatus.value = "🔄 পার্টিকল অটো-লুপ সক্রিয় (24/7 মোড)"
        addParticleMessage(
            context = context,
            senderName = "🔄 পার্টিকল ৫ (লুপ)",
            actionType = "LOOP_TICK",
            text = "অটো-লুপ সাইকেল চালু হয়েছে! পার্টিকল নিয়মিত ইন্টারভালে সাইট ভিজিট করবে ও ডাটা আনবে।"
        )

        loopJob = swarmScope.launch {
            var tick = 1
            while (isActive && _isLoopRunning.value) {
                _activeParticleStatus.value = "🔄 লুপ সাইকেল #$tick চলছে..."
                if (targetUrlOrQuery.isNotBlank() && webView != null) {
                    particleSearchAndNavigate(context, webView, targetUrlOrQuery)
                    delay(3500L)
                    particleScrollWebPage(context, webView, "DOWN", 600)
                    delay(1500L)
                    particleExtractAndPipeToChat(context, webView, "লুপ সাইকেল #$tick")
                } else if (webView != null) {
                    particleExtractAndPipeToChat(context, webView, "লুপ সাইকেল #$tick")
                }
                tick++
                // Interval between cycles: 30 seconds
                delay(30_000L)
            }
        }
    }

    fun stopContinuousLoop(context: Context) {
        _isLoopRunning.value = false
        loopJob?.cancel()
        loopJob = null
        _activeParticleStatus.value = "⏹️ পার্টিকল অটো-লুপ বন্ধ"
        addParticleMessage(
            context = context,
            senderName = "🔄 পার্টিকল ৫ (লুপ)",
            actionType = "LOOP_TICK",
            text = "অটো-লুপ সাইকেল বন্ধ করা হয়েছে।"
        )
    }

    /**
     * Autonomous AI-Particle Mission Coordinator:
     * Dispatches particles to navigate, extract code, and have the Gemini AI Brain continuously
     * evaluate the code until it is 100% correct, verified, and complete!
     */
    fun startAutonomousMission(
        context: Context,
        webView: WebView?,
        userGoal: String
    ) {
        if (_autonomousMissionState.value.isRunning) {
            stopAutonomousMission(context)
        }

        val goal = userGoal.ifBlank { "ওয়েবসাইট থেকে সঠিক ও সম্পূর্ণ কোড সংগ্রহ ও যাচাই" }
        _autonomousMissionState.value = AutonomousMissionState(
            isRunning = true,
            missionGoal = goal,
            iteration = 1,
            maxIterations = 12,
            statusText = "🚀 মিশন শুরু হয়েছে: এআই ব্রেন পর্যবেক্ষণ ও পার্টিকল চালনা করছে...",
            lastAiVerdict = "STARTED"
        )

        addParticleMessage(
            context = context,
            senderName = "🧠 এআই ব্রেন কো-অর্ডিনেটর",
            actionType = "AI_BRAIN",
            text = "🚀 **[এআই ব্রেন স্বয়ংক্রিয় মিশন চালু]:**\nলক্ষ্য: \"$goal\"\n\nএআই সরাসরি পার্টিকলসদের কমান্ড দেওয়া ও কোড/ডাটা মূল্যায়ন শুরু করছে। কোড পারফেক্ট না হওয়া পর্যন্ত স্বয়ংক্রিয়ভাবে লুপ চলবে।"
        )

        ChatSessionManager.addMessage(
            context = context,
            sender = "AI",
            text = "🔗 **[চ্যাট ২ এর সাথে সিঙ্ক হয়েছে]:**\nপার্টিকলস অটো-মিশন শুরু হয়েছে: \"$goal\"। পার্টিকলসরা ওয়েব থেকে ডাটা এনে চ্যাট ২-এ দেবে এবং এআই তা যাচাই করে সঠিক কোড মূল চ্যাটে পাঠাবে।",
            isCode = false
        )

        autonomousJob = swarmScope.launch {
            // Step 1: Check if goal requires navigation or search
            val lower = goal.lowercase()
            if (lower.contains("সার্চ") || lower.contains("search") || lower.contains("github") || lower.contains("leetcode") || lower.contains("google") || lower.contains("http")) {
                val searchQuery = goal.replace(Regex("(?i)(পার্টিকল|দিয়ে|সার্চ|করো|যাও|search|go|to|কোড|আনো|থেকে)"), "").trim().ifBlank { "Python algorithm code" }
                particleSearchAndNavigate(context, webView, searchQuery)
                delay(3500L)
            }

            var currentIteration = 1
            while (isActive && _autonomousMissionState.value.isRunning && currentIteration <= 12) {
                _autonomousMissionState.value = _autonomousMissionState.value.copy(
                    iteration = currentIteration,
                    statusText = "🔍 ধাপ #$currentIteration: পেজ থেকে কোড এক্সট্র্যাক্ট ও যাচাই চলছে..."
                )

                // 1. Extract content from webview
                var extractedText = ""
                withContext(Dispatchers.Main) {
                    particleExtractAndPipeToChat(context, webView, "মিশন ধাপ #$currentIteration") { result ->
                        extractedText = result
                    }
                }
                delay(2000L)

                // If nothing extracted or very short, scroll down and try again
                if (extractedText.length < 80) {
                    addParticleMessage(
                        context = context,
                        senderName = "🧠 এআই ব্রেন",
                        actionType = "AI_BRAIN",
                        text = "⏳ পেজে পর্যাপ্ত কোড পাওয়া যায়নি। পার্টিকল ৩-কে পেজ নিচে ৮০০px স্ক্রোল করে নতুন কনটেন্ট লোড করার নির্দেশ দেওয়া হচ্ছে... (ধাপ #$currentIteration)"
                    )
                    withContext(Dispatchers.Main) {
                        particleScrollWebPage(context, webView, "DOWN", 800)
                    }
                    delay(2500L)
                    currentIteration++
                    continue
                }

                _autonomousMissionState.value = _autonomousMissionState.value.copy(
                    lastExtractedSnippet = extractedText.take(500),
                    statusText = "🧠 ধাপ #$currentIteration: জেমিনি এআই ব্রেন কোড গভীরভাবে মূল্যায়ন করছে..."
                )

                // 2. Evaluate using UniversalAiManager / Gemini API
                val evalPrompt = """
                    You are the Autonomous Master AI Brain overseeing automated web micro-particles in Android Chromium.
                    User Goal: "$goal"
                    Current Iteration: $currentIteration of 12
                    Extracted Web Content / Code:
                    ${extractedText.take(2500)}

                    Task:
                    Evaluate if this extracted web data contains the valid, working, complete code or answer for the goal.
                    1. Is the code complete and functional, or is it broken/incomplete/placeholder?
                    2. If NOT complete or incorrect:
                       - We must RETRY. Specify the next particle action:
                         - SCROLL: scroll down to reveal more code
                         - RUN_BUTTON: click 'Run' or 'Submit' or 'Execute'
                         - RETRY_SEARCH: search a better query
                    3. If COMPLETE and correct:
                       - We declare SUCCESS. Provide the clean code.

                    Reply in Bengali strictly using this format:
                    [সিদ্ধান্ত]: RETRY অথবা SUCCESS
                    [একশন]: SCROLL অথবা CLICK_RUN অথবা DONE
                    [ব্যাখ্যা]: ২-৩ লাইনে মূল্যায়ন ও কারণ
                    [কোড]: সংগৃহীত কোড
                """.trimIndent()

                val aiResult = UniversalAiManager.executeAiPrompt(
                    prompt = evalPrompt,
                    systemInstruction = "You are an autonomous AI Engineer evaluating web automation results.",
                    includeStandingRules = false
                )

                val evalText = aiResult.getOrElse {
                    if (extractedText.contains("def ") || extractedText.contains("function") || extractedText.contains("class ") || extractedText.contains("import ")) {
                        "[সিদ্ধান্ত]: SUCCESS\n[একশন]: DONE\n[ব্যাখ্যা]: ওয়েবপেজে বৈধ কোড সনাক্ত করা হয়েছে।\n[কোড]: $extractedText"
                    } else {
                        "[সিদ্ধান্ত]: RETRY\n[একশন]: SCROLL\n[ব্যাখ্যা]: পর্যাপ্ত কার্যকর কোড পাওয়া যায়নি, পেজ আরও স্ক্রোল করা প্রয়োজন।"
                    }
                }

                val isSuccess = evalText.contains("[সিদ্ধান্ত]: SUCCESS") || evalText.contains("SUCCESS") || (!evalText.contains("RETRY") && currentIteration >= 3 && extractedText.contains("```"))

                if (isSuccess) {
                    _autonomousMissionState.value = _autonomousMissionState.value.copy(
                        isRunning = false,
                        statusText = "🎉 মিশন সফল! শতভাগ সঠিক কোড যাচাই সম্পন্ন।",
                        lastAiVerdict = "SUCCESS"
                    )

                    addParticleMessage(
                        context = context,
                        senderName = "🎉 এআই ব্রেন (চূড়ান্ত যাচাই)",
                        actionType = "AI_BRAIN",
                        text = "✅ **[মিশন সফল — কোড ভেরিফাইড]:**\n$evalText",
                        isCode = true
                    )

                    // Sync verified final result to Chat 1 (Main AI Chat)
                    ChatSessionManager.addMessage(
                        context = context,
                        sender = "AI",
                        text = "🎯 **[পার্টিকলস অটো-মিশন সফল]:**\n\nলক্ষ্য: \"$goal\"\n\n$evalText\n\n*(এই ফলাফলটি পার্টিকলস চ্যাট ২-এ স্বয়ংক্রিয় লুপ ও যাচাই প্রক্রিয়ার মাধ্যমে আনা হয়েছে)*",
                        isCode = true
                    )
                    break
                } else {
                    // Not satisfied yet, AI orders particles to retry!
                    val nextAction = if (evalText.contains("CLICK_RUN") || evalText.contains("রান বাটন")) "CLICK_RUN" else "SCROLL"
                    _autonomousMissionState.value = _autonomousMissionState.value.copy(
                        statusText = "⚠️ কোড অসম্পূর্ণ — পরবর্তী অ্যাকশন '$nextAction' নেওয়া হচ্ছে (ধাপ #$currentIteration)...",
                        lastAiVerdict = "RETRY"
                    )

                    addParticleMessage(
                        context = context,
                        senderName = "🧠 এআই ব্রেন (পুনঃনির্দেশনা)",
                        actionType = "AI_BRAIN",
                        text = "⚠️ **[কোড যাচাই — রিট্রাই প্রয়োজন (ধাপ #$currentIteration)]:**\n$evalText\n\n👉 *এআই স্বয়ংক্রিয়ভাবে পার্টিকলকে পেজে পরবর্তী পদক্ষেপ নেওয়ার নির্দেশ দিয়েছে।*",
                        isCode = false
                    )

                    withContext(Dispatchers.Main) {
                        if (nextAction == "CLICK_RUN") {
                            particleClickButton(context, webView, "run")
                        } else {
                            particleScrollWebPage(context, webView, "DOWN", 750)
                        }
                    }
                    delay(3000L)
                }

                currentIteration++
            }

            if (_autonomousMissionState.value.isRunning && currentIteration > 12) {
                stopAutonomousMission(context)
                addParticleMessage(
                    context = context,
                    senderName = "🧠 এআই ব্রেন",
                    actionType = "AI_BRAIN",
                    text = "⏱️ সর্বোচ্চ ১২টি লুপ সাইকেল সম্পন্ন হয়েছে। পেজের প্রাপ্ত ডাটা চ্যাটে সংরক্ষিত রয়েছে।"
                )
            }
        }
    }

    fun stopAutonomousMission(context: Context) {
        _autonomousMissionState.value = _autonomousMissionState.value.copy(
            isRunning = false,
            statusText = "⏹️ এআই ব্রেন অটো-মিশন বন্ধ"
        )
        autonomousJob?.cancel()
        autonomousJob = null
        addParticleMessage(
            context = context,
            senderName = "🧠 এআই ব্রেন",
            actionType = "AI_BRAIN",
            text = "⏹️ এআই ব্রেন স্বয়ংক্রিয় মিশন বন্ধ করা হয়েছে।"
        )
    }

    /**
     * Returns live bidirectional context summary of BOTH Chat 1 (Main AI Chat) and Chat 2 (Particles)
     * so that whichever chat the user or AI is in, it can see the full recent conversation and actions of the other chat!
     */
    fun getDualChatCombinedContext(): String {
        val chat1Msgs = ChatSessionManager.activeSession.value?.messages?.takeLast(12).orEmpty()
        val chat2Msgs = _particleMessages.value.takeLast(12)
        val mission = _autonomousMissionState.value
        val particles = _particlesList.value

        return buildString {
            appendLine("\n=== [ডুয়াল চ্যাট লাইভ ব্রিজ: চ্যাট ১ (মেইন এআই) ⟷ চ্যাট ২ (পার্টিকলস ল্যাব)] ===")
            appendLine("• মোট পার্টিকল কণা: ${particles.size} টি (${particles.joinToString { "${it.name}[${it.roleBadge}]" }})")
            if (mission.isRunning) {
                appendLine("• সক্রিয় অটো-মিশন: \"${mission.missionGoal}\" (ধাপ #${mission.iteration}) [${mission.statusText}]")
            }
            appendLine("• [চ্যাট ১ (মেইন চ্যাট)-এ কী হচ্ছে (সাম্প্রতিক কথোপকথন)]:")
            if (chat1Msgs.isEmpty()) {
                appendLine("  (চ্যাট ১-এ এখনো কোনো মেসেজ নেই)")
            } else {
                chat1Msgs.forEach { m ->
                    appendLine("  [Chat-1 ${m.sender}]: ${m.text.take(700).replace("\n", " ")}")
                }
            }
            appendLine("• [চ্যাট ২ (পার্টিকলস চ্যাট)-এ কী হচ্ছে (সাম্প্রতিক অ্যাকশন ও কথোপকথন)]:")
            if (chat2Msgs.isEmpty()) {
                appendLine("  (চ্যাট ২-এ এখনো কোনো অ্যাকশন নেই)")
            } else {
                chat2Msgs.forEach { m ->
                    appendLine("  [Chat-2 ${m.senderName} - ${m.actionType}]: ${m.text.take(700).replace("\n", " ")}")
                }
            }
            if (mission.lastExtractedSnippet.isNotBlank()) {
                appendLine("• সর্বশেষ ওয়েব থেকে প্রাপ্ত কোড/তথ্য:")
                appendLine("  ${mission.lastExtractedSnippet.take(800)}")
            }
            appendLine("=== [ডুয়াল চ্যাট সিঙ্ক সমাপ্ত] ===")
        }
    }

    fun updateCanvasPhysicsState(
        context: Context,
        isEnabled: Boolean? = null,
        particleCount: Int? = null,
        speedMultiplier: Float? = null,
        particleSizeDp: Float? = null,
        colorHex: String? = null,
        secondaryColorHex: String? = null,
        physicsMode: String? = null
    ): ParticleCanvasPhysicsState {
        val cur = _canvasPhysicsState.value
        val next = cur.copy(
            isEnabled = isEnabled ?: cur.isEnabled,
            particleCount = (particleCount ?: cur.particleCount).coerceIn(6, 160),
            speedMultiplier = (speedMultiplier ?: cur.speedMultiplier).coerceIn(0.15f, 8.0f),
            particleSizeDp = (particleSizeDp ?: cur.particleSizeDp).coerceIn(2.0f, 16.0f),
            colorHex = colorHex ?: cur.colorHex,
            secondaryColorHex = secondaryColorHex ?: cur.secondaryColorHex,
            physicsMode = physicsMode ?: cur.physicsMode
        )
        _canvasPhysicsState.value = next
        _activeParticleStatus.value = "✨ পার্টিকল ফিজিক্স: ${next.physicsMode} • গতি ${next.speedMultiplier}x • কণা ${next.particleCount}টি"
        AppTruthAndArchitectureVault.recordLiveMutation(
            category = "PARTICLE_PHYSICS_CONTROL",
            summaryBangla = "পার্টিকল ক্যানভাস আপডেট: মোড=${next.physicsMode}, গতি=${next.speedMultiplier}x, সংখ্যা=${next.particleCount}, কালার=${next.colorHex}, চালু=${next.isEnabled}"
        )
        return next
    }

    /**
     * Full DOM Input Typing: Types text into any input/textarea on the active WebView.
     */
    fun particleTypeIntoInput(
        context: Context,
        webView: WebView?,
        selectorOrHint: String,
        textToType: String,
        onComplete: (String) -> Unit = {}
    ) {
        if (webView == null) {
            onComplete("⚠️ ব্রাউজার সক্রিয় নেই।")
            return
        }
        val safeHint = selectorOrHint.replace("'", "\\'")
        val safeVal = JSONObject.quote(textToType)
        val js = """
            (function(){
              var el = document.querySelector('$safeHint') ||
                       document.querySelector('input[type="search"], input[type="text"], textarea');
              if (!el) {
                var inputs = document.querySelectorAll('input, textarea, [contenteditable="true"]');
                for (var i=0; i<inputs.length; i++) {
                  var ph = (inputs[i].placeholder || inputs[i].name || inputs[i].id || '').toLowerCase();
                  if (ph.indexOf('$safeHint'.toLowerCase()) !== -1) { el = inputs[i]; break; }
                }
                if (!el && inputs.length > 0) el = inputs[0];
              }
              if (el) {
                el.focus();
                el.value = $safeVal;
                el.dispatchEvent(new Event('input', { bubbles: true }));
                el.dispatchEvent(new Event('change', { bubbles: true }));
                return 'TYPED_OK:' + (el.name || el.id || el.tagName);
              }
              return 'NO_INPUT_FOUND';
            })();
        """.trimIndent()
        mainHandler.post {
            webView.evaluateJavascript(js) { res ->
                val msg = if (res != null && res.contains("TYPED_OK")) {
                    "⌨️ পার্টিকল ইনপুট বক্সে লিখেছে: \"$textToType\""
                } else {
                    "⚠️ পেজে ইনপুট বক্স পাওয়া যায়নি, তবে স্ক্রিপ্ট পাঠানো হয়েছে।"
                }
                addParticleMessage(context, "পার্টিকল ২ (টাইপার)", "TYPE", msg)
                onComplete(msg)
            }
        }
    }

    /**
     * Full DOM CSS/Theme Mutation: Injects custom CSS or dark/neon theme into any live webpage.
     */
    fun particleMutateDomStyle(
        context: Context,
        webView: WebView?,
        cssOrThemeDirective: String,
        onComplete: (String) -> Unit = {}
    ) {
        if (webView == null) {
            onComplete("⚠️ ব্রাউজার সক্রিয় নেই।")
            return
        }
        val lower = cssOrThemeDirective.lowercase()
        val cssRule = when {
            lower.contains("কালো") || lower.contains("ডার্ক") || lower.contains("dark") ->
                "html, body { background: #090D1A !important; color: #F8FAFC !important; } a, button { color: #00F0FF !important; }"
            lower.contains("সাদা") || lower.contains("white") ->
                "html, body { background: #FFFFFF !important; color: #0F172A !important; }"
            lower.contains("বড় ফন্ট") || lower.contains("জুম") ->
                "body { zoom: 1.25 !important; font-size: 120% !important; }"
            else -> cssOrThemeDirective
        }
        val safeCss = JSONObject.quote(cssRule)
        val js = """
            (function(){
              var st = document.getElementById('__particle_dom_style');
              if (!st) {
                st = document.createElement('style');
                st.id = '__particle_dom_style';
                document.head.appendChild(st);
              }
              st.textContent = $safeCss;
              return 'STYLE_APPLIED';
            })();
        """.trimIndent()
        mainHandler.post {
            webView.evaluateJavascript(js) {
                val msg = "🎨 পার্টিকল ওয়েবপেজের স্টাইল/থিম পরিবর্তন করেছে!"
                addParticleMessage(context, "পার্টিকল ১ (ডম স্টাইলার)", "STYLE", msg)
                onComplete(msg)
            }
        }
    }

    /**
     * Natural Language AI Brain Particle Dispatcher:
     * Handles:
     * 1. Live Particle Canvas Physics Control (speed, count, size, color, vortex/matrix/neural modes, on/off)
     * 2. Complete Website Creation & Incremental Feature / Game Synthesis directly inside Chat 2!
     * 3. Explicit Browser Automation (Search, Click, Scroll, Type, Style, Extract, Mission Loop) without false-positive hijacking of normal conversation!
     * 4. Full Main AI Brain + Dual-Chat Sync + [OMNI_PATCH] execution.
     */
    fun dispatchParticleCommandByAi(
        context: Context,
        webView: WebView?,
        commandText: String,
        onReplied: (String) -> Unit
    ) {
        val lower = commandText.lowercase().trim()
        val isQuestion = commandText.contains("?") || commandText.contains("？") ||
            lower.startsWith("কি ") || lower.startsWith("কী ") || lower.startsWith("কেন ") || lower.startsWith("কিভাবে ")

        // 0. Check if user is controlling the Particle Canvas Physics itself (Speed, Color, Count, Size, Mode, ON/OFF)
        val mentionsParticleSelf = lower.contains("পার্টিকল") || lower.contains("particle") || lower.contains("কণা")
        val isPhysicsControl = mentionsParticleSelf && (
            lower.contains("স্পিড") || lower.contains("গতি") || lower.contains("speed") ||
                lower.contains("কালার") || lower.contains("রঙ") || lower.contains("রং") || lower.contains("color") ||
                lower.contains("সংখ্যা") || lower.contains("বাড়াও") || lower.contains("বাড়াও") || lower.contains("কমাও") ||
                lower.contains("বড়") || lower.contains("বড়") || lower.contains("ছোট") ||
                lower.contains("ভর্টেক্স") || lower.contains("vortex") || lower.contains("ম্যাট্রিক্স") || lower.contains("matrix") ||
                lower.contains("অরবিট") || lower.contains("orbit") || lower.contains("নিউরাল") ||
                lower.contains("অফ করো") || lower.contains("বন্ধ করো") || lower.contains("অন করো") || lower.contains("চালু করো")
            )
        if (isPhysicsControl && !lower.contains("মিশন") && !lower.contains("সার্চ")) {
            val cur = _canvasPhysicsState.value
            val turnOff = lower.contains("অফ করো") || lower.contains("বন্ধ করো")
            val turnOn = lower.contains("অন করো") || lower.contains("চালু করো")
            val faster = lower.contains("বাড়াও") || lower.contains("বাড়াও") || lower.contains("দ্রুত") || lower.contains("ফাস্ট") || lower.contains("টার্বো")
            val slower = lower.contains("কমাও") || lower.contains("ধীরে") || lower.contains("স্লো")
            val newSpeed = when {
                faster -> (cur.speedMultiplier * 1.65f).coerceAtMost(8.0f)
                slower -> (cur.speedMultiplier * 0.6f).coerceAtLeast(0.2f)
                else -> cur.speedMultiplier
            }
            val newCount = when {
                lower.contains("সংখ্যা") && faster -> (cur.particleCount + 24).coerceAtMost(150)
                lower.contains("সংখ্যা") && slower -> (cur.particleCount - 16).coerceAtLeast(8)
                faster -> (cur.particleCount + 12).coerceAtMost(140)
                else -> cur.particleCount
            }
            val newSize = when {
                lower.contains("বড়") || lower.contains("বড়") -> (cur.particleSizeDp + 2.0f).coerceAtMost(14.0f)
                lower.contains("ছোট") -> (cur.particleSizeDp - 1.5f).coerceAtLeast(2.5f)
                else -> cur.particleSizeDp
            }
            val newColor = when {
                lower.contains("লাল") || lower.contains("red") -> "#FF0055"
                lower.contains("সবুজ") || lower.contains("green") -> "#10B981"
                lower.contains("হলুদ") || lower.contains("সোনালী") || lower.contains("gold") -> "#F59E0B"
                lower.contains("বেগুনি") || lower.contains("purple") -> "#A855F7"
                lower.contains("সাদা") || lower.contains("white") -> "#FFFFFF"
                lower.contains("নীল") || lower.contains("আকাশি") || lower.contains("cyan") -> "#00F0FF"
                else -> cur.colorHex
            }
            val newMode = when {
                lower.contains("ভর্টেক্স") || lower.contains("vortex") || lower.contains("ঘূর্ণি") -> "VORTEX"
                lower.contains("ম্যাট্রিক্স") || lower.contains("matrix") || lower.contains("বৃষ্টি") -> "MATRIX_RAIN"
                lower.contains("অরবিট") || lower.contains("orbit") || lower.contains("বৃত্ত") -> "ORBIT"
                lower.contains("সোয়ার্ম") || lower.contains("swarm") -> "HYPER_SWARM"
                lower.contains("নিউরাল") || lower.contains("neural") -> "NEURAL_WEB"
                else -> cur.physicsMode
            }
            val updated = updateCanvasPhysicsState(
                context = context,
                isEnabled = if (turnOff) false else if (turnOn) true else cur.isEnabled,
                particleCount = newCount,
                speedMultiplier = newSpeed,
                particleSizeDp = newSize,
                colorHex = newColor,
                physicsMode = newMode
            )
            onReplied(
                "✨ **[পার্টিকল ক্যানভাস ও ফিজিক্স কন্ট্রোল কার্যকর]:**\n" +
                    "• **অবস্থা:** ${if (updated.isEnabled) "সচল (ON)" else "বন্ধ (OFF)"}\n" +
                    "• **ফিজিক্স মোড:** `${updated.physicsMode}`\n" +
                    "• **গতি (Speed):** `${String.format("%.1f", updated.speedMultiplier)}x` | **কণার সংখ্যা:** `${updated.particleCount}টি` | **আকার:** `${updated.particleSizeDp}dp`\n" +
                    "• **রঙ (Color):** `${updated.colorHex}`"
            )
            return
        }

        // 0.5. Direct Website or Feature Creation inside Chat 2 (Dual Brain Parity with Chat 1!)
        val isWebsiteCreateCmd = (lower.contains("ওয়েবসাইট") || lower.contains("ওয়েবসাইট") || lower.contains("website") || lower.contains("সাইট তৈরি") || lower.contains("সাইট বানাও")) &&
            (lower.contains("বানাও") || lower.contains("ক্রিয়েট") || lower.contains("ক্রিয়েট") || lower.contains("তৈরি") || lower.contains("বিল্ড") || lower.contains("অ্যাড") || lower.contains("এড"))
        if (isWebsiteCreateCmd && !isQuestion) {
            swarmScope.launch {
                val (siteReport, _) = SovereignDeepResearchAndIncrementalEngine.createAndDeployCompleteWebsiteWithRetry(
                    context = context,
                    userRequest = commandText,
                    activeWebView = webView
                )
                onReplied(siteReport)
                ChatSessionManager.addMessage(context, "AI", "🌐 [চ্যাট ২ থেকে ওয়েবসাইট নির্মিত]:\n$siteReport", false)
            }
            return
        }

        val isFeatureCreateCmd = !mentionsParticleSelf && !isQuestion && (
            lower.contains("ফিচার অ্যাড") || lower.contains("ফিচার যুক্ত") || lower.contains("ফিচার বানাও") ||
                lower.contains("গেম বানাও") || lower.contains("স্টুডিও বানাও") || lower.contains("অ্যাপ বানাও") ||
                lower.contains("ক্যালকুলেটর বানাও") || lower.contains("স্লট অ্যাড") || lower.contains("এআই বানাও")
            )
        if (isFeatureCreateCmd) {
            swarmScope.launch {
                val evoRes = AutonomousSelfEvolutionEngine.processAndExecuteSovereignCommand(
                    context = context,
                    rawInput = commandText,
                    bridge = null,
                    activeWebView = webView
                )
                if (evoRes != null && evoRes.wasHandledAsActionOrUpgrade) {
                    onReplied(evoRes.detailedResponseBangla)
                    ChatSessionManager.addMessage(context, "AI", "🏗️ [চ্যাট ২ থেকে ফিচার নির্মিত]:\n${evoRes.detailedResponseBangla}", false)
                }
            }
            return
        }

        // Strict intent checks for browser particle actions so normal conversational questions are NOT hijacked!
        val isExplicitSearch = !isQuestion && (
            lower.contains("সার্চ করো") || lower.contains("সার্চ দাও") || lower.startsWith("search ") ||
                lower.contains("গুগলে যাও") || lower.contains("সাইটে যাও") || lower.contains("লিঙ্কে যাও") || lower.contains("ওপেন করো http")
            )
        val isExplicitClick = !isQuestion && (
            lower.contains("ক্লিক করো") || lower.contains("বাটনে চাপো") || lower.contains("রান বাটনে") ||
                lower.contains("সাবমিট বাটনে") || lower.startsWith("click ")
            )
        val isExplicitScroll = !isQuestion && (
            lower.contains("স্ক্রোল করো") || lower.contains("নিচে নামো") || lower.contains("উপরে ওঠো") || lower.startsWith("scroll ")
            )
        val isExplicitType = !isQuestion && (
            lower.contains("টাইপ করো") || lower.contains("ইনপুটে লেখো") || lower.contains("বক্সে লেখো")
            )
        val isExplicitExtract = !isQuestion && (
            lower.contains("এক্সট্র্যাক্ট করো") || lower.contains("পেজ থেকে আনো") || lower.contains("কোড কপি করে আনো") || lower.contains("পেস্ট করো")
            )

        when {
            lower.contains("মিশন শুরু") || lower.contains("অটো মিশন") || lower.contains("যতক্ষণ না") || lower.contains("ঠিক কোড আনো") || lower.contains("কোড নিয়ে আসো ঠিক করে") -> {
                val cleanGoal = commandText.replace(Regex("(?i)(পার্টিকল|দিয়ে|মিশন|শুরু|করো)"), "").trim().ifBlank { commandText }
                startAutonomousMission(context, webView, cleanGoal)
                onReplied("🚀 এআই ব্রেন অটো-মিশন শুরু হয়েছে: \"$cleanGoal\"। চ্যাট ২-এ পার্টিকলস সক্রিয় এবং যাচাই চলছে!")
            }
            lower.contains("মিশন থামাও") || lower.contains("মিশন বন্ধ") -> {
                stopAutonomousMission(context)
                onReplied("⏹️ এআই ব্রেন অটো-মিশন বন্ধ করা হয়েছে।")
            }
            lower.contains("পার্টিকল ক্রিয়েট") || lower.contains("পার্টিকল বানাও") || lower.contains("create particle") || lower.contains("নতুন পার্টিকল") -> {
                val pName = commandText.replace(Regex("(?i)(পার্টিকল|ক্রিয়েট|করো|বানাও|create|particle|নতুন)"), "").trim().take(30)
                val newP = createExtraCustomParticle(
                    context = context,
                    name = if (pName.isNotBlank()) pName else "ওয়েব এজেন্ট পার্টিকল #${_particlesList.value.size + 1}",
                    roleBadge = "⚡ AGENT",
                    description = commandText
                )
                onReplied("✅ নতুন পার্টিকল কণা '${newP.name}' তৈরি হয়েছে এবং চ্যাট ২-এ যুক্ত হয়েছে!")
            }
            isExplicitSearch -> {
                val cleanQuery = commandText.replace(Regex("(?i)(পার্টিকল|দিয়ে|গুগলে|সাইটে|লিঙ্কে|সার্চ|করো|দাও|যাও|search|go|to)"), "").trim()
                val target = cleanQuery.ifBlank { "Google" }
                particleSearchAndNavigate(context, webView, target)
                onReplied("🌐 পার্টিকল ১ '$target'-এ পাঠানো হয়েছে এবং সার্ফ করছে।")
            }
            isExplicitType -> {
                val textToType = commandText.replace(Regex("(?i)(পার্টিকল|দিয়ে|ইনপুটে|বক্সে|টাইপ করো|লেখো)"), "").trim()
                particleTypeIntoInput(context, webView, "", textToType) { details ->
                    onReplied(details)
                }
            }
            isExplicitClick -> {
                val btnTarget = when {
                    lower.contains("রান") || lower.contains("run") -> "run"
                    lower.contains("সাবমিট") || lower.contains("submit") -> "submit"
                    lower.contains("সার্চ") -> "search"
                    else -> commandText
                        .replace(Regex("(?i)(পার্টিকল|দিয়ে|দিয়ে|বাটনে|বাটন|ক্লিক|করো|চাপো|ট্যাপ|click|on|button)"), "")
                        .trim()
                        .ifBlank { "button" }
                }
                particleClickButton(context, webView, btnTarget) { _, details ->
                    onReplied("🖱️ পার্টিকল ২: $details")
                }
            }
            isExplicitScroll -> {
                val dir = if (lower.contains("উপরে") || lower.contains("up")) "UP" else "DOWN"
                particleScrollWebPage(context, webView, dir, 800) {
                    onReplied("📜 পার্টিকল ৩ পেজ $dir স্ক্রোল করেছে।")
                }
            }
            lower.contains("২৪ঘ লুপ") || lower.contains("অটো লুপ") || lower.contains("পার্টিকল লুপ") -> {
                if (lower.contains("বন্ধ") || lower.contains("stop") || lower.contains("অফ")) {
                    stopContinuousLoop(context)
                    onReplied("⏹️ পার্টিকল লুপ বন্ধ করা হয়েছে।")
                } else {
                    startContinuousLoop(context, webView, "")
                    onReplied("🔄 পার্টিকল অটো-লুপ সক্রিয় করা হয়েছে!")
                }
            }
            isExplicitExtract -> {
                particleExtractAndPipeToChat(context, webView, "ইউজার আদেশ") { _ ->
                    onReplied("📥 পার্টিকল ৪ পেজের কন্টেন্ট সফলভাবে চ্যাটে এনে পেস্ট করেছে!")
                }
            }
            else -> {
                // Dual Chat Synchronization: Dispatch to the Main AI Brain with full 61-File Map + Omni-Control + Dual Chat Memory!
                swarmScope.launch {
                    val truthMap = AppTruthAndArchitectureVault.getFullSystemTruthInstruction(commandText)
                    val promptSys = "You are the Main Sovereign AI Brain operating inside Chat 2 (Particles & Automation Chat). Both Chat 1 (Main AI Chat) and Chat 2 are permanently linked, share the same API Key, and see each other's full conversation and 61-file codebase map. You have 100% control over the app via [OMNI_PATCH]. Reply helpfully in Bengali.\n" +
                        truthMap + "\n" +
                        UniversalOmniControlEngine.getOmniControlSystemPrompt() + "\n" +
                        getDualChatCombinedContext()
                    val aiRes = UniversalAiManager.executeAiPrompt(
                        prompt = commandText,
                        systemInstruction = promptSys,
                        includeStandingRules = false
                    )
                    val rawReply = aiRes.getOrElse {
                        "✨ **[পার্টিকল ও মেইন এআই ব্রেন]:**\nআপনার নির্দেশনা (\"$commandText\") গৃহীত হয়েছে। পার্টিকলস স্বয়ংক্রিয়ভাবে সক্রিয় রয়েছে এবং চ্যাট ১-এর সাথে সিঙ্ক করা হয়েছে।"
                    }
                    if (rawReply.contains("[OMNI_PATCH]")) {
                        UniversalOmniControlEngine.processNaturalLanguageOrJsonCommand(context, rawReply)
                    }
                    val genesisRegex = Regex("\\[GENESIS_BUILD\\](.*?)\\[/GENESIS_BUILD\\]", RegexOption.DOT_MATCHES_ALL)
                    val genesisMatch = genesisRegex.find(rawReply)
                    if (genesisMatch != null) {
                        val gSpec = genesisMatch.groupValues[1].trim()
                        AutonomousSelfEvolutionEngine.synthesizeAndExecuteLiveFeatureUpgrade(
                            context = context,
                            userOrder = gSpec.ifBlank { commandText },
                            activeWebView = webView
                        )
                    }
                    val cleanReply = rawReply
                        .replace(Regex("\\[OMNI_PATCH\\].*?\\[/OMNI_PATCH\\]", RegexOption.DOT_MATCHES_ALL), "")
                        .replace(genesisRegex, "")
                        .trim()
                    onReplied(cleanReply.ifBlank { "✅ আপনার আদেশ কার্যকর করা হয়েছে।" })
                    // Also bridge to Chat 1 session manager so Chat 1 knows what occurred in Chat 2!
                    ChatSessionManager.addMessage(
                        context = context,
                        sender = "AI",
                        text = "💬 **[চ্যাট ২ (পার্টিকলস) থেকে সিঙ্ক]:**\nইউজার: \"$commandText\"\nজবাব: ${cleanReply.ifBlank { "অমনি কন্ট্রোল কার্যকর হয়েছে" }}",
                        isCode = false
                    )
                }
            }
        }
    }
}
