package com.example.dukaai.ui.screens.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dukaai.ui.navigation.Screen
import com.example.dukaai.ui.theme.*

/**
 * Dashboard Screen - Modern, minimalistic home screen of Duka.AI
 * Features:
 * - Elegant gradient header with prominent CTA
 * - Quick action buttons (Scan/Voice)
 * - Smart suggestions with pagination dots
 * - Sales summary with comparison context
 * - Low stock & credit alerts
 * - Top selling products with empty state handling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val currentHour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val greeting = when {
        currentHour < 12 -> "Good morning"
        currentHour < 17 -> "Good afternoon"
        else -> "Good evening"
    }

    Scaffold(
        containerColor = SlateBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Modern Header with Gradient
            item {
                DashboardHeader(
                    greeting = greeting,
                    onSettingsClick = { navController.navigate(Screen.Settings.route) },
                    onSearchClick = { navController.navigate(Screen.Products.route) }
                )
            }

            // Primary Action Buttons
            item {
                PrimaryActionsRow(
                    onScanClick = { navController.navigate(Screen.CameraScanner.route) },
                    onVoiceClick = { navController.navigate(Screen.VoiceCommand.route) },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // Smart Suggestions
            item {
                SmartSuggestionsSection(
                    onProductClick = { navController.navigate(Screen.QuickSale.route) },
                    modifier = Modifier.padding(top = 24.dp)
                )
            }

            // Sales Summary Card
            item {
                SalesSummaryCard(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
            }

            // Alerts Row
            item {
                AlertsRow(
                    onLowStockClick = { navController.navigate(Screen.Products.route) },
                    onCreditClick = { navController.navigate(Screen.Credit.route) },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // Top Sellers Section
            item {
                TopSellersSection(
                    navController = navController,
                    modifier = Modifier.padding(top = 20.dp)
                )
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    greeting: String,
    onSettingsClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CopperPrimaryDark,
                        CopperPrimary,
                        CopperPrimaryLight.copy(alpha = 0.8f)
                    )
                )
            )
            .padding(bottom = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Top Row: Logo & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Duka.AI",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Greeting (smaller, secondary)
            Text(
                text = greeting,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Primary CTA (prominent)
            Text(
                text = "Ready to make a sale?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun PrimaryActionsRow(
    onScanClick: () -> Unit,
    onVoiceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .offset(y = (-24).dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Scan Button - Primary CTA (simplified label)
        PrimaryActionButton(
            icon = Icons.Default.QrCodeScanner,
            label = "Scan Barcode",
            onClick = onScanClick,
            containerColor = ZambianGreen,
            modifier = Modifier.weight(1f)
        )

        // Voice Button (simplified label)
        PrimaryActionButton(
            icon = Icons.Default.Mic,
            label = "Voice Sale",
            onClick = onVoiceClick,
            containerColor = AccentOrange,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PrimaryActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .height(80.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = containerColor.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun SmartSuggestionsSection(
    onProductClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentHour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }

    // Time-based suggestions - exactly 2 items to show fully
    val suggestions = remember(currentHour) {
        when {
            currentHour < 10 -> listOf(
                SuggestedProduct("Bread", "K 5", StockStatus.OK, 45),
                SuggestedProduct("Milk 500ml", "K 12", StockStatus.LOW, 8)
            )
            currentHour < 14 -> listOf(
                SuggestedProduct("Coca-Cola", "K 12", StockStatus.OK, 36),
                SuggestedProduct("Fanta Orange", "K 12", StockStatus.OK, 28)
            )
            else -> listOf(
                SuggestedProduct("Mosi Lager", "K 15", StockStatus.OK, 48),
                SuggestedProduct("Castle Lite", "K 18", StockStatus.LOW, 6)
            )
        }
    }

    val timeLabel = when {
        currentHour < 10 -> "Morning favorites"
        currentHour < 14 -> "Lunchtime picks"
        currentHour < 17 -> "Afternoon favorites"
        else -> "Evening essentials"
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = AccentOrange
                    )
                    Text(
                        text = "SMART SUGGESTIONS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SlateTextSecondary, // Better contrast
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = timeLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = SlateTextPrimary // Explicit dark color
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (suggestions.isEmpty()) {
            // Empty state
            EmptySuggestionsState(
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        } else {
            // Show exactly 2 full cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                suggestions.take(2).forEach { product ->
                    SuggestedProductCard(
                        product = product,
                        onClick = onProductClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptySuggestionsState(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Lightbulb,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = SlateTextTertiary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No suggestions yet",
                style = MaterialTheme.typography.bodyMedium,
                color = SlateTextSecondary
            )
            Text(
                text = "Start selling to get smart recommendations",
                style = MaterialTheme.typography.bodySmall,
                color = SlateTextTertiary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SuggestedProductCard(
    product: SuggestedProduct,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = SlateSurface // Explicit white background
        ),
        border = BorderStroke(1.dp, SlateBorder), // Subtle border for definition
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp) // Subtle shadow
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Product image placeholder with stock badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Inventory2,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )

                // Stock badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (product.stockStatus) {
                                StockStatus.OK -> StockOk.copy(alpha = 0.9f)
                                StockStatus.LOW -> StockLow.copy(alpha = 0.9f)
                                StockStatus.OUT -> StockOut.copy(alpha = 0.9f)
                            }
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${product.stock}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = SlateTextPrimary, // Explicit dark color for contrast
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = product.price,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = EmeraldAccent
            )
        }
    }
}

@Composable
private fun SalesSummaryCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = SlateSurface // Explicit white background
        ),
        border = BorderStroke(1.dp, SlateBorder), // Subtle border for definition
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp) // Subtle shadow
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
                    text = "Today's Sales",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = SlateTextPrimary // Explicit dark color
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SuccessGreen.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = SuccessGreen
                        )
                        Text(
                            text = "+18%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main amount with comparison
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "K 456.00",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = SlateTextPrimary // Explicit dark color
                )
                Text(
                    text = "vs K 387 yesterday",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateTextSecondary, // Better contrast
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Transactions",
                    value = "32",
                    icon = Icons.Outlined.Receipt
                )
                StatItem(
                    label = "Profit",
                    value = "K 120",
                    icon = Icons.Outlined.Payments
                )
                StatItem(
                    label = "Items Sold",
                    value = "87",
                    icon = Icons.Outlined.ShoppingCart
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = SlateTextSecondary // Better contrast
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = SlateTextPrimary // Explicit dark color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = SlateTextSecondary // Better contrast
        )
    }
}

@Composable
private fun AlertsRow(
    onLowStockClick: () -> Unit,
    onCreditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Low Stock Alert
        AlertCard(
            icon = Icons.Default.Inventory,
            title = "5 Low Stock",
            subtitle = "Items need restock",
            alertColor = WarningYellow,
            onClick = onLowStockClick,
            modifier = Modifier.weight(1f)
        )

        // Credit Alert
        AlertCard(
            icon = Icons.Default.CreditCard,
            title = "K 2,450",
            subtitle = "Outstanding credit",
            alertColor = InfoBlue,
            onClick = onCreditClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AlertCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    alertColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = alertColor.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(alertColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = alertColor
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun TopSellersSection(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // Top sellers data - could be empty
    val topSellers = listOf(
        TopSeller("Coca-Cola 500ml", 24, "K 288", StockStatus.OK, 36),
        TopSeller("Bread (loaf)", 18, "K 90", StockStatus.OK, 22)
    )

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Top Sellers Today",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = SlateTextPrimary // Explicit dark color
            )
            TextButton(onClick = { navController.navigate(Screen.Analytics.route) }) {
                Text(
                    text = "View All",
                    color = EmeraldAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (topSellers.isEmpty()) {
            // Empty state for no top sellers
            EmptyTopSellersState(
                onStartSelling = { navController.navigate(Screen.QuickSale.route) },
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        } else {
            // Show exactly 2 full cards side by side
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                topSellers.take(2).forEach { seller ->
                    TopSellerCard(
                        topSeller = seller,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyTopSellersState(
    onStartSelling: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(SlateSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.TrendingUp,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = SlateTextTertiary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No sales today yet",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = SlateTextPrimary
            )
            Text(
                text = "Your top sellers will appear here",
                style = MaterialTheme.typography.bodySmall,
                color = SlateTextTertiary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onStartSelling,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldAccent
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Make a Sale")
            }
        }
    }
}

@Composable
private fun TopSellerCard(
    topSeller: TopSeller,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = SlateSurface // Explicit white background
        ),
        border = BorderStroke(1.dp, SlateBorder), // Subtle border for definition
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp) // Subtle shadow
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Product image with badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Inventory2,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )

                // Stock badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (topSeller.stockStatus) {
                                StockStatus.OK -> StockOk.copy(alpha = 0.9f)
                                StockStatus.LOW -> StockLow.copy(alpha = 0.9f)
                                StockStatus.OUT -> StockOut.copy(alpha = 0.9f)
                            }
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${topSeller.stock} left",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Sold badge
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(SuccessGreen.copy(alpha = 0.9f))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        modifier = Modifier.size(10.dp),
                        tint = Color.White
                    )
                    Text(
                        text = "${topSeller.quantity} sold",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = topSeller.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = SlateTextPrimary, // Explicit dark color for contrast
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = topSeller.revenue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = EmeraldAccent
            )
        }
    }
}

// Data classes
private enum class StockStatus { OK, LOW, OUT }

private data class SuggestedProduct(
    val name: String,
    val price: String,
    val stockStatus: StockStatus,
    val stock: Int
)

private data class TopSeller(
    val name: String,
    val quantity: Int,
    val revenue: String,
    val stockStatus: StockStatus,
    val stock: Int
)
