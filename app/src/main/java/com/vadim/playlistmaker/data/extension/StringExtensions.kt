package com.vadim.playlistmaker.data.extension

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit

fun String.cleanText(): String {
    return this
        .trim()
        .replace("\\s+".toRegex(), " ")
        .replace("[\\u0000-\\u001F\\u007F-\\u009F]".toRegex(), "")
        .replace("[\\u2028\\u2029]".toRegex(), "")
        .replace("\uFEFF".toRegex(), "")
}

fun String?.epochTimeToFormatedTxt(): String {
    return this?.let {
        SimpleDateFormat("mm:ss", Locale.getDefault()).format(it.toLong())
    } ?: ""
}

fun String.parseToYear(): Int {
    return runCatching {
        LocalDateTime.parse(this, DateTimeFormatter.ISO_DATE_TIME).year
    }.getOrDefault(0)
}

fun String.artWorkToFullSize(): String {
    return this.replaceAfterLast('/',"512x512bb.jpg")
}

fun String.timeFormatToMillisString(): String {
    return try {
        val parts = this.split(":")
        when (parts.size) {
            2 -> { // MM:SS
                val minutes = parts[0].toLong()
                val seconds = parts[1].toLong()
                val millis = TimeUnit.MINUTES.toMillis(minutes) +
                        TimeUnit.SECONDS.toMillis(seconds)
                millis.toString()
            }
            1 -> {
                val seconds = parts[0].toLong()
                TimeUnit.SECONDS.toMillis(seconds).toString()
            }
            else -> "0"
        }
    } catch (e: Exception) {
        "0"
    }
}