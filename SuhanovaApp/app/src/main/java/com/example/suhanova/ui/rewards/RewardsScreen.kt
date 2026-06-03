package com.example.suhanova.ui.rewards

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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.suhanova.theme.*
import com.example.suhanova.ui.components.*
import kotlin.random.Random

data class Badge(
    val emoji: String,
    val name: String,
    val color: Color,
    val desc: String,
    val locked: Boolean = false,
)

val badges = listOf(
    Badge("🧠", "Biology Beast",    BioGreen,   "100% Biology accuracy",       locked = false),
    Badge("⚡", "Speed Solver",     PhysBlue,   "10 questions under 5s each",  locked = false),
    Badge("🔥", "7-Day Nova",       Color(0xFFFF8C00), "7 day study streak",  locked = false),
    Badge("🌙", "Midnight Scholar", Color(0xFF9B59B6), "Studied past midnight", locked = false),
    Badge("🩺", "Future Doctor",    NovaGold,   "Complete all NEET chapters",   locked = true),
    Badge("💫", "Nova Master",      StellarPink,"Reach Level 50",               locked = true),
)

data class RankInfo(val name: String, val level: Int, val xpNeeded: Int)

val ranks = listOf(
    RankInfo("Nova Spark",    1,  500),
    RankInfo("Nova Scholar",  14, 3500),
    RankInfo("Nova Champion", 20, 8000),
    RankInfo("Nova Master",   35, 18000),
    RankInfo("Future Doctor", 50, 50000),
)

@Composable
fun RewardsScreen(onBack: () -> Unit = {}) {
    var showLevelUp by remember { mutableStateOf(false) }
    val currentXP = 2840
    val nextXP    = 3500
    val xpPct     = currentXP.toFloat() / nextXP

    val infiniteTransition = rememberInfiniteTransition(label = "rewards")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            tween(2000, easing = EaseInOutSine), RepeatMode.Reverse
        ), label = "crownPulse"
    )
    val floatY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -8f,
        animationSpec = infiniteRepeatable(
            tween(2500, easing = EaseInOutSine), RepeatMode.Reverse
        ), label = "floatY"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(SpaceBlack).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
    ) {
        item {
            PageBackButton(onClick = onBack)
        }
        item {
            Text("Nova Ranks 🏆",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = TextPrimary, fontWeight = FontWeight.ExtraBold
                ))
            Text("Every session makes you stronger, Suhana.",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
        }

        // Rank Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(2.dp, Brush.sweepGradient(listOf(NovaGold, StellarPink, NovaGold)), RoundedCornerShape(24.dp))
                    .background(SpaceBlack.copy(alpha = 0.85f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "👑",
                        fontSize = 44.sp,
                        modifier = Modifier
                            .scale(pulseScale)
                            .offset(y = floatY.dp),
                    )
                    Text("CURRENT RANK",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted, letterSpacing = 2.sp
                        ))
                    Text("NOVA SCHOLAR",
                        style = MaterialTheme.typography.displaySmall.copy(
                            brush = Brush.horizontalGradient(listOf(NovaGold, StellarPink)),
                            fontWeight = FontWeight.ExtraBold,
                        ))
                    Chip("Level 14", StellarPink)

                    // XP Bar
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("${currentXP.formatXP()} XP",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = NovaGold, fontWeight = FontWeight.Bold
                                ))
                            Text("${nextXP.formatXP()} XP → Nova Champion",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                        }
                        Box(
                            Modifier.fillMaxWidth().height(8.dp).clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                        ) {
                            val animXP by animateFloatAsState(xpPct, spring(stiffness = Spring.StiffnessLow), label = "xp")
                            Box(
                                Modifier.fillMaxHeight().fillMaxWidth(animXP).clip(CircleShape)
                                    .background(Brush.horizontalGradient(listOf(NovaGold, StellarPink)))
                            )
                        }
                    }

                    Chip("↑ +320 XP today ✨", BioGreen)
                }
            }
        }

        // Preview Level Up Button
        item {
            Button(
                onClick = { showLevelUp = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NovaGold, contentColor = SpaceBlack),
                shape = CircleShape,
            ) {
                Text("Preview Level Up 🎉", fontWeight = FontWeight.Bold)
            }
        }

        // Badges
        item {
            Text("EARNED BADGES",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted, letterSpacing = 2.sp
                ))
            Spacer(Modifier.height(10.dp))

            val rows = badges.chunked(3)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                rows.forEach { rowBadges ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowBadges.forEach { badge ->
                            val scale by animateFloatAsState(
                                if (!badge.locked) 1f else 0.9f,
                                spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                                label = badge.name
                            )
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .scale(scale)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (!badge.locked) badge.color.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.03f))
                                    .border(1.dp, if (!badge.locked) badge.color.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(if (badge.locked) "🔒" else badge.emoji, fontSize = 28.sp)
                                Text(
                                    badge.name,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (!badge.locked) badge.color else TextMuted,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        fontSize = 10.sp,
                                    )
                                )
                                Text(
                                    badge.desc,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextMuted, textAlign = TextAlign.Center, fontSize = 9.sp
                                    )
                                )
                            }
                        }
                        // Fill empty slots
                        repeat(3 - rowBadges.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }

        // Rank Progression
        item {
            GlassCard {
                Text("Nova Rank Progression",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = TextPrimary, fontWeight = FontWeight.Bold
                    ))
                Spacer(Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    ranks.forEachIndexed { i, rank ->
                        val isCurrent = rank.name == "Nova Scholar"
                        val isPast    = i < 1
                        val dotColor  = when { isPast -> BioGreen; isCurrent -> NovaGold; else -> Color.White.copy(alpha = 0.15f) }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                Modifier.size(32.dp).clip(CircleShape)
                                    .background(dotColor.copy(alpha = 0.15f))
                                    .border(2.dp, dotColor.copy(alpha = 0.6f), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    when { isPast -> "✓"; isCurrent -> "★"; else -> "${rank.level}" },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = dotColor, fontWeight = FontWeight.Bold, fontSize = 11.sp
                                    )
                                )
                            }

                            Column {
                                Text(
                                    rank.name + if (isCurrent) " ← You are here" else "",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = when { isPast -> BioGreen; isCurrent -> NovaGold; else -> TextSecondary },
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    )
                                )
                                Text(
                                    "${rank.xpNeeded.formatXP()} XP · Level ${rank.level}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                                )
                            }
                        }

                        if (i < ranks.lastIndex) {
                            Box(
                                Modifier.padding(start = 15.dp).width(2.dp).height(8.dp)
                                    .background(if (isPast) BioGreen.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.06f))
                            )
                        }
                    }
                }
            }
        }
    }

    // Level Up Dialog
    if (showLevelUp) {
        LevelUpDialog(onDismiss = { showLevelUp = false })
    }
}

fun Int.formatXP(): String = if (this >= 1000) "${this / 1000},${(this % 1000).toString().padStart(3, '0')}" else toString()

@Composable
fun LevelUpDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "levelup")
        val scale by infiniteTransition.animateFloat(
            0.95f, 1.05f,
            infiniteRepeatable(tween(1500, easing = EaseInOutSine), RepeatMode.Reverse),
            label = "lvlScale"
        )

        Box(
            modifier = Modifier.fillMaxSize().background(SpaceBlack.copy(alpha = 0.95f)),
            contentAlignment = Alignment.Center,
        ) {
            // Confetti particles
            repeat(20) { i ->
                val x by infiniteTransition.animateFloat(
                    Random.nextFloat(), Random.nextFloat(),
                    infiniteRepeatable(tween((1000..2000).random()), RepeatMode.Reverse),
                    label = "cx$i"
                )
                val y by infiniteTransition.animateFloat(
                    Random.nextFloat(), Random.nextFloat(),
                    infiniteRepeatable(tween((1000..2000).random()), RepeatMode.Reverse),
                    label = "cy$i"
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = (x * 350).dp,
                            top   = (y * 700).dp,
                        )
                ) {
                    Box(
                        Modifier.size(8.dp).clip(CircleShape)
                            .background(if (i % 2 == 0) NovaGold else StellarPink)
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text("👑", fontSize = 64.sp, modifier = Modifier.scale(scale))

                Text("LEVEL UP!",
                    style = MaterialTheme.typography.displaySmall.copy(
                        brush = Brush.horizontalGradient(listOf(NovaGold, StellarPink)),
                        fontWeight = FontWeight.ExtraBold,
                    ))
                Text("NOVA CHAMPION",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = StellarPink, fontWeight = FontWeight.ExtraBold
                    ))
                Chip("Level 15", NovaGold)

                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                        .background(GlassBg).border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("You reached Level 15! 🎉",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
                    Text("🔓 UNLOCKED: Future Doctor Badge 🩺",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = BioGreen, fontWeight = FontWeight.Bold
                        ))
                }

                Text(
                    "\"⭐ Suhana, Level 15. Not everyone makes it here. But then again, not everyone is you.\"",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextPrimary, fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center, lineHeight = 24.sp
                    )
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NovaGold, contentColor = SpaceBlack),
                    shape = CircleShape,
                ) { Text("Claim Reward ✨", fontWeight = FontWeight.Bold) }

                TextButton(onClick = onDismiss) {
                    Text("Continue Studying", color = TextMuted)
                }
            }
        }
    }
}
