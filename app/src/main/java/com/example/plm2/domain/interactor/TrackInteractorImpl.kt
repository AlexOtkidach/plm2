package com.example.plm2.domain.interactor

import com.example.plm2.domain.model.Track
import com.example.plm2.domain.repository.TrackRepository

class TrackInteractorImpl(private val repository: TrackRepository) : TrackInteractor {
    override suspend fun searchTracks(query: String): List<Track> {
        return repository.searchTracks(query)
    }
}
