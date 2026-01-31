package com.vadim.playlistmaker.data.network

import com.vadim.playlistmaker.data.dto.TrackApiResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TrackApiService {
    @GET("search?entity=song")
    fun getTracks(@Query("term") term: String): Call<TrackApiResponse>
}