package com.vadim.playlistmaker.domain.useCase

import androidx.appcompat.app.AppCompatDelegate
import com.vadim.playlistmaker.domain.repository.ThemeRepository

class ThemeUseCaseImpl(private val themeRepository: ThemeRepository
): ThemeUseCase {

    override fun getTheme(callback: (Boolean) -> Unit) = themeRepository.getCurrentTheme(callback)

    override fun saveTheme(darkThemeEnabled: Boolean, callback: (Boolean) -> Unit) {
        themeRepository.saveTheme(darkThemeEnabled, callback)
    }

    override fun applyThemeToUi(darkThemeEnabled: Boolean) {
            AppCompatDelegate.setDefaultNightMode(
                if (darkThemeEnabled) {
                    AppCompatDelegate.MODE_NIGHT_YES
                } else {
                    AppCompatDelegate.MODE_NIGHT_NO
                }
            )
    }

    override fun getAndApplyTheme(callback: (Boolean) -> Unit) {
        getTheme { theme ->
            applyThemeToUi(theme)
            callback(theme)
        }
    }

    override fun saveAndApplyTheme(darkThemeEnabled: Boolean, callback: (Boolean) -> Unit) {
        saveTheme(darkThemeEnabled) { success ->
            if (success) {
                applyThemeToUi(darkThemeEnabled)
            }
            callback(success)
        }
    }

}