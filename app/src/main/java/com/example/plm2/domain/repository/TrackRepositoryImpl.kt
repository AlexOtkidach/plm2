
package com.example.plm2.data.repository

import android.util.Log
import com.example.plm2.domain.model.Track
import com.example.plm2.domain.repository.TrackRepository
import com.example.plm2.data.network.ApiService

class TrackRepositoryImpl(private val apiService: ApiService) : TrackRepository {
    override suspend fun searchTracks(query: String): List<Track> {
        return try {
            val response = apiService.getTracks(query)
            response.results.map { it.toDomainModel() }
        } catch (e: Exception) {
            Log.e("TrackRepository", "Error searching tracks", e)
            emptyList()
        }
    }
}