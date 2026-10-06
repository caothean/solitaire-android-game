package com.solitaire.game

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.solitaire.game.managers.SaveManager
import com.solitaire.game.models.Difficulty
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var saveManager: SaveManager
    private lateinit var tvWelcome: TextView
    private lateinit var tvStats: TextView
    private lateinit var btnEasy: Button
    private lateinit var btnNormal: Button
    private lateinit var btnHard: Button
    private lateinit var btnExtreme: Button
    private lateinit var btnLegendary: Button
    private lateinit var btnHistory: Button
    private lateinit var btnSettings: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        saveManager = SaveManager(this)
        initializeViews()
        initializeDailyFeatures()
        loadAndDisplayStats()
    }
    
    private fun initializeViews() {
        tvWelcome = findViewById(R.id.tv_welcome)
        tvStats = findViewById(R.id.tv_stats)
        btnEasy = findViewById(R.id.btn_easy)
        btnNormal = findViewById(R.id.btn_normal)
        btnHard = findViewById(R.id.btn_hard)
        btnExtreme = findViewById(R.id.btn_extreme)
        btnLegendary = findViewById(R.id.btn_legendary)
        btnHistory = findViewById(R.id.btn_history)
        btnSettings = findViewById(R.id.btn_settings)
        
        // Set click listeners
        btnEasy.setOnClickListener { startGame(Difficulty.EASY) }
        btnNormal.setOnClickListener { startGame(Difficulty.NORMAL) }
        btnHard.setOnClickListener { startGame(Difficulty.HARD) }
        btnExtreme.setOnClickListener { startGame(Difficulty.EXTREME) }
        btnLegendary.setOnClickListener { startGame(Difficulty.LEGENDARY) }
        btnHistory.setOnClickListener { openHistory() }
        btnSettings.setOnClickListener { openSettings() }
    }
    
    private fun initializeDailyFeatures() {
        lifecycleScope.launch {
            saveManager.initializeDailyQuests()
            saveManager.initializeCardSets()
            saveManager.initializeAchievements()
        }
    }
    
    private fun loadAndDisplayStats() {
        lifecycleScope.launch {
            val totalWins = saveManager.getTotalGamesWon()
            val loginStreak = saveManager.getLoginStreak()
            val eventPoints = saveManager.getEventPoints()
            
            val statsText = buildString {
                append("📊 Stats\n")
                append("Total Wins: $totalWins\n")
                append("Login Streak: 🔥 $loginStreak days\n")
                append("Event Points: ⭐ $eventPoints")
            }
            
            tvStats.text = statsText
            
            val greeting = when (loginStreak) {
                0 -> "Welcome to Solitaire!"
                1 -> "Good to see you!"
                else -> "Welcome back! 🔥 You're on a $loginStreak day streak!"
            }
            tvWelcome.text = greeting
        }
    }
    
    private fun startGame(difficulty: Difficulty) {
        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra("difficulty", difficulty)
        }
        startActivity(intent)
    }
    
    private fun openHistory() {
        val intent = Intent(this, HistoryActivity::class.java)
        startActivity(intent)
    }
    
    private fun openSettings() {
        // TODO: Implement settings screen
    }
    
    override fun onResume() {
        super.onResume()
        loadAndDisplayStats()
    }
}
