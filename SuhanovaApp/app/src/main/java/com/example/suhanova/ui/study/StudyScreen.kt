package com.example.suhanova.ui.study

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.suhanova.theme.*
import com.example.suhanova.ui.components.*
import com.example.suhanova.ui.utils.hapticClick
import com.example.suhanova.ui.utils.hapticSuccess
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

data class Flashcard(val id: String, val term: String, val definition: String, val subject: String, val emoji: String)

val flashcards = listOf(
    Flashcard("fc1", "MITOSIS",      "Cell division producing 2 identical daughter cells with the same chromosome number. PMAT: Prophase → Metaphase → Anaphase → Telophase.", "Biology", "🧬"),
    Flashcard("fc2", "OSMOSIS",      "Movement of water through a semi-permeable membrane from high water concentration (dilute) to low water concentration (concentrated).", "Biology", "💧"),
    Flashcard("fc3", "ATP",          "Adenosine Triphosphate — the energy currency of the cell. Produced in mitochondria. 36–38 ATP per glucose molecule.", "Biology", "⚡"),
    Flashcard("fc4", "NEWTON'S 1ST", "Law of Inertia: An object at rest stays at rest, in motion stays in motion — unless acted upon by an external net force.", "Physics", "🔵"),
    Flashcard("fc5", "PLASMOLYSIS",  "Shrinkage of protoplasm when a plant cell is in hypertonic solution. Water moves OUT via osmosis.", "Biology", "🌱"),
    Flashcard("fc6", "REFRACTION",   "Bending of light passing from one medium to another due to speed change. Snell's Law: n₁sinθ₁ = n₂sinθ₂.", "Physics", "🔦"),
)

val scheduleItems = listOf(
    Triple("🧬 Biology",   "Cell Division",    "9:00 – 10:30 AM"),
    Triple("⚡ Physics",   "Optics",            "11:00 AM – 12:30 PM"),
    Triple("🧪 Chemistry", "Organic Reactions", "2:00 – 3:30 PM"),
    Triple("📐 Maths",     "Calculus",          "4:00 – 5:00 PM"),
)

val studyTabs = listOf("Subjects", "Flashcards", "Planner")

@Composable
fun StudyScreen(onNavigate: (String) -> Unit = {}) {
    val ctx = LocalContext.current
    var activeTab by remember { mutableStateOf("Subjects") }

    Column(Modifier.fillMaxSize().background(SpaceBlack)) {
        // Header
        Column(Modifier.padding(horizontal = 20.dp).padding(top = 24.dp, bottom = 4.dp)) {
            Text("Study 📚", style = MaterialTheme.typography.headlineMedium.copy(color = TextPrimary, fontWeight = FontWeight.ExtraBold))
        }

        // Tab Bar with animated indicator
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.04f)).padding(4.dp)
        ) {
            studyTabs.forEach { tab ->
                val selected  = activeTab == tab
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(if (isPressed) 0.94f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "tab_$tab")
                val bgAlpha by animateFloatAsState(if (selected) 1f else 0f, tween(200), label = "tabBg_$tab")

                Box(
                    Modifier.weight(1f).scale(scale).clip(RoundedCornerShape(10.dp))
                        .background(NovaGold.copy(alpha = bgAlpha))
                        .clickable(interactionSource, null) { hapticClick(ctx); activeTab = tab }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(tab, style = MaterialTheme.typography.labelMedium.copy(
                        color = if (selected) SpaceBlack else TextMuted,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    ))
                }
            }
        }

        // Animated tab content
        AnimatedContent(
            targetState  = activeTab,
            transitionSpec = { (fadeIn(tween(250)) + slideInHorizontally(tween(250)) { it / 6 }) togetherWith (fadeOut(tween(150)) + slideOutHorizontally(tween(150)) { -it / 6 }) },
            label        = "studyTab",
        ) { tab ->
            when (tab) {
                "Subjects"   -> SubjectsTab(onNavigate = onNavigate)
                "Flashcards" -> FlashcardsTab()
                "Planner"    -> PlannerTab()
            }
        }
    }
}

// ─── SUBJECTS TAB ─────────────────────────────────────────────────────────────

@Composable
fun SubjectsTab(onNavigate: (String) -> Unit) {
    val items = listOf(
        Triple("Biology 🧬",   BioGreen, "42% · 45 chapters"),
        Triple("Physics ⚡",   PhysBlue, "38% · 38 chapters"),
        Triple("Chemistry 🧪", ChemRed,  "55% · 30 chapters"),
        Triple("Maths 📐",     MathGold, "71% · 16 chapters"),
    )
    var mounted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { mounted = true }

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)) {
        itemsIndexed(items) { idx, (name, color, meta) ->
            val pct = meta.split("%").firstOrNull()?.toFloatOrNull()?.div(100f) ?: 0f
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(mounted) { delay(idx * 80L); visible = mounted }

            AnimatedVisibility(visible, enter = fadeIn(tween(300)) + slideInHorizontally(tween(300)) { -40 }) {
                GlassCard(glowColor = color, onClick = { onNavigate("quiz") }) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(name.takeLast(2), fontSize = 28.sp)
                            Column {
                                Text(name.dropLast(3), style = MaterialTheme.typography.titleSmall.copy(color = color, fontWeight = FontWeight.Bold))
                                Text(meta.substringAfter("· "), style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                            }
                        }
                        Text(meta.substringBefore("·").trim(),
                            style = MaterialTheme.typography.headlineSmall.copy(color = color, fontWeight = FontWeight.ExtraBold))
                    }
                    Spacer(Modifier.height(10.dp))
                    NovaProgressBar(pct, color = color)
                    Spacer(Modifier.height(6.dp))
                    Text("Tap to quiz this subject →",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted.copy(alpha = 0.6f), fontSize = 9.sp))
                }
            }
        }
    }
}

// ─── FLASHCARDS TAB ───────────────────────────────────────────────────────────

@Composable
fun FlashcardsTab() {
    val ctx = LocalContext.current
    var cardIndex by remember { mutableIntStateOf(0) }
    var flipped   by remember { mutableStateOf(false) }
    var ratings   by remember { mutableStateOf(Triple(0, 0, 0)) }
    var doneCount by remember { mutableIntStateOf(0) }
    var showRating by remember { mutableStateOf(false) }

    val card = flashcards[cardIndex % flashcards.size]

    val flipAngle by animateFloatAsState(
        if (flipped) 180f else 0f,
        tween(600, easing = EaseInOutCubic),
        label = "flip",
    )
    val cardScale by animateFloatAsState(
        if (flipped) 1.02f else 1f,
        spring(Spring.DampingRatioLowBouncy),
        label = "cardScale",
    )

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Chip(card.subject, BioGreen)
                Text("${(cardIndex % flashcards.size) + 1} / ${flashcards.size}",
                    style = MaterialTheme.typography.labelMedium.copy(color = TextMuted))
            }
        }

        // 3D Flip Card
        item {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val pressScale by animateFloatAsState(if (isPressed) 0.96f else cardScale, spring(Spring.DampingRatioMediumBouncy), label = "flipPress")

            Box(
                Modifier.fillMaxWidth().height(220.dp).scale(pressScale)
                    .clickable(interactionSource, null) {
                        hapticClick(ctx)
                        flipped = !flipped
                        showRating = flipped
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (flipAngle <= 90f) {
                    // Front
                    Box(
                        Modifier.fillMaxSize()
                            .graphicsLayer { rotationY = flipAngle; cameraDistance = 12f * density }
                            .clip(RoundedCornerShape(24.dp))
                            .background(GlassBg)
                            .border(1.dp, Brush.linearGradient(listOf(NovaGold.copy(alpha = 0.5f), StellarPink.copy(alpha = 0.3f))), RoundedCornerShape(24.dp))
                            .padding(28.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        val infTrans = rememberInfiniteTransition(label = "tapHint")
                        val tapAlpha by infTrans.animateFloat(0.3f, 0.8f, infiniteRepeatable(tween(1400, easing = EaseInOutSine), RepeatMode.Reverse), "tap")
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(card.emoji, fontSize = 32.sp)
                            Text(card.term, style = MaterialTheme.typography.displaySmall.copy(
                                color = NovaGold, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center))
                            Text("TAP TO REVEAL", style = MaterialTheme.typography.labelSmall.copy(
                                color = TextMuted.copy(alpha = tapAlpha), letterSpacing = 2.sp))
                        }
                    }
                } else {
                    // Back
                    Box(
                        Modifier.fillMaxSize()
                            .graphicsLayer { rotationY = flipAngle - 180f; cameraDistance = 12f * density }
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFF0D1A14))
                            .border(1.dp, Brush.linearGradient(listOf(BioGreen.copy(alpha = 0.5f), NovaGold.copy(alpha = 0.3f))), RoundedCornerShape(24.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(card.definition, style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextPrimary, textAlign = TextAlign.Center, lineHeight = 24.sp))
                    }
                }
            }
        }

        // Rating buttons (appear after flip)
        item {
            AnimatedVisibility(
                showRating,
                enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { 20 },
                exit  = fadeOut(tween(200)),
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(
                        Triple("😵", "Hard",  ChemRed),
                        Triple("🤔", "Okay",  NovaGold),
                        Triple("😄", "Easy!", BioGreen),
                    ).forEach { (emoji, label, color) ->
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val scale by animateFloatAsState(if (isPressed) 0.88f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "rating$label")

                        Box(
                            Modifier.weight(1f).scale(scale)
                                .clip(RoundedCornerShape(14.dp))
                                .background(color.copy(alpha = 0.08f))
                                .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                .clickable(interactionSource, null) {
                                    if (label == "Easy!") { hapticSuccess(ctx) } else { hapticClick(ctx) }
                                    flipped    = false
                                    showRating = false
                                    doneCount++
                                    ratings = when (label) {
                                        "Easy!" -> ratings.copy(first = ratings.first + 1)
                                        "Okay"  -> ratings.copy(second = ratings.second + 1)
                                        else    -> ratings.copy(third = ratings.third + 1)
                                    }
                                    cardIndex++
                                }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(emoji, fontSize = 20.sp)
                                Text(label, style = MaterialTheme.typography.labelMedium.copy(color = color, fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }
        }

        // Daily goal
        item {
            GlassCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("🔥 Daily Goal: 20 cards", style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                        Text("$doneCount reviewed today", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
                    }
                    Chip("$doneCount/20", NovaGold)
                }
                Spacer(Modifier.height(10.dp))
                NovaProgressBar(doneCount.toFloat() / 20f)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chip("✓ Easy: ${ratings.first}", BioGreen)
                    Chip("∼ Okay: ${ratings.second}", NovaGold)
                    Chip("✗ Hard: ${ratings.third}", ChemRed)
                }
            }
        }
    }
}

// ─── PLANNER TAB ──────────────────────────────────────────────────────────────

@Composable
fun PlannerTab() {
    val ctx   = LocalContext.current
    val scope = rememberCoroutineScope()
    var pomodoroSeconds by remember { mutableIntStateOf(25 * 60) }
    var running by remember { mutableStateOf(false) }
    var currentSession by remember { mutableStateOf("Physics · Optics") }

    LaunchedEffect(running) {
        if (!running) return@LaunchedEffect
        while (running && pomodoroSeconds > 0) { delay(1000); pomodoroSeconds-- }
        if (pomodoroSeconds <= 0) { running = false; hapticSuccess(ctx) }
    }

    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val todayIdx   = 1
    val pomoPct    = pomodoroSeconds / (25f * 60f)
    val infTrans   = rememberInfiniteTransition(label = "planner")
    val timerColor = if (running) StellarPink else NovaGold
    val timerGlow by infTrans.animateFloat(0.15f, 0.45f, infiniteRepeatable(tween(1500, easing = EaseInOutSine), RepeatMode.Reverse), "timerGlow")

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp)) {
        // NEET Countdown
        item {
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                    .border(2.dp, Brush.sweepGradient(listOf(NovaGold, StellarPink, NovaGold)), RoundedCornerShape(24.dp))
                    .background(SpaceBlack.copy(alpha = 0.85f)).padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("🩺 NEET 2025", style = MaterialTheme.typography.labelSmall.copy(color = NovaGold, letterSpacing = 2.sp))
                    val days = remember { ((Calendar.getInstance().apply { set(2025, java.util.Calendar.JULY, 17) }.timeInMillis - System.currentTimeMillis()) / 86_400_000).toInt().coerceAtLeast(0) }
                    Text(days.toString(), style = MaterialTheme.typography.displayLarge.copy(color = NovaGold, fontWeight = FontWeight.ExtraBold))
                    Text("days remaining", style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
                    Text("\"The stethoscope is already yours, Suhana.\"",
                        style = MaterialTheme.typography.bodySmall.copy(color = StellarPink, fontStyle = FontStyle.Italic, textAlign = TextAlign.Center))
                }
            }
        }

        // Week strip
        item {
            Text("THIS WEEK", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, letterSpacing = 2.sp))
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(daysOfWeek) { i, day ->
                    val isToday = i == todayIdx
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val scale by animateFloatAsState(if (isPressed) 0.9f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "day$i")

                    Box(
                        Modifier.width(52.dp).scale(scale)
                            .clip(CircleShape)
                            .background(if (isToday) NovaGold else Color.White.copy(alpha = 0.04f))
                            .border(1.dp, if (isToday) NovaGold else GlassBorder, CircleShape)
                            .clickable(interactionSource, null) { hapticClick(ctx) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(day, style = MaterialTheme.typography.labelMedium.copy(
                                color = if (isToday) SpaceBlack else TextSecondary,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal))
                            if (isToday) Text("today", style = MaterialTheme.typography.labelSmall.copy(color = SpaceBlack.copy(alpha = 0.6f), fontSize = 8.sp))
                        }
                    }
                }
            }
        }

        // Schedule
        item {
            Text("TUESDAY SCHEDULE", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, letterSpacing = 2.sp))
            Spacer(Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                scheduleItems.forEachIndexed { i, (subject, topic, time) ->
                    val isDone   = i == 0
                    val isActive = i == 1
                    val dotColor = when { isDone -> BioGreen; isActive -> PhysBlue; else -> TextMuted }
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "sched$i")

                    Row(
                        Modifier.fillMaxWidth().scale(scale)
                            .clip(RoundedCornerShape(12.dp)).background(GlassBg)
                            .border(1.dp, if (isActive) PhysBlue.copy(alpha = 0.4f) else GlassBorder, RoundedCornerShape(12.dp))
                            .clickable(interactionSource, null) { hapticClick(ctx); if (isActive) currentSession = "$subject · $topic" }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(dotColor))
                        Text(subject.take(2), fontSize = 18.sp)
                        Column(Modifier.weight(1f)) {
                            Text("$subject · $topic" + if (isDone) " ✅" else if (isActive) " ▶" else "",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (isDone) TextMuted else TextPrimary,
                                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal))
                            Text(time, style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                        }
                    }
                }
            }
        }

        // Pomodoro Timer
        item {
            GlassCard {
                Text("FOCUS SESSION 🍅", style = MaterialTheme.typography.labelMedium.copy(color = NovaGold, letterSpacing = 1.sp, fontWeight = FontWeight.Bold))
                Spacer(Modifier.height(16.dp))

                // Timer ring
                Box(Modifier.size(120.dp).align(Alignment.CenterHorizontally), contentAlignment = Alignment.Center) {
                    androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                        val sw = 10.dp.toPx(); val r = (size.minDimension - sw) / 2
                        drawCircle(Color.White.copy(alpha = 0.06f), r, style = androidx.compose.ui.graphics.drawscope.Stroke(sw))
                        if (running) drawCircle(timerColor.copy(alpha = timerGlow * 0.3f), r * 1.1f)
                        drawArc(Brush.sweepGradient(listOf(timerColor, timerColor.copy(alpha = 0.5f), timerColor)), -90f, 360f * pomoPct, false, style = androidx.compose.ui.graphics.drawscope.Stroke(sw, cap = androidx.compose.ui.graphics.StrokeCap.Round))
                    }
                    Text(
                        "${(pomodoroSeconds / 60).toString().padStart(2,'0')}:${(pomodoroSeconds % 60).toString().padStart(2,'0')}",
                        style = MaterialTheme.typography.headlineMedium.copy(color = timerColor, fontWeight = FontWeight.ExtraBold)
                    )
                }

                Spacer(Modifier.height(6.dp))
                Text(currentSession, style = MaterialTheme.typography.labelMedium.copy(color = TextMuted),
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))

                // Ambient sounds
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    listOf("🌧️" to "Rain", "🎵" to "Lo-fi", "📚" to "Library", "🌿" to "Nature").forEach { (emoji, label) ->
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val scale by animateFloatAsState(if (isPressed) 0.85f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "ambient$label")
                        Box(
                            Modifier.scale(scale).clip(RoundedCornerShape(10.dp))
                                .background(NovaGold.copy(alpha = 0.06f))
                                .clickable(interactionSource, null) { hapticClick(ctx) }
                                .padding(10.dp),
                            contentAlignment = Alignment.Center,
                        ) { Text(emoji, fontSize = 22.sp) }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NovaButton(
                        text     = if (running) "⏸ Pause" else "▶ Start",
                        modifier = Modifier.weight(1f),
                        onClick  = { hapticClick(ctx); running = !running },
                    )
                    val interactionSource2 = remember { MutableInteractionSource() }
                    val isPressed2 by interactionSource2.collectIsPressedAsState()
                    val scale2 by animateFloatAsState(if (isPressed2) 0.9f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "resetScale")
                    Box(
                        Modifier.scale(scale2).clip(CircleShape)
                            .border(1.dp, NovaGold.copy(alpha = 0.4f), CircleShape)
                            .clickable(interactionSource2, null) { hapticClick(ctx); pomodoroSeconds = 25 * 60; running = false }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) { Text("↺", style = MaterialTheme.typography.titleLarge.copy(color = NovaGold, fontWeight = FontWeight.Bold)) }
                }
            }
        }
    }
}

