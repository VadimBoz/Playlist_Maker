package com.vadim.playlistmaker.data.dto

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.vadim.playlistmaker.data.extension.cleanText
import com.vadim.playlistmaker.data.extension.epochTimeToFormatedTxt
import com.vadim.playlistmaker.data.extension.parseToYear
import java.lang.reflect.Type

class TrackDeserializerAdapter: JsonDeserializer<TrackDto> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): TrackDto {
        val jsonObject = json.asJsonObject

        return TrackDto(
            trackId = jsonObject.getSafe("trackId")?.asString?.cleanText()?.toLongOrNull() ?: 0L,
            trackName = jsonObject.getSafe("trackName")?.asString?.cleanText() ?: "",
            artistName = jsonObject.getSafe("artistName")?.asString?.cleanText() ?: "",
            trackDuration = jsonObject.getSafe("trackTimeMillis")?.asString?.cleanText() ?: "0",
            artworkUrl100 = jsonObject.getSafe("artworkUrl100")?.asString?.cleanText() ?: "",
            album = jsonObject.getSafe("collectionName")?.asString?.cleanText() ?: "",
            year = jsonObject.getSafe("releaseDate")?.asString?.cleanText()?.parseToYear() ?: 0,
            genre = jsonObject.getSafe("primaryGenreName")?.asString?.cleanText() ?: "",
            country = jsonObject.getSafe("country")?.asString?.cleanText() ?: "",
            previewUrl = jsonObject.getSafe("previewUrl")?.asString?.cleanText() ?: ""
        )
    }

    private fun JsonObject.getSafe(key: String): JsonElement? {
        return if (has(key) && !get(key).isJsonNull) get(key) else null
    }
}