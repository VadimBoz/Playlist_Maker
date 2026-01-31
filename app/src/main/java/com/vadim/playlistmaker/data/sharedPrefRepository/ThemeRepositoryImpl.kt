package com.vadim.playlistmaker.data.sharedPrefRepository

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import androidx.core.content.edit
import com.vadim.playlistmaker.data.sharedPrefRepository.ThemeRepositoryConstants.SETTING_PREFS
import com.vadim.playlistmaker.data.sharedPrefRepository.ThemeRepositoryConstants.THEME_KEY
import com.vadim.playlistmaker.domain.repository.ThemeRepository

private object ThemeRepositoryConstants {
    const val SETTING_PREFS = "settings_preferences"
    const val THEME_KEY = "settings_theme"
}

class ThemeRepositoryImpl(private val context: Context) : ThemeRepository {

    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(SETTING_PREFS, Context.MODE_PRIVATE)
    }

    override fun getCurrentTheme(callback: (Boolean) -> Unit) {
        try {
            val theme = preferences.getBoolean(THEME_KEY, false)
            callback(theme)
        } catch (e: Exception) {
            callback(false)
        }
    }

    override fun saveTheme(darkThemeEnabled: Boolean, callback: (Boolean) -> Unit) {
        try {
            preferences.edit {
                putBoolean(THEME_KEY, darkThemeEnabled)
            }
            callback(true)
        } catch (e: Exception) {
            callback(false)
        }
    }

}


