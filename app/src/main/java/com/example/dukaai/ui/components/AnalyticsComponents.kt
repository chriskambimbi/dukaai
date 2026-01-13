package com.example.dukaai.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dukaai.ui.theme.ErrorRed
import com.example.dukaai.ui.theme.SuccessGreen
import com.example.dukaai.ui.theme.WarningYellow

/**
 * Time period options for analytics
 */
enum class TimePeriod(val label: String) {
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month")
}

/**
 * Insight severity types
 */
enum class InsightType {
    INFO, WARNING, SUCCESS
}

/**
 * Data class for top seller items
 */
data class TopSellerItem(
    val name: String,
    val unitsSold: Int,
    val revenue: Double,
    val rank: Int
)

/**
 * Data class for category breakdown
 */
data class CategoryItem(
    val name: String,
    val revenue: Double,
    val percentage: Float
)

/**
 * Data class for profit margin display
 */
data class ProfitMarginItem(
    val label: String,
    val percentage: Int,
    val color: Color
)

/**
 * Data class for insight cards
 */
data class InsightItem(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val type: InsightType
)

/**
 * Period selector chips row
 */
@Composable
fun PeriodSelectorChips(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TimePeriod.entries.forEach { period ->
            FilterChip(
                selected = selectedPeriod == period,
                onClick = { onPeriodSelected(period) },
                label = { Text(period.label) },
                leadingIcon = if (selectedPeriod == period) {
                    {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null
            )
        }
    }
}

/**
 * Simple bar chart for sales visualization
 */
@Composable
fun SimpleSalesBarChart(
    period: TimePeriod,
    modifier: Modifier = Modifier
) {
    val labels = when (period) {
        TimePeriod.TODAY -> listOf("8AM", "10AM", "12PM", "2PM", "4PM", "6PM")
        TimePeriod.THIS_WEEK -> listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        TimePeriod.THIS_MONTH -> listOf("W1", "W2", "W3", "W4")
    }

    val values = listOf(0.6f, 0.8f, 1.0f, 0.7f, 0.5f, 0.9f)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            values.forEachIndexed { index, value ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        modifier = Modifier
                            .width(32.dp)
                            .height((150 * value).dp),
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small
                    ) {}

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = labels.getOrElse(index) { "" },
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

/**
 * Chart legend item
 */
@Composable
fun ChartLegendItem(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            modifier = Modifier.size(12.dp),
            color = color,
            shape = MaterialTheme.shapes.extraSmall
        ) {}

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/**
 * Top seller card
 */
@Composable
fun TopSellerCard(
    seller: TopSellerItem,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RankBadge(rank = seller.rank)

                Column {
                    Text(
                        text = seller.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "${seller.unitsSold} units sold",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Text(
                text = "K ${String.format("%.0f", seller.revenue)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun RankBadge(rank: Int) {
    Surface(
        modifier = Modifier.size(32.dp),
        shape = MaterialTheme.shapes.medium,
        color = when (rank) {
            1 -> WarningYellow.copy(alpha = 0.2f)
            2 -> MaterialTheme.colorScheme.secondaryContainer
            3 -> MaterialTheme.colorScheme.tertiaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "#$rank",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Category breakdown row with progress bar
 */
@Composable
fun CategoryBreakdownRow(
    category: CategoryItem,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "K ${String.format("%.0f", category.revenue)} (${(category.percentage * 100).toInt()}%)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { category.percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

/**
 * Profit margin row item
 */
@Composable
fun ProfitMarginRow(
    item: ProfitMarginItem,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.size(12.dp),
                color = item.color,
                shape = MaterialTheme.shapes.extraSmall
            ) {}

            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Text(
            text = "${item.percentage}% of products",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

/**
 * Insight card for recommendations
 */
@Composable
fun InsightCard(
    insight: InsightItem,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (insight.type) {
        InsightType.INFO -> MaterialTheme.colorScheme.primaryContainer
        InsightType.WARNING -> WarningYellow.copy(alpha = 0.2f)
        InsightType.SUCCESS -> SuccessGreen.copy(alpha = 0.2f)
    }

    val iconColor = when (insight.type) {
        InsightType.INFO -> MaterialTheme.colorScheme.primary
        InsightType.WARNING -> WarningYellow
        InsightType.SUCCESS -> SuccessGreen
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = insight.icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = insight.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Section header for analytics sections
 */
@Composable
fun AnalyticsSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

/**
 * Get previous period label for comparison
 */
fun getPreviousPeriodLabel(period: TimePeriod): String {
    return when (period) {
        TimePeriod.TODAY -> "yesterday"
        TimePeriod.THIS_WEEK -> "last week"
        TimePeriod.THIS_MONTH -> "last month"
    }
}
