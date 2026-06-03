package com.example.suhanova.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SuhanovaDarkColorScheme = darkColorScheme(
    primary             = NovaGold,
    onPrimary           = SpaceBlack,
    primaryContainer    = Color(0xFF3D3000),
    onPrimaryContainer  = NovaGoldDim,
    secondary           = StellarPink,
    onSecondary         = SpaceBlack,
    secondaryContainer  = Color(0xFF4D0030),
    onSecondaryContainer = Color(0xFFFFB3D8),
    tertiary            = BioGreen,
    onTertiary          = SpaceBlack,
    background          = SpaceBlack,
    onBackground        = TextPrimary,
    surface             = SpaceSurface,
    onSurface           = TextPrimary,
    surfaceVariant      = SpaceCard,
    onSurfaceVariant    = TextSecondary,
    error               = WrongRed,
    onError             = SpaceBlack,
    outline             = GlassBorder,
    outlineVariant      = Color(0xFF252545),
)

@Composable
fun SuhanovaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SuhanovaDarkColorScheme,
        typography  = SuhanovaTypography,
        content     = content,
    )
}
