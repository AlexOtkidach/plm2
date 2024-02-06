package com.example.plm2.domain

import com.example.plm2.domain.Track
import com.example.plm2.data.AudioPlayerRepository

class AudioPlayerUseCase(private val repository: AudioPlayerRepository) : AudioPlayerInteractor {
    private var isPlaying: Boolean = false

    override fun playTrack(track: Track): Boolean {
        isPlaying = true
        return true
    }

    override fun pauseTrack(): Boolean {
        isPlaying = false
        return true
    }

    override fun isPlaying(): Boolean {
        return isPlaying
    }
}
