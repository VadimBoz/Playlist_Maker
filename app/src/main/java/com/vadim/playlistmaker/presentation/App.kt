package com.vadim.playlistmaker.presentation

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatDelegate
import com.vadim.playlistmaker.Creator
import com.vadim.playlistmaker.domain.useCase.ThemeUseCase
import com.vadim.playlistmaker.domain.useCase.TrackHistoryUseCase
import com.vadim.playlistmaker.domain.useCase.TrackSearchUseCase
import com.vadim.playlistmaker.domain.repository.ImageLoader


class App : Application() {

    val themeUseCase: ThemeUseCase by lazy { Creator.provideThemeUseCase(this) }
    val trackSearchUseCase: TrackSearchUseCase by lazy { Creator.provideTrackSearchUseCase() }
    val trackHistoryUseCase: TrackHistoryUseCase by lazy { Creator.provideTrackHistoryUseCase(this) }
    val imageLoader: ImageLoader by lazy { Creator.provideImageLoader(this) }
    private val mainHandler = Handler(Looper.getMainLooper())


    override fun onCreate() {
        super.onCreate()
        themeUseCase.getAndApplyTheme()

    }



    override fun onTerminate() {
        super.onTerminate()
        mainHandler.removeCallbacksAndMessages(null)
    }
}