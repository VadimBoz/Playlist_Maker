package com.vadim.playlistmaker.data.sharedPrefRepository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vadim.playlistmaker.data.dto.TrackDto
import com.vadim.playlistmaker.data.extension.trackDtoToTrack
import com.vadim.playlistmaker.data.extension.trackToTrackDto
import com.vadim.playlistmaker.data.sharedPrefRepository.HistoryRepositoryConstants.KEY_TRACK_HISTORY
import com.vadim.playlistmaker.data.sharedPrefRepository.HistoryRepositoryConstants.MAX_HISTORY_SIZE
import com.vadim.playlistmaker.data.sharedPrefRepository.HistoryRepositoryConstants.PREFS_NAME
import com.vadim.playlistmaker.domain.model.Track
import com.vadim.playlistmaker.domain.repository.TrackHistoryRepository

private object HistoryRepositoryConstants {
    const val PREFS_NAME = "search_preferences"
    const val KEY_TRACK_HISTORY = "track_history"
    const val MAX_HISTORY_SIZE = 10
}


class HistoryRepositoryImpl(private val context: Context) : TrackHistoryRepository {


    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    private val gson = Gson()

    override fun getSearchHistory(callback: (List<Track>) -> Unit) {

        try {
            val historyJson = preferences.getString(KEY_TRACK_HISTORY, null)
            Log.d("History", "Loaded history JSON: $historyJson")
            val historyDto: List<TrackDto> = if (historyJson != null) {
                gson.fromJson(historyJson, object : TypeToken<List<TrackDto>>() {}.type)
            } else {
                emptyList()
            }
            val history = historyDto.map { it.trackDtoToTrack() }
            callback(history)
        } catch (e: Exception) {
            callback(emptyList())
        }
    }

    override fun addToHistory(track: Track, callback: (Boolean) -> Unit) {
        val trackDto = track.trackToTrackDto()

        try {
            val currentHistory = loadHistory().toMutableList()
            currentHistory.removeAll { it.trackId == trackDto.trackId }
            currentHistory.add(0, trackDto)

            if (currentHistory.size > MAX_HISTORY_SIZE) {
                currentHistory.removeAt(currentHistory.size - 1)
            }

            saveHistory(currentHistory)
            callback(true)
        } catch (e: Exception) {
            callback(false)
        }
    }

    override fun clearHistory(callback: (Boolean) -> Unit) {
        try {
            preferences.edit {
                remove(KEY_TRACK_HISTORY)
            }
            callback(true)
        } catch (e: Exception) {
            callback(false)
        }
    }

    private fun saveHistory(history: List<TrackDto>) {
        val historyJson = gson.toJson(history)
        preferences.edit {
            putString(KEY_TRACK_HISTORY, historyJson)
        }
    }

    private fun loadHistory(): List<TrackDto> {
        val historyJson = preferences.getString(KEY_TRACK_HISTORY, null)
        return if (historyJson != null) {
            gson.fromJson(historyJson, object : TypeToken<List<TrackDto>>() {}.type)
        } else {
            emptyList()
        }
    }

}