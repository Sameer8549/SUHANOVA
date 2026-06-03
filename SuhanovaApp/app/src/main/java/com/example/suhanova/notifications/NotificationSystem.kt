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
                description = "Today's NEET practice questions are ready"
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

// ─── MORNING NOVA MESSAGE (7 AM daily) ───────────────────────────────────────

val MORNING_MESSAGES = listOf(
    "Good morning, Dr. Suhana! ☀️ Today's goal: Cell Division + 10 Optics MCQs. Nova is waiting for you!",
    "Rise and shine, future Doctor! 🌟 NEET is coming — every morning you show up, you're already ahead.",
    "Hey Suhana! ☀️ Your weak area today: Thermodynamics. Let's crack it together. Open Nova!",
    "Good morning! 💪 Yesterday you scored 82%. Let's hit 85% today. You've got this!",
    "Morning, Dr. Suhana! 🩺 3 new flashcards are due today. 10 minutes is all it takes.",
    "Wake up, Suhana! 🌸 NEET toppers aren't born — they're made at 7 AM. That's you. Right now.",
)

class MorningNovaWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        createNotificationChannels(applicationContext)
        sendNovaNotification(
            context   = applicationContext,
            channelId = CHANNEL_NOVA,
            notifId   = 1001,
            title     = "Good morning, Dr. Suhana! ☀️",
            message   = MORNING_MESSAGES.random(),
            emoji     = "🩺",
        )
        return Result.success()
    }
}

// ─── EVENING QUIZ REMINDER (7 PM daily) ──────────────────────────────────────

val QUIZ_REMINDERS = listOf(
    "Suhana! 📝 Your personalized weak-topic quiz is ready. 5 questions on Membrane Transport — your toughest topic.",
    "Hey! 🎯 Today's quiz: Genetics (your weak area). Nova made these just for you. 5 minutes, 5 questions.",
    "Quiz time, Suhana! ⚡ 5 Physics MCQs on Optics — the topic from your last weak area report. Let's go!",
    "Daily quiz is live! 🧬 Today: Organic Chemistry nomenclature. These questions will definitely appear in NEET.",
)

class EveningQuizWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        createNotificationChannels(applicationContext)
        sendNovaNotification(
            context   = applicationContext,
            channelId = CHANNEL_QUIZ,
            notifId   = 1002,
            title     = "Your daily quiz is ready! ✨",
            message   = QUIZ_REMINDERS.random(),
            emoji     = "📝",
        )
        return Result.success()
    }
}

// ─── STREAK GUARD (9 PM — if no session today) ───────────────────────────────

val STREAK_MESSAGES = listOf(
    "Suhana! 🔥 Your 12-day streak is at risk! Study even 10 minutes to keep it alive. Nova misses you!",
    "Hey future Doctor! 🌟 Don't break your streak — 15 minutes of Biology right now = streak saved!",
    "Suhana! ⚠️ You haven't studied today yet. Your NEET date isn't moving. But you can still make today count!",
    "Missing you! 🩺 Your study streak is waiting. Open Suhanova and let's knock out one topic together.",
)

class StreakGuardWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        createNotificationChannels(applicationContext)
        sendNovaNotification(
            context   = applicationContext,
            channelId = CHANNEL_STREAK,
            notifId   = 1003,
            title     = "Your streak needs you! 🔥",
            message   = STREAK_MESSAGES.random(),
            emoji     = "🔥",
        )
        return Result.success()
    }
}

// ─── MIDNIGHT CHECK-IN (12 AM — Easter egg) ──────────────────────────────────

val MIDNIGHT_MESSAGES = listOf(
    "Midnight again, future Doctor? 🌙 Your dedication is extraordinary. Rest soon — a sharp mind saves more patients.",
    "It's midnight, Suhana. 🌟 The fact that you're still up says everything about who you'll become. Sleep now — tomorrow, we conquer.",
    "Hey midnight scholar! 🌙 Suhana, even doctors need rest. You've done enough today. I'm proud of you.",
    "Suhana, it's 12 AM. 💫 NEET toppers sleep too. Your brain consolidates today's learning during sleep. Rest is studying.",
)

class MidnightWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        createNotificationChannels(applicationContext)
        sendNovaNotification(
            context   = applicationContext,
            channelId = CHANNEL_MIDNIGHT,
            notifId   = 1004,
            title     = "Still up, Suhana? 🌙",
            message   = MIDNIGHT_MESSAGES.random(),
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
