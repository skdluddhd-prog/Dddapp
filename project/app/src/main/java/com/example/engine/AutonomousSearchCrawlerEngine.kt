package com.example.engine

import android.content.Context
import android.net.Uri
import android.webkit.WebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLDecoder
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

data class CrawledPageSummary(
    val title: String,
    val url: String,
    val extractedText: String,
    val extractedCodeBlocks: String
)

/**
 * Step 3: Universal Autonomous Search, Multi-Link Deep Crawler & Parallel Worker Pool.
 * Allows Nanobots to search any topic across the web without requiring a fixed URL,
 * automatically open the top organic links in parallel, strip ads/popups, and harvest
 * clean articles + source code.
 */
object AutonomousSearchCrawlerEngine {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val _isCrawling = MutableStateFlow(false)
    val isCrawling: StateFlow<Boolean> = _isCrawling.asStateFlow()

    private val _crawlerStatus = MutableStateFlow("🌐 অটো-সার্চ ও মাল্টি-লিংক ক্রলার প্রস্তুত")
    val crawlerStatus: StateFlow<String> = _crawlerStatus.asStateFlow()

    private val _lastCrawledPages = MutableStateFlow<List<CrawledPageSummary>>(emptyList())
    val lastCrawledPages: StateFlow<List<CrawledPageSummary>> = _lastCrawledPages.asStateFlow()

    /**
     * Checks whether a Nanobot's targetInput or message represents an autonomous web search
     * rather than a fixed chat platform URL.
     */
    fun isSearchTarget(targetInput: String): Boolean {
        val t = targetInput.trim().lowercase()
        if (t.startsWith("search://") || t.startsWith("crawl://")) return true
        if (t.contains("google.com/search") || t.contains("duckduckgo.com") || t.contains("bing.com/search")) return true
        if (!t.startsWith("http://") && !t.startsWith("https://") && !t.contains(".com") && !t.contains(".ai") && !t.contains(".org")) {
            return true
        }
        return false
    }

    /**
     * Performs a Parallel Autonomous Search + Multi-Page Deep Crawl and returns a formatted
     * Research Dossier ready for the next Deep-Thinker Nanobot.
     */
    suspend fun executeAutonomousSearchAndCrawl(
        context: Context,
        rawQueryOrTarget: String,
        fallbackTaskMessage: String = "",
        optionalWebView: WebView? = null,
        maxLinksToCrawl: Int = 3
    ): String = withContext(Dispatchers.IO) {
        _isCrawling.value = true
        val cleanQuery = rawQueryOrTarget
            .removePrefix("search://")
            .removePrefix("crawl://")
            .trim()
            .let { q ->
                if (q.isBlank() || q.equals("Internal Deep-Think Brain", ignoreCase = true) || q.equals("Deep-Think Core", ignoreCase = true)) {
                    fallbackTaskMessage.lines().firstOrNull()?.take(110)?.trim().orEmpty()
                } else {
                    q
                }
            }
            .ifBlank { "Modern Software Architecture & AI Automation Best Practices" }

        _crawlerStatus.value = "🔍 স্বয়ংক্রিয়ভাবে ওয়েবে সার্চ করা হচ্ছে: '$cleanQuery'..."

        // 1. Discover top organic URLs from DuckDuckGo HTML SERP
        val discoveredLinks = fetchTopSearchLinks(cleanQuery, maxLinksToCrawl)
        _crawlerStatus.value =
            "🌐 ${discoveredLinks.size}টি সেরা ওয়েবসাইট পাওয়া গেছে—প্যারালাল ওয়ার্কার দিয়ে ভেতরের তথ্য তোলা হচ্ছে..."

        // 2. Crawl discovered pages in parallel using coroutines (Parallel Worker Pool)
        val crawledPages = coroutineScope {
            discoveredLinks.mapIndexed { idx, pair ->
                async(Dispatchers.IO) {
                    crawlSingleUrl(
                        titleHint = pair.first,
                        url = pair.second,
                        index = idx + 1
                    )
                }
            }.awaitAll().filterNotNull()
        }.toMutableList()

        // 3. Also harvest live WebView DOM if user has an active research page open
        if (optionalWebView != null && crawledPages.isEmpty()) {
            val domSnapshot = harvestCleanDomFromWebView(optionalWebView)
            if (domSnapshot.isNotBlank()) {
                crawledPages.add(
                    CrawledPageSummary(
                        title = "Active Browser Live Page",
                        url = cleanQuery,
                        extractedText = domSnapshot,
                        extractedCodeBlocks = WorkerEngine.extractCodeBlocks(domSnapshot)
                    )
                )
            }
        }

        _lastCrawledPages.value = crawledPages

        val compiledDossier = buildString {
            appendLine("### 🌐 অটো-সার্চ ও মাল্টি-সাইট ক্রল রিপোর্ট (Topic: $cleanQuery)")
            appendLine()
            if (crawledPages.isNotEmpty()) {
                crawledPages.forEachIndexed { i, page ->
                    appendLine("#### [${i + 1}] ${page.title}")
                    appendLine("- **Source URL:** ${page.url}")
                    appendLine("- **মূল সংগৃহীত তথ্য:**")
                    appendLine(page.extractedText.take(1600))
                    if (page.extractedCodeBlocks.isNotBlank() && page.extractedCodeBlocks != page.extractedText) {
                        appendLine("- **সংগৃহীত কোড স্নিপেট:**")
                        appendLine("```")
                        appendLine(page.extractedCodeBlocks.take(1000))
                        appendLine("```")
                    }
                    appendLine()
                }
            } else {
                appendLine("ℹ️ সরাসরি HTML পেজ রেসপন্স সীমিত থাকায় AI ব্রেন ও মেমোরি ভল্ট থেকে গভীর বিশ্লেষণ যুক্ত করা হচ্ছে...")
            }
        }.trim()

        // 4. Enrich with AI synthesis if configured so the output is always comprehensive
        val finalResult = if (crawledPages.isEmpty()) {
            val aiFallback = UniversalAiManager.executeAiPrompt(
                prompt = "Search & Research Topic: $cleanQuery\nAdditional Task Context: $fallbackTaskMessage\nProvide a comprehensive, factual research report and complete solution.",
                systemInstruction = "তুমি একজন অটোনোমাস ওয়েব রিসার্চার ও ডিপ-থিংক বট।"
            ).getOrNull()
            if (!aiFallback.isNullOrBlank()) {
                "$compiledDossier\n\n### 🧠 AI ডিপ-রিসার্চ সিনথেসিস:\n$aiFallback"
            } else {
                compiledDossier
            }
        } else {
            compiledDossier
        }

        SwarmMemoryVaultEngine.recordBotInsight(
            context = context,
            topicTitle = "Web Crawl: ${cleanQuery.take(65)}",
            sourceBotName = "Auto-Search Crawler",
            sourceUrlOrPlatform = discoveredLinks.firstOrNull()?.second ?: "DuckDuckGo / Web Pool",
            rawOutput = finalResult
        )

        _isCrawling.value = false
        _crawlerStatus.value = "✅ প্যারালাল ক্রলিং সম্পন্ন: ${crawledPages.size}টি সাইট থেকে মূল তথ্য ও কোড সংগ্রহ করা হয়েছে।"
        finalResult
    }

    private fun fetchTopSearchLinks(query: String, limit: Int): List<Pair<String, String>> {
        val results = mutableListOf<Pair<String, String>>()
        try {
            val encoded = Uri.encode(query)
            val searchUrl = "https://html.duckduckgo.com/html/?q=$encoded"
            val req = Request.Builder()
                .url(searchUrl)
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
                )
                .header("Accept-Language", "en-US,en;q=0.9,bn;q=0.8")
                .build()

            httpClient.newCall(req).execute().use { resp ->
                val html = resp.body?.string().orEmpty()
                val linkRegex = Regex(
                    """<a[^>]+class="[^"]*result__a[^"]*"[^>]+href="([^"]+)"[^>]*>(.*?)</a>""",
                    setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
                )
                for (match in linkRegex.findAll(html)) {
                    val rawHref = match.groupValues[1]
                    val rawTitle = stripHtmlTags(match.groupValues[2]).trim()
                    val resolvedUrl = decodeDuckDuckGoRedirect(rawHref)
                    if (resolvedUrl.startsWith("http") && rawTitle.isNotBlank()) {
                        results.add(rawTitle to resolvedUrl)
                        if (results.size >= limit) break
                    }
                }
            }
        } catch (_: Exception) {
        }

        if (results.isEmpty()) {
            val encoded = Uri.encode(query)
            results.add("Wikipedia Knowledge Search: $query" to "https://en.wikipedia.org/w/index.php?search=$encoded")
        }
        return results
    }

    private fun decodeDuckDuckGoRedirect(rawHref: String): String {
        return try {
            if (rawHref.contains("uddg=")) {
                val sub = rawHref.substringAfter("uddg=").substringBefore("&")
                URLDecoder.decode(sub, "UTF-8")
            } else if (rawHref.startsWith("//")) {
                "https:$rawHref"
            } else {
                rawHref
            }
        } catch (_: Exception) {
            rawHref
        }
    }

    private fun crawlSingleUrl(
        titleHint: String,
        url: String,
        index: Int
    ): CrawledPageSummary? {
        return try {
            val req = Request.Builder()
                .url(url)
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 15; Pixel 9 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Mobile Safari/537.36"
                )
                .build()

            httpClient.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val html = resp.body?.string().orEmpty()
                if (html.isBlank()) return null

                val pageTitle = Regex("""<title[^>]*>(.*?)</title>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
                    .find(html)?.groupValues?.getOrNull(1)?.let { stripHtmlTags(it).trim() }
                    ?.ifBlank { titleHint } ?: titleHint

                // Extract <pre> and <code> blocks before stripping HTML
                val codeBlocks = Regex("""<(?:pre|code)[^>]*>(.*?)</(?:pre|code)>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
                    .findAll(html)
                    .map { stripHtmlTags(it.groupValues[1]).trim() }
                    .filter { it.length > 30 }
                    .take(4)
                    .joinToString("\n\n")

                // Remove scripts, styles, nav, footer, svg
                val cleanedHtml = html
                    .replace(Regex("""<(script|style|noscript|svg|nav|footer|header|aside)[^>]*>.*?</\1>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)), " ")
                val plainText = stripHtmlTags(cleanedHtml)
                    .replace(Regex("""\s+"""), " ")
                    .trim()
                    .take(2400)

                if (plainText.length < 40) return null

                CrawledPageSummary(
                    title = pageTitle,
                    url = url,
                    extractedText = plainText,
                    extractedCodeBlocks = codeBlocks
                )
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun stripHtmlTags(html: String): String {
        return html
            .replace(Regex("""<[^>]+>"""), " ")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
    }

    private suspend fun harvestCleanDomFromWebView(webView: WebView): String =
        withContext(Dispatchers.Main) {
            delay(400L)
            suspendCancellableCoroutine { cont ->
                val js = """
                    (function() {
                        try {
                            // Auto-dismiss common cookie/popup banners
                            var dismissSelectors = [
                                'button[aria-label*="close" i]',
                                'button[aria-label*="accept" i]',
                                '#onetrust-accept-btn-handler'
                            ];
                            for (var i = 0; i < dismissSelectors.length; i++) {
                                var b = document.querySelector(dismissSelectors[i]);
                                if (b) b.click();
                            }
                            var mainEl = document.querySelector('main, article, [role="main"]') || document.body;
                            return mainEl ? (mainEl.innerText || '').trim().slice(0, 5000) : '';
                        } catch (e) {
                            return '';
                        }
                    })();
                """.trimIndent()
                webView.evaluateJavascript(js) { raw ->
                    val clean = raw?.removeSurrounding("\"")
                        ?.replace("\\n", "\n")
                        ?.replace("\\\"", "\"")
                        ?.trim()
                        .orEmpty()
                    if (cont.isActive) cont.resume(clean)
                }
            }
        }
}
