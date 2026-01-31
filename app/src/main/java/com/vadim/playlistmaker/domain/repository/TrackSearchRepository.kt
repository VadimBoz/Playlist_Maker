package com.vadim.playlistmaker.domain.repository

import com.vadim.playlistmaker.domain.model.Track

interface TrackSearchRepository {
    fun searchTracks(query: String): Result<List<Track>>
    fun cancelSearch()
}