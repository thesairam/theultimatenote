package com.theultimatenote.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = WarmWhite,
    primaryContainer = PaleGreen,
    onPrimaryContainer = DarkForest,
    secondary = SageGreen,
    onSecondary = WarmWhite,
    secondaryContainer = MintMist.copy(alpha = 0.5f),
    onSecondaryContainer = DeepForest,
    tertiary = WarmGold,
    onTertiary = DarkForest,
    tertiaryContainer = PaleGold,
    onTertiaryContainer = DeepForest,
    background = SoftCream,
    onBackground = DarkForest,
    surface = WarmWhite,
    onSurface = DarkForest,
    surfaceVariant = IvoryCream,
    onSurfaceVariant = SageGreen,
    error = SoftRose,
    outline = CardBorder,
    outlineVariant = CardBorder.copy(alpha = 0.5f),
    surfaceTint = ForestGreen.copy(alpha = 0.04f),
)

private val DarkColorScheme = darkColorScheme(
    primary = OnDarkPrimary,
    onPrimary = DarkForest,
    primaryContainer = DeepForest,
    onPrimaryContainer = OnDarkPrimary,
    secondary = OnDarkSecondary,
    onSecondary = DarkForest,
    secondaryContainer = SageGreen.copy(alpha = 0.3f),
    onSecondaryContainer = OnDarkSecondary,
    tertiary = WarmGold,
    onTertiary = DarkForest,
    tertiaryContainer = WarmGold.copy(alpha = 0.2f),
    onTertiaryContainer = LightGold,
    background = DarkBackground,
    onBackground = PaleGreen,
    surface = DarkSurface,
    onSurface = PaleGreen,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = OnDarkSecondary,
    error = ErrorRed,
    outline = DarkCardBorder,
    outlineVariant = DarkCardBorder.copy(alpha = 0.5f),
    surfaceTint = OnDarkPrimary.copy(alpha = 0.05f),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun UltimateNoteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
