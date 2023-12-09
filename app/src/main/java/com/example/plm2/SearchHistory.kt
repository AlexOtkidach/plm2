package com.example.plm2

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken



class SearchHistory(private val sharedPreferences: SharedPreferences) {

    companion object {
        const val MAX_HISTORY_SIZE = 10
        const val HISTORY_KEY = "search_history"
    }

    // История поиска
    fun getSearchHistory(): List<Track> {
        val json = sharedPreferences.getString(HISTORY_KEY, null)
        return json?.let {
            val type = object : TypeToken<List<Track>>() {}.type
            Gson().fromJson<List<Track>>(it, type)
        } ?: emptyList()
    }

    // Добавление строки поиска в историю
    fun addSearchQuery(query: String) {
        val track = Track(
            trackName = query,
            artistName = "", // Пустые значения, так как это история поиска
            trackTime = "",
            artworkUrl100 = "",
            trackId = ""
        )
        addTrackToHistory(track)
    }

    // Добавление трека в историю поиска
    fun addTrackToHistory(track: Track) {
        val currentHistory = getSearchHistory().toMutableList()

        // Удаление предыдущей записи о треке
        currentHistory.removeIf { it.trackId == track.trackId }

        // Добавление трека в верх списка
        currentHistory.add(0, track)

        if (currentHistory.size > MAX_HISTORY_SIZE) {
            currentHistory.removeAt(MAX_HISTORY_SIZE)
        }

        // Ограничение размера истории
        saveSearchHistory(currentHistory)
    }

    // Метод для очистки истории
    fun clearSearchHistory() {
        saveSearchHistory(emptyList())
    }

    // Внутренний метод для сохранения истории в SharedPreferences
    private fun saveSearchHistory(history: List<Track>) {
        val editor = sharedPreferences.edit()
        val json = Gson().toJson(history)
        editor.putString(HISTORY_KEY, json)
        editor.apply()
    }
}
