package com.vadim.playlistmaker

import android.content.Context
import com.vadim.playlistmaker.data.network.ImageLoaderImpl
import com.vadim.playlistmaker.data.network.RetrofitNetworkClient
import com.vadim.playlistmaker.data.network.TrackRepositoryImpl
import com.vadim.playlistmaker.data.sharedPrefRepository.HistoryRepositoryImpl
import com.vadim.playlistmaker.data.sharedPrefRepository.ThemeRepositoryImpl
import com.vadim.playlistmaker.domain.repository.ImageLoader
import com.vadim.playlistmaker.domain.useCase.ThemeUseCase
import com.vadim.playlistmaker.domain.useCase.TrackHistoryUseCase
import com.vadim.playlistmaker.domain.useCase.TrackSearchUseCase

object Creator {

    fun provideThemeUseCase(context: Context): ThemeUseCase {
        val themeRepository = ThemeRepositoryImpl(context)
        return ThemeUseCase(themeRepository)
    }

    fun provideTrackSearchUseCase(): TrackSearchUseCase {
        val trackSearchRepository = TrackRepositoryImpl(
            RetrofitNetworkClient.createApiService()
        )
        return TrackSearchUseCase(trackSearchRepository)
    }

    fun provideTrackHistoryUseCase(context: Context): TrackHistoryUseCase {
        val trackHistoryRepository = HistoryRepositoryImpl(context)
        return TrackHistoryUseCase(trackHistoryRepository)
    }

    fun provideImageLoader(context: Context): ImageLoader {
        return ImageLoaderImpl(context)
    }


}