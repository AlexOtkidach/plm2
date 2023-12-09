package com.example.plm2

data class Song(
    val trackId: String,
    val trackName: String,
    val artistName: String,
    val trackTime: String,
    val trackTimeMillis: Long,
    val artworkUrl100: String
)