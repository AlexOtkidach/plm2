package com.example.plm2

import android.content.Intent
import android.net.Uri
import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar

class SettingsActivity : AppCompatActivity() {
    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        sharedPrefs = getSharedPreferences("settings", Context.MODE_PRIVATE)

        val switchTheme2 = findViewById<SwitchCompat>(R.id.switchTheme)

        // Установка цвета свитча
        val switchCompat = findViewById<SwitchCompat>(R.id.switchTheme)
        switchCompat.thumbTintList = ColorStateList.valueOf(resources.getColor(R.color.SwitchColorRe))
        switchCompat.trackTintList = ColorStateList.valueOf(resources.getColor(R.color.SwitchColorRe))

        // Цвет подложки (track)
        switchCompat.trackTintList = ColorStateList.valueOf(resources.getColor(R.color.switchTrackColor))

        // Цвет рычажка (thumb)
        switchCompat.thumbTintList = ColorStateList.valueOf(resources.getColor(R.color.switchThumbColor))



        // Получаем текущую цветовую схему приложения
        val isDarkTheme2 = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        // Устанавливаем состояние переключателя
        switchTheme2.isChecked = isDarkTheme2

         // Установка слушателя для переключателя
        switchTheme2.setOnCheckedChangeListener { _, isChecked ->
            // Обработка изменения состояния переключателя
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
        // Состояние Switch на основе сохраненных настроек
        val switchTheme = findViewById<SwitchCompat>(R.id.switchTheme)
        val isDarkTheme = sharedPrefs.getBoolean("isDarkTheme", false)
        switchTheme.isChecked = isDarkTheme

        // Обработчик для Switch
        switchTheme.setOnCheckedChangeListener { _, isChecked ->

            // Сохраняем состояние в настройках
            sharedPrefs.edit().putBoolean("isDarkTheme", isChecked).apply()

            // Установка выбранной темы
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // Стрелка назад с настроек на главную
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Устанавливаем слушатель для кнопки назад (стрелки)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressed() // Это обработчик для кнопки "назад"
        }

        // Кнопка поделиться приложением
        val shareButton = findViewById<FrameLayout>(R.id.shareButton)
        shareButton.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                val shareButtonText = resources.getString(R.string.shareButtonText)
                putExtra(Intent.EXTRA_TEXT, shareButtonText)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

        // Кнопка Написать в техподдержку
        val supportButton = findViewById<FrameLayout>(R.id.btnSupport)
        supportButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SENDTO)
            val support_theme = resources.getString(R.string.themeMail)
            val support_text = resources.getString(R.string.textMail)
            shareIntent.data = Uri.parse("mailto:otkidach.lesha@yandex.ru")
            shareIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("mailto:alexotkidach@gmail.com"))
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, support_theme)
            shareIntent.putExtra(Intent.EXTRA_TEXT, support_text)
            startActivity(shareIntent)
        }

        // Кнопка Пользовательское соглашение
        val termsButton = findViewById<FrameLayout>(R.id.btnTerms)
        termsButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_VIEW)
            val termsOfUseArticle = resources.getString(R.string.termsArticle)
            shareIntent.data = Uri.parse(termsOfUseArticle)
            startActivity(shareIntent)
        }
    }
}
