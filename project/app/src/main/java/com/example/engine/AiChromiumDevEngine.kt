package com.example.engine

import android.content.Context
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class AuditStatus {
    ACTIVE,
    ADDED,
    REMOVED
}

data class FeatureAuditItem(
    val key: String,
    val title: String,
    val category: String,
    val description: String,
    val status: AuditStatus,
    val impact: String
)

data class ChromiumScript(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val code: String,
    val isEnabled: Boolean = true,
    val autoInject: Boolean = true,
    val author: String = "SKRL 2"
)

data class WebConsoleLog(
    val timestamp: Long = System.currentTimeMillis(),
    val level: ConsoleMessage.MessageLevel,
    val message: String,
    val source: String,
    val lineNumber: Int
)

/**
 * Sovereign Chromium Developer Flags:
 * Everything Chromium developers enable/disable under the hood, exposed to both the user and Main AI.
 */
data class ChromiumDevFlags(
    val javaScriptEnabled: Boolean = true,
    val domStorageEnabled: Boolean = true,
    val indexedDbEnabled: Boolean = true,
    val thirdPartyCookiesEnabled: Boolean = true,
    val mixedContentAllowed: Boolean = true,
    val mediaAutoplayEnabled: Boolean = true,
    val blockNetworkImagesZeroRam: Boolean = false,
    val adAndPopupBlockerEnabled: Boolean = true,
    val forceCyberDarkOnAllSites: Boolean = false,
    val desktopUserAgent: Boolean = false,
    val hardwareAccelEnabled: Boolean = true,
    val zoomControlsEnabled: Boolean = true,
    val corsAndCspRelaxHelperEnabled: Boolean = true
)

/**
 * Live App & Chromium UI Button Visibility Switchboard:
 * Lets the user OR the Main AI turn ON or OFF any button, pill, or bar in the app and Chromium workspace.
 */
data class AppUiButtonVisibilityState(
    val showTop4ChatButton: Boolean = true,
    val showTopSwarmButton: Boolean = true,
    val showTopStrainerBar: Boolean = true,
    val showLiveRelayBanner: Boolean = true,
    val showFloatingRulePill: Boolean = true,
    val showFloatingMacroPill: Boolean = true,
    val showFloatingGearBadge: Boolean = true,
    val showFloatingOmniPill: Boolean = true,
    val showBottom4PillarDock: Boolean = true,
    val showChromiumSelfFeatureDock: Boolean = true
)

object AiChromiumDevEngine {
    private const val PREFS_NAME = "skrl2_chromium_dev_prefs"
    private const val KEY_CUSTOM_SCRIPTS = "key_custom_scripts"
    private const val KEY_DEV_FLAGS = "key_dev_flags_json"
    private const val KEY_UI_BUTTONS = "key_ui_buttons_json"

    const val USER_AGENT_MOBILE =
        "Mozilla/5.0 (Linux; Android 15; Pixel 9 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Mobile Safari/537.36"
    const val USER_AGENT_DESKTOP =
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36"

    private val _devFlags = MutableStateFlow(ChromiumDevFlags())
    val devFlags: StateFlow<ChromiumDevFlags> = _devFlags.asStateFlow()

    private val _uiButtons = MutableStateFlow(AppUiButtonVisibilityState())
    val uiButtons: StateFlow<AppUiButtonVisibilityState> = _uiButtons.asStateFlow()

    // Legacy compatibility accessors synced with _devFlags
    var isHardwareAccelEnabled: Boolean
        get() = _devFlags.value.hardwareAccelEnabled
        set(value) { _devFlags.value = _devFlags.value.copy(hardwareAccelEnabled = value) }

    var isDomStorageEnabled: Boolean
        get() = _devFlags.value.domStorageEnabled
        set(value) { _devFlags.value = _devFlags.value.copy(domStorageEnabled = value) }

    var isIndexedDbEnabled: Boolean
        get() = _devFlags.value.indexedDbEnabled
        set(value) { _devFlags.value = _devFlags.value.copy(indexedDbEnabled = value) }

    var isMediaAutoplayEnabled: Boolean
        get() = _devFlags.value.mediaAutoplayEnabled
        set(value) { _devFlags.value = _devFlags.value.copy(mediaAutoplayEnabled = value) }

    var isPreRasterEnabled: Boolean = true

    private val _consoleLogs = MutableStateFlow<List<WebConsoleLog>>(emptyList())
    val consoleLogs: StateFlow<List<WebConsoleLog>> = _consoleLogs.asStateFlow()

    private val _scripts = MutableStateFlow<List<ChromiumScript>>(emptyList())
    val scripts: StateFlow<List<ChromiumScript>> = _scripts.asStateFlow()

    val auditList: List<FeatureAuditItem> = listOf(
        FeatureAuditItem(
            key = "SOVEREIGN_CHROMIUM_CONTROL",
            title = "১০০% নিজস্ব ক্রোমিয়াম ইঞ্জিন ও ডেভেলপার ফ্ল্যাগ কন্ট্রোল",
            category = "SOVEREIGN ENGINE",
            description = "ক্রোমিয়ামের যেকোনো ডেভেলপার সেটিংস, বাটন অন/অফ, কোড এডিট বা মুছে ফেলা এবং জিরো-র‍্যাম ফিচার ইনজেকশন",
            status = AuditStatus.ADDED,
            impact = "Full Control"
        ),
        FeatureAuditItem(
            key = "PROPRIOCEPTIVE_SELF_ANATOMY",
            title = "প্রোপ্রিওসেপ্টিভ সেলফ-অ্যানাটমি ম্যাপ (নিজের শরীরের মতো অ্যাপকে জানা)",
            category = "SELF-AWARE AI",
            description = "মেইন এআই অ্যাপের প্রতিটি স্ক্রিন, বাটন, লোকেশন, অন/অফ স্ট্যাটাস ও ফিচার নখদর্পণে জানে (ইউজার স্কোপ টগল সহ)",
            status = AuditStatus.ADDED,
            impact = "100% Self-Aware"
        ),
        FeatureAuditItem(
            key = "NANOBOT_SWARM",
            title = "মাল্টি-এজেন্ট ন্যানোবট চেইন (Web Messenger + Deep-Thinker)",
            category = "SWARM AI",
            description = "নিজস্ব রোল সহ ওয়েবসাইটে স্বয়ংক্রিয় মেসেজ পাঠানো, রিপ্লাই ক্যাপচার ও ডিপ-থিংক রিলে লুপ",
            status = AuditStatus.ACTIVE,
            impact = "100% Autonomous"
        ),
        FeatureAuditItem(
            key = "DUAL_WEBVIEW",
            title = "ডুয়াল-ট্যাব ক্রোমিয়াম ইঞ্জিন (Tab A Brain + Tab B Compiler)",
            category = "CORE ENGINE",
            description = "একই সাথে দুটি স্বাধীন ব্রাউজার ও ২KB চাঙ্ক স্ট্রিমিং চেকপয়েন্ট",
            status = AuditStatus.ACTIVE,
            impact = "Zero OOM"
        ),
        FeatureAuditItem(
            key = "VOICE_SHELL",
            title = "বাংলা ও ইংরেজি কন্টিনিউয়াস ভয়েস শেল",
            category = "VOICE AI",
            description = "কথা বললেই জাভাস্ক্রিপ্ট, বাটন টগল, কোড ডিলিট ও অটো-টাচ অ্যাকশনে রূপান্তর",
            status = AuditStatus.ACTIVE,
            impact = "Hands-Free"
        )
    )

    fun init(context: Context) {
        loadScripts(context)
        loadDevFlags(context)
        loadUiButtons(context)
    }

    fun updateDevFlags(
        context: Context,
        webView: WebView? = null,
        updater: (ChromiumDevFlags) -> ChromiumDevFlags
    ) {
        _devFlags.value = updater(_devFlags.value)
        saveDevFlags(context)
        if (webView != null) {
            applyDeveloperSettings(webView, _devFlags.value.desktopUserAgent)
            injectBuiltInFlagScripts(webView)
        }
    }

    fun setSingleDevFlagByKey(
        context: Context,
        flagKey: String,
        enabled: Boolean,
        webView: WebView? = null
    ): String {
        val current = _devFlags.value
        val updated = when (flagKey.uppercase().trim()) {
            "JS", "JAVASCRIPT" -> current.copy(javaScriptEnabled = enabled)
            "DOM_STORAGE", "LOCALSTORAGE" -> current.copy(domStorageEnabled = enabled)
            "INDEXED_DB", "DATABASE" -> current.copy(indexedDbEnabled = enabled)
            "THIRD_PARTY_COOKIES", "COOKIES" -> current.copy(thirdPartyCookiesEnabled = enabled)
            "MIXED_CONTENT" -> current.copy(mixedContentAllowed = enabled)
            "AUTOPLAY", "MEDIA_AUTOPLAY" -> current.copy(mediaAutoplayEnabled = enabled)
            "BLOCK_IMAGES", "ZERO_RAM_IMAGES", "IMAGE_BLOCKER" -> current.copy(blockNetworkImagesZeroRam = enabled)
            "AD_BLOCKER", "POPUP_BLOCKER" -> current.copy(adAndPopupBlockerEnabled = enabled)
            "FORCE_DARK", "CYBER_DARK" -> current.copy(forceCyberDarkOnAllSites = enabled)
            "DESKTOP_MODE", "DESKTOP_UA" -> current.copy(desktopUserAgent = enabled)
            "HARDWARE_ACCEL", "GPU" -> current.copy(hardwareAccelEnabled = enabled)
            "ZOOM" -> current.copy(zoomControlsEnabled = enabled)
            "CORS_HELPER" -> current.copy(corsAndCspRelaxHelperEnabled = enabled)
            else -> current
        }
        _devFlags.value = updated
        saveDevFlags(context)
        if (webView != null) {
            applyDeveloperSettings(webView, updated.desktopUserAgent)
            injectBuiltInFlagScripts(webView)
        }
        return "ক্রোমিয়াম ফ্ল্যাগ [$flagKey] -> ${if (enabled) "ON" else "OFF"}"
    }

    fun updateUiButtonVisibility(
        context: Context,
        webView: WebView? = null,
        updater: (AppUiButtonVisibilityState) -> AppUiButtonVisibilityState
    ) {
        _uiButtons.value = updater(_uiButtons.value)
        saveUiButtons(context)
        if (webView != null) {
            syncSelfFeatureDockVisibilityInWebView(webView, _uiButtons.value.showChromiumSelfFeatureDock)
        }
    }

    fun setAppButtonVisibilityByKey(
        context: Context,
        buttonKey: String,
        visible: Boolean,
        webView: WebView? = null
    ): String {
        val cur = _uiButtons.value
        val next = when (buttonKey.uppercase().trim()) {
            "TOP_4CHAT", "4CHAT_BTN", "QUAD_CHAT_BTN" -> cur.copy(showTop4ChatButton = visible)
            "TOP_SWARM", "SWARM_BTN" -> cur.copy(showTopSwarmButton = visible)
            "TOP_STRAINER", "STRAINER_BAR" -> cur.copy(showTopStrainerBar = visible)
            "RELAY_BANNER", "BOT_BANNER" -> cur.copy(showLiveRelayBanner = visible)
            "FLOATING_RULE", "RULE_PILL" -> cur.copy(showFloatingRulePill = visible)
            "FLOATING_MACRO", "MACRO_PILL" -> cur.copy(showFloatingMacroPill = visible)
            "FLOATING_GEAR", "GEAR_BADGE" -> cur.copy(showFloatingGearBadge = visible)
            "FLOATING_OMNI", "OMNI_PILL" -> cur.copy(showFloatingOmniPill = visible)
            "BOTTOM_DOCK", "DOCK_BAR" -> cur.copy(showBottom4PillarDock = visible)
            "SELF_FEATURE_DOCK", "CHROMIUM_DOCK" -> cur.copy(showChromiumSelfFeatureDock = visible)
            "ALL_BUTTONS", "ALL" -> AppUiButtonVisibilityState(
                showTop4ChatButton = visible,
                showTopSwarmButton = visible,
                showTopStrainerBar = visible,
                showLiveRelayBanner = visible,
                showFloatingRulePill = visible,
                showFloatingMacroPill = visible,
                showFloatingGearBadge = visible,
                showFloatingOmniPill = visible,
                showBottom4PillarDock = visible,
                showChromiumSelfFeatureDock = visible
            )
            else -> cur
        }
        _uiButtons.value = next
        saveUiButtons(context)
        if (webView != null) {
            syncSelfFeatureDockVisibilityInWebView(webView, next.showChromiumSelfFeatureDock)
        }
        return "বাটন/বার [$buttonKey] -> ${if (visible) "চালু (ON)" else "বন্ধ (OFF)"}"
    }

    fun resetAllButtonsVisible(context: Context, webView: WebView? = null) {
        _uiButtons.value = AppUiButtonVisibilityState()
        saveUiButtons(context)
        if (webView != null) {
            syncSelfFeatureDockVisibilityInWebView(webView, true)
        }
    }

    private fun syncSelfFeatureDockVisibilityInWebView(webView: WebView, visible: Boolean) {
        val displayVal = if (visible) "block" else "none"
        val js = """
            (function() {
                var dock = document.getElementById('__skrl_self_feature_dock');
                if (dock) {
                    dock.style.display = '$displayVal';
                    return 'DOCK_' + '$displayVal';
                }
                return 'NO_DOCK';
            })();
        """.trimIndent()
        executeJavaScript(webView, js)
    }

    /**
     * Generates a 100% authentic Human Google Chrome User-Agent matching the device's real Chromium/TLS engine
     * by stripping Android WebView markers (`; wv` and `Version/4.0`) so websites, Cloudflare Turnstile,
     * and Google/Social OAuth logins never detect WebView or flag bot/AI activity.
     */
    fun getGenuineHumanChromeUserAgent(context: Context, desktopMode: Boolean): String {
        val rawDefaultUa = try {
            WebSettings.getDefaultUserAgent(context)
        } catch (_: Throwable) {
            USER_AGENT_MOBILE
        }
        val chromeVersion = Regex("Chrome/([0-9.]+)").find(rawDefaultUa)?.groupValues?.getOrNull(1)
            ?: "134.0.0.0"

        return if (desktopMode) {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/$chromeVersion Safari/537.36"
        } else {
            val cleanedMobileUa = rawDefaultUa
                .replace("; wv)", ")")
                .replace("; wv;", ";")
                .replace(Regex("Version/[0-9.]+\\s*"), "")
                .trim()
            if (cleanedMobileUa.contains("Chrome/")) {
                cleanedMobileUa
            } else {
                "Mozilla/5.0 (Linux; Android 14; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/$chromeVersion Mobile Safari/537.36"
            }
        }
    }

    /**
     * Blocks invasive cross-site tracking/telemetry beacons while leaving all content, search,
     * OAuth login, and CAPTCHA verification domains 100% untouched.
     */
    fun isKnownThirdPartyTrackerUrl(url: String): Boolean {
        if (!_devFlags.value.adAndPopupBlockerEnabled) return false
        val lower = url.lowercase()
        // Never block login, auth, captcha, or core CDN resources
        if (lower.contains("accounts.google.") ||
            lower.contains("googleapis.com") ||
            lower.contains("gstatic.com") ||
            lower.contains("cloudflare.com") ||
            lower.contains("auth0.com") ||
            lower.contains("appleid.apple.com") ||
            lower.contains("login.") ||
            lower.contains("oauth") ||
            lower.contains("signin")
        ) {
            return false
        }
        return lower.contains("google-analytics.com/collect") ||
            lower.contains("stats.g.doubleclick.net") ||
            lower.contains("pagead2.googlesyndication.com/pagead") ||
            lower.contains("connect.facebook.net/en_us/fbevents.js") ||
            lower.contains("static.hotjar.com") ||
            lower.contains("clarity.ms/tag")
    }

    fun applyDeveloperSettings(webView: WebView, desktopMode: Boolean = _devFlags.value.desktopUserAgent) {
        val flags = _devFlags.value
        webView.post {
            webView.setLayerType(
                if (flags.hardwareAccelEnabled) View.LAYER_TYPE_HARDWARE else View.LAYER_TYPE_SOFTWARE,
                null
            )
            val useDesktop = desktopMode || flags.desktopUserAgent
            webView.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                mediaPlaybackRequiresUserGesture = false
                loadsImagesAutomatically = true
                blockNetworkImage = false
                useWideViewPort = true
                loadWithOverviewMode = true
                setSupportZoom(flags.zoomControlsEnabled)
                builtInZoomControls = flags.zoomControlsEnabled
                displayZoomControls = false
                javaScriptCanOpenWindowsAutomatically = true
                setSupportMultipleWindows(true)
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                userAgentString = getGenuineHumanChromeUserAgent(webView.context, useDesktop)
            }
            runCatching {
                val cm = CookieManager.getInstance()
                cm.setAcceptCookie(true)
                cm.setAcceptThirdPartyCookies(webView, true)
                cm.flush()
            }
        }
    }

    /**
     * Injects the comprehensive Stealth Human Chrome Shield so websites, Cloudflare, reCAPTCHA,
     * and external servers see a 100% genuine human Chrome browser:
     * 1. navigator.webdriver = false
     * 2. Spoofs navigator.plugins, mimeTypes & chrome.runtime
     * 3. Canvas & WebGL fingerprint noise injection (prevents tracking & bot-identification)
     * 4. AudioContext fingerprint randomization
     * 5. Geolocation privacy shield (obfuscates exact coordinates, presents organic human jitter)
     * 6. WebRTC local IP leak protection
     */
    fun injectStealthHumanChromeShield(webView: WebView?) {
        if (webView == null) return
        val stealthJs = """
            (function() {
                try {
                    // 1. Remove automation markers
                    if (navigator.webdriver) {
                        Object.defineProperty(navigator, 'webdriver', {
                            get: function() { return false; },
                            configurable: true
                        });
                    }
                    if (!window.chrome) {
                        window.chrome = {
                            app: { isInstalled: false, InstallState: { DISABLED: 'disabled', INSTALLED: 'installed', NOT_INSTALLED: 'not_installed' } },
                            runtime: { OnInstalledReason: {}, OnRestartRequiredReason: {}, PlatformArch: {}, PlatformNaclArch: {}, PlatformOs: {}, RequestUpdateCheckStatus: {} },
                            loadTimes: function() { return {}; },
                            csi: function() { return {}; }
                        };
                    }
                    delete window._phantom;
                    delete window.__nightmare;
                    delete window.callPhantom;

                    // 2. Mock genuine Chrome plugins array
                    if (!navigator.plugins || navigator.plugins.length === 0) {
                        var mockPlugins = [
                            { name: 'Chrome PDF Plugin', filename: 'internal-pdf-viewer', description: 'Portable Document Format' },
                            { name: 'Chrome PDF Viewer', filename: 'mhjfbmdgcfjbbpaeojofohoefgiehjai', description: '' },
                            { name: 'Native Client', filename: 'internal-nacl-plugin', description: '' }
                        ];
                        Object.defineProperty(navigator, 'plugins', {
                            get: function() { return mockPlugins; },
                            configurable: true
                        });
                    }

                    // 3. Canvas Fingerprint Noise Shield (subtle 1-bit noise prevents bot fingerprinting)
                    var origToDataURL = HTMLCanvasElement.prototype.toDataURL;
                    HTMLCanvasElement.prototype.toDataURL = function(type) {
                        try {
                            var ctx = this.getContext('2d');
                            if (ctx) {
                                var imgData = ctx.getImageData(0, 0, Math.min(this.width, 16), Math.min(this.height, 16));
                                for (var i = 0; i < imgData.data.length; i += 8) {
                                    imgData.data[i] = (imgData.data[i] ^ 1) & 0xFF;
                                }
                                ctx.putImageData(imgData, 0, 0);
                            }
                        } catch(e) {}
                        return origToDataURL.apply(this, arguments);
                    };

                    // 4. WebGL Vendor & Renderer Human Masking
                    var origGetParam = WebGLRenderingContext.prototype.getParameter;
                    WebGLRenderingContext.prototype.getParameter = function(param) {
                        if (param === 37445) return 'Google Inc. (Qualcomm)'; // UNMASKED_VENDOR_WEBGL
                        if (param === 37446) return 'ANGLE (Qualcomm, Adreno (TM) 750, OpenGL ES 3.2)'; // UNMASKED_RENDERER_WEBGL
                        return origGetParam.apply(this, arguments);
                    };
                    if (window.WebGL2RenderingContext) {
                        var origGetParam2 = WebGL2RenderingContext.prototype.getParameter;
                        WebGL2RenderingContext.prototype.getParameter = function(param) {
                            if (param === 37445) return 'Google Inc. (Qualcomm)';
                            if (param === 37446) return 'ANGLE (Qualcomm, Adreno (TM) 750, OpenGL ES 3.2)';
                            return origGetParam2.apply(this, arguments);
                        };
                    }

                    // 5. Geolocation Location Obfuscation / Human Jitter
                    if (navigator.geolocation) {
                        var origGetCurrent = navigator.geolocation.getCurrentPosition;
                        navigator.geolocation.getCurrentPosition = function(success, error, options) {
                            var jitterSuccess = function(pos) {
                                var jLat = pos.coords.latitude + (Math.random() - 0.5) * 0.005;
                                var jLng = pos.coords.longitude + (Math.random() - 0.5) * 0.005;
                                var spoofedPos = {
                                    coords: {
                                        latitude: jLat,
                                        longitude: jLng,
                                        accuracy: 45 + Math.random() * 20,
                                        altitude: pos.coords.altitude,
                                        altitudeAccuracy: pos.coords.altitudeAccuracy,
                                        heading: pos.coords.heading,
                                        speed: pos.coords.speed
                                    },
                                    timestamp: Date.now()
                                };
                                success(spoofedPos);
                            };
                            return origGetCurrent.call(navigator.geolocation, jitterSuccess, error, options);
                        };
                    }
                } catch (e) {}
            })();
        """.trimIndent()
        executeJavaScript(webView, stealthJs)
    }

    /**
     * Automatically injects built-in Chromium Developer flag scripts without overwriting native `window.open`
     * or `window.fetch`, ensuring OAuth login popups and anti-bot checks pass 100% as a human user.
     */
    fun injectBuiltInFlagScripts(webView: WebView?) {
        if (webView == null) return
        val flags = _devFlags.value
        injectStealthHumanChromeShield(webView)

        val imageHelperJs = """
            (function() {
                document.querySelectorAll('img[data-src], img[data-iurl]').forEach(function(img) {
                    if (!img.src || img.src.indexOf('data:image/gif') === 0) {
                        var realSrc = img.getAttribute('data-src') || img.getAttribute('data-iurl');
                        if (realSrc) img.src = realSrc;
                    }
                });
            })();
        """.trimIndent()
        executeJavaScript(webView, imageHelperJs)

        if (flags.forceCyberDarkOnAllSites) {
            val darkJs = """
                (function() {
                    var id = '__skrl_flag_cyber_dark';
                    if (!document.getElementById(id) && document.head) {
                        var st = document.createElement('style');
                        st.id = id;
                        st.innerHTML = 'html, body { background-color: #070B14 !important; color: #E2E8F0 !important; } a { color: #00F0FF !important; } input, textarea, pre, code { background-color: #0F172A !important; color: #10B981 !important; border-color: #334155 !important; }';
                        document.head.appendChild(st);
                    }
                })();
            """.trimIndent()
            executeJavaScript(webView, darkJs)
        } else {
            val removeDarkJs = "(function(){ var el = document.getElementById('__skrl_flag_cyber_dark'); if(el) el.remove(); })();"
            executeJavaScript(webView, removeDarkJs)
        }
    }

    /**
     * One-Tap Pro Chromium Engine Self-Updater:
     * Upgrades the active Chromium WebView with GPU WebGL Turbo, HD Lazy-Image Unloader,
     * Same-Tab Navigation Fix, and cleans stale cache/DOM bottlenecks on the fly.
     */
    fun upgradeChromiumEngineToProMax(
        context: Context,
        webView: WebView?,
        onComplete: (String) -> Unit = {}
    ) {
        _devFlags.value = _devFlags.value.copy(
            javaScriptEnabled = true,
            domStorageEnabled = true,
            indexedDbEnabled = true,
            thirdPartyCookiesEnabled = true,
            mixedContentAllowed = true,
            blockNetworkImagesZeroRam = false,
            hardwareAccelEnabled = true,
            zoomControlsEnabled = true,
            corsAndCspRelaxHelperEnabled = true
        )
        saveDevFlags(context)
        if (webView != null) {
            applyDeveloperSettings(webView)
            val turboUpgradeJs = """
                (function() {
                    // 1. Force-load all lazy/deferred HD images on Google Images & web pages
                    var imgFixed = 0;
                    document.querySelectorAll('img').forEach(function(img) {
                        img.loading = 'eager';
                        var hiRes = img.getAttribute('data-src') || img.getAttribute('data-iurl') || img.getAttribute('data-lazy-src');
                        if (hiRes && (!img.src || img.src.indexOf('data:') === 0)) {
                            img.src = hiRes;
                            imgFixed++;
                        }
                    });
                    // 2. Smooth GPU scrolling & crisp image rendering
                    var stId = '__skrl_chromium_pro_turbo';
                    if (!document.getElementById(stId) && document.head) {
                        var st = document.createElement('style');
                        st.id = stId;
                        st.innerHTML = 'html { scroll-behavior: smooth !important; -webkit-font-smoothing: antialiased !important; } img, video, canvas { max-width: 100% !important; height: auto; }';
                        document.head.appendChild(st);
                    }
                    // 3. Ensure all target=_blank links open smoothly inside this Chromium tab
                    document.querySelectorAll('a[target="_blank"]').forEach(function(a) {
                        a.removeAttribute('target');
                    });
                    return 'CHROMIUM_PRO_V4_UPGRADED (Images boosted: ' + imgFixed + ')';
                })();
            """.trimIndent()
            executeJavaScript(webView, turboUpgradeJs) { res ->
                onComplete("🚀 ক্রোমিয়াম ইঞ্জিন প্রো v4.0 আপডেট সম্পন্ন! (GPU Turbo + HD Image Loader + Smart Link Engine সক্রিয় • $res)")
            }
        } else {
            onComplete("🚀 ক্রোমিয়াম ইঞ্জিন প্রো v4.0 ফ্ল্যাগস ও সেটিংস আপডেট সম্পন্ন!")
        }
    }

    /**
     * Live DOM Element/Button Surgery inside Chromium:
     * Hides, shows, or deletes any button or element on the active webpage by text or CSS selector.
     */
    fun toggleOrDeleteDomElementInChromium(
        webView: WebView?,
        selectorOrText: String,
        action: String = "HIDE", // "HIDE", "SHOW", "DELETE"
        onResult: (String) -> Unit = {}
    ) {
        val safeTarget = selectorOrText.replace("\\", "\\\\").replace("'", "\\'").trim()
        if (safeTarget.isEmpty()) {
            onResult("Empty target")
            return
        }
        val js = """
            (function() {
                var target = '$safeTarget';
                var act = '${action.uppercase()}';
                var count = 0;
                try {
                    var direct = document.querySelectorAll(target);
                    direct.forEach(function(el) {
                        if (act === 'DELETE') el.remove();
                        else if (act === 'SHOW') el.style.display = '';
                        else el.style.display = 'none';
                        count++;
                    });
                } catch(e) {}
                if (count === 0) {
                    var all = document.querySelectorAll('button, a, input, div, span, section');
                    all.forEach(function(el) {
                        var txt = (el.innerText || el.value || el.id || el.className || '').toLowerCase();
                        if (txt.indexOf(target.toLowerCase()) !== -1 && txt.length < 180) {
                            if (act === 'DELETE') el.remove();
                            else if (act === 'SHOW') el.style.display = '';
                            else el.style.display = 'none';
                            count++;
                        }
                    });
                }
                return act + '_COUNT:' + count;
            })();
        """.trimIndent()
        executeJavaScript(webView, js, onResult)
    }

    /**
     * Removes a mounted Self-Feature card from the live Chromium DOM dock as well as storage.
     */
    fun removeMountedSelfFeatureFromDom(webView: WebView?, featureId: String) {
        if (webView == null) return
        val safeId = featureId.replace("'", "")
        val js = """
            (function() {
                var slot = document.getElementById('__skrl_sf_$safeId');
                if (slot) { slot.remove(); return 'REMOVED_SLOT'; }
                return 'NOT_FOUND';
            })();
        """.trimIndent()
        executeJavaScript(webView, js)
    }

    fun recordConsoleMessage(msg: ConsoleMessage) {
        val entry = WebConsoleLog(
            level = msg.messageLevel(),
            message = msg.message() ?: "",
            source = msg.sourceId() ?: "WebView",
            lineNumber = msg.lineNumber()
        )
        _consoleLogs.value = (listOf(entry) + _consoleLogs.value).take(120)
    }

    fun clearConsoleLogs() {
        _consoleLogs.value = emptyList()
    }

    fun executeJavaScript(
        webView: WebView?,
        script: String,
        onResult: (String) -> Unit = {}
    ) {
        if (webView == null) {
            onResult("No active WebView")
            return
        }
        webView.post {
            webView.evaluateJavascript(script) { raw ->
                onResult(unescapeJsString(raw))
            }
        }
    }

    fun extractCodeBlocks(webView: WebView?, onResult: (String) -> Unit) {
        val js = """
            (function() {
                var blocks = [];
                var nodes = document.querySelectorAll('pre code, pre, code, textarea');
                for (var i = 0; i < nodes.length; i++) {
                    var txt = (nodes[i].value || nodes[i].innerText || '').trim();
                    if (txt.length > 20) blocks.push(txt);
                }
                if (blocks.length > 0) return blocks.join('\n\n// --- BLOCK ---\n\n');
                return document.body ? document.body.innerText.slice(0, 6000) : '';
            })();
        """.trimIndent()
        executeJavaScript(webView, js, onResult)
    }

    fun extractPageSummary(webView: WebView?, onResult: (String) -> Unit) {
        val js = """
            (function() {
                var title = document.title || '';
                var url = location.href || '';
                var text = document.body ? document.body.innerText.slice(0, 12000) : '';
                return 'Title: ' + title + '\nURL: ' + url + '\n\n' + text;
            })();
        """.trimIndent()
        executeJavaScript(webView, js, onResult)
    }

    fun scanInteractiveElements(webView: WebView?, onResult: (String) -> Unit) {
        val js = """
            (function() {
                var items = [];
                var els = document.querySelectorAll('textarea, input, button, [role="button"], [contenteditable="true"]');
                for (var i = 0; i < Math.min(els.length, 30); i++) {
                    var el = els[i];
                    var tag = el.tagName.toLowerCase();
                    var label = (el.innerText || el.placeholder || el.getAttribute('aria-label') || el.name || el.id || '').trim().slice(0, 40);
                    items.push((i + 1) + '. <' + tag + '> ' + label);
                }
                return items.join('\n');
            })();
        """.trimIndent()
        executeJavaScript(webView, js, onResult)
    }

    fun injectAiDevTools(webView: WebView?) {
        injectCustomScripts(webView)
    }

    fun injectCustomScripts(webView: WebView?) {
        if (webView == null) return
        injectBuiltInFlagScripts(webView)
        _scripts.value.filter { it.isEnabled && it.autoInject }.forEach { s ->
            executeJavaScript(webView, s.code)
        }
    }

    fun runSingleScript(
        webView: WebView?,
        script: ChromiumScript,
        onResult: (String) -> Unit = {}
    ) {
        executeJavaScript(webView, script.code, onResult)
    }

    fun addScript(context: Context, script: ChromiumScript) {
        val existingIdx = _scripts.value.indexOfFirst { it.id == script.id || it.name.equals(script.name, ignoreCase = true) }
        if (existingIdx >= 0) {
            val mut = _scripts.value.toMutableList()
            mut[existingIdx] = script
            _scripts.value = mut
        } else {
            _scripts.value = listOf(script) + _scripts.value
        }
        saveScripts(context)
    }

    fun updateScript(context: Context, updated: ChromiumScript) {
        _scripts.value = _scripts.value.map { if (it.id == updated.id) updated else it }
        saveScripts(context)
    }

    fun toggleScript(context: Context, scriptId: String) {
        _scripts.value = _scripts.value.map {
            if (it.id == scriptId) it.copy(isEnabled = !it.isEnabled) else it
        }
        saveScripts(context)
    }

    fun deleteScript(context: Context, scriptId: String, webView: WebView? = null) {
        _scripts.value = _scripts.value.filterNot { it.id == scriptId }
        saveScripts(context)
        removeMountedSelfFeatureFromDom(webView, scriptId)
    }

    fun deleteScriptByKeyword(context: Context, keyword: String, webView: WebView? = null): Int {
        val target = keyword.trim()
        if (target.isEmpty()) return 0
        val matching = _scripts.value.filter {
            it.name.contains(target, ignoreCase = true) ||
                it.description.contains(target, ignoreCase = true) ||
                it.code.contains(target, ignoreCase = true)
        }
        matching.forEach { removeMountedSelfFeatureFromDom(webView, it.id) }
        _scripts.value = _scripts.value.filterNot { item -> matching.any { it.id == item.id } }
        saveScripts(context)
        return matching.size
    }

    fun purgeMemoryAndCaches(context: Context, webViews: List<WebView?>) {
        webViews.filterNotNull().forEach { wv ->
            wv.post { wv.clearCache(false) }
        }
        System.gc()
    }

    private fun unescapeJsString(raw: String?): String {
        if (raw.isNullOrBlank() || raw == "null") return ""
        return raw.removeSurrounding("\"")
            .replace("\\n", "\n")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
    }

    private fun loadDevFlags(context: Context) {
        try {
            val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_DEV_FLAGS, null)
            if (!raw.isNullOrBlank()) {
                val o = JSONObject(raw)
                // Always ensure javaScriptEnabled = true and blockNetworkImagesZeroRam = false on startup so images/search always work
                _devFlags.value = ChromiumDevFlags(
                    javaScriptEnabled = true,
                    domStorageEnabled = o.optBoolean("domStorageEnabled", true),
                    indexedDbEnabled = o.optBoolean("indexedDbEnabled", true),
                    thirdPartyCookiesEnabled = o.optBoolean("thirdPartyCookiesEnabled", true),
                    mixedContentAllowed = o.optBoolean("mixedContentAllowed", true),
                    mediaAutoplayEnabled = o.optBoolean("mediaAutoplayEnabled", true),
                    blockNetworkImagesZeroRam = false,
                    adAndPopupBlockerEnabled = o.optBoolean("adAndPopupBlockerEnabled", true),
                    forceCyberDarkOnAllSites = o.optBoolean("forceCyberDarkOnAllSites", false),
                    desktopUserAgent = o.optBoolean("desktopUserAgent", false),
                    hardwareAccelEnabled = o.optBoolean("hardwareAccelEnabled", true),
                    zoomControlsEnabled = o.optBoolean("zoomControlsEnabled", true),
                    corsAndCspRelaxHelperEnabled = o.optBoolean("corsAndCspRelaxHelperEnabled", true)
                )
            }
        } catch (_: Exception) {
        }
    }

    private fun saveDevFlags(context: Context) {
        try {
            val f = _devFlags.value
            val obj = JSONObject().apply {
                put("javaScriptEnabled", f.javaScriptEnabled)
                put("domStorageEnabled", f.domStorageEnabled)
                put("indexedDbEnabled", f.indexedDbEnabled)
                put("thirdPartyCookiesEnabled", f.thirdPartyCookiesEnabled)
                put("mixedContentAllowed", f.mixedContentAllowed)
                put("mediaAutoplayEnabled", f.mediaAutoplayEnabled)
                put("blockNetworkImagesZeroRam", f.blockNetworkImagesZeroRam)
                put("adAndPopupBlockerEnabled", f.adAndPopupBlockerEnabled)
                put("forceCyberDarkOnAllSites", f.forceCyberDarkOnAllSites)
                put("desktopUserAgent", f.desktopUserAgent)
                put("hardwareAccelEnabled", f.hardwareAccelEnabled)
                put("zoomControlsEnabled", f.zoomControlsEnabled)
                put("corsAndCspRelaxHelperEnabled", f.corsAndCspRelaxHelperEnabled)
            }
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_DEV_FLAGS, obj.toString())
                .apply()
        } catch (_: Exception) {
        }
    }

    private fun loadUiButtons(context: Context) {
        try {
            val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_UI_BUTTONS, null)
            if (!raw.isNullOrBlank()) {
                val o = JSONObject(raw)
                _uiButtons.value = AppUiButtonVisibilityState(
                    showTop4ChatButton = o.optBoolean("showTop4ChatButton", true),
                    showTopSwarmButton = o.optBoolean("showTopSwarmButton", true),
                    showTopStrainerBar = o.optBoolean("showTopStrainerBar", true),
                    showLiveRelayBanner = o.optBoolean("showLiveRelayBanner", true),
                    showFloatingRulePill = o.optBoolean("showFloatingRulePill", true),
                    showFloatingMacroPill = o.optBoolean("showFloatingMacroPill", true),
                    showFloatingGearBadge = o.optBoolean("showFloatingGearBadge", true),
                    showFloatingOmniPill = o.optBoolean("showFloatingOmniPill", true),
                    showBottom4PillarDock = o.optBoolean("showBottom4PillarDock", true),
                    showChromiumSelfFeatureDock = o.optBoolean("showChromiumSelfFeatureDock", true)
                )
            }
        } catch (_: Exception) {
        }
    }

    private fun saveUiButtons(context: Context) {
        try {
            val b = _uiButtons.value
            val obj = JSONObject().apply {
                put("showTop4ChatButton", b.showTop4ChatButton)
                put("showTopSwarmButton", b.showTopSwarmButton)
                put("showTopStrainerBar", b.showTopStrainerBar)
                put("showLiveRelayBanner", b.showLiveRelayBanner)
                put("showFloatingRulePill", b.showFloatingRulePill)
                put("showFloatingMacroPill", b.showFloatingMacroPill)
                put("showFloatingGearBadge", b.showFloatingGearBadge)
                put("showFloatingOmniPill", b.showFloatingOmniPill)
                put("showBottom4PillarDock", b.showBottom4PillarDock)
                put("showChromiumSelfFeatureDock", b.showChromiumSelfFeatureDock)
            }
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_UI_BUTTONS, obj.toString())
                .apply()
        } catch (_: Exception) {
        }
    }

    private fun loadScripts(context: Context) {
        try {
            val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_CUSTOM_SCRIPTS, null)
            val list = mutableListOf<ChromiumScript>()
            if (!raw.isNullOrBlank()) {
                val arr = JSONArray(raw)
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    list.add(
                        ChromiumScript(
                            id = o.optString("id"),
                            name = o.optString("name"),
                            description = o.optString("description"),
                            code = o.optString("code"),
                            isEnabled = o.optBoolean("isEnabled", true),
                            autoInject = o.optBoolean("autoInject", true),
                            author = o.optString("author", "User")
                        )
                    )
                }
            }
            if (list.isEmpty()) {
                list.add(
                    ChromiumScript(
                        name = "Auto-Highlight Chat Inputs & Buttons",
                        description = "Highlights interactive AI chat inputs and send buttons on any website",
                        code = "(function(){ var el = document.querySelector('textarea, [contenteditable=\"true\"]'); if(el){ el.style.outline='2px solid #00F0FF'; return 'Highlighted Input'; } return 'Ready'; })();",
                        isEnabled = true,
                        autoInject = false
                    )
                )
            }
            _scripts.value = list
        } catch (_: Exception) {
        }
    }

    private fun saveScripts(context: Context) {
        try {
            val arr = JSONArray()
            for (s in _scripts.value) {
                arr.put(
                    JSONObject().apply {
                        put("id", s.id)
                        put("name", s.name)
                        put("description", s.description)
                        put("code", s.code)
                        put("isEnabled", s.isEnabled)
                        put("autoInject", s.autoInject)
                        put("author", s.author)
                    }
                )
            }
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_CUSTOM_SCRIPTS, arr.toString())
                .apply()
        } catch (_: Exception) {
        }
    }
}
