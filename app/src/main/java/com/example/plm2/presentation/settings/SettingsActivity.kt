package com.example.plm2.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.example.plm2.R
import com.example.plm2.data.local.SharedPreferencesManager
import com.example.plm2.domain.interactor.SettingsInteractor

class SettingsActivity : AppCompatActivity() {
    private lateinit var settingsViewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val sharedPreferencesManager = SharedPreferencesManager(this)
        val settingsInteractor = SettingsInteractor(sharedPreferencesManager)

        settingsViewModel = ViewModelProvider(this, SettingsViewModelFactory(settingsInteractor))[SettingsViewModel::class.java]

        val switchTheme = findViewById<SwitchCompat>(R.id.switchTheme)

        // Инициализация состояния свитча из ViewModel
        val isDarkTheme = settingsViewModel.isDarkTheme()
        switchTheme.isChecked = isDarkTheme

        // Слушатель для переключения темы
        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.setDarkTheme(isChecked)
        }

        setupToolbar()
        setupShareButton()
        setupSupportButton()
        setupTermsButton()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupShareButton() {
        val shareButton = findViewById<FrameLayout>(R.id.shareButton)
        shareButton.setOnClickListener {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_TEXT, getString(R.string.shareButtonText))
                type = "text/plain"
            }
            startActivity(Intent.createChooser(sendIntent, null))
        }
    }

    private fun setupSupportButton() {
        val supportButton = findViewById<FrameLayout>(R.id.btnSupport)
        supportButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("support@example.com"))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.themeMail))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.textMail))
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }
    }

    private fun setupTermsButton() {
        val termsButton = findViewById<FrameLayout>(R.id.btnTerms)
        termsButton.setOnClickListener {
            val url = getString(R.string.termsArticle)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
    }
}
