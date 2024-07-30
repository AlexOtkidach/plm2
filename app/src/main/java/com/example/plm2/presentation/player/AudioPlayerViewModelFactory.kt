package com.example.plm2.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.plm2.domain.interactor.AudioPlayerInteractorImpl

class AudioPlayerViewModelFactory(private val trackInteractor: AudioPlayerInteractorImpl) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AudioPlayerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AudioPlayerViewModel(trackInteractor) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}