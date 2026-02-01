package com.vadim.playlistmaker.data.dto


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class TrackDto(
        val trackId: Long,
        val trackName: String? = "",
        val artistName: String? = "",
        @SerializedName("trackTimeMillis") val trackDuration: String? = "00:00",
        val artworkUrl100: String? = "",
        @SerializedName("collectionName") val album: String? = "",
        @SerializedName("releaseDate") val year: Int = 0,
        @SerializedName("primaryGenreName") val genre: String? = "",
        val country: String? = "",
        val previewUrl: String? = ""
        ): Parcelable