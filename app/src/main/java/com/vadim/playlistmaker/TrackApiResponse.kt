package com.vadim.playlistmaker

import com.google.gson.annotations.SerializedName

data class TrackApiResponse (
    @SerializedName("resultCount") val trackCount: Int,
    @SerializedName("results") val tracksList: List<Track>
)