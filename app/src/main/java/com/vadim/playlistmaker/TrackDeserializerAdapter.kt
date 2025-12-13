package com.vadim.playlistmaker

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

class TrackDeserializerAdapter: JsonDeserializer<Track> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Track {
        val jsonObject = json.asJsonObject

        return Track(
            trackId = jsonObject.getSafe("trackId")?.asString?.cleanText()?.toLongOrNull() ?: 0L,
            trackName = jsonObject.getSafe("trackName")?.asString?.cleanText() ?: "",
            artistName = jsonObject.getSafe("artistName")?.asString?.cleanText() ?: "",
            trackDuration = jsonObject.getSafe("trackTimeMillis")?.asString?.cleanText()?.epochTimeToTxt() ?: "",
            artworkUrl100 = jsonObject.getSafe("artworkUrl100")?.asString?.cleanText() ?: "",
            album = jsonObject.getSafe("collectionName")?.asString?.cleanText() ?: "",
            year = jsonObject.getSafe("releaseDate")?.asString?.cleanText()?.parseToYear() ?: 0,
            genre = jsonObject.getSafe("primaryGenreName")?.asString?.cleanText() ?: "",
            country = jsonObject.getSafe("country")?.asString?.cleanText() ?: ""
        )
    }

    private fun JsonObject.getSafe(key: String): JsonElement? {
        return if (has(key) && !get(key).isJsonNull) get(key) else null
    }
}