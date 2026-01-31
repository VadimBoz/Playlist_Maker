package com.vadim.playlistmaker.domain.useCase

import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatDelegate
import com.vadim.playlistmaker.domain.repository.ThemeRepository

class ThemeUseCase(private val themeRepository: ThemeRepository) {

    private val mainHandler = Handler(Looper.getMainLooper())

    fun getTheme(callback: (Boolean) -> Unit) = themeRepository.getCurrentTheme(callback)

    fun switchTheme(callback: (Boolean) -> Unit) {
        getTheme { currentTheme ->
            saveTheme(!currentTheme, callback)
        }
    }

    fun saveTheme(darkThemeEnabled: Boolean, callback: (Boolean) -> Unit) {
        themeRepository.saveTheme(darkThemeEnabled, callback)
    }

    fun applyThemeToUi(darkThemeEnabled: Boolean) {
        mainHandler.post {
            AppCompatDelegate.setDefaultNightMode(
                if (darkThemeEnabled) {
                    AppCompatDelegate.MODE_NIGHT_YES
                } else {
                    AppCompatDelegate.MODE_NIGHT_NO
                }
            )
        }
    }

    fun getAndApplyTheme(callback: (Boolean) -> Unit = {}) {
        getTheme { theme ->
            applyThemeToUi(theme)
            callback(theme)
        }
    }

    fun saveAndApplyTheme(darkThemeEnabled: Boolean, callback: (Boolean) -> Unit = {}) {
        saveTheme(darkThemeEnabled) { success ->
            if (success) {
                applyThemeToUi(darkThemeEnabled)
            }
            callback(success)
        }
    }

}