package data

import domain.Track

interface AudioPlayerRepository {
    suspend fun loadTracks(): List<Track>
    fun checkInternetConnection(): Boolean
}
