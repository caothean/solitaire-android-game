package com.solitaire.game.managers

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.solitaire.game.utils.Constants

class AdManager(private val context: Context) {
    
    companion object {
        private const val TAG = "AdManager"
    }
    
    private var interstitialAd: InterstitialAd? = null
    private var isLoadingInterstitial = false
    private var totalGamesPlayed = 0
    private var gamesSinceLastAd = 0
    
    private val sharedPreferences = context.getSharedPreferences(
        Constants.PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    init {
        MobileAds.initialize(context)
        loadSavedStats()
    }
    
    // ============ BANNER ADS ============
    
    fun createBannerAd(container: AdView) {
        val adRequest = AdRequest.Builder().build()
        container.loadAd(adRequest)
        
        container.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d(TAG, "Banner ad loaded successfully")
            }
            
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.w(TAG, "Banner ad failed to load: ${adError.message}")
            }
            
            override fun onAdOpened() {
                Log.d(TAG, "Banner ad opened")
            }
            
            override fun onAdClicked() {
                Log.d(TAG, "Banner ad clicked")
            }
        }
    }
    
    // ============ INTERSTITIAL ADS ============
    
    /**
     * Ghi nhận khi người chơi hoàn thành một ván
     * Kiểm tra điều kiện hiển thị interstitial ad
     */
    fun recordGameCompletion(isWon: Boolean) {
        totalGamesPlayed++
        gamesSinceLastAd++
        
        saveStats()
        
        // Kiểm tra điều kiện để hiển thị interstitial
        if (shouldShowInterstitialAd()) {
            loadInterstitialAd()
        }
    }
    
    /**
     * Logic điều kiện hiển thị Interstitial Ad:
     * 1. Chỉ sau khi đã chơi ít nhất 20 ván
     * 2. Random 2-3 ván một lần mới hiển thị 1 lần
     */
    private fun shouldShowInterstitialAd(): Boolean {
        // Điều kiện 1: Phải chơi ít nhất 20 ván
        if (totalGamesPlayed < Constants.MIN_GAMES_FOR_INTERSTITIAL) {
            Log.d(TAG, "Not enough games played yet: $totalGamesPlayed < ${Constants.MIN_GAMES_FOR_INTERSTITIAL}")
            return false
        }
        
        // Điều kiện 2: Random 2-3 ván một lần
        val interval = (Constants.MIN_GAMES_BETWEEN_ADS..Constants.MAX_GAMES_BETWEEN_ADS).random()
        if (gamesSinceLastAd >= interval) {
            Log.d(TAG, "Showing interstitial: gamesSinceLastAd=$gamesSinceLastAd, interval=$interval")
            gamesSinceLastAd = 0
            return true
        }
        
        return false
    }
    
    private fun loadInterstitialAd() {
        if (isLoadingInterstitial || interstitialAd != null) {
            Log.d(TAG, "Interstitial ad already loading or available")
            return
        }
        
        isLoadingInterstitial = true
        val adRequest = AdRequest.Builder().build()
        
        Log.d(TAG, "Loading interstitial ad...")
        
        InterstitialAd.load(
            context,
            Constants.INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded successfully")
                    interstitialAd = ad
                    isLoadingInterstitial = false
                    
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            Log.d(TAG, "Interstitial ad dismissed")
                            interstitialAd = null
                            // Load lại quảng cáo tiếp theo
                            loadInterstitialAd()
                        }
                        
                        override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                            Log.e(TAG, "Interstitial ad failed to show: ${adError.message}")
                            interstitialAd = null
                        }
                        
                        override fun onAdShowedFullScreenContent() {
                            Log.d(TAG, "Interstitial ad showed")
                        }
                    }
                }
                
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.w(TAG, "Interstitial ad failed to load: ${adError.message}")
                    isLoadingInterstitial = false
                }
            }
        )
    }
    
    fun showInterstitialAdIfAvailable(activity: Activity) {
        if (interstitialAd != null) {
            Log.d(TAG, "Showing interstitial ad")
            interstitialAd?.show(activity)
        } else {
            Log.d(TAG, "Interstitial ad not ready yet")
        }
    }
    
    fun hasInterstitialAdReady(): Boolean = interstitialAd != null
    
    private fun saveStats() {
        sharedPreferences.edit().apply {
            putInt(Constants.PREFS_TOTAL_GAMES, totalGamesPlayed)
            putInt(Constants.PREFS_GAMES_SINCE_AD, gamesSinceLastAd)
            apply()
        }
    }
    
    private fun loadSavedStats() {
        totalGamesPlayed = sharedPreferences.getInt(Constants.PREFS_TOTAL_GAMES, 0)
        gamesSinceLastAd = sharedPreferences.getInt(Constants.PREFS_GAMES_SINCE_AD, 0)
    }
    
    fun getTotalGamesPlayed(): Int = totalGamesPlayed
    
    fun getGamesSinceLastAd(): Int = gamesSinceLastAd
    
    fun release() {
        interstitialAd = null
    }
}
