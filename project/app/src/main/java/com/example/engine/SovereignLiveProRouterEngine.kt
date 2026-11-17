package com.example.engine

import com.example.BuildConfig
import com.example.model.AiProviderType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class LiveRoute(val labelBangla: String) {
    FAST("⚡ ফাস্ট"),
    SEARCH("🌐 ইন্টারনেট সার্চ"),
    DEEP("🧠 ডিপ-থিংক")
}

/**
 * Live Pro router: every spoken question takes the cheapest path that can answer it well.
 *  FAST   - text only, thinking OFF, max ~260 tokens: typically 1-2 seconds.
 *  SEARCH - Gemini with Google Search grounding for news, prices, weather, scores, "latest" facts.
 *  DEEP   - the existing screen-aware multimodal path (image + long answer) for analysis or screen questions.
 * Needs a Gemini key for FAST/SEARCH; with any other provider it falls back to the normal path.
 */
object SovereignLiveProRouter {
    private val http = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .readTimeout(40, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    @Volatile var lastRoute: LiveRoute = LiveRoute.FAST
        private set
    @Volatile var lastSources: List<String> = emptyList()
        private set

    private val screenWords = listOf(
        "স্ক্রিনে", "স্ক্রিন", "এটা কী", "এখানে কী", "দেখো", "ছবিতে", "screen", "this button", "what is this"
    )
    private val searchWords = listOf(
        "আজকের", "এখনকার", "সর্বশেষ", "লেটেস্ট", "খবর", "নিউজ", "আবহাওয়া", "দাম", "রেট", "স্কোর",
        "ফলাফল", "সার্চ", "ইন্টারনেটে", "গুগলে", "latest", "today", "news", "price", "weather",
        "score", "search", "current", "right now", "2026"
    )
    private val deepWords = listOf(
        "বিশ্লেষণ", "তুলনা", "কেন ", "কীভাবে", "কিভাবে", "পরিকল্পনা", "প্ল্যান", "কোড", "ব্যাখ্যা",
        "ডিপ থিংক", "explain", "why", "how ", "compare", "analy", "plan", "code", "design"
    )

    private val actionWords = listOf(
        "ক্লিক", "ট্যাপ", "টাচ", "চাপো", "টিপো", "খোলো", "স্ক্রল", "লিখো", "টাইপ",
        "click", "tap", "scroll", "type ", "open "
    )

    /** A fresh screenshot costs time (capture + JPEG encode); plain chat questions reuse the watcher's last frame. */
    fun needsFreshFrame(spoken: String): Boolean {
        val lower = spoken.trim().lowercase()
        return classify(lower) != LiveRoute.FAST || lower.length >= 60 || actionWords.any { lower.contains(it) }
    }

    fun clearSources() {
        lastSources = emptyList()
    }

    fun classify(spoken: String): LiveRoute {
        val lower = spoken.trim().lowercase()
        if (screenWords.any { lower.contains(it) }) return LiveRoute.DEEP
        if (searchWords.any { lower.contains(it) }) return LiveRoute.SEARCH
        if (lower.length > 90 || deepWords.any { lower.contains(it) }) return LiveRoute.DEEP
        return LiveRoute.FAST
    }

    suspend fun answer(
        prompt: String,
        domSummary: String,
        base64Image: String?,
        systemPrompt: String,
        onAck: (String) -> Unit
    ): Result<String> {
        val route = classify(prompt)
        lastRoute = route
        lastSources = emptyList()
        if (route == LiveRoute.DEEP) {
            return UniversalAiManager.executeMultimodalVisionPrompt(
                prompt = prompt,
                base64Image = base64Image,
                systemInstruction = systemPrompt,
                screenContextSummary = domSummary
            )
        }
        if (route == LiveRoute.SEARCH) {
            try { onAck("ইন্টারনেটে খুঁজে দেখছি।") } catch (_: Throwable) { }
        }
        val direct = geminiText(prompt, domSummary, systemPrompt, route == LiveRoute.SEARCH)
        return direct ?: UniversalAiManager.executeMultimodalVisionPrompt(
            prompt = prompt,
            base64Image = base64Image,
            systemInstruction = systemPrompt,
            screenContextSummary = domSummary
        )
    }

    private suspend fun geminiText(
        prompt: String,
        domSummary: String,
        systemPrompt: String,
        search: Boolean
    ): Result<String>? = withContext(Dispatchers.IO) {
        val prof = UniversalAiManager.getActiveProfile()
        if (prof != null && prof.provider != AiProviderType.GEMINI) return@withContext null
        val envKey = runCatching { BuildConfig.GEMINI_API_KEY }.getOrDefault("").trim()
        val key = prof?.apiKey?.trim()?.takeIf {
            it.length >= 15 && !it.startsWith("AQ.DEFAULT") && it != "YOUR_GEMINI_API_KEY"
        } ?: envKey.takeIf { it.length >= 15 && it != "YOUR_GEMINI_API_KEY" }.orEmpty()
        if (key.isBlank()) return@withContext null

        val history = runCatching {
            ChatSessionManager.getActiveConversationHistoryPairs(maxMessages = 6)
        }.getOrDefault(emptyList())

        val text = buildString {
            appendLine(systemPrompt)
            appendLine()
            appendLine(
                if (search) "ইন্টারনেট থেকে সর্বশেষ তথ্য খুঁজে ২-৩ বাক্যে সঠিক উত্তর দাও। তথ্য নিশ্চিত না হলে স্পষ্ট বলো।"
                else "উত্তর ১-২টি ছোট, সরাসরি বাক্যে দাও।"
            )
            appendLine()
            if (domSummary.isNotBlank()) {
                appendLine("[স্ক্রিন]: ${domSummary.take(500)}")
                appendLine()
            }
            if (history.isNotEmpty()) {
                appendLine("[সাম্প্রতিক কথা]:")
                history.forEach { (s, t) -> appendLine("- $s: ${t.replace("\n", " ").take(200)}") }
                appendLine()
            }
            append(prompt)
        }

        for (model in listOf("gemini-2.5-flash", "gemini-2.0-flash")) {
            // maxOutputTokens counts thinking tokens too, so SEARCH gets room for its small thinking budget
            val gen = JSONObject()
                .put("temperature", 0.4)
                .put("maxOutputTokens", if (search) 1100 else 300)
            if (model.contains("2.5")) {
                gen.put("thinkingConfig", JSONObject().put("thinkingBudget", if (search) 512 else 0))
            }
            val payload = JSONObject()
                .put(
                    "contents",
                    JSONArray().put(
                        JSONObject().put("role", "user")
                            .put("parts", JSONArray().put(JSONObject().put("text", text)))
                    )
                )
                .put("generationConfig", gen)
            if (search) {
                payload.put("tools", JSONArray().put(JSONObject().put("google_search", JSONObject())))
            }
            try {
                val req = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent")
                    .header("x-goog-api-key", key)
                    .post(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                    .build()
                http.newCall(req).execute().use { resp ->
                    val body = resp.body?.string().orEmpty()
                    if (resp.isSuccessful) {
                        val cand = JSONObject(body).optJSONArray("candidates")?.optJSONObject(0)
                        val parts = cand?.optJSONObject("content")?.optJSONArray("parts")
                        val sb = StringBuilder()
                        if (parts != null) {
                            for (i in 0 until parts.length()) {
                                val p = parts.optJSONObject(i) ?: continue
                                if (!p.optBoolean("thought", false)) sb.append(p.optString("text", ""))
                            }
                        }
                        val reply = sb.toString().trim()
                        if (reply.isNotBlank()) {
                            val chunks = cand?.optJSONObject("groundingMetadata")?.optJSONArray("groundingChunks")
                            val titles = mutableListOf<String>()
                            if (chunks != null) {
                                for (i in 0 until minOf(chunks.length(), 3)) {
                                    val t = chunks.optJSONObject(i)?.optJSONObject("web")?.optString("title").orEmpty()
                                    if (t.isNotBlank()) titles.add(t)
                                }
                            }
                            lastSources = titles
                            return@withContext Result.success(reply)
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }
        null
    }
}
