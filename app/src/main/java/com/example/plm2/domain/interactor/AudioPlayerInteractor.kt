package com.example.plm2.domain.interactor

import com.example.plm2.domain.Track

interface AudioPlayerInteractor {
    fun playTrack(track: Track): Boolean
    fun pauseTrack(): Boolean
    fun isPlaying(): Boolean
}