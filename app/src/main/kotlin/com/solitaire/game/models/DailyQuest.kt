package com.solitaire.game.models

import java.io.Serializable

data class DailyQuest(
    val id: Int,
    val questType: String,     // WIN_5_EASY, WIN_HARD_UNDER_180S, etc
    val description: String,
    var progress: Int,
    val target: Int,
    val rewardPoints: Int,
    var isCompleted: Boolean,
    val dateCreated: Long,
    var isClaimed: Boolean
) : Serializable {
    
    fun getProgressPercent(): Int = (progress * 100 / target).coerceAtMost(100)
    
    fun getProgressText(): String = "$progress / $target"
    
    fun getIcon(): String = when (questType) {
        "WIN_5_EASY" -> "🎮"
        "WIN_HARD_UNDER_180S" -> "⚡"
        "TOTAL_MOVES_100" -> "🎯"
        "WIN_EXTREME_1" -> "💪"
        "STREAK_WINS_3" -> "🔥"
        "NO_UNDO_CHALLENGE" -> "🎲"
        else -> "✨"
    }
    
    fun canClaim(): Boolean = isCompleted && !isClaimed
}
