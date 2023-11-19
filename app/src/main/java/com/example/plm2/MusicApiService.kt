package com.example.plm2
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MusicApiService {
    @GET("/search?entity=song")
    fun search(@Query("term") text: String): Call<SearchResults>
}