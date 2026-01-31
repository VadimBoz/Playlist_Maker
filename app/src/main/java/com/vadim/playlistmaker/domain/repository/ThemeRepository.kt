package com.vadim.playlistmaker.domain.repository

import com.vadim.playlistmaker.domain.model.Track

interface ThemeRepository {
    fun getCurrentTheme(callback: (Boolean) -> Unit)
    fun saveTheme(darkThemeEnabled: Boolean, callback: (Boolean) -> Unit)
}