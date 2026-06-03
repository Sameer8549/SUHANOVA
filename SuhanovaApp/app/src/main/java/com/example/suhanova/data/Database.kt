package com.example.suhanova.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
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

@Entity(
    tableName = "education_catalog",
    indices = [Index(value = ["board", "studentClass", "subject", "chapter"], unique = true)]
)
data class EducationCatalogItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val board: String,
    val studentClass: String,
    val subject: String,
    val chapter: String,
    val category: String = "Core",
    val examTags: String = "",
    val resourceQuery: String = "",
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

@Dao
interface EducationCatalogDao {
    @Query("SELECT * FROM education_catalog ORDER BY board, studentClass, subject, chapter")
    fun getAll(): Flow<List<EducationCatalogItem>>

    @Query("SELECT DISTINCT subject FROM education_catalog WHERE (:board = '' OR board = :board) AND (:studentClass = '' OR studentClass = :studentClass) ORDER BY subject")
    fun getSubjects(board: String, studentClass: String): Flow<List<String>>

    @Query("SELECT chapter FROM education_catalog WHERE (:board = '' OR board = :board) AND (:studentClass = '' OR studentClass = :studentClass) AND subject = :subject ORDER BY chapter")
    fun getChapters(board: String, studentClass: String, subject: String): Flow<List<String>>

    @Query("SELECT * FROM education_catalog WHERE (:board = '' OR board = :board) AND (:studentClass = '' OR studentClass = :studentClass) AND (:subject = '' OR subject = :subject) ORDER BY subject, chapter")
    suspend fun findCatalog(board: String, studentClass: String, subject: String): List<EducationCatalogItem>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<EducationCatalogItem>)

    @Query("SELECT COUNT(*) FROM education_catalog")
    suspend fun count(): Int
}

// ─── DATABASE ─────────────────────────────────────────────────────────────────

@Database(
    entities = [
        UserProfile::class,
        QuizSession::class,
        ChatMessage::class,
        StudySession::class,
        FlashcardReview::class,
        EducationCatalogItem::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class SuhanovaDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun quizSessionDao(): QuizSessionDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun flashcardReviewDao(): FlashcardReviewDao
    abstract fun educationCatalogDao(): EducationCatalogDao

    companion object {
        @Volatile private var INSTANCE: SuhanovaDatabase? = null

        fun getDatabase(context: Context): SuhanovaDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    SuhanovaDatabase::class.java,
                    "suhanova_db"
                )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            super.onCreate(db)
                            seedEducationCatalog(context.applicationContext)
                        }

                        override fun onOpen(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            super.onOpen(db)
                            seedEducationCatalog(context.applicationContext)
                        }
                    })
                    .build().also { INSTANCE = it }
            }
    }
}

private fun seedEducationCatalog(context: Context) {
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
        val dao = SuhanovaDatabase.getDatabase(context).educationCatalogDao()
        if (dao.count() > 0) return@launch
        dao.insertAll(defaultEducationCatalog())
    }
}

fun defaultEducationCatalog(): List<EducationCatalogItem> {
    val rows = mutableListOf<EducationCatalogItem>()

    fun add(board: String, cls: String, subject: String, chapters: List<String>, tags: String = "") {
        chapters.forEach { chapter ->
            rows += EducationCatalogItem(
                board = board,
                studentClass = cls,
                subject = subject,
                chapter = chapter,
                examTags = tags,
                resourceQuery = "$board Class $cls $subject $chapter notes questions",
            )
        }
    }

    val boards = listOf("CBSE", "ICSE")
    boards.forEach { board ->
        add(board, "10th", "Science", listOf(
            "Chemical Reactions and Equations", "Acids Bases and Salts", "Metals and Non-metals",
            "Carbon and Its Compounds", "Life Processes", "Control and Coordination",
            "How do Organisms Reproduce", "Heredity", "Light", "Human Eye",
            "Electricity", "Magnetic Effects of Electric Current", "Our Environment"
        ), "Board Exam")
        add(board, "10th", "Maths", listOf(
            "Real Numbers", "Polynomials", "Pair of Linear Equations", "Quadratic Equations",
            "Arithmetic Progressions", "Triangles", "Coordinate Geometry", "Trigonometry",
            "Circles", "Areas Related to Circles", "Surface Areas and Volumes", "Statistics", "Probability"
        ), "Board Exam")
        add(board, "10th", "Social Science", listOf(
            "Nationalism in Europe", "Nationalism in India", "Resources and Development",
            "Agriculture", "Minerals and Energy Resources", "Power Sharing", "Federalism",
            "Money and Credit", "Development", "Globalisation"
        ), "Board Exam")
        add(board, "10th", "English", listOf(
            "Reading Comprehension", "Writing Skills", "Grammar", "First Flight", "Footprints Without Feet"
        ), "Board Exam")

        add(board, "11th", "Physics", listOf(
            "Units and Measurements", "Motion in a Straight Line", "Motion in a Plane",
            "Laws of Motion", "Work Energy and Power", "System of Particles",
            "Gravitation", "Mechanical Properties of Solids", "Thermal Properties of Matter",
            "Thermodynamics", "Kinetic Theory", "Oscillations", "Waves"
        ), "School Exam NEET JEE")
        add(board, "11th", "Chemistry", listOf(
            "Some Basic Concepts of Chemistry", "Structure of Atom", "Classification of Elements",
            "Chemical Bonding", "States of Matter", "Thermodynamics", "Equilibrium",
            "Redox Reactions", "Hydrogen", "Organic Chemistry Basics", "Hydrocarbons"
        ), "School Exam NEET JEE")
        add(board, "11th", "Biology", listOf(
            "The Living World", "Biological Classification", "Plant Kingdom", "Animal Kingdom",
            "Morphology of Flowering Plants", "Anatomy of Flowering Plants", "Cell",
            "Biomolecules", "Cell Cycle and Division", "Plant Physiology", "Human Physiology"
        ), "School Exam NEET")
        add(board, "11th", "Maths", listOf(
            "Sets", "Relations and Functions", "Trigonometric Functions", "Complex Numbers",
            "Linear Inequalities", "Permutations and Combinations", "Binomial Theorem",
            "Sequences and Series", "Straight Lines", "Conic Sections", "Limits and Derivatives",
            "Statistics", "Probability"
        ), "School Exam JEE")

        add(board, "12th", "Physics", listOf(
            "Electric Charges and Fields", "Electrostatic Potential", "Current Electricity",
            "Moving Charges and Magnetism", "Magnetism and Matter", "Electromagnetic Induction",
            "Alternating Current", "Electromagnetic Waves", "Ray Optics", "Wave Optics",
            "Dual Nature of Radiation", "Atoms", "Nuclei", "Semiconductor Electronics"
        ), "Board Exam NEET JEE")
        add(board, "12th", "Chemistry", listOf(
            "Solutions", "Electrochemistry", "Chemical Kinetics", "d and f Block Elements",
            "Coordination Compounds", "Haloalkanes and Haloarenes", "Alcohols Phenols and Ethers",
            "Aldehydes Ketones and Carboxylic Acids", "Amines", "Biomolecules"
        ), "Board Exam NEET JEE")
        add(board, "12th", "Biology", listOf(
            "Reproduction in Organisms", "Human Reproduction", "Reproductive Health",
            "Principles of Inheritance", "Molecular Basis of Inheritance", "Evolution",
            "Human Health and Disease", "Microbes in Human Welfare", "Biotechnology",
            "Organisms and Populations", "Ecosystem", "Biodiversity"
        ), "Board Exam NEET")
        add(board, "12th", "Maths", listOf(
            "Relations and Functions", "Inverse Trigonometric Functions", "Matrices",
            "Determinants", "Continuity and Differentiability", "Applications of Derivatives",
            "Integrals", "Applications of Integrals", "Differential Equations", "Vector Algebra",
            "Three Dimensional Geometry", "Linear Programming", "Probability"
        ), "Board Exam JEE")
        add(board, "12th", "English", listOf(
            "Reading Comprehension", "Writing Skills", "Flamingo Prose", "Flamingo Poetry", "Vistas"
        ), "Board Exam")
    }

    return rows
}
