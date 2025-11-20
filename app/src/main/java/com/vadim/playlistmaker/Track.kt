package com.vadim.playlistmaker

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Track(
    val trackName: String,
    val artistName: String,
    @SerializedName("trackTimeMillis") val trackTime: String,
    val artworkUrl100: String
): Parcelable