package com.example.plm2

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView

class SettingsActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Стрелка назад переход с настроек на главную
        val arrToMain = findViewById<ImageView>(R.id.arrBack)
        arrToMain.setOnClickListener {
            val arrToMainIntent = Intent(this, MainActivity::class.java)
            startActivity(arrToMainIntent)
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
            shareIntent.data = Uri.parse("otkidach:")
            shareIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("otkidach.lesha@yandex.ru"))
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