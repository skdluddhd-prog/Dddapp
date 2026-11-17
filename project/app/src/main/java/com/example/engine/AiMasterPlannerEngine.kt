package com.example.engine

import com.example.model.BotExecutionStatus
import com.example.model.NanobotRole
import com.example.model.NodeConnection
import com.example.model.RuleType
import com.example.model.WorkflowNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class SwarmBlueprintResult(
    val blueprintTitle: String,
    val summaryDescription: String,
    val nodes: List<WorkflowNode>,
    val connections: List<NodeConnection>
)

data class SwarmPresetTemplate(
    val id: String,
    val title: String,
    val subtitle: String,
    val badge: String,
    val defaultGoalPrompt: String
)

/**
 * Step 2: AI Master Planner (Auto-Role & Auto-Rule Architect) + 1-Tap Swarm Templates.
 * Uses the configured API Key (or intelligent decomposition fallback) to automatically create
 * Nanobots, assign their custom roles, bind Global Standing Rules, set search/website targets,
 * and connect the entire pipeline from a single user sentence.
 */
object AiMasterPlannerEngine {

    private val _isPlanning = MutableStateFlow(false)
    val isPlanning: StateFlow<Boolean> = _isPlanning.asStateFlow()

    private val _plannerStatus = MutableStateFlow("🧠 AI মাস্টার প্ল্যানার প্রস্তুত: ১ লাইনে আপনার লক্ষ্য লিখলেই সব বট ও রুল নিজে সেট হয়ে যাবে")
    val plannerStatus: StateFlow<String> = _plannerStatus.asStateFlow()

    val presetTemplates: List<SwarmPresetTemplate> = listOf(
        SwarmPresetTemplate(
            id = "DEEP_SEARCH_SWARM",
            title = "🔍 অটো ওয়েব সার্চ ও ডিপ রিসার্চ সোয়ার্ম",
            subtitle = "নিজে সার্চ ইঞ্জিনে সার্চ করে ৪-৫টি সাইটের তথ্য এনে ডিপ-থিংক রিপোর্ট তৈরি করে",
            badge = "SEARCH + THINK",
            defaultGoalPrompt = "ওয়েব ও সার্চ ইঞ্জিন থেকে এই বিষয়ে সেরা ও সর্বশেষ তথ্য খুঁজে বের করো, ভেতরের লিংকগুলো বিশ্লেষণ করো এবং ডিপ-থিংক করে সম্পূর্ণ নির্ভুল গাইড ও সমাধান দাও।"
        ),
        SwarmPresetTemplate(
            id = "FULLSTACK_CODE_SWARM",
            title = "💻 ফুল-স্ট্যাক কোড ও স্যান্ডবক্স আর্কিটেক্ট",
            subtitle = "কোড সার্চ ➔ ডিপ-থিংক আর্কিটেকচার ➔ বাগ চেকিং ➔ লাইভ স্যান্ডবক্স প্রিভিউ",
            badge = "CODE + SANDBOX",
            defaultGoalPrompt = "এই প্রজেক্টের জন্য ওয়েব ও গিটহাব প্যাটার্ন বিশ্লেষণ করে সম্পূর্ণ বাগ-মুক্ত, প্রোডাকশন-রেডি কোড তৈরি করো এবং স্যান্ডবক্সে রান করার উপযোগী করো।"
        ),
        SwarmPresetTemplate(
            id = "MULTI_AI_DEBATE_SWARM",
            title = "🧠 মাল্টি-AI ডিবেট ও ক্রিটিক ভেরিফায়ার",
            subtitle = "এক বট উত্তর আনে, দ্বিতীয় বট ভুল ধরে চ্যালেঞ্জ করে, তৃতীয় বট সেরা সিদ্ধান্ত দেয়",
            badge = "MULTI-AI DEBATE",
            defaultGoalPrompt = "এই সমস্যাটির সম্ভাব্য সব সমাধান বের করো, প্রতিটি সমাধানের দুর্বলতা ও ঝুঁকি ডিপ-থিংক করে যাচাই করো এবং সবচেয়ে নিখুঁত চূড়ান্ত সমাধানটি দাও।"
        ),
        SwarmPresetTemplate(
            id = "AUTO_RULE_SCRAPER_SWARM",
            title = "⚡ অটো-রুল ইনজেক্টর ও নলেজ হারভেস্টার",
            subtitle = "সব সাইটে স্থায়ী রুল অটো-পেস্ট করে মূল ডেটা ও কোড ছেঁকে মেমোরি ভল্টে জমা রাখে",
            badge = "AUTO-RULE + VAULT",
            defaultGoalPrompt = "লক্ষ্যের ওয়েবসাইটে আমার স্থায়ী রুলগুলো স্বয়ংক্রিয়ভাবে প্রয়োগ করো, মূল ডেটা ও কোড এক্সট্রাক্ট করো এবং সাজিয়ে যৌথ মেমোরি ভল্টে সংরক্ষণ করো।"
        )
    )

    /**
     * Generates a complete Multi-Agent Nanobot Swarm from a single high-level user goal.
     * Automatically injects Global Standing Rules into every bot so the user never has to paste rules.
     */
    suspend fun buildAutonomousSwarmFromGoal(
        userGoal: String,
        preferredSearchOrUrl: String = ""
    ): SwarmBlueprintResult = withContext(Dispatchers.IO) {
        _isPlanning.value = true
        _plannerStatus.value = "🧠 মাস্টার ব্রেন আপনার লক্ষ্য বিশ্লেষণ করে বটদের রোল, রুল ও পাইপলাইন তৈরি করছে..."

        val cleanGoal = userGoal.trim().ifBlank {
            "ওয়েব থেকে তথ্য খুঁজে ডিপ-থিংক করে সম্পূর্ণ সমাধান ও কোড তৈরি করো।"
        }
        val globalRulesBlock = GlobalRuleVaultEngine.buildCompiledRulesBlock(forWebsitePaste = false)

        val plannerSystemPrompt = """
            You are the Master Nanobot Swarm Architect.
            Given a user's high-level goal, design a 4-bot autonomous relay chain in strict JSON format.
            Return ONLY valid JSON with this structure:
            {
              "blueprintTitle": "Short Bangla Title",
              "summaryDescription": "1 sentence Bangla overview",
              "bots": [
                {
                  "title": "Bot 1 Name",
                  "role": "WEB_MESSENGER",
                  "customRoleName": "Bangla Role Badge",
                  "customRolePrompt": "Detailed Bengali system prompt for this bot",
                  "targetInput": "search://query or https://url",
                  "initialMessage": "Exact query or prompt this bot will send"
                }
              ]
            }
            Roles allowed: WEB_MESSENGER, DEEP_THINKER, CRITIC_VERIFIER, ACTION_COMPILER.
        """.trimIndent()

        val aiResponse = UniversalAiManager.executeAiPrompt(
            prompt = "User Goal: $cleanGoal\nPreferred Target/Search: $preferredSearchOrUrl\nDesign 4 specialized Nanobots that work step-by-step.",
            systemInstruction = plannerSystemPrompt
        )

        val result = aiResponse.getOrNull()?.let { rawJson ->
            parseAiPlannedSwarmJson(rawJson, cleanGoal, preferredSearchOrUrl, globalRulesBlock)
        } ?: buildSmartHeuristicSwarm(cleanGoal, preferredSearchOrUrl, globalRulesBlock)

        _isPlanning.value = false
        _plannerStatus.value =
            "✅ '${result.blueprintTitle}' প্রস্তুত! ${result.nodes.count { !it.isHomeNode }}টি ন্যানোবট ও গ্লোবাল রুল স্বয়ংক্রিয়ভাবে সেট হয়েছে।"
        result
    }

    /**
     * Builds a complete Swarm from one of the 1-Tap Preset Templates.
     */
    fun buildPresetSwarm(
        templateId: String,
        customTopicOrUrl: String = ""
    ): SwarmBlueprintResult {
        val globalRulesBlock = GlobalRuleVaultEngine.buildCompiledRulesBlock(forWebsitePaste = false)
        val topic = customTopicOrUrl.trim().ifBlank {
            presetTemplates.find { it.id == templateId }?.defaultGoalPrompt
                ?: "স্বয়ংক্রিয় ওয়েব রিসার্চ ও ডিপ-থিংক সমাধান"
        }

        val homeNode = createHomeNode()

        val workers: List<WorkflowNode> = when (templateId) {
            "FULLSTACK_CODE_SWARM" -> listOf(
                WorkflowNode(
                    title = "Bot 1: কোড ও ডক হান্টার",
                    x = 180f,
                    y = 180f,
                    rule = RuleType.WEB_AUTO_MESSAGE,
                    nanobotRole = NanobotRole.WEB_MESSENGER,
                    customRoleName = "🌐 Code Search & Harvester",
                    customRolePrompt = appendGlobalRules(
                        "তুমি একজন অটো-সার্চ ও কোড হারভেস্টার বট। প্রদত্ত টপিক বা ওয়েবসাইট থেকে সেরা আর্কিটেকচার, সোর্স কোড ও রিসোর্স খুঁজে আনো।",
                        globalRulesBlock
                    ),
                    targetInput = if (customTopicOrUrl.startsWith("http")) customTopicOrUrl else "search://$topic",
                    initialMessageToSend = topic
                ),
                WorkflowNode(
                    title = "Bot 2: ডিপ-থিংক সিস্টেম আর্কিটেক্ট",
                    x = 520f,
                    y = 180f,
                    rule = RuleType.DEEP_THINK_ANALYZE,
                    nanobotRole = NanobotRole.DEEP_THINKER,
                    customRoleName = "🧠 Deep-Think Code Architect",
                    customRolePrompt = appendGlobalRules(
                        "তুমি একজন সিনিয়র সফটওয়্যার আর্কিটেক্ট। Bot 1 যে তথ্য এনেছে তা ডিপ-থিংক করে সম্পূর্ণ ফাইল স্ট্রাকচার ও ১০০% পূর্ণাঙ্গ কোড লিখবে।",
                        globalRulesBlock
                    ),
                    targetInput = "Internal Deep-Think Brain",
                    initialMessageToSend = "প্রাপ্ত তথ্য থেকে সম্পূর্ণ কার্যকরী ও আধুনিক কোড তৈরি করো।"
                ),
                WorkflowNode(
                    title = "Bot 3: বাগ ও সিকিউরিটি ভেরিফায়ার",
                    x = 180f,
                    y = 430f,
                    rule = RuleType.DEEP_THINK_ANALYZE,
                    nanobotRole = NanobotRole.CRITIC_VERIFIER,
                    customRoleName = "🔍 Zero-Bug Verifier",
                    customRolePrompt = appendGlobalRules(
                        "তুমি একজন কঠোর কোড অডিটর। আগের বটের কোডে কোনো সিনট্যাক্স এরর, অসম্পূর্ণ অংশ বা লজিক্যাল বাগ আছে কি না চেক করে ফিক্স করো।",
                        globalRulesBlock
                    ),
                    targetInput = "Zero-Bug Audit Engine",
                    initialMessageToSend = "কোডটি নিখুঁতভাবে যাচাই করে বাগ-মুক্ত সংস্করণ দাও।"
                ),
                WorkflowNode(
                    title = "Bot 4: লাইভ স্যান্ডবক্স কম্পাইলার",
                    x = 520f,
                    y = 430f,
                    rule = RuleType.EXTRACT_CODE,
                    nanobotRole = NanobotRole.ACTION_COMPILER,
                    customRoleName = "⚡ Sandbox & Tab B Executor",
                    customRolePrompt = appendGlobalRules(
                        "চূড়ান্ত কোড ও প্রিভিউ প্রস্তুত করে স্ক্রিনে, লাইভ স্যান্ডবক্সে এবং Tab B কম্পাইলারে পাঠাও।",
                        globalRulesBlock
                    ),
                    targetInput = "Live Sandbox & Tab B",
                    initialMessageToSend = "চূড়ান্ত আউটপুট কম্পাইল ও প্রিভিউ করো।"
                )
            )

            "MULTI_AI_DEBATE_SWARM" -> listOf(
                WorkflowNode(
                    title = "Bot 1: প্রাইমারি রিসার্চ ও প্রস্তাবক",
                    x = 180f,
                    y = 200f,
                    rule = RuleType.WEB_AUTO_MESSAGE,
                    nanobotRole = NanobotRole.WEB_MESSENGER,
                    customRoleName = "🌐 Thesis Proposer Bot",
                    customRolePrompt = appendGlobalRules(
                        "প্রদত্ত বিষয়ে ওয়েব বা এআই প্ল্যাটফর্ম থেকে প্রাথমিক সমাধান ও যুক্তি সংগ্রহ করো।",
                        globalRulesBlock
                    ),
                    targetInput = if (customTopicOrUrl.startsWith("http")) customTopicOrUrl else "search://$topic",
                    initialMessageToSend = topic
                ),
                WorkflowNode(
                    title = "Bot 2: কাউন্টার-ক্রিটিক ও চ্যালেঞ্জার",
                    x = 520f,
                    y = 200f,
                    rule = RuleType.DEEP_THINK_ANALYZE,
                    nanobotRole = NanobotRole.CRITIC_VERIFIER,
                    customRoleName = "🔍 Devil's Advocate Critic",
                    customRolePrompt = appendGlobalRules(
                        "Bot 1-এর উত্তরের প্রতিটি পয়েন্টে কোথায় ঘাটতি, ঝুঁকি বা ভুল হতে পারে তা চ্যালেঞ্জ করো এবং বিকল্প উন্নত যুক্তি দাও।",
                        globalRulesBlock
                    ),
                    targetInput = "Counter-Analysis Brain",
                    initialMessageToSend = "প্রাথমিক উত্তরের গভীর সমালোচনা ও উন্নয়ন করো।"
                ),
                WorkflowNode(
                    title = "Bot 3: সুপ্রিম জাজ ও সিনথেসাইজার",
                    x = 350f,
                    y = 440f,
                    rule = RuleType.DEEP_THINK_ANALYZE,
                    nanobotRole = NanobotRole.DEEP_THINKER,
                    customRoleName = "⚖️ Master Consensus Judge",
                    customRolePrompt = appendGlobalRules(
                        "প্রস্তাবক বট এবং ক্রিটিক বটের উভয় যুক্তি মিলিয়ে সবচেয়ে নির্ভুল, পরীক্ষিত ও চূড়ান্ত সিদ্ধান্ত বাংলায় সাজিয়ে দাও।",
                        globalRulesBlock
                    ),
                    targetInput = "Consensus Synthesis Engine",
                    initialMessageToSend = "চূড়ান্ত ও সেরা সমাধানটি তৈরি করো।"
                )
            )

            else -> listOf(
                WorkflowNode(
                    title = "Bot 1: অটো-সার্চ ও ওয়েব ক্রলার",
                    x = 180f,
                    y = 180f,
                    rule = RuleType.WEB_AUTO_MESSAGE,
                    nanobotRole = NanobotRole.WEB_MESSENGER,
                    customRoleName = "🌐 Autonomous Search Crawler",
                    customRolePrompt = appendGlobalRules(
                        "তুমি স্বয়ংক্রিয়ভাবে সার্চ ইঞ্জিনে সার্চ করবে এবং সেরা ওয়েবসাইটগুলোর ভেতরে ঢুকে মূল তথ্য তুলে আনবে।",
                        globalRulesBlock
                    ),
                    targetInput = if (customTopicOrUrl.startsWith("http")) customTopicOrUrl else "search://$topic",
                    initialMessageToSend = topic
                ),
                WorkflowNode(
                    title = "Bot 2: ডিপ-থিংক অ্যানালিস্ট",
                    x = 520f,
                    y = 180f,
                    rule = RuleType.DEEP_THINK_ANALYZE,
                    nanobotRole = NanobotRole.DEEP_THINKER,
                    customRoleName = "🧠 Deep-Think Strategist",
                    customRolePrompt = appendGlobalRules(
                        "ক্রলার বট যে তথ্য এনেছে তা গভীরভাবে বিশ্লেষণ করে মূল ইনসাইট, লজিক এবং পরবর্তী পদক্ষেপ তৈরি করো।",
                        globalRulesBlock
                    ),
                    targetInput = "Deep-Think Core",
                    initialMessageToSend = "সংগৃহীত তথ্য ডিপ-থিংক করে বিশ্লেষণ করো।"
                ),
                WorkflowNode(
                    title = "Bot 3: ফ্যাক্ট চেকার ও রিপোর্ট কম্পাইলার",
                    x = 350f,
                    y = 430f,
                    rule = RuleType.DEEP_THINK_ANALYZE,
                    nanobotRole = NanobotRole.ACTION_COMPILER,
                    customRoleName = "⚡ Verified Action Compiler",
                    customRolePrompt = appendGlobalRules(
                        "সব তথ্য যাচাই করে সম্পূর্ণ রিপোর্ট বা কোড স্ক্রিনে এবং যৌথ মেমোরি ভল্টে সংরক্ষণের উপযোগী করো।",
                        globalRulesBlock
                    ),
                    targetInput = "Screen & Memory Vault",
                    initialMessageToSend = "চূড়ান্ত ভেরিফাইড আউটপুট তৈরি করো।"
                )
            )
        }

        val connections = wireSequentialConnections(workers, homeNode)
        val allNodes = workers + homeNode
        _plannerStatus.value = "✅ প্রিসেট সোয়ার্ম '${presetTemplates.find { it.id == templateId }?.title ?: templateId}' লোড হয়েছে!"

        return SwarmBlueprintResult(
            blueprintTitle = presetTemplates.find { it.id == templateId }?.title ?: "Autonomous Swarm",
            summaryDescription = "গ্লোবাল রুল ভল্ট ও স্বয়ংক্রিয় পাইপলাইন সহ ${workers.size}টি ন্যানোবট প্রস্তুত।",
            nodes = allNodes,
            connections = connections
        )
    }

    private fun parseAiPlannedSwarmJson(
        rawText: String,
        cleanGoal: String,
        preferredSearchOrUrl: String,
        globalRulesBlock: String
    ): SwarmBlueprintResult? {
        return try {
            val jsonStart = rawText.indexOf('{')
            val jsonEnd = rawText.lastIndexOf('}')
            if (jsonStart < 0 || jsonEnd <= jsonStart) return null
            val obj = JSONObject(rawText.substring(jsonStart, jsonEnd + 1))

            val bpTitle = obj.optString("blueprintTitle", "AI মাস্টার প্ল্যানড সোয়ার্ম")
            val summary = obj.optString("summaryDescription", cleanGoal)
            val botsArr: JSONArray = obj.optJSONArray("bots") ?: return null
            if (botsArr.length() == 0) return null

            val positions = listOf(
                180f to 180f,
                520f to 180f,
                180f to 430f,
                520f to 430f,
                350f to 520f
            )

            val workers = mutableListOf<WorkflowNode>()
            val count = botsArr.length().coerceAtMost(5)
            for (i in 0 until count) {
                val b = botsArr.getJSONObject(i)
                val roleStr = b.optString("role", if (i == 0) "WEB_MESSENGER" else "DEEP_THINKER")
                val role = runCatching { NanobotRole.valueOf(roleStr) }.getOrDefault(
                    if (i == 0) NanobotRole.WEB_MESSENGER else NanobotRole.DEEP_THINKER
                )
                val ruleType = when (role) {
                    NanobotRole.WEB_MESSENGER -> RuleType.WEB_AUTO_MESSAGE
                    NanobotRole.ACTION_COMPILER -> RuleType.EXTRACT_CODE
                    else -> RuleType.DEEP_THINK_ANALYZE
                }
                val pos = positions.getOrElse(i) { 300f to (200f + i * 120f) }
                val rawTarget = b.optString("targetInput", "").ifBlank {
                    if (i == 0) {
                        preferredSearchOrUrl.ifBlank { "search://$cleanGoal" }
                    } else {
                        "Deep-Think Engine"
                    }
                }

                workers.add(
                    WorkflowNode(
                        title = b.optString("title", "Bot ${i + 1}"),
                        x = pos.first,
                        y = pos.second,
                        rule = ruleType,
                        nanobotRole = role,
                        customRoleName = b.optString("customRoleName", role.badge),
                        customRolePrompt = appendGlobalRules(
                            b.optString("customRolePrompt", role.defaultSystemPrompt),
                            globalRulesBlock
                        ),
                        targetInput = rawTarget,
                        initialMessageToSend = b.optString("initialMessage", cleanGoal)
                    )
                )
            }

            val homeNode = createHomeNode()
            val connections = wireSequentialConnections(workers, homeNode)
            SwarmBlueprintResult(
                blueprintTitle = bpTitle,
                summaryDescription = summary,
                nodes = workers + homeNode,
                connections = connections
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun buildSmartHeuristicSwarm(
        cleanGoal: String,
        preferredSearchOrUrl: String,
        globalRulesBlock: String
    ): SwarmBlueprintResult {
        val searchTarget = when {
            preferredSearchOrUrl.startsWith("http://") || preferredSearchOrUrl.startsWith("https://") ->
                preferredSearchOrUrl
            preferredSearchOrUrl.isNotBlank() ->
                "search://$preferredSearchOrUrl"
            else ->
                "search://$cleanGoal"
        }

        val homeNode = createHomeNode()
        val workers = listOf(
            WorkflowNode(
                title = "Bot 1: অটো-সার্চ ও ওয়েব মেসেঞ্জার",
                x = 180f,
                y = 180f,
                rule = RuleType.WEB_AUTO_MESSAGE,
                nanobotRole = NanobotRole.WEB_MESSENGER,
                customRoleName = "🌐 Web Search & Harvester",
                customRolePrompt = appendGlobalRules(
                    "তুমি স্বয়ংক্রিয়ভাবে '$searchTarget'-এ সার্চ বা মেসেজ করে মূল তথ্য, ডেটা ও কোড সংগ্রহ করবে।",
                    globalRulesBlock
                ),
                targetInput = searchTarget,
                initialMessageToSend = cleanGoal
            ),
            WorkflowNode(
                title = "Bot 2: ডিপ-থিংক লজিক আর্কিটেক্ট",
                x = 520f,
                y = 180f,
                rule = RuleType.DEEP_THINK_ANALYZE,
                nanobotRole = NanobotRole.DEEP_THINKER,
                customRoleName = "🧠 Deep-Think Architect",
                customRolePrompt = appendGlobalRules(
                    "আগের বটের সংগৃহীত তথ্য ডিপ-থিংক করে বিশ্লেষণ করো এবং ব্যবহারকারীর লক্ষ্যের ('${cleanGoal.take(80)}') জন্য সম্পূর্ণ সমাধান ও স্ট্রাকচার তৈরি করো।",
                    globalRulesBlock
                ),
                targetInput = "Deep-Think Core",
                initialMessageToSend = "সংগৃহীত তথ্যের ওপর ডিপ-থিংক করে পূর্ণাঙ্গ সমাধান তৈরি করো।"
            ),
            WorkflowNode(
                title = "Bot 3: কোয়ালিটি ও রুল ভেরিফায়ার",
                x = 180f,
                y = 430f,
                rule = RuleType.DEEP_THINK_ANALYZE,
                nanobotRole = NanobotRole.CRITIC_VERIFIER,
                customRoleName = "🔍 Rule & Quality Verifier",
                customRolePrompt = appendGlobalRules(
                    "আগের ধাপের আউটপুট আমাদের স্থায়ী গ্লোবাল রুল ১০০% মেনেছে কি না এবং কোনো অংশ অসম্পূর্ণ আছে কি না তা পরীক্ষা করে নিখুঁত করো।",
                    globalRulesBlock
                ),
                targetInput = "Rule Verification Engine",
                initialMessageToSend = "আউটপুট যাচাই ও পরিমার্জন করো।"
            ),
            WorkflowNode(
                title = "Bot 4: ফাইনাল কম্পাইলার ও স্যান্ডবক্সার",
                x = 520f,
                y = 430f,
                rule = RuleType.EXTRACT_CODE,
                nanobotRole = NanobotRole.ACTION_COMPILER,
                customRoleName = "⚡ Action & Sandbox Compiler",
                customRolePrompt = appendGlobalRules(
                    "চূড়ান্ত ফলাফল স্ক্রিনে দেখানোর এবং লাইভ স্যান্ডবক্স বা Tab B-তে রান করার উপযোগী করো।",
                    globalRulesBlock
                ),
                targetInput = "Live Sandbox & Tab B",
                initialMessageToSend = "চূড়ান্ত আউটপুট ডেলিভারি দাও।"
            )
        )

        val connections = wireSequentialConnections(workers, homeNode)
        return SwarmBlueprintResult(
            blueprintTitle = "অটো-প্ল্যানড ৪-বট ডিপ-থিংক সোয়ার্ম",
            summaryDescription = cleanGoal,
            nodes = workers + homeNode,
            connections = connections
        )
    }

    private fun createHomeNode(): WorkflowNode = WorkflowNode(
        id = "home_base_node",
        title = "Home Base (Our App)",
        x = 350f,
        y = 700f,
        rule = RuleType.EXTRACT_CODE,
        radius = 78f,
        isHomeNode = true,
        nanobotRole = NanobotRole.ACTION_COMPILER,
        customRoleName = "🏰 Master Screen & Checkpoint",
        status = BotExecutionStatus.IDLE
    )

    private fun wireSequentialConnections(
        workers: List<WorkflowNode>,
        homeNode: WorkflowNode
    ): List<NodeConnection> {
        val conns = mutableListOf<NodeConnection>()
        for (i in workers.indices) {
            val current = workers[i]
            val next = if (i + 1 < workers.size) workers[i + 1] else homeNode
            current.nextTargetNodeId = next.id
            conns.add(NodeConnection(fromNodeId = current.id, toNodeId = next.id))
        }
        return conns
    }

    private fun appendGlobalRules(basePrompt: String, globalRulesBlock: String): String {
        if (globalRulesBlock.isBlank()) return basePrompt
        return "$basePrompt\n\n$globalRulesBlock"
    }
}
