package data

import domain.Track
import android.content.Context
import android.net.ConnectivityManager
import domain.GetTracksUseCase
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AudioPlayerRepositoryImpl(private val context: Context) : AudioPlayerRepository {

    private val apiService: ApiService // Инициализируйте ваш ApiService

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
                response
            } else {
                // Обработка ошибки загрузки (например, пустой ответ)
                emptyList()
            }
        } catch (e: Exception) {
            // Обработка ошибки, например, отсутствия интернет-соединения или других исключений
            emptyList()
        }
    }

    override fun checkInternetConnection(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
    class TracksRepositoryImpl: GetTracksUseCase {
        override suspend fun execute(): List<Track> {
            // Здесь реализация загрузки треков, например, из SharedPreferences
            return listOf() // Возвращаем пустой список для примера
        }
    }
}