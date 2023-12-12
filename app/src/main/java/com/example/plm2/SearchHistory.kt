package com.example.plm2

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken




class SearchHistory(private val sharedPreferences: SharedPreferences) {

    companion object {
        const val MAX_HISTORY_SIZE = 10
        const val HISTORY_KEY = "search_history"
        val gson = Gson()
    }

    fun getSearchHistory(): List<Track> {
        val json = sharedPreferences.getString(HISTORY_KEY, null)
        return json?.let {
            val type = object : TypeToken<List<Track>>() {}.type
            gson.fromJson<List<Track>>(it, type)
        } ?: emptyList()
    }

    fun addTrackToHistory(track: Track) {
        val currentHistory = getSearchHistory().toMutableList()
        currentHistory.removeIf { it.trackId == track.trackId }
        currentHistory.add(0, track)
        if (currentHistory.size > MAX_HISTORY_SIZE) {
            currentHistory.removeAt(MAX_HISTORY_SIZE)
        }
        saveSearchHistory(currentHistory)
    }

    fun clearSearchHistory() {
        saveSearchHistory(emptyList())
    }

    private fun saveSearchHistory(history: List<Track>) {
        val json = Gson().toJson(history)
        sharedPreferences.edit().putString("search_history", json).apply()
    }
}
