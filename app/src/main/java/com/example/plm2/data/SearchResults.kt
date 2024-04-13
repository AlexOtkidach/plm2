package com.example.plm2.data

import com.example.plm2.domain.Song

data class SearchResults(
    val resultCount: Int,
    val results: List<Song>
)