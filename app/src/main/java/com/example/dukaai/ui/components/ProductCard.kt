package com.example.dukaai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dukaai.ui.theme.*

/**
 * Modern Product Card - Flat and Clean Design
 *
 * Features:
 * - Subtle border instead of heavy shadows
 * - Initial-based placeholder with category colors
 * - Clean typography with Emerald price accent
 * - Optional sensitive info toggle
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
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = SlateBorder,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = SlateSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product initial/image placeholder
            ProductInitialAvatar(
                productName = productName,
                category = category,
                imageUrl = imageUrl
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Product info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Product name
                Text(
                    text = productName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = SlateTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Stock info with dot indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StockDotIndicator(stockStatus = stockStatus)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (stockStatus) {
                            StockStatus.OUT -> "Out of stock"
                            StockStatus.LOW -> "$currentStock left"
                            StockStatus.OK -> "$currentStock in stock"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Price section - right aligned
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Main price in Emerald
                Text(
                    text = "K ${formatPrice(sellingPrice)}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = EmeraldAccent
                )

                if (showSensitiveInfo) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Cost K ${formatPrice(buyingPrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateTextTertiary
                    )
                }
            }
        }
    }
}

/**
 * Product initial avatar with category-based colors
 */
@Composable
fun ProductInitialAvatar(
    productName: String,
    category: String,
    imageUrl: String?,
    modifier: Modifier = Modifier
) {
    val initial = productName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(SlateSurfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            // TODO: Load actual image with Coil/Glide
            Text(
                text = initial,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = SlatePrimaryDark
            )
        } else {
            Text(
                text = initial,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = SlatePrimaryDark
            )
        }
    }
}

/**
 * Product image placeholder with initials or category icon
 * (Full version with gradient - for backward compatibility)
 */
@Composable
fun ProductImagePlaceholder(
    productName: String,
    category: String,
    imageUrl: String?,
    modifier: Modifier = Modifier
) {
    ProductInitialAvatar(
        productName = productName,
        category = category,
        imageUrl = imageUrl,
        modifier = modifier
    )
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
 * Compact margin badge
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
        color = color.copy(alpha = 0.12f)
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
        StockStatus.OK -> Triple(SuccessBg, StockOk, "In Stock")
        StockStatus.LOW -> Triple(WarningBg, StockLow, "Low: $currentStock")
        StockStatus.OUT -> Triple(ErrorBg, StockOut, "Out of Stock")
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
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
        profitMargin >= 25 -> Pair(SuccessBg, SuccessGreen)
        profitMargin >= 15 -> Pair(WarningBg, WarningYellow)
        else -> Pair(ErrorBg, ErrorRed)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
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
 * Compact product card for grids
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

    val initial = productName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    Card(
        onClick = onClick,
        modifier = modifier
            .width(140.dp)
            .border(
                width = 1.dp,
                color = SlateBorder,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = SlateSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Product placeholder with stock indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SlateSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = SlatePrimaryDark
                )

                // Stock indicator dot
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
                color = SlateTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "K ${formatPrice(sellingPrice)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = EmeraldAccent
            )
        }
    }
}
