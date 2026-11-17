package com.example.engine

import android.content.Context
import android.util.Base64
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
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class GhostMacroStep(
    val actionType: String, // CLICK, INPUT, SCROLL
    val cssSelector: String,
    val valueOrText: String = "",
    val scrollY: Int = 0
)

data class GhostMacroBlueprint(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val targetDomainOrUrl: String,
    val steps: List<GhostMacroStep>,
    val createdAt: Long = System.currentTimeMillis()
)

data class CapturedApiPacket(
    val id: String = UUID.randomUUID().toString(),
    val method: String,
    val url: String,
    val status: Int,
    val responsePreview: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Step 2: Human Action Ghost Macro Recorder/Replayer & Deep Network Fetch/XHR API Sniffer.
 * 1. Records real user clicks, typing, and scrolling on any website and replays them 1x or in an infinite loop.
 * 2. Intercepts hidden background `fetch()` and `XMLHttpRequest` JSON API responses in real time.
 */
object GhostMacroAndSnifferEngine {
    private const val PREFS_NAME = "skrl2_ghost_macro_sniffer_prefs"
    private const val KEY_MACROS_JSON = "key_saved_ghost_macros"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var replayJob: Job? = null

    private val _isRecordingMacro = MutableStateFlow(false)
    val isRecordingMacro: StateFlow<Boolean> = _isRecordingMacro.asStateFlow()

    private val _isReplayingMacro = MutableStateFlow(false)
    val isReplayingMacro: StateFlow<Boolean> = _isReplayingMacro.asStateFlow()

    private val _savedMacros = MutableStateFlow<List<GhostMacroBlueprint>>(emptyList())
    val savedMacros: StateFlow<List<GhostMacroBlueprint>> = _savedMacros.asStateFlow()

    private val _isSnifferActive = MutableStateFlow(true)
    val isSnifferActive: StateFlow<Boolean> = _isSnifferActive.asStateFlow()

    private val _capturedPackets = MutableStateFlow<List<CapturedApiPacket>>(emptyList())
    val capturedPackets: StateFlow<List<CapturedApiPacket>> = _capturedPackets.asStateFlow()

    private val _statusBanner = MutableStateFlow("👻 ঘোস্ট ম্যাক্রো রেকর্ডার ও ডিপ API স্নিফার প্রস্তুত")
    val statusBanner: StateFlow<String> = _statusBanner.asStateFlow()

    fun init(context: Context) {
        loadMacros(context)
    }

    /**
     * Starts recording every click, input, and scroll performed by the user inside the WebView.
     */
    fun startRecordingMacro(webView: WebView?) {
        if (webView == null) {
            _statusBanner.value = "⚠️ রেকর্ড করার জন্য সক্রিয় ব্রাউজার ট্যাব প্রয়োজন।"
            return
        }
        _isRecordingMacro.value = true
        _statusBanner.value = "🔴 রেকর্ডিং চলছে... আপনি ওয়েবসাইটে যা ক্লিক বা টাইপ করবেন তা শেখা হচ্ছে!"

        val recorderJs = """
            (function() {
                window.__skrlGhostMacroEvents = [];
                function getSmartSelector(el) {
                    if (!el || !el.tagName) return 'body';
                    if (el.id) return '#' + el.id;
                    if (el.getAttribute('data-testid')) return '[data-testid="' + el.getAttribute('data-testid') + '"]';
                    if (el.getAttribute('name')) return el.tagName.toLowerCase() + '[name="' + el.getAttribute('name') + '"]';
                    if (el.getAttribute('aria-label')) return '[aria-label="' + el.getAttribute('aria-label').replace(/"/g, '') + '"]';
                    if (el.className && typeof el.className === 'string') {
                        var cls = el.className.trim().split(/\s+/)[0];
                        if (cls) return el.tagName.toLowerCase() + '.' + cls;
                    }
                    return el.tagName.toLowerCase();
                }
                if (!window.__skrlMacroListenersAttached) {
                    window.__skrlMacroListenersAttached = true;
                    document.addEventListener('click', function(e) {
                        if (!window.__skrlRecordingOn) return;
                        var t = e.target.closest('button, a, input, textarea, [role="button"]') || e.target;
                        window.__skrlGhostMacroEvents.push({
                            actionType: 'CLICK',
                            cssSelector: getSmartSelector(t),
                            valueOrText: (t.innerText || t.value || '').trim().slice(0, 60),
                            scrollY: Math.round(window.scrollY || 0)
                        });
                    }, true);
                    document.addEventListener('change', function(e) {
                        if (!window.__skrlRecordingOn) return;
                        var t = e.target;
                        if (t && (t.tagName === 'INPUT' || t.tagName === 'TEXTAREA' || t.isContentEditable)) {
                            window.__skrlGhostMacroEvents.push({
                                actionType: 'INPUT',
                                cssSelector: getSmartSelector(t),
                                valueOrText: (t.value || t.innerText || '').slice(0, 500),
                                scrollY: Math.round(window.scrollY || 0)
                            });
                        }
                    }, true);
                }
                window.__skrlRecordingOn = true;
                return 'REC_STARTED';
            })();
        """.trimIndent()

        webView.post {
            webView.evaluateJavascript(recorderJs, null)
        }
    }

    /**
     * Stops recording and saves the captured user actions as a reusable Ghost Macro Blueprint.
     */
    fun stopAndSaveMacro(
        context: Context,
        webView: WebView?,
        customTitle: String = ""
    ) {
        _isRecordingMacro.value = false
        if (webView == null) return

        val harvestJs = """
            (function() {
                window.__skrlRecordingOn = false;
                var evs = window.__skrlGhostMacroEvents || [];
                return JSON.stringify(evs);
            })();
        """.trimIndent()

        webView.post {
            webView.evaluateJavascript(harvestJs) { raw ->
                val cleanJson = unescapeJsString(raw)
                val steps = mutableListOf<GhostMacroStep>()
                try {
                    if (cleanJson.startsWith("[")) {
                        val arr = JSONArray(cleanJson)
                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)
                            steps.add(
                                GhostMacroStep(
                                    actionType = obj.optString("actionType", "CLICK"),
                                    cssSelector = obj.optString("cssSelector", "body"),
                                    valueOrText = obj.optString("valueOrText", ""),
                                    scrollY = obj.optInt("scrollY", 0)
                                )
                            )
                        }
                    }
                } catch (_: Exception) {
                }

                if (steps.isEmpty()) {
                    steps.add(
                        GhostMacroStep(
                            actionType = "CLICK",
                            cssSelector = "button, [type=\"submit\"]",
                            valueOrText = "Auto Action",
                            scrollY = 200
                        )
                    )
                }

                val urlNow = webView.url ?: "https://web"
                val title = customTitle.ifBlank {
                    "Ghost Macro #${_savedMacros.value.size + 1} (${urlNow.substringAfter("://").substringBefore("/")})"
                }
                val bp = GhostMacroBlueprint(
                    title = title,
                    targetDomainOrUrl = urlNow,
                    steps = steps
                )
                _savedMacros.value = listOf(bp) + _savedMacros.value
                saveMacros(context)
                _statusBanner.value = "✅ ঘোস্ট ম্যাক্রো '${bp.title}' সেভ হয়েছে (${steps.size}টি অ্যাকশন ধাপ)!"
            }
        }
    }

    /**
     * Replays a saved Ghost Macro Blueprint 1x or in a multi-round loop.
     */
    fun replayMacro(
        webView: WebView?,
        macro: GhostMacroBlueprint,
        repeatRounds: Int = 1
    ) {
        if (webView == null) return
        replayJob?.cancel()
        replayJob = scope.launch {
            _isReplayingMacro.value = true
            val rounds = repeatRounds.coerceIn(1, 50)
            for (r in 1..rounds) {
                _statusBanner.value = "👻 ম্যাক্রো রিপ্লে চলছে: '${macro.title}' (রাউন্ড $r/$rounds)..."
                for ((idx, step) in macro.steps.withIndex()) {
                    if (!_isReplayingMacro.value) break
                    executeSingleMacroStep(webView, step)
                    _statusBanner.value = "👻 ধাপ ${idx + 1}/${macro.steps.size}: ${step.actionType} -> ${step.cssSelector}"
                    delay(850L)
                }
                delay(700L)
            }
            _isReplayingMacro.value = false
            _statusBanner.value = "✅ ঘোস্ট ম্যাক্রো '${macro.title}' সফলভাবে সম্পন্ন হয়েছে!"
        }
    }

    fun stopMacroReplay() {
        replayJob?.cancel()
        _isReplayingMacro.value = false
        _statusBanner.value = "⏹ ম্যাক্রো রিপ্লে থামানো হয়েছে।"
    }

    fun deleteMacro(context: Context, macroId: String) {
        _savedMacros.value = _savedMacros.value.filterNot { it.id == macroId }
        saveMacros(context)
    }

    private fun executeSingleMacroStep(webView: WebView, step: GhostMacroStep) {
        val b64Val = Base64.encodeToString(step.valueOrText.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        val selEscaped = step.cssSelector.replace("'", "\\'")
        val js = """
            (function() {
                try {
                    if (${step.scrollY} > 0) {
                        window.scrollTo({ top: ${step.scrollY}, behavior: 'smooth' });
                    }
                    var el = document.querySelector('$selEscaped');
                    if (!el) return 'NOT_FOUND';
                    if ('${step.actionType}' === 'INPUT') {
                        var bytes = Uint8Array.from(atob('$b64Val'), function(c) { return c.charCodeAt(0); });
                        var val = new TextDecoder('utf-8').decode(bytes);
                        el.focus();
                        if (el.isContentEditable) {
                            el.innerText = val;
                        } else {
                            el.value = val;
                        }
                        el.dispatchEvent(new Event('input', { bubbles: true }));
                        el.dispatchEvent(new Event('change', { bubbles: true }));
                    } else {
                        el.click();
                    }
                    return 'OK';
                } catch (e) {
                    return 'ERR:' + e.message;
                }
            })();
        """.trimIndent()
        webView.post { webView.evaluateJavascript(js, null) }
    }

    /**
     * Injects the Deep Network Fetch/XHR Interceptor into any loaded WebView page.
     */
    fun injectNetworkSnifferHook(webView: WebView?) {
        if (webView == null || !_isSnifferActive.value) return
        val hookJs = """
            (function() {
                if (window.__skrlNetworkSnifferInstalled) return 'ALREADY_INSTALLED';
                window.__skrlNetworkSnifferInstalled = true;
                window.__skrlCapturedApiLogs = window.__skrlCapturedApiLogs || [];

                function pushApiLog(method, url, status, bodyText) {
                    try {
                        if (!url || url.startsWith('data:')) return;
                        window.__skrlCapturedApiLogs.unshift({
                            method: method || 'GET',
                            url: String(url).slice(0, 240),
                            status: status || 200,
                            preview: String(bodyText || '').slice(0, 900),
                            ts: Date.now()
                        });
                        if (window.__skrlCapturedApiLogs.length > 35) {
                            window.__skrlCapturedApiLogs.pop();
                        }
                    } catch (e) {}
                }

                var origFetch = window.fetch;
                if (origFetch) {
                    window.fetch = function() {
                        var args = arguments;
                        var url = (typeof args[0] === 'string') ? args[0] : (args[0] && args[0].url ? args[0].url : '');
                        var method = (args[1] && args[1].method) ? args[1].method : 'GET';
                        return origFetch.apply(this, args).then(function(response) {
                            try {
                                var clone = response.clone();
                                clone.text().then(function(txt) {
                                    pushApiLog(method, url, response.status, txt);
                                }).catch(function(){});
                            } catch (e) {}
                            return response;
                        });
                    };
                }

                var origOpen = XMLHttpRequest.prototype.open;
                var origSend = XMLHttpRequest.prototype.send;
                XMLHttpRequest.prototype.open = function(method, url) {
                    this.__skrlMethod = method;
                    this.__skrlUrl = url;
                    return origOpen.apply(this, arguments);
                };
                XMLHttpRequest.prototype.send = function() {
                    var xhr = this;
                    xhr.addEventListener('load', function() {
                        try {
                            pushApiLog(xhr.__skrlMethod || 'XHR', xhr.__skrlUrl || '', xhr.status, xhr.responseText || '');
                        } catch (e) {}
                    });
                    return origSend.apply(this, arguments);
                };
                return 'SNIFFER_READY';
            })();
        """.trimIndent()

        webView.post {
            webView.evaluateJavascript(hookJs, null)
        }
    }

    /**
     * Pulls the latest intercepted fetch/XHR API packets from the active WebView.
     */
    fun refreshCapturedApiPackets(webView: WebView?) {
        if (webView == null) return
        injectNetworkSnifferHook(webView)
        val pullJs = """
            (function() {
                return JSON.stringify(window.__skrlCapturedApiLogs || []);
            })();
        """.trimIndent()

        webView.post {
            webView.evaluateJavascript(pullJs) { raw ->
                val clean = unescapeJsString(raw)
                val parsed = mutableListOf<CapturedApiPacket>()
                try {
                    if (clean.startsWith("[")) {
                        val arr = JSONArray(clean)
                        for (i in 0 until arr.length()) {
                            val o = arr.getJSONObject(i)
                            parsed.add(
                                CapturedApiPacket(
                                    method = o.optString("method", "GET"),
                                    url = o.optString("url", ""),
                                    status = o.optInt("status", 200),
                                    responsePreview = o.optString("preview", ""),
                                    timestamp = o.optLong("ts", System.currentTimeMillis())
                                )
                            )
                        }
                    }
                } catch (_: Exception) {
                }
                if (parsed.isNotEmpty()) {
                    _capturedPackets.value = parsed
                    _statusBanner.value = "📡 ${parsed.size}টি হিডেন নেটওয়ার্ক API প্যাকেট ক্যাপচার করা হয়েছে!"
                } else {
                    _statusBanner.value = "📡 স্নিফার চালু আছে — ওয়েবসাইটে যেকোনো অ্যাকশন নিলেই গোপন API কল এখানে ধরা পড়বে।"
                }
            }
        }
    }

    fun clearCapturedPackets() {
        _capturedPackets.value = emptyList()
    }

    private fun unescapeJsString(raw: String?): String {
        if (raw.isNullOrBlank() || raw == "null") return ""
        return raw.removeSurrounding("\"")
            .replace("\\n", "\n")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
    }

    private fun loadMacros(context: Context) {
        try {
            val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_MACROS_JSON, null)
            val list = mutableListOf<GhostMacroBlueprint>()
            if (!raw.isNullOrBlank()) {
                val arr = JSONArray(raw)
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    val stepsArr = o.optJSONArray("steps") ?: JSONArray()
                    val steps = mutableListOf<GhostMacroStep>()
                    for (j in 0 until stepsArr.length()) {
                        val s = stepsArr.getJSONObject(j)
                        steps.add(
                            GhostMacroStep(
                                actionType = s.optString("actionType", "CLICK"),
                                cssSelector = s.optString("cssSelector", "body"),
                                valueOrText = s.optString("valueOrText", ""),
                                scrollY = s.optInt("scrollY", 0)
                            )
                        )
                    }
                    list.add(
                        GhostMacroBlueprint(
                            id = o.optString("id", UUID.randomUUID().toString()),
                            title = o.optString("title", "Macro"),
                            targetDomainOrUrl = o.optString("targetDomainOrUrl", ""),
                            steps = steps,
                            createdAt = o.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }
            _savedMacros.value = list
        } catch (_: Exception) {
        }
    }

    private fun saveMacros(context: Context) {
        try {
            val arr = JSONArray()
            _savedMacros.value.forEach { bp ->
                val stepsArr = JSONArray()
                bp.steps.forEach { s ->
                    stepsArr.put(
                        JSONObject().apply {
                            put("actionType", s.actionType)
                            put("cssSelector", s.cssSelector)
                            put("valueOrText", s.valueOrText)
                            put("scrollY", s.scrollY)
                        }
                    )
                }
                arr.put(
                    JSONObject().apply {
                        put("id", bp.id)
                        put("title", bp.title)
                        put("targetDomainOrUrl", bp.targetDomainOrUrl)
                        put("steps", stepsArr)
                        put("createdAt", bp.createdAt)
                    }
                )
            }
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_MACROS_JSON, arr.toString())
                .apply()
        } catch (_: Exception) {
        }
    }
}
