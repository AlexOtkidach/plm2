package com.example.plm2.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.plm2.domain.model.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPreferencesManager(context: Context) {
    val sharedPreferences: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun addTrackToHistory(track: Track) {
        val history = getSearchHistory().toMutableList()
        history.removeIf { it.itemId == track.itemId }
        history.add(0, track)
        if (history.size > MAX_HISTORY_SIZE) {
            history.removeAt(history.size - 1)
        }
        saveSearchHistory(history)
    }

    fun getSearchHistory(): List<Track> {
        val json = sharedPreferences.getString(HISTORY_KEY, null)
        return json?.let {
            val type = object : TypeToken<List<Track>>() {}.type
            Gson().fromJson(it, type)
        } ?: emptyList()
    }

    fun clearSearchHistory() {
        sharedPreferences.edit().remove(HISTORY_KEY).apply()
    }

    fun setDarkTheme(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(DARK_THEME_KEY, enabled).apply()
    }

    fun isDarkTheme(): Boolean {
        return sharedPreferences.getBoolean(DARK_THEME_KEY, false)
    }

    private fun saveSearchHistory(history: List<Track>) {
        val json = Gson().toJson(history)
        sharedPreferences.edit().putString(HISTORY_KEY, json).apply()
    }

    companion object {
        private const val MAX_HISTORY_SIZE = 10
        private const val HISTORY_KEY = "search_history_key"
        private const val DARK_THEME_KEY = "isDarkTheme"
    }
}
