import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.plm2.R

class TrackAdapter(private val trackList: List<Track>) :
    RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    private var query: String = ""
    private var trackListFiltered: List<Track> = trackList

    class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val trackNameTextView: TextView = itemView.findViewById(R.id.trackNameTextView)
        private val artistAndTimeTextView: TextView = itemView.findViewById(R.id.artistAndTimeTextView)
        private val artworkImageView: ImageView = itemView.findViewById(R.id.artworkImageView)

        fun bind(track: Track, query: String) {
            if (track.trackName.contains(query, ignoreCase = true) || track.artistName.contains(query, ignoreCase = true)) {
                itemView.visibility = View.VISIBLE
                trackNameTextView.text = track.trackName
                artistAndTimeTextView.text = "${track.artistName} • ${track.trackTime}"

                // Используем Glide для загрузки изображения
                Glide.with(itemView)
                    .load(track.artworkUrl100)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .transform(RoundedCorners(8))
                    .into(artworkImageView)
            } else {
                // Если трек не соответствует запросу, скрываем элемент списка
                itemView.visibility = View.GONE
                itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
            }
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
        return trackList.size
    }

    fun filter(query: String) {
        // логгирование для вывода фильтруемого запроса
        Log.d("TrackAdapter", "filter: $query")
        this.query = query

        notifyDataSetChanged()
    }
}
