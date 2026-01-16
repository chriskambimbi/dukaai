package com.example.dukaai.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Shared design tokens for consistent UI across all screens
 * Use these values to maintain visual consistency
 */
object DukaDesign {
    // Card Corner Radii - Standardized across the app
    val CardCornerRadius = 12.dp
    val CardCornerRadiusSmall = 8.dp
    val CardCornerRadiusLarge = 16.dp

    // Common Card Shapes
    val CardShape = RoundedCornerShape(CardCornerRadius)
    val CardShapeSmall = RoundedCornerShape(CardCornerRadiusSmall)
    val CardShapeLarge = RoundedCornerShape(CardCornerRadiusLarge)

    // Bottom Sheet Shape
    val BottomSheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

    // Spacing
    val SpacingXs = 4.dp
    val SpacingSm = 8.dp
    val SpacingMd = 12.dp
    val SpacingLg = 16.dp
    val SpacingXl = 20.dp
    val SpacingXxl = 24.dp

    // Screen Padding
    val ScreenPadding = 20.dp

    // Avatar Sizes
    val AvatarSizeSmall = 36.dp
    val AvatarSizeMedium = 48.dp
    val AvatarSizeLarge = 56.dp

    // Icon Sizes
    val IconSizeSmall = 16.dp
    val IconSizeMedium = 20.dp
    val IconSizeLarge = 24.dp
    val IconSizeXLarge = 32.dp

    // Button Heights
    val ButtonHeightSmall = 32.dp
    val ButtonHeightMedium = 44.dp
    val ButtonHeightLarge = 56.dp
}
