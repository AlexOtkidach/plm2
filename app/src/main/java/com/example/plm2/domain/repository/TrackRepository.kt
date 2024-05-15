package com.example.plm2.domain.repository

import com.example.plm2.domain.Track

interface TrackRepository {
    suspend fun loadTracks(query: String): List<Track>
}