package com.vadim.playlistmaker.domain.useCase

import com.vadim.playlistmaker.domain.model.Track

interface TrackHistoryUseCase {
    fun getHistory(callback: (List<Track>) -> Unit)
    fun addTrackToHistory(track: Track, callback: (Boolean) -> Unit)
    fun clearHistory(callback: (Boolean) -> Unit)
}