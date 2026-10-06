package com.solitaire.game.utils

object Constants {
    // Game Configuration
    const val TOTAL_CARDS = 52
    const val CARD_WIDTH = 80f
    const val CARD_HEIGHT = 120f
    const val CARD_CORNER_RADIUS = 8f
    
    // Stack Positions
    const val TABLEAU_COLUMNS = 7
    const val FOUNDATION_PILES = 4
    const val CARD_OVERLAP_TABLEAU = 20f
    const val CARD_SPACING_FOUNDATION = 20f
    
    // Screen Padding
    const val SCREEN_PADDING = 20f
    const val STACK_SPACING = 100f
    
    // Animation Durations (ms)
    const val DEAL_ANIMATION_DURATION = 300L
    const val WIN_ANIMATION_DURATION = 1000L
    const val POP_ANIMATION_DURATION = 500L
    const val CARD_FLIP_DURATION = 200L
    
    // AdMob Configuration
    // IMPORTANT: Replace these with your actual Ad Unit IDs from AdMob Console
    const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111" // Test ID
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712" // Test ID
    
    // AdMob Conditions
    const val MIN_GAMES_FOR_INTERSTITIAL = 20
    const val MIN_GAMES_BETWEEN_ADS = 2
    const val MAX_GAMES_BETWEEN_ADS = 3
    
    // Database
    const val DATABASE_NAME = "solitaire_db"
    const val DATABASE_VERSION = 2
    
    // Shared Preferences Keys
    const val PREFS_NAME = "com.solitaire.game.prefs"
    const val PREFS_TOTAL_GAMES = "total_games_played"
    const val PREFS_GAMES_SINCE_AD = "games_since_last_ad"
    const val PREFS_LAST_QUEST_DATE = "last_quest_date"
    const val PREFS_LAST_LOGIN_DATE = "last_login_date"
    const val PREFS_LOGIN_STREAK = "login_streak"
    const val PREFS_EVENT_POINTS = "event_points"
    const val PREFS_LAST_SPIN_DATE = "last_spin_date"
    const val PREFS_UNLOCKED_ACHIEVEMENTS = "unlocked_achievements"
    const val PREFS_AUDIO_ENABLED = "audio_enabled"
    const val PREFS_MUSIC_ENABLED = "music_enabled"
    
    // Collection
    const val PIECES_TO_UNLOCK = 10
    
    // Daily Quest
    const val DAILY_QUEST_COUNT = 3
    
    // Audio
    const val AUDIO_VOLUME = 0.7f
    
    // Colors
    const val COLOR_GREEN_DARK = "#004d1a"
    const val COLOR_GREEN_LIGHT = "#00ff4d"
    const val COLOR_RED = "#ff0000"
    const val COLOR_BLACK = "#000000"
    const val COLOR_WHITE = "#ffffff"
}

// Stack IDs
fun getTableauStackId(index: Int) = "tableau_$index"
fun getFoundationStackId(suit: Int) = "foundation_$suit"
const val STOCK_STACK_ID = "stock"
const val WASTE_STACK_ID = "waste"
