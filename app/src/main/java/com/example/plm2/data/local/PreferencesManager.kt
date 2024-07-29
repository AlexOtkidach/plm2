package com.example.plm2.data.local

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun setDarkTheme(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("isDarkTheme", enabled).apply()
    }

    fun isDarkTheme(): Boolean {
        return sharedPreferences.getBoolean("isDarkTheme", false)
    }
}