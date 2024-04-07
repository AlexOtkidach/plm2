package com.example.plm2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.lifecycle.ViewModelProvider
import com.example.plm2.presentation.MainViewModel

class MainActivity : BaseActivity() {
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        val searchButton = findViewById<Button>(R.id.search)
        val mediaButton = findViewById<Button>(R.id.media)
        val settingsButton = findViewById<Button>(R.id.settings)

        searchButton.setOnClickListener {
            viewModel.onSearchButtonClicked()
        }

        mediaButton.setOnClickListener {
            viewModel.onMediaButtonClicked()
        }

        settingsButton.setOnClickListener {
            viewModel.onSettingsButtonClicked()
        }

        // Наблюдаем за LiveData
        viewModel.navigateToSearch.observe(this) {
            if (it) {
                navigateToSearchActivity()
                viewModel.onNavigationHandled()
            }
        }

        viewModel.navigateToMedia.observe(this) {
            if (it) {
                navigateToMediaActivity()
                viewModel.onNavigationHandled()
            }
        }

        viewModel.navigateToSettings.observe(this) {
            if (it) {
                navigateToSettingsActivity()
                viewModel.onNavigationHandled()
            }
        }
    }

    private fun navigateToSearchActivity() {
        val intent = Intent(this, SearchActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToMediaActivity() {
        val intent = Intent(this, MediaActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToSettingsActivity() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
}
