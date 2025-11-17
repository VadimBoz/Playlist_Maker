package com.vadim.playlistmaker

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TrackApiService {
    @GET("search?entity=song")
    fun getTracks(@Query("term") term: String): Call<TrackApiResponse>
}