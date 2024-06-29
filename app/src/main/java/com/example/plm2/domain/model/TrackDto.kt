package com.example.plm2.data.model

import com.example.plm2.domain.model.Track

data class TrackDto(
    val trackId: Long,
    val trackName: String,
    val artistName: String,
    val artworkUrl100: String,
    val collectionName: String?,
    val releaseDate: String?,
    val primaryGenreName: String?,
    val country: String?,
    val previewUrl: String?,
    val trackTimeMillis: Long
) {
    fun toDomainModel(): Track {
        return Track(
            itemId = trackId,
            trackName = trackName,
            artistName = artistName,
            trackTimeMillis = trackTimeMillis,
            artworkUrl100 = artworkUrl100,
            collectionName = collectionName,
            releaseDate = releaseDate,
            primaryGenreName = primaryGenreName,
            country = country,
            previewUrl = previewUrl,
            currentPlaybackTime = 0
        )
    }
}