package com.solitaire.game.models

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WinHistory(
    val id: Int,
    val completionTime: Long,  // milliseconds
    val dateTimestamp: Long,   // System.currentTimeMillis()
    val difficulty: String,
    val score: Int,
    val moves: Int
) : Serializable {
    
    fun getFormattedTime(): String {
        val minutes = completionTime / 60000
        val seconds = (completionTime % 60000) / 1000
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(dateTimestamp))
    }
    
    fun getShortDate(): String {
        val sdf = SimpleDateFormat("MM/dd/yy", Locale.getDefault())
        return sdf.format(Date(dateTimestamp))
    }
    
    fun getShortTime(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(dateTimestamp))
    }
    
    fun getDifficultyColor(): Int {
        return when (difficulty) {
            "EASY" -> android.graphics.Color.GREEN
            "NORMAL" -> android.graphics.Color.BLUE
            "HARD" -> android.graphics.Color.parseColor("#FFA500") // Orange
            "EXTREME" -> android.graphics.Color.RED
            "LEGENDARY" -> android.graphics.Color.parseColor("#FFD700") // Gold
            else -> android.graphics.Color.BLACK
        }
    }
}
