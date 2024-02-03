package com.example.plm2

import domain.Track
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
class SearchHistory(private val sharedPreferences: SharedPreferences) {

    fun getSearchHistory(): List<Track> {
        val json = sharedPreferences.getString(HISTORY_KEY, null)
        return json?.let {
            val type = object : TypeToken<List<Track>>() {}.type
            gson.fromJson<List<Track>>(it, type)
        } ?: emptyList()
    }
    fun addTrackToHistory(track: Track) {
        val currentHistory = getSearchHistory().toMutableList()

        currentHistory.removeIf { it.itemId == track.itemId }
        currentHistory.add(0, track)
        if (currentHistory.size > MAX_HISTORY_SIZE) {
            currentHistory.removeAt(MAX_HISTORY_SIZE)
        }
        Log.d("SearchHistory", "Adding track to history: ${track.trackName}")
        saveSearchHistory(currentHistory)
    }
    fun clearSearchHistory() {
        Log.d("SearchHistory", "Clearing search history")
        saveSearchHistory(emptyList())
    }
    private fun saveSearchHistory(history: List<Track>) {
        val json = Gson().toJson(history)
        sharedPreferences.edit().putString(HISTORY_KEY, json).apply()
        Log.d("SearchHistory", "Saving search history: ${history.size} items")
    }
    companion object {
        const val MAX_HISTORY_SIZE = 10
        const val HISTORY_KEY = "search_history_key"
        val gson = Gson()
    }
    data class HistoryItem(val trackId: String) // Идентификатор элемента истории поиска
    data class TrackItem(val trackId: String) // Идентификатор элемента списка треков
}

