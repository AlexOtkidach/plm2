package com.example.plm2.presentation.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.plm2.data.local.SharedPreferencesManager
import com.example.plm2.data.repository.PreferencesRepository

open class BaseActivity : AppCompatActivity() {

    private lateinit var preferencesRepository: PreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferencesManager = SharedPreferencesManager(this)
        preferencesRepository = PreferencesRepository(sharedPreferencesManager)

        // Загрузка темы из SharedPreferences
        val isDarkTheme = preferencesRepository.isDarkTheme()
        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        preferencesRepository.setDarkTheme(enabled)
        if (enabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}
