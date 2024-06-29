package com.example.plm2.domain

import com.example.plm2.domain.model.Track

interface AudioPlayerRepository {
    suspend fun loadTracks(): List<Track>
}