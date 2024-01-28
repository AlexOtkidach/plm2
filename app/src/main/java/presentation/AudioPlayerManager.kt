package presentation

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper

class AudioPlayerManager(private val mediaPlayer: MediaPlayer) {
    private val handler = Handler(Looper.getMainLooper())

    fun play() {
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
        }
    }

    fun pause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        }
    }

    fun isPlaying(): Boolean {
        return mediaPlayer.isPlaying
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer.currentPosition
    }

    fun seekTo(position: Int) {
        mediaPlayer.seekTo(position)
    }
}