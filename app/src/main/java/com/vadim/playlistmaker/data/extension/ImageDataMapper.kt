package com.vadim.playlistmaker.data.extension

import com.vadim.playlistmaker.data.dto.ImageDataDto
import com.vadim.playlistmaker.domain.model.ImageData

fun ImageDataDto.imageDataDtoToImageData(): ImageData {
    return ImageData(
        imageBytes = this.imageBytes,
        errorMessage = this.errorMessage
    )
}

fun ImageData.imageDataToImageDataDto(): ImageDataDto {
    return ImageDataDto(
        imageBytes = this.imageBytes,
        errorMessage = this.errorMessage
    )
}