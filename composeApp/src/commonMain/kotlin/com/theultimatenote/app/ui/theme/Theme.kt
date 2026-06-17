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
    primary = DeepEmerald,
    onPrimary = PaleGold,
    primaryContainer = PaleGreen,
    onPrimaryContainer = DarkEmerald,
    secondary = JewelGreen,
    onSecondary = WarmWhite,
    secondaryContainer = MintMist.copy(alpha = 0.4f),
    onSecondaryContainer = RichEmerald,
    tertiary = BurnishedGold,
    onTertiary = DarkEmerald,
    tertiaryContainer = PaleGold,
    onTertiaryContainer = RichEmerald,
    background = SoftCream,
    onBackground = DarkEmerald,
    surface = WarmWhite,
    onSurface = DarkEmerald,
    surfaceVariant = IvoryCream,
    onSurfaceVariant = SageGreen,
    error = SoftRose,
    outline = GoldBorder.copy(alpha = 0.4f),
    outlineVariant = SubtleGold.copy(alpha = 0.3f),
    surfaceTint = BurnishedGold.copy(alpha = 0.03f),
)

private val DarkColorScheme = darkColorScheme(
    primary = OnDarkPrimary,
    onPrimary = DarkEmerald,
    primaryContainer = RichEmerald,
    onPrimaryContainer = OnDarkPrimary,
    secondary = OnDarkSecondary,
    onSecondary = DarkEmerald,
    secondaryContainer = JewelGreen.copy(alpha = 0.3f),
    onSecondaryContainer = OnDarkSecondary,
    tertiary = RichGold,
    onTertiary = DarkEmerald,
    tertiaryContainer = BurnishedGold.copy(alpha = 0.2f),
    onTertiaryContainer = LightGold,
    background = DarkBackground,
    onBackground = PaleGreen,
    surface = DarkSurface,
    onSurface = PaleGreen,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = OnDarkSecondary,
    error = ErrorRed,
    outline = DarkGoldBorder,
    outlineVariant = DarkGoldBorder.copy(alpha = 0.5f),
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
