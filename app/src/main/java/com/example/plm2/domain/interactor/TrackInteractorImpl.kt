package com.example.plm2.domain.interactor

import com.example.plm2.data.repository.TrackRepositoryImpl
import com.example.plm2.domain.model.Track

class TrackInteractorImpl(private val trackRepository: TrackRepositoryImpl) : TrackInteractor {

    override suspend fun searchTracks(query: String): List<Track> {
        return trackRepository.searchTracks(query)
    }

    override suspend fun loadTracks(): List<Track> {
        return trackRepository.loadTracks()
    }

    override fun loadSearchHistory(): List<Track> {
        return trackRepository.loadSearchHistory()
    }

    override fun addTrackToHistory(track: Track) {
        trackRepository.addTrackToHistory(track)
    }

    override fun clearSearchHistory() {
        trackRepository.clearSearchHistory()
    }

    // Реализация методов управления воспроизведением
    override suspend fun playTrack(track: Track) {
        // Логика воспроизведения трека
    }

    override suspend fun pauseTrack() {
        // Логика паузы воспроизведения
    }

    override suspend fun resumeTrack() {
        // Логика возобновления воспроизведения
    }

    override suspend fun stopTrack() {
        // Логика остановки воспроизведения
    }
}
