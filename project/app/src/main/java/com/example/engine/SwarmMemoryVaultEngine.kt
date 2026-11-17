package com.example.engine

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class SwarmMemoryEntry(
    val id: String = UUID.randomUUID().toString(),
    val topicTitle: String,
    val sourceBotName: String,
    val sourceUrlOrPlatform: String,
    val keyInsightOrSolution: String,
    val isPinned: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Shared Long-Term Swarm Memory Vault:
 * Allows all Nanobots across sessions to share learned facts, scraped results,
 * verified code patterns, and user decisions without re-searching from scratch.
 */
object SwarmMemoryVaultEngine {
    private const val PREFS_NAME = "skrl2_swarm_memory_vault_prefs"
    private const val KEY_MEMORIES_JSON = "key_swarm_memories_json"
    private const val KEY_AUTO_SHARE_MEMORY = "key_auto_share_memory"

    private val _memories = MutableStateFlow<List<SwarmMemoryEntry>>(emptyList())
    val memories: StateFlow<List<SwarmMemoryEntry>> = _memories.asStateFlow()

    private val _isAutoMemoryRecallEnabled = MutableStateFlow(true)
    val isAutoMemoryRecallEnabled: StateFlow<Boolean> = _isAutoMemoryRecallEnabled.asStateFlow()

    fun init(context: Context) {
        loadFromPrefs(context)
    }

    fun setAutoMemoryRecall(context: Context, enabled: Boolean) {
        _isAutoMemoryRecallEnabled.value = enabled
        saveToPrefs(context)
    }

    fun recordBotInsight(
        context: Context,
        topicTitle: String,
        sourceBotName: String,
        sourceUrlOrPlatform: String,
        rawOutput: String,
        isPinned: Boolean = false
    ) {
        val cleaned = rawOutput.trim()
        if (cleaned.length < 20) return

        val compactInsight = cleaned.take(1500)
        // Avoid exact duplicate of the most recent entry
        val latest = _memories.value.firstOrNull()
        if (latest != null && latest.keyInsightOrSolution == compactInsight) return

        val entry = SwarmMemoryEntry(
            topicTitle = topicTitle.take(90).ifBlank { "Nanobot Insight" },
            sourceBotName = sourceBotName.ifBlank { "Swarm Worker" },
            sourceUrlOrPlatform = sourceUrlOrPlatform.ifBlank { "Internal Deep-Think" },
            keyInsightOrSolution = compactInsight,
            isPinned = isPinned
        )

        val updated = (listOf(entry) + _memories.value)
            .sortedByDescending { if (it.isPinned) Long.MAX_VALUE else it.timestamp }
            .take(60)
        _memories.value = updated
        saveToPrefs(context)
    }

    fun togglePin(context: Context, memoryId: String) {
        _memories.value = _memories.value.map {
            if (it.id == memoryId) it.copy(isPinned = !it.isPinned) else it
        }.sortedByDescending { if (it.isPinned) Long.MAX_VALUE else it.timestamp }
        saveToPrefs(context)
    }

    fun deleteMemory(context: Context, memoryId: String) {
        _memories.value = _memories.value.filterNot { it.id == memoryId }
        saveToPrefs(context)
    }

    fun clearAllUnpinned(context: Context) {
        _memories.value = _memories.value.filter { it.isPinned }
        saveToPrefs(context)
    }

    /**
     * Retrieves relevant shared memories (pinned + keyword-matched) so any Nanobot
     * automatically remembers what other bots discovered earlier.
     */
    fun buildRelevantMemoryContext(queryOrTask: String, maxEntries: Int = 3): String {
        if (!_isAutoMemoryRecallEnabled.value) return ""
        val all = _memories.value
        if (all.isEmpty()) return ""

        val keywords = queryOrTask.lowercase()
            .split(Regex("[\\s,.;:?!/\\-]+"))
            .filter { it.length >= 3 }
            .take(8)

        val matched = all.filter { entry ->
            entry.isPinned || keywords.any { kw ->
                entry.topicTitle.lowercase().contains(kw) ||
                    entry.keyInsightOrSolution.lowercase().contains(kw)
            }
        }.take(maxEntries).ifEmpty {
            all.filter { it.isPinned }.take(2)
        }

        if (matched.isEmpty()) return ""

        return buildString {
            appendLine("--- SHARED SWARM MEMORY VAULT (পূর্ববর্তী বটদের সংরক্ষিত অভিজ্ঞতা) ---")
            matched.forEachIndexed { idx, item ->
                appendLine("${idx + 1}. [${item.sourceBotName} @ ${item.sourceUrlOrPlatform}] (${item.topicTitle}):")
                appendLine(item.keyInsightOrSolution.take(550))
                appendLine()
            }
            appendLine("--- END OF SHARED SWARM MEMORY ---")
        }.trim()
    }

    private fun loadFromPrefs(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            _isAutoMemoryRecallEnabled.value = prefs.getBoolean(KEY_AUTO_SHARE_MEMORY, true)
            val raw = prefs.getString(KEY_MEMORIES_JSON, null)
            val list = mutableListOf<SwarmMemoryEntry>()
            if (!raw.isNullOrBlank()) {
                val arr = JSONArray(raw)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        SwarmMemoryEntry(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            topicTitle = obj.optString("topicTitle", "Swarm Insight"),
                            sourceBotName = obj.optString("sourceBotName", "Bot"),
                            sourceUrlOrPlatform = obj.optString("sourceUrlOrPlatform", ""),
                            keyInsightOrSolution = obj.optString("keyInsightOrSolution", ""),
                            isPinned = obj.optBoolean("isPinned", false),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
            }
            _memories.value = list
        } catch (_: Exception) {
        }
    }

    private fun saveToPrefs(context: Context) {
        try {
            val arr = JSONArray()
            _memories.value.forEach { m ->
                arr.put(
                    JSONObject().apply {
                        put("id", m.id)
                        put("topicTitle", m.topicTitle)
                        put("sourceBotName", m.sourceBotName)
                        put("sourceUrlOrPlatform", m.sourceUrlOrPlatform)
                        put("keyInsightOrSolution", m.keyInsightOrSolution)
                        put("isPinned", m.isPinned)
                        put("timestamp", m.timestamp)
                    }
                )
            }
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_AUTO_SHARE_MEMORY, _isAutoMemoryRecallEnabled.value)
                .putString(KEY_MEMORIES_JSON, arr.toString())
                .apply()
        } catch (_: Exception) {
        }
    }
}
