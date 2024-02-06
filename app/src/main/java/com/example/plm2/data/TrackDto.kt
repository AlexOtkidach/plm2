package com.example.plm2.data

import com.google.gson.annotations.SerializedName

data class TrackDto(
    @SerializedName("ItemId") val itemId: Long,
    @SerializedName("compositionName") val trackName: String,
    @SerializedName("artistName") val artistName: String,
    @SerializedName("coverImageURL") val artworkUrl100: String
)

