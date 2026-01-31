package com.vadim.playlistmaker.data.network

import com.google.gson.GsonBuilder
import com.vadim.playlistmaker.data.network.Response
import com.vadim.playlistmaker.data.dto.TrackDto
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.vadim.playlistmaker.data.dto.TrackDeserializerAdapter

object RetrofitNetworkClient: NetworkClient {

    private const val BASE_URL = "https://itunes.apple.com/"

    fun createApiService(): TrackApiService {
        val gson = GsonBuilder()
            .registerTypeAdapter(TrackDto::class.java, TrackDeserializerAdapter())
            .create()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder()
                        .registerTypeAdapter(TrackDto::class.java, TrackDeserializerAdapter())
                        .create()
                )
            )
            .build()
            .create(TrackApiService::class.java)
    }


}