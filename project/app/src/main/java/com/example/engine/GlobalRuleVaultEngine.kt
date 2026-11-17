package com.example.engine

import android.content.Context
import android.util.Base64
import android.webkit.WebView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class GlobalRuleItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val ruleInstruction: String,
    val category: String = "ALL", // ALL, SEARCH, CODING, DEEP_THINK
    val isEnabled: Boolean = true,
    val autoPasteToWebsites: Boolean = true,
    val autoInjectToBotsAndApi: Boolean = true
)

/**
 * Step 1: Global Permanent Rule Vault & Zero-Touch Auto-Rule Paster Engine.
 * Eliminates the need to repeatedly write or copy-paste rules when switching websites,
 * searching on new platforms, or spawning new Nanobots.
 */
object GlobalRuleVaultEngine {
    private const val PREFS_NAME = "skrl2_global_rule_vault_prefs"
    private const val KEY_RULES_JSON = "key_global_rules_json"
    private const val KEY_MASTER_AUTO_INJECT = "key_master_auto_inject"
    private const val KEY_AUTO_PASTE_ON_WEB_LOAD = "key_auto_paste_on_web_load"
    private const val RULE_SIGNATURE_MARKER = "[GLOBAL_RULE_VAULT_APPLIED]"

    private val _rules = MutableStateFlow<List<GlobalRuleItem>>(emptyList())
    val rules: StateFlow<List<GlobalRuleItem>> = _rules.asStateFlow()

    private val _isMasterAutoRuleEnabled = MutableStateFlow(true)
    val isMasterAutoRuleEnabled: StateFlow<Boolean> = _isMasterAutoRuleEnabled.asStateFlow()

    private val _isAutoPasteOnPageLoadEnabled = MutableStateFlow(false)
    val isAutoPasteOnPageLoadEnabled: StateFlow<Boolean> = _isAutoPasteOnPageLoadEnabled.asStateFlow()

    private val _lastAutoPasteStatus = MutableStateFlow("📜 গ্লোবাল অটো-রুল ভল্ট সক্রিয় (বারবার রুল পেস্ট করার ঝামেলা নেই)")
    val lastAutoPasteStatus: StateFlow<String> = _lastAutoPasteStatus.asStateFlow()

    fun init(context: Context) {
        loadFromPrefs(context)
    }

    fun setMasterAutoRuleEnabled(context: Context, enabled: Boolean) {
        _isMasterAutoRuleEnabled.value = enabled
        saveToPrefs(context)
        _lastAutoPasteStatus.value = if (enabled) {
            "✅ অটো-রুল ইনজেকশন চালু: সব বট, API ও ওয়েবসাইটে স্বয়ংক্রিয়ভাবে রুল যুক্ত হবে।"
        } else {
            "⏸ অটো-রুল ইনজেকশন সাময়িকভাবে বন্ধ করা হয়েছে।"
        }
    }

    fun setAutoPasteOnPageLoad(context: Context, enabled: Boolean) {
        _isAutoPasteOnPageLoadEnabled.value = enabled
        saveToPrefs(context)
        _lastAutoPasteStatus.value = if (enabled) {
            "⚡ ওয়েবসাইট লোড হলেই অটো-রুল বক্সে পেস্ট হবে (Auto-Paste on Load: ON)"
        } else {
            "📜 ওয়েবসাইট অটো-পেস্ট ম্যানুয়াল/বট মোডে আছে।"
        }
    }

    fun addRule(
        context: Context,
        title: String,
        ruleInstruction: String,
        category: String = "ALL",
        autoPasteToWebsites: Boolean = true,
        autoInjectToBotsAndApi: Boolean = true
    ) {
        if (title.isBlank() || ruleInstruction.isBlank()) return
        val newItem = GlobalRuleItem(
            title = title.trim(),
            ruleInstruction = ruleInstruction.trim(),
            category = category,
            isEnabled = true,
            autoPasteToWebsites = autoPasteToWebsites,
            autoInjectToBotsAndApi = autoInjectToBotsAndApi
        )
        _rules.value = listOf(newItem) + _rules.value
        saveToPrefs(context)
        _lastAutoPasteStatus.value = "➕ নতুন স্থায়ী রুল যুক্ত হয়েছে: '${newItem.title}'"
    }

    fun updateRule(context: Context, updated: GlobalRuleItem) {
        _rules.value = _rules.value.map { if (it.id == updated.id) updated else it }
        saveToPrefs(context)
        _lastAutoPasteStatus.value = "✏️ রুল আপডেট হয়েছে: '${updated.title}'"
    }

    fun toggleRule(context: Context, ruleId: String) {
        _rules.value = _rules.value.map {
            if (it.id == ruleId) it.copy(isEnabled = !it.isEnabled) else it
        }
        saveToPrefs(context)
    }

    fun deleteRule(context: Context, ruleId: String) {
        _rules.value = _rules.value.filterNot { it.id == ruleId }
        saveToPrefs(context)
        _lastAutoPasteStatus.value = "🗑️ রুল মুছে ফেলা হয়েছে।"
    }

    /**
     * Returns the compiled block of all enabled Global Rules for Bot/API execution.
     */
    fun buildCompiledRulesBlock(forWebsitePaste: Boolean = false): String {
        if (!_isMasterAutoRuleEnabled.value) return ""
        val activeList = _rules.value.filter {
            it.isEnabled && (if (forWebsitePaste) it.autoPasteToWebsites else it.autoInjectToBotsAndApi)
        }
        if (activeList.isEmpty()) return ""

        return buildString {
            appendLine("--- MANDATORY PERMANENT RULES $RULE_SIGNATURE_MARKER ---")
            activeList.forEachIndexed { index, item ->
                appendLine("${index + 1}. [${item.title}]: ${item.ruleInstruction}")
            }
            appendLine("--- END OF PERMANENT RULES ---")
        }.trim()
    }

    /**
     * Automatically attaches all active Global Rules to any prompt or message
     * so the user never has to paste rules manually.
     */
    fun enrichPromptWithGlobalRules(
        rawPrompt: String,
        forWebsitePaste: Boolean = false
    ): String {
        if (!_isMasterAutoRuleEnabled.value) return rawPrompt
        if (rawPrompt.contains(RULE_SIGNATURE_MARKER)) return rawPrompt

        val compiledRules = buildCompiledRulesBlock(forWebsitePaste = forWebsitePaste)
        if (compiledRules.isBlank()) return rawPrompt

        return if (rawPrompt.isBlank()) {
            compiledRules
        } else {
            "$rawPrompt\n\n$compiledRules"
        }
    }

    /**
     * Automatically pastes active Global Rules (plus optional prompt) into the active WebView's
     * chat input, search box, or contenteditable area, and optionally clicks Send/Submit.
     */
    fun pasteActiveRulesIntoWebView(
        webView: WebView?,
        optionalPrompt: String = "",
        autoSubmit: Boolean = false,
        onComplete: (String) -> Unit = {}
    ) {
        if (webView == null) {
            onComplete("⚠️ কোনো সক্রিয় ব্রাউজার ট্যাব পাওয়া যায়নি।")
            return
        }

        val finalPayload = enrichPromptWithGlobalRules(
            rawPrompt = optionalPrompt,
            forWebsitePaste = true
        )

        if (finalPayload.isBlank()) {
            onComplete("⚠️ পেস্ট করার মতো কোনো সক্রিয় রুল বা মেসেজ নেই।")
            return
        }

        val b64Payload = Base64.encodeToString(finalPayload.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        val submitFlag = if (autoSubmit) "true" else "false"

        val js = """
            (function() {
                try {
                    var bytes = Uint8Array.from(atob('$b64Payload'), function(c) { return c.charCodeAt(0); });
                    var text = new TextDecoder('utf-8').decode(bytes);
                    var el = document.querySelector('#prompt-textarea')
                        || document.querySelector('textarea')
                        || document.querySelector('[contenteditable="true"]')
                        || document.querySelector('input[type="search"]')
                        || document.querySelector('input[name="q"]')
                        || document.querySelector('input[type="text"]');
                    if (!el) return 'NO_INPUT_FOUND';
                    el.focus();
                    if (el.isContentEditable || el.getAttribute('contenteditable') === 'true') {
                        el.innerText = text;
                        el.dispatchEvent(new InputEvent('input', { bubbles: true, data: text }));
                    } else {
                        var proto = el.tagName === 'TEXTAREA'
                            ? window.HTMLTextAreaElement.prototype
                            : window.HTMLInputElement.prototype;
                        var nativeSetter = Object.getOwnPropertyDescriptor(proto, 'value');
                        if (nativeSetter && nativeSetter.set) {
                            nativeSetter.set.call(el, text);
                        } else {
                            el.value = text;
                        }
                        el.dispatchEvent(new Event('input', { bubbles: true }));
                        el.dispatchEvent(new Event('change', { bubbles: true }));
                    }
                    if ($submitFlag) {
                        setTimeout(function() {
                            var sendSelectors = [
                                'button[data-testid="send-button"]',
                                'button[aria-label*="Send" i]',
                                'button[aria-label*="Submit" i]',
                                'button[type="submit"]',
                                'button.send-button'
                            ];
                            var clicked = false;
                            for (var i = 0; i < sendSelectors.length; i++) {
                                var btn = document.querySelector(sendSelectors[i]);
                                if (btn && !btn.disabled) {
                                    btn.click();
                                    clicked = true;
                                    break;
                                }
                            }
                            if (!clicked && el.form) {
                                el.form.submit();
                            }
                        }, 300);
                    }
                    return 'PASTED_OK';
                } catch (e) {
                    return 'ERR:' + e.message;
                }
            })();
        """.trimIndent()

        webView.post {
            webView.evaluateJavascript(js) { res ->
                val clean = res?.removeSurrounding("\"") ?: ""
                val msg = if (clean == "PASTED_OK") {
                    val actionWord = if (autoSubmit) "পেস্ট ও সেন্ড" else "অটো-পেস্ট"
                    "✅ বর্তমান ওয়েবসাইটে গ্লোবাল রুল সফলভাবে $actionWord করা হয়েছে!"
                } else {
                    "ℹ️ পেজের ইনপুট বক্সে রুল প্রস্তুত রাখা হয়েছে ($clean)"
                }
                _lastAutoPasteStatus.value = msg
                onComplete(msg)
            }
        }
    }

    fun restoreAllProRules(context: Context) {
        val current = _rules.value.toMutableList()
        val defaults = defaultStandingRules()
        for (defRule in defaults) {
            if (current.none { it.title == defRule.title }) {
                current.add(defRule)
            }
        }
        _rules.value = current
        saveToPrefs(context)
        _lastAutoPasteStatus.value = "👑 ১২টি সার্বভৌম প্রো-ইঞ্জিনিয়ারিং ও সেলফ-আপগ্রেড রুল সক্রিয় করা হয়েছে!"
    }

    private fun defaultStandingRules(): List<GlobalRuleItem> = listOf(
        GlobalRuleItem(
            title = "১. জিরো শর্টকাট ও ১০০% সম্পূর্ণ কোড রুল (Zero-Shortcut Policy)",
            ruleInstruction = "কাজ যতই বড় বা জটিল হোক, কোডের কোনো অংশ অসম্পূর্ণ রাখা, ডামি রাখা বা '// TODO', '// add logic later' লেখা সম্পূর্ণ নিষিদ্ধ। প্রথমবারেই ১০০% সম্পূর্ণ, কম্পাইলযোগ্য ও ফাংশনাল কোড দিতে হবে।",
            category = "CODING",
            isEnabled = true,
            autoPasteToWebsites = true,
            autoInjectToBotsAndApi = true
        ),
        GlobalRuleItem(
            title = "২. ডিপ-থিংকিং ও আর্কিটেকচারাল নিখুঁততা রুল (Deep-Think Architecture)",
            ruleInstruction = "উত্তর বা ফিচার তৈরির আগে চারদিক থেকে ডিপ-থিংক (Deep-Think) করে পুরো আর্কিটেকচার, এজ-কেস (Edge Cases) এবং লজিক সাজাতে হবে যেন কোনো বাগ বা ক্র্যাশ না ঘটে।",
            category = "DEEP_THINK",
            isEnabled = true,
            autoPasteToWebsites = true,
            autoInjectToBotsAndApi = true
        ),
        GlobalRuleItem(
            title = "৩. ধাপে ধাপে বাস্তবায়ন ও স্পষ্ট বাংলা ব্যাখ্যা (Step-by-Step Execution)",
            ruleInstruction = "যেকোনো বড় কাজকে ছোট ছোট ধাপে (Step 1, Step 2, Step 3...) ভাগ করে একটি একটি করে ১০০% নির্ভুলভাবে সম্পন্ন করতে হবে এবং মূল ব্যাখ্যা পরিষ্কার বাংলায় দিতে হবে।",
            category = "ALL",
            isEnabled = true,
            autoPasteToWebsites = true,
            autoInjectToBotsAndApi = true
        ),
        GlobalRuleItem(
            title = "৪. প্রো-লেভেল ফিনিশিং ও আল্ট্রা-পলিশড UI রুল (Ultra-Pro Excellence)",
            ruleInstruction = "সাধারণ ফিচার চাইলেও কখনো সস্তা বা দায়সারা সমাধান দেওয়া যাবে না। প্রতিটি ফিচার আধুনিক সফটওয়্যারের শীর্ষ মানদণ্ডে (Top-Tier Cyber Dark UI, রেসপন্সিভ ও আকর্ষণীয়) তৈরি করতে হবে।",
            category = "CODING",
            isEnabled = true,
            autoPasteToWebsites = true,
            autoInjectToBotsAndApi = true
        ),
        GlobalRuleItem(
            title = "৫. জিরো-ক্লাটার ও তাৎক্ষণিক ভিজ্যুয়াল রেসপন্স রুল (Zero-Dead-Button Policy)",
            ruleInstruction = "কোনো বাটন যেন নিষ্ক্রিয় বা মৃত (Dead Button) না থাকে। প্রতিটি ক্লিক ও অ্যাকশনে তাৎক্ষণিক ভিজ্যুয়াল রেসপন্স, লাইভ আউটপুট, টোস্ট বা অ্যানিমেশন নিশ্চিত করতে হবে।",
            category = "CODING",
            isEnabled = true,
            autoPasteToWebsites = true,
            autoInjectToBotsAndApi = true
        ),
        GlobalRuleItem(
            title = "৬. ১০০% এরর ও সত্যের স্বচ্ছতা রুল (Absolute Error Transparency)",
            ruleInstruction = "কোনো এরর বা বাধা এলে তা কখনো গোপন করা বা এড়িয়ে যাওয়া যাবে না। ঠিক কী কারণে বাধা এসেছিল (Root Cause) এবং কীভাবে তা সমাধান করা হলো—তা শতভাগ সততার সাথে স্পষ্ট করে তুলে ধরতে হবে।",
            category = "ALL",
            isEnabled = true,
            autoPasteToWebsites = true,
            autoInjectToBotsAndApi = true
        ),
        GlobalRuleItem(
            title = "৭. রিকার্সিভ মেটা-ইঞ্জিন সেলফ-হিলিং রুল (Recursive Self-Healing)",
            ruleInstruction = "কোনো ফিচার বা স্ক্রিপ্ট রান হতে সমস্যা হলে মেটা-ইঞ্জিন নিজে থেকেই সিনট্যাক্স স্যানিটাইজ করবে, try-catch র‍্যাপার বসাবে এবং বিকল্প পথে ফিচারটি ১০০% সচল করবে।",
            category = "DEEP_THINK",
            isEnabled = true,
            autoPasteToWebsites = true,
            autoInjectToBotsAndApi = true
        ),
        GlobalRuleItem(
            title = "৮. নেটিভ অ্যান্ড্রয়েড ওএস ব্রিজ ইন্টিগ্রেশন রুল (Native SovereignOS Bridge)",
            ruleInstruction = "তৈরি করা প্রতিটি অ্যাপ ও টুল প্রয়োজনে window.SovereignOS ব্রিজ ব্যবহার করে সরাসরি ভয়েস স্পিচ (speak), ভাইব্রেশন (vibrate), ক্লিপবোর্ড কপি (copyToClipboard) ও ফুল-স্ক্রিন মোড ব্যবহার করবে।",
            category = "CODING",
            isEnabled = true,
            autoPasteToWebsites = false,
            autoInjectToBotsAndApi = true
        ),
        GlobalRuleItem(
            title = "৯. লোকাল স্টোরেজ ও জিরো ডেটা-লস ভল্ট রুল (Persistent Local Vault)",
            ruleInstruction = "ব্যবহারকারীর যেকোনো ইনপুট, নোট, হিসাব বা তৈরি করা ডেটা localStorage ও ডিস্ক ভল্টে স্বয়ংক্রিয়ভাবে সংরক্ষিত রাখতে হবে যাতে অ্যাপ বন্ধ বা রিলোড করলেও ১% ডেটাও না হারায়।",
            category = "CODING",
            isEnabled = true,
            autoPasteToWebsites = false,
            autoInjectToBotsAndApi = true
        ),
        GlobalRuleItem(
            title = "১০. ইন্ডিয়ান বাংলা (bn-IN) ও বহুভাষিক ভয়েস নির্ভুলতা রুল (Voice Precision)",
            ruleInstruction = "ব্যবহারকারী মুখে যেভাবেই বা যত দূর থেকেই আদেশ দিন, তা ইন্ডিয়ান বাংলা (bn-IN) ও ইংরেজি কমান্ড হিসেবে নিখুঁতভাবে বুঝে সরাসরি কাজে পরিণত করতে হবে।",
            category = "ALL",
            isEnabled = true,
            autoPasteToWebsites = false,
            autoInjectToBotsAndApi = true
        ),
        GlobalRuleItem(
            title = "১১. সার্চ ও ওয়েব এক্সট্রাকশন পিউরিটি রুল (Web Extraction Purity)",
            ruleInstruction = "যেকোনো ওয়েবসাইট বা সার্চ রেজাল্ট থেকে অপ্রয়োজনীয় বিজ্ঞাপন, পপআপ বা ট্র্যাকার বাদ দিয়ে শুধুমাত্র মূল তথ্য, ফ্যাক্ট এবং কার্যকরী কোড/সমাধান ছেঁকে আনতে হবে।",
            category = "SEARCH",
            isEnabled = true,
            autoPasteToWebsites = true,
            autoInjectToBotsAndApi = true
        ),
        GlobalRuleItem(
            title = "১২. আনলিমিটেড উদ্ভাবনী অ্যাপ ও ফুল-স্ক্রিন স্টুডিও রুল (Unbounded Invention)",
            ruleInstruction = "শুধু সাধারণ ক্যালকুলেটর বা ছোট উইজেট নয়—ব্যবহারকারী যেকোনো বড় ফিচার, এআই জেনারেটর, আইডিই বা পৃথিবীতে নেই এমন নতুন কোনো সিস্টেম চাইলে তা ফুল-স্ক্রিন প্রো স্টুডিও হিসেবে তৈরি করতে হবে।",
            category = "DEEP_THINK",
            isEnabled = true,
            autoPasteToWebsites = true,
            autoInjectToBotsAndApi = true
        )
    )

    private fun loadFromPrefs(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            _isMasterAutoRuleEnabled.value = prefs.getBoolean(KEY_MASTER_AUTO_INJECT, true)
            _isAutoPasteOnPageLoadEnabled.value = prefs.getBoolean(KEY_AUTO_PASTE_ON_WEB_LOAD, false)

            val rawJson = prefs.getString(KEY_RULES_JSON, null)
            val parsed = mutableListOf<GlobalRuleItem>()
            if (!rawJson.isNullOrBlank()) {
                val arr = JSONArray(rawJson)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    parsed.add(
                        GlobalRuleItem(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            title = obj.optString("title", "Rule"),
                            ruleInstruction = obj.optString("ruleInstruction", ""),
                            category = obj.optString("category", "ALL"),
                            isEnabled = obj.optBoolean("isEnabled", true),
                            autoPasteToWebsites = obj.optBoolean("autoPasteToWebsites", true),
                            autoInjectToBotsAndApi = obj.optBoolean("autoInjectToBotsAndApi", true)
                        )
                    )
                }
            }
            // Ensure all 12 Sovereign Default Rules are present
            val defaults = defaultStandingRules()
            if (parsed.isEmpty()) {
                parsed.addAll(defaults)
            } else if (parsed.size < defaults.size) {
                for (defRule in defaults) {
                    if (parsed.none { it.title.take(6) == defRule.title.take(6) }) {
                        parsed.add(defRule)
                    }
                }
            }
            _rules.value = parsed
        } catch (_: Exception) {
            _rules.value = defaultStandingRules()
        }
    }

    private fun saveToPrefs(context: Context) {
        try {
            val arr = JSONArray()
            _rules.value.forEach { item ->
                arr.put(
                    JSONObject().apply {
                        put("id", item.id)
                        put("title", item.title)
                        put("ruleInstruction", item.ruleInstruction)
                        put("category", item.category)
                        put("isEnabled", item.isEnabled)
                        put("autoPasteToWebsites", item.autoPasteToWebsites)
                        put("autoInjectToBotsAndApi", item.autoInjectToBotsAndApi)
                    }
                )
            }
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_MASTER_AUTO_INJECT, _isMasterAutoRuleEnabled.value)
                .putBoolean(KEY_AUTO_PASTE_ON_WEB_LOAD, _isAutoPasteOnPageLoadEnabled.value)
                .putString(KEY_RULES_JSON, arr.toString())
                .apply()
        } catch (_: Exception) {
        }
    }
}
