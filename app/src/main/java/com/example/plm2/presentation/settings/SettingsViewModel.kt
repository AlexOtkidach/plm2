package com.example.plm2.presentation.settings

import androidx.lifecycle.ViewModel
import com.example.plm2.data.repository.PreferencesRepository

class SettingsViewModel(private val settingsInteractor: PreferencesRepository) : ViewModel() {

    fun isDarkTheme(): Boolean {
        return settingsInteractor.isDarkTheme()
    }

    fun setDarkTheme(enabled: Boolean) {
        settingsInteractor.setDarkTheme(enabled)
    }
}
