package com.vadim.playlistmaker.domain.useCase

import com.vadim.playlistmaker.domain.model.Track
import com.vadim.playlistmaker.domain.repository.TrackHistoryRepository

class TrackHistoryUseCase(
    private val repository: TrackHistoryRepository
) {
    fun getHistory(callback: (List<Track>) -> Unit) {
        repository.getSearchHistory(callback)
    }

    fun addTrackToHistory(track: Track, callback: (Boolean) -> Unit) {
        repository.addToHistory(track, callback)
    }

    fun clearHistory(callback: (Boolean) -> Unit) {
        repository.clearHistory(callback)
    }

}