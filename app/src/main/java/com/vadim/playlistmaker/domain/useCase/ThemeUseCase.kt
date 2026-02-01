package com.vadim.playlistmaker.domain.useCase

interface ThemeUseCase {
    fun getTheme(callback: (Boolean) -> Unit)
    fun saveTheme(darkThemeEnabled: Boolean, callback: (Boolean) -> Unit)
    fun applyThemeToUi(darkThemeEnabled: Boolean)
    fun getAndApplyTheme(callback: (Boolean) -> Unit)
    fun saveAndApplyTheme(darkThemeEnabled: Boolean, callback: (Boolean) -> Unit)
}