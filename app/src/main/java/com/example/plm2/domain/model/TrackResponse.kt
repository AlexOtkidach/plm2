package com.example.plm2.data.model

data class TrackResponse(
    val resultCount: Int,  // Общее количество найденных результатов
    val results: List<TrackDto>  // Список треков в формате DTO
)