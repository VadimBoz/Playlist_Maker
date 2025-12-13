package com.vadim.playlistmaker

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class TrackDeserializerAdapter: JsonDeserializer<Track> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Track {
        val jsonObject = json.asJsonObject

        return Track(
            trackId = jsonObject.get("trackId").asString.cleanText().toLong(),
            trackName = jsonObject.get("trackName").asString.cleanText(),
            artistName = jsonObject.get("artistName").asString.cleanText(),
            trackTime = jsonObject.get("trackTimeMillis").asString.cleanText().epochTimeToTxt(),
            artworkUrl100 = jsonObject.get("artworkUrl100").asString.cleanText()
        )
    }

}