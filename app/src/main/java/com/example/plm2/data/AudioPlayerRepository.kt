package com.example.plm2.data

import com.example.plm2.domain.Track

interface AudioPlayerRepository {
    suspend fun loadTracks(): List<Track>
    fun checkInternetConnection(): Boolean
}
