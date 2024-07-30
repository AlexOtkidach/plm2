package com.example.plm2.presentation.settings

import androidx.lifecycle.ViewModel
import com.example.plm2.domain.interactor.SettingsInteractor

class SettingsViewModel(private val settingsInteractor: SettingsInteractor) : ViewModel() {

    fun isDarkTheme(): Boolean {
        return settingsInteractor.isDarkTheme()
    }

    fun setDarkTheme(enabled: Boolean) {
        settingsInteractor.setDarkTheme(enabled)
    }
}
