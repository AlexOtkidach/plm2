package com.example.plm2.data.local

import android.content.SharedPreferences
import com.example.plm2.domain.model.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistory(private val sharedPreferences: SharedPreferences) {

    fun getSearchHistory(): List<Track> {
        val json = sharedPreferences.getString(HISTORY_KEY, null)
        return json?.let {
            val type = object : TypeToken<List<Track>>() {}.type
            Gson().fromJson(it, type)
        } ?: emptyList()
    }

    fun addTrackToHistory(track: Track) {
        val currentHistory = getSearchHistory().toMutableList()
        currentHistory.removeIf { it.itemId == track.itemId }
        currentHistory.add(0, track)
        if (currentHistory.size > MAX_HISTORY_SIZE) {
            currentHistory.removeAt(currentHistory.size - 1)
        }
        saveSearchHistory(currentHistory)
    }

    fun clearSearchHistory() {
        saveSearchHistory(emptyList())
    }

    private fun saveSearchHistory(history: List<Track>) {
        val json = Gson().toJson(history)
        sharedPreferences.edit().putString(HISTORY_KEY, json).apply()
    }

    companion object {
        const val MAX_HISTORY_SIZE = 10
        const val HISTORY_KEY = "search_history_key"
    }
}
