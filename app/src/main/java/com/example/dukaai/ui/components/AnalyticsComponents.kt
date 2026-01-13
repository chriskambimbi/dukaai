package com.example.dukaai.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dukaai.ui.theme.*

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
 * Data class for chart data points
 */
data class ChartDataPoint(
    val label: String,
    val value: Float
)

// ============================================
// Modern Segmented Control for Time Period
// ============================================

@Composable
fun ModernPeriodSelector(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = SlateSurfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TimePeriod.entries.forEach { period ->
                val isSelected = selectedPeriod == period

                Surface(
                    onClick = { onPeriodSelected(period) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) SlateSurface else Color.Transparent,
                    shadowElevation = if (isSelected) 2.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = EmeraldAccent
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = period.label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) SlateTextPrimary else SlateTextSecondary
                        )
                    }
                }
            }
        }
    }
}

// ============================================
// Key Metric Summary Card
// ============================================

@Composable
fun MetricSummaryCard(
    title: String,
    value: String,
    change: String?,
    isPositive: Boolean = true,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(
                width = 1.dp,
                color = SlateBorder,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = SlateTextSecondary,
                    fontWeight = FontWeight.Medium
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = iconColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = SlateTextPrimary
            )

            if (change != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (isPositive) SuccessGreen else ErrorRed
                    )
                    Text(
                        text = change,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isPositive) SuccessGreen else ErrorRed,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ============================================
// Simple Line Chart with Canvas
// ============================================

@Composable
fun SimpleLineChart(
    dataPoints: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = EmeraldAccent,
    fillColor: Color = EmeraldAccent.copy(alpha = 0.2f),
    showLabels: Boolean = true
) {
    if (dataPoints.isEmpty()) return

    val maxValue = dataPoints.maxOfOrNull { it.value } ?: 1f
    val normalizedPoints = dataPoints.map { it.value / maxValue }

    // Animate chart appearance
    var animationProgress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animationProgress,
        animationSpec = tween(durationMillis = 1000),
        label = "chartAnimation"
    )

    LaunchedEffect(dataPoints) {
        animationProgress = 1f
    }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            val width = size.width
            val height = size.height
            val padding = 40f
            val chartWidth = width - padding * 2
            val chartHeight = height - padding

            if (normalizedPoints.size < 2) return@Canvas

            val stepX = chartWidth / (normalizedPoints.size - 1)

            // Create points
            val points = normalizedPoints.mapIndexed { index, value ->
                Offset(
                    x = padding + index * stepX,
                    y = padding + chartHeight * (1 - value * animatedProgress)
                )
            }

            // Draw gradient fill
            val fillPath = Path().apply {
                moveTo(points.first().x, height - padding)
                points.forEach { point ->
                    lineTo(point.x, point.y)
                }
                lineTo(points.last().x, height - padding)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        fillColor,
                        fillColor.copy(alpha = 0.05f)
                    ),
                    startY = 0f,
                    endY = height
                )
            )

            // Draw smooth line using Bezier curves
            val linePath = Path().apply {
                moveTo(points.first().x, points.first().y)

                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]

                    val controlX1 = p0.x + (p1.x - p0.x) / 3
                    val controlX2 = p0.x + 2 * (p1.x - p0.x) / 3

                    cubicTo(
                        controlX1, p0.y,
                        controlX2, p1.y,
                        p1.x, p1.y
                    )
                }
            }

            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Draw data points
            points.forEach { point ->
                drawCircle(
                    color = SlateSurface,
                    radius = 6.dp.toPx(),
                    center = point
                )
                drawCircle(
                    color = lineColor,
                    radius = 4.dp.toPx(),
                    center = point
                )
            }
        }

        // X-axis labels
        if (showLabels && dataPoints.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dataPoints.forEach { point ->
                    Text(
                        text = point.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateTextTertiary,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// ============================================
// Revenue Trend Chart Card
// ============================================

@Composable
fun RevenueTrendCard(
    dataPoints: List<ChartDataPoint>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = SlateBorder,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Revenue Trend",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = SlateTextPrimary
            )

            Spacer(modifier = Modifier.height(20.dp))

            SimpleLineChart(
                dataPoints = dataPoints,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ============================================
// Top Seller Row Component
// ============================================

@Composable
fun TopSellerRow(
    rank: Int,
    productName: String,
    unitsSold: Int,
    revenue: Double,
    maxRevenue: Double,
    modifier: Modifier = Modifier
) {
    val progress = (revenue / maxRevenue).toFloat().coerceIn(0f, 1f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = SlateBorder,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank badge
            RankBadge(rank = rank)

            Spacer(modifier = Modifier.width(12.dp))

            // Product info and progress bar
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = productName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = SlateTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "$unitsSold units sold",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateTextTertiary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(SlateSurfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(EmeraldAccent, EmeraldLight)
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Revenue
            Text(
                text = "K ${formatAmount(revenue)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = EmeraldAccent
            )
        }
    }
}

@Composable
private fun RankBadge(rank: Int) {
    val backgroundColor = when (rank) {
        1 -> Color(0xFFFEF3C7) // Amber 100
        2 -> Color(0xFFE5E7EB) // Gray 200
        3 -> Color(0xFFFFEDD5) // Orange 100
        else -> SlateSurfaceVariant
    }

    val textColor = when (rank) {
        1 -> Color(0xFFD97706) // Amber 600
        2 -> Color(0xFF6B7280) // Gray 500
        3 -> Color(0xFFEA580C) // Orange 600
        else -> SlateTextSecondary
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "#$rank",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

// ============================================
// Category Breakdown Row
// ============================================

@Composable
fun ModernCategoryRow(
    categoryName: String,
    revenue: Double,
    percentage: Float,
    color: Color = EmeraldAccent,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = categoryName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = SlateTextPrimary
            )

            Text(
                text = "K ${formatAmount(revenue)} (${(percentage * 100).toInt()}%)",
                style = MaterialTheme.typography.bodyMedium,
                color = SlateTextSecondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(SlateSurfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

// ============================================
// Empty State Component
// ============================================

@Composable
fun AnalyticsEmptyState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(SlateSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Analytics,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = SlateTextTertiary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No sales data for this period",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = SlateTextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Start making sales to see your analytics",
            style = MaterialTheme.typography.bodyMedium,
            color = SlateTextSecondary
        )
    }
}

// ============================================
// Section Header
// ============================================

@Composable
fun AnalyticsSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = SlateTextPrimary
        )

        action?.invoke()
    }
}

// ============================================
// Insight Card (Modernized)
// ============================================

@Composable
fun ModernInsightCard(
    icon: ImageVector,
    title: String,
    description: String,
    type: InsightType,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (type) {
        InsightType.INFO -> InfoBg
        InsightType.WARNING -> WarningBg
        InsightType.SUCCESS -> SuccessBg
    }

    val iconColor = when (type) {
        InsightType.INFO -> InfoBlue
        InsightType.WARNING -> WarningYellow
        InsightType.SUCCESS -> SuccessGreen
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = SlateBorder,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = SlateTextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateTextSecondary
                )
            }
        }
    }
}

// ============================================
// Helper Functions
// ============================================

private fun formatAmount(amount: Double): String {
    return if (amount == amount.toLong().toDouble()) {
        amount.toLong().toString()
    } else {
        String.format("%.2f", amount)
    }
}

fun getPreviousPeriodLabel(period: TimePeriod): String {
    return when (period) {
        TimePeriod.TODAY -> "yesterday"
        TimePeriod.THIS_WEEK -> "last week"
        TimePeriod.THIS_MONTH -> "last month"
    }
}

// ============================================
// Legacy Components (For Backward Compatibility)
// ============================================

@Composable
fun PeriodSelectorChips(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    ModernPeriodSelector(
        selectedPeriod = selectedPeriod,
        onPeriodSelected = onPeriodSelected,
        modifier = modifier
    )
}

@Composable
fun SimpleSalesBarChart(
    period: TimePeriod,
    modifier: Modifier = Modifier
) {
    val dataPoints = when (period) {
        TimePeriod.TODAY -> listOf(
            ChartDataPoint("8AM", 120f),
            ChartDataPoint("10AM", 180f),
            ChartDataPoint("12PM", 240f),
            ChartDataPoint("2PM", 200f),
            ChartDataPoint("4PM", 150f),
            ChartDataPoint("6PM", 220f)
        )
        TimePeriod.THIS_WEEK -> listOf(
            ChartDataPoint("Mon", 450f),
            ChartDataPoint("Tue", 520f),
            ChartDataPoint("Wed", 480f),
            ChartDataPoint("Thu", 600f),
            ChartDataPoint("Fri", 550f),
            ChartDataPoint("Sat", 700f),
            ChartDataPoint("Sun", 400f)
        )
        TimePeriod.THIS_MONTH -> listOf(
            ChartDataPoint("W1", 2800f),
            ChartDataPoint("W2", 3200f),
            ChartDataPoint("W3", 2900f),
            ChartDataPoint("W4", 3500f)
        )
    }

    SimpleLineChart(
        dataPoints = dataPoints,
        modifier = modifier
    )
}

@Composable
fun StatCard(
    title: String,
    value: String,
    change: String,
    isPositive: Boolean,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    MetricSummaryCard(
        title = title,
        value = value,
        change = change,
        isPositive = isPositive,
        icon = icon,
        iconColor = if (isPositive) EmeraldAccent else ErrorRed,
        modifier = modifier
    )
}

@Composable
fun ChartLegendItem(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = SlateTextSecondary
        )
    }
}

@Composable
fun TopSellerCard(
    seller: TopSellerItem,
    modifier: Modifier = Modifier
) {
    TopSellerRow(
        rank = seller.rank,
        productName = seller.name,
        unitsSold = seller.unitsSold,
        revenue = seller.revenue,
        maxRevenue = seller.revenue * 1.2,
        modifier = modifier
    )
}

@Composable
fun CategoryBreakdownRow(
    category: CategoryItem,
    modifier: Modifier = Modifier
) {
    ModernCategoryRow(
        categoryName = category.name,
        revenue = category.revenue,
        percentage = category.percentage,
        modifier = modifier
    )
}

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
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(item.color)
            )

            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyMedium,
                color = SlateTextPrimary
            )
        }

        Text(
            text = "${item.percentage}% of products",
            style = MaterialTheme.typography.bodyMedium,
            color = SlateTextSecondary
        )
    }
}

@Composable
fun InsightCard(
    insight: InsightItem,
    modifier: Modifier = Modifier
) {
    ModernInsightCard(
        icon = insight.icon,
        title = insight.title,
        description = insight.description,
        type = insight.type,
        modifier = modifier
    )
}

@Composable
fun AnalyticsSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    AnalyticsSectionTitle(title = title, modifier = modifier)
}
