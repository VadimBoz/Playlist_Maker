package com.vadim.playlistmaker.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Track(
    val trackId: Long,
    val trackName: String,
    val artistName: String,
    val trackDuration: String, //mm:ss
    val artworkUrl100: String,
    val album: String,
    val year: Int,
    val genre: String,
    val country: String,
    val previewUrl: String
): Parcelable