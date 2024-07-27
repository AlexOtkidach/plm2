package com.example.plm2.data.local

import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.plm2.domain.model.Track

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
        // Удаляем трек, если он уже есть в истории
        currentHistory.removeIf { it.itemId == track.itemId }
        currentHistory.add(0, track)
        // Ограничиваем размер истории
        if (currentHistory.size > MAX_HISTORY_SIZE) {
            currentHistory.removeAt(currentHistory.size - 1)
        }
        Log.d("SearchHistory", "Adding track to history: ${track.trackName}")
        saveSearchHistory(currentHistory)
    }

    fun clearSearchHistory() {
        Log.d("SearchHistory", "Clearing search history")
        saveSearchHistory(emptyList())
    }

    private fun saveSearchHistory(history: List<Track>) {
        val json = gson.toJson(history)
        sharedPreferences.edit().putString(HISTORY_KEY, json).apply()
        Log.d("SearchHistory", "Saving search history: ${history.size} items")
    }

    companion object {
        const val MAX_HISTORY_SIZE = 10
        const val HISTORY_KEY = "search_history_key"
        val gson = Gson()
    }
}

