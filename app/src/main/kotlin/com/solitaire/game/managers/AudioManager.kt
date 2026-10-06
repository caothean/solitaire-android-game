package com.solitaire.game.managers

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.solitaire.game.utils.Constants

class AudioManager(private val context: Context) {
    
    companion object {
        private const val TAG = "AudioManager"
    }
    
    private val soundEffects = mutableMapOf<String, MediaPlayer>()
    private var backgroundMusic: MediaPlayer? = null
    private val sharedPrefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
    
    private val audioEnabled: Boolean
        get() = sharedPrefs.getBoolean(Constants.PREFS_AUDIO_ENABLED, true)
    
    private val musicEnabled: Boolean
        get() = sharedPrefs.getBoolean(Constants.PREFS_MUSIC_ENABLED, true)
    
    fun loadSoundEffects() {
        try {
            // Tạo placeholder cho các sound effects
            val soundIds = listOf(
                "card_flip" to android.R.raw.notification,
                "card_place" to android.R.raw.notification,
                "win" to android.R.raw.notification,
                "error" to android.R.raw.notification
            )
            
            for ((name, resId) in soundIds) {
                try {
                    val mediaPlayer = MediaPlayer.create(context, resId)
                    mediaPlayer?.let {
                        it.volume = Constants.AUDIO_VOLUME
                        soundEffects[name] = it
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to load sound: $name", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading sound effects", e)
        }
    }
    
    fun playSound(soundName: String) {
        if (!audioEnabled) return
        
        try {
            val mediaPlayer = soundEffects[soundName]
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound: $soundName", e)
        }
    }
    
    fun playCardFlip() = playSound("card_flip")
    fun playCardPlace() = playSound("card_place")
    fun playWinSound() = playSound("win")
    fun playErrorSound() = playSound("error")
    
    fun startBackgroundMusic() {
        if (!musicEnabled) return
        
        try {
            backgroundMusic = MediaPlayer.create(context, android.R.raw.notification)
            backgroundMusic?.let {
                it.isLooping = true
                it.volume = Constants.AUDIO_VOLUME * 0.5f
                it.start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting background music", e)
        }
    }
    
    fun stopBackgroundMusic() {
        try {
            backgroundMusic?.stop()
            backgroundMusic?.release()
            backgroundMusic = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping background music", e)
        }
    }
    
    fun toggleAudio(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(Constants.PREFS_AUDIO_ENABLED, enabled).apply()
    }
    
    fun toggleMusic(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(Constants.PREFS_MUSIC_ENABLED, enabled).apply()
        if (enabled) {
            startBackgroundMusic()
        } else {
            stopBackgroundMusic()
        }
    }
    
    fun release() {
        try {
            soundEffects.values.forEach { it.release() }
            soundEffects.clear()
            backgroundMusic?.release()
            backgroundMusic = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing audio resources", e)
        }
    }
}
