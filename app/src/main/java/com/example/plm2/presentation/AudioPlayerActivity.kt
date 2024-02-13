package com.example.plm2.presentation

import android.content.res.Configuration
import android.graphics.PorterDuff
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.plm2.R
import com.example.plm2.data.AudioPlayerRepository
import com.example.plm2.data.AudioPlayerRepositoryImpl
import com.example.plm2.domain.AudioPlayerUseCase
import java.util.concurrent.TimeUnit
import com.squareup.picasso.Picasso
import com.example.plm2.domain.Track


class AudioPlayerActivity : AppCompatActivity() {
    private lateinit var playPauseButton: ImageButton
    private lateinit var playbackProgressTextView: TextView
    private lateinit var audioPlayerManager: AudioPlayerManager
    private lateinit var audioPlayerUseCase: AudioPlayerUseCase
    private lateinit var audioPlayerRepository: AudioPlayerRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)


        val mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
        }
        audioPlayerManager = AudioPlayerManager(mediaPlayer)

        // Инициализация AudioPlayerRepository (если у вас уже есть реализация)
        audioPlayerRepository = AudioPlayerRepositoryImpl(this) // Замените на вашу реализацию

        // Инициализация AudioPlayerUseCase с использованием AudioPlayerRepository
        audioPlayerUseCase = AudioPlayerUseCase(audioPlayerRepository)

        // Инициализация и настройка AudioPlayerManager и UI
        audioPlayerManager.onProgressUpdate = { currentPosition ->
            updatePlaybackTime(currentPosition)
        }
        initializeUI()

        // Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Показываем кнопку Назад на Toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Проверяем, что нажата именно кнопка "Назад"
        if (item.itemId == android.R.id.home) {
            audioPlayerManager.pausePlaybackIfNeeded()
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    private fun updatePlaybackTime(timeInMillis: Int) {
        // Добавляем 1000 миллисекунд к текущему времени воспроизведения (+1 секунда)
        val adjustedTimeInMillis = timeInMillis + 1000
        val minutes = TimeUnit.MILLISECONDS.toMinutes(adjustedTimeInMillis.toLong())
        val seconds = TimeUnit.MILLISECONDS.toSeconds(adjustedTimeInMillis.toLong()) % 60
        playbackProgressTextView.text = String.format("%02d:%02d", minutes, seconds)
    }
    override fun onResume() {
        super.onResume()
        audioPlayerManager.resumePlaybackIfNeeded()
    }
    override fun onPause() {
        super.onPause()
        audioPlayerManager.stopUpdateTimeTask()
    }
    private fun initializeUI() {
        playPauseButton = findViewById(R.id.play_button)
        playbackProgressTextView = findViewById(R.id.time_playback)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        val track: Track? = intent.getParcelableExtra("track")
        track?.let { displayTrackInfo(it) }

        playPauseButton.setOnClickListener {
            togglePlayback()
        }
    }
    private fun displayTrackInfo(track: Track) {
        findViewById<TextView>(R.id.track_name).text = track.trackName
        findViewById<TextView>(R.id.artist_name).text = track.artistName
        findViewById<TextView>(R.id.album_name).text = track.collectionName ?: getString(R.string.album)
        findViewById<TextView>(R.id.genre).text = track.primaryGenreName ?: getString(R.string.genre)
        findViewById<TextView>(R.id.country).text = track.country ?: getString(R.string.country)
        findViewById<TextView>(R.id.release_year).text = track.releaseDate?.substring(0, 4) ?: getString(R.string.refresh_button)
        val playbackProgressTextView = findViewById<TextView>(R.id.playback_progress)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(track.trackTimeMillis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(track.trackTimeMillis) - TimeUnit.MINUTES.toSeconds(minutes)
        playbackProgressTextView.text = String.format("%02d:%02d", minutes, seconds)
        adjustTheme()
        track.previewUrl?.let { url ->
            audioPlayerManager.prepare(url)
        }
        track.artworkUrl512?.let { artworkUrl ->
            Picasso.get()
                .load(artworkUrl)
                .placeholder(R.drawable.placeholder_image)
                .into(findViewById<ImageView>(R.id.album_cover))
        } ?: run {
            findViewById<ImageView>(R.id.album_cover).setImageResource(R.drawable.placeholder_image)
        }
    }
    private fun togglePlayback() {
        if (audioPlayerManager.isPlaying()) {
            audioPlayerManager.pause()
            playPauseButton.setImageResource(R.drawable.ic_play_circle_outline_black_24dp)
        } else {
            audioPlayerManager.play()
            playPauseButton.setImageResource(R.drawable.ic_pause_black_24dp)
        }
    }
    private fun adjustTheme() {
        val nightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val playlistButtonColor = if (nightMode == Configuration.UI_MODE_NIGHT_YES) {
            R.color.playlist_favorite_icon_color_dark
        } else {
            R.color.play_button_color
        }
        findViewById<ImageButton>(R.id.add_to_playlist_button).setColorFilter(ContextCompat.getColor(this, playlistButtonColor), PorterDuff.Mode.SRC_IN)
        findViewById<ImageButton>(R.id.add_to_favorites_button).setColorFilter(ContextCompat.getColor(this, playlistButtonColor), PorterDuff.Mode.SRC_IN)
    }
}