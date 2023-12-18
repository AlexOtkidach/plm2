import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.example.plm2.R
import java.util.concurrent.TimeUnit

class AudioPlayerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_player)

        // Получите данные о треке из Intent
        val trackName = intent.getStringExtra("compositionName")
        val artistName = intent.getStringExtra("artistName")
        val collectionName = intent.getStringExtra("albumName")
        val releaseDate = intent.getStringExtra("releaseDate")
        val primaryGenreName = intent.getStringExtra("genre")
        val country = intent.getStringExtra("country")
        val trackTimeMillis = intent.getLongExtra("durationInMillis", 0L)
        val coverImageURL = intent.getStringExtra("coverImageURL")

        // Найдите соответствующие TextView на макете активности
        val trackNameTextView = findViewById<TextView>(R.id.track_name)
        val artistNameTextView = findViewById<TextView>(R.id.artist_name)
        val albumNameTextView = findViewById<TextView>(R.id.album_name)
        val releaseYearTextView = findViewById<TextView>(R.id.release_year)
        val genreTextView = findViewById<TextView>(R.id.genre)
        val countryTextView = findViewById<TextView>(R.id.country)
        val playbackProgressTextView = findViewById<TextView>(R.id.playback_progress)

        // Заполните TextView данными о треке
        trackNameTextView.text = trackName
        artistNameTextView.text = artistName
        albumNameTextView.text = collectionName
        genreTextView.text = primaryGenreName
        countryTextView.text = country

        // Отобразите продолжительность трека в формате минут:секунды
        val minutes = TimeUnit.MILLISECONDS.toMinutes(trackTimeMillis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(trackTimeMillis) -
                TimeUnit.MINUTES.toSeconds(minutes)
        playbackProgressTextView.text = String.format("%02d:%02d", minutes, seconds)

        // Проверьте, что releaseDate не равен null и не пуст
        if (!releaseDate.isNullOrEmpty()) {
            // Извлеките только первые четыре символа (год) из строки releaseDate
            val year = releaseDate.substring(0, 4)

            // Установите отформатированный год в TextView
            releaseYearTextView.text = year
        } else {
            // Если releaseDate равен null или пуст, скройте TextView или установите текст по умолчанию
            releaseYearTextView.visibility = View.GONE // или установите текст по умолчанию
        }

        val albumCoverImageView = findViewById<ImageView>(R.id.album_cover)

        // Получите URL обложки альбома из объекта Track
        val imageUrl = coverImageURL

        if (!imageUrl.isNullOrEmpty()) {
            // Опции для загрузки изображения
            val requestOptions = RequestOptions()
                .placeholder(R.drawable.placeholder_album_cover) // Заглушка, если изображение не загружено
                .transform(RoundedCorners(8))
                .error(R.drawable.placeholder_album_cover) // Заглушка, если произошла ошибка загрузки

            // Загрузка изображения с использованием Glide
            Glide.with(this)
                .load(imageUrl)
                .apply(requestOptions)
                .transition(DrawableTransitionOptions.withCrossFade()) // Плавное переключение между изображениями
                .into(albumCoverImageView)
        } else {
            // Если URL обложки отсутствует, вы можете установить заглушку или другое действие по умолчанию
            albumCoverImageView.setImageResource(R.drawable.placeholder_album_cover)
        }
        }
    }
