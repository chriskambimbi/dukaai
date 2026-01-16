package com.example.dukaai.ui.screens.analytics

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
 * - Segmented control for time period filtering with custom date option
 * - Expandable key metrics with breakdowns
 * - Profit info tooltip explaining calculation
 * - Enhanced line chart with Y-axis labels and comparison line
 * - Interactive data points on chart
 * - AI-powered insights section
 * - Export/Share functionality
 * - Top performing products with progress bars
 * - Category breakdown
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var selectedPeriod by remember { mutableStateOf(TimePeriod.TODAY) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showVoiceInput by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Check if there's data (for empty state demo, we'll always have data)
    val hasData = true

    // Voice command examples for analytics
    val voiceCommandExamples = listOf(
        "Show me yesterday's sales",
        "What's my profit this week?",
        "Show top products",
        "Export report"
    )

    Scaffold(
        containerColor = SlateBackground,
        floatingActionButton = {
            // Voice command FAB
            FloatingActionButton(
                onClick = { showVoiceInput = true },
                containerColor = EmeraldAccent,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice command"
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header with export action
            item {
                AnalyticsHeader(
                    onExportClick = { showExportDialog = true }
                )
            }

            // Period Selector with Custom Date option
            item {
                ModernPeriodSelector(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { selectedPeriod = it },
                    onCustomDateClick = { showDatePicker = true },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            if (hasData) {
                // Key Metrics Section with expandable cards
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    EnhancedKeyMetricsSection(selectedPeriod)
                }

                // Revenue Trend Chart with comparison
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    EnhancedRevenueTrendSection(
                        selectedPeriod = selectedPeriod,
                        onExportClick = { showExportDialog = true }
                    )
                }

                // AI Insights Section
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    AIInsightsSection(selectedPeriod)
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

                // Standard Insights Section
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    InsightsSection()
                }

                // Export Button
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    ExportReportButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
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

    // Date Picker Dialog (placeholder - would integrate with DatePicker)
    if (showDatePicker) {
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text("Select Date Range", fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    Text(
                        "Custom date range selection coming soon.",
                        color = SlateTextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "For now, use the preset periods: Today, This Week, or This Month.",
                        color = SlateTextSecondary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Got it", color = EmeraldAccent)
                }
            },
            containerColor = SlateSurface
        )
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Report", fontWeight = FontWeight.SemiBold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ExportOption(
                        icon = Icons.Outlined.Share,
                        title = "Share Summary",
                        description = "Share key metrics via WhatsApp or email",
                        onClick = {
                            val shareText = buildShareText(selectedPeriod)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Report"))
                            showExportDialog = false
                        }
                    )

                    ExportOption(
                        icon = Icons.Outlined.PictureAsPdf,
                        title = "Download PDF",
                        description = "Save detailed report as PDF",
                        onClick = {
                            // PDF export logic would go here
                            showExportDialog = false
                        }
                    )

                    ExportOption(
                        icon = Icons.Outlined.TableChart,
                        title = "Export CSV",
                        description = "Download raw data for spreadsheets",
                        onClick = {
                            // CSV export logic would go here
                            showExportDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancel", color = SlateTextSecondary)
                }
            },
            containerColor = SlateSurface
        )
    }

    // Voice Input Dialog
    VoiceInputDialog(
        isVisible = showVoiceInput,
        onDismiss = { showVoiceInput = false },
        onResult = { spokenText ->
            // Parse voice command and take action
            val command = parseAnalyticsVoiceCommand(spokenText)
            when (command?.first) {
                "show_period" -> {
                    when (command.second["period"]) {
                        "yesterday" -> selectedPeriod = TimePeriod.THIS_WEEK // closest match
                        "today" -> selectedPeriod = TimePeriod.TODAY
                        "last_week", "this_week" -> selectedPeriod = TimePeriod.THIS_WEEK
                        "last_month", "this_month" -> selectedPeriod = TimePeriod.THIS_MONTH
                    }
                }
                "export" -> showExportDialog = true
                // Other commands can be handled here
            }
        },
        title = "Ask about your sales",
        hint = "Try: 'Show yesterday's sales'",
        exampleCommands = voiceCommandExamples
    )
}

@Composable
private fun AnalyticsHeader(
    onExportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
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

        IconButton(
            onClick = onExportClick,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(SlateSurfaceVariant)
        ) {
            Icon(
                imageVector = Icons.Outlined.FileDownload,
                contentDescription = "Export",
                tint = SlateTextPrimary
            )
        }
    }
}

@Composable
private fun EnhancedKeyMetricsSection(period: TimePeriod) {
    val (revenue, profit, orders, avgSale) = when (period) {
        TimePeriod.TODAY -> MetricsData(456.0, 120.0, 32, 14.25)
        TimePeriod.THIS_WEEK -> MetricsData(3250.0, 780.0, 156, 20.83)
        TimePeriod.THIS_MONTH -> MetricsData(13500.0, 3200.0, 645, 20.93)
        TimePeriod.CUSTOM -> MetricsData(5000.0, 1200.0, 240, 20.83)
    }

    // Revenue breakdown data
    val revenueBreakdown = listOf(
        MetricBreakdown("Cash Sales", "K ${formatValue(revenue * 0.65)}", 0.65f),
        MetricBreakdown("Credit Sales", "K ${formatValue(revenue * 0.35)}", 0.35f)
    )

    // Profit breakdown data
    val profitBreakdown = listOf(
        MetricBreakdown("Gross Margin", "K ${formatValue(profit * 1.2)}", null),
        MetricBreakdown("Operating Costs", "- K ${formatValue(profit * 0.2)}", null),
        MetricBreakdown("Net Profit", "K ${formatValue(profit)}", null)
    )

    // Orders breakdown data
    val ordersBreakdown = listOf(
        MetricBreakdown("Completed", "${(orders * 0.9).toInt()} orders", 0.9f),
        MetricBreakdown("Pending", "${(orders * 0.07).toInt()} orders", 0.07f),
        MetricBreakdown("Cancelled", "${(orders * 0.03).toInt()} orders", 0.03f)
    )

    Column(
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        // First row: Revenue & Profit
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ExpandableMetricCard(
                title = "Revenue",
                value = "K ${formatValue(revenue)}",
                change = "+18% vs ${getPreviousPeriodLabel(period)}",
                isPositive = true,
                icon = Icons.Outlined.AccountBalanceWallet,
                iconColor = EmeraldAccent,
                breakdown = revenueBreakdown,
                modifier = Modifier.weight(1f)
            )

            ExpandableMetricCard(
                title = "Profit",
                value = "K ${formatValue(profit)}",
                change = "+12%",
                isPositive = true,
                icon = Icons.Outlined.TrendingUp,
                iconColor = InfoBlue,
                breakdown = profitBreakdown,
                infoTooltip = "Profit = Revenue - Cost of Goods Sold - Operating Expenses. Margin: ${String.format("%.1f", (profit / revenue) * 100)}%",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Second row: Orders & Avg Sale
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ExpandableMetricCard(
                title = "Orders",
                value = orders.toString(),
                change = "+8%",
                isPositive = true,
                icon = Icons.Outlined.ShoppingBag,
                iconColor = WarningYellow,
                breakdown = ordersBreakdown,
                modifier = Modifier.weight(1f)
            )

            ExpandableMetricCard(
                title = "Avg. Sale",
                value = "K ${formatValue(avgSale)}",
                change = "-2%",
                isPositive = false,
                icon = Icons.Outlined.Receipt,
                iconColor = SlateTextSecondary,
                infoTooltip = "Average sale value = Total Revenue ÷ Number of Orders",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EnhancedRevenueTrendSection(
    selectedPeriod: TimePeriod,
    onExportClick: () -> Unit
) {
    // Chart data with comparison values for previous period
    val chartData = when (selectedPeriod) {
        TimePeriod.TODAY -> listOf(
            ChartDataPoint("8AM", 45f, 38f),
            ChartDataPoint("10AM", 78f, 65f),
            ChartDataPoint("12PM", 120f, 95f),
            ChartDataPoint("2PM", 95f, 110f),
            ChartDataPoint("4PM", 68f, 72f),
            ChartDataPoint("6PM", 50f, 45f)
        )
        TimePeriod.THIS_WEEK -> listOf(
            ChartDataPoint("Mon", 420f, 380f),
            ChartDataPoint("Tue", 380f, 410f),
            ChartDataPoint("Wed", 550f, 490f),
            ChartDataPoint("Thu", 480f, 520f),
            ChartDataPoint("Fri", 620f, 580f),
            ChartDataPoint("Sat", 520f, 450f),
            ChartDataPoint("Sun", 280f, 320f)
        )
        TimePeriod.THIS_MONTH, TimePeriod.CUSTOM -> listOf(
            ChartDataPoint("W1", 2800f, 2500f),
            ChartDataPoint("W2", 3400f, 3100f),
            ChartDataPoint("W3", 3100f, 3300f),
            ChartDataPoint("W4", 4200f, 3800f)
        )
    }

    EnhancedRevenueTrendCard(
        dataPoints = chartData,
        showComparison = true,
        onExportClick = onExportClick,
        modifier = Modifier.padding(horizontal = 20.dp)
    )
}

@Composable
private fun AIInsightsSection(selectedPeriod: TimePeriod) {
    val insights = when (selectedPeriod) {
        TimePeriod.TODAY -> listOf(
            AIInsightData(
                title = "Peak Sales Hour",
                description = "Your best selling time is 12PM-2PM. Consider running promotions during slower hours (8-10AM) to boost sales.",
                metric = "K 120",
                metricLabel = "at 12PM"
            ),
            AIInsightData(
                title = "Top Revenue Driver",
                description = "Coca-Cola 500ml accounts for 23% of today's revenue. Stock up to avoid running out.",
                metric = "23%",
                metricLabel = "of revenue"
            )
        )
        TimePeriod.THIS_WEEK -> listOf(
            AIInsightData(
                title = "Best Performing Day",
                description = "Friday generates the most revenue. Consider special weekend promotions to maintain momentum.",
                metric = "K 620",
                metricLabel = "on Friday"
            ),
            AIInsightData(
                title = "Growth Opportunity",
                description = "Sunday sales are 55% lower than Friday. Weekend specials could help boost slow days.",
                metric = "55%",
                metricLabel = "lower"
            )
        )
        TimePeriod.THIS_MONTH, TimePeriod.CUSTOM -> listOf(
            AIInsightData(
                title = "Monthly Trend",
                description = "Sales increased 50% from Week 1 to Week 4. Your growth trajectory is strong.",
                metric = "+50%",
                metricLabel = "growth"
            ),
            AIInsightData(
                title = "Category Insight",
                description = "Beverages dominate at 42% of sales. Consider expanding your beverage selection.",
                metric = "42%",
                metricLabel = "of sales"
            )
        )
    }

    Column(
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        AnalyticsSectionTitle(
            title = "AI Insights",
            action = {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = EmeraldAccent.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = EmeraldAccent
                        )
                        Text(
                            text = "Powered by AI",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldAccent,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        insights.forEach { insight ->
            AIInsightCard(
                title = insight.title,
                description = insight.description,
                metric = insight.metric,
                metricLabel = insight.metricLabel
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
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
        AnalyticsSectionTitle(title = "Tips & Alerts")

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

@Composable
private fun ExportOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = SlateSurfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(EmeraldAccent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = EmeraldAccent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = SlateTextPrimary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateTextSecondary
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = SlateTextTertiary,
                modifier = Modifier.size(20.dp)
            )
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

private data class AIInsightData(
    val title: String,
    val description: String,
    val metric: String,
    val metricLabel: String
)

private fun formatValue(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        String.format("%.2f", value)
    }
}

private fun buildShareText(period: TimePeriod): String {
    val periodLabel = when (period) {
        TimePeriod.TODAY -> "Today"
        TimePeriod.THIS_WEEK -> "This Week"
        TimePeriod.THIS_MONTH -> "This Month"
        TimePeriod.CUSTOM -> "Custom Period"
    }

    val (revenue, profit, orders) = when (period) {
        TimePeriod.TODAY -> Triple(456.0, 120.0, 32)
        TimePeriod.THIS_WEEK -> Triple(3250.0, 780.0, 156)
        TimePeriod.THIS_MONTH -> Triple(13500.0, 3200.0, 645)
        TimePeriod.CUSTOM -> Triple(5000.0, 1200.0, 240)
    }

    return """
📊 Business Report - $periodLabel

💰 Revenue: K ${String.format("%.2f", revenue)}
📈 Profit: K ${String.format("%.2f", profit)}
🛒 Orders: $orders
📉 Avg Sale: K ${String.format("%.2f", revenue / orders)}

🏆 Top Seller: Coca-Cola 500ml
📦 Category Lead: Beverages (42%)

Generated by DukaAI
    """.trimIndent()
}
