package com.example.model

import java.util.UUID

enum class AiProviderType(
    val displayName: String,
    val company: String,
    val defaultModel: String,
    val defaultVersion: String,
    val endpointPlaceholder: String = ""
) {
    GEMINI("Google Gemini", "Google DeepMind", "gemini-3.1-flash-lite-preview", "v1beta"),
    GEMINI_FLASH("Google Gemini Flash", "Google DeepMind", "gemini-3.1-flash-lite-preview", "v1beta"),
    OPENAI("OpenAI (ChatGPT)", "OpenAI", "gpt-4o-mini", "v1"),
    DEEPSEEK("DeepSeek AI", "DeepSeek", "deepseek-chat", "v1"),
    GROQ("Groq (LPU Ultra-Fast)", "Groq", "llama-3.3-70b-versatile", "openai/v1"),
    CLAUDE("Anthropic Claude", "Anthropic", "claude-3-5-sonnet-latest", "v1"),
    OPENROUTER("OpenRouter (Universal)", "OpenRouter", "openai/gpt-4o-mini", "api/v1"),
    MISTRAL("Mistral AI", "Mistral", "mistral-small-latest", "v1"),
    PERPLEXITY("Perplexity AI", "Perplexity", "sonar", "v1"),
    XAI_GROK("xAI (Grok)", "xAI", "grok-2-latest", "v1"),
    TOGETHER("Together AI", "Together", "meta-llama/Llama-3.3-70B-Instruct-Turbo", "v1"),
    CEREBRAS("Cerebras Wafer-Scale", "Cerebras", "llama-3.3-70b", "v1"),
    CUSTOM("Custom AI Endpoint", "Custom Provider", "custom-model", "v1", "https://api.your-ai.com/v1/chat/completions")
}

data class KeyValidationResult(
    val isValid: Boolean,
    val detectedProvider: AiProviderType,
    val detectedModel: String,
    val availableModels: List<String> = emptyList(),
    val latencyMs: Long = 0L,
    val statusMessage: String = "",
    val errorMessage: String = ""
)

enum class AiKeySpecialistRole(
    val banglaTitle: String,
    val shortBadge: String,
    val description: String,
    val autoRoleSystemRule: String
) {
    OMNI_MASTER_BRAIN(
        banglaTitle = "👑 মেইন ব্রেইন (প্রধান সহকারী)",
        shortBadge = "MAIN BRAIN",
        description = "সাধারণ আলাপচারিতা, প্রধান সিদ্ধান্ত ও মূল কাজ সম্পাদন করবে",
        autoRoleSystemRule = ""
    ),
    CODING_ARCHITECT(
        banglaTitle = "💻 কোডিং ও প্রজেক্ট স্পেশালিস্ট",
        shortBadge = "CODING PRO",
        description = "যেকোনো কোডিং ও ফাইল তৈরির কাজ নিজে থেকে পরিচালনা করবে",
        autoRoleSystemRule = ""
    ),
    FAST_REFLEX_WORKER(
        banglaTitle = "⚡ ফাস্ট স্পিড ওয়ার্কার",
        shortBadge = "FAST WORK",
        description = "ছোটখাটো সাধারণ প্রশ্ন ও দ্রুত রিপ্লাই ০.৫ সেকেন্ডে দেবে",
        autoRoleSystemRule = ""
    ),
    DEEP_RESEARCH_SCOUT(
        banglaTitle = "🌐 ডিপ রিসার্চ স্পেশালিস্ট",
        shortBadge = "RESEARCH",
        description = "ওয়েব অনুসন্ধান ও বিশ্লেষণ",
        autoRoleSystemRule = ""
    ),
    QA_VERIFIER_CRITIC(
        banglaTitle = "🛡️ কোড ও বাগ ভেরিফায়ার",
        shortBadge = "QA CRITIC",
        description = "বাগ ফিক্স ও ভেরিফিকেশন",
        autoRoleSystemRule = ""
    ),
    BACKUP_STANDBY(
        banglaTitle = "🔋 ব্যাকআপ কী (অন্য কী ফেল করলে চলবে)",
        shortBadge = "STANDBY",
        description = "প্রধান কী-র কোটা শেষ হলে স্বয়ংক্রিয়ভাবে কাজ ধরবে",
        autoRoleSystemRule = ""
    ),
    IGNORED_PAUSED(
        banglaTitle = "🚫 বন্ধ / পজড",
        shortBadge = "PAUSED",
        description = "এই কী সাময়িকভাবে বন্ধ থাকবে",
        autoRoleSystemRule = ""
    )
}

data class GeminiModelOption(
    val modelId: String,
    val title: String,
    val badge: String,
    val description: String
)

val RECOMMENDED_GEMINI_MODELS = listOf(
    GeminiModelOption(
        modelId = "gemini-3.1-flash-lite-preview",
        title = "Gemini 3.1 Flash Lite",
        badge = "ULTRA FAST • DEFAULT",
        description = "সবচেয়ে দ্রুত (০.৬ সেকেন্ড) ও ভয়েস-ট্রান্সক্রিপশন সাপোর্টেড ডিফল্ট মডেল"
    ),
    GeminiModelOption(
        modelId = "gemini-3.5-flash-lite",
        title = "Gemini 3.5 Flash Lite",
        badge = "VERIFIED STABLE",
        description = "স্থিতিশীল ও ৪0৪-মুক্ত ফ্ল্যাগশিপ মডেল"
    ),
    GeminiModelOption(
        modelId = "gemini-3-flash-preview",
        title = "Gemini 3.0 Flash",
        badge = "DEEP THINK + AUDIO",
        description = "উন্নত বিশ্লেষণ, কোডিং ও অডিও বোঝার ক্ষমতা"
    ),
    GeminiModelOption(
        modelId = "gemini-3.1-pro-preview",
        title = "Gemini 3.1 Pro",
        badge = "PRO REASONING",
        description = "জটিল প্রো-লেভেল লজিক ও আর্কিটেকচার"
    )
)

data class AiKeyProfile(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val apiKey: String,
    val provider: AiProviderType = AiProviderType.GEMINI,
    val modelName: String = provider.defaultModel,
    val availableModels: List<String> = emptyList(),
    val apiVersion: String = provider.defaultVersion,
    val customEndpoint: String = "",
    val customSystemInstruction: String = "",
    val isActive: Boolean = false,
    val specialistRole: AiKeySpecialistRole = AiKeySpecialistRole.OMNI_MASTER_BRAIN,
    val sameKeyAutoFallback: Boolean = true,
    val lastValidatedStatus: String = "UNTESTED", // "VALID", "INVALID", "UNTESTED"
    val lastValidationDetails: String = ""
)

object AiKeyDetector {
    /**
     * Automatically identifies which AI company and model family a key belongs to by prefix/signature.
     * Note: Actual models are dynamically fetched from the provider's live server upon validation!
     */
    fun detectProviderFromKey(rawKey: String): Pair<AiProviderType, String> {
        val trimmed = rawKey.trim()
        return when {
            trimmed.startsWith("AIzaSy") || trimmed.startsWith("AIza") || trimmed.startsWith("AQ.") ->
                Pair(AiProviderType.GEMINI_FLASH, "gemini-3.1-flash-lite-preview")

            trimmed.startsWith("sk-ant-") ->
                Pair(AiProviderType.CLAUDE, "claude-3-5-sonnet-latest")

            trimmed.startsWith("gsk_") ->
                Pair(AiProviderType.GROQ, "llama-3.3-70b-versatile")

            trimmed.startsWith("sk-or-") ->
                Pair(AiProviderType.OPENROUTER, "openai/gpt-4o-mini")

            trimmed.startsWith("pplx-") ->
                Pair(AiProviderType.PERPLEXITY, "sonar")

            trimmed.startsWith("xai-") ->
                Pair(AiProviderType.XAI_GROK, "grok-2-latest")

            trimmed.startsWith("csk-") ->
                Pair(AiProviderType.CEREBRAS, "llama-3.3-70b")

            // DeepSeek API keys are typically sk- followed by 32 hex chars (total ~35-36 chars)
            trimmed.startsWith("sk-") && trimmed.length in 30..37 && !trimmed.startsWith("sk-proj-") ->
                Pair(AiProviderType.DEEPSEEK, "deepseek-chat")

            // OpenAI API keys are typically sk-proj- or sk- followed by 48+ chars
            trimmed.startsWith("sk-proj-") || (trimmed.startsWith("sk-") && trimmed.length >= 40) ->
                Pair(AiProviderType.OPENAI, "gpt-4o-mini")

            trimmed.startsWith("sk-") ->
                Pair(AiProviderType.OPENAI, "gpt-4o-mini")

            else ->
                Pair(AiProviderType.GEMINI_FLASH, "gemini-3.1-flash-lite-preview")
        }
    }
}
