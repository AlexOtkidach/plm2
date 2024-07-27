package com.example.plm2.data.local

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesManager(context: Context) {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun addTrackToHistory(track: String) {
        val history = getSearchHistory().toMutableList()
        if (!history.contains(track)) {
            history.add(track)
            sharedPreferences.edit().putStringSet("search_history", history.toSet()).apply()
        }
    }

    fun getSearchHistory(): List<String> {
        return sharedPreferences.getStringSet("search_history", emptySet())?.toList() ?: emptyList()
    }

    fun clearSearchHistory() {
        sharedPreferences.edit().remove("search_history").apply()
    }

    fun setDarkTheme(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("isDarkTheme", enabled).apply()
    }

    fun isDarkTheme(): Boolean {
        return sharedPreferences.getBoolean("isDarkTheme", false)
    }
}
