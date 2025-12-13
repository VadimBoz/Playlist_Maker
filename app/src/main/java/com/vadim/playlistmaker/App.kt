package com.vadim.playlistmaker

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import kotlin.apply

private const val PREFS_NAME = "settings_preferences"
private const val KEY_SETTINGS_THEME = "settings_theme"

class App: Application() {

    var darkTheme = false
    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }


    override fun onCreate() {
        super.onCreate()
        darkTheme = sharedPreferences.getBoolean(KEY_SETTINGS_THEME, false)
        switchTheme(darkTheme)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        darkTheme = darkThemeEnabled
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )

        sharedPreferences
            .edit()
            .putBoolean(KEY_SETTINGS_THEME, darkThemeEnabled)
            .apply()
    }
}