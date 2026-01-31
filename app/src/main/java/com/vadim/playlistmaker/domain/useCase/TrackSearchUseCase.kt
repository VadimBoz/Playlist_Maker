package com.vadim.playlistmaker.domain.useCase

import com.vadim.playlistmaker.domain.repository.TrackSearchRepository
import com.vadim.playlistmaker.domain.state.TrackSearchResult

class TrackSearchUseCase(private val trackRepository: TrackSearchRepository) {
    fun search(query: String): TrackSearchResult {
        if (query.length < 3) {
            return TrackSearchResult.EmptyQuery
        }

        val result = trackRepository.searchTracks(query)
        return when {
            result.isSuccess && result.getOrNull()?.isNotEmpty() == true ->
                TrackSearchResult.Success(result.getOrNull()!!)
            result.isSuccess ->
                TrackSearchResult.Empty
            else ->
                TrackSearchResult.Error(result.exceptionOrNull()?.message ?: "Unknown error")
        }
    }

    fun cancel() {
        trackRepository.cancelSearch()
    }
}