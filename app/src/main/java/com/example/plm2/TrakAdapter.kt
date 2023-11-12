import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.plm2.R
import com.example.plm2.Track

class TrackAdapter(private val trackList: List<Track>) : RecyclerView.Adapter<TrackViewHolder>() {

    // Создание нового ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_track, parent, false)
        return TrackViewHolder(itemView)
    }

    // Связывание данных с ViewHolder
    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(trackList[position])
    }

    // Возвращает общее количество элементов в списке
    override fun getItemCount(): Int {
        return trackList.size
    }
}