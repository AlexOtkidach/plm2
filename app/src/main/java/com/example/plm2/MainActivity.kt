package com.example.plm2

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Кнопка Поиск
        val searchButton = findViewById<Button>(R.id.search)
        searchButton.setOnClickListener {
            val displayIntent = Intent(
                this,
                SearchActivity::class.java)
            startActivity(displayIntent)
        }
        // Кнопка Медиатека
        val mediaButton = findViewById<Button>(R.id.media)
        mediaButton.setOnClickListener {
            val displayIntent2 = Intent(
                this,
                MediaActivity::class.java)
            startActivity(displayIntent2)
        }
        /* Кнопка Настройки 1*/
        val settingsButton = findViewById<Button>(R.id.settings)
        settingsButton.setOnClickListener {
            val displayIntent3 = Intent(
                this,
                SettingsActivity::class.java)
            startActivity(displayIntent3)
        }
    }

}