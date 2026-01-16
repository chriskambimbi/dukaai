package com.example.dukaai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.dukaai.ui.components.NetworkStatus
import com.example.dukaai.ui.components.OfflineStatusBanner
import com.example.dukaai.ui.components.rememberNetworkStatus
import com.example.dukaai.ui.navigation.BottomNavigationBar
import com.example.dukaai.ui.navigation.DukaNavGraph
import com.example.dukaai.ui.navigation.Screen
import com.example.dukaai.ui.theme.*
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity for Duka.AI
 * Sets up the navigation and theme
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DukaAITheme {
                DukaAIApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DukaAIApp() {
    val navController = rememberNavController()
    var showSellSheet by remember { mutableStateOf(false) }

    // Network status for offline indicator
    val networkStatus by rememberNetworkStatus()
    val isOffline = networkStatus is NetworkStatus.Unavailable

    // Onboarding state for first-time users
    val onboardingState = com.example.dukaai.ui.components.rememberOnboardingState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                onSellClick = { showSellSheet = true }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Offline banner at the top
            OfflineStatusBanner(
                isOffline = isOffline,
                pendingSyncCount = 0 // TODO: Connect to actual pending sync count
            )

            // Main content
            DukaNavGraph(
                navController = navController,
                modifier = Modifier.weight(1f)
            )
        }
    }

    // Quick Actions Bottom Sheet
    if (showSellSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSellSheet = false },
            containerColor = SlateSurface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            QuickActionsSheetContent(
                onDismiss = { showSellSheet = false },
                onQuickSale = {
                    showSellSheet = false
                    navController.navigate(Screen.QuickSale.route)
                },
                onScanBarcode = {
                    showSellSheet = false
                    navController.navigate(Screen.CameraScanner.route)
                },
                onVoiceSale = {
                    showSellSheet = false
                    navController.navigate(Screen.VoiceCommand.route)
                },
                onAddProduct = {
                    showSellSheet = false
                    navController.navigate(Screen.AddProduct.route)
                }
            )
        }
    }

        // Onboarding overlay for first-time users
        com.example.dukaai.ui.components.OnboardingOverlay(
            state = onboardingState,
            onActionClick = { stepId ->
                when (stepId) {
                    "quick_sale" -> {
                        onboardingState.complete()
                        showSellSheet = true
                    }
                    "voice" -> {
                        onboardingState.nextStep()
                    }
                    "analytics" -> {
                        onboardingState.complete()
                        navController.navigate(Screen.Analytics.route)
                    }
                }
            }
        )
    }
}

/**
 * Quick Actions Bottom Sheet Content
 * Provides multiple ways to make a sale
 */
@Composable
private fun QuickActionsSheetContent(
    onDismiss: () -> Unit,
    onQuickSale: () -> Unit,
    onScanBarcode: () -> Unit,
    onVoiceSale: () -> Unit,
    onAddProduct: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        // Header
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = SlateTextPrimary
        )

        Text(
            text = "Choose how you want to make a sale",
            style = MaterialTheme.typography.bodyMedium,
            color = SlateTextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Primary action - Quick Sale (full product list)
        QuickActionCard(
            icon = Icons.Default.ShoppingCart,
            title = "Quick Sale",
            description = "Browse products and record sale",
            containerColor = EmeraldAccent,
            contentColor = Color.White,
            onClick = onQuickSale,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Secondary actions row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Scan Barcode
            QuickActionCard(
                icon = Icons.Default.QrCodeScanner,
                title = "Scan",
                description = "Scan barcode",
                containerColor = SlateSurfaceVariant,
                contentColor = SlateTextPrimary,
                iconColor = EmeraldAccent,
                onClick = onScanBarcode,
                compact = true,
                modifier = Modifier.weight(1f)
            )

            // Voice Sale
            QuickActionCard(
                icon = Icons.Default.Mic,
                title = "Voice",
                description = "Speak to sell",
                containerColor = SlateSurfaceVariant,
                contentColor = SlateTextPrimary,
                iconColor = AccentOrange,
                onClick = onVoiceSale,
                compact = true,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        HorizontalDivider(color = SlateBorder)

        Spacer(modifier = Modifier.height(16.dp))

        // Add new product shortcut
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Product not listed?",
                style = MaterialTheme.typography.bodyMedium,
                color = SlateTextSecondary
            )

            TextButton(onClick = onAddProduct) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Product")
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    title: String,
    description: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconColor: Color = contentColor,
    compact: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        if (compact) {
            // Compact layout for secondary actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.7f)
                )
            }
        } else {
            // Full width layout for primary action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = contentColor.copy(alpha = 0.6f)
                )
            }
        }
    }
}
