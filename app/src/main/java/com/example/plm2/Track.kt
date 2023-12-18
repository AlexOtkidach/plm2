package com.example.plm2
import com.google.gson.annotations.SerializedName

data class Track(
    @SerializedName("ItemId") val itemId: Long,
    @SerializedName("compositionName") val trackName: String,
    @SerializedName("artistName") val artistName: String,
    @SerializedName("durationInMillis") val trackTimeMillis: Long,
    @SerializedName("coverImageURL") val artworkUrl100: String,
    @SerializedName("albumName") val collectionName: String?, // может быть null
    @SerializedName("releaseDate") val releaseDate: String?, // может быть null
    @SerializedName("genre") val primaryGenreName: String?, // может быть null
    @SerializedName("country") val country: String? // может быть null
) {

    val trackId: Long
        get() = itemId
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Track

        if (itemId != other.itemId) return false
        if (trackName != other.trackName) return false
        if (artistName != other.artistName) return false
        if (trackTimeMillis != other.trackTimeMillis) return false
        if (artworkUrl100 != other.artworkUrl100) return false
        if (collectionName != other.collectionName) return false
        if (releaseDate != other.releaseDate) return false
        if (primaryGenreName != other.primaryGenreName) return false
        if (country != other.country) return false

        return true
    }

    override fun hashCode(): Int {
        var result = itemId.hashCode()
        result = 31 * result + trackName.hashCode()
        result = 31 * result + artistName.hashCode()
        result = 31 * result + trackTimeMillis.hashCode()
        result = 31 * result + artworkUrl100.hashCode()
        result = 31 * result + (collectionName?.hashCode() ?: 0)
        result = 31 * result + (releaseDate?.hashCode() ?: 0)
        result = 31 * result + (primaryGenreName?.hashCode() ?: 0)
        result = 31 * result + (country?.hashCode() ?: 0)
        return result
    }
}

var tracks: ArrayList<Track> = arrayListOf()