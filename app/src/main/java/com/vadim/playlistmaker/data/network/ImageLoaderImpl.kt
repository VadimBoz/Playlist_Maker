package com.vadim.playlistmaker.data.network

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.vadim.playlistmaker.R
import com.vadim.playlistmaker.data.dto.ImageDataDto
import com.vadim.playlistmaker.domain.model.ImageData
import com.vadim.playlistmaker.domain.model.Track
import com.vadim.playlistmaker.domain.repository.ImageLoader
import com.vadim.playlistmaker.data.extension.artWorkToFullSize
import com.vadim.playlistmaker.data.extension.imageDataDtoToImageData
import com.vadim.playlistmaker.data.extension.trackToTrackDto
import java.io.ByteArrayOutputStream

class ImageLoaderImpl(private val context: Context) : ImageLoader {

    override fun loadTrackImage(track: Track, callback: (ImageData) -> Unit) {
        val trackDto = track.trackToTrackDto()
        loadImage(trackDto.artworkUrl100) { imageDataDto ->
            callback(imageDataDto.imageDataDtoToImageData())
        }
    }

    override fun loadTrackCoverImage(track: Track, callback: (ImageData) -> Unit) {
        val trackDto = track.trackToTrackDto()
        loadImage(trackDto.artworkUrl100?.artWorkToFullSize()) { imageDataDto ->
            callback(imageDataDto.imageDataDtoToImageData())
        }
    }

    private fun loadImage(url: String?, callback: (ImageDataDto) -> Unit) {
        var cornerRadiusRes = 0
        if (url?.contains("512x512bb") ?: false) {
            cornerRadiusRes =
                context.resources.getDimensionPixelSize(R.dimen.corner_radius_art_work)
        }

        Glide.with(context)
            .asBitmap()
            .load(url)
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .apply {
                if (cornerRadiusRes > 0) {
                    transform(CenterCrop(), RoundedCorners(cornerRadiusRes))
                } 
                else {
                    transform(CenterCrop())
                }
            }
            .into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                val stream = ByteArrayOutputStream()
                resource.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val imageBytes = stream.toByteArray()
                callback(ImageDataDto(
                    imageBytes = imageBytes,
                    errorMessage = null))
            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                callback(ImageDataDto(
                    imageBytes = null,
                    errorMessage = "Ошибка загрузки изображения"
                ))
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                //
            }
        })
    }

}