package com.vadim.playlistmaker.domain.useCase

import com.vadim.playlistmaker.domain.model.Track
import com.vadim.playlistmaker.domain.repository.TrackHistoryRepository

class TrackHistoryUseCaseImpl(
    private val repository: TrackHistoryRepository
): TrackHistoryUseCase {
    override fun getHistory(callback: (List<Track>) -> Unit) {
        repository.getSearchHistory(callback)
    }

    override fun addTrackToHistory(track: Track, callback: (Boolean) -> Unit) {
        repository.addToHistory(track, callback)
    }

    override fun clearHistory(callback: (Boolean) -> Unit) {
        repository.clearHistory(callback)
    }

}