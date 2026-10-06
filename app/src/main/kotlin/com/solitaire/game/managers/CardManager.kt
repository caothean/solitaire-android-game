package com.solitaire.game.managers

import com.solitaire.game.models.Card
import com.solitaire.game.models.Rank
import com.solitaire.game.models.Suit
import com.solitaire.game.utils.Constants

class CardManager {
    
    /**
     * Tạo bộ bài hoàn chỉnh 52 lá
     */
    fun createDeck(): MutableList<Card> {
        val deck = mutableListOf<Card>()
        for (suit in Suit.values()) {
            for (rank in Rank.values()) {
                deck.add(Card(suit = suit, rank = rank, isFaceUp = false))
            }
        }
        return deck
    }
    
    /**
     * Tính toán tọa độ thẻ dựa vào vị trí trong stack
     */
    fun calculateCardPosition(
        stackCenterX: Float,
        stackCenterY: Float,
        indexInStack: Int,
        isTableau: Boolean = false
    ): Pair<Float, Float> {
        val offsetY = if (isTableau) {
            // Trong tableau, thẻ xếp chồng với offset nhỏ
            Constants.CARD_OVERLAP_TABLEAU * indexInStack
        } else {
            // Trong foundation/waste, không overlap
            0f
        }
        return Pair(stackCenterX, stackCenterY + offsetY)
    }
    
    /**
     * Kiểm tra điểm (x,y) có nằm trong vùng thẻ nào
     * Trả về Pair<StackId, CardIndex> hoặc null nếu không tìm thấy
     */
    fun getCardAtPoint(
        x: Float,
        y: Float,
        cardWidth: Float,
        cardHeight: Float,
        stackPositions: Map<String, Pair<Float, Float>>,
        stacks: Map<String, com.solitaire.game.models.CardStack>
    ): Pair<String, Int>? {
        // Từ dưới lên để lấy thẻ trên cùng (interactive)
        for ((stackId, stack) in stacks) {
            if (stack.isEmpty()) continue
            
            val pos = stackPositions[stackId] ?: continue
            val stackX = pos.first
            val stackY = pos.second
            
            // Từ card cuối cùng (top) lên đầu tiên (bottom)
            for (i in stack.cards.indices.reversed()) {
                val card = stack.cards[i]
                
                // Tính tọa độ Y của thẻ (có overlap nếu tableau)
                val cardY = if (stackId.startsWith("tableau_")) {
                    stackY + (Constants.CARD_OVERLAP_TABLEAU * i).coerceAtMost(cardHeight - 10f)
                } else {
                    stackY
                }
                
                // Kiểm tra điểm có nằm trong hitbox của thẻ
                if (x >= stackX && x <= stackX + cardWidth &&
                    y >= cardY && y <= cardY + cardHeight) {
                    return Pair(stackId, i)
                }
            }
        }
        return null
    }
    
    /**
     * Kiểm tra xem thẻ có thể di chuyển từ stack này không
     */
    fun canMoveFromStack(stackId: String, cardIndex: Int, stack: com.solitaire.game.models.CardStack): Boolean {
        // Chỉ có thể di chuyển thẻ mặt ngửa
        return stack.cards.getOrNull(cardIndex)?.isFaceUp ?: false
    }
    
    /**
     * Lấy tất cả thẻ từ index trở đi trong stack (cho kéo thả nhiều thẻ)
     */
    fun getCardsFromIndex(
        stack: com.solitaire.game.models.CardStack,
        fromIndex: Int
    ): List<Card>? {
        if (fromIndex < 0 || fromIndex >= stack.cards.size) return null
        return stack.cards.subList(fromIndex, stack.cards.size)
    }
    
    /**
     * Kiểm tra dãy thẻ có valid không (màu xen kẽ, rank liên tiếp giảm)
     */
    fun isValidSequence(cards: List<Card>): Boolean {
        if (cards.isEmpty()) return false
        
        for (i in 1 until cards.size) {
            val prev = cards[i - 1]
            val curr = cards[i]
            
            // Kiểm tra màu xen kẽ
            if (prev.isRed() == curr.isRed()) return false
            
            // Kiểm tra rank liên tiếp giảm
            if (prev.rank.value != curr.rank.value + 1) return false
        }
        
        return true
    }
    
    /**
     * Tìm tất cả nước đi hợp lệ từ một stack
     */
    fun findValidMoves(
        stackId: String,
        stack: com.solitaire.game.models.CardStack,
        allStacks: Map<String, com.solitaire.game.models.CardStack>
    ): List<Pair<String, String>> {
        val validMoves = mutableListOf<Pair<String, String>>()
        
        if (stack.isEmpty()) return validMoves
        
        val topCard = stack.getTopCard() ?: return validMoves
        if (!topCard.isFaceUp) return validMoves
        
        // Kiểm tra xem có thể đặt lên foundation nào không
        for (i in 0..3) {
            val foundationId = "foundation_$i"
            val foundation = allStacks[foundationId] ?: continue
            
            if (foundation.canPlaceCard(topCard)) {
                validMoves.add(Pair(stackId, foundationId))
            }
        }
        
        // Kiểm tra xem có thể đặt lên tableau nào không
        for (i in 0..6) {
            val tableauId = "tableau_$i"
            if (tableauId == stackId) continue
            val tableau = allStacks[tableauId] ?: continue
            
            if (tableau.canPlaceCard(topCard)) {
                validMoves.add(Pair(stackId, tableauId))
            }
        }
        
        return validMoves
    }
    
    /**
     * Kiểm tra xem game đã thắng hay chưa
     */
    fun checkWinCondition(allStacks: Map<String, com.solitaire.game.models.CardStack>): Boolean {
        // Thắng khi tất cả 52 thẻ đều ở foundation (13 thẻ x 4 foundation)
        for (i in 0..3) {
            val foundation = allStacks["foundation_$i"] ?: continue
            if (foundation.cards.size != 13) return false
        }
        return true
    }
    
    /**
     * Đếm thẻ mặt ngửa trong tableau
     */
    fun countFaceUpCardsInTableau(allStacks: Map<String, com.solitaire.game.models.CardStack>): Int {
        var count = 0
        for (i in 0..6) {
            val tableau = allStacks["tableau_$i"] ?: continue
            count += tableau.cards.count { it.isFaceUp }
        }
        return count
    }
}
