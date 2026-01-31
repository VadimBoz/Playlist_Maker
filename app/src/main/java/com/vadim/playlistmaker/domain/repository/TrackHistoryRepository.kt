package com.vadim.playlistmaker.domain.repository

import com.vadim.playlistmaker.domain.model.Track

interface TrackHistoryRepository {
    fun getSearchHistory(callback: (List<Track>) -> Unit)
    fun addToHistory(track: Track, callback: (Boolean) -> Unit)
    fun clearHistory(callback: (Boolean) -> Unit)
}