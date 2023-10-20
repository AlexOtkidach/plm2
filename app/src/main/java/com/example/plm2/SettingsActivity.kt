package com.example.plm2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Стрелка назад переход с настроек на главную
        val arrToMain = findViewById<ImageView>(R.id.arrBack)
        arrToMain.setOnClickListener {
            val arrToMainIntent = Intent(this, MainActivity::class.java)
            startActivity(arrToMainIntent)
        }
    }
}