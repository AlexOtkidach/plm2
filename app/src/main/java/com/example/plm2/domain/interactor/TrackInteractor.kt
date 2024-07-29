package com.example.plm2.domain.interactor

import com.example.plm2.domain.model.Track

interface TrackInteractor {
    suspend fun searchTracks(query: String): List<Track>
    suspend fun loadTracks(): List<Track>
    fun loadSearchHistory(): List<Track>
    fun addTrackToHistory(track: Track)
    fun clearSearchHistory()
}