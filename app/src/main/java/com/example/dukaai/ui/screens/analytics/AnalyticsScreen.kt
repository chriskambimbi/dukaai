package com.example.dukaai.ui.screens.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dukaai.ui.components.*
import com.example.dukaai.ui.theme.ErrorRed
import com.example.dukaai.ui.theme.SuccessGreen
import com.example.dukaai.ui.theme.WarningYellow

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
            item {
                PeriodSelectorChips(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { selectedPeriod = it }
                )
            }

            item {
                KeyMetricsSection(selectedPeriod)
            }

            item {
                SalesChartCard(selectedPeriod)
            }

            item {
                TopSellersSection()
            }

            item {
                CategoryBreakdownCard()
            }

            item {
                ProfitMarginCard()
            }

            item {
                InsightsSection()
            }
        }
    }
}

@Composable
private fun KeyMetricsSection(period: TimePeriod) {
    val (revenue, profit, transactions) = when (period) {
        TimePeriod.TODAY -> Triple(456.0, 120.0, 32)
        TimePeriod.THIS_WEEK -> Triple(3250.0, 780.0, 156)
        TimePeriod.THIS_MONTH -> Triple(13500.0, 3200.0, 645)
    }

    Column {
        AnalyticsSectionHeader(title = "KEY METRICS")
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Revenue",
                value = "K ${String.format("%.2f", revenue)}",
                change = "+18% vs ${getPreviousPeriodLabel(period)}",
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
private fun SalesChartCard(period: TimePeriod) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            AnalyticsSectionHeader(title = "SALES TREND")
            Spacer(modifier = Modifier.height(16.dp))

            SimpleSalesBarChart(period)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ChartLegendItem(color = MaterialTheme.colorScheme.primary, label = "Sales")
                ChartLegendItem(color = MaterialTheme.colorScheme.secondary, label = "Profit")
            }
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

    Column {
        AnalyticsSectionHeader(title = "TOP SELLERS")
        Spacer(modifier = Modifier.height(12.dp))

        topSellers.forEach { seller ->
            TopSellerCard(seller)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CategoryBreakdownCard() {
    val categories = listOf(
        CategoryItem("Beverages", 5680.0, 0.42f),
        CategoryItem("Food", 3890.0, 0.29f),
        CategoryItem("Toiletries", 2450.0, 0.18f),
        CategoryItem("Cooking Oil", 1480.0, 0.11f)
    )

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            AnalyticsSectionHeader(title = "CATEGORY BREAKDOWN")
            Spacer(modifier = Modifier.height(16.dp))

            categories.forEach { category ->
                CategoryBreakdownRow(category)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ProfitMarginCard() {
    val profitMargins = listOf(
        ProfitMarginItem("High Margin (>25%)", 38, SuccessGreen),
        ProfitMarginItem("Medium (15-25%)", 45, WarningYellow),
        ProfitMarginItem("Low (<15%)", 17, ErrorRed)
    )

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            AnalyticsSectionHeader(title = "PROFIT MARGINS")
            Spacer(modifier = Modifier.height(16.dp))

            profitMargins.forEach { item ->
                ProfitMarginRow(item)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun InsightsSection() {
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

    Column {
        AnalyticsSectionHeader(title = "INSIGHTS & RECOMMENDATIONS")
        Spacer(modifier = Modifier.height(12.dp))

        insights.forEach { insight ->
            InsightCard(insight)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
