package com.vadim.playlistmaker.data.extension

import com.vadim.playlistmaker.data.dto.TrackDto
import com.vadim.playlistmaker.domain.model.Track


fun TrackDto.trackDtoToTrack(): Track {
    return Track(
        trackId = this.trackId,
        trackName = this.trackName,
        artistName = this.artistName,
        trackDuration = this.trackDuration,
        artworkUrl100 = this.artworkUrl100,
        album = this.album,
        year = this.year,
        genre = this.genre,
        country = this.country,
        previewUrl = this.previewUrl
    )
}


fun Track.trackToTrackDto(): TrackDto {
    return TrackDto(
        trackId = this.trackId,
        trackName = this.trackName,
        artistName = this.artistName,
        trackDuration = this.trackDuration,
        artworkUrl100 = this.artworkUrl100,
        album = this.album,
        year = this.year,
        genre = this.genre,
        country = this.country,
        previewUrl = this.previewUrl
    )
}