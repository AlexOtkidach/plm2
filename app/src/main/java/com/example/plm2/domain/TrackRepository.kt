package com.example.plm2.domain

interface TrackRepository {
    suspend fun loadTracks(query: String): List<Track>
}