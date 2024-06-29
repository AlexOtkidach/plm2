package com.example.plm2.data.repository

import com.example.plm2.data.model.TrackResponse
import com.example.plm2.data.network.ApiService
import com.example.plm2.domain.model.Track
import com.example.plm2.domain.repository.TrackRepository

class TrackRepositoryImpl(private val apiService: ApiService) : TrackRepository {
    override suspend fun searchTracks(query: String): List<Track> {
        val response = apiService.getTracks(query)
        return response.results.map { it.toDomainModel() }
    }
}