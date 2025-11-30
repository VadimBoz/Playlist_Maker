package com.vadim.playlistmaker
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Array
import androidx.core.content.edit

private const val PREFS_NAME = "track_history_preferences"
private const val KEY_TRACK_HISTORY = "track_history"


class SearchHistoryManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val maxHistoryListSize = 10
    var trackListHistory: MutableList<Track> = mutableListOf()
    

    fun addTrackToHistory(track: Track) {
        if (trackListHistory.none { it.trackId == track.trackId } ) {
            trackListHistory.add(0,track)
        } else {
            trackListHistory.remove(track)
            trackListHistory.add(0,track)
        }

        if (trackListHistory.size > maxHistoryListSize) {
            trackListHistory.removeAt(trackListHistory.size - 1)
        }
    }


    fun loadTrackHistoryFromPref() {
        val trackHistoryJson: String? = sharedPreferences.getString(KEY_TRACK_HISTORY, null)
        if (trackHistoryJson != null) {
            trackListHistory = gson.fromJson(trackHistoryJson, object : TypeToken<List<Track>>() {}.type)
            
        } else {
            trackListHistory = mutableListOf()
        }
    }

    fun saveTrackHistory() {
        val historyJson = gson.toJson(trackListHistory)
        sharedPreferences
            .edit {
                putString(KEY_TRACK_HISTORY, historyJson)
            }
    }


    fun clearTrackHistory() {
        trackListHistory = mutableListOf()
    }


}
