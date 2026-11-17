package com.example.model

import java.util.UUID

enum class RuleType(val title: String, val description: String) {
    WEB_AUTO_MESSAGE("Web Messenger", "নিজে ওয়েবসাইটে/AI প্ল্যাটফর্মে মেসেজ করে রিপ্লাই আনে"),
    DEEP_THINK_ANALYZE("Deep-Thinker", "আগের বটের রিপ্লাই ডিপ-থিংক করে পরের মেসেজ/সিদ্ধান্ত বানায়"),
    EXTRACT_CODE("Extract Code", "Extract code blocks via Regex"),
    TAKE_SCREENSHOT("Vision Capture", "Capture WebView screenshot as Base64"),
    APPEND_TEXT("Prompt Chain", "Append custom text to context"),
    POINTER_PASS("Pointer Pass", "Zero-RAM pointer reference handoff")
}

enum class NanobotRole(
    val badge: String,
    val banglaTitle: String,
    val description: String,
    val defaultSystemPrompt: String
) {
    WEB_MESSENGER(
        badge = "🌐 Web Messenger",
        banglaTitle = "ওয়েব/প্ল্যাটফর্ম মেসেঞ্জার বট",
        description = "আপনি কোনো ওয়েবসাইটে না ঢুকলেও নিজে ওই সাইটে বা AI-তে মেসেজ পাঠিয়ে তার রিপ্লাই তুলে আনে।",
        defaultSystemPrompt = "তুমি একজন স্মার্ট ওয়েব মেসেঞ্জার ন্যানোবট। প্রদত্ত টাস্ক বা প্রশ্নটি লক্ষ্যের ওয়েবসাইট বা এআই প্ল্যাটফর্মে পাঠাও এবং সম্পূর্ণ উত্তরটি সংগ্রহ করে পরের ন্যানোবটের জন্য সাজিয়ে দাও।"
    ),
    DEEP_THINKER(
        badge = "🧠 Deep-Thinker",
        banglaTitle = "ডিপ-থিংকার ও বিশ্লেষক বট",
        description = "আগের বটের আনা রিপ্লাই গভীরভাবে (Deep-Think) বিশ্লেষণ করে পরবর্তী প্রশ্ন, লজিক বা সমাধান তৈরি করে।",
        defaultSystemPrompt = "তুমি একজন ডিপ-থিংকিং আর্কিটেক্ট ন্যানোবট। আগের বট যে রিপ্লাই বা তথ্য এনেছে তা গভীরভাবে বিশ্লেষণ করো, এর ভেতরের মূল পয়েন্ট ও ঘাটতি খুঁজে বের করো এবং পরবর্তী ন্যানোবট কী মেসেজ পাঠাবে বা কী কাজ করবে তার নিখুঁত নির্দেশনা তৈরি করো।"
    ),
    CRITIC_VERIFIER(
        badge = "🔍 Critic & Verifier",
        banglaTitle = "ভেরিফায়ার ও বাগ চেকার বট",
        description = "আগের বটের আউটপুট বা কোড যাচাই করে ভুল থাকলে ঠিক করে এবং পরবর্তী ধাপে পাঠায়।",
        defaultSystemPrompt = "তুমি একজন কঠোর কোয়ালিটি ভেরিফায়ার ন্যানোবট। আগের ধাপের আউটপুট নিখুঁত কি না পরীক্ষা করো, কোনো ভুল বা অসম্পূর্ণতা থাকলে তা সংশোধন করে উন্নত আউটপুট দাও।"
    ),
    ACTION_COMPILER(
        badge = "⚡ Action Executor",
        banglaTitle = "অ্যাকশন ও কম্পাইলার বট",
        description = "চূড়ান্ত উত্তর বা কোড স্ক্রিনে দেখায় এবং সরাসরি ব্রাউজার/কম্পাইলারে রান করে।",
        defaultSystemPrompt = "তুমি একজন অ্যাকশন এক্সিকিউটর ন্যানোবট। প্রাপ্ত আউটপুট থেকে কার্যকরী কোড বা চূড়ান্ত ফলাফল আলাদা করে স্ক্রিনে এবং টার্গেট ট্যাবে প্রয়োগ করার উপযোগী করো।"
    ),
    CUSTOM_AGENT(
        badge = "🤖 Custom Role",
        banglaTitle = "নিজস্ব কাস্টম রোল (Custom Bot)",
        description = "আপনার লিখে দেওয়া যেকোনো নিজস্ব রোল, চরিত্র ও নিয়ম ১০০% মেনে কাজ করবে।",
        defaultSystemPrompt = "ব্যবহারকারী তোমাকে যে কাস্টম রোল ও নির্দেশনা দিয়েছেন তা অক্ষরে অক্ষরে পালন করো।"
    )
}

enum class BotExecutionStatus(val label: String) {
    IDLE("প্রস্তুত (IDLE)"),
    MESSAGING_WEB("ওয়েবে মেসেজ পাঠাচ্ছে..."),
    WAITING_REPLY("রিপ্লাইয়ের অপেক্ষায়..."),
    DEEP_THINKING("ডিপ-থিংক করছে 🧠..."),
    COMPLETED("সম্পন্ন ✅"),
    PAUSED("পজ করা ⏸"),
    ERROR("ত্রুটি ⚠️")
}

data class WorkflowNode(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var x: Float,
    var y: Float,
    var rule: RuleType = RuleType.WEB_AUTO_MESSAGE,
    var customParameter: String = "",
    val radius: Float = 66f,
    var targetInput: String = "https://chatgpt.com",
    var brainPower: Int = 85,
    var isCloudExecution: Boolean = false,
    var cloudWebhookUrl: String = "",
    var autoFlushMemory: Boolean = true,
    var outputPointerId: String = "",
    var is24x7Loop: Boolean = false,
    var loopIntervalMinutes: Int = 15,
    var isHomeNode: Boolean = false,
    // Step 1-5 Nanobot Swarm Extensions
    var nanobotRole: NanobotRole = NanobotRole.WEB_MESSENGER,
    var customRoleName: String = "",
    var customRolePrompt: String = "",
    var initialMessageToSend: String = "",
    var lastReplyReceived: String = "",
    var status: BotExecutionStatus = BotExecutionStatus.IDLE,
    var nextTargetNodeId: String = ""
)

data class NodeConnection(
    val id: String = UUID.randomUUID().toString(),
    val fromNodeId: String,
    val toNodeId: String
)

data class NanobotRelayEvent(
    val id: String = UUID.randomUUID().toString(),
    val stepNumber: Int,
    val botId: String,
    val botTitle: String,
    val roleLabel: String,
    val targetWebsiteOrAi: String,
    val messageSent: String,
    val replyOrThoughtOutput: String,
    val status: BotExecutionStatus,
    val timestamp: Long = System.currentTimeMillis(),
    val nextBotTitle: String = ""
)
