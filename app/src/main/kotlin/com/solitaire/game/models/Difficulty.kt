package com.solitaire.game.models

import java.io.Serializable

enum class Difficulty(
    val displayName: String,
    val cardsDrawn: Int,              // Số lá rút từ stock mỗi lần
    val maxWastePiles: Int,           // Số lần xào lại (-1 = vô hạn)
    val timeLimit: Long = -1,         // milliseconds, -1 = không giới hạn
    val pointMultiplier: Float = 1.0f,// Điểm thắng = 500 * multiplier
    val hintCount: Int = 3,
    val undoCount: Int = 3,
    val revealCount: Int = 1          // Số lần mở khóa thẻ ẩn
) : Serializable {
    
    EASY(
        displayName = "Easy",
        cardsDrawn = 1,
        maxWastePiles = -1,           // Xào lại vô hạn
        timeLimit = -1,
        pointMultiplier = 1.0f,
        hintCount = 5,
        undoCount = 5,
        revealCount = 3
    ),
    
    NORMAL(
        displayName = "Normal",
        cardsDrawn = 1,
        maxWastePiles = 3,            // Xào lại 3 lần
        timeLimit = -1,
        pointMultiplier = 1.2f,
        hintCount = 3,
        undoCount = 3,
        revealCount = 2
    ),
    
    HARD(
        displayName = "Hard",
        cardsDrawn = 3,                // Rút 3 lá cùng lúc
        maxWastePiles = 3,
        timeLimit = -1,
        pointMultiplier = 1.5f,
        hintCount = 2,
        undoCount = 2,
        revealCount = 1
    ),
    
    EXTREME(
        displayName = "Extreme",
        cardsDrawn = 3,
        maxWastePiles = 1,             // Chỉ xào lại 1 lần
        timeLimit = 5 * 60 * 1000,     // 5 phút
        pointMultiplier = 2.0f,
        hintCount = 1,
        undoCount = 1,
        revealCount = 0
    ),
    
    LEGENDARY(
        displayName = "Legendary",
        cardsDrawn = 3,
        maxWastePiles = 1,
        timeLimit = 3 * 60 * 1000,     // 3 phút
        pointMultiplier = 3.0f,
        hintCount = 0,                 // Không có hint
        undoCount = 0,                 // Không có undo
        revealCount = 0
    );
    
    fun getDescription(): String {
        return when (this) {
            EASY -> "Perfect for learning - unlimited reshuffles"
            NORMAL -> "Draw 1 card, standard rules"
            HARD -> "Challenge - draw 3 cards, 3 reshuffles"
            EXTREME -> "Very difficult - 5 min time limit"
            LEGENDARY -> "Master level - 3 min time limit, no boosters"
        }
    }
}

data class GameState(
    val difficulty: Difficulty,
    var score: Int = 0,
    var moves: Int = 0,
    var elapsedTime: Long = 0L,
    var isWon: Boolean = false,
    var wasteReshuffles: Int = 0,
    var hintsUsed: Int = 0,
    var undosUsed: Int = 0,
    var revealsUsed: Int = 0,
    val gameStartTime: Long = System.currentTimeMillis(),
    var gameEndTime: Long? = null,
    var cardStates: Map<String, String> = emptyMap() // Serialized states
) : Serializable {
    
    fun getElapsedTimeMs(): Long = System.currentTimeMillis() - gameStartTime
    
    fun getCompletionTimeMs(): Long = (gameEndTime ?: System.currentTimeMillis()) - gameStartTime
    
    fun isTimeLimitExceeded(): Boolean {
        return difficulty.timeLimit > 0 && getElapsedTimeMs() > difficulty.timeLimit
    }
    
    fun canUseHint(): Boolean = hintsUsed < difficulty.hintCount
    
    fun canUseUndo(): Boolean = undosUsed < difficulty.undoCount
    
    fun canUseReveal(): Boolean = revealsUsed < difficulty.revealCount
    
    fun calculateFinalScore(): Int {
        val baseScore = 500
        val foundationBonus = score
        val timePenalty = if (difficulty.timeLimit > 0) {
            (getCompletionTimeMs() / 1000).toInt() * 2
        } else {
            0
        }
        val movePenalty = moves / 2
        
        return ((baseScore + foundationBonus - timePenalty - movePenalty) * difficulty.pointMultiplier).toInt()
            .coerceAtLeast(0)
    }
}
