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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dukaai.data.local.entity.ProductEntity
import com.example.dukaai.ui.components.StockBadge
import com.example.dukaai.ui.components.StockStatus
import com.example.dukaai.ui.viewmodel.ProductViewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction

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
    // Load product data from ViewModel
    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    val product by viewModel.selectedProduct.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showRestockDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Show loading state
    if (isLoading && product == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Show error state
    if (error != null) {
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
                    text = error ?: "Unknown error",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(onClick = { viewModel.loadProduct(productId) }) {
                    Text("Retry")
                }
            }
        }
        return
    }

    // Show empty state if product not found
    if (product == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Product not found",
                style = MaterialTheme.typography.titleMedium
            )
        }
        return
    }

    val currentProduct = product!!
    val stockStatus = when {
        currentProduct.currentStock == 0 -> StockStatus.OUT
        currentProduct.currentStock <= currentProduct.minStockThreshold -> StockStatus.LOW
        else -> StockStatus.OK
    }

    val profitMargin = if (currentProduct.buyingPrice > 0) {
        ((currentProduct.sellingPrice - currentProduct.buyingPrice) / currentProduct.buyingPrice * 100).toInt()
    } else {
        0
    }

    // TODO: Activity items will come from InventoryLog once implemented
    val recentActivity = remember {
        listOf(
            ActivityItem("Info", "Activity tracking coming soon", "Pending", Icons.Default.Info)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
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
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Product header
            item {
                ProductHeaderCard(currentProduct, stockStatus, profitMargin)
            }

            // Action buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalButton(
                        onClick = { showRestockDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restock")
                    }

                    OutlinedButton(
                        onClick = { /* TODO: Navigate to QuickSale with this product */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sell")
                    }
                }
            }

            // Stock info
            item {
                StockInfoCard(currentProduct)
            }

            // Price info
            item {
                PriceInfoCard(currentProduct, profitMargin)
            }

            // Sales performance - Coming soon
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sales Performance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Sales tracking coming soon",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Recent activity
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

    // Restock dialog
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

    // Edit dialog
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

@Composable
private fun ProductHeaderCard(
    product: ProductEntity,
    stockStatus: StockStatus,
    profitMargin: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { },
                    label = { Text(product.category) }
                )

                StockBadge(
                    stockStatus = stockStatus,
                    currentStock = product.currentStock
                )
            }

            if (product.barcode != null && product.barcode!!.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Barcode: ${product.barcode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun StockInfoCard(
    product: ProductEntity,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "STOCK INFORMATION",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(label = "Current Stock", value = "${product.currentStock} units")
                InfoItem(label = "Min. Threshold", value = "${product.minStockThreshold} units")
            }

            Spacer(modifier = Modifier.height(12.dp))

            val stockValue = product.currentStock * product.buyingPrice
            InfoItem(
                label = "Stock Value",
                value = "K ${String.format("%.2f", stockValue)}"
            )
        }
    }
}

@Composable
private fun PriceInfoCard(
    product: ProductEntity,
    profitMargin: Int,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "PRICING",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Selling Price",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "K ${String.format("%.2f", product.sellingPrice)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Buying Price",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "K ${String.format("%.2f", product.buyingPrice)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profit per unit: K ${String.format("%.2f", product.sellingPrice - product.buyingPrice)}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (profitMargin >= 25) {
                        com.example.dukaai.ui.theme.SuccessGreen.copy(alpha = 0.2f)
                    } else {
                        com.example.dukaai.ui.theme.WarningYellow.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = "$profitMargin% margin",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (profitMargin >= 25) {
                            com.example.dukaai.ui.theme.SuccessGreen
                        } else {
                            com.example.dukaai.ui.theme.WarningYellow
                        },
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ActivityItemCard(
    activity: ActivityItem,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = activity.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = activity.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Text(
                text = activity.time,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun RestockDialog(
    productName: String,
    currentStock: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var quantity by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restock $productName") },
        text = {
            Column {
                Text("Current stock: $currentStock units")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity to add") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    quantity.toIntOrNull()?.let { onConfirm(it) }
                },
                enabled = quantity.toIntOrNull() != null && quantity.toInt() > 0
            ) {
                Text("Restock")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EditProductDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onConfirm: (ProductEntity) -> Unit
) {
    var name by remember { mutableStateOf(product.name) }
    var sellingPrice by remember { mutableStateOf(product.sellingPrice.toString()) }
    var buyingPrice by remember { mutableStateOf(product.buyingPrice.toString()) }
    var minThreshold by remember { mutableStateOf(product.minStockThreshold.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Product") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = sellingPrice,
                    onValueChange = { sellingPrice = it },
                    label = { Text("Selling Price (K)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(
                    value = buyingPrice,
                    onValueChange = { buyingPrice = it },
                    label = { Text("Buying Price (K)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(
                    value = minThreshold,
                    onValueChange = { minThreshold = it },
                    label = { Text("Min. Stock Threshold") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val updatedProduct = product.copy(
                    name = name,
                    sellingPrice = sellingPrice.toDoubleOrNull() ?: product.sellingPrice,
                    buyingPrice = buyingPrice.toDoubleOrNull() ?: product.buyingPrice,
                    minStockThreshold = minThreshold.toIntOrNull() ?: product.minStockThreshold,
                    updatedAt = System.currentTimeMillis()
                )
                onConfirm(updatedProduct)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Data classes
private data class ActivityItem(
    val title: String,
    val description: String,
    val time: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
