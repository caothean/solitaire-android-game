package com.solitaire.game.managers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import com.solitaire.game.utils.Constants

class AssetManager(private val context: Context) {
    
    companion object {
        private const val TAG = "AssetManager"
        private const val CARD_WIDTH = 80
        private const val CARD_HEIGHT = 120
    }
    
    private val cardBitmaps = mutableMapOf<String, Bitmap>()
    private val paintCache = mutableMapOf<String, Paint>()
    
    fun loadAssets() {
        loadCardBitmaps()
        loadPaints()
    }
    
    private fun loadCardBitmaps() {
        try {
            // Tạo placeholder bitmaps cho card
            for (suit in 0..3) {
                for (rank in 0..12) {
                    val key = "card_${suit}_${rank}"
                    cardBitmaps[key] = createCardBitmap(suit, rank)
                }
            }
            
            // Card back
            cardBitmaps["card_back"] = createCardBackBitmap()
            
            // Empty stack placeholder
            cardBitmaps["stack_empty"] = createEmptyStackBitmap()
            
            Log.d(TAG, "Card bitmaps loaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading card bitmaps", e)
        }
    }
    
    private fun createCardBitmap(suit: Int, rank: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        
        // Background trắng
        canvas.drawColor(Color.WHITE)
        
        // Border đen
        val borderPaint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        canvas.drawRect(2f, 2f, CARD_WIDTH - 2f, CARD_HEIGHT - 2f, borderPaint)
        
        // Tính toán rank và suit symbol
        val rankNames = arrayOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
        val suitSymbols = arrayOf("♥", "♦", "♣", "♠")
        val suitColors = arrayOf(Color.RED, Color.RED, Color.BLACK, Color.BLACK)
        
        val rankText = rankNames[rank]
        val suitText = suitSymbols[suit]
        val suitColor = suitColors[suit]
        
        // Vẽ rank ở trên trái
        val rankPaint = Paint().apply {
            color = suitColor
            textSize = 16f
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText(rankText, 8f, 20f, rankPaint)
        
        // Vẽ suit ở giữa
        val suitPaint = Paint().apply {
            color = suitColor
            textSize = 40f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(suitText, CARD_WIDTH / 2f, CARD_HEIGHT / 2f + 15f, suitPaint)
        
        // Vẽ rank ở dưới phải (xoay 180 độ - placeholder)
        canvas.drawText(rankText, CARD_WIDTH - 22f, CARD_HEIGHT - 8f, rankPaint)
        
        return bitmap
    }
    
    private fun createCardBackBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        
        // Background xanh da trời gradient
        canvas.drawColor(Color.parseColor("#1976D2"))
        
        // Pattern trên mặt sau
        val patternPaint = Paint().apply {
            color = Color.parseColor("#0D47A1")
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        
        // Vẽ grid pattern
        var y = 10f
        while (y < CARD_HEIGHT) {
            canvas.drawLine(10f, y, CARD_WIDTH - 10f, y, patternPaint)
            y += 15f
        }
        
        // Border trắng
        val borderPaint = Paint().apply {
            color = Color.WHITE
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        canvas.drawRect(2f, 2f, CARD_WIDTH - 2f, CARD_HEIGHT - 2f, borderPaint)
        
        return bitmap
    }
    
    private fun createEmptyStackBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        
        // Transparent background
        canvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)
        
        // Dashed border
        val dashPaint = Paint().apply {
            color = Color.parseColor("#FFFFFF")
            strokeWidth = 2f
            style = Paint.Style.STROKE
            pathEffect = android.graphics.DashPathEffect(floatArrayOf(5f, 5f), 0f)
        }
        canvas.drawRect(2f, 2f, CARD_WIDTH - 2f, CARD_HEIGHT - 2f, dashPaint)
        
        return bitmap
    }
    
    private fun loadPaints() {
        // Chứa các paint objects để tái sử dụng
        paintCache["card_border"] = Paint().apply {
            color = Color.BLACK
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }
        
        paintCache["text_rank"] = Paint().apply {
            color = Color.BLACK
            textSize = 14f
            textAlign = Paint.Align.CENTER
        }
        
        paintCache["background_green"] = Paint().apply {
            color = Color.parseColor(Constants.COLOR_GREEN_DARK)
            style = Paint.Style.FILL
        }
    }
    
    fun getCardBitmap(suit: Int, rank: Int): Bitmap? {
        return cardBitmaps["card_${suit}_${rank}"]
    }
    
    fun getCardBackBitmap(): Bitmap? {
        return cardBitmaps["card_back"]
    }
    
    fun getEmptyStackBitmap(): Bitmap? {
        return cardBitmaps["stack_empty"]
    }
    
    fun getPaint(name: String): Paint? {
        return paintCache[name]
    }
    
    fun release() {
        try {
            cardBitmaps.values.forEach { it.recycle() }
            cardBitmaps.clear()
            paintCache.clear()
            Log.d(TAG, "Assets released successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing assets", e)
        }
    }
}
