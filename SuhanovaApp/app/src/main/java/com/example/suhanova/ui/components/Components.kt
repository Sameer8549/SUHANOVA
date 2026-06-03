package com.example.suhanova.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.composed
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.suhanova.theme.*
import com.example.suhanova.ui.utils.hapticClick

// ─── ANIMATED PRESS SCALE MODIFIER ───────────────────────────────────────────

fun Modifier.bouncyClick(
    pressScale: Float = 0.93f,
    onClick: () -> Unit,
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val ctx = LocalContext.current
    val scale by animateFloatAsState(
        targetValue   = if (isPressed) pressScale else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessHigh),
        label         = "bouncyScale",
    )
    Modifier
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication        = null,
            onClick           = { hapticClick(ctx); onClick() },
        )
}

// ─── STAR FIELD BACKGROUND ────────────────────────────────────────────────────

data class Star(val x: Float, val y: Float, val radius: Float, val alpha: Float, val speed: Float)

@Composable
fun StarFieldCanvas(modifier: Modifier = Modifier) {
    val stars = remember {
        List(80) {
            Star(
                x      = (0..1000).random() / 1000f,
                y      = (0..1000).random() / 1000f,
                radius = 0.8f + (0..12).random() / 10f,
                alpha  = 0.1f + (0..6).random() / 10f,
                speed  = 1000f + (0..1000).random().toFloat(),
            )
        }
    }
    val infiniteTransition = rememberInfiniteTransition(label = "stars")

    val twinkles = stars.mapIndexed { i, star ->
        infiniteTransition.animateFloat(
            initialValue  = star.alpha * 0.3f,
            targetValue   = star.alpha,
            animationSpec = infiniteRepeatable(
                tween(star.speed.toInt(), easing = EaseInOutSine),
                RepeatMode.Reverse,
            ),
            label = "star$i",
        )
    }

    Canvas(modifier = modifier) {
        stars.forEachIndexed { i, star ->
            val alpha = twinkles[i].value
            val color = if (i % 3 == 0) NovaGold else if (i % 3 == 1) StellarPink else Color.White
            drawCircle(
                color  = color.copy(alpha = alpha),
                radius = star.radius.dp.toPx(),
                center = Offset(star.x * size.width, star.y * size.height),
            )
        }
    }
}

// ─── GLASSMORPHISM CARD ───────────────────────────────────────────────────────

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    glowColor: Color = NovaGold,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cardGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        0.05f, 0.18f,
        infiniteRepeatable(tween(2500, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "cardGlowAlpha",
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed && onClick != null) 0.97f else 1f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessHigh),
        label = "cardScale",
    )
    val ctx = LocalContext.current

    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(GlassBg)
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(glowColor.copy(alpha = 0.35f), StellarPink.copy(alpha = 0.12f), glowColor.copy(alpha = 0.35f))
                ),
                RoundedCornerShape(20.dp),
            )
            .drawBehind {
                drawRect(
                    Brush.radialGradient(
                        listOf(glowColor.copy(alpha = glowAlpha), Color.Transparent),
                        radius = size.maxDimension * 0.7f,
                    )
                )
            }
            .then(
                if (onClick != null) Modifier.clickable(interactionSource, null) {
                    hapticClick(ctx); onClick()
                } else Modifier
            )
            .padding(16.dp),
        content = content,
    )
}

// ─── NOVA MOMENT CARD ─────────────────────────────────────────────────────────

@Composable
fun NovaMomentCard(
    greeting: String,
    aiMessage: String,
    examCountdown: String,
    currentStreak: Int,
) {
    val infTrans = rememberInfiniteTransition(label = "novaMoment")
    val pulse by infTrans.animateFloat(0.92f, 1.08f, infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse), "steth")
    val borderRot by infTrans.animateFloat(0f, 360f, infiniteRepeatable(tween(5000, easing = LinearEasing)), label = "borderRot")
    val glow by infTrans.animateFloat(0.06f, 0.18f, infiniteRepeatable(tween(2500, easing = EaseInOutSine), RepeatMode.Reverse), "cardGlow")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(2.dp, Brush.sweepGradient(listOf(NovaGold, StellarPink, Color(0xFF00BFFF), NovaGold)), RoundedCornerShape(24.dp))
            .drawBehind {
                drawRect(Brush.radialGradient(listOf(NovaGold.copy(alpha = glow), Color.Transparent), radius = size.maxDimension))
            }
            .background(Color(0xFF0D0D18))
            .padding(20.dp),
    ) {
        Text("🩺", fontSize = 26.sp,
            modifier = Modifier.align(Alignment.TopEnd).graphicsLayer { scaleX = pulse; scaleY = pulse }
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("✦  YOUR NOVA MOMENT",
                style = MaterialTheme.typography.labelSmall.copy(color = NovaGold, letterSpacing = 2.sp, fontWeight = FontWeight.Bold))
            Text(greeting,
                style = MaterialTheme.typography.headlineMedium.copy(
                    brush = Brush.horizontalGradient(listOf(NovaGold, StellarPink)),
                    fontWeight = FontWeight.ExtraBold,
                ))
            Text("\"$aiMessage\"",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, fontStyle = FontStyle.Italic, lineHeight = 22.sp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip("🩺 $examCountdown", NovaGold)
                Chip("🔥 $currentStreak Day Streak",  StellarPink)
            }
            HorizontalDivider(color = StellarPink.copy(alpha = 0.2f))
            Text("Made specially for Suhana. My Doctor. 🩺",
                style = MaterialTheme.typography.bodySmall.copy(color = StellarPink, fontStyle = FontStyle.Italic),
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

// ─── SUBJECT CARD ─────────────────────────────────────────────────────────────

@Composable
fun SubjectCard(
    emoji: String,
    name: String,
    progress: Float,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val infTrans = rememberInfiniteTransition(label = "subj_${name}")
    val glow by infTrans.animateFloat(0.1f, 0.35f, infiniteRepeatable(tween(2200, easing = EaseInOutSine), RepeatMode.Reverse), label = "subjGlow$name")

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.92f else 1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessHigh), label = "subjScale$name")
    val ctx = LocalContext.current

    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.05f))
            .border(1.dp, color.copy(alpha = glow + 0.2f), RoundedCornerShape(16.dp))
            .drawBehind { drawRect(color.copy(alpha = glow * 0.15f)) }
            .clickable(interactionSource, null) { hapticClick(ctx); onClick() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(emoji, fontSize = 26.sp)
        Text(name, style = MaterialTheme.typography.labelSmall.copy(color = color, fontWeight = FontWeight.SemiBold), textAlign = TextAlign.Center)
        val animProgress by animateFloatAsState(progress, spring(stiffness = Spring.StiffnessLow), label = "subjProg$name")
        LinearProgressIndicator(
            { animProgress }, Modifier.fillMaxWidth().height(3.dp).clip(CircleShape),
            color = color, trackColor = color.copy(alpha = 0.15f),
        )
        Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 9.sp))
    }
}

// ─── CHIP ─────────────────────────────────────────────────────────────────────

@Composable
fun Chip(
    text: String,
    color: Color,
    onClick: (() -> Unit)? = null,
) {
    val ctx = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed && onClick != null) 0.92f else 1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessHigh), label = "chipScale")

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.45f), CircleShape)
            .then(
                if (onClick != null) Modifier.clickable(interactionSource, null) { hapticClick(ctx); onClick() }
                else Modifier
            )
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall.copy(color = color, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp))
    }
}

// ─── NOVA BUTTON ──────────────────────────────────────────────────────────────

@Composable
fun NovaButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit,
) {
    val ctx = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed && enabled) 0.94f else 1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessHigh), label = "novaBtnScale")
    val infTrans = rememberInfiniteTransition(label = "novaBtn")
    val glowAlpha by infTrans.animateFloat(0.3f, 0.7f, infiniteRepeatable(tween(1500, easing = EaseInOutSine), RepeatMode.Reverse), label = "btnGlow")

    Box(
        modifier = modifier
            .scale(scale)
            .clip(CircleShape)
            .then(
                if (enabled)
                    Modifier
                        .drawBehind {
                            drawRect(Brush.radialGradient(listOf(NovaGold.copy(alpha = glowAlpha * 0.25f), Color.Transparent), radius = size.maxDimension * 0.8f))
                        }
                        .background(Brush.horizontalGradient(listOf(NovaGold, StellarPink)))
                else
                    Modifier.background(TextMuted.copy(alpha = 0.3f))
            )
            .then(
                if (enabled) Modifier.clickable(interactionSource, null) { hapticClick(ctx); onClick() }
                else Modifier
            )
            .padding(vertical = 14.dp, horizontal = 28.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(18.dp), color = SpaceBlack, strokeWidth = 2.dp)
                Text(text, style = MaterialTheme.typography.labelLarge.copy(color = SpaceBlack, fontWeight = FontWeight.Bold))
            }
        } else {
            Text(text, style = MaterialTheme.typography.labelLarge.copy(color = if (enabled) SpaceBlack else TextMuted, fontWeight = FontWeight.Bold))
        }
    }
}

// ─── STREAK RING ──────────────────────────────────────────────────────────────

@Composable
fun StreakRing(streak: Int, progress: Float, modifier: Modifier = Modifier) {
    val animPct by animateFloatAsState(progress, spring(Spring.DampingRatioMediumBouncy), label = "streakRing")
    Box(modifier.size(72.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val sw = 6.dp.toPx(); val r = (size.minDimension - sw) / 2
            drawCircle(Color.White.copy(alpha = 0.08f), r, style = Stroke(sw, cap = StrokeCap.Round))
            drawArc(Brush.sweepGradient(listOf(NovaGold, StellarPink, NovaGold)), -90f, 360f * animPct, false, style = Stroke(sw, cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🔥", fontSize = 16.sp)
            Text(streak.toString(), style = MaterialTheme.typography.labelLarge.copy(color = NovaGold, fontWeight = FontWeight.ExtraBold, lineHeight = 14.sp))
        }
    }
}

// ─── NOVA PROGRESS BAR ────────────────────────────────────────────────────────

@Composable
fun NovaProgressBar(progress: Float, modifier: Modifier = Modifier, color: Color = NovaGold) {
    val animPct by animateFloatAsState(progress, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "novaProgress")
    val infTrans = rememberInfiniteTransition(label = "progressShimmer")
    val shimmerX by infTrans.animateFloat(-200f, 800f, infiniteRepeatable(tween(2000, easing = LinearEasing)), label = "shimX")

    Box(modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.08f))) {
        Box(
            Modifier.fillMaxHeight().fillMaxWidth(animPct).clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(color, StellarPink)))
                .drawBehind {
                    drawRect(Brush.linearGradient(
                        listOf(Color.Transparent, Color.White.copy(alpha = 0.3f), Color.Transparent),
                        start = Offset(shimmerX, 0f), end = Offset(shimmerX + 200f, 0f)
                    ))
                }
        )
    }
}

// ─── READINESS RING ───────────────────────────────────────────────────────────

@Composable
fun ReadinessRing(percentage: Int, modifier: Modifier = Modifier) {
    val animPct by animateFloatAsState(percentage / 100f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessVeryLow), label = "readiness")
    val infTrans = rememberInfiniteTransition(label = "ringPulse")
    val pulse by infTrans.animateFloat(0.97f, 1.03f, infiniteRepeatable(tween(2000, easing = EaseInOutSine), RepeatMode.Reverse), label = "ringPulse")

    Box(modifier.size(160.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().scale(pulse)) {
            val sw = 12.dp.toPx(); val r = (size.minDimension - sw) / 2
            drawCircle(Color.White.copy(alpha = 0.06f), r, style = Stroke(sw))
            drawArc(Brush.sweepGradient(listOf(NovaGold, StellarPink, Color(0xFF00BFFF), NovaGold)), -90f, 360f * animPct, false, style = Stroke(sw, cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$percentage%", style = MaterialTheme.typography.displaySmall.copy(color = NovaGold, fontWeight = FontWeight.ExtraBold))
            Text("READINESS", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, letterSpacing = 2.sp))
        }
    }
}

// ─── NOVA FAB (floating action button — Nova Chat accessible everywhere) ──────

@Composable
fun NovaFAB(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val infTrans = rememberInfiniteTransition(label = "fab")
    val fabPulse by infTrans.animateFloat(0.93f, 1.07f, infiniteRepeatable(tween(1800, easing = EaseInOutSine), RepeatMode.Reverse), label = "fabPulse")
    val fabGlow  by infTrans.animateFloat(0.2f, 0.6f, infiniteRepeatable(tween(1800, easing = EaseInOutSine), RepeatMode.Reverse), label = "fabGlow")

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(if (isPressed) 0.88f else fabPulse, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessHigh), label = "fabPressScale")

    Box(
        modifier = modifier
            .size(60.dp)
            .scale(pressScale)
            .drawBehind {
                drawCircle(NovaGold.copy(alpha = fabGlow * 0.4f), radius = size.minDimension * 0.7f)
            }
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(NovaGold, StellarPink)))
            .clickable(interactionSource, null) { hapticClick(ctx); onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text("✨", fontSize = 22.sp)
    }
}

// ─── BOTTOM NAV BAR ───────────────────────────────────────────────────────────

data class NavItem(val route: String, val emoji: String, val label: String)

val bottomNavItems = listOf(
    NavItem("home",     "🏠", "Home"),
    NavItem("study",    "📚", "Study"),
    NavItem("roadmap",  "🗺️", "Roadmap"),
    NavItem("library",  "🎥", "Library"),
    NavItem("progress", "📈", "Progress"),
)

@Composable
fun SuhanovaBottomNav(currentRoute: String, onNavigate: (String) -> Unit) {
    val ctx = LocalContext.current
    NavigationBar(
        containerColor = SpaceBlack.copy(alpha = 0.96f),
        tonalElevation = 0.dp,
        modifier = Modifier
            .border(width = 1.dp, brush = Brush.horizontalGradient(listOf(NovaGold.copy(alpha = 0.2f), StellarPink.copy(alpha = 0.2f), NovaGold.copy(alpha = 0.2f))), shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            val scale by animateFloatAsState(if (selected) 1.25f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "nav_${item.route}")
            NavigationBarItem(
                selected = selected,
                onClick  = { hapticClick(ctx); onNavigate(item.route) },
                icon     = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(item.emoji, fontSize = 20.sp, modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale })
                        if (selected) {
                            Spacer(Modifier.height(2.dp))
                            Box(Modifier.size(4.dp).clip(CircleShape).background(NovaGold))
                        }
                    }
                },
                label    = {
                    Text(item.label, style = MaterialTheme.typography.labelSmall.copy(
                        color = if (selected) NovaGold else TextMuted,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    ))
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor          = NovaGold.copy(alpha = 0.15f),
                    selectedIconColor       = NovaGold,
                    unselectedIconColor     = TextMuted,
                    selectedTextColor       = NovaGold,
                    unselectedTextColor     = TextMuted,
                ),
            )
        }
    }
}
