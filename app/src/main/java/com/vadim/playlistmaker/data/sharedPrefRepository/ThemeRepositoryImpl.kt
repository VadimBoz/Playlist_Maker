package com.vadim.playlistmaker.data.sharedPrefRepository

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import androidx.core.content.edit
import com.vadim.playlistmaker.domain.repository.ThemeRepository



class ThemeRepositoryImpl(private val context: Context) : ThemeRepository {

    private val THREAD_NAME = "ThemeRepositoryThread"

    private val mainHandler = Handler(Looper.getMainLooper())
    val handlerThread = HandlerThread(THREAD_NAME).apply {
        start()
    }
    private val backgroundHandler: Handler = Handler(handlerThread.looper)
    private var isDisposed = false

    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(SharedPrefsConstants.SETTING_PREFS, Context.MODE_PRIVATE)
    }

    override fun getCurrentTheme(callback: (Boolean) -> Unit) {
        if (isDisposed) {
            return
        }
        backgroundHandler.post {
            if (isDisposed) return@post
            try {
                val theme = preferences.getBoolean(SharedPrefsConstants.THEME_KEY, false)
                mainHandler.post {
                    if (!isDisposed) {
                        callback(theme)
                    }
                }
            } catch (e: Exception) {
                return@post
            }
        }
    }

    override fun saveTheme(darkThemeEnabled: Boolean, callback: (Boolean) -> Unit) {
        if (isDisposed) {
            callback(false)
            return
        }

        backgroundHandler.post {
            if (isDisposed) return@post

            try {
                preferences.edit {
                    putBoolean(SharedPrefsConstants.THEME_KEY, darkThemeEnabled)
                }
                mainHandler.post {
                    if (!isDisposed) {
                        callback(true)
                    } else {
                        callback(false)
                    }
                }
            } catch (e: Exception) {
                return@post
            }
        }
    }

    fun dispose() {
        isDisposed = true
        mainHandler.removeCallbacksAndMessages(null)
        backgroundHandler.looper.quitSafely()
    }


}


