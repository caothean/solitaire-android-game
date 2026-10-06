package com.solitaire.game.database

import android.content.Context
import androidx.room.*
import android.database.sqlite.SQLiteDatabase

// ============ ENTITIES ============

@Entity(tableName = "win_history")
data class WinHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "completion_time") val completionTime: Long,        // milliseconds
    @ColumnInfo(name = "date_timestamp") val dateTimestamp: Long,         // System.currentTimeMillis()
    @ColumnInfo(name = "difficulty") val difficulty: String,              // EASY, NORMAL, HARD, etc
    @ColumnInfo(name = "score") val score: Int,
    @ColumnInfo(name = "moves") val moves: Int
)

@Entity(tableName = "daily_quests")
data class DailyQuestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "quest_type") val questType: String,               // WIN_5_EASY, WIN_HARD_UNDER_180S, etc
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "progress") val progress: Int,
    @ColumnInfo(name = "target") val target: Int,
    @ColumnInfo(name = "reward_points") val rewardPoints: Int,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean,
    @ColumnInfo(name = "date_created") val dateCreated: Long,
    @ColumnInfo(name = "is_claimed") val isClaimed: Boolean
)

@Entity(tableName = "card_collection")
data class CardCollectionEntity(
    @PrimaryKey val cardSetId: String,                                   // "classic", "gold", "neon", etc
    @ColumnInfo(name = "pieces_collected") val piecesCollected: Int,
    @ColumnInfo(name = "is_unlocked") val isUnlocked: Boolean,
    @ColumnInfo(name = "unlock_date") val unlockDate: Long?              // When unlocked
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val achievementId: String,                                // FIRST_WIN, WINSTREAK_5, etc
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "is_unlocked") val isUnlocked: Boolean,
    @ColumnInfo(name = "unlock_date") val unlockDate: Long?,
    @ColumnInfo(name = "reward_points") val rewardPoints: Int
)

// ============ DAOs ============

@Dao
interface WinHistoryDao {
    @Insert
    suspend fun insertWinHistory(history: WinHistoryEntity)
    
    @Query("SELECT * FROM win_history ORDER BY completion_time ASC")
    suspend fun getAllWinHistory(): List<WinHistoryEntity>
    
    @Query("SELECT * FROM win_history WHERE difficulty = :difficulty ORDER BY completion_time ASC")
    suspend fun getHistoryByDifficulty(difficulty: String): List<WinHistoryEntity>
    
    @Query("SELECT COUNT(*) FROM win_history")
    suspend fun getTotalGamesWon(): Int
    
    @Query("SELECT COUNT(*) FROM win_history WHERE difficulty = :difficulty")
    suspend fun getWinCountByDifficulty(difficulty: String): Int
    
    @Query("SELECT MIN(completion_time) FROM win_history WHERE difficulty = :difficulty")
    suspend fun getBestTimeByDifficulty(difficulty: String): Long?
    
    @Query("SELECT AVG(completion_time) FROM win_history WHERE difficulty = :difficulty")
    suspend fun getAverageTimeByDifficulty(difficulty: String): Long?
    
    @Query("SELECT * FROM win_history WHERE date_timestamp >= :startDate ORDER BY date_timestamp DESC LIMIT :limit")
    suspend fun getRecentWins(startDate: Long, limit: Int = 10): List<WinHistoryEntity>
    
    @Delete
    suspend fun deleteWinHistory(history: WinHistoryEntity)
}

@Dao
interface DailyQuestDao {
    @Insert
    suspend fun insertQuest(quest: DailyQuestEntity)
    
    @Update
    suspend fun updateQuest(quest: DailyQuestEntity)
    
    @Query("SELECT * FROM daily_quests WHERE date_created = :date ORDER BY is_completed ASC")
    suspend fun getQuestsByDate(date: Long): List<DailyQuestEntity>
    
    @Query("SELECT * FROM daily_quests WHERE date_created = :date AND is_completed = 0 AND is_claimed = 0")
    suspend fun getActiveQuestsByDate(date: Long): List<DailyQuestEntity>
    
    @Query("SELECT * FROM daily_quests WHERE is_completed = 1 AND is_claimed = 0 AND date_created = :date")
    suspend fun getClaimableQuestsByDate(date: Long): List<DailyQuestEntity>
    
    @Query("SELECT SUM(reward_points) FROM daily_quests WHERE is_claimed = 1 AND date_created = :date")
    suspend fun getTotalClaimedPointsForDate(date: Long): Int
    
    @Delete
    suspend fun deleteQuest(quest: DailyQuestEntity)
    
    @Query("DELETE FROM daily_quests WHERE date_created < :cutoffDate")
    suspend fun deleteOldQuests(cutoffDate: Long)
}

@Dao
interface CardCollectionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCardSet(collection: CardCollectionEntity)
    
    @Update
    suspend fun updateCollection(collection: CardCollectionEntity)
    
    @Query("SELECT * FROM card_collection ORDER BY is_unlocked DESC")
    suspend fun getAllCollections(): List<CardCollectionEntity>
    
    @Query("SELECT * FROM card_collection WHERE is_unlocked = 1")
    suspend fun getUnlockedCardSets(): List<CardCollectionEntity>
    
    @Query("SELECT * FROM card_collection WHERE cardSetId = :setId")
    suspend fun getCardSet(setId: String): CardCollectionEntity?
    
    @Query("SELECT SUM(pieces_collected) FROM card_collection WHERE is_unlocked = 0")
    suspend fun getTotalUnlockedPieces(): Int
}

@Dao
interface AchievementDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAchievement(achievement: AchievementEntity)
    
    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)
    
    @Query("SELECT * FROM achievements ORDER BY is_unlocked DESC")
    suspend fun getAllAchievements(): List<AchievementEntity>
    
    @Query("SELECT * FROM achievements WHERE is_unlocked = 1 ORDER BY unlock_date DESC")
    suspend fun getUnlockedAchievements(): List<AchievementEntity>
    
    @Query("SELECT COUNT(*) FROM achievements WHERE is_unlocked = 1")
    suspend fun getUnlockedCount(): Int
}

// ============ DATABASE ============

@Database(
    entities = [
        WinHistoryEntity::class,
        DailyQuestEntity::class,
        CardCollectionEntity::class,
        AchievementEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun winHistoryDao(): WinHistoryDao
    abstract fun dailyQuestDao(): DailyQuestDao
    abstract fun cardCollectionDao(): CardCollectionDao
    abstract fun achievementDao(): AchievementDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "solitaire_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SQLiteDatabase) {
                // Thêm bảng card_collection
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS card_collection(
                        cardSetId TEXT PRIMARY KEY,
                        pieces_collected INTEGER,
                        is_unlocked INTEGER,
                        unlock_date INTEGER
                    )
                """)
                
                // Thêm bảng achievements
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS achievements(
                        achievementId TEXT PRIMARY KEY,
                        title TEXT,
                        description TEXT,
                        is_unlocked INTEGER,
                        unlock_date INTEGER,
                        reward_points INTEGER
                    )
                """)
            }
        }
    }
}
