package com.example.engine

import java.util.regex.Pattern

object RegexSurgeon {
    private val CODE_FENCE_PATTERN: Pattern = Pattern.compile(
        "```(?:[a-zA-Z0-9_+\\-]+)?\\s*([\\s\\S]*?)```",
        Pattern.MULTILINE
    )

    fun extractCodeOnly(rawText: String): String {
        val matcher = CODE_FENCE_PATTERN.matcher(rawText)
        val extractedBlocks = mutableListOf<String>()
        while (matcher.find()) {
            val block = matcher.group(1)?.trim().orEmpty()
            if (block.isNotEmpty()) {
                extractedBlocks.add(block)
            }
        }
        return if (extractedBlocks.isNotEmpty()) {
            extractedBlocks.joinToString("\n\n// --- Next Extracted Code Block ---\n\n")
        } else {
            rawText.trim()
        }
    }
}
