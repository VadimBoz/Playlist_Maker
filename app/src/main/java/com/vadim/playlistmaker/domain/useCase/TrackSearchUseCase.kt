package com.vadim.playlistmaker.domain.useCase

import com.vadim.playlistmaker.domain.state.TrackSearchResult

interface TrackSearchUseCase {
    fun search(query: String): TrackSearchResult
    fun cancelSearching()
}