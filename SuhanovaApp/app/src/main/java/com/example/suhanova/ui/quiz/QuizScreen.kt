package com.example.suhanova.ui.quiz

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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.suhanova.network.*
import com.example.suhanova.theme.*
import com.example.suhanova.ui.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Quiz subjects the user can pick from
val QUIZ_SUBJECTS = listOf(
    Triple("Biology",   "🧬", BioGreen),
    Triple("Physics",   "⚡", PhysBlue),
    Triple("Chemistry", "🧪", ChemRed),
    Triple("Mixed",     "✨", NovaGold),
)

val QUIZ_TOPICS = mapOf(
    "Biology"   to listOf("Cell Biology", "Genetics", "Photosynthesis", "Human Physiology", "Ecology", "Molecular Biology"),
    "Physics"   to listOf("Kinematics", "Laws of Motion", "Work & Energy", "Waves", "Optics", "Electrostatics"),
    "Chemistry" to listOf("Atomic Structure", "Chemical Bonding", "Equilibrium", "Organic Chemistry", "Thermodynamics", "Electrochemistry"),
    "Mixed"     to listOf("NEET Mixed — All Subjects"),
)

@Composable
fun QuizScreen(onNavigate: (String) -> Unit) {
    val repository = remember { QuizRepository() }
    val scope      = rememberCoroutineScope()

    var screen by remember { mutableStateOf("subject_select") } // "subject_select" | "quiz" | "results"
    var selectedSubject by remember { mutableStateOf("Biology") }
    var selectedTopic   by remember { mutableStateOf("Cell Biology") }
    var questions       by remember { mutableStateOf<List<GeneratedQuestion>>(emptyList()) }
    var isLoading       by remember { mutableStateOf(false) }
    var loadError       by remember { mutableStateOf("") }

    // Quiz state
    var questionIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var answered       by remember { mutableStateOf(false) }
    var showExplanation by remember { mutableStateOf(false) }
    var score          by remember { mutableStateOf(Triple(0, 0, 0)) } // correct, wrong, skipped
    var timeLeft       by remember { mutableIntStateOf(60) }

    when (screen) {
        "subject_select" -> {
            SubjectSelectScreen(
                isLoading     = isLoading,
                loadError     = loadError,
                selectedSubject = selectedSubject,
                selectedTopic   = selectedTopic,
                onSubjectSelect = { selectedSubject = it; selectedTopic = QUIZ_TOPICS[it]?.firstOrNull() ?: "" },
                onTopicSelect   = { selectedTopic = it },
                onStart = {
                    isLoading = true
                    loadError = ""
                    scope.launch {
                        val result = repository.generateQuestions(
                            subject    = selectedSubject,
                            topic      = selectedTopic,
                            difficulty = "Mixed",
                            count      = 5,
                        )
                        result.fold(
                            onSuccess = { qs ->
                                questions      = qs
                                questionIndex  = 0
                                selectedOption = null
                                answered       = false
                                showExplanation = false
                                score          = Triple(0, 0, 0)
                                timeLeft       = 60
                                screen         = "quiz"
                                isLoading      = false
                            },
                            onFailure = { err ->
                                // Fallback to offline question bank
                                questions = NEET_QUESTION_BANK.filter {
                                    it.subject == selectedSubject || selectedSubject == "Mixed"
                                }.shuffled().take(5).ifEmpty { NEET_QUESTION_BANK.shuffled().take(5) }

                                loadError = when {
                                    err.message?.contains("401") == true ->
                                        "Using offline questions — add GROQ_API_KEY for AI-generated quizzes!"
                                    err.message?.contains("connect") == true ->
                                        "Offline mode — using practice questions from your question bank."
                                    else -> "Using offline questions. (${err.message})"
                                }
                                questionIndex  = 0
                                selectedOption = null
                                answered       = false
                                showExplanation = false
                                score          = Triple(0, 0, 0)
                                timeLeft       = 60
                                screen         = "quiz"
                                isLoading      = false
                            }
                        )
                    }
                }
            )
        }

        "quiz" -> {
            val current = questions.getOrNull(questionIndex)
            if (current == null) { screen = "results"; return }

            // Timer
            LaunchedEffect(questionIndex, answered) {
                if (answered) return@LaunchedEffect
                timeLeft = 60
                while (timeLeft > 0 && !answered) {
                    delay(1000)
                    timeLeft--
                }
                if (!answered) {
                    answered = true
                    score = score.copy(third = score.third + 1)
                    delay(400)
                    showExplanation = true
                }
            }

            ActiveQuizScreen(
                question       = current,
                questionIndex  = questionIndex,
                totalQuestions = questions.size,
                selectedOption = selectedOption,
                answered       = answered,
                showExplanation = showExplanation,
                score          = score,
                timeLeft       = timeLeft,
                loadError      = loadError,
                onOptionSelect = { i ->
                    selectedOption = i
                    answered = true
                    score = if (i == current.correctIndex) {
                        score.copy(first = score.first + 1)
                    } else {
                        score.copy(second = score.second + 1)
                    }
                    showExplanation = true
                },
                onNext = {
                    if (questionIndex + 1 >= questions.size) {
                        screen = "results"
                    } else {
                        questionIndex++
                        selectedOption  = null
                        answered        = false
                        showExplanation = false
                        timeLeft        = 60
                    }
                },
            )
        }

        "results" -> {
            QuizResultsScreen(
                correct    = score.first,
                wrong      = score.second,
                total      = questions.size,
                subject    = selectedSubject,
                onHome     = { onNavigate("home") },
                onNewQuiz  = {
                    screen    = "subject_select"
                    questions = emptyList()
                    loadError = ""
                },
            )
        }
    }
}

// ─── SUBJECT SELECTION ────────────────────────────────────────────────────────

@Composable
fun SubjectSelectScreen(
    isLoading: Boolean,
    loadError: String,
    selectedSubject: String,
    selectedTopic: String,
    onSubjectSelect: (String) -> Unit,
    onTopicSelect: (String) -> Unit,
    onStart: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SpaceBlack).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
    ) {
        item {
            Text("NEET Quiz ✨",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = NovaGold, fontWeight = FontWeight.ExtraBold
                ))
            Spacer(Modifier.height(4.dp))
            Text("Questions generated live by Mistral AI 🤖",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
        }

        item {
            Text("CHOOSE SUBJECT",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, letterSpacing = 2.sp))
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QUIZ_SUBJECTS.forEach { (name, emoji, color) ->
                    val selected = selectedSubject == name
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (selected) color.copy(alpha = 0.2f) else GlassBg)
                            .border(
                                if (selected) 2.dp else 1.dp,
                                if (selected) color else GlassBorder,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { onSubjectSelect(name) }
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(emoji, fontSize = 22.sp)
                        Text(
                            name,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (selected) color else TextMuted,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.sp,
                            )
                        )
                    }
                }
            }
        }

        item {
            Text("CHOOSE TOPIC",
                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, letterSpacing = 2.sp))
            Spacer(Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                QUIZ_TOPICS[selectedSubject]?.forEach { topic ->
                    val selected = selectedTopic == topic
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) NovaGold.copy(alpha = 0.15f) else GlassBg)
                            .border(
                                if (selected) 2.dp else 1.dp,
                                if (selected) NovaGold else GlassBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onTopicSelect(topic) }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            topic,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (selected) NovaGold else TextPrimary,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        )
                        if (selected) Text("✓", style = MaterialTheme.typography.bodyMedium.copy(color = NovaGold, fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        item {
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NovaGold, contentColor = SpaceBlack),
                shape = CircleShape,
                enabled = !isLoading,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = SpaceBlack, strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text("Generating with Mistral AI...", fontWeight = FontWeight.Bold)
                } else {
                    Text("Start Quiz ✨", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        if (loadError.isNotEmpty()) {
            item {
                Text(
                    "ℹ️ $loadError",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = NovaGold, fontStyle = FontStyle.Italic, textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// ─── ACTIVE QUIZ ──────────────────────────────────────────────────────────────

@Composable
fun ActiveQuizScreen(
    question: GeneratedQuestion,
    questionIndex: Int,
    totalQuestions: Int,
    selectedOption: Int?,
    answered: Boolean,
    showExplanation: Boolean,
    score: Triple<Int, Int, Int>,
    timeLeft: Int,
    loadError: String,
    onOptionSelect: (Int) -> Unit,
    onNext: () -> Unit,
) {
    val timerColor = if (timeLeft < 15) ChemRed else NovaGold
    val timerPct   = timeLeft / 60f

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SpaceBlack).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("${question.subject} Quiz",
                        style = MaterialTheme.typography.titleMedium.copy(color = NovaGold, fontWeight = FontWeight.Bold))
                    Chip(question.topic, BioGreen)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${questionIndex + 1}/$totalQuestions",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = NovaGold, fontWeight = FontWeight.ExtraBold
                        ))
                    Text("questions", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                }
            }
        }

        if (loadError.isNotEmpty()) {
            item {
                Text("📴 $loadError",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = NovaGold.copy(alpha = 0.7f),
                        fontStyle = FontStyle.Italic,
                    ))
            }
        }

        item { NovaProgressBar(progress = (questionIndex + 1f) / totalQuestions) }

        // Timer + Score
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                    androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                        val sw = 6.dp.toPx(); val r = (size.minDimension - sw) / 2
                        drawCircle(Color.White.copy(alpha = 0.08f), r, style = androidx.compose.ui.graphics.drawscope.Stroke(sw))
                        drawArc(color = timerColor, startAngle = -90f, sweepAngle = 360f * timerPct,
                            useCenter = false, style = androidx.compose.ui.graphics.drawscope.Stroke(sw, cap = StrokeCap.Round))
                    }
                    Text(
                        "${timeLeft / 60}:${(timeLeft % 60).toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.titleMedium.copy(color = timerColor, fontWeight = FontWeight.ExtraBold)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chip("✓ ${score.first}", CorrectGreen)
                    Chip("✗ ${score.second}", WrongRed)
                    if (score.third > 0) Chip("⏭ ${score.third}", TextMuted)
                }
            }
        }

        // Question
        item {
            AnimatedContent(questionIndex, label = "question",
                transitionSpec = { (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut()) }
            ) { _ ->
                GlassCard {
                    Row(Modifier.padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(28.dp).clip(CircleShape).background(NovaGold.copy(alpha = 0.15f)).border(1.dp, NovaGold.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) { Text("${questionIndex + 1}", style = MaterialTheme.typography.labelMedium.copy(color = NovaGold, fontWeight = FontWeight.Bold)) }
                        Chip(question.difficulty, when (question.difficulty) { "Easy" -> BioGreen; "Hard" -> ChemRed; else -> NovaGold })
                    }
                    Text(
                        question.question,
                        style = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary, fontWeight = FontWeight.Medium, lineHeight = 26.sp)
                    )
                }
            }
        }

        // Options
        items(question.options.size) { i ->
            val isCorrect  = i == question.correctIndex
            val isSelected = i == selectedOption
            val borderCol  = when { answered && isCorrect -> CorrectGreen; answered && isSelected -> WrongRed; else -> GlassBorder }
            val bgCol      = when { answered && isCorrect -> CorrectGreen.copy(alpha = 0.08f); answered && isSelected -> WrongRed.copy(alpha = 0.08f); else -> GlassBg }

            Row(
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)).background(bgCol)
                    .border(if (answered && (isCorrect || isSelected)) 2.dp else 1.dp, borderCol, RoundedCornerShape(12.dp))
                    .clickable(enabled = !answered) { onOptionSelect(i) }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(28.dp).clip(CircleShape).background(borderCol.copy(alpha = 0.15f)).border(1.dp, borderCol.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        when { answered && isCorrect -> "✓"; answered && isSelected -> "✗"; else -> ('A' + i).toString() },
                        style = MaterialTheme.typography.labelMedium.copy(color = borderCol, fontWeight = FontWeight.Bold)
                    )
                }
                Text(question.options[i], style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary))
            }
        }

        // AI Explanation
        if (showExplanation) {
            item {
                AnimatedVisibility(true, enter = fadeIn() + slideInVertically { 30 }) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(NovaGold.copy(alpha = 0.04f))
                            .border(1.dp, Brush.linearGradient(listOf(NovaGold, StellarPink)), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("💡 Nova says:",
                            style = MaterialTheme.typography.labelMedium.copy(color = NovaGold, fontWeight = FontWeight.Bold))
                        Text(question.explanation,
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, lineHeight = 22.sp))
                        Button(
                            onClick = onNext,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = NovaGold, contentColor = SpaceBlack),
                            shape = CircleShape,
                        ) { Text(if (questionIndex + 1 >= 5) "See Results 🎉" else "Next Question →", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}

// ─── RESULTS ──────────────────────────────────────────────────────────────────

@Composable
fun QuizResultsScreen(correct: Int, wrong: Int, total: Int, subject: String, onHome: () -> Unit, onNewQuiz: () -> Unit) {
    val pct = if (total > 0) (correct * 100 / total) else 0
    val xpEarned = correct * 80

    val novaMessages = listOf(
        "Suhana, $pct% on $subject. That's real knowledge, not luck. 🩺",
        "Every question you nailed is a patient you'll help one day. $correct correct!",
        "${if (pct >= 80) "Excellent! " else if (pct >= 60) "Good work! " else "Keep going! "}$pct% — the next quiz will be even better.",
    )

    Column(
        modifier = Modifier.fillMaxSize().background(SpaceBlack).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Spacer(Modifier.height(20.dp))
        Text("QUIZ COMPLETE! ✨",
            style = MaterialTheme.typography.headlineLarge.copy(
                brush = Brush.horizontalGradient(listOf(NovaGold, StellarPink)),
                fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center
            ))

        ReadinessRing(percentage = pct)

        Text("$correct / $total Correct · +$xpEarned XP 💥",
            style = MaterialTheme.typography.titleMedium.copy(color = TextSecondary))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(
                Triple("✓ $correct", "Correct", CorrectGreen),
                Triple("✗ $wrong",   "Wrong",   WrongRed),
            ).forEach { (value, label, color) ->
                Column(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp))
                        .background(color.copy(alpha = 0.08f)).border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(16.dp)).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(value, style = MaterialTheme.typography.headlineSmall.copy(color = color, fontWeight = FontWeight.ExtraBold))
                    Text(label, style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                .background(NovaGold.copy(alpha = 0.04f))
                .border(1.dp, Brush.horizontalGradient(listOf(NovaGold, StellarPink)), RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("🩺 Nova says:", style = MaterialTheme.typography.labelMedium.copy(color = NovaGold, fontWeight = FontWeight.Bold))
            Text("\"${novaMessages.random()}\"",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontStyle = FontStyle.Italic, lineHeight = 22.sp))
        }

        Button(onClick = onNewQuiz, modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = NovaGold, contentColor = SpaceBlack), shape = CircleShape
        ) { Text("New Quiz with Mistral ✨", fontWeight = FontWeight.Bold) }

        OutlinedButton(onClick = onHome, modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NovaGold),
            border = BorderStroke(1.dp, NovaGold.copy(alpha = 0.4f)), shape = CircleShape
        ) { Text("Back to Home 🏠") }

        Text("Made specially for Suhana. My Doctor. 🩺",
            style = MaterialTheme.typography.bodySmall.copy(
                color = StellarPink, fontStyle = FontStyle.Italic, textAlign = TextAlign.Center
            ))
    }
}
