package com.solitaire.game

import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.AdView
import com.solitaire.game.managers.GameManager
import com.solitaire.game.managers.AdManager
import com.solitaire.game.managers.AudioManager
import com.solitaire.game.managers.AssetManager
import com.solitaire.game.managers.SaveManager
import com.solitaire.game.managers.FeatureManager
import com.solitaire.game.models.Difficulty
import com.solitaire.game.ui.GameView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameActivity : AppCompatActivity() {
    
    private lateinit var gameView: GameView
    private lateinit var gameManager: GameManager
    private lateinit var adManager: AdManager
    private lateinit var audioManager: AudioManager
    private lateinit var assetManager: AssetManager
    private lateinit var saveManager: SaveManager
    private lateinit var featureManager: FeatureManager
    
    private lateinit var tvTimer: TextView
    private lateinit var tvScore: TextView
    private lateinit var btnUndo: Button
    private lateinit var btnHint: Button
    private lateinit var btnReveal: Button
    private lateinit var btnNewGame: Button
    private lateinit var btnDrawStock: Button
    private lateinit var tvHintsLeft: TextView
    private lateinit var tvUndosLeft: TextView
    private lateinit var tvRevealsLeft: TextView
    
    private var timerJob: kotlinx.coroutines.Job? = null
    private var selectedDifficulty: Difficulty = Difficulty.NORMAL
    private var isGameOver = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        
        selectedDifficulty = (intent.getSerializableExtra("difficulty") as? Difficulty) ?: Difficulty.NORMAL
        
        initializeManagers()
        initializeViews()
        startNewGame()
    }
    
    private fun initializeManagers() {
        assetManager = AssetManager(this)
        assetManager.loadAssets()
        
        gameManager = GameManager()
        adManager = AdManager(this)
        audioManager = AudioManager(this)
        audioManager.loadSoundEffects()
        audioManager.startBackgroundMusic()
        
        saveManager = SaveManager(this)
        featureManager = FeatureManager(this)
    }
    
    private fun initializeViews() {
        gameView = findViewById(R.id.game_view)
        tvTimer = findViewById(R.id.tv_timer)
        tvScore = findViewById(R.id.tv_score)
        btnUndo = findViewById(R.id.btn_undo)
        btnHint = findViewById(R.id.btn_hint)
        btnReveal = findViewById(R.id.btn_reveal)
        btnNewGame = findViewById(R.id.btn_new_game)
        btnDrawStock = findViewById(R.id.btn_draw_stock)
        tvHintsLeft = findViewById(R.id.tv_hints_left)
        tvUndosLeft = findViewById(R.id.tv_undos_left)
        tvRevealsLeft = findViewById(R.id.tv_reveals_left)
        
        // Setup game view
        gameView.setGameManager(gameManager, assetManager)
        gameView.setMoveCallback { _, _ ->
            audioManager.playCardPlace()
            updateUI()
        }
        gameView.setGameOverCallback { onGameWon() }
        
        // Setup button listeners
        btnUndo.setOnClickListener { onUndoClicked() }
        btnHint.setOnClickListener { onHintClicked() }
        btnReveal.setOnClickListener { onRevealClicked() }
        btnNewGame.setOnClickListener { onNewGameClicked() }
        btnDrawStock.setOnClickListener { onDrawStockClicked() }
        
        // Setup banner ad
        val bannerAd = findViewById<AdView>(R.id.banner_ad)
        adManager.createBannerAd(bannerAd)
    }
    
    private fun startNewGame() {
        isGameOver = false
        gameManager.initializeGame(selectedDifficulty)
        gameView.invalidate()
        startTimer()
        updateUI()
    }
    
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = lifecycleScope.launch {
            while (!isGameOver) {
                val elapsed = gameManager.getCurrentElapsedTime()
                val minutes = elapsed / 60000
                val seconds = (elapsed % 60000) / 1000
                
                tvTimer.text = String.format("%02d:%02d", minutes, seconds)
                
                // Check time limit
                val gameState = gameManager.getGameState()
                if (gameState.difficulty.timeLimit > 0 &&
                    elapsed > gameState.difficulty.timeLimit) {
                    onGameOver(false, "⏰ Time's up!")
                    break
                }
                
                delay(1000)
            }
        }
    }
    
    private fun onDrawStockClicked() {
        if (!isGameOver) {
            gameManager.drawFromStock()
            audioManager.playCardFlip()
            gameView.invalidate()
            updateUI()
        }
    }
    
    private fun onUndoClicked() {
        if (!isGameOver) {
            if (gameManager.undoMove()) {
                gameView.invalidate()
                updateUI()
            } else {
                audioManager.playErrorSound()
                showToast("❌ No undo available!")
            }
        }
    }
    
    private fun onHintClicked() {
        if (!isGameOver) {
            val hint = gameManager.getHint()
            if (hint != null) {
                val (from, to) = hint
                showToast("💡 Try: $from → $to")
            } else {
                audioManager.playErrorSound()
                showToast("❌ No hints available!")
            }
            updateUI()
        }
    }
    
    private fun onRevealClicked() {
        if (!isGameOver) {
            if (gameManager.revealCard()) {
                audioManager.playCardFlip()
                gameView.invalidate()
                updateUI()
            } else {
                audioManager.playErrorSound()
                showToast("❌ No reveals available!")
            }
        }
    }
    
    private fun updateUI() {
        val gameState = gameManager.getGameState()
        tvScore.text = "Score: ${gameState.score}"
        tvHintsLeft.text = "Hints: ${gameState.difficulty.hintCount - gameState.hintsUsed}"
        tvUndosLeft.text = "Undos: ${gameState.difficulty.undoCount - gameState.undosUsed}"
        tvRevealsLeft.text = "Reveals: ${gameState.difficulty.revealCount - gameState.revealsUsed}"
    }
    
    private fun onNewGameClicked() {
        if (isGameOver) {
            startNewGame()
        } else {
            AlertDialog.Builder(this)
                .setTitle("Abandon Game?")
                .setMessage("Are you sure you want to start a new game?")
                .setPositiveButton("Yes") { _, _ -> startNewGame() }
                .setNegativeButton("No", null)
                .show()
        }
    }
    
    private fun onGameWon() {
        isGameOver = true
        timerJob?.cancel()
        
        val gameState = gameManager.getGameState()
        val completionTime = gameState.gameEndTime!! - gameState.gameStartTime
        
        audioManager.playWinSound()
        gameView.addWinAnimation()
        
        lifecycleScope.launch {
            // Save win history
            saveManager.saveWinHistory(
                completionTimeMs = completionTime,
                difficulty = gameState.difficulty.name,
                score = gameState.score,
                moves = gameState.moves
            )
            
            // Update ad stats
            adManager.recordGameCompletion(true)
            
            // Update quests
            featureManager.updateQuestProgress("${gameState.difficulty.name}_WIN", 1)
            
            delay(500)
            showWinDialog(completionTime, gameState.score)
        }
    }
    
    private fun onGameOver(isWon: Boolean, reason: String) {
        if (isWon) {
            onGameWon()
        } else {
            isGameOver = true
            timerJob?.cancel()
            audioManager.playErrorSound()
            showToast(reason)
            adManager.recordGameCompletion(false)
        }
    }
    
    private fun showWinDialog(timeMs: Long, score: Int) {
        val minutes = timeMs / 60000
        val seconds = (timeMs % 60000) / 1000
        val timeString = String.format("%02d:%02d", minutes, seconds)
        
        AlertDialog.Builder(this)
            .setTitle("🎉 Congratulations!")
            .setMessage("""Completed in $timeString
                |Difficulty: ${selectedDifficulty.displayName}
                |Score: $score
                |Moves: ${gameManager.getGameState().moves}
            """.trimMargin())
            .setPositiveButton("New Game") { _, _ ->
                if (adManager.hasInterstitialAdReady()) {
                    adManager.showInterstitialAdIfAvailable(this@GameActivity)
                }
                startNewGame()
            }
            .setNegativeButton("Back to Menu") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        audioManager.release()
        assetManager.release()
        adManager.release()
    }
}
