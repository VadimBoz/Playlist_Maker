package com.vadim.playlistmaker

import android.content.Context
import android.util.TypedValue
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import kotlin.math.roundToInt

fun <T: Number> Context.dpToPx(dp: T): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        this.resources.displayMetrics
    ).roundToInt()
}

fun String.cleanText(): String {
    return this
        .trim()
        .replace("\\s+".toRegex(), " ")
        .replace("[\\u0000-\\u001F\\u007F-\\u009F]".toRegex(), "")
        .replace("[\\u2028\\u2029]".toRegex(), "")
        .replace("\uFEFF".toRegex(), "")
}

fun String?.epochTimeToTxt(): String {
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