package com.example.plm2.data

import android.content.Context
import com.example.plm2.domain.model.Track
import com.example.plm2.domain.AudioPlayerRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TracksRepositoryImpl(private val context: Context) : AudioPlayerRepository {
    private val retrofitService: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    companion object {
        const val BASE_URL = "https://itunes.apple.com"
    }

    override suspend fun loadTracks(): List<Track> {
        return try {
            val response = retrofitService.getTracks()
            response.map { it.toTrack() }
        } catch (e: Exception) {
            emptyList()
        }
    }
}