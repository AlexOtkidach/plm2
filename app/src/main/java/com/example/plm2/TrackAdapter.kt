package com.example.plm2

import android.content.res.Resources
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import java.text.SimpleDateFormat
import java.util.Locale

class TrackAdapter(private var trackList: List<Track>) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    var onTrackClickListener: ((Track) -> Unit)? = null
    private var selectedTrackId: Long? = null

    companion object {
        // Функция для конвертации dp в пиксели
        private fun dpToPixels(dp: Int): Int {
            val density = Resources.getSystem().displayMetrics.density
            return (dp * density + 0.5f).toInt()
        }
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = trackList[position]
        holder.bind(track, track.trackId == selectedTrackId)
        holder.itemView.setOnClickListener {
            onTrackClickListener?.invoke(track)
            setSelectedTrackId(track.trackId)
        }
    }


    fun setSelectedTrackId(trackId: Long) {
        selectedTrackId = trackId
        notifyDataSetChanged()  // Обновить список для отображения выбранного элемента
    }

    class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val trackNameTextView: TextView = itemView.findViewById(R.id.trackNameTextView)
        private val artistAndTimeTextView: TextView = itemView.findViewById(R.id.artistAndTimeTextView)
        private val artworkImageView: ImageView = itemView.findViewById(R.id.artworkImageView)

        fun bind(track: Track, isSelected: Boolean) {
            val formattedTime = SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis)
            trackNameTextView.text = track.trackName
            artistAndTimeTextView.text = itemView.context.getString(
                R.string.artist_and_time,
                track.artistName,
                formattedTime
            )

            Glide.with(itemView)
                .load(track.artworkUrl100)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .transform(RoundedCorners(dpToPixels(8)))
                .into(artworkImageView)

            // Изменение внешнего вида элемента при выборе
            itemView.setBackgroundColor(if (isSelected) Color.LTGRAY else Color.TRANSPARENT)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(view)
    }

    override fun getItemCount(): Int = trackList.size

    fun setTracks(tracks: List<Track>?) {
        trackList = tracks ?: emptyList()
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        trackList = if (query.isBlank()) {
            trackList
        } else {
            trackList.filter { track ->
                track.trackName.contains(query, ignoreCase = true) ||
                        track.artistName.contains(query, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }
}