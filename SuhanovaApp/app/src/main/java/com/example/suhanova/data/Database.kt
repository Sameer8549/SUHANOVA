package com.example.suhanova.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

// ─── ENTITIES ────────────────────────────────────────────────────────────────

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Suhana",
    val neetExamDate: Long = 0L,
    val weakSubjects: String = "",           // comma-separated
    val biometricEnrolled: Boolean = false,
    val pin: String = "",
    val currentStreak: Int = 0,
    val lastStudyDate: Long = 0L,
    val totalXP: Int = 0,
    val currentLevel: Int = 1,
    val onboardingComplete: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "quiz_sessions")
data class QuizSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val topic: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val wrongAnswers: Int,
    val timeTakenSeconds: Int,
    val xpEarned: Int,
    val completedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val role: String,   // "user" or "nova"
    val content: String,
    val subject: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
)

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val topic: String,
    val durationMinutes: Int,
    val pomodoroCount: Int = 0,
    val date: Long = System.currentTimeMillis(),
)

@Entity(tableName = "flashcard_reviews")
data class FlashcardReview(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cardId: String,
    val subject: String,
    val term: String,
    val rating: String,  // "easy" | "okay" | "hard"
    val nextReviewAt: Long,
    val reviewedAt: Long = System.currentTimeMillis(),
)

// ─── DAOs ─────────────────────────────────────────────────────────────────────

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getProfileOnce(): UserProfile?

    @Upsert
    suspend fun upsertProfile(profile: UserProfile)

    @Query("UPDATE user_profile SET currentStreak = :streak, lastStudyDate = :date WHERE id = 1")
    suspend fun updateStreak(streak: Int, date: Long)

    @Query("UPDATE user_profile SET totalXP = totalXP + :xp, currentLevel = :level WHERE id = 1")
    suspend fun addXP(xp: Int, level: Int)
}

@Dao
interface QuizSessionDao {
    @Insert
    suspend fun insert(session: QuizSession): Long

    @Query("SELECT * FROM quiz_sessions ORDER BY completedAt DESC LIMIT 20")
    fun getRecentSessions(): Flow<List<QuizSession>>

    @Query("SELECT AVG(CAST(correctAnswers AS FLOAT) / totalQuestions * 100) FROM quiz_sessions WHERE subject = :subject")
    fun getSubjectAccuracy(subject: String): Flow<Float?>

    @Query("SELECT * FROM quiz_sessions WHERE completedAt > :since ORDER BY completedAt ASC")
    fun getSessionsSince(since: Long): Flow<List<QuizSession>>
}

@Dao
interface ChatMessageDao {
    @Insert
    suspend fun insert(message: ChatMessage): Long

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Query("DELETE FROM chat_messages WHERE timestamp < :before")
    suspend fun deleteOldMessages(before: Long)
}

@Dao
interface StudySessionDao {
    @Insert
    suspend fun insert(session: StudySession): Long

    @Query("SELECT * FROM study_sessions ORDER BY date DESC LIMIT 30")
    fun getRecentSessions(): Flow<List<StudySession>>

    @Query("SELECT SUM(durationMinutes) FROM study_sessions WHERE date > :since")
    fun getTotalMinutesSince(since: Long): Flow<Int?>
}

@Dao
interface FlashcardReviewDao {
    @Upsert
    suspend fun upsert(review: FlashcardReview)

    @Query("SELECT * FROM flashcard_reviews WHERE nextReviewAt <= :now ORDER BY nextReviewAt ASC")
    fun getDueCards(now: Long): Flow<List<FlashcardReview>>

    @Query("SELECT COUNT(*) FROM flashcard_reviews WHERE date(reviewedAt/1000,'unixepoch') = date('now')")
    fun getTodayReviewCount(): Flow<Int>
}

// ─── DATABASE ─────────────────────────────────────────────────────────────────

@Database(
    entities = [
        UserProfile::class,
        QuizSession::class,
        ChatMessage::class,
        StudySession::class,
        FlashcardReview::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class SuhanovaDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun quizSessionDao(): QuizSessionDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun flashcardReviewDao(): FlashcardReviewDao

    companion object {
        @Volatile private var INSTANCE: SuhanovaDatabase? = null

        fun getDatabase(context: Context): SuhanovaDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    SuhanovaDatabase::class.java,
                    "suhanova_db"
                ).build().also { INSTANCE = it }
            }
    }
}
