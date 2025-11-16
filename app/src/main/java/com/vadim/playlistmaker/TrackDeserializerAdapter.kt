package com.vadim.playlistmaker

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

class TrackDeserializerAdapter: TypeAdapter<Track>() {

    override fun write(out: JsonWriter?, value: Track?) {
        TODO("Not yet implemented")
    }

    override fun read(reader: JsonReader): Track {
        var trackName = ""
        var artistName = ""
        var trackTimeMillis: String? = null
        var artworkUrl100 = ""
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "trackName" -> trackName = reader.nextString().cleanText()
                "artistName" -> artistName = reader.nextString().cleanText()
                "trackTimeMillis" -> trackTimeMillis = reader.nextString().cleanText()
                "artworkUrl100" -> artworkUrl100 = reader.nextString().cleanText()
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        return Track(
            trackName = trackName,
            artistName = artistName,
            trackTime = trackTimeMillis.epochTimeToTxt(),
            artworkUrl100 = artworkUrl100
        )
    }

}