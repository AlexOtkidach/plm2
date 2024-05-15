package com.example.plm2.presentation

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import com.example.plm2.R
import com.example.plm2.presentation.base.BaseActivity
import com.example.plm2.presentation.viewmodel.MainViewModel


class MainActivity : BaseActivity() {

    // Объявляем переменную viewModel внутри класса MainActivity
    private lateinit var viewModel: MainViewModel

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализируем viewModel
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        // Эта кнопка чтобы производить поиск
        val searchButton = findViewById<Button>(R.id.search)
        searchButton.setOnClickListener {
            val mainGoToSearch = Intent(
                this,
                SearchActivity::class.java
            )
            startActivity(mainGoToSearch)
        }
        // Это кнопка для открытия медиатеки
        val mediaButton = findViewById<Button>(R.id.media)
        mediaButton.setOnClickListener {
            val mainGoToMedia = Intent(
                this,
                MediaActivity::class.java
            )
            startActivity(mainGoToMedia)
        }
        // Это кнопка для настроек
        val settingsButton = findViewById<Button>(R.id.settings)
        settingsButton.setOnClickListener {
            val mainGoToSettings = Intent(
                this,
                SettingsActivity::class.java
            )
            startActivity(mainGoToSettings)
        }
    }
}