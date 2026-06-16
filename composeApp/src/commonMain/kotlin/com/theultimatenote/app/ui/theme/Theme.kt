package com.theultimatenote.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = WarmBrown,
    onPrimary = WarmWhite,
    primaryContainer = LightBrown,
    onPrimaryContainer = DarkChocolate,
    secondary = Caramel,
    onSecondary = WarmWhite,
    secondaryContainer = LightBrown.copy(alpha = 0.4f),
    onSecondaryContainer = DeepBrown,
    tertiary = MutedGold,
    onTertiary = DarkChocolate,
    tertiaryContainer = LightGold,
    onTertiaryContainer = DeepBrown,
    background = SoftCream,
    onBackground = DarkChocolate,
    surface = WarmWhite,
    onSurface = DarkChocolate,
    surfaceVariant = WarmLightBeige,
    onSurfaceVariant = WarmBrown,
    error = ErrorRed,
    outline = SoftBrown,
)

private val DarkColorScheme = darkColorScheme(
    primary = OnDarkPrimary,
    onPrimary = DarkChocolate,
    primaryContainer = DeepBrown,
    onPrimaryContainer = OnDarkPrimary,
    secondary = OnDarkSecondary,
    onSecondary = DarkChocolate,
    secondaryContainer = Caramel.copy(alpha = 0.3f),
    onSecondaryContainer = OnDarkSecondary,
    tertiary = MutedGold,
    onTertiary = DarkChocolate,
    tertiaryContainer = MutedGold.copy(alpha = 0.2f),
    onTertiaryContainer = LightGold,
    background = DarkBackground,
    onBackground = WarmLightBeige,
    surface = DarkSurface,
    onSurface = WarmLightBeige,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = OnDarkSecondary,
    error = ErrorRed,
    outline = SoftBrown.copy(alpha = 0.5f),
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
        content = content,
    )
}
