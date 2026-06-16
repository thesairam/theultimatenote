package com.theultimatenote.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = DeepForestGreen,
    onPrimary = IvoryWhite,
    primaryContainer = SageLightGreen,
    onPrimaryContainer = DeepForestGreen,
    secondary = SageGreen,
    onSecondary = IvoryWhite,
    secondaryContainer = SageLightGreen.copy(alpha = 0.3f),
    onSecondaryContainer = SageGreen,
    tertiary = WarmBeige,
    onTertiary = DeepForestGreen,
    tertiaryContainer = WarmBeige.copy(alpha = 0.3f),
    onTertiaryContainer = DeepForestGreen,
    background = SoftCream,
    onBackground = DeepForestGreen,
    surface = IvoryWhite,
    onSurface = DeepForestGreen,
    surfaceVariant = SoftCream,
    onSurfaceVariant = ForestGreen,
    error = ErrorRed,
    outline = SageGreen.copy(alpha = 0.5f),
)

private val DarkColorScheme = darkColorScheme(
    primary = OnDarkPrimary,
    onPrimary = DeepForestGreen,
    primaryContainer = ForestGreen,
    onPrimaryContainer = OnDarkPrimary,
    secondary = OnDarkSecondary,
    onSecondary = DeepForestGreen,
    secondaryContainer = SageGreen.copy(alpha = 0.3f),
    onSecondaryContainer = OnDarkSecondary,
    tertiary = WarmBeige,
    onTertiary = DeepForestGreen,
    tertiaryContainer = WarmBeige.copy(alpha = 0.2f),
    onTertiaryContainer = WarmBeige,
    background = DarkBackground,
    onBackground = SoftCream,
    surface = DarkSurface,
    onSurface = SoftCream,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = SageLightGreen,
    error = ErrorRed,
    outline = SageGreen.copy(alpha = 0.4f),
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
