package com.vadim.playlistmaker.data.dto

data class ImageDataDto(
    val imageBytes: ByteArray? = null,
    val errorMessage: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageDataDto

        if (!imageBytes.contentEquals(other.imageBytes)) return false
        if (errorMessage != other.errorMessage) return false

        return true
    }

    override fun hashCode(): Int {
        var result = imageBytes?.contentHashCode() ?: 0
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        return result
    }
}