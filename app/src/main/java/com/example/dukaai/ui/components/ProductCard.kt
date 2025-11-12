package com.example.dukaai.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.dukaai.ui.theme.*

/**
 * Reusable Product Card component
 * Used in Product List, Dashboard, etc.
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
    modifier: Modifier = Modifier
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product icon placeholder
            Surface(
                modifier = Modifier.size(56.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Product info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = productName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Stock badge
                    StockBadge(
                        stockStatus = stockStatus,
                        currentStock = currentStock
                    )

                    // Profit margin
                    ProfitMarginBadge(profitMargin = profitMargin)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Price info
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "K ${String.format("%.2f", sellingPrice)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Cost: K ${String.format("%.2f", buyingPrice)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * Stock status badge
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
 * Profit margin badge
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
