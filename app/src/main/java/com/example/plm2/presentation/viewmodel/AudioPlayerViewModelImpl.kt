package com.example.plm2.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plm2.data.TracksRepositoryImpl
import com.example.plm2.domain.Track
import kotlinx.coroutines.launch
import com.example.plm2.presentation.viewmodel.AudioPlayerViewModel

class AudioPlayerViewModelImpl(private val repository: TracksRepositoryImpl) : ViewModel(),
    AudioPlayerViewModel {
    private val _tracksLiveData = MutableLiveData<List<Track>>()
    override val tracksLiveData: LiveData<List<Track>> = _tracksLiveData

    override fun loadTracks() {
        viewModelScope.launch {
            _tracksLiveData.value = repository.loadTracks()
        }
    }
}
