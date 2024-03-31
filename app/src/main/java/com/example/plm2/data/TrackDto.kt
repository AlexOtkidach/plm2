package com.example.plm2.data

import com.example.plm2.domain.Track
import com.google.gson.annotations.SerializedName

data class TrackDto(
    val ItemId: Long,
    val compositionName: String,
    val artistName: String,
    val coverImageURL: String
)

fun TrackDto.toTrack(): Track {
    return Track(
        itemId = ItemId,
        trackName = compositionName,
        artistName = artistName,
        trackTimeMillis = 0,
        artworkUrl100 = coverImageURL,
        collectionName = null,
        releaseDate = null,
        primaryGenreName = null,
        country = null,
        previewUrl = null
    )
}
