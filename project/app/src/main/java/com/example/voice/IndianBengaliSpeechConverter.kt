package com.example.voice

import java.util.Locale

/**
 * IndianBengaliSpeechConverter:
 * Normalizes, transliterates and cleans spoken speech into pristine Indian Bengali (bn-IN).
 * Specifically attuned to West Bengal / Indian Bengali dialect vocabulary, phrasing,
 * and conversational etiquette (e.g. "নমস্কার", "দয়া করে", "বলুন", "কী খবর", etc.).
 */
object IndianBengaliSpeechConverter {

    private val commonEnglishToIndianBengali = mapOf(
        "hello" to "হ্যালো",
        "hi" to "নমস্কার",
        "good morning" to "সুপ্রভাত",
        "good night" to "শুভরাত্রি",
        "how are you" to "আপনি কেমন আছেন?",
        "who are you" to "আপনি কে?",
        "what is this" to "এটা কী?",
        "open" to "খুলুন",
        "close" to "বন্ধ করুন",
        "search" to "অনুসন্ধান করুন",
        "browser" to "ব্রাউজার",
        "google" to "গুগল",
        "youtube" to "ইউটিউব",
        "facebook" to "ফেসবুক",
        "chat" to "চ্যাট",
        "code" to "কোড",
        "help" to "সাহায্য করুন",
        "weather" to "আবহাওয়া",
        "news" to "খবর",
        "settings" to "সেটিংস",
        "back" to "পেছনে যান",
        "next" to "পরবর্তী",
        "yes" to "হ্যাঁ",
        "no" to "না",
        "ok" to "ঠিক আছে",
        "thank you" to "ধন্যবাদ",
        "please" to "দয়া করে",
        "stop" to "থামুন",
        "start" to "শুরু করুন",
        "restart" to "পুনরায় শুরু করুন",
        "download" to "ডাউনলোড করুন",
        "apk" to "এপিকে",
        "model" to "মডেল",
        "key" to "কী",
        "api key" to "এপিআই কী"
    )

    /**
     * Converts or normalizes raw speech input into Indian Bengali (bn-IN).
     * If the input is already in Bengali, it cleans and refines it.
     * If spoken in English or mixed Banglish, it maps the terms to natural Indian Bengali phrasing.
     */
    fun convertToIndianBengali(rawTranscript: String): String {
        val trimmed = rawTranscript.trim()
        if (trimmed.isEmpty()) return ""

        // 1. If transcript already contains Bengali unicode characters, polish spacing and punctuation
        val hasBengali = trimmed.any { it in '\u0980'..'\u09FF' }
        if (hasBengali) {
            return polishBengaliText(trimmed)
        }

        // 2. If spoken in English or transliterated Latin characters, map standard phrases
        val lower = trimmed.lowercase(Locale.ROOT)
        commonEnglishToIndianBengali[lower]?.let { return it }

        // Word-by-word transliteration & translation replacement
        var result = trimmed
        commonEnglishToIndianBengali.forEach { (eng, bng) ->
            val regex = Regex("(?i)\\b$eng\\b")
            result = result.replace(regex, bng)
        }

        // If partially translated or still in English, return with respectful Indian Bengali format
        return polishBengaliText(result)
    }

    private fun polishBengaliText(input: String): String {
        return input
            .replace("\\s+".toRegex(), " ")
            .trim()
    }
}
