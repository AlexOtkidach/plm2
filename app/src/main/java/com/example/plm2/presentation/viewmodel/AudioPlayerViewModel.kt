package com.example.plm2.presentation.viewmodel


import androidx.lifecycle.LiveData
import com.example.plm2.domain.model.Track

interface AudioPlayerViewModel {
    val tracksLiveData: LiveData<List<Track>>
    fun loadTracks()
}
