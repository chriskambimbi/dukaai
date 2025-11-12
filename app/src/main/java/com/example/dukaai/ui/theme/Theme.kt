package com.example.dukaai.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Dark color scheme (Zambian copper & green theme)
private val DarkColorScheme = darkColorScheme(
    primary = CopperPrimaryLight,
    onPrimary = TextOnPrimary,
    primaryContainer = CopperPrimaryDark,
    onPrimaryContainer = CopperPrimaryLight,

    secondary = ZambianGreenLight,
    onSecondary = TextOnPrimary,
    secondaryContainer = ZambianGreenDark,
    onSecondaryContainer = ZambianGreenLight,

    tertiary = AccentOrange,
    onTertiary = TextOnPrimary,

    error = ErrorRed,
    onError = TextOnPrimary,

    background = BackgroundDark,
    onBackground = Color(0xFFE6E1E5),

    surface = SurfaceDark,
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),

    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F)
)

// Light color scheme (Zambian copper & green theme)
private val LightColorScheme = lightColorScheme(
    primary = CopperPrimary,
    onPrimary = TextOnPrimary,
    primaryContainer = CopperPrimaryLight,
    onPrimaryContainer = CopperPrimaryDark,

    secondary = ZambianGreen,
    onSecondary = TextOnPrimary,
    secondaryContainer = ZambianGreenLight,
    onSecondaryContainer = ZambianGreenDark,

    tertiary = AccentOrange,
    onTertiary = TextOnPrimary,

    error = ErrorRed,
    onError = TextOnPrimary,

    background = BackgroundLight,
    onBackground = TextPrimary,

    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = TextSecondary,

    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0)
)

@Composable
fun DukaAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+ but we'll disable it to keep Zambian branding
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Update system bars
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
