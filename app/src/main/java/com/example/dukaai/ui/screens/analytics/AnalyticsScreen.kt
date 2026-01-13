package com.example.dukaai.ui.screens.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dukaai.ui.components.*
import com.example.dukaai.ui.theme.*

/**
 * Analytics Screen - Modern Data Dashboard Design
 *
 * Features:
 * - Clean header with "Business Overview" title
 * - Segmented control for time period filtering
 * - Key metrics row (Revenue, Profit, Orders)
 * - Custom line chart with gradient fill
 * - Top performing products with progress bars
 * - Category breakdown
 * - Insights section
 * - Beautiful empty state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var selectedPeriod by remember { mutableStateOf(TimePeriod.TODAY) }

    // Check if there's data (for empty state demo, we'll always have data)
    val hasData = true

    Scaffold(
        containerColor = SlateBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header
            item {
                AnalyticsHeader()
            }

            // Period Selector
            item {
                ModernPeriodSelector(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { selectedPeriod = it },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            if (hasData) {
                // Key Metrics Section
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    KeyMetricsSection(selectedPeriod)
                }

                // Revenue Trend Chart
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    RevenueTrendSection(selectedPeriod)
                }

                // Top Sellers Section
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    TopSellersSection()
                }

                // Category Breakdown
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    CategoryBreakdownSection()
                }

                // Insights Section
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    InsightsSection()
                }
            } else {
                // Empty State
                item {
                    Spacer(modifier = Modifier.height(60.dp))
                    AnalyticsEmptyState()
                }
            }
        }
    }
}

@Composable
private fun AnalyticsHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Text(
            text = "Business Overview",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = SlateTextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Track your sales performance",
            style = MaterialTheme.typography.bodyMedium,
            color = SlateTextSecondary
        )
    }
}

@Composable
private fun KeyMetricsSection(period: TimePeriod) {
    val (revenue, profit, orders, avgSale) = when (period) {
        TimePeriod.TODAY -> MetricsData(456.0, 120.0, 32, 14.25)
        TimePeriod.THIS_WEEK -> MetricsData(3250.0, 780.0, 156, 20.83)
        TimePeriod.THIS_MONTH -> MetricsData(13500.0, 3200.0, 645, 20.93)
    }

    Column(
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        // First row: Revenue & Profit
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricSummaryCard(
                title = "Revenue",
                value = "K ${formatValue(revenue)}",
                change = "+18% vs ${getPreviousPeriodLabel(period)}",
                isPositive = true,
                icon = Icons.Outlined.AccountBalanceWallet,
                iconColor = EmeraldAccent,
                modifier = Modifier.weight(1f)
            )

            MetricSummaryCard(
                title = "Profit",
                value = "K ${formatValue(profit)}",
                change = "+12%",
                isPositive = true,
                icon = Icons.Outlined.TrendingUp,
                iconColor = InfoBlue,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Second row: Orders & Avg Sale
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricSummaryCard(
                title = "Orders",
                value = orders.toString(),
                change = "+8%",
                isPositive = true,
                icon = Icons.Outlined.ShoppingBag,
                iconColor = WarningYellow,
                modifier = Modifier.weight(1f)
            )

            MetricSummaryCard(
                title = "Avg. Sale",
                value = "K ${formatValue(avgSale)}",
                change = "-2%",
                isPositive = false,
                icon = Icons.Outlined.Receipt,
                iconColor = SlateTextSecondary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RevenueTrendSection(period: TimePeriod) {
    val chartData = when (period) {
        TimePeriod.TODAY -> listOf(
            ChartDataPoint("8AM", 45f),
            ChartDataPoint("10AM", 78f),
            ChartDataPoint("12PM", 120f),
            ChartDataPoint("2PM", 95f),
            ChartDataPoint("4PM", 68f),
            ChartDataPoint("6PM", 50f)
        )
        TimePeriod.THIS_WEEK -> listOf(
            ChartDataPoint("Mon", 420f),
            ChartDataPoint("Tue", 380f),
            ChartDataPoint("Wed", 550f),
            ChartDataPoint("Thu", 480f),
            ChartDataPoint("Fri", 620f),
            ChartDataPoint("Sat", 520f),
            ChartDataPoint("Sun", 280f)
        )
        TimePeriod.THIS_MONTH -> listOf(
            ChartDataPoint("W1", 2800f),
            ChartDataPoint("W2", 3400f),
            ChartDataPoint("W3", 3100f),
            ChartDataPoint("W4", 4200f)
        )
    }

    RevenueTrendCard(
        dataPoints = chartData,
        modifier = Modifier.padding(horizontal = 20.dp)
    )
}

@Composable
private fun TopSellersSection() {
    val topSellers = listOf(
        TopSellerItem("Coca-Cola 500ml", 245, 2450.0, 1),
        TopSellerItem("Mosi Lager 500ml", 189, 2268.0, 2),
        TopSellerItem("Boom Detergent 1kg", 156, 3432.0, 3),
        TopSellerItem("Bread (Loaf)", 234, 1170.0, 4),
        TopSellerItem("Sugar 2kg", 98, 2940.0, 5)
    )

    val maxRevenue = topSellers.maxOfOrNull { it.revenue } ?: 1.0

    Column(
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        AnalyticsSectionTitle(title = "Best Sellers")

        Spacer(modifier = Modifier.height(12.dp))

        topSellers.forEach { seller ->
            TopSellerRow(
                rank = seller.rank,
                productName = seller.name,
                unitsSold = seller.unitsSold,
                revenue = seller.revenue,
                maxRevenue = maxRevenue
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CategoryBreakdownSection() {
    val categories = listOf(
        CategoryData("Beverages", 5680.0, 0.42f, EmeraldAccent),
        CategoryData("Food", 3890.0, 0.29f, InfoBlue),
        CategoryData("Toiletries", 2450.0, 0.18f, WarningYellow),
        CategoryData("Cooking Oil", 1480.0, 0.11f, ErrorRed)
    )

    Card(
        modifier = Modifier
            .padding(horizontal = 20.dp)
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
                text = "Category Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = SlateTextPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            categories.forEach { category ->
                ModernCategoryRow(
                    categoryName = category.name,
                    revenue = category.revenue,
                    percentage = category.percentage,
                    color = category.color
                )
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun InsightsSection() {
    val insights = listOf(
        InsightData(
            icon = Icons.Outlined.Schedule,
            title = "Peak Sales Hour",
            description = "Your busiest time is 12-2pm. Ensure you're well-stocked during this period.",
            type = InsightType.INFO
        ),
        InsightData(
            icon = Icons.Outlined.Inventory2,
            title = "Low Stock Alert",
            description = "8 products are running low. Restock soon to avoid lost sales.",
            type = InsightType.WARNING
        ),
        InsightData(
            icon = Icons.Outlined.Star,
            title = "Best Performer",
            description = "Coca-Cola is your top seller. Consider bulk ordering for better margins.",
            type = InsightType.SUCCESS
        )
    )

    Column(
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        AnalyticsSectionTitle(title = "Insights & Tips")

        Spacer(modifier = Modifier.height(12.dp))

        insights.forEach { insight ->
            ModernInsightCard(
                icon = insight.icon,
                title = insight.title,
                description = insight.description,
                type = insight.type
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// Data classes for this screen
private data class MetricsData(
    val revenue: Double,
    val profit: Double,
    val orders: Int,
    val avgSale: Double
)

private data class CategoryData(
    val name: String,
    val revenue: Double,
    val percentage: Float,
    val color: androidx.compose.ui.graphics.Color
)

private data class InsightData(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val description: String,
    val type: InsightType
)

private fun formatValue(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        String.format("%.2f", value)
    }
}
