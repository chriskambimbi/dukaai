package com.example.dukaai.ui.screens.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dukaai.ui.components.StatCard

/**
 * Analytics Screen
 * Shows sales reports, insights, and business analytics
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var selectedPeriod by remember { mutableStateOf(TimePeriod.TODAY) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Period selector
            item {
                PeriodSelectorChips(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { selectedPeriod = it }
                )
            }

            // Key metrics cards
            item {
                KeyMetricsSection(selectedPeriod)
            }

            // Sales chart
            item {
                SalesChartCard(selectedPeriod)
            }

            // Top sellers
            item {
                TopSellersSection()
            }

            // Category breakdown
            item {
                CategoryBreakdownSection()
            }

            // Profit margin analysis
            item {
                ProfitMarginSection()
            }

            // Insights and recommendations
            item {
                InsightsSection()
            }
        }
    }
}

@Composable
private fun PeriodSelectorChips(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(TimePeriod.values().size) { index ->
            val period = TimePeriod.values()[index]
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

@Composable
private fun KeyMetricsSection(
    period: TimePeriod,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "KEY METRICS",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Sample data based on period
        val (revenue, profit, transactions) = when (period) {
            TimePeriod.TODAY -> Triple(456.0, 120.0, 32)
            TimePeriod.THIS_WEEK -> Triple(3250.0, 780.0, 156)
            TimePeriod.THIS_MONTH -> Triple(13500.0, 3200.0, 645)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Revenue",
                value = "K ${String.format("%.2f", revenue)}",
                change = "+18% vs ${getPreviousPeriod(period)}",
                isPositive = true,
                icon = Icons.Default.TrendingUp,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Profit",
                value = "K ${String.format("%.2f", profit)}",
                change = "+12%",
                isPositive = true,
                icon = Icons.Default.AttachMoney,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Transactions",
                value = transactions.toString(),
                change = "+5%",
                isPositive = true,
                icon = Icons.Default.ShoppingCart,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Avg. Sale",
                value = "K ${String.format("%.2f", revenue / transactions)}",
                change = "-2%",
                isPositive = false,
                icon = Icons.Default.Receipt,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SalesChartCard(
    period: TimePeriod,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "SALES TREND",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Simple bar chart visualization (placeholder)
            // In a real app, you'd use a charting library like Vico or MPAndroidChart
            SimpleSalesBarChart(period)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                LegendItem(color = MaterialTheme.colorScheme.primary, label = "Sales")
                LegendItem(color = MaterialTheme.colorScheme.secondary, label = "Profit")
            }
        }
    }
}

@Composable
private fun SimpleSalesBarChart(
    period: TimePeriod,
    modifier: Modifier = Modifier
) {
    // Simple visualization using basic composables
    // This is a placeholder - use a proper charting library for production
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val labels = when (period) {
            TimePeriod.TODAY -> listOf("8AM", "10AM", "12PM", "2PM", "4PM", "6PM")
            TimePeriod.THIS_WEEK -> listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            TimePeriod.THIS_MONTH -> listOf("W1", "W2", "W3", "W4")
        }

        val values = listOf(0.6f, 0.8f, 1.0f, 0.7f, 0.5f, 0.9f)

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
                    // Bar
                    Surface(
                        modifier = Modifier
                            .width(32.dp)
                            .height((150 * value).dp),
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small
                    ) {}

                    Spacer(modifier = Modifier.height(4.dp))

                    // Label
                    Text(
                        text = labels.getOrElse(index) { "" },
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: androidx.compose.ui.graphics.Color,
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

@Composable
private fun TopSellersSection(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TOP SELLERS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        val topSellers = listOf(
            TopSellerItem("Coca-Cola 500ml", 245, 2450.0, 1),
            TopSellerItem("Mosi Lager 500ml", 189, 2268.0, 2),
            TopSellerItem("Boom Detergent 1kg", 156, 3432.0, 3),
            TopSellerItem("Bread (Loaf)", 234, 1170.0, 4),
            TopSellerItem("Sugar 2kg", 98, 2940.0, 5)
        )

        topSellers.forEach { seller ->
            TopSellerCard(seller)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TopSellerCard(
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
                // Rank badge
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = when (seller.rank) {
                        1 -> com.example.dukaai.ui.theme.WarningYellow.copy(alpha = 0.2f)
                        2 -> MaterialTheme.colorScheme.secondaryContainer
                        3 -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "#${seller.rank}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

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
private fun CategoryBreakdownSection(
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "CATEGORY BREAKDOWN",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            val categories = listOf(
                CategoryItem("Beverages", 5680.0, 0.42f),
                CategoryItem("Food", 3890.0, 0.29f),
                CategoryItem("Toiletries", 2450.0, 0.18f),
                CategoryItem("Cooking Oil", 1480.0, 0.11f)
            )

            categories.forEach { category ->
                CategoryBreakdownRow(category)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun CategoryBreakdownRow(
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

@Composable
private fun ProfitMarginSection(
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "PROFIT MARGINS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            val profitMargins = listOf(
                ProfitMarginItem("High Margin (>25%)", 38, com.example.dukaai.ui.theme.SuccessGreen),
                ProfitMarginItem("Medium (15-25%)", 45, com.example.dukaai.ui.theme.WarningYellow),
                ProfitMarginItem("Low (<15%)", 17, com.example.dukaai.ui.theme.ErrorRed)
            )

            profitMargins.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
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

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun InsightsSection(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "INSIGHTS & RECOMMENDATIONS",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        val insights = listOf(
            InsightItem(
                icon = Icons.Default.TrendingUp,
                title = "Peak Sales Hour",
                description = "Your busiest time is 12-2pm. Ensure you're well-stocked during this period.",
                type = InsightType.INFO
            ),
            InsightItem(
                icon = Icons.Default.Warning,
                title = "Low Stock Alert",
                description = "8 products are running low. Restock soon to avoid lost sales.",
                type = InsightType.WARNING
            ),
            InsightItem(
                icon = Icons.Default.Star,
                title = "Best Performer",
                description = "Coca-Cola is your top seller. Consider bulk ordering for better margins.",
                type = InsightType.SUCCESS
            )
        )

        insights.forEach { insight ->
            InsightCard(insight)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun InsightCard(
    insight: InsightItem,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (insight.type) {
        InsightType.INFO -> MaterialTheme.colorScheme.primaryContainer
        InsightType.WARNING -> com.example.dukaai.ui.theme.WarningYellow.copy(alpha = 0.2f)
        InsightType.SUCCESS -> com.example.dukaai.ui.theme.SuccessGreen.copy(alpha = 0.2f)
    }

    val iconColor = when (insight.type) {
        InsightType.INFO -> MaterialTheme.colorScheme.primary
        InsightType.WARNING -> com.example.dukaai.ui.theme.WarningYellow
        InsightType.SUCCESS -> com.example.dukaai.ui.theme.SuccessGreen
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

// Helper function
private fun getPreviousPeriod(period: TimePeriod): String {
    return when (period) {
        TimePeriod.TODAY -> "yesterday"
        TimePeriod.THIS_WEEK -> "last week"
        TimePeriod.THIS_MONTH -> "last month"
    }
}

// Data classes and enums
private enum class TimePeriod(val label: String) {
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month")
}

private data class TopSellerItem(
    val name: String,
    val unitsSold: Int,
    val revenue: Double,
    val rank: Int
)

private data class CategoryItem(
    val name: String,
    val revenue: Double,
    val percentage: Float
)

private data class ProfitMarginItem(
    val label: String,
    val percentage: Int,
    val color: androidx.compose.ui.graphics.Color
)

private data class InsightItem(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val description: String,
    val type: InsightType
)

private enum class InsightType {
    INFO, WARNING, SUCCESS
}
