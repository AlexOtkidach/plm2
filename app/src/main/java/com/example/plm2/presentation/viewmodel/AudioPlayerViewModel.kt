package com.example.plm2.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import com.example.plm2.domain.repository.TrackRepository
import kotlinx.coroutines.Dispatchers

class AudioPlayerViewModel(private val trackRepository: TrackRepository) : ViewModel() {
    fun loadTracks() = liveData(Dispatchers.IO) {
        val tracks = trackRepository.loadTracks()
        emit(tracks)
    }
}

class AudioPlayerViewModelFactory(private val trackRepository: TrackRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AudioPlayerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AudioPlayerViewModel(trackRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}