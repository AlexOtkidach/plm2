package com.example.plm2.domain.interactor

import com.example.plm2.data.repository.TrackRepositoryImpl
import com.example.plm2.domain.model.Track

class AudioPlayerInteractorImpl(private val trackRepository: TrackRepositoryImpl) : AudioPlayerInteractor {

    override suspend fun playTrack(track: Track) {
    }

    override suspend fun pauseTrack() {
    }

    override suspend fun resumeTrack() {
    }

    override suspend fun stopTrack() {
    }
}
