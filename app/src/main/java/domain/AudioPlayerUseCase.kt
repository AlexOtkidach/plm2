package domain

import domain.Track
import data.AudioPlayerRepository

class AudioPlayerUseCase(private val repository: AudioPlayerRepository) : AudioPlayerInteractor {
    private var isPlaying: Boolean = false

    override fun playTrack(track: Track): Boolean {
        // Ваша реализация воспроизведения трека здесь, например, с использованием MediaPlayer
        // Верните true, если трек успешно начат воспроизводиться, иначе false
        // Обновите флаг isPlaying
        isPlaying = true
        return true
    }

    override fun pauseTrack(): Boolean {
        // Ваша реализация паузы воспроизведения здесь
        // Верните true, если трек успешно поставлен на паузу, иначе false
        // Обновите флаг isPlaying
        isPlaying = false
        return true
    }

    override fun isPlaying(): Boolean {
        // Верните текущее состояние воспроизведения (играет ли трек)
        return isPlaying
    }
}
