package com.example.plm2.presentation

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import java.io.IOException

class AudioPlayerManager(private val mediaPlayer: MediaPlayer, private val view: AudioPlayerView) {
    private val handler = Handler(Looper.getMainLooper())
    private var updateTimeTask: Runnable? = null
    var onProgressUpdate: ((Int) -> Unit)? = null
    private var isPrepared = false // Флаг готовности к воспроизведению
    private var wasPlayingBeforePause = false //флаг, который будет указывать, было ли воспроизведение
    // активно перед переходом пользователя на другой экран или нет

    // Обработчик прогресса воспроизведения
    private var playbackProgressListener: ((Int) -> Unit)? = null

    // Метод для установки обработчика прогресса воспроизведения
    fun setPlaybackProgressListener(listener: (Int) -> Unit) {
        playbackProgressListener = listener
    }
    fun startUpdateTimeTask() {
        updateTimeTask = object : Runnable {
            override fun run() {
                view.updatePlaybackProgress(mediaPlayer.currentPosition)
                handler.postDelayed(this, 1000)
            }
        }.also { handler.postDelayed(it, 1000) }
    }

    fun stopUpdateTimeTask() {
        updateTimeTask?.let { handler.removeCallbacks(it) }
    }

    fun prepare(url: String) {
        try {
            mediaPlayer.reset() // Сбросить MediaPlayer в исходное состояние
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            mediaPlayer.setDataSource(url)
            mediaPlayer.prepareAsync() // Асинхронная подготовка
            mediaPlayer.setOnPreparedListener {
                isPrepared = true // Трек готов к воспроизведению
                startUpdateTimeTask() // <--- Здесь начинаем отсчет времени
            }
            mediaPlayer.setOnCompletionListener {
                isPrepared = false // Сброс флага готовности после завершения трека
                stopUpdateTimeTask() // Останавливаем отсчет времени при завершении трека
            }
            mediaPlayer.setOnErrorListener { _, _, _ ->
                isPrepared = false // Сброс флага готовности в случае ошибки
                stopUpdateTimeTask() // Останавливаем отсчет времени при ошибке
                true
            }
        } catch (e: IOException) {
            e.printStackTrace() // Обработка исключения, связанного с установкой источника данных
        }
    }

    fun play() {
        if (isPrepared && !mediaPlayer.isPlaying) {
            mediaPlayer.start() // Начать воспроизведение только если трек готов
            startUpdateTimeTask() // Возобновляем отсчет времени
        }
    }

    fun pause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            stopUpdateTimeTask() // Останавливаем отсчет времени при паузе
        }
    }

    fun isPlaying(): Boolean {
        return mediaPlayer.isPlaying
    }
    fun pausePlaybackIfNeeded() {
        if (mediaPlayer.isPlaying) {
            pause()
            wasPlayingBeforePause = true
        } else {
            wasPlayingBeforePause = false
        }
    }

    fun resumePlaybackIfNeeded() {
        if (wasPlayingBeforePause && isPrepared) {
            play()
        }
    }
}