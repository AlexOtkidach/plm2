package com.example.plm2.presentation.player

import android.content.Context
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.plm2.R
import com.example.plm2.data.local.SearchHistory
import com.example.plm2.data.local.SharedPreferencesManager
import com.example.plm2.data.network.ApiService
import com.example.plm2.data.repository.TrackRepositoryImpl
import com.example.plm2.domain.interactor.TrackInteractor
import com.example.plm2.domain.interactor.TrackInteractorImpl
import com.example.plm2.domain.model.Track
import com.squareup.picasso.Picasso
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AudioPlayerActivity : AppCompatActivity(), AudioPlayerView {
    private lateinit var playPauseButton: ImageButton
    private lateinit var playbackProgressTextView: TextView
    private lateinit var audioPlayerManager: AudioPlayerManager
    private lateinit var trackInteractor: TrackInteractor

    private val TRACK_KEY = "track"

    private val audioPlayerViewModel: AudioPlayerViewModel by lazy {
        ViewModelProvider(this, AudioPlayerViewModelFactory(trackInteractor))[AudioPlayerViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        // Инициализация Retrofit, SearchHistory и TrackRepository
        val apiService = Retrofit.Builder()
            .baseUrl(TrackRepositoryImpl.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

        val sharedPreferencesManager = SharedPreferencesManager(this)
        val searchHistory = SearchHistory(sharedPreferencesManager.sharedPreferences)
        val trackRepository = TrackRepositoryImpl(apiService, searchHistory)
        trackInteractor = TrackInteractorImpl(trackRepository)

        val mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
        }

        audioPlayerManager = AudioPlayerManager(mediaPlayer, this)

        audioPlayerManager.setPlaybackProgressListener { timeInMillis ->
            runOnUiThread {
                updatePlaybackTime(timeInMillis)
            }
        }

        initializeUI()

        playbackProgressTextView = findViewById(R.id.time_playback)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        if (!isInternetConnected()) {
            Toast.makeText(this, "Нет соединения с Интернетом", Toast.LENGTH_SHORT).show()
        }

        val track: Track? = intent.getParcelableExtra("track")
        track?.let {
            displayTrackInfo(it)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            audioPlayerManager.pausePlaybackIfNeeded()
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updatePlaybackTime(timeInMillis: Int) {
        val adjustedTimeInMillis = timeInMillis + 1000
        val minutes = TimeUnit.MILLISECONDS.toMinutes(adjustedTimeInMillis.toLong())
        val seconds = TimeUnit.MILLISECONDS.toSeconds(adjustedTimeInMillis.toLong()) % 60
        playbackProgressTextView.text = String.format("%02d:%02d", minutes, seconds)
        updatePlaybackProgress(timeInMillis)
    }

    override fun updatePlaybackProgress(timeInMillis: Int) {
        val formattedTime = formatTime(timeInMillis)
        playbackProgressTextView.text = formattedTime
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun updateTrackInfo(track: Track) {
        // Реализуйте логику обновления информации о треке
    }

    override fun onResume() {
        super.onResume()
        audioPlayerManager.resumePlaybackIfNeeded()
    }

    private fun isInternetConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
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
        val track: Track? = intent.getParcelableExtra(TRACK_KEY)
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
        if (track.artworkUrl512 != null) {
            Picasso.get()
                .load(track.artworkUrl512)
                .placeholder(R.drawable.placeholder_image)
                .into(findViewById<ImageView>(R.id.album_cover))
        } else {
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

    private fun formatTime(timeInMillis: Int): String {
        val minutes = (timeInMillis / 1000) / 60
        val seconds = (timeInMillis / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun loadTracks() {
        // Реализуйте логику загрузки треков, если необходимо
    }
}

