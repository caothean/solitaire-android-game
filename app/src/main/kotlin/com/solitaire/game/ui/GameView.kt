package com.solitaire.game.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.solitaire.game.managers.GameManager
import com.solitaire.game.managers.AssetManager
import com.solitaire.game.models.Card
import com.solitaire.game.models.CardStack
import com.solitaire.game.utils.Constants
import kotlin.math.abs
import kotlin.math.sqrt

class GameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    private var gameManager: GameManager? = null
    private var assetManager: AssetManager? = null
    
    private val cardWidth = Constants.CARD_WIDTH.toFloat()
    private val cardHeight = Constants.CARD_HEIGHT.toFloat()
    private val screenPadding = Constants.SCREEN_PADDING
    private val stackSpacing = Constants.STACK_SPACING
    
    private val cardPaint = Paint().apply { isAntiAlias = true }
    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 14f
        isAntiAlias = true
    }
    private val borderPaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 2f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    
    private lateinit var stackPositions: Map<String, Pair<Float, Float>>
    
    private var selectedCard: Pair<String, Int>? = null  // (stackId, cardIndex)
    private var dragX = 0f
    private var dragY = 0f
    private var isDragging = false
    private var dragOffsetX = 0f
    private var dragOffsetY = 0f
    
    private val animationFrames = mutableListOf<AnimationFrame>()
    data class AnimationFrame(
        val type: String,  // "deal", "win", "pop"
        val x: Float,
        val y: Float,
        val duration: Long,
        val startTime: Long
    )
    
    private var gameOverCallback: (() -> Unit)? = null
    private var moveCallback: ((String, String) -> Unit)? = null
    
    fun setGameManager(manager: GameManager, assets: AssetManager) {
        gameManager = manager
        assetManager = assets
        calculateStackPositions()
        invalidate()
    }
    
    fun setGameOverCallback(callback: () -> Unit) {
        gameOverCallback = callback
    }
    
    fun setMoveCallback(callback: (String, String) -> Unit) {
        moveCallback = callback
    }
    
    private fun calculateStackPositions() {
        val positions = mutableMapOf<String, Pair<Float, Float>>()
        
        // Stock, Waste, Foundation (hàng trên)
        positions["stock"] = Pair(screenPadding, screenPadding)
        positions["waste"] = Pair(screenPadding + cardWidth + 20f, screenPadding)
        for (i in 0..3) {
            positions["foundation_$i"] = Pair(
                screenPadding + (cardWidth + 20f) * (3 + i),
                screenPadding
            )
        }
        
        // Tableau (hàng dưới)
        for (i in 0..6) {
            positions["tableau_$i"] = Pair(
                screenPadding + (cardWidth + 20f) * i,
                screenPadding + cardHeight + 60f
            )
        }
        
        stackPositions = positions
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val gameManager = gameManager ?: return
        val stacks = gameManager.getStacks()
        
        // Vẽ background
        canvas.drawColor(Color.parseColor(Constants.COLOR_GREEN_DARK))
        
        // Vẽ tất cả các stack
        for ((stackId, stack) in stacks) {
            drawStack(canvas, stackId, stack)
        }
        
        // Vẽ hiệu ứng animation
        drawAnimations(canvas)
        
        // Vẽ thẻ đang kéo (drag preview)
        if (isDragging && selectedCard != null) {
            drawDraggingCard(canvas)
        }
    }
    
    private fun drawStack(canvas: Canvas, stackId: String, stack: CardStack) {
        val pos = stackPositions[stackId] ?: return
        val x = pos.first
        val y = pos.second
        
        // Vẽ vùng stack (hình chữ nhật dashed hoặc solid)
        if (stack.isEmpty()) {
            // Empty stack - dashed border
            val dashPaint = Paint().apply {
                color = Color.WHITE
                strokeWidth = 2f
                style = Paint.Style.STROKE
                pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 0f)
            }
            canvas.drawRect(x, y, x + cardWidth, y + cardHeight, dashPaint)
        } else {
            // Non-empty stack - solid border
            canvas.drawRect(x, y, x + cardWidth, y + cardHeight, borderPaint)
        }
        
        // Vẽ các thẻ trong stack
        for ((index, card) in stack.cards.withIndex()) {
            val cardY = if (stackId.startsWith("tableau_")) {
                // Tableau: overlap cards
                y + (Constants.CARD_OVERLAP_TABLEAU * index).coerceAtMost(cardHeight - 10f)
            } else {
                // Foundation, Stock, Waste: không overlap
                y
            }
            
            drawCard(canvas, card, x, cardY)
        }
    }
    
    private fun drawCard(canvas: Canvas, card: Card, x: Float, y: Float) {
        try {
            val bitmap = if (card.isFaceUp) {
                assetManager?.getCardBitmap(card.suit.ordinal, card.rank.ordinal)
            } else {
                assetManager?.getCardBackBitmap()
            }
            
            if (bitmap != null) {
                canvas.drawBitmap(
                    Bitmap.createScaledBitmap(
                        bitmap,
                        cardWidth.toInt(),
                        cardHeight.toInt(),
                        true
                    ),
                    x, y, cardPaint
                )
            }
        } catch (e: Exception) {
            // Fallback: vẽ rectangle
            val paint = Paint().apply {
                color = if (card.isFaceUp) Color.WHITE else Color.parseColor("#1976D2")
                style = Paint.Style.FILL
            }
            canvas.drawRect(x, y, x + cardWidth, y + cardHeight, paint)
            canvas.drawRect(x, y, x + cardWidth, y + cardHeight, borderPaint)
        }
    }
    
    private fun drawDraggingCard(canvas: Canvas) {
        val (stackId, cardIndex) = selectedCard ?: return
        val gameManager = gameManager ?: return
        val stack = gameManager.getStacks()[stackId] ?: return
        val card = stack.cards.getOrNull(cardIndex) ?: return
        
        val paint = Paint().apply { alpha = 200 }
        canvas.saveLayer(dragX, dragY, dragX + cardWidth, dragY + cardHeight, paint)
        drawCard(canvas, card, dragX - dragOffsetX, dragY - dragOffsetY)
        canvas.restore()
    }
    
    private fun drawAnimations(canvas: Canvas) {
        val now = System.currentTimeMillis()
        val animationsToRemove = mutableListOf<AnimationFrame>()
        
        for (frame in animationFrames) {
            val elapsed = now - frame.startTime
            if (elapsed > frame.duration) {
                animationsToRemove.add(frame)
                continue
            }
            
            when (frame.type) {
                "deal" -> drawDealAnimation(canvas, frame, elapsed)
                "win" -> drawWinAnimation(canvas, frame, elapsed)
                "pop" -> drawPopAnimation(canvas, frame, elapsed)
            }
        }
        
        animationFrames.removeAll(animationsToRemove)
        if (animationFrames.isNotEmpty()) {
            postInvalidateDelayed(16)  // ~60 FPS
        }
    }
    
    private fun drawDealAnimation(canvas: Canvas, frame: AnimationFrame, elapsed: Long) {
        val progress = (elapsed.toFloat() / frame.duration).coerceIn(0f, 1f)
        val scale = (1f - progress * 0.2f).coerceAtLeast(0.8f)
        
        val paint = Paint().apply {
            alpha = (255 * (1 - progress * 0.3f)).toInt()
        }
        
        val w = cardWidth * scale
        val h = cardHeight * scale
        val offsetX = (cardWidth - w) / 2
        val offsetY = (cardHeight - h) / 2
        
        val rect = RectF(
            frame.x + offsetX,
            frame.y + offsetY,
            frame.x + offsetX + w,
            frame.y + offsetY + h
        )
        canvas.drawRect(rect, paint)
    }
    
    private fun drawWinAnimation(canvas: Canvas, frame: AnimationFrame, elapsed: Long) {
        val progress = (elapsed.toFloat() / frame.duration)
        val distance = 200f * progress
        
        val paint = Paint().apply {
            color = Color.YELLOW
            alpha = (255 * (1 - progress)).toInt()
        }
        
        for (i in 0..7) {
            val angle = (i * 45).toFloat() * Math.PI / 180f
            val px = frame.x + distance * Math.cos(angle).toFloat()
            val py = frame.y + distance * Math.sin(angle).toFloat()
            canvas.drawCircle(px, py, 5f, paint)
        }
    }
    
    private fun drawPopAnimation(canvas: Canvas, frame: AnimationFrame, elapsed: Long) {
        val progress = (elapsed.toFloat() / frame.duration)
        val scale = 1f + progress * 0.5f
        
        val paint = Paint().apply {
            alpha = (255 * (1 - progress)).toInt()
        }
        
        val w = cardWidth * scale
        val h = cardHeight * scale
        
        val rect = RectF(
            frame.x - (w - cardWidth) / 2,
            frame.y - (h - cardHeight) / 2,
            frame.x + (w - cardWidth) / 2 + cardWidth,
            frame.y + (h - cardHeight) / 2 + cardHeight
        )
        canvas.drawRect(rect, paint)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val gameManager = gameManager ?: return false
        
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                dragX = event.x
                dragY = event.y
                
                val stacks = gameManager.getStacks()
                val touchResult = findCardAtPosition(dragX, dragY, stacks)
                
                if (touchResult != null) {
                    selectedCard = touchResult
                    val (stackId, cardIndex) = touchResult
                    val stack = stacks[stackId]!!
                    val card = stack.cards[cardIndex]
                    
                    val pos = stackPositions[stackId]!!
                    dragOffsetX = dragX - pos.first
                    dragOffsetY = dragY - pos.second
                    
                    isDragging = true
                    invalidate()
                }
                return true
            }
            
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    dragX = event.x
                    dragY = event.y
                    invalidate()
                    return true
                }
            }
            
            MotionEvent.ACTION_UP -> {
                if (isDragging && selectedCard != null) {
                    val (fromStackId, _) = selectedCard!!
                    val targetStackId = findStackAtPosition(dragX, dragY)
                    
                    if (targetStackId != null && targetStackId != fromStackId) {
                        if (gameManager.moveCard(fromStackId, targetStackId)) {
                            addDealAnimation(dragX, dragY)
                            moveCallback?.invoke(fromStackId, targetStackId)
                            
                            // Check win condition
                            if (gameManager.checkWinCondition()) {
                                gameManager.setGameWon(true)
                                addWinAnimation()
                                gameOverCallback?.invoke()
                            }
                        }
                    }
                    
                    isDragging = false
                    selectedCard = null
                    invalidate()
                    return true
                }
            }
        }
        
        return super.onTouchEvent(event)
    }
    
    private fun findCardAtPosition(
        x: Float,
        y: Float,
        stacks: Map<String, CardStack>
    ): Pair<String, Int>? {
        for ((stackId, stack) in stacks) {
            if (stack.isEmpty()) continue
            
            val pos = stackPositions[stackId] ?: continue
            val stackX = pos.first
            val stackY = pos.second
            
            // Từ dưới lên để lấy thẻ trên cùng (interactive)
            for (i in stack.cards.indices.reversed()) {
                val card = stack.cards[i]
                
                val cardY = if (stackId.startsWith("tableau_")) {
                    stackY + (Constants.CARD_OVERLAP_TABLEAU * i).coerceAtMost(cardHeight - 10f)
                } else {
                    stackY
                }
                
                if (x >= stackX && x <= stackX + cardWidth &&
                    y >= cardY && y <= cardY + cardHeight) {
                    return Pair(stackId, i)
                }
            }
        }
        return null
    }
    
    private fun findStackAtPosition(x: Float, y: Float): String? {
        for ((stackId, pos) in stackPositions) {
            val sx = pos.first
            val sy = pos.second
            
            if (x >= sx - 20f && x <= sx + cardWidth + 20f &&
                y >= sy - 20f && y <= sy + cardHeight + 20f) {
                return stackId
            }
        }
        return null
    }
    
    fun addDealAnimation(x: Float, y: Float) {
        animationFrames.add(
            AnimationFrame("deal", x, y, Constants.DEAL_ANIMATION_DURATION, System.currentTimeMillis())
        )
    }
    
    fun addWinAnimation() {
        val centerX = width / 2f
        val centerY = height / 2f
        repeat(10) {
            animationFrames.add(
                AnimationFrame("win", centerX, centerY, Constants.WIN_ANIMATION_DURATION, System.currentTimeMillis())
            )
        }
    }
}
