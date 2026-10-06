package com.solitaire.game.models

import java.io.Serializable

enum class Suit(val symbol: String) : Serializable {
    HEARTS("♥"),
    DIAMONDS("♦"),
    CLUBS("♣"),
    SPADES("♠")
}

enum class Rank(val value: Int, val shortName: String) : Serializable {
    ACE(1, "A"),
    TWO(2, "2"),
    THREE(3, "3"),
    FOUR(4, "4"),
    FIVE(5, "5"),
    SIX(6, "6"),
    SEVEN(7, "7"),
    EIGHT(8, "8"),
    NINE(9, "9"),
    TEN(10, "10"),
    JACK(11, "J"),
    QUEEN(12, "Q"),
    KING(13, "K")
}

data class Card(
    val suit: Suit,
    val rank: Rank,
    var isFaceUp: Boolean = false,
    var x: Float = 0f,
    var y: Float = 0f,
    var isSelected: Boolean = false
) : Serializable {
    
    fun isRed(): Boolean = suit in listOf(Suit.HEARTS, Suit.DIAMONDS)
    
    fun isBlack(): Boolean = !isRed()
    
    fun getDisplayName(): String = "${rank.name} of ${suit.name}"
    
    fun getShortName(): String = "${rank.shortName}${suit.symbol}"
    
    override fun toString(): String = "${rank.shortName}${suit.ordinal}"
    
    // Serialization for database storage
    fun toSerialString(): String = "${suit.ordinal}|${rank.ordinal}|$isFaceUp"
    
    companion object {
        fun fromSerialString(str: String): Card? {
            return try {
                val parts = str.split("|")
                if (parts.size != 3) return null
                Card(
                    suit = Suit.values()[parts[0].toInt()],
                    rank = Rank.values()[parts[1].toInt()],
                    isFaceUp = parts[2].toBoolean()
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

data class CardStack(
    val id: String, // "stock", "waste", "foundation_0_3", "tableau_0_6"
    var cards: MutableList<Card> = mutableListOf()
) : Serializable {
    
    fun canPlaceCard(card: Card): Boolean {
        return when {
            id.startsWith("foundation_") -> canPlaceOnFoundation(card)
            id.startsWith("tableau_") -> canPlaceOnTableau(card)
            id == "waste" || id == "stock" -> false
            else -> false
        }
    }
    
    private fun canPlaceOnFoundation(card: Card): Boolean {
        if (cards.isEmpty()) return card.rank == Rank.ACE
        val topCard = cards.last()
        return card.suit == topCard.suit &&
               card.rank.value == topCard.rank.value + 1
    }
    
    private fun canPlaceOnTableau(card: Card): Boolean {
        if (cards.isEmpty()) return card.rank == Rank.KING
        val topCard = cards.last()
        return card.isRed() != topCard.isRed() && // Alternating colors
               card.rank.value == topCard.rank.value - 1
    }
    
    fun isEmpty(): Boolean = cards.isEmpty()
    
    fun getTopCard(): Card? = cards.lastOrNull()
    
    fun getTopVisibleCard(): Card? {
        // For tableau: return topmost card
        // For foundation: return topmost card
        return cards.lastOrNull()
    }
    
    fun popCard(): Card? = if (cards.isNotEmpty()) cards.removeAt(cards.size - 1) else null
    
    fun popCards(count: Int): List<Card> {
        val result = mutableListOf<Card>()
        repeat(count.coerceAtMost(cards.size)) {
            cards.lastOrNull()?.let {
                result.add(it)
                cards.remove(it)
            }
        }
        return result.reversed() // Maintain order
    }
    
    fun pushCard(card: Card) {
        cards.add(card)
    }
    
    fun pushCards(cardsList: List<Card>) {
        cards.addAll(cardsList)
    }
    
    fun getCardCount(): Int = cards.size
    
    fun getVisibleCardCount(): Int = cards.size
    
    fun peekCard(index: Int): Card? = cards.getOrNull(index)
    
    fun clear() {
        cards.clear()
    }
}
