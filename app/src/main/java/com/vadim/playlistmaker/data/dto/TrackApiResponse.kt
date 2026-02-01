package com.vadim.playlistmaker.data.dto

import com.google.gson.annotations.SerializedName
import com.vadim.playlistmaker.data.network.Response

data class TrackApiResponse (
    @SerializedName("resultCount")
    val trackCount: Int,
    @SerializedName("results")
    val tracksList: List<TrackDto>
): Response()