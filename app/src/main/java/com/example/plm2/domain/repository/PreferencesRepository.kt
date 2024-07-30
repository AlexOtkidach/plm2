package com.example.plm2.domain.repository

interface PreferencesRepository {
    fun addTrackToHistory(track: String)
    fun getSearchHistory(): List<String>
    fun clearSearchHistory()
    fun setDarkTheme(enabled: Boolean)
    fun isDarkTheme(): Boolean
}