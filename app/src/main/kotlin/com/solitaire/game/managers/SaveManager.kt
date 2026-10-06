package com.solitaire.game.managers

import android.content.Context
import com.solitaire.game.database.AppDatabase
import com.solitaire.game.database.WinHistoryEntity
import com.solitaire.game.database.DailyQuestEntity
import com.solitaire.game.database.CardCollectionEntity
import com.solitaire.game.database.AchievementEntity
import com.solitaire.game.models.WinHistory
import com.solitaire.game.models.DailyQuest
import com.solitaire.game.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class SaveManager(private val context: Context) {
    
    private val database = AppDatabase.getInstance(context)
    private val winHistoryDao = database.winHistoryDao()
    private val questDao = database.dailyQuestDao()
    private val collectionDao = database.cardCollectionDao()
    private val achievementDao = database.achievementDao()
    private val sharedPrefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
    
    // ============ WIN HISTORY ============
    
    suspend fun saveWinHistory(
        completionTimeMs: Long,
        difficulty: String,
        score: Int,
        moves: Int
    ) = withContext(Dispatchers.IO) {
        val entity = WinHistoryEntity(
            completionTime = completionTimeMs,
            dateTimestamp = System.currentTimeMillis(),
            difficulty = difficulty,
            score = score,
            moves = moves
        )
        winHistoryDao.insertWinHistory(entity)
    }
    
    suspend fun getAllWinHistory(): List<WinHistory> = withContext(Dispatchers.IO) {
        winHistoryDao.getAllWinHistory().map { entity ->
            WinHistory(
                id = entity.id,
                completionTime = entity.completionTime,
                dateTimestamp = entity.dateTimestamp,
                difficulty = entity.difficulty,
                score = entity.score,
                moves = entity.moves
            )
        }
    }
    
    suspend fun getWinHistoryByDifficulty(difficulty: String): List<WinHistory> =
        withContext(Dispatchers.IO) {
            winHistoryDao.getHistoryByDifficulty(difficulty).map { entity ->
                WinHistory(
                    id = entity.id,
                    completionTime = entity.completionTime,
                    dateTimestamp = entity.dateTimestamp,
                    difficulty = entity.difficulty,
                    score = entity.score,
                    moves = entity.moves
                )
            }
        }
    
    suspend fun getTotalGamesWon(): Int = withContext(Dispatchers.IO) {
        winHistoryDao.getTotalGamesWon()
    }
    
    suspend fun getWinCountByDifficulty(difficulty: String): Int = withContext(Dispatchers.IO) {
        winHistoryDao.getWinCountByDifficulty(difficulty)
    }
    
    suspend fun getBestTimeByDifficulty(difficulty: String): Long? =
        withContext(Dispatchers.IO) {
            winHistoryDao.getBestTimeByDifficulty(difficulty)
        }
    
    suspend fun getRecentWins(limit: Int = 10): List<WinHistory> = withContext(Dispatchers.IO) {
        val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        winHistoryDao.getRecentWins(sevenDaysAgo, limit).map { entity ->
            WinHistory(
                id = entity.id,
                completionTime = entity.completionTime,
                dateTimestamp = entity.dateTimestamp,
                difficulty = entity.difficulty,
                score = entity.score,
                moves = entity.moves
            )
        }
    }
    
    // ============ DAILY QUESTS ============
    
    suspend fun initializeDailyQuests() = withContext(Dispatchers.IO) {
        val today = getDateKey(System.currentTimeMillis())
        val lastQuestDate = sharedPrefs.getLong(Constants.PREFS_LAST_QUEST_DATE, 0L)
        
        if (lastQuestDate != today) {
            // Xóa quests cũ
            questDao.deleteOldQuests(today)
            
            // Tạo 3 quest mới
            val newQuests = generateDailyQuests(today)
            for (quest in newQuests) {
                questDao.insertQuest(quest)
            }
            
            sharedPrefs.edit().putLong(Constants.PREFS_LAST_QUEST_DATE, today).apply()
            
            // Increment login streak
            incrementLoginStreak()
        }
    }
    
    private fun generateDailyQuests(dateKey: Long): List<DailyQuestEntity> {
        val questTypes = listOf(
            Triple("WIN_5_EASY", "Win 5 games on Easy", 50),
            Triple("WIN_HARD_UNDER_180S", "Win 1 Hard in under 3 min", 100),
            Triple("TOTAL_MOVES_100", "Make 100 moves total", 75),
            Triple("WIN_EXTREME_1", "Conquer Extreme (1 game)", 150),
            Triple("STREAK_WINS_3", "Win 3 games in a row", 80),
            Triple("NO_UNDO_CHALLENGE", "Win without using Undo", 120)
        )
        
        return questTypes.shuffled().take(3).map { (type, desc, points) ->
            DailyQuestEntity(
                questType = type,
                description = desc,
                progress = 0,
                target = when (type) {
                    "WIN_5_EASY" -> 5
                    "STREAK_WINS_3" -> 3
                    "TOTAL_MOVES_100" -> 100
                    else -> 1
                },
                rewardPoints = points,
                isCompleted = false,
                dateCreated = dateKey,
                isClaimed = false
            )
        }
    }
    
    suspend fun updateQuestProgress(questType: String, value: Int = 1) {
        withContext(Dispatchers.IO) {
            val today = getDateKey(System.currentTimeMillis())
            val activeQuests = questDao.getActiveQuestsByDate(today)
            
            for (quest in activeQuests) {
                if (quest.questType == questType) {
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
    
    suspend fun claimQuestReward(questId: Int) = withContext(Dispatchers.IO) {
        val today = getDateKey(System.currentTimeMillis())
        val quests = questDao.getQuestsByDate(today)
        
        quests.find { it.id == questId }?.let { quest ->
            if (quest.isCompleted && !quest.isClaimed) {
                questDao.updateQuest(quest.copy(isClaimed = true))
                addEventPoints(quest.rewardPoints)
                return@withContext true
            }
        }
        return@withContext false
    }
    
    suspend fun getTodayQuests(): List<DailyQuestEntity> = withContext(Dispatchers.IO) {
        val today = getDateKey(System.currentTimeMillis())
        questDao.getQuestsByDate(today)
    }
    
    suspend fun getClaimableQuests(): List<DailyQuestEntity> = withContext(Dispatchers.IO) {
        val today = getDateKey(System.currentTimeMillis())
        questDao.getClaimableQuestsByDate(today)
    }
    
    // ============ CARD COLLECTION ============
    
    suspend fun initializeCardSets() = withContext(Dispatchers.IO) {
        val sets = listOf(
            CardCollectionEntity("classic", 0, true, null),
            CardCollectionEntity("gold", 0, false, null),
            CardCollectionEntity("neon", 0, false, null),
            CardCollectionEntity("nature", 0, false, null),
            CardCollectionEntity("vintage", 0, false, null)
        )
        for (set in sets) {
            collectionDao.insertCardSet(set)
        }
    }
    
    suspend fun addCollectionPiece(cardSetId: String, pieces: Int = 1) =
        withContext(Dispatchers.IO) {
            val collection = collectionDao.getCardSet(cardSetId)
            
            if (collection != null) {
                val newPieces = collection.piecesCollected + pieces
                val isNowUnlocked = newPieces >= Constants.PIECES_TO_UNLOCK
                
                collectionDao.updateCollection(
                    collection.copy(
                        piecesCollected = newPieces,
                        isUnlocked = isNowUnlocked || collection.isUnlocked,
                        unlockDate = if (isNowUnlocked && !collection.isUnlocked)
                            System.currentTimeMillis() else collection.unlockDate
                    )
                )
                return@withContext isNowUnlocked && !collection.isUnlocked
            }
            return@withContext false
        }
    
    suspend fun getUnlockedCardSets(): List<String> = withContext(Dispatchers.IO) {
        collectionDao.getUnlockedCardSets().map { it.cardSetId }
    }
    
    suspend fun getAllCardCollections() = withContext(Dispatchers.IO) {
        collectionDao.getAllCollections()
    }
    
    // ============ LOGIN STREAK & POINTS ============
    
    fun incrementLoginStreak() {
        val lastLoginDate = sharedPrefs.getLong(Constants.PREFS_LAST_LOGIN_DATE, 0L)
        val today = getDateKey(System.currentTimeMillis())
        
        val streak = if (isConsecutiveDay(lastLoginDate)) {
            sharedPrefs.getInt(Constants.PREFS_LOGIN_STREAK, 1) + 1
        } else {
            1
        }
        
        sharedPrefs.edit().apply {
            putInt(Constants.PREFS_LOGIN_STREAK, streak)
            putLong(Constants.PREFS_LAST_LOGIN_DATE, today)
            apply()
        }
        
        // Bonus: tặng điểm cho login liên tiếp
        if (streak > 1) {
            addEventPoints(10 * (streak - 1))
        }
    }
    
    fun getLoginStreak(): Int = sharedPrefs.getInt(Constants.PREFS_LOGIN_STREAK, 0)
    
    fun addEventPoints(points: Int) {
        val current = sharedPrefs.getInt(Constants.PREFS_EVENT_POINTS, 0)
        sharedPrefs.edit().putInt(Constants.PREFS_EVENT_POINTS, current + points).apply()
    }
    
    fun getEventPoints(): Int = sharedPrefs.getInt(Constants.PREFS_EVENT_POINTS, 0)
    
    fun spendEventPoints(points: Int): Boolean {
        val current = getEventPoints()
        if (current >= points) {
            sharedPrefs.edit().putInt(Constants.PREFS_EVENT_POINTS, current - points).apply()
            return true
        }
        return false
    }
    
    // ============ ACHIEVEMENTS ============
    
    suspend fun initializeAchievements() = withContext(Dispatchers.IO) {
        val achievements = listOf(
            AchievementEntity(
                achievementId = "FIRST_WIN",
                title = "First Victory",
                description = "Win your first game",
                isUnlocked = false,
                unlockDate = null,
                rewardPoints = 50
            ),
            AchievementEntity(
                achievementId = "WINSTREAK_5",
                title = "On Fire!",
                description = "Win 5 games in a row",
                isUnlocked = false,
                unlockDate = null,
                rewardPoints = 100
            ),
            AchievementEntity(
                achievementId = "SPEED_RUNNER",
                title = "Speed Runner",
                description = "Complete a game under 2 minutes",
                isUnlocked = false,
                unlockDate = null,
                rewardPoints = 75
            ),
            AchievementEntity(
                achievementId = "LEGEND",
                title = "True Legend",
                description = "Win 50 games on Legendary difficulty",
                isUnlocked = false,
                unlockDate = null,
                rewardPoints = 500
            )
        )
        for (achievement in achievements) {
            achievementDao.insertAchievement(achievement)
        }
    }
    
    suspend fun unlockAchievement(achievementId: String) = withContext(Dispatchers.IO) {
        val achievements = achievementDao.getAllAchievements()
        val achievement = achievements.find { it.achievementId == achievementId }
        
        if (achievement != null && !achievement.isUnlocked) {
            achievementDao.updateAchievement(
                achievement.copy(
                    isUnlocked = true,
                    unlockDate = System.currentTimeMillis()
                )
            )
            addEventPoints(achievement.rewardPoints)
            return@withContext true
        }
        return@withContext false
    }
    
    suspend fun getUnlockedAchievements() = withContext(Dispatchers.IO) {
        achievementDao.getUnlockedAchievements()
    }
    
    suspend fun getUnlockedAchievementCount(): Int = withContext(Dispatchers.IO) {
        achievementDao.getUnlockedCount()
    }
    
    // ============ HELPERS ============
    
    private fun isConsecutiveDay(lastDate: Long): Boolean {
        if (lastDate == 0L) return false
        val today = getDateKey(System.currentTimeMillis())
        val yesterday = getDateKey(lastDate)
        return today - yesterday == 1L
    }
    
    private fun getDateKey(timeMs: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeMs
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        return calendar.timeInMillis / 86400000 // Chia cho số ms trong 1 ngày
    }
}
