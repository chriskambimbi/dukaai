package com.example.dukaai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dukaai.ui.theme.*

/**
 * Enhanced Product Card component with modern, minimalistic design
 * Features:
 * - Dynamic product initials or category icons as placeholders
 * - Simple stock dot indicator
 * - Cleaner price display with optional sensitive info
 * - Better spacing and typography
 */
@Composable
fun ProductCard(
    productName: String,
    category: String,
    currentStock: Int,
    minStockThreshold: Int,
    sellingPrice: Double,
    buyingPrice: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showSensitiveInfo: Boolean = true,
    imageUrl: String? = null
) {
    val stockStatus = when {
        currentStock == 0 -> StockStatus.OUT
        currentStock <= minStockThreshold -> StockStatus.LOW
        else -> StockStatus.OK
    }

    val profitMargin = ((sellingPrice - buyingPrice) / buyingPrice * 100).toInt()

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product image/initials placeholder
            ProductImagePlaceholder(
                productName = productName,
                category = category,
                imageUrl = imageUrl
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Product info with stock indicator
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Name with stock dot
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Stock dot indicator
                    StockDotIndicator(stockStatus = stockStatus)

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = productName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Category and stock count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )

                    Text(
                        text = when (stockStatus) {
                            StockStatus.OUT -> "Out of stock"
                            StockStatus.LOW -> "$currentStock left"
                            StockStatus.OK -> "$currentStock in stock"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = when (stockStatus) {
                            StockStatus.OUT -> StockOut
                            StockStatus.LOW -> StockLow
                            StockStatus.OK -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Price section
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Main price - prominent
                Text(
                    text = "K ${formatPrice(sellingPrice)}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = CopperPrimary
                )

                if (showSensitiveInfo) {
                    Spacer(modifier = Modifier.height(2.dp))

                    // Cost - subtle
                    Text(
                        text = "Cost K ${formatPrice(buyingPrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )

                    // Margin badge - small and aligned with price
                    Spacer(modifier = Modifier.height(4.dp))
                    CompactMarginBadge(profitMargin = profitMargin)
                }
            }
        }
    }
}

/**
 * Product image placeholder with initials or category icon
 */
@Composable
fun ProductImagePlaceholder(
    productName: String,
    category: String,
    imageUrl: String?,
    modifier: Modifier = Modifier
) {
    // Generate initials from product name
    val initials = productName
        .split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifEmpty { productName.take(2).uppercase() }

    // Get category-specific color and icon
    val (backgroundColor, icon) = getCategoryStyle(category)

    Box(
        modifier = modifier
            .size(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = 0.7f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            // TODO: Load actual image when available
            // AsyncImage(model = imageUrl, ...)
            Text(
                text = initials,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        } else {
            // Show initials or category icon
            if (initials.length >= 2) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * Simple stock dot indicator
 */
@Composable
fun StockDotIndicator(
    stockStatus: StockStatus,
    modifier: Modifier = Modifier
) {
    val color = when (stockStatus) {
        StockStatus.OK -> StockOk
        StockStatus.LOW -> StockLow
        StockStatus.OUT -> StockOut
    }

    Box(
        modifier = modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color)
    )
}

/**
 * Compact margin badge that shows percentage
 */
@Composable
fun CompactMarginBadge(
    profitMargin: Int,
    modifier: Modifier = Modifier
) {
    val color = when {
        profitMargin >= 25 -> SuccessGreen
        profitMargin >= 15 -> WarningYellow
        else -> ErrorRed
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = "+$profitMargin%",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Get category-specific styling
 */
private fun getCategoryStyle(category: String): Pair<Color, ImageVector> {
    return when (category.lowercase()) {
        "beverages" -> Pair(Color(0xFF2196F3), Icons.Outlined.LocalDrink)
        "food" -> Pair(Color(0xFF4CAF50), Icons.Outlined.Restaurant)
        "toiletries" -> Pair(Color(0xFF9C27B0), Icons.Outlined.Soap)
        "cooking oil" -> Pair(Color(0xFFFF9800), Icons.Outlined.OilBarrel)
        "household" -> Pair(Color(0xFF607D8B), Icons.Outlined.Home)
        "electronics" -> Pair(Color(0xFF3F51B5), Icons.Outlined.Devices)
        "airtime" -> Pair(Color(0xFF00BCD4), Icons.Outlined.PhoneAndroid)
        else -> Pair(CopperPrimary, Icons.Outlined.ShoppingBag)
    }
}

/**
 * Format price for display
 */
private fun formatPrice(price: Double): String {
    return if (price == price.toLong().toDouble()) {
        price.toLong().toString()
    } else {
        String.format("%.2f", price)
    }
}

/**
 * Stock status badge (legacy - kept for compatibility)
 */
@Composable
fun StockBadge(
    stockStatus: StockStatus,
    currentStock: Int,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, label) = when (stockStatus) {
        StockStatus.OK -> Triple(StockOk.copy(alpha = 0.2f), StockOk, "In Stock")
        StockStatus.LOW -> Triple(StockLow.copy(alpha = 0.2f), StockLow, "Low: $currentStock")
        StockStatus.OUT -> Triple(StockOut.copy(alpha = 0.2f), StockOut, "Out of Stock")
    }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = backgroundColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Profit margin badge (legacy - kept for compatibility)
 */
@Composable
fun ProfitMarginBadge(
    profitMargin: Int,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when {
        profitMargin >= 25 -> Pair(SuccessGreen.copy(alpha = 0.2f), SuccessGreen)
        profitMargin >= 15 -> Pair(WarningYellow.copy(alpha = 0.2f), WarningYellow)
        else -> Pair(ErrorRed.copy(alpha = 0.2f), ErrorRed)
    }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = backgroundColor
    ) {
        Text(
            text = "$profitMargin% margin",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Stock status enum
 */
enum class StockStatus {
    OK, LOW, OUT
}

/**
 * Compact product card for grids or smaller displays
 */
@Composable
fun CompactProductCard(
    productName: String,
    category: String,
    currentStock: Int,
    minStockThreshold: Int,
    sellingPrice: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stockStatus = when {
        currentStock == 0 -> StockStatus.OUT
        currentStock <= minStockThreshold -> StockStatus.LOW
        else -> StockStatus.OK
    }

    val initials = productName
        .split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifEmpty { productName.take(2).uppercase() }

    val (backgroundColor, _) = getCategoryStyle(category)

    Card(
        onClick = onClick,
        modifier = modifier.width(140.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Product image placeholder with stock badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                backgroundColor,
                                backgroundColor.copy(alpha = 0.7f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Stock indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            when (stockStatus) {
                                StockStatus.OK -> StockOk
                                StockStatus.LOW -> StockLow
                                StockStatus.OUT -> StockOut
                            }
                        )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = productName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "K ${formatPrice(sellingPrice)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CopperPrimary
            )
        }
    }
}
