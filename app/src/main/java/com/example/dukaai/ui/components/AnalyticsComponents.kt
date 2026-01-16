package com.example.dukaai.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    THIS_MONTH("This Month"),
    CUSTOM("Custom")
}

/**
 * Insight severity types
 */
enum class InsightType {
    INFO, WARNING, SUCCESS, AI
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
    val value: Float,
    val comparisonValue: Float? = null
)

/**
 * Data class for metric breakdown details
 */
data class MetricBreakdown(
    val label: String,
    val value: String,
    val percentage: Float? = null
)

// ============================================
// Modern Segmented Control for Time Period
// With Custom Date Range Option
// ============================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernPeriodSelector(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    onCustomDateClick: (() -> Unit)? = null,
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
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Standard periods (excluding CUSTOM)
            TimePeriod.entries.filter { it != TimePeriod.CUSTOM }.forEach { period ->
                val isSelected = selectedPeriod == period

                Surface(
                    onClick = { onPeriodSelected(period) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) SlateSurface else Color.Transparent,
                    shadowElevation = if (isSelected) 2.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = EmeraldAccent
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = period.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) SlateTextPrimary else SlateTextSecondary
                        )
                    }
                }
            }

            // Custom date button with calendar icon
            if (onCustomDateClick != null) {
                val isCustomSelected = selectedPeriod == TimePeriod.CUSTOM

                Surface(
                    onClick = {
                        onCustomDateClick()
                        onPeriodSelected(TimePeriod.CUSTOM)
                    },
                    modifier = Modifier.padding(start = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = if (isCustomSelected) SlateSurface else Color.Transparent,
                    shadowElevation = if (isCustomSelected) 2.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = "Custom date range",
                            modifier = Modifier.size(18.dp),
                            tint = if (isCustomSelected) EmeraldAccent else SlateTextSecondary
                        )
                    }
                }
            }
        }
    }
}

// ============================================
// Expandable Key Metric Summary Card
// ============================================

@Composable
fun ExpandableMetricCard(
    title: String,
    value: String,
    change: String?,
    isPositive: Boolean = true,
    icon: ImageVector,
    iconColor: Color,
    breakdown: List<MetricBreakdown>? = null,
    infoTooltip: String? = null,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showTooltip by remember { mutableStateOf(false) }

    Card(
        onClick = { if (breakdown != null) isExpanded = !isExpanded },
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = SlateTextSecondary,
                        fontWeight = FontWeight.Medium
                    )

                    // Info tooltip icon
                    if (infoTooltip != null) {
                        Box {
                            IconButton(
                                onClick = { showTooltip = !showTooltip },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.HelpOutline,
                                    contentDescription = "Info",
                                    modifier = Modifier.size(14.dp),
                                    tint = SlateTextTertiary
                                )
                            }

                            // Tooltip dropdown
                            DropdownMenu(
                                expanded = showTooltip,
                                onDismissRequest = { showTooltip = false }
                            ) {
                                Text(
                                    text = infoTooltip,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .widthIn(max = 200.dp),
                                    color = SlateTextPrimary
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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

                    // Expand indicator
                    if (breakdown != null) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            modifier = Modifier.size(20.dp),
                            tint = SlateTextTertiary
                        )
                    }
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

            // Expanded breakdown section
            AnimatedVisibility(
                visible = isExpanded && breakdown != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    HorizontalDivider(color = SlateBorder)

                    Spacer(modifier = Modifier.height(12.dp))

                    breakdown?.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateTextSecondary
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = item.value,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = SlateTextPrimary
                                )
                                item.percentage?.let {
                                    Text(
                                        text = "(${String.format("%.0f", it * 100)}%)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SlateTextTertiary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Legacy wrapper for backward compatibility
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
    ExpandableMetricCard(
        title = title,
        value = value,
        change = change,
        isPositive = isPositive,
        icon = icon,
        iconColor = iconColor,
        breakdown = null,
        infoTooltip = null,
        modifier = modifier
    )
}

// ============================================
// Enhanced Line Chart with Y-axis, Interaction & Comparison
// ============================================

@Composable
fun EnhancedLineChart(
    dataPoints: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = EmeraldAccent,
    comparisonLineColor: Color = SlateTextTertiary,
    fillColor: Color = EmeraldAccent.copy(alpha = 0.2f),
    showComparison: Boolean = true,
    showYAxis: Boolean = true,
    yAxisLabel: String = "Revenue (K)",
    onDataPointTap: ((ChartDataPoint) -> Unit)? = null
) {
    if (dataPoints.isEmpty()) return

    val maxValue = maxOf(
        dataPoints.maxOfOrNull { it.value } ?: 1f,
        dataPoints.maxOfOrNull { it.comparisonValue ?: 0f } ?: 1f
    )
    val normalizedPoints = dataPoints.map { it.value / maxValue }
    val normalizedComparison = dataPoints.map { (it.comparisonValue ?: 0f) / maxValue }

    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
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
        // Y-axis label
        if (showYAxis) {
            Text(
                text = yAxisLabel,
                style = MaterialTheme.typography.labelSmall,
                color = SlateTextTertiary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Y-axis values
            if (showYAxis) {
                Column(
                    modifier = Modifier
                        .width(40.dp)
                        .height(180.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatChartValue(maxValue),
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateTextTertiary
                    )
                    Text(
                        text = formatChartValue(maxValue * 0.5f),
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateTextTertiary
                    )
                    Text(
                        text = "0",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateTextTertiary
                    )
                }
            }

            // Chart canvas
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(180.dp)
                    .pointerInput(dataPoints) {
                        detectTapGestures { offset ->
                            val chartWidth = size.width.toFloat()
                            val stepX = chartWidth / (dataPoints.size - 1)
                            val tappedIndex = ((offset.x / stepX) + 0.5f).toInt()
                                .coerceIn(0, dataPoints.size - 1)
                            selectedPointIndex = tappedIndex
                            onDataPointTap?.invoke(dataPoints[tappedIndex])
                        }
                    }
            ) {
                val width = size.width
                val height = size.height
                val padding = 10f
                val chartHeight = height - padding * 2

                if (normalizedPoints.size < 2) return@Canvas

                val stepX = width / (normalizedPoints.size - 1)

                // Draw grid lines
                for (i in 0..4) {
                    val y = padding + (chartHeight * i / 4)
                    drawLine(
                        color = SlateBorder,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
                    )
                }

                // Create main line points
                val points = normalizedPoints.mapIndexed { index, value ->
                    Offset(
                        x = index * stepX,
                        y = padding + chartHeight * (1 - value * animatedProgress)
                    )
                }

                // Create comparison line points
                val comparisonPoints = if (showComparison && normalizedComparison.any { it > 0 }) {
                    normalizedComparison.mapIndexed { index, value ->
                        Offset(
                            x = index * stepX,
                            y = padding + chartHeight * (1 - value * animatedProgress)
                        )
                    }
                } else null

                // Draw comparison line first (behind main line)
                comparisonPoints?.let { compPoints ->
                    val compPath = Path().apply {
                        moveTo(compPoints.first().x, compPoints.first().y)
                        for (i in 0 until compPoints.size - 1) {
                            val p0 = compPoints[i]
                            val p1 = compPoints[i + 1]
                            val controlX1 = p0.x + (p1.x - p0.x) / 3
                            val controlX2 = p0.x + 2 * (p1.x - p0.x) / 3
                            cubicTo(controlX1, p0.y, controlX2, p1.y, p1.x, p1.y)
                        }
                    }
                    drawPath(
                        path = compPath,
                        color = comparisonLineColor,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f))
                        )
                    )
                }

                // Draw gradient fill for main line
                val fillPath = Path().apply {
                    moveTo(points.first().x, height - padding)
                    points.forEach { point -> lineTo(point.x, point.y) }
                    lineTo(points.last().x, height - padding)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(fillColor, fillColor.copy(alpha = 0.05f)),
                        startY = 0f,
                        endY = height
                    )
                )

                // Draw smooth main line
                val linePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 0 until points.size - 1) {
                        val p0 = points[i]
                        val p1 = points[i + 1]
                        val controlX1 = p0.x + (p1.x - p0.x) / 3
                        val controlX2 = p0.x + 2 * (p1.x - p0.x) / 3
                        cubicTo(controlX1, p0.y, controlX2, p1.y, p1.x, p1.y)
                    }
                }

                drawPath(
                    path = linePath,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Draw data points
                points.forEachIndexed { index, point ->
                    val isSelected = selectedPointIndex == index
                    val pointRadius = if (isSelected) 8.dp.toPx() else 6.dp.toPx()

                    drawCircle(color = SlateSurface, radius = pointRadius, center = point)
                    drawCircle(
                        color = if (isSelected) EmeraldDark else lineColor,
                        radius = pointRadius - 2.dp.toPx(),
                        center = point
                    )
                }
            }
        }

        // X-axis labels
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = if (showYAxis) 40.dp else 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dataPoints.forEachIndexed { index, point ->
                Text(
                    text = point.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selectedPointIndex == index) EmeraldAccent else SlateTextTertiary,
                    fontWeight = if (selectedPointIndex == index) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }

        // Legend
        if (showComparison && dataPoints.any { it.comparisonValue != null }) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ChartLegendItem(color = lineColor, label = "This period")
                Spacer(modifier = Modifier.width(16.dp))
                ChartLegendItem(color = comparisonLineColor, label = "Previous period", isDashed = true)
            }
        }

        // Selected point details
        selectedPointIndex?.let { index ->
            val selectedPoint = dataPoints[index]
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = EmeraldAccent.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = selectedPoint.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = SlateTextPrimary
                        )
                        Text(
                            text = "K ${formatChartValue(selectedPoint.value)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldAccent
                        )
                    }
                    selectedPoint.comparisonValue?.let { comp ->
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "vs Previous",
                                style = MaterialTheme.typography.labelSmall,
                                color = SlateTextTertiary
                            )
                            Text(
                                text = "K ${formatChartValue(comp)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SlateTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

// Legacy wrapper
@Composable
fun SimpleLineChart(
    dataPoints: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = EmeraldAccent,
    fillColor: Color = EmeraldAccent.copy(alpha = 0.2f),
    showLabels: Boolean = true
) {
    EnhancedLineChart(
        dataPoints = dataPoints,
        modifier = modifier,
        lineColor = lineColor,
        fillColor = fillColor,
        showComparison = false,
        showYAxis = false
    )
}

// ============================================
// Enhanced Revenue Trend Chart Card
// ============================================

@Composable
fun EnhancedRevenueTrendCard(
    dataPoints: List<ChartDataPoint>,
    showComparison: Boolean = true,
    onExportClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedPoint by remember { mutableStateOf<ChartDataPoint?>(null) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Revenue Trend",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = SlateTextPrimary
                )

                if (onExportClick != null) {
                    IconButton(
                        onClick = onExportClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Export",
                            tint = SlateTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Tap on data points to see details",
                style = MaterialTheme.typography.bodySmall,
                color = SlateTextTertiary
            )

            Spacer(modifier = Modifier.height(16.dp))

            EnhancedLineChart(
                dataPoints = dataPoints,
                showComparison = showComparison,
                showYAxis = true,
                yAxisLabel = "Revenue (K)",
                onDataPointTap = { selectedPoint = it },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// Legacy wrapper
@Composable
fun RevenueTrendCard(
    dataPoints: List<ChartDataPoint>,
    modifier: Modifier = Modifier
) {
    EnhancedRevenueTrendCard(
        dataPoints = dataPoints,
        showComparison = false,
        onExportClick = null,
        modifier = modifier
    )
}

// ============================================
// AI Insight Card
// ============================================

@Composable
fun AIInsightCard(
    title: String,
    description: String,
    metric: String? = null,
    metricLabel: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = EmeraldAccent.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldAccent.copy(alpha = 0.08f)),
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
                    .background(EmeraldAccent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = EmeraldAccent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = SlateTextPrimary
                    )

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = EmeraldAccent.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "AI",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldAccent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateTextSecondary
                )

                if (metric != null && metricLabel != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = metric,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldAccent
                        )
                        Text(
                            text = metricLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateTextTertiary
                        )
                    }
                }
            }
        }
    }
}

// ============================================
// Export/Share Button Component
// ============================================

@Composable
fun ExportReportButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = EmeraldAccent
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldAccent.copy(alpha = 0.5f))
    ) {
        Icon(
            imageVector = Icons.Outlined.FileDownload,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Export Report",
            fontWeight = FontWeight.SemiBold
        )
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
            RankBadge(rank = rank)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
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
        1 -> Color(0xFFFEF3C7)
        2 -> Color(0xFFE5E7EB)
        3 -> Color(0xFFFFEDD5)
        else -> SlateSurfaceVariant
    }

    val textColor = when (rank) {
        1 -> Color(0xFFD97706)
        2 -> Color(0xFF6B7280)
        3 -> Color(0xFFEA580C)
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
fun AnalyticsEmptyState(modifier: Modifier = Modifier) {
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
        InsightType.AI -> EmeraldAccent.copy(alpha = 0.08f)
    }

    val iconColor = when (type) {
        InsightType.INFO -> InfoBlue
        InsightType.WARNING -> WarningYellow
        InsightType.SUCCESS -> SuccessGreen
        InsightType.AI -> EmeraldAccent
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
// Chart Legend Item
// ============================================

@Composable
fun ChartLegendItem(
    color: Color,
    label: String,
    isDashed: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (isDashed) {
            Canvas(
                modifier = Modifier
                    .width(16.dp)
                    .height(2.dp)
            ) {
                drawLine(
                    color = color,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 2f))
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = SlateTextSecondary
        )
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

private fun formatChartValue(value: Float): String {
    return if (value >= 1000) {
        String.format("%.1fK", value / 1000)
    } else {
        String.format("%.0f", value)
    }
}

fun getPreviousPeriodLabel(period: TimePeriod): String {
    return when (period) {
        TimePeriod.TODAY -> "yesterday"
        TimePeriod.THIS_WEEK -> "last week"
        TimePeriod.THIS_MONTH -> "last month"
        TimePeriod.CUSTOM -> "previous period"
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
fun SimpleSalesBarChart(period: TimePeriod, modifier: Modifier = Modifier) {
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
        TimePeriod.THIS_MONTH, TimePeriod.CUSTOM -> listOf(
            ChartDataPoint("W1", 2800f),
            ChartDataPoint("W2", 3200f),
            ChartDataPoint("W3", 2900f),
            ChartDataPoint("W4", 3500f)
        )
    }

    SimpleLineChart(dataPoints = dataPoints, modifier = modifier)
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
fun TopSellerCard(seller: TopSellerItem, modifier: Modifier = Modifier) {
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
fun CategoryBreakdownRow(category: CategoryItem, modifier: Modifier = Modifier) {
    ModernCategoryRow(
        categoryName = category.name,
        revenue = category.revenue,
        percentage = category.percentage,
        modifier = modifier
    )
}

@Composable
fun ProfitMarginRow(item: ProfitMarginItem, modifier: Modifier = Modifier) {
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
fun InsightCard(insight: InsightItem, modifier: Modifier = Modifier) {
    ModernInsightCard(
        icon = insight.icon,
        title = insight.title,
        description = insight.description,
        type = insight.type,
        modifier = modifier
    )
}

@Composable
fun AnalyticsSectionHeader(title: String, modifier: Modifier = Modifier) {
    AnalyticsSectionTitle(title = title, modifier = modifier)
}
