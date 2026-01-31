package com.vadim.playlistmaker.domain.state

import com.vadim.playlistmaker.domain.model.Track

sealed class TrackSearchResult {
    object EmptyQuery : TrackSearchResult()
    object Empty : TrackSearchResult()
    data class Success(val tracks: List<Track>) : TrackSearchResult()
    data class Error(val message: String) : TrackSearchResult()
}