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

// ============================================
// Modern Slate & Emerald Theme
// Clean, professional, SaaS-style design
// ============================================

// Dark color scheme (Slate & Emerald)
private val DarkColorScheme = darkColorScheme(
    primary = EmeraldAccent,
    onPrimary = Color.White,
    primaryContainer = EmeraldDark,
    onPrimaryContainer = EmeraldLight,

    secondary = SlatePrimary,
    onSecondary = Color.White,
    secondaryContainer = SlatePrimaryDark,
    onSecondaryContainer = Color(0xFFE2E8F0),

    tertiary = EmeraldLight,
    onTertiary = SlatePrimaryDark,

    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = Color(0xFF0F172A),           // Slate 900
    onBackground = Color(0xFFF1F5F9),         // Slate 100

    surface = Color(0xFF1E293B),              // Slate 800
    onSurface = Color(0xFFF1F5F9),            // Slate 100
    surfaceVariant = Color(0xFF334155),       // Slate 700
    onSurfaceVariant = Color(0xFFCBD5E1),     // Slate 300

    outline = Color(0xFF475569),              // Slate 600
    outlineVariant = Color(0xFF334155)        // Slate 700
)

// Light color scheme (Slate & Emerald - Modern SaaS Style)
private val LightColorScheme = lightColorScheme(
    primary = SlatePrimaryDark,               // Slate 800 for primary actions
    onPrimary = Color.White,
    primaryContainer = SlateSurfaceVariant,   // Slate 100
    onPrimaryContainer = SlatePrimaryDark,

    secondary = EmeraldAccent,                // Emerald for accents
    onSecondary = Color.White,
    secondaryContainer = EmeraldSubtle,       // Emerald 100
    onSecondaryContainer = EmeraldDark,

    tertiary = EmeraldAccent,
    onTertiary = Color.White,

    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorBg,
    onErrorContainer = Color(0xFF7F1D1D),     // Red 900

    background = SlateBackground,             // Slate 50 (#F8FAFC)
    onBackground = SlateTextPrimary,          // Slate 900

    surface = SlateSurface,                   // White
    onSurface = SlateTextPrimary,             // Slate 900
    surfaceVariant = SlateSurfaceVariant,     // Slate 100
    onSurfaceVariant = SlateTextSecondary,    // Slate 500

    outline = SlateBorder,                    // Slate 200
    outlineVariant = Color(0xFFE2E8F0)        // Slate 200
)

@Composable
fun DukaAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+ but we'll disable it for consistent branding
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

    // Update system bars for seamless look
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Use background color for status bar (seamless SaaS style)
            window.statusBarColor = colorScheme.background.toArgb()
            // Light status bar icons for light theme, dark for dark theme
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
