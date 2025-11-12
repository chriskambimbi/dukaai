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
import androidx.navigation.NavController
import com.example.dukaai.ui.components.SearchBar
import com.example.dukaai.ui.navigation.Screen
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
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<SaleProduct?>(null) }
    var quantity by remember { mutableStateOf(1) }
    var showConfirmation by remember { mutableStateOf(false) }
    var saleType by remember { mutableStateOf(SaleType.CASH) }

    // Sample products (will be replaced with ViewModel)
    val sampleProducts = remember {
        listOf(
            SaleProduct("1", "Coca-Cola 500ml", "Beverages", 60, 10.0),
            SaleProduct("2", "Mosi Lager 500ml", "Beverages", 45, 12.0),
            SaleProduct("3", "Boom Detergent 1kg", "Toiletries", 15, 22.0),
            SaleProduct("4", "Bread (Loaf)", "Food", 8, 5.0),
            SaleProduct("5", "Sugar 2kg", "Food", 25, 30.0),
            SaleProduct("6", "Fanta Orange 500ml", "Beverages", 55, 10.0),
            SaleProduct("7", "Sprite 500ml", "Beverages", 55, 10.0)
        )
    }

    val filteredProducts = sampleProducts.filter {
        it.name.contains(searchQuery, ignoreCase = true)
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
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                // Handle sale
                showConfirmation = false
                selectedProduct = null
                // Show success message
            }
        )
    }
}

@Composable
private fun ProductQuickSaleCard(
    product: SaleProduct,
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
                        text = "Stock: ${product.stock}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (product.stock <= 10) {
                            com.example.dukaai.ui.theme.WarningYellow
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        }
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "K ${String.format("%.2f", product.price)}",
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
    product: SaleProduct,
    quantity: Int,
    saleType: SaleType,
    onQuantityChange: (Int) -> Unit,
    onSaleTypeChange: (SaleType) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val totalAmount = product.price * quantity
    val remainingStock = product.stock - quantity

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
                            text = "Unit price: K ${String.format("%.2f", product.price)}",
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
                            onClick = { if (quantity < product.stock) onQuantityChange(quantity + 1) },
                            enabled = quantity < product.stock
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
                enabled = quantity > 0 && quantity <= product.stock
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

// Data classes
private data class SaleProduct(
    val id: String,
    val name: String,
    val category: String,
    val stock: Int,
    val price: Double
)

private enum class SaleType {
    CASH, CREDIT
}
