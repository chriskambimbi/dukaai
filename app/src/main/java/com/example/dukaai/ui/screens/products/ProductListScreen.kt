package com.example.dukaai.ui.screens.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dukaai.ui.components.*
import com.example.dukaai.ui.navigation.Screen

/**
 * Product List Screen
 * Shows all products with search, filter, and sort options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showFilterSheet by remember { mutableStateOf(false) }

    // Sample product data (will be replaced with ViewModel data)
    val sampleProducts = remember {
        listOf(
            ProductItem("1", "Coca-Cola 500ml", "Beverages", 60, 20, 10.0, 8.0),
            ProductItem("2", "Mosi Lager 500ml", "Beverages", 45, 10, 12.0, 10.0),
            ProductItem("3", "Boom Detergent 1kg", "Toiletries", 15, 10, 22.0, 18.0),
            ProductItem("4", "Bread (Loaf)", "Food", 8, 15, 5.0, 4.0),
            ProductItem("5", "Sugar 2kg", "Food", 25, 10, 30.0, 25.0),
            ProductItem("6", "Jikelele Oil 2L", "Cooking Oil", 12, 10, 85.0, 70.0),
            ProductItem("7", "Blue Band 500g", "Food", 18, 10, 35.0, 28.0),
            ProductItem("8", "Fanta Orange 500ml", "Beverages", 3, 20, 10.0, 8.0),
            ProductItem("9", "Omo Detergent 1kg", "Toiletries", 0, 10, 45.0, 38.0),
            ProductItem("10", "Sprite 500ml", "Beverages", 55, 20, 10.0, 8.0)
        )
    }

    // Filter products
    val filteredProducts = sampleProducts.filter { product ->
        val matchesSearch = product.name.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "All" || product.category == selectedCategory
        matchesSearch && matchesCategory
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Products") },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddProduct.route) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Search bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = { /* Search action */ },
                placeholder = "Search products..."
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category filter chips
            CategoryFilterChips(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Products list
            if (filteredProducts.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Inventory,
                    title = "No products found",
                    message = if (searchQuery.isEmpty()) {
                        "Add your first product to get started"
                    } else {
                        "Try adjusting your search or filters"
                    },
                    actionText = if (searchQuery.isEmpty()) "Add Product" else null,
                    onAction = if (searchQuery.isEmpty()) {
                        { navController.navigate(Screen.AddProduct.route) }
                    } else null
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "${filteredProducts.size} products",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    items(filteredProducts) { product ->
                        ProductCard(
                            productName = product.name,
                            category = product.category,
                            currentStock = product.currentStock,
                            minStockThreshold = product.minStockThreshold,
                            sellingPrice = product.sellingPrice,
                            buyingPrice = product.buyingPrice,
                            onClick = {
                                navController.navigate(
                                    Screen.ProductDetail.createRoute(product.id)
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    // Filter bottom sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false }
        ) {
            FilterBottomSheetContent(
                selectedCategory = selectedCategory,
                onCategorySelected = {
                    selectedCategory = it
                    showFilterSheet = false
                }
            )
        }
    }
}

@Composable
private fun CategoryFilterChips(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("All", "Beverages", "Food", "Toiletries", "Cooking Oil")

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
                leadingIcon = if (selectedCategory == category) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null
            )
        }
    }
}

@Composable
private fun FilterBottomSheetContent(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Filter Products",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Category",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        val categories = listOf("All", "Beverages", "Food", "Toiletries", "Cooking Oil", "Soap", "Other")

        categories.forEach { category ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                RadioButton(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Data class for product items
private data class ProductItem(
    val id: String,
    val name: String,
    val category: String,
    val currentStock: Int,
    val minStockThreshold: Int,
    val sellingPrice: Double,
    val buyingPrice: Double
)
