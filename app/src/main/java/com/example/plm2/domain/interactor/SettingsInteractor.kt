package com.example.plm2.domain.interactor

import com.example.plm2.data.repository.PreferencesRepository

class SettingsInteractor(private val preferencesManager: PreferencesRepository) {
    fun isDarkTheme(): Boolean {
        return preferencesManager.isDarkTheme()
    }

    fun setDarkTheme(enabled: Boolean) {
        preferencesManager.setDarkTheme(enabled)
    }
}

