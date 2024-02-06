package com.example.plm2.domain

interface GetTracksUseCase {
    suspend fun execute(): List<Track>
}