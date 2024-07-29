package com.example.plm2.presentation

import com.example.plm2.domain.model.Track

interface AudioPlayerView {
    fun updatePlaybackProgress(timeInMillis: Int)
    fun showError(message: String)
    fun updateTrackInfo(track: Track)
}