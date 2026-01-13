package com.example.dukaai.ui.screens.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dukaai.ui.components.*
import com.example.dukaai.ui.viewmodel.ProductViewModel

/**
 * Product Detail Screen
 * Shows product details with options to edit, restock, and view history
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    navController: NavController,
    viewModel: ProductViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    val product by viewModel.selectedProduct.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showRestockDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Loading state
    if (isLoading && product == null) {
        LoadingState()
        return
    }

    // Error state
    error?.let { errorMessage ->
        ErrorState(
            message = errorMessage,
            onRetry = { viewModel.loadProduct(productId) }
        )
        return
    }

    // Empty state
    val currentProduct = product
    if (currentProduct == null) {
        EmptyState(message = "Product not found")
        return
    }

    // Calculate stock status and profit margin
    val stockStatus = when {
        currentProduct.currentStock == 0 -> StockStatus.OUT
        currentProduct.currentStock <= currentProduct.minStockThreshold -> StockStatus.LOW
        else -> StockStatus.OK
    }

    val profitMargin = if (currentProduct.buyingPrice > 0) {
        ((currentProduct.sellingPrice - currentProduct.buyingPrice) / currentProduct.buyingPrice * 100).toInt()
    } else 0

    Scaffold(
        topBar = {
            ProductDetailTopBar(
                onBackClick = { navController.popBackStack() },
                onEditClick = { showEditDialog = true }
            )
        }
    ) { paddingValues ->
        ProductDetailContent(
            product = currentProduct,
            stockStatus = stockStatus,
            profitMargin = profitMargin,
            onRestockClick = { showRestockDialog = true },
            onSellClick = { /* TODO: Navigate to QuickSale */ },
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        )
    }

    // Dialogs
    if (showRestockDialog) {
        RestockDialog(
            productName = currentProduct.name,
            currentStock = currentProduct.currentStock,
            onDismiss = { showRestockDialog = false },
            onConfirm = { quantity ->
                val newStock = currentProduct.currentStock + quantity
                viewModel.updateStock(currentProduct.id, newStock, "Restock: +$quantity units")
                showRestockDialog = false
            }
        )
    }

    if (showEditDialog) {
        EditProductDialog(
            product = currentProduct,
            onDismiss = { showEditDialog = false },
            onConfirm = { updatedProduct ->
                viewModel.updateProduct(updatedProduct)
                showEditDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductDetailTopBar(
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    TopAppBar(
        title = { Text("Product Details") },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
private fun ProductDetailContent(
    product: com.example.dukaai.data.local.entity.ProductEntity,
    stockStatus: StockStatus,
    profitMargin: Int,
    onRestockClick: () -> Unit,
    onSellClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recentActivity = remember {
        listOf(
            ActivityItem("Info", "Activity tracking coming soon", "Pending", Icons.Default.Info)
        )
    }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ProductHeaderCard(product, stockStatus, profitMargin)
        }

        item {
            ActionButtonsRow(
                onRestockClick = onRestockClick,
                onSellClick = onSellClick
            )
        }

        item {
            StockInfoCard(product)
        }

        item {
            PriceInfoCard(product, profitMargin)
        }

        item {
            ComingSoonCard(
                title = "Sales Performance",
                message = "Sales tracking coming soon"
            )
        }

        item {
            Text(
                text = "RECENT ACTIVITY",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(recentActivity) { activity ->
            ActivityItemCard(activity)
        }
    }
}

@Composable
private fun ActionButtonsRow(
    onRestockClick: () -> Unit,
    onSellClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FilledTonalButton(
            onClick = onRestockClick,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Restock")
        }

        OutlinedButton(
            onClick = onSellClick,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sell")
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Error loading product",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
