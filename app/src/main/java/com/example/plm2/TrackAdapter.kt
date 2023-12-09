import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.plm2.R
import com.example.plm2.Track

class TrackAdapter(private var trackList: List<Track>) :
    RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    private var query: String = ""
    private var trackListFiltered: List<Track> = trackList

    companion object {
        // Функция для конвертации dp в пиксели
        private fun dpToPixels(dp: Int): Int {
            val density = Resources.getSystem().displayMetrics.density
            return (dp * density + 0.5f).toInt()
        }
    }

    class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val trackNameTextView: TextView = itemView.findViewById(R.id.trackNameTextView)
        private val artistAndTimeTextView: TextView =
            itemView.findViewById(R.id.artistAndTimeTextView)
        private val artworkImageView: ImageView = itemView.findViewById(R.id.artworkImageView)

        fun bind(track: Track, query: String) {
            itemView.visibility = View.VISIBLE
            trackNameTextView.text = track.trackName
            artistAndTimeTextView.text = itemView.context.getString(
                R.string.artist_and_time,
                track.artistName,
                track.trackTime
            )

            // Используем Glide для загрузки изображения
            Glide.with(itemView)
                .load(track.artworkUrl100)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .transform(RoundedCorners(dpToPixels(8)))  // Используем dpToPixels
                .into(artworkImageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(trackListFiltered[position], query)
    }

    override fun getItemCount(): Int {
        return trackListFiltered.size
    }

    fun filter(query: String) {
        trackListFiltered = if (query.isBlank()) {
            trackList // Если запрос пустой, покажем все треки
        } else {
            trackList.filter { track ->
                track.trackName.contains(query, ignoreCase = true) || track.artistName.contains(
                    query,
                    ignoreCase = true
                )
            }
        }
        notifyDataSetChanged()
    }

    // Метод для обновления данных в адаптере
    fun setTracks(tracks: List<Track>?) {
        trackList = tracks?.map { song ->
            Track(
                trackName = song.trackName,
                artistName = song.artistName,
                trackTime = song.trackTime,
                artworkUrl100 = song.artworkUrl100,
                trackId = song.trackId

            )
        } ?: emptyList()

        filter(query)
    }
}
