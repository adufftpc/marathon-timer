package com.kartimer.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = KarTimerPrimary,
    onPrimary = KarTimerOnPrimary,
    primaryContainer = KarTimerPrimaryContainer,
    onPrimaryContainer = KarTimerOnPrimaryContainer,
    secondary = KarTimerSecondary,
    onSecondary = KarTimerOnSecondary,
    secondaryContainer = KarTimerSecondaryContainer,
    onSecondaryContainer = KarTimerOnSecondaryContainer,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = KarTimerOnBackground,
    onSurface = KarTimerOnSurface
)

private val LightColorScheme = lightColorScheme(
    primary = KarTimerPrimary,
    onPrimary = KarTimerOnPrimary,
    primaryContainer = KarTimerPrimaryContainer,
    onPrimaryContainer = KarTimerOnPrimaryContainer,
    secondary = KarTimerSecondary,
    onSecondary = KarTimerOnSecondary,
    secondaryContainer = KarTimerSecondaryContainer,
    onSecondaryContainer = KarTimerOnSecondaryContainer,
    background = Color(0xFFF8F9FF),
    surface = Color(0xFFF8F9FF),
    onBackground = Color(0xFF191C20),
    onSurface = Color(0xFF191C20)
)

@Composable
fun KarTimerTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
