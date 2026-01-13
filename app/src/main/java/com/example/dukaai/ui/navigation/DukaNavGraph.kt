package com.example.dukaai.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.dukaai.ui.screens.dashboard.DashboardScreen
import com.example.dukaai.ui.screens.products.ProductListScreen
import com.example.dukaai.ui.screens.products.ProductDetailScreen
import com.example.dukaai.ui.screens.products.AddProductScreen
import com.example.dukaai.ui.screens.sales.QuickSaleScreen
import com.example.dukaai.ui.screens.credit.CreditLedgerScreen
import com.example.dukaai.ui.screens.credit.CustomerDetailScreen
import com.example.dukaai.ui.screens.credit.AddCustomerScreen
import com.example.dukaai.ui.screens.credit.RecordPaymentScreen
import com.example.dukaai.ui.screens.analytics.AnalyticsScreen
import com.example.dukaai.ui.screens.settings.SettingsScreen
import com.example.dukaai.ui.screens.settings.SyncSettingsScreen
import com.example.dukaai.ui.screens.scanner.CameraScannerScreen
import com.example.dukaai.ui.screens.voice.VoiceCommandScreen

/**
 * Main navigation graph for Duka.AI
 * Defines all routes and screen destinations
 */
@Composable
fun DukaNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        // Dashboard Screen (Home)
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }

        // Products List Screen
        composable(Screen.Products.route) {
            ProductListScreen(navController = navController)
        }

        // Quick Sale Screen
        composable(Screen.QuickSale.route) {
            QuickSaleScreen(navController = navController)
        }

        // Credit Ledger Screen
        composable(Screen.Credit.route) {
            CreditLedgerScreen(navController = navController)
        }

        // Analytics Screen
        composable(Screen.Analytics.route) {
            AnalyticsScreen(navController = navController)
        }

        // Product Detail Screen (with argument)
        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(
                navArgument("productId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailScreen(
                productId = productId,
                navController = navController
            )
        }

        // Add Product Screen
        composable(Screen.AddProduct.route) {
            AddProductScreen(navController = navController)
        }

        // Customer Detail Screen (with argument)
        composable(
            route = Screen.CustomerDetail.route,
            arguments = listOf(
                navArgument("customerId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId") ?: ""
            CustomerDetailScreen(
                customerId = customerId,
                navController = navController
            )
        }

        // Add Customer Screen
        composable(Screen.AddCustomer.route) {
            AddCustomerScreen(navController = navController)
        }

        // Record Payment Screen (with argument)
        composable(
            route = Screen.RecordPayment.route,
            arguments = listOf(
                navArgument("customerId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId") ?: ""
            RecordPaymentScreen(
                customerId = customerId,
                navController = navController
            )
        }

        // Camera Scanner Screen
        composable(Screen.CameraScanner.route) {
            CameraScannerScreen(
                navController = navController,
                onBarcodeScanned = { scannedCode ->
                    // Save the scanned barcode to previous screen's savedStateHandle
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("scanned_barcode", scannedCode)
                }
            )
        }

        // Settings Screen
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }

        // Sync Settings Screen
        composable(Screen.SyncSettings.route) {
            SyncSettingsScreen(navController = navController)
        }

        // Voice Command Screen
        composable(Screen.VoiceCommand.route) {
            VoiceCommandScreen(navController = navController)
        }
    }
}

/**
 * Temporary placeholder screen for development
 * Will be replaced with actual screens
 */
@Composable
private fun PlaceholderScreen(title: String) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.Text(
            text = title,
            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
        )
    }
}
