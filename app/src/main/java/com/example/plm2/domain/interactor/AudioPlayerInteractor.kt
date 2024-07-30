package com.example.plm2.domain.interactor

import com.example.plm2.domain.model.Track

interface AudioPlayerInteractor {
    suspend fun playTrack(track: Track)
    suspend fun pauseTrack()
    suspend fun resumeTrack()
    suspend fun stopTrack()
}