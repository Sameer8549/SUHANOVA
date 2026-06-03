package com.example.suhanova.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.example.suhanova.MainActivity
import com.example.suhanova.data.SuhanovaDatabase
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit

// ─── NOTIFICATION CHANNELS ────────────────────────────────────────────────────

const val CHANNEL_NOVA    = "suhanova_nova"
const val CHANNEL_STREAK  = "suhanova_streak"
const val CHANNEL_QUIZ    = "suhanova_quiz"
const val CHANNEL_MIDNIGHT = "suhanova_midnight"

fun createNotificationChannels(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        listOf(
            NotificationChannel(CHANNEL_NOVA, "Nova Daily Message", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Daily motivational messages from Nova for Suhana"
                enableLights(true)
                lightColor = android.graphics.Color.parseColor("#FFD700")
            },
            NotificationChannel(CHANNEL_STREAK, "Streak Reminders", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Don't break your study streak, Suhana!"
                enableLights(true)
                lightColor = android.graphics.Color.parseColor("#FF6EB4")
            },
            NotificationChannel(CHANNEL_QUIZ, "Daily Quiz", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Daily practice questions for the selected exam"
            },
            NotificationChannel(CHANNEL_MIDNIGHT, "Midnight Check-in", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Late-night study companion"
            },
        ).forEach { manager.createNotificationChannel(it) }
    }
}

// ─── NOTIFICATION SENDER ──────────────────────────────────────────────────────

fun sendNovaNotification(
    context: Context,
    channelId: String,
    notifId: Int,
    title: String,
    message: String,
    emoji: String = "✨",
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
    }

    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

    val notif = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.star_on)  // Replace with custom star icon
        .setContentTitle("$emoji  $title")
        .setContentText(message)
        .setStyle(NotificationCompat.BigTextStyle().bigText(message))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .setColor(android.graphics.Color.parseColor("#FFD700"))
        .build()

    NotificationManagerCompat.from(context).notify(notifId, notif)
}

data class NotificationContext(
    val goal: String,
    val board: String,
    val studentClass: String,
    val targetExam: String,
    val weakAreas: String,
    val streak: Int,
    val totalXP: Int,
    val latestQuizSubject: String?,
    val latestQuizTopic: String?,
    val latestQuizAccuracy: Int?,
    val studiedTodayMinutes: Int,
)

private suspend fun loadNotificationContext(context: Context): NotificationContext {
    val appContext = context.applicationContext
    val prefs = appContext.getSharedPreferences("suhanova_first_run", Context.MODE_PRIVATE)
    val db = SuhanovaDatabase.getDatabase(appContext)
    val profile = db.userProfileDao().getProfileOnce()
    val latestQuiz = db.quizSessionDao().getRecentSessions().first().firstOrNull()

    val startOfToday = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    val studiedToday = db.studySessionDao().getTotalMinutesSince(startOfToday).first() ?: 0

    val latestAccuracy = latestQuiz?.let {
        if (it.totalQuestions > 0) it.correctAnswers * 100 / it.totalQuestions else null
    }

    return NotificationContext(
        goal = prefs.getString("goal", "").orEmpty(),
        board = prefs.getString("board", "").orEmpty(),
        studentClass = prefs.getString("student_class", "").orEmpty(),
        targetExam = prefs.getString("target_exam", "").orEmpty(),
        weakAreas = prefs.getString("weak_areas", "").orEmpty(),
        streak = profile?.currentStreak ?: 0,
        totalXP = profile?.totalXP ?: 0,
        latestQuizSubject = latestQuiz?.subject,
        latestQuizTopic = latestQuiz?.topic,
        latestQuizAccuracy = latestAccuracy,
        studiedTodayMinutes = studiedToday,
    )
}

private fun morningMessage(ctx: NotificationContext): String = when {
    ctx.weakAreas.isNotBlank() ->
        "Good morning. Start with ${ctx.weakAreas}; Nova can generate a ${ctx.studentClass} ${ctx.board} plan or quiz from it."
    ctx.goal.isNotBlank() ->
        "Good morning. Your goal is ${ctx.goal} for ${ctx.targetExam}. Open Nova and choose today's exact topic."
    else ->
        "Good morning. Tell Nova your goal and weak area so today's plan is based on you."
}

class MorningNovaWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        createNotificationChannels(applicationContext)
        val context = loadNotificationContext(applicationContext)
        sendNovaNotification(
            context   = applicationContext,
            channelId = CHANNEL_NOVA,
            notifId   = 1001,
            title     = "Morning study check-in",
            message   = morningMessage(context),
            emoji     = "🩺",
        )
        return Result.success()
    }
}

private fun quizMessage(ctx: NotificationContext): String = when {
    ctx.latestQuizTopic != null && (ctx.latestQuizAccuracy ?: 100) < 70 ->
        "Your last ${ctx.latestQuizSubject} quiz on ${ctx.latestQuizTopic} was ${ctx.latestQuizAccuracy}%. Regenerate a live practice quiz for that topic."
    ctx.weakAreas.isNotBlank() ->
        "Quiz time. Use your weak area (${ctx.weakAreas}) and Nova will generate fresh ${ctx.board} ${ctx.studentClass} questions now."
    ctx.latestQuizTopic != null ->
        "Build on your last topic: ${ctx.latestQuizTopic}. Generate a fresh live quiz when you open the app."
    else ->
        "Ready for your first real quiz? Pick a subject, type a topic, and Nova will generate it live."
}

class EveningQuizWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        createNotificationChannels(applicationContext)
        val context = loadNotificationContext(applicationContext)
        sendNovaNotification(
            context   = applicationContext,
            channelId = CHANNEL_QUIZ,
            notifId   = 1002,
            title     = "Live quiz reminder",
            message   = quizMessage(context),
            emoji     = "📝",
        )
        return Result.success()
    }
}

private fun streakMessage(ctx: NotificationContext): String = when {
    ctx.studiedTodayMinutes > 0 ->
        "You studied ${ctx.studiedTodayMinutes} minutes today. Add one quick live quiz if you want to finish strong."
    ctx.streak > 0 ->
        "Your ${ctx.streak}-day streak needs one real session today. Open Study and generate a focused plan."
    ctx.weakAreas.isNotBlank() ->
        "No session logged today. Spend 10 minutes on ${ctx.weakAreas} and let Nova guide it."
    else ->
        "No session logged today. Open Nova, answer one question, and start building real progress."
}

class StreakGuardWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        createNotificationChannels(applicationContext)
        val context = loadNotificationContext(applicationContext)
        sendNovaNotification(
            context   = applicationContext,
            channelId = CHANNEL_STREAK,
            notifId   = 1003,
            title     = "Study status check",
            message   = streakMessage(context),
            emoji     = "🔥",
        )
        return Result.success()
    }
}

private fun midnightMessage(ctx: NotificationContext): String = when {
    ctx.studiedTodayMinutes > 0 ->
        "You already logged ${ctx.studiedTodayMinutes} minutes today. Rest now so tomorrow's study is sharper."
    ctx.goal.isNotBlank() ->
        "It's late. Your goal (${ctx.goal}) needs consistency, and sleep is part of that plan."
    else ->
        "It's late. Rest now, then let Nova build tomorrow's plan from your real answers."
}

class MidnightWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        createNotificationChannels(applicationContext)
        val context = loadNotificationContext(applicationContext)
        sendNovaNotification(
            context   = applicationContext,
            channelId = CHANNEL_MIDNIGHT,
            notifId   = 1004,
            title     = "Late-night check-in",
            message   = midnightMessage(context),
            emoji     = "🌙",
        )
        return Result.success()
    }
}

// ─── SCHEDULE ALL NOTIFICATIONS ───────────────────────────────────────────────

fun scheduleAllNotifications(context: Context) {
    val workManager = WorkManager.getInstance(context)

    // Morning message — 7 AM daily
    val morningDelay = getDelayUntil(7, 0)
    workManager.enqueueUniquePeriodicWork(
        "morning_nova",
        ExistingPeriodicWorkPolicy.UPDATE,
        PeriodicWorkRequestBuilder<MorningNovaWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(morningDelay, TimeUnit.MILLISECONDS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
            .build()
    )

    // Evening quiz — 7 PM daily
    val eveningDelay = getDelayUntil(19, 0)
    workManager.enqueueUniquePeriodicWork(
        "evening_quiz",
        ExistingPeriodicWorkPolicy.UPDATE,
        PeriodicWorkRequestBuilder<EveningQuizWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(eveningDelay, TimeUnit.MILLISECONDS)
            .build()
    )

    // Streak guard — 9 PM daily
    val streakDelay = getDelayUntil(21, 0)
    workManager.enqueueUniquePeriodicWork(
        "streak_guard",
        ExistingPeriodicWorkPolicy.UPDATE,
        PeriodicWorkRequestBuilder<StreakGuardWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(streakDelay, TimeUnit.MILLISECONDS)
            .build()
    )

    // Midnight check-in — 12 AM daily
    val midnightDelay = getDelayUntil(0, 0)
    workManager.enqueueUniquePeriodicWork(
        "midnight_checkin",
        ExistingPeriodicWorkPolicy.UPDATE,
        PeriodicWorkRequestBuilder<MidnightWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(midnightDelay, TimeUnit.MILLISECONDS)
            .build()
    )
}

fun getDelayUntil(hour: Int, minute: Int): Long {
    val now    = Calendar.getInstance()
    val target = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE,      minute)
        set(Calendar.SECOND,      0)
        set(Calendar.MILLISECOND, 0)
        if (before(now)) add(Calendar.DAY_OF_MONTH, 1) // If time has passed today, schedule for tomorrow
    }
    return target.timeInMillis - now.timeInMillis
}
