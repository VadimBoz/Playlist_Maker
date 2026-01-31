package com.vadim.playlistmaker.data.sharedPrefRepository

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vadim.playlistmaker.domain.repository.TrackHistoryRepository
import com.vadim.playlistmaker.domain.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HistoryRepositoryImpl(private val context: Context) : TrackHistoryRepository {

    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(SharedPrefsConstants.PREFS_NAME, Context.MODE_PRIVATE)
    }
    private val THREAD_NAME = "HistoryRepositoryThread"
    private val mainHandler = Handler(Looper.getMainLooper())
    private val gson = Gson()
    private val backgroundHandler: Handler
    private var isDisposed = false

    init {
        val handlerThread = HandlerThread(THREAD_NAME).apply {
            start()
        }
        backgroundHandler = Handler(handlerThread.looper)
    }

    override fun getSearchHistory(callback: (List<Track>) -> Unit) {
        if (isDisposed) {
            callback(emptyList())
            return
        }

        backgroundHandler.post {
            try {
                val historyJson = preferences.getString(SharedPrefsConstants.KEY_TRACK_HISTORY, null)
                val history: List<Track> = if (historyJson != null) {
                    gson.fromJson(historyJson, object : TypeToken<List<Track>>() {}.type)
                } else {
                    emptyList()
                }

                mainHandler.post {
                    if (!isDisposed) {
                        callback(history)
                    }
                }
            } catch (e: Exception) {
                mainHandler.post {
                    if (!isDisposed) {
                        callback(emptyList())
                    }
                }
            }
        }
    }

    override fun addToHistory(track: Track, callback: (Boolean) -> Unit) {
        if (isDisposed) {
            callback(false)
            return
        }

        backgroundHandler.post {
            try {
                val currentHistory = loadHistory().toMutableList()

                currentHistory.removeAll { it.trackId == track.trackId }

                currentHistory.add(0, track)

                if (currentHistory.size > SharedPrefsConstants.MAX_HISTORY_SIZE) {
                    currentHistory.removeAt(currentHistory.size - 1)
                }

                saveHistory(currentHistory)

                mainHandler.post {
                    if (!isDisposed) {
                        callback(true)
                    } else {
                        callback(false)
                    }
                }
            } catch (e: Exception) {
                mainHandler.post {
                    if (!isDisposed) {
                        callback(false)
                    }
                }
            }
        }
    }

    override fun clearHistory(callback: (Boolean) -> Unit) {
        if (isDisposed) {
            callback(false)
            return
        }

        backgroundHandler.post {
            try {
                preferences.edit {
                    remove(SharedPrefsConstants.KEY_TRACK_HISTORY)
                }

                mainHandler.post {
                    if (!isDisposed) {
                        callback(true)
                    } else {
                        callback(false)
                    }
                }
            } catch (e: Exception) {
                mainHandler.post {
                    if (!isDisposed) {
                        callback(false)
                    }
                }
            }
        }
    }

    private fun saveHistory(history: List<Track>) {
        val historyJson = gson.toJson(history)
        preferences.edit {
            putString(SharedPrefsConstants.KEY_TRACK_HISTORY, historyJson)
        }
    }

    private fun loadHistory(): List<Track> {
        val historyJson = preferences.getString(SharedPrefsConstants.KEY_TRACK_HISTORY, null)
        return if (historyJson != null) {
            gson.fromJson(historyJson, object : TypeToken<List<Track>>() {}.type)
        } else {
            emptyList()
        }
    }

}