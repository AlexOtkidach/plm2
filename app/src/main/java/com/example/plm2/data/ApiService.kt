package com.example.plm2.data

import com.example.plm2.domain.Track
import retrofit2.http.GET

interface ApiService {
    @GET("https://itunes.apple.com")
    suspend fun getTracks(): List<Track>
}