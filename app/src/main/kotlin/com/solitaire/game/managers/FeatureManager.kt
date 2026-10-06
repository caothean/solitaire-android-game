package com.solitaire.game.managers

import android.content.Context
import com.solitaire.game.database.AppDatabase
import com.solitaire.game.database.DailyQuestEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class FeatureManager(private val context: Context) {
    
    private val database = AppDatabase.getInstance(context)
    private val questDao = database.dailyQuestDao()
    private val collectionDao = database.cardCollectionDao()
    private val saveManager = SaveManager(context)
    private val sharedPrefs = context.getSharedPreferences("features_prefs", Context.MODE_PRIVATE)
    
    // ============ DAILY QUESTS ============
    
    suspend fun updateQuestProgress(questType: String, value: Int = 1) {
        withContext(Dispatchers.IO) {
            val today = getDateKey(System.currentTimeMillis())
            val activeQuests = questDao.getActiveQuestsByDate(today)
            
            for (quest in activeQuests) {
                if (quest.questType.contains(questType)) {
                    val newProgress = (quest.progress + value).coerceAtMost(quest.target)
                    val isCompleted = newProgress >= quest.target
                    
                    questDao.updateQuest(
                        quest.copy(
                            progress = newProgress,
                            isCompleted = isCompleted
                        )
                    )
                }
            }
        }
    }
    
    // ============ REWARD WHEEL ============
    
    fun canSpinWheel(): Boolean {
        val lastSpinDate = sharedPrefs.getLong("last_spin_date", 0L)
        val today = getDateKey(System.currentTimeMillis())
        return lastSpinDate != today
    }
    
    fun spinRewardWheel(): RewardWheelResult {
        val rewards = listOf(
            RewardWheelResult("POINTS_50", 50),
            RewardWheelResult("POINTS_100", 100),
            RewardWheelResult("COLLECTION_PIECE_1", 1),
            RewardWheelResult("COLLECTION_PIECE_2", 2),
            RewardWheelResult("BOOSTER_HINT", 3),
            RewardWheelResult("BOOSTER_UNDO", 3),
            RewardWheelResult("POINTS_200", 200)
        )
        
        val reward = rewards.random()
        
        sharedPrefs.edit()
            .putLong("last_spin_date", getDateKey(System.currentTimeMillis()))
            .apply()
        
        return reward
    }
    
    data class RewardWheelResult(
        val rewardType: String,
        val value: Int
    )
    
    // ============ ACHIEVEMENT SYSTEM ============
    
    suspend fun unlockAchievement(achievementId: String) = withContext(Dispatchers.IO) {
        saveManager.unlockAchievement(achievementId)
    }
    
    private fun getDateKey(timeMs: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeMs
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        return calendar.timeInMillis / 86400000
    }
}
