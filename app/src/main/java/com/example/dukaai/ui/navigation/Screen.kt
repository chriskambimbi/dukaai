package com.example.dukaai.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Navigation routes for Duka.AI
 * Sealed class ensures type-safety for navigation
 */
sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null
) {
    // Bottom navigation destinations
    object Dashboard : Screen(
        route = "dashboard",
        title = "Dashboard",
        icon = Icons.Default.Home
    )

    object Products : Screen(
        route = "products",
        title = "Products",
        icon = Icons.Default.ShoppingBag
    )

    object QuickSale : Screen(
        route = "quick_sale",
        title = "Sell",
        icon = Icons.Default.Add
    )

    object Credit : Screen(
        route = "credit",
        title = "Credit",
        icon = Icons.Default.AccountBalanceWallet
    )

    object Analytics : Screen(
        route = "analytics",
        title = "Analytics",
        icon = Icons.Default.Analytics
    )

    // Detail screens (not in bottom nav)
    object ProductDetail : Screen(
        route = "product_detail/{productId}",
        title = "Product Details"
    ) {
        fun createRoute(productId: String) = "product_detail/$productId"
    }

    object AddProduct : Screen(
        route = "add_product",
        title = "Add Product"
    )

    object CustomerDetail : Screen(
        route = "customer_detail/{customerId}",
        title = "Customer Details"
    ) {
        fun createRoute(customerId: String) = "customer_detail/$customerId"
    }

    object AddCustomer : Screen(
        route = "add_customer",
        title = "Add Customer"
    )

    object RecordPayment : Screen(
        route = "record_payment/{customerId}",
        title = "Record Payment"
    ) {
        fun createRoute(customerId: String) = "record_payment/$customerId"
    }

    object CameraScanner : Screen(
        route = "camera_scanner",
        title = "Scan Product"
    )

    object Settings : Screen(
        route = "settings",
        title = "Settings",
        icon = Icons.Default.Settings
    )

    object SyncSettings : Screen(
        route = "sync_settings",
        title = "Sync Settings"
    )

    companion object {
        // Bottom navigation items
        val bottomNavItems = listOf(
            Dashboard,
            Products,
            QuickSale,
            Credit,
            Analytics
        )
    }
}
