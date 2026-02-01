package com.vadim.playlistmaker.domain.repository

import com.vadim.playlistmaker.domain.model.ImageData
import com.vadim.playlistmaker.domain.model.Track

interface ImageLoader {
    fun loadTrackImage(track: Track, callback: (ImageData) -> Unit)
    fun loadTrackCoverImage(track: Track, callback: (ImageData) -> Unit)
}
