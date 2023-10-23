package com.example.plm2
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
//import androidx.appcompat.app.AppCompatDelegate

class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Кнопка Поиск
        val searchButton = findViewById<Button>(R.id.search)
        searchButton.setOnClickListener {
            val mainGoToSearch = Intent(
                this,
                SearchActivity::class.java)
            startActivity(mainGoToSearch)
        }
        // Кнопка Медиатека
        val mediaButton = findViewById<Button>(R.id.media)
        mediaButton.setOnClickListener {
            val mainGoToMedia = Intent(
                this,
                MediaActivity::class.java)
            startActivity(mainGoToMedia)
        }
        /* Кнопка Настройки 1*/
        val settingsButton = findViewById<Button>(R.id.settings)
        settingsButton.setOnClickListener {
            val mainGoToSettings = Intent(
                this,
                SettingsActivity::class.java)
            startActivity(mainGoToSettings)
        }
        // Включение ночного режима AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        // Выключение ночного режима AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
}