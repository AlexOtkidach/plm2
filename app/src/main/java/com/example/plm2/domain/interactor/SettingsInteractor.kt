package com.example.plm2.domain.interactor

import com.example.plm2.data.local.SharedPreferencesManager

class SettingsInteractor(private val preferencesManager: SharedPreferencesManager) {
    fun isDarkTheme(): Boolean {
        return preferencesManager.isDarkTheme()
    }

    fun setDarkTheme(enabled: Boolean) {
        preferencesManager.setDarkTheme(enabled)
    }
}

