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
        val searchBut = findViewById<Button>(R.id.search)
        searchBut.setOnClickListener {
            val mainGoToSearch = Intent(
                this,
                SearchActivity::class.java)
            startActivity(mainGoToSearch)
        }
        // Кнопка Медиатека
        val mediaBut = findViewById<Button>(R.id.media)
        mediaBut.setOnClickListener {
            val mainGoToMedia = Intent(
                this,
                MediaActivity::class.java)
            startActivity(mainGoToMedia)
        }
        // кнопка Настройки
        val settingsBut = findViewById<Button>(R.id.settings)
        settingsBut.setOnClickListener {
            val mainGoToSettings = Intent(
                this,
                SettingsActivity::class.java)
            startActivity(mainGoToSettings)
        }
        // Включение ночного режима AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        // Выключение ночного режима AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
}