package com.example.plm2.presentation.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.plm2.domain.interactor.AudioPlayerInteractorImpl
import com.example.plm2.domain.model.Track
import kotlinx.coroutines.launch

class AudioPlayerViewModel(private val trackInteractor: AudioPlayerInteractorImpl) : ViewModel() {

    private val _track = MutableLiveData<Track>()
    val track: LiveData<Track> get() = _track

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    fun playTrack(track: Track) {
        _track.value = track
        viewModelScope.launch {
            trackInteractor.playTrack(track)
            _isPlaying.value = true
        }
    }

    fun pauseTrack() {
        viewModelScope.launch {
            trackInteractor.pauseTrack()
            _isPlaying.value = false
        }
    }

    fun resumeTrack() {
        viewModelScope.launch {
            trackInteractor.resumeTrack()
            _isPlaying.value = true
        }
    }

    fun stopTrack() {
        viewModelScope.launch {
            trackInteractor.stopTrack()
            _isPlaying.value = false
        }
    }

    fun loadTracks() {

    }
}
