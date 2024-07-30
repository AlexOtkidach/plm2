package com.example.plm2.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.plm2.data.repository.PreferencesRepository

class PreferencesViewModel(private val preferencesRepository: PreferencesRepository) : ViewModel() {

    fun setDarkTheme(enabled: Boolean) {
        preferencesRepository.setDarkTheme(enabled)
    }

    fun isDarkTheme(): Boolean {
        return preferencesRepository.isDarkTheme()
    }
}
