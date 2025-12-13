package com.vadim.playlistmaker

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Track(
    val trackId: Long,
    val trackName: String,
    val artistName: String,
    @SerializedName("trackTimeMillis") val trackDuration: String,
    val artworkUrl100: String,
    @SerializedName("collectionName") val album: String,
    @SerializedName("releaseDate") val year: Int,
    @SerializedName("primaryGenreName") val genre: String,
    @SerializedName("country") val country: String,
): Parcelable