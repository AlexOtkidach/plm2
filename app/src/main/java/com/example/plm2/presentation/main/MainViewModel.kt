package com.example.plm2.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _navigateToSearch = MutableLiveData<Boolean>()
    val navigateToSearch: LiveData<Boolean>
        get() = _navigateToSearch

    private val _navigateToMedia = MutableLiveData<Boolean>()
    val navigateToMedia: LiveData<Boolean>
        get() = _navigateToMedia

    private val _navigateToSettings = MutableLiveData<Boolean>()
    val navigateToSettings: LiveData<Boolean>
        get() = _navigateToSettings

    fun onSearchButtonClicked() {
        _navigateToSearch.value = true
    }

    fun onMediaButtonClicked() {
        _navigateToMedia.value = true
    }

    fun onSettingsButtonClicked() {
        _navigateToSettings.value = true
    }

    fun onNavigationHandled() {
        _navigateToSearch.value = false
        _navigateToMedia.value = false
        _navigateToSettings.value = false
    }
}