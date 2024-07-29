package com.example.plm2.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.plm2.domain.interactor.TrackInteractor

class AudioPlayerViewModelFactory(private val trackInteractor: TrackInteractor) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AudioPlayerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AudioPlayerViewModel(trackInteractor) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}