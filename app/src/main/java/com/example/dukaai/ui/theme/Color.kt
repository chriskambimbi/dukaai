package com.example.dukaai.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================
// Modern Slate & Emerald Design System
// A clean, professional palette for business apps
// ============================================

// Primary Colors (Slate - Professional & Modern)
val SlatePrimaryDark = Color(0xFF1E293B)    // Slate 800 - Headers, primary actions
val SlatePrimary = Color(0xFF334155)         // Slate 700 - Secondary elements
val SlatePrimaryLight = Color(0xFF475569)    // Slate 600 - Hover states

// Background & Surface Colors
val SlateBackground = Color(0xFFF8FAFC)      // Slate 50 - App background
val SlateSurface = Color(0xFFFFFFFF)         // Pure white - Cards, surfaces
val SlateSurfaceVariant = Color(0xFFF1F5F9)  // Slate 100 - Secondary surfaces
val SlateBorder = Color(0xFFE2E8F0)          // Slate 200 - Borders, dividers

// Accent Colors (Emerald - Money, Success, Actions)
val EmeraldAccent = Color(0xFF10B981)        // Emerald 500 - Primary action
val EmeraldLight = Color(0xFF34D399)         // Emerald 400 - Hover/Light
val EmeraldDark = Color(0xFF059669)          // Emerald 600 - Pressed state
val EmeraldSubtle = Color(0xFFD1FAE5)        // Emerald 100 - Background tint

// Text Colors
val SlateTextPrimary = Color(0xFF0F172A)     // Slate 900 - Primary text
val SlateTextSecondary = Color(0xFF64748B)   // Slate 500 - Secondary text
val SlateTextTertiary = Color(0xFF94A3B8)    // Slate 400 - Disabled/hint text

// Status Colors
val SuccessGreen = Color(0xFF10B981)         // Emerald 500
val WarningYellow = Color(0xFFF59E0B)        // Amber 500
val ErrorRed = Color(0xFFEF4444)             // Red 500
val InfoBlue = Color(0xFF3B82F6)             // Blue 500

// Status Background Tints
val SuccessBg = Color(0xFFD1FAE5)            // Emerald 100
val WarningBg = Color(0xFFFEF3C7)            // Amber 100
val ErrorBg = Color(0xFFFEE2E2)              // Red 100
val InfoBg = Color(0xFFDBEAFE)               // Blue 100

// Stock Status Colors
val StockOk = Color(0xFF10B981)              // Emerald
val StockLow = Color(0xFFF59E0B)             // Amber
val StockOut = Color(0xFFEF4444)             // Red

// Credit Status Colors
val CreditPaid = Color(0xFF10B981)           // Emerald
val CreditDueSoon = Color(0xFFF59E0B)        // Amber
val CreditOverdue = Color(0xFFEF4444)        // Red

// Pastel colors for avatars
val AvatarPastelColors = listOf(
    Color(0xFFE0F2FE), // Sky 100
    Color(0xFFDCFCE7), // Green 100
    Color(0xFFFEF3C7), // Amber 100
    Color(0xFFFCE7F3), // Pink 100
    Color(0xFFE0E7FF), // Indigo 100
    Color(0xFFF3E8FF), // Purple 100
    Color(0xFFFFEDD5), // Orange 100
    Color(0xFFCCFBF1)  // Teal 100
)

// ============================================
// Legacy color aliases (for backward compatibility)
// These map to the new Slate & Emerald system
// ============================================

val CopperPrimary = SlatePrimaryDark
val CopperPrimaryLight = SlatePrimary
val CopperPrimaryDark = SlatePrimaryDark
val ZambianGreen = EmeraldAccent
val ZambianGreenLight = EmeraldLight
val ZambianGreenDark = EmeraldDark
val AccentOrange = Color(0xFFF59E0B)         // Amber 500 for accents
val AccentRed = ErrorRed
val BackgroundLight = SlateBackground
val BackgroundDark = Color(0xFF0F172A)       // Slate 900
val SurfaceLight = SlateSurface
val SurfaceDark = Color(0xFF1E293B)          // Slate 800
val TextPrimary = SlateTextPrimary
val TextSecondary = SlateTextSecondary
val TextOnPrimary = Color(0xFFFFFFFF)
