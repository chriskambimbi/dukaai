package com.example.dukaai.ui.screens.sales

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.dukaai.data.local.entity.ProductEntity
import com.example.dukaai.ui.components.SearchBar
import com.example.dukaai.ui.navigation.Screen
import com.example.dukaai.ui.viewmodel.ProductViewModel
import com.example.dukaai.ui.viewmodel.SaleViewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction

/**
 * Quick Sale Screen
 * Fast interface for logging sales with multiple input methods
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSaleScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    productViewModel: ProductViewModel = hiltViewModel(),
    saleViewModel: SaleViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var quantity by remember { mutableStateOf(1) }
    var showConfirmation by remember { mutableStateOf(false) }
    var saleType by remember { mutableStateOf(SaleType.CASH) }
    var showSuccessSnackbar by remember { mutableStateOf(false) }

    // Get products from ViewModel
    val products by productViewModel.products.collectAsState()
    val isLoading by productViewModel.isLoading.collectAsState()
    val saleCompleted by saleViewModel.saleCompleted.collectAsState()
    val saleError by saleViewModel.error.collectAsState()

    // Filter products based on search query
    val filteredProducts = products.filter {
        searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
    }

    // Handle sale completed
    LaunchedEffect(saleCompleted) {
        if (saleCompleted) {
            showSuccessSnackbar = true
            saleViewModel.clearCart()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quick Sale") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Quick action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { navController.navigate(Screen.CameraScanner.route) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scan")
                }

                OutlinedButton(
                    onClick = { /* Voice sale */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Voice")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider()

            Spacer(modifier = Modifier.height(16.dp))

            // Manual selection
            Text(
                text = "MANUAL SELECTION",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Search bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = { /* Search */ },
                placeholder = "Search products..."
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Product list
            if (isLoading && products.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (filteredProducts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) "No products available" else "No products found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredProducts) { product ->
                    ProductQuickSaleCard(
                        product = product,
                        onClick = {
                            selectedProduct = product
                            quantity = 1
                            showConfirmation = true
                        }
                    )
                }
            }
        }
    }

    // Success snackbar
    if (showSuccessSnackbar) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            showSuccessSnackbar = false
        }
    }

    // Sale confirmation dialog
    if (showConfirmation && selectedProduct != null) {
        SaleConfirmationDialog(
            product = selectedProduct!!,
            quantity = quantity,
            saleType = saleType,
            onQuantityChange = { quantity = it },
            onSaleTypeChange = { saleType = it },
            onDismiss = {
                showConfirmation = false
                selectedProduct = null
            },
            onConfirm = {
                // Record the sale
                saleViewModel.addToCart(selectedProduct!!, quantity)
                if (saleType == SaleType.CASH) {
                    saleViewModel.completeCashSale()
                } else {
                    // For credit sales, would need to select customer first
                    // For now, complete as cash
                    saleViewModel.completeCashSale()
                }
                showConfirmation = false
                selectedProduct = null
            }
        )
    }
}

@Composable
private fun ProductQuickSaleCard(
    product: ProductEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = product.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Text(
                        text = "Stock: ${product.currentStock}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (product.currentStock <= product.minStockThreshold) {
                            com.example.dukaai.ui.theme.WarningYellow
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        }
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "K ${String.format("%.2f", product.sellingPrice)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SaleConfirmationDialog(
    product: ProductEntity,
    quantity: Int,
    saleType: SaleType,
    onQuantityChange: (Int) -> Unit,
    onSaleTypeChange: (SaleType) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val totalAmount = product.sellingPrice * quantity
    val remainingStock = product.currentStock - quantity

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Sale") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Product info
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Unit price: K ${String.format("%.2f", product.sellingPrice)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Quantity selector
                Column {
                    Text(
                        text = "Quantity",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = { if (quantity > 1) onQuantityChange(quantity - 1) },
                            enabled = quantity > 1
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                        }

                        Text(
                            text = quantity.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = { if (quantity < product.currentStock) onQuantityChange(quantity + 1) },
                            enabled = quantity < product.currentStock
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                }

                // Sale type selector
                Column {
                    Text(
                        text = "Sale Type",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = saleType == SaleType.CASH,
                            onClick = { onSaleTypeChange(SaleType.CASH) },
                            label = { Text("Cash") },
                            leadingIcon = if (saleType == SaleType.CASH) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )

                        FilterChip(
                            selected = saleType == SaleType.CREDIT,
                            onClick = { onSaleTypeChange(SaleType.CREDIT) },
                            label = { Text("Credit") },
                            leadingIcon = if (saleType == SaleType.CREDIT) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }

                // Summary
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Amount:")
                            Text(
                                text = "K ${String.format("%.2f", totalAmount)}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Stock after sale:")
                            Text(
                                text = "$remainingStock units",
                                color = if (remainingStock <= 10) {
                                    com.example.dukaai.ui.theme.WarningYellow
                                } else {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                }
                            )
                        }
                    }
                }

                // Warning for low stock
                if (remainingStock <= 10) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = com.example.dukaai.ui.theme.WarningYellow,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (remainingStock == 0) "This will deplete stock!" else "Low stock warning",
                            style = MaterialTheme.typography.bodySmall,
                            color = com.example.dukaai.ui.theme.WarningYellow
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = quantity > 0 && quantity <= product.currentStock
            ) {
                Text("Confirm Sale")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private enum class SaleType {
    CASH, CREDIT
}
