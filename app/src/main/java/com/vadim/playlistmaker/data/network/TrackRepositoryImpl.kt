package com.vadim.playlistmaker.data.network

import com.vadim.playlistmaker.data.dto.TrackApiResponse
import com.vadim.playlistmaker.data.extension.trackDtoToTrack
import com.vadim.playlistmaker.domain.model.Track
import com.vadim.playlistmaker.domain.repository.TrackSearchRepository
import retrofit2.Call

class TrackRepositoryImpl(private val trackApiService: TrackApiService) : TrackSearchRepository {

    private var currentCall: Call<TrackApiResponse>? = null

    override fun searchTracks(query: String): Result<List<Track>> {
        return try {
            currentCall?.cancel()
            val call = trackApiService.getTracks(query)
            currentCall = call
            
            val response = call.execute()
            if (response.isSuccessful) {
                val tracks = response.body()?.tracksList?.map { it.trackDtoToTrack() } ?: emptyList()
                Result.success(tracks)
            } else {
                Result.failure(Exception("API error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun cancelSearch() {
        currentCall?.cancel()
        currentCall = null
    }
}