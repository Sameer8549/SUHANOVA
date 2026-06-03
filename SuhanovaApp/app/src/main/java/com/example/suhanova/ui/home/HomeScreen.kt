package com.example.suhanova.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.suhanova.data.SuhanovaDatabase
import com.example.suhanova.theme.*
import com.example.suhanova.ui.components.*
import com.example.suhanova.ui.utils.hapticClick
import androidx.compose.ui.res.painterResource
import com.example.suhanova.R
import kotlinx.coroutines.delay
import java.util.Calendar

data class SubjectInfo(val emoji: String, val name: String, val color: Color, val route: String = "study")

val SUBJECTS = listOf(
    SubjectInfo("🧬", "Biology",   BioGreen),
    SubjectInfo("⚡", "Physics",   PhysBlue),
    SubjectInfo("🧪", "Chemistry", ChemRed),
    SubjectInfo("📐", "Maths",     MathGold),
)

fun getGreeting(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 0..3   -> "Midnight again, Dr. Suhana? 🌙"
    in 4..11  -> "Good morning, Dr. Suhana! ☀️"
    in 12..16 -> "Still going, future Doctor? 💪"
    in 17..20 -> "Evening, Dr. Suhana 🌆"
    else      -> "Late night grind, Suhana 🌟"
}

val quickActions = listOf(
    Triple("✨", "Today's\nQuiz",  "quiz"),
    Triple("🎥", "Library\nHub",  "library"),
    Triple("🃏", "Flash-\ncards", "study"),
    Triple("📊", "Progress",      "progress"),
)

@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    val ctx      = LocalContext.current
    val db       = remember { SuhanovaDatabase.getDatabase(ctx) }
    val setupPrefs = remember { ctx.getSharedPreferences("suhanova_first_run", android.content.Context.MODE_PRIVATE) }
    val greeting = remember { getGreeting() }
    val setupGoal = remember { setupPrefs.getString("goal", "") ?: "" }
    val setupWeakAreas = remember { setupPrefs.getString("weak_areas", "") ?: "" }
    val examDateMillis = remember { setupPrefs.getLong("exam_date_millis", 0L) }
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(examDateMillis) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1000)
        }
    }

    // Real data from DB
    val profile         by db.userProfileDao().getProfile().collectAsStateWithLifecycle(null)
    val recentSessions  by db.quizSessionDao().getRecentSessions().collectAsStateWithLifecycle(emptyList())
    val bioAccuracy     by db.quizSessionDao().getSubjectAccuracy("Biology").collectAsStateWithLifecycle(null)
    val physAccuracy    by db.quizSessionDao().getSubjectAccuracy("Physics").collectAsStateWithLifecycle(null)
    val chemAccuracy    by db.quizSessionDao().getSubjectAccuracy("Chemistry").collectAsStateWithLifecycle(null)

    val streak       = profile?.currentStreak ?: 0
    val totalXP      = profile?.totalXP ?: 0
    val totalQuizzes = recentSessions.size
    val overallAcc   = if (recentSessions.sumOf { it.totalQuestions } > 0)
        recentSessions.sumOf { it.correctAnswers }.toFloat() / recentSessions.sumOf { it.totalQuestions }
        else 0f

    val subjectAccuracies = mapOf(
        "Biology"   to (bioAccuracy ?: 0f) / 100f,
        "Physics"   to (physAccuracy ?: 0f) / 100f,
        "Chemistry" to (chemAccuracy ?: 0f) / 100f,
        "Maths"     to 0f,
    )

    val lastSession = recentSessions.firstOrNull()
    val examCountdown = remember(examDateMillis, nowMillis) {
        formatExamCountdown(examDateMillis, nowMillis)
    }
    val aiMsg = when {
        totalQuizzes == 0 && setupWeakAreas.isNotBlank() ->
            "Start with ${setupWeakAreas}. I will generate live questions from your exact topic."
        totalQuizzes == 0 && setupGoal.isNotBlank() ->
            "Your goal is $setupGoal. Start with a live quiz or ask Nova to diagnose your level."
        totalQuizzes == 0 ->
            "No old progress loaded. Start a live quiz or study session to build your real data."
        else ->
            "Your dashboard is now based on real quiz sessions saved on this device."
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(Modifier.fillMaxSize().background(SpaceBlack)) {
        StarFieldCanvas(Modifier.fillMaxSize())

        LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 120.dp),
        ) {
            // ── Header ─────────────────────────────────────────────────────────
            item {
                AnimatedVisibility(visible, enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { -40 }) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("SUHANOVA",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    brush = Brush.horizontalGradient(listOf(NovaGold, StellarPink)),
                                    fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp,
                                ))
                            Text("RISE LIKE A NOVA ✦",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, letterSpacing = 2.sp))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(38.dp).clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.06f))
                                    .border(1.dp, GlassBorder, CircleShape)
                                    .bouncyClick { hapticClick(ctx); onNavigate("settings") },
                                contentAlignment = Alignment.Center,
                            ) { Text("⚙️", fontSize = 16.sp) }
                            Box(
                                Modifier.size(46.dp).clip(CircleShape)
                                    .bouncyClick { hapticClick(ctx); onNavigate("progress") },
                                contentAlignment = Alignment.Center,
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.logo_suhanova),
                                    contentDescription = "Suhanova",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                )
                            }
                        }
                    }
                }
            }

            // ── Nova Moment Card ──────────────────────────────────────────────
            item {
                AnimatedVisibility(visible, enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { 40 }) {
                    NovaMomentCard(greeting, aiMsg, examCountdown, streak)
                }
            }

            // ── Subject Rooms ─────────────────────────────────────────────────
            item {
                AnimatedVisibility(visible, enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { 40 }) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("SUBJECT ROOMS",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, letterSpacing = 2.sp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SUBJECTS.forEach { subj ->
                                SubjectCard(
                                    emoji    = subj.emoji,
                                    name     = subj.name,
                                    progress = subjectAccuracies[subj.name] ?: 0f,
                                    color    = subj.color,
                                    onClick  = { onNavigate("study") },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }

            // ── Today's Quiz Count / Stats ────────────────────────────────────
            item {
                AnimatedVisibility(visible, enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { 40 }) {
                    GlassCard(onClick = { onNavigate("progress") }) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Overall Accuracy",
                                    style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                                Text(
                                    if (totalQuizzes == 0) "Take your first quiz!"
                                    else "$totalQuizzes quiz sessions completed",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                                )
                            }
                            Text(
                                if (totalQuizzes == 0) "--" else "${(overallAcc * 100).toInt()}%",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    color = when {
                                        overallAcc >= 0.8f -> BioGreen
                                        overallAcc >= 0.6f -> NovaGold
                                        overallAcc > 0f    -> ChemRed
                                        else               -> TextMuted
                                    },
                                    fontWeight = FontWeight.ExtraBold,
                                ),
                            )
                        }
                        if (totalQuizzes > 0) {
                            Spacer(Modifier.height(10.dp))
                            NovaProgressBar(overallAcc)
                            Spacer(Modifier.height(8.dp))
                        }
                        Text("Tap to see full progress →",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted.copy(alpha = 0.6f), fontSize = 10.sp))
                    }
                }
            }

            // ── Last Session / Resume ─────────────────────────────────────────
            if (lastSession != null) {
                item {
                    AnimatedVisibility(visible, enter = fadeIn(tween(650)) + slideInVertically(tween(650)) { 40 }) {
                        val sessionColor = when (lastSession.subject) {
                            "Biology"   -> BioGreen
                            "Physics"   -> PhysBlue
                            "Chemistry" -> ChemRed
                            else        -> MathGold
                        }
                        val sessionEmoji = when (lastSession.subject) {
                            "Biology"   -> "🧬"
                            "Physics"   -> "⚡"
                            "Chemistry" -> "🧪"
                            else        -> "📐"
                        }
                        val acc = if (lastSession.totalQuestions > 0)
                            lastSession.correctAnswers * 100 / lastSession.totalQuestions else 0

                        GlassCard(onClick = { onNavigate("quiz") }) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier.size(46.dp).clip(RoundedCornerShape(10.dp)).background(sessionColor.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center,
                                    ) { Text(sessionEmoji, fontSize = 22.sp) }
                                    Column {
                                        Text("LAST SESSION",
                                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, letterSpacing = 1.sp))
                                        Text("${lastSession.subject} — ${lastSession.topic}",
                                            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontWeight = FontWeight.Medium))
                                        Text("$acc% accuracy · ${lastSession.correctAnswers}/${lastSession.totalQuestions} correct",
                                            style = MaterialTheme.typography.bodySmall.copy(color = sessionColor))
                                    }
                                }
                                Text("›", style = MaterialTheme.typography.headlineMedium.copy(color = NovaGold))
                            }
                        }
                    }
                }
            }

            // ── Quick Actions ─────────────────────────────────────────────────
            item {
                AnimatedVisibility(visible, enter = fadeIn(tween(700)) + slideInVertically(tween(700)) { 40 }) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("QUICK ACTIONS",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, letterSpacing = 2.sp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            quickActions.forEach { (emoji, label, route) ->
                                GlassCard(modifier = Modifier.weight(1f), onClick = { onNavigate(route) }) {
                                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(emoji, fontSize = 22.sp)
                                        Text(label,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = NovaGold, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold, fontSize = 9.sp,
                                            ))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Streak Card ───────────────────────────────────────────────────
            item {
                AnimatedVisibility(visible, enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { 40 }) {
                    GlassCard(onClick = { onNavigate("rewards") }) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            StreakRing(streak, if (streak > 0) streak.toFloat() / 30f else 0f)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    if (streak == 0) "Start Your Streak! 🌟"
                                    else "$streak-Day Streak 🔥",
                                    style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold),
                                )
                                Text(
                                    when {
                                        streak == 0 -> "Study today to start your streak!"
                                        streak < 3  -> "Keep going — $streak day${if (streak != 1) "s" else ""} in a row!"
                                        streak < 7  -> "You're on fire, Suhana! 🔥"
                                        else        -> "Suhana, you are unstoppable. $streak days!"
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                                )
                                if (totalXP > 0) Chip("$totalXP XP earned ✨", NovaGold)
                            }
                        }
                    }
                }
            }

            // ── Start Quiz CTA ────────────────────────────────────────────────
            item {
                AnimatedVisibility(visible, enter = fadeIn(tween(900)) + slideInVertically(tween(900)) { 40 }) {
                    NovaButton(
                        text = if (totalQuizzes == 0) "Take Your First Quiz ✨" else "Start Today's NEET Quiz ✨",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onNavigate("quiz") },
                    )
                }
            }

            // ── Footer Message ────────────────────────────────────────────────
            item {
                AnimatedVisibility(visible, enter = fadeIn(tween(1200)) + slideInVertically(tween(1200)) { 20 }) {
                    Column(
                        Modifier.fillMaxWidth().padding(top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text("This app is specially made for you, Su.",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = StellarPink.copy(alpha = 0.8f), fontStyle = FontStyle.Italic, letterSpacing = 1.sp,
                            ))
                        Text("— From your friend 💖",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

private fun formatExamCountdown(examDateMillis: Long, nowMillis: Long): String {
    if (examDateMillis <= 0L) return "Exam date not set"
    val remainingSeconds = ((examDateMillis - nowMillis) / 1000L).coerceAtLeast(0L)
    val days = remainingSeconds / 86_400L
    val hours = (remainingSeconds % 86_400L) / 3_600L
    val minutes = (remainingSeconds % 3_600L) / 60L
    val seconds = remainingSeconds % 60L
    return "${days}d ${hours}h ${minutes}m ${seconds}s to NEET"
}
