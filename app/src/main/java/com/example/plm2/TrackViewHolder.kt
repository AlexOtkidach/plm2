// TrackViewHolder.kt

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.plm2.R
import com.example.plm2.Track

class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val coverImageURL: ImageView = itemView.findViewById(R.id.coverImageURL)
    private val compositionName: TextView = itemView.findViewById(R.id.compositionName)
    private val artistName: TextView = itemView.findViewById(R.id.artistName)
    private val ellipse_1: View = itemView.findViewById(R.id.ellipse_1)
    private val duration: TextView = itemView.findViewById(R.id.duration)
    private val vector: View = itemView.findViewById(R.id.vector)

    // Метод для привязки данных трека к элементам макета
    fun bind(track: Track) {
        // Здесь вы устанавливаете данные трека в соответствующие элементы макета
        compositionName.text = track.trackName
        artistName.text = track.artistName
        duration.text = track.trackTime

        // Используем библиотеку Glide для загрузки изображения
        Glide.with(itemView)
            .load(track.artworkUrl100)
            .placeholder(R.drawable.placeholder) // плейсхолдер, если изображение не загружено
            .fitCenter() // или .centerCrop(), в зависимости от предпочтений
            .into(coverImageURL)
    }
}
