package com.example.plm2.domain

interface AudioPlayerInteractor {
    fun playTrack(track: Track): Boolean
    fun pauseTrack(): Boolean
    fun isPlaying(): Boolean
}