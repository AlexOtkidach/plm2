package domain

import Track
import data.AudioPlayerRepository

interface AudioPlayerInteractor {
    fun playTrack(track: Track): Boolean
    fun pauseTrack(): Boolean
    fun isPlaying(): Boolean
}