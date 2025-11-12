package com.example.dukaai.ui.screens.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.dukaai.data.local.entity.ProductEntity
import com.example.dukaai.ui.navigation.Screen
import com.example.dukaai.ui.viewmodel.ProductViewModel

/**
 * Screen for adding a new product
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    navController: NavController,
    viewModel: ProductViewModel = hiltViewModel()
) {
    // Form state
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var buyingPrice by remember { mutableStateOf("") }
    var sellingPrice by remember { mutableStateOf("") }
    var initialStock by remember { mutableStateOf("") }
    var minStockThreshold by remember { mutableStateOf("10") }

    // Validation error states
    var nameError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }
    var buyingPriceError by remember { mutableStateOf<String?>(null) }
    var sellingPriceError by remember { mutableStateOf<String?>(null) }
    var initialStockError by remember { mutableStateOf<String?>(null) }

    // Success dialog state
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Observe loading and error states
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Handle scanned barcode result from CameraScannerScreen
    LaunchedEffect(navController) {
        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
        savedStateHandle?.getLiveData<String>("scanned_barcode")?.observeForever { scannedCode ->
            if (scannedCode != null) {
                barcode = scannedCode
                // Clear the result so it doesn't trigger again
                savedStateHandle.remove<String>("scanned_barcode")
            }
        }
    }

    // Category suggestions
    val categorySuggestions = listOf(
        "Beverages",
        "Food & Snacks",
        "Dairy Products",
        "Personal Care",
        "Household Items",
        "Electronics",
        "Clothing",
        "Stationery",
        "Other"
    )
    var showCategoryMenu by remember { mutableStateOf(false) }

    // Validation function
    fun validateForm(): Boolean {
        var isValid = true

        // Name validation
        if (name.isBlank()) {
            nameError = "Product name is required"
            isValid = false
        } else if (name.length < 3) {
            nameError = "Name must be at least 3 characters"
            isValid = false
        } else {
            nameError = null
        }

        // Category validation
        if (category.isBlank()) {
            categoryError = "Category is required"
            isValid = false
        } else {
            categoryError = null
        }

        // Buying price validation
        val buyingPriceValue = buyingPrice.toDoubleOrNull()
        if (buyingPrice.isBlank()) {
            buyingPriceError = "Buying price is required"
            isValid = false
        } else if (buyingPriceValue == null || buyingPriceValue <= 0) {
            buyingPriceError = "Buying price must be greater than 0"
            isValid = false
        } else {
            buyingPriceError = null
        }

        // Selling price validation
        val sellingPriceValue = sellingPrice.toDoubleOrNull()
        if (sellingPrice.isBlank()) {
            sellingPriceError = "Selling price is required"
            isValid = false
        } else if (sellingPriceValue == null || sellingPriceValue <= 0) {
            sellingPriceError = "Selling price must be greater than 0"
            isValid = false
        } else if (buyingPriceValue != null && sellingPriceValue < buyingPriceValue) {
            sellingPriceError = "Selling price should be greater than buying price"
            isValid = false
        } else {
            sellingPriceError = null
        }

        // Initial stock validation
        val stockValue = initialStock.toIntOrNull()
        if (initialStock.isBlank()) {
            initialStockError = "Initial stock is required"
            isValid = false
        } else if (stockValue == null || stockValue < 0) {
            initialStockError = "Stock must be 0 or greater"
            isValid = false
        } else {
            initialStockError = null
        }

        return isValid
    }

    // Handle save
    fun saveProduct() {
        if (validateForm()) {
            val product = ProductEntity(
                name = name.trim(),
                category = category.trim(),
                barcode = barcode.trim().takeIf { it.isNotBlank() },
                buyingPrice = buyingPrice.toDouble(),
                sellingPrice = sellingPrice.toDouble(),
                currentStock = initialStock.toInt(),
                minStockThreshold = minStockThreshold.toIntOrNull() ?: 10
            )
            viewModel.addProduct(product)
            showSuccessDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Product") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Product name field
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = null
                },
                label = { Text("Product Name *") },
                placeholder = { Text("e.g., Coca Cola 500ml") },
                leadingIcon = {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null)
                },
                isError = nameError != null,
                supportingText = {
                    if (nameError != null) {
                        Text(
                            text = nameError!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Category field with dropdown
            ExposedDropdownMenuBox(
                expanded = showCategoryMenu,
                onExpandedChange = { showCategoryMenu = !showCategoryMenu }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {
                        category = it
                        categoryError = null
                    },
                    label = { Text("Category *") },
                    placeholder = { Text("Select or type category") },
                    leadingIcon = {
                        Icon(Icons.Default.Category, contentDescription = null)
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu)
                    },
                    isError = categoryError != null,
                    supportingText = {
                        if (categoryError != null) {
                            Text(
                                text = categoryError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    singleLine = true
                )

                ExposedDropdownMenu(
                    expanded = showCategoryMenu,
                    onDismissRequest = { showCategoryMenu = false }
                ) {
                    categorySuggestions.forEach { suggestion ->
                        DropdownMenuItem(
                            text = { Text(suggestion) },
                            onClick = {
                                category = suggestion
                                showCategoryMenu = false
                            }
                        )
                    }
                }
            }

            // Barcode field (optional)
            OutlinedTextField(
                value = barcode,
                onValueChange = { barcode = it },
                label = { Text("Barcode (Optional)") },
                placeholder = { Text("Scan or enter barcode") },
                leadingIcon = {
                    Icon(Icons.Default.QrCode, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = {
                        navController.navigate(Screen.CameraScanner.route)
                    }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Scan")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Pricing section
            Text(
                text = "Pricing Information",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Buying price
                OutlinedTextField(
                    value = buyingPrice,
                    onValueChange = {
                        buyingPrice = it
                        buyingPriceError = null
                    },
                    label = { Text("Buying Price *") },
                    placeholder = { Text("0.00") },
                    leadingIcon = {
                        Text(
                            "K",
                            modifier = Modifier.padding(start = 12.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    isError = buyingPriceError != null,
                    supportingText = {
                        if (buyingPriceError != null) {
                            Text(
                                text = buyingPriceError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                // Selling price
                OutlinedTextField(
                    value = sellingPrice,
                    onValueChange = {
                        sellingPrice = it
                        sellingPriceError = null
                    },
                    label = { Text("Selling Price *") },
                    placeholder = { Text("0.00") },
                    leadingIcon = {
                        Text(
                            "K",
                            modifier = Modifier.padding(start = 12.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    isError = sellingPriceError != null,
                    supportingText = {
                        if (sellingPriceError != null) {
                            Text(
                                text = sellingPriceError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            // Calculate profit margin
            val buyingPriceValue = buyingPrice.toDoubleOrNull() ?: 0.0
            val sellingPriceValue = sellingPrice.toDoubleOrNull() ?: 0.0
            if (buyingPriceValue > 0 && sellingPriceValue > buyingPriceValue) {
                val profit = sellingPriceValue - buyingPriceValue
                val margin = (profit / buyingPriceValue) * 100
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Profit per unit: K${"%.2f".format(profit)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Margin: ${"%.1f".format(margin)}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Stock section
            Text(
                text = "Stock Information",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Initial stock
                OutlinedTextField(
                    value = initialStock,
                    onValueChange = {
                        initialStock = it
                        initialStockError = null
                    },
                    label = { Text("Initial Stock *") },
                    placeholder = { Text("0") },
                    leadingIcon = {
                        Icon(Icons.Default.Inventory, contentDescription = null)
                    },
                    isError = initialStockError != null,
                    supportingText = {
                        if (initialStockError != null) {
                            Text(
                                text = initialStockError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Min threshold
                OutlinedTextField(
                    value = minStockThreshold,
                    onValueChange = { minStockThreshold = it },
                    label = { Text("Low Stock Alert") },
                    placeholder = { Text("10") },
                    leadingIcon = {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null)
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            // Note about stock alerts
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "You'll be alerted when stock falls below the low stock threshold",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save button
            Button(
                onClick = { saveProduct() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Product")
                }
            }

            // Error display
            if (error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = error!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }

    // Success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Product Added Successfully") },
            text = { Text("$name has been added to your inventory") },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    navController.navigateUp()
                }) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    // Reset form for adding another product
                    showSuccessDialog = false
                    name = ""
                    category = ""
                    barcode = ""
                    buyingPrice = ""
                    sellingPrice = ""
                    initialStock = ""
                    minStockThreshold = "10"
                    viewModel.clearError()
                }) {
                    Text("Add Another")
                }
            }
        )
    }
}
