package com.example.suhanova.ui.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer

// ─── PRESS SCALE MODIFIER ─────────────────────────────────────────────────────
// Adds a satisfying spring-bounce press effect to any composable

fun Modifier.pressScale(
    scale: Float = 0.93f,
    onClick: () -> Unit = {},
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animScale by animateFloatAsState(
        targetValue    = if (isPressed) scale else 1f,
        animationSpec  = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessHigh,
        ),
        label = "pressScale",
    )
    this
        .scale(animScale)
        .clickable(
            interactionSource = interactionSource,
            indication        = null,
            onClick           = onClick,
        )
}

// ─── HAPTIC FEEDBACK ──────────────────────────────────────────────────────────

fun hapticClick(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator.vibrate(
                VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
            )
        } else {
            @Suppress("DEPRECATION")
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            @Suppress("DEPRECATION")
            v.vibrate(30)
        }
    } catch (_: Exception) {}
}

fun hapticSuccess(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator.vibrate(
                VibrationEffect.createWaveform(longArrayOf(0, 40, 60, 40), -1)
            )
        } else {
            @Suppress("DEPRECATION")
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            @Suppress("DEPRECATION")
            v.vibrate(longArrayOf(0, 40, 60, 40), -1)
        }
    } catch (_: Exception) {}
}

fun hapticError(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator.vibrate(
                VibrationEffect.createWaveform(longArrayOf(0, 80, 60, 80), -1)
            )
        } else {
            @Suppress("DEPRECATION")
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            @Suppress("DEPRECATION")
            v.vibrate(longArrayOf(0, 80, 60, 80), -1)
        }
    } catch (_: Exception) {}
}

// ─── SHIMMER EFFECT ───────────────────────────────────────────────────────────

@Composable
fun shimmerBrush(highlighted: Boolean = true): androidx.compose.ui.graphics.Brush {
    val colors = if (highlighted) {
        listOf(
            com.example.suhanova.theme.GlassBg,
            com.example.suhanova.theme.NovaGold.copy(alpha = 0.08f),
            com.example.suhanova.theme.GlassBg,
        )
    } else {
        listOf(
            com.example.suhanova.theme.GlassBg,
            com.example.suhanova.theme.GlassBg,
        )
    }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val startX by transition.animateFloat(
        initialValue = -400f, targetValue = 1200f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing)),
        label = "shimmerX"
    )
    return androidx.compose.ui.graphics.Brush.linearGradient(
        colors = colors,
        start  = androidx.compose.ui.geometry.Offset(startX, 0f),
        end    = androidx.compose.ui.geometry.Offset(startX + 400f, 0f),
    )
}
