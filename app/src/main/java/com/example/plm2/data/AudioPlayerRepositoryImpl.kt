package com.example.plm2.data

import com.example.plm2.domain.Track
import android.content.Context
import android.net.ConnectivityManager
import com.example.plm2.domain.GetTracksUseCase
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
class AudioPlayerRepositoryImpl(private val context: Context) : AudioPlayerRepository, GetTracksUseCase {

    private val apiService: ApiService
    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    override suspend fun loadTracks(): List<Track> {
        return try {
            val response = apiService.getTracks()
            if (response.isNotEmpty()) {
                // Маппим список TrackDto в список Track с помощью функции расширения
                response.map { it.toTrack() } // Вызываем функцию toTrack() здесь
            } else {
                // Если список пуст, вернуть пустой список
                emptyList()
            }
        } catch (e: Exception) {
            // Обработка ошибки, например, отсутствия интернет-соединения или других исключений
            // Вернуть пустой список в случае ошибки
            emptyList()
        }
    }

    override fun checkInternetConnection(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
    override suspend fun execute(): List<Track> {
        return listOf()
    }
}
