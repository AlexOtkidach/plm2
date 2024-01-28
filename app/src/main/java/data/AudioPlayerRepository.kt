package data

import Track

interface AudioPlayerRepository {
    suspend fun loadTracks(): List<Track>
    fun checkInternetConnection(): Boolean
}
