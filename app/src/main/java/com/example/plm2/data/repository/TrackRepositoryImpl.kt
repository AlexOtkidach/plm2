package com.example.plm2.data.repository

import com.example.plm2.data.local.SearchHistory
import com.example.plm2.data.network.ApiService
import com.example.plm2.domain.model.Track
import com.example.plm2.domain.repository.TrackRepository

class TrackRepositoryImpl(private val apiService: ApiService, private val searchHistory: SearchHistory) : TrackRepository {

    companion object {
        const val BASE_URL = "https://itunes.apple.com"
    }

    override suspend fun searchTracks(query: String): List<Track> {
        return try {
            val response = apiService.searchTracks(query)
            response.results.map { it.toDomainModel() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun loadTracks(): List<Track> {
        return emptyList()
    }

    override fun loadSearchHistory(): List<Track> {
        return searchHistory.getSearchHistory()
    }

    override fun addTrackToHistory(track: Track) {
        searchHistory.addTrackToHistory(track)
    }

    override fun clearSearchHistory() {
        searchHistory.clearSearchHistory()
    }
}
