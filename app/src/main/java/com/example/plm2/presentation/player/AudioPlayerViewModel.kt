package com.example.plm2.presentation.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.plm2.domain.interactor.TrackInteractor
import com.example.plm2.domain.model.Track
import kotlinx.coroutines.Dispatchers

class AudioPlayerViewModel(private val trackInteractor: TrackInteractor) : ViewModel() {

    fun loadTracks(): LiveData<List<Track>> = liveData(Dispatchers.IO) {
        val tracks = trackInteractor.loadTracks()
        emit(tracks)
    }
}
