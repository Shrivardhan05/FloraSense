package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MinimalGreenPrimaryDark,
    secondary = MinimalGreenSecondaryDark,
    tertiary = MinimalGreenTertiaryDark,
    background = MinimalBackgroundDark,
    surface = MinimalSurfaceDark,
    onPrimary = MinimalBackgroundDark,
    onSecondary = MinimalBackgroundDark,
    onTertiary = MinimalBackgroundDark,
    onBackground = MinimalTextLight,
    onSurface = MinimalTextLight,
    surfaceVariant = MinimalSurfaceDark.copy(alpha = 0.8f),
    onSurfaceVariant = MinimalTextLight.copy(alpha = 0.7f)
)

private val LightColorScheme = lightColorScheme(
    primary = MinimalGreenPrimary,
    secondary = MinimalGreenSecondary,
    tertiary = MinimalGreenTertiary,
    background = MinimalBackground,
    surface = MinimalSurface,
    onPrimary = MinimalSurface,
    onSecondary = MinimalSurface,
    onTertiary = MinimalSurface,
    onBackground = MinimalTextDark,
    onSurface = MinimalTextDark,
    surfaceVariant = Color(0xFFE8F3E9), // Light green container background from HTML
    onSurfaceVariant = MinimalTextMuted // Muted text secondary from HTML
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Standard signature branding is preferred, so dynamicColor defaults to false
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
