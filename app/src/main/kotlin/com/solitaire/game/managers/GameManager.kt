package com.solitaire.game.managers

import com.solitaire.game.models.*
import com.solitaire.game.utils.Constants
import kotlinx.coroutines.*
import kotlin.random.Random

class GameManager {
    
    private val stacks = mutableMapOf<String, CardStack>()
    private lateinit var gameState: GameState
    private val moveHistory = mutableListOf<GameMove>()
    private val cardManager = CardManager()
    
    data class GameMove(
        val fromStack: String,
        val toStack: String,
        val cardCount: Int,
        val timestamp: Long
    )
    
    fun initializeGame(difficulty: Difficulty) {
        gameState = GameState(difficulty = difficulty)
        moveHistory.clear()
        
        // Initialize stack structures
        initializeStacks()
        
        // Tạo bộ bài và xáo
        val deck = cardManager.createDeck()
        deck.shuffle(Random(System.nanoTime()))
        
        // Phân phối bài theo quy tắc Solitaire (Klondike)
        dealCards(deck)
    }
    
    private fun initializeStacks() {
        stacks.clear()
        
        // 7 tableau piles
        for (i in 0..6) {
            stacks["tableau_$i"] = CardStack("tableau_$i")
        }
        
        // 4 foundation piles
        for (i in 0..3) {
            stacks["foundation_$i"] = CardStack("foundation_$i")
        }
        
        // Stock và Waste
        stacks["stock"] = CardStack("stock")
        stacks["waste"] = CardStack("waste")
    }
    
    private fun dealCards(deck: List<Card>) {
        var cardIndex = 0
        
        // Phân phối cho 7 tableau (mỗi tableau nhận n lá từ 1 đến 7)
        for (tableauIndex in 0..6) {
            for (j in tableauIndex..6) {
                val card = deck[cardIndex++]
                card.isFaceUp = (j == tableauIndex) // Chỉ lá cuối cùng mặt ngửa
                stacks["tableau_$j"]?.pushCard(card)
            }
        }
        
        // Phần còn lại vào stock
        while (cardIndex < deck.size) {
            val card = deck[cardIndex++]
            card.isFaceUp = false
            stacks["stock"]?.pushCard(card)
        }
    }
    
    /**
     * Rút bài từ stock và đặt vào waste
     */
    fun drawFromStock() {
        val stock = stacks["stock"]!!
        val waste = stacks["waste"]!!
        
        if (stock.isEmpty()) {
            // Kiểm tra điều kiện xào lại
            if (gameState.difficulty.maxWastePiles != -1 &&
                gameState.wasteReshuffles >= gameState.difficulty.maxWastePiles) {
                return // Không được phép xào thêm nữa
            }
            
            // Xào lại: chuyển waste trở lại stock
            while (!waste.isEmpty()) {
                val card = waste.popCard()!!
                card.isFaceUp = false
                stock.pushCard(card)
            }
            gameState.wasteReshuffles++
        } else {
            // Rút từ 1 đến 3 lá từ stock
            val drawCount = gameState.difficulty.cardsDrawn
            repeat(drawCount) {
                if (!stock.isEmpty()) {
                    val card = stock.popCard()!!
                    card.isFaceUp = true
                    waste.pushCard(card)
                }
            }
        }
    }
    
    /**
     * Di chuyển thẻ từ stack này sang stack khác
     */
    fun moveCard(fromStackId: String, toStackId: String, cardCount: Int = 1): Boolean {
        val fromStack = stacks[fromStackId] ?: return false
        val toStack = stacks[toStackId] ?: return false
        
        if (fromStack.isEmpty()) return false
        
        // Lấy các lá cần di chuyển
        val cardsToMove = if (cardCount > 1) {
            fromStack.cards.takeLast(cardCount)
        } else {
            listOf(fromStack.cards.last())
        }
        
        // Kiểm tra có thể đặt lên toStack không
        if (!toStack.canPlaceCard(cardsToMove.first())) return false
        
        // Verify sequence validity cho tableau
        if (toStackId.startsWith("tableau_") && !toStack.isEmpty()) {
            if (!cardManager.isValidSequence(cardsToMove)) return false
        }
        
        // Thực hiện di chuyển
        repeat(cardCount) {
            val card = fromStack.popCard()
            if (card != null) {
                toStack.pushCard(card)
            }
        }
        
        // Lưu vào lịch sử
        moveHistory.add(GameMove(fromStackId, toStackId, cardCount, System.currentTimeMillis()))
        gameState.moves++
        updateScore()
        
        // Mở lá tiếp theo ở tableau nếu cần
        if (fromStackId.startsWith("tableau_") || fromStackId == "stock") {
            fromStack.getTopCard()?.let { it.isFaceUp = true }
        }
        
        return true
    }
    
    /**
     * Hoàn tác lần di chuyển cuối cùng
     */
    fun undoMove(): Boolean {
        if (moveHistory.isEmpty() || gameState.undosUsed >= gameState.difficulty.undoCount) {
            return false
        }
        
        val lastMove = moveHistory.removeAt(moveHistory.size - 1)
        val fromStack = stacks[lastMove.toStack]!!
        val toStack = stacks[lastMove.fromStack]!!
        
        // Di chuyển lại ngược
        repeat(lastMove.cardCount) {
            val card = fromStack.popCard()
            if (card != null) {
                toStack.pushCard(card)
            }
        }
        
        gameState.undosUsed++
        gameState.moves--
        updateScore()
        return true
    }
    
    /**
     * Gợi ý nước đi tiếp theo
     */
    fun getHint(): Pair<String, String>? {
        if (gameState.hintsUsed >= gameState.difficulty.hintCount) {
            return null
        }
        
        // Ưu tiên di chuyển lên foundation
        for (stackId in stacks.keys) {
            val stack = stacks[stackId]!!
            if (stack.isEmpty()) continue
            
            val topCard = stack.getTopCard() ?: continue
            if (!topCard.isFaceUp) continue
            
            // Kiểm tra foundation trước
            for (i in 0..3) {
                val foundationId = "foundation_$i"
                val foundation = stacks[foundationId]!!
                
                if (foundation.canPlaceCard(topCard)) {
                    gameState.hintsUsed++
                    return Pair(stackId, foundationId)
                }
            }
        }
        
        // Sau đó kiểm tra tableau
        for (stackId in stacks.keys) {
            val stack = stacks[stackId]!!
            if (stack.isEmpty()) continue
            
            val topCard = stack.getTopCard() ?: continue
            if (!topCard.isFaceUp) continue
            
            for (i in 0..6) {
                val tableauId = "tableau_$i"
                if (tableauId == stackId) continue
                val tableau = stacks[tableauId]!!
                
                if (tableau.canPlaceCard(topCard)) {
                    gameState.hintsUsed++
                    return Pair(stackId, tableauId)
                }
            }
        }
        
        gameState.hintsUsed++
        return null
    }
    
    /**
     * Mở khóa 1 thẻ ẩn (reveal)
     */
    fun revealCard(): Boolean {
        if (gameState.revealsUsed >= gameState.difficulty.revealCount) {
            return false
        }
        
        // Tìm thẻ ẩn trong tableau
        for (i in 0..6) {
            val tableau = stacks["tableau_$i"]!!
            tableau.getTopCard()?.let {
                if (!it.isFaceUp) {
                    it.isFaceUp = true
                    gameState.revealsUsed++
                    gameState.moves++
                    return true
                }
            }
        }
        
        return false
    }
    
    /**
     * Tính điểm: 10 điểm cho mỗi lá lên foundation
     */
    private fun updateScore() {
        val foundation = (0..3).sumOf { stacks["foundation_$it"]?.cards?.size ?: 0 }
        gameState.score = (foundation * 10 * gameState.difficulty.pointMultiplier).toInt()
    }
    
    /**
     * Kiểm tra điều kiện thắng
     */
    fun checkWinCondition(): Boolean {
        return cardManager.checkWinCondition(stacks)
    }
    
    // ============ GETTERS ============
    
    fun getGameState(): GameState = gameState.copy()
    
    fun getStacks(): Map<String, CardStack> = stacks.toMap()
    
    fun getCurrentElapsedTime(): Long = System.currentTimeMillis() - gameState.gameStartTime
    
    fun getStack(stackId: String): CardStack? = stacks[stackId]
    
    fun isGameWon(): Boolean = gameState.isWon
    
    fun canDrawFromStock(): Boolean = !stacks["stock"]!!.isEmpty() || gameState.wasteReshuffles < gameState.difficulty.maxWastePiles
    
    fun setGameWon(won: Boolean) {
        gameState = gameState.copy(
            isWon = won,
            gameEndTime = System.currentTimeMillis()
        )
    }
}
