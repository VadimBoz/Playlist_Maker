package com.vadim.playlistmaker

import android.content.Context
import android.util.TypedValue
import kotlin.math.roundToInt

fun <T: Number> Context.dpToPx(dp: T): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        this.resources.displayMetrics
    ).roundToInt()
}