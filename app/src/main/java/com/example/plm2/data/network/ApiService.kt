package com.example.plm2.data.network

import com.example.plm2.data.model.TrackResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("search")
    suspend fun getTracks(@Query("term") query: String): TrackResponse
}
