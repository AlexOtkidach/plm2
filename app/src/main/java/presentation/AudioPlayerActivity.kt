package presentation

import Track
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.plm2.R
import java.util.concurrent.TimeUnit
import com.squareup.picasso.Picasso
import data.AudioPlayerRepositoryImpl
import domain.AudioPlayerInteractor
import domain.AudioPlayerUseCase


private lateinit var playPauseButton: ImageButton
private lateinit var playbackProgressTextView: TextView
private lateinit var audioPlayerUseCase: AudioPlayerUseCase // Инициализация audioPlayerUseCase
private lateinit var viewModel: AudioPlayerViewModel // Создание viewModel



class AudioPlayerActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var audioPlayerManager: AudioPlayerManager
    private lateinit var audioPlayerInteractor: AudioPlayerInteractor


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        // Инициализация mediaPlayer перед его использованием
        mediaPlayer = MediaPlayer()
        mediaPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        audioPlayerManager = AudioPlayerManager(mediaPlayer)

        mediaPlayer = MediaPlayer().apply {
            // Инициализация аудиоплеера и установка DataSource
        }
        // Инициализация audioPlayerUseCase
        audioPlayerInteractor = AudioPlayerUseCase(AudioPlayerRepositoryImpl(this))

        // Создание viewModel, передавая ей audioPlayerUseCase
        viewModel = AudioPlayerViewModel(audioPlayerUseCase)

        val track: Track? = intent.getParcelableExtra("track")

        if (track != null) {
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            mediaPlayer.setDataSource(track.previewUrl)
            mediaPlayer.prepareAsync()

            mediaPlayer.setOnCompletionListener {
                // Код, выполняемый по окончании трека
                playPauseButton.setImageResource(R.drawable.ic_play_circle_outline_black_24dp) // Установка иконки "Играть"
                // Остановка обновления UI воспроизведения
                handler.removeCallbacks(updateTimeRunnable)
                // Сброс счетчика времени воспроизведения
                playbackProgressTextView.text = "00:00"
            }

            playPauseButton = findViewById(R.id.play_button)
            playbackProgressTextView = findViewById(R.id.time_playback)

            playPauseButton.setOnClickListener {
                if (audioPlayerInteractor.isPlaying()) {
                    audioPlayerInteractor.pauseTrack()
                    playPauseButton.setImageResource(R.drawable.ic_play_circle_outline_black_24dp)
                } else {
                    // Вам также нужно передать выбранный трек сюда
                    val track: Track? = intent.getParcelableExtra("track")
                    if (track != null) {
                        audioPlayerInteractor.playTrack(track)
                        playPauseButton.setImageResource(R.drawable.ic_pause_black_24dp)
                        handler.post(updateTimeRunnable)
                    }
                }
            }

            val toolbar = findViewById<Toolbar>(R.id.toolbar)
            setSupportActionBar(toolbar)

            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                setDisplayShowHomeEnabled(true)
            }

            // Использование свойств объекта Track
            val trackNameTextView = findViewById<TextView>(R.id.track_name)
            trackNameTextView.text = track.trackName
            val artistNameTextView = findViewById<TextView>(R.id.artist_name)
            artistNameTextView.text = track.artistName
            val albumNameTextView = findViewById<TextView>(R.id.album_name)
            albumNameTextView.text = track.collectionName ?: "Название альбома отсутствует"
            val genreTextView = findViewById<TextView>(R.id.genre)
            genreTextView.text = track.primaryGenreName ?: "Жанр отсутствует"
            val countryTextView = findViewById<TextView>(R.id.country)
            countryTextView.text = track.country ?: "Страна отсутствует"
            val playbackProgressTextView = findViewById<TextView>(R.id.playback_progress)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(track.trackTimeMillis)
            val seconds =
                TimeUnit.MILLISECONDS.toSeconds(track.trackTimeMillis) - TimeUnit.MINUTES.toSeconds(
                    minutes
                )
            playbackProgressTextView.text = String.format("%02d:%02d", minutes, seconds)
            val releaseYearTextView = findViewById<TextView>(R.id.release_year)
            track.releaseDate?.substring(0, 4)?.let { year ->
                releaseYearTextView.text = year
            } ?: run {
                releaseYearTextView.visibility = View.GONE
            }

            val addToPlaylistButton = findViewById<ImageButton>(R.id.add_to_playlist_button)
            val addToFavoritesButton = findViewById<ImageButton>(R.id.add_to_favorites_button)

            val nightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            if (nightMode == Configuration.UI_MODE_NIGHT_YES) {

                // Текущая тема - темная
                addToPlaylistButton.setColorFilter(
                    ContextCompat.getColor(
                        this,
                        R.color.playlist_favorite_icon_color_dark
                    ), PorterDuff.Mode.SRC_IN
                )
                addToFavoritesButton.setColorFilter(
                    ContextCompat.getColor(
                        this,
                        R.color.playlist_favorite_icon_color_dark
                    ), PorterDuff.Mode.SRC_IN
                )
            }

            // Загрузка обложки трека с использованием Picasso
            track.artworkUrl512?.let { artworkUrl ->
                val imageView = findViewById<ImageView>(R.id.album_cover)
                Picasso.get()
                    .load(artworkUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .into(imageView)
            } ?: run {

                // Если artworkUrl512 пуст, установливает изображение плейсхолдера
                val imageView = findViewById<ImageView>(R.id.album_cover)
                imageView.setImageResource(R.drawable.placeholder_image)
            }
        }
    }

    private val handler = Handler(Looper.getMainLooper())
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            if (audioPlayerInteractor.isPlaying()) {
                val currentTime = mediaPlayer.currentPosition
                updatePlaybackTime(currentTime)
                handler.postDelayed(this, 1000)
            }
        }
    }

    private fun updatePlaybackTime(timeInMillis: Int) {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis.toLong())
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillis.toLong()) % 60
        playbackProgressTextView.text = String.format("%02d:%02d", minutes, seconds)
    }

    override fun onResume() {
        super.onResume()
        // Проверка, было ли аудио воспроизведено до перехода приложения в фон
        // Если да, возобновляем обновление UI.
        if (mediaPlayer.isPlaying) {
            playPauseButton.setImageResource(R.drawable.ic_pause_black_24dp) // Иконка "Пауза"
            handler.post(updateTimeRunnable)
        }
    }

    override fun onPause() {
        super.onPause()
        // При переходе в фоновый режим приостанавливаем воспроизведение и обновление UI
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            playPauseButton.setImageResource(R.drawable.ic_play_circle_outline_black_24dp) // Иконка "Играть"
        }
        handler.removeCallbacks(updateTimeRunnable)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}