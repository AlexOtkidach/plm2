package com.example.plm2.data.repository

import com.example.plm2.data.local.SharedPreferencesManager
import com.example.plm2.domain.model.Track

class PreferencesRepository(private val sharedPreferencesManager: SharedPreferencesManager) {

    fun addTrackToHistory(track: Track) {
        sharedPreferencesManager.addTrackToHistory(track)
    }

    fun getSearchHistory(): List<Track> {
        return sharedPreferencesManager.getSearchHistory()
    }

    fun clearSearchHistory() {
        sharedPreferencesManager.clearSearchHistory()
    }

    fun setDarkTheme(enabled: Boolean) {
        sharedPreferencesManager.setDarkTheme(enabled)
    }

    fun isDarkTheme(): Boolean {
        return sharedPreferencesManager.isDarkTheme()
    }
}
