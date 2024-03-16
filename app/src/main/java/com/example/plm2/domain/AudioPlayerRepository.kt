package com.example.plm2.domain

interface AudioPlayerRepository {
    suspend fun loadTracks(): List<Track>
}