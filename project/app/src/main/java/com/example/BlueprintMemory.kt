package com.example

import android.content.Context
import android.content.SharedPreferences

enum class BrowserTab {
    TAB_A,
    TAB_B
}

object BlueprintMemory {
    private const val PREF_NAME = "skrl2_blueprint_memory"
    private const val KEY_CONTEXT_NOTE = "key_context_note"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveBlueprintContext(context: Context, note: String) {
        getPrefs(context).edit().putString(KEY_CONTEXT_NOTE, note).apply()
    }

    fun getBlueprintContext(context: Context): String {
        return getPrefs(context).getString(KEY_CONTEXT_NOTE, "") ?: ""
    }

    fun clear(context: Context) {
        getPrefs(context).edit().remove(KEY_CONTEXT_NOTE).apply()
    }
}
