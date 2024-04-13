package com.example.plm2.presentation


import androidx.lifecycle.LiveData
import com.example.plm2.domain.Track
import com.example.plm2.presentation.AudioPlayerViewModel

interface AudioPlayerViewModel {
    val tracksLiveData: LiveData<List<Track>>
    fun loadTracks()
}
