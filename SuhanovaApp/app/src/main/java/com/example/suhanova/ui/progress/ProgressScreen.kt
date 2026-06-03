package com.example.suhanova.ui.progress

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.suhanova.data.QuizSession
import com.example.suhanova.data.SuhanovaDatabase
import com.example.suhanova.theme.*
import com.example.suhanova.ui.components.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProgressScreen() {
    val ctx = LocalContext.current
    val db  = remember { SuhanovaDatabase.getDatabase(ctx) }
    val setupPrefs = remember { ctx.getSharedPreferences("suhanova_first_run", android.content.Context.MODE_PRIVATE) }
    val examDateMillis = remember { setupPrefs.getLong("exam_date_millis", 0L) }
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(examDateMillis) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1000)
        }
    }

    // Real data from Room DB
    val recentSessions by db.quizSessionDao().getRecentSessions().collectAsStateWithLifecycle(emptyList())
    val bioAccuracy    by db.quizSessionDao().getSubjectAccuracy("Biology").collectAsStateWithLifecycle(null)
    val physAccuracy   by db.quizSessionDao().getSubjectAccuracy("Physics").collectAsStateWithLifecycle(null)
    val chemAccuracy   by db.quizSessionDao().getSubjectAccuracy("Chemistry").collectAsStateWithLifecycle(null)
    val profile        by db.userProfileDao().getProfile().collectAsStateWithLifecycle(null)

    val neetCountdown = remember(examDateMillis, nowMillis) {
        formatProgressExamCountdown(examDateMillis, nowMillis)
    }
    val totalQuizzes = recentSessions.size
    val totalCorrect = recentSessions.sumOf { it.correctAnswers }
    val totalQuestions = recentSessions.sumOf { it.totalQuestions }
    val overallAccuracy = if (totalQuestions > 0) totalCorrect.toFloat() / totalQuestions else 0f
    val readinessScore = (overallAccuracy * 100).toInt().coerceAtLeast(0)

    // Last 7 days accuracy data
    val weekData = run {
        val calendar = Calendar.getInstance()
        (6 downTo 0).map { daysAgo ->
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
            val startOfDay = calendar.apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
            }.timeInMillis
            val endOfDay = startOfDay + 86_400_000L
            val daySessions = recentSessions.filter { it.completedAt in startOfDay until endOfDay }
            val dayCorrect = daySessions.sumOf { it.correctAnswers }
            val dayTotal = daySessions.sumOf { it.totalQuestions }
            if (dayTotal > 0) dayCorrect.toFloat() / dayTotal else 0f
        }
    }
    val weekLabels = run {
        val sdf = SimpleDateFormat("EEE", Locale.getDefault())
        val cal = Calendar.getInstance()
        (6 downTo 0).map { daysAgo ->
            cal.time = Date()
            cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
            sdf.format(cal.time)
        }
    }

    val subjectData = listOf(
        Triple("Biology",   BioGreen,  bioAccuracy),
        Triple("Physics",   PhysBlue,  physAccuracy),
        Triple("Chemistry", ChemRed,   chemAccuracy),
    )

    LazyColumn(
        Modifier.fillMaxSize().background(SpaceBlack).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 110.dp),
    ) {
        item {
            Text("Your Progress",
                style = MaterialTheme.typography.headlineMedium.copy(color = TextPrimary, fontWeight = FontWeight.ExtraBold))
            Text(
                if (totalQuizzes == 0) "Take your first quiz to see your stats here!"
                else "Real data from your $totalQuizzes quiz sessions",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
            )
        }

        // Empty state
        if (totalQuizzes == 0) {
            item {
                GlassCard {
                    Column(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("No quiz data yet", fontSize = 32.sp)
                        Text(
                            "Take a quiz and your real accuracy, progress, and subject breakdown will appear here automatically!",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary, textAlign = TextAlign.Center, lineHeight = 22.sp,
                            ),
                        )
                        Text(
                            "$neetCountdown — start now!",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = StellarPink, fontWeight = FontWeight.Bold,
                            ),
                        )
                    }
                }
            }
        }

        // Readiness Ring — only if has data
        if (totalQuizzes > 0) {
            item {
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                        .border(2.dp, Brush.sweepGradient(listOf(NovaGold, StellarPink, NovaGold)), RoundedCornerShape(24.dp))
                        .background(SpaceBlack.copy(alpha = 0.85f)).padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("NEET READINESS SCORE", style = MaterialTheme.typography.labelSmall.copy(color = NovaGold, letterSpacing = 2.sp))
                        ReadinessRing(readinessScore)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Chip("$totalQuizzes quizzes done", BioGreen)
                            Chip(neetCountdown, StellarPink)
                        }
                        Text(
                            when {
                                readinessScore >= 80 -> "Excellent prep! Keep this pace."
                                readinessScore >= 60 -> "Good progress! Focus on weak areas."
                                readinessScore > 0  -> "Keep practicing — every quiz improves your score!"
                                else                -> "Start a quiz to track your readiness."
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary, fontStyle = FontStyle.Italic, textAlign = TextAlign.Center,
                            ),
                        )
                    }
                }
            }

            // Weekly bar chart
            item {
                GlassCard {
                    Text("Accuracy This Week", style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth().height(100.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
                        weekData.forEachIndexed { i, pct ->
                            val isToday = i == weekData.lastIndex
                            val animH by animateFloatAsState(pct, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "bar$i")
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                                if (pct > 0f) {
                                    Box(
                                        Modifier.fillMaxWidth().height((animH * 80).dp)
                                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                            .background(
                                                if (isToday) Brush.verticalGradient(listOf(NovaGold, StellarPink))
                                                else Brush.verticalGradient(listOf(NovaGold.copy(alpha = 0.55f), NovaGold.copy(alpha = 0.2f)))
                                            )
                                    )
                                } else {
                                    Box(
                                        Modifier.fillMaxWidth().height(4.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color.White.copy(alpha = 0.07f))
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(weekLabels[i], style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isToday) NovaGold else TextMuted, fontSize = 9.sp))
                            }
                        }
                    }
                }
            }

            // Subject breakdown
            item {
                GlassCard {
                    Text("Subject Breakdown", style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                    Spacer(Modifier.height(14.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        subjectData.forEach { (name, color, accuracy) ->
                            val pct = accuracy ?: 0f
                            val animPct by animateFloatAsState(pct, tween(800), label = "acc$name")
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(name, style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontWeight = FontWeight.SemiBold))
                                    Text(
                                        if (accuracy == null) "No data yet" else "${(pct * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = if (accuracy == null) TextMuted else color, fontWeight = FontWeight.Bold,
                                        ),
                                    )
                                }
                                Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.07f))) {
                                    Box(Modifier.fillMaxWidth(animPct).height(8.dp).clip(CircleShape).background(Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.6f)))))
                                }
                            }
                        }
                    }
                }
            }

            // Recent sessions list
            if (recentSessions.isNotEmpty()) {
                item {
                    Text("RECENT SESSIONS", style = MaterialTheme.typography.labelSmall.copy(color = NovaGold, letterSpacing = 2.sp))
                }
                itemsIndexed(recentSessions.take(8)) { _, session ->
                    val accuracy = if (session.totalQuestions > 0)
                        session.correctAnswers * 100 / session.totalQuestions else 0
                    val color = when { accuracy >= 80 -> BioGreen; accuracy >= 60 -> NovaGold; else -> ChemRed }
                    val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(GlassBg).border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("${session.subject} — ${session.topic}",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontWeight = FontWeight.SemiBold))
                            Text(sdf.format(Date(session.completedAt)),
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
                        }
                        Text("$accuracy%",
                            style = MaterialTheme.typography.titleMedium.copy(color = color, fontWeight = FontWeight.ExtraBold))
                    }
                }
            }
        }
    }
}

private fun formatProgressExamCountdown(examDateMillis: Long, nowMillis: Long): String {
    if (examDateMillis <= 0L) return "Exam date not set"
    val remainingSeconds = ((examDateMillis - nowMillis) / 1000L).coerceAtLeast(0L)
    val days = remainingSeconds / 86_400L
    val hours = (remainingSeconds % 86_400L) / 3_600L
    val minutes = (remainingSeconds % 3_600L) / 60L
    val seconds = remainingSeconds % 60L
    return "${days}d ${hours}h ${minutes}m ${seconds}s left"
}
