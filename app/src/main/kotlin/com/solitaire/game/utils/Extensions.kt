package com.solitaire.game.utils

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Toast Extensions
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

// SharedPreferences Extensions
fun SharedPreferences.putInt(key: String, value: Int) {
    edit().putInt(key, value).apply()
}

fun SharedPreferences.putLong(key: String, value: Long) {
    edit().putLong(key, value).apply()
}

fun SharedPreferences.putBoolean(key: String, value: Boolean) {
    edit().putBoolean(key, value).apply()
}

fun SharedPreferences.putString(key: String, value: String) {
    edit().putString(key, value).apply()
}

fun SharedPreferences.incrementInt(key: String, default: Int = 0, increment: Int = 1) {
    val current = getInt(key, default)
    putInt(key, current + increment)
}

// Date Extensions
fun Long.formatTime(): String {
    val minutes = this / 60000
    val seconds = (this % 60000) / 1000
    return String.format("%02d:%02d", minutes, seconds)
}

fun Long.formatDate(): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.formatShortDate(): String {
    val sdf = SimpleDateFormat("MM/dd/yy", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.getDateKey(): Long {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    return calendar.timeInMillis / 86400000
}

fun isConsecutiveDay(lastDate: Long): Boolean {
    if (lastDate == 0L) return false
    val today = System.currentTimeMillis().getDateKey()
    val yesterday = lastDate.getDateKey()
    return today - yesterday == 1L
}

// Float Extensions
fun Float.coerceInRange(min: Float, max: Float): Float {
    return when {
        this < min -> min
        this > max -> max
        else -> this
    }
}

// Math Extensions
fun Int.clamp(min: Int, max: Int): Int {
    return when {
        this < min -> min
        this > max -> max
        else -> this
    }
}

fun calculateDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val dx = x2 - x1
    val dy = y2 - y1
    return kotlin.math.sqrt(dx * dx + dy * dy)
}

// List Extensions
fun <T> List<T>.second(): T? {
    return if (size >= 2) this[1] else null
}

fun <T> List<T>.lastOrNull(predicate: (T) -> Boolean): T? {
    for (i in indices.reversed()) {
        if (predicate(this[i])) return this[i]
    }
    return null
}
