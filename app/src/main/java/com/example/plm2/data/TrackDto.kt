import com.example.plm2.domain.Track

data class TrackDto(
    val itemId: Long,
    val trackName: String,
    val artistName: String,
    val coverImageURL: String
)

fun TrackDto.toTrack(): Track {
    return Track(
        itemId = itemId,
        trackName = trackName,
        artistName = artistName,
        trackTimeMillis = 0,
        artworkUrl100 = coverImageURL,
        collectionName = null,
        releaseDate = null,
        primaryGenreName = null,
        country = null,
        previewUrl = null,
        currentPlaybackTime = 0
    )
}
