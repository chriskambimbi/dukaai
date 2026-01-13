package com.example.dukaai.ui.screens.products

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dukaai.ui.components.*
import com.example.dukaai.ui.navigation.Screen
import com.example.dukaai.ui.theme.*

/**
 * Product List Screen - Modern, minimalistic design
 * Features:
 * - Clean header without solid colored AppBar
 * - Integrated search with smooth design
 * - Sensitive info toggle (hide cost/margin)
 * - Modern filter chips
 * - Enhanced product cards
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
    var showSensitiveInfo by remember { mutableStateOf(true) }
    var sortOption by remember { mutableStateOf(SortOption.NAME_ASC) }
    val focusManager = LocalFocusManager.current

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

    // Filter and sort products
    val filteredProducts = remember(sampleProducts, searchQuery, selectedCategory, sortOption) {
        sampleProducts
            .filter { product ->
                val matchesSearch = product.name.contains(searchQuery, ignoreCase = true)
                val matchesCategory = selectedCategory == "All" || product.category == selectedCategory
                matchesSearch && matchesCategory
            }
            .let { products ->
                when (sortOption) {
                    SortOption.NAME_ASC -> products.sortedBy { it.name }
                    SortOption.NAME_DESC -> products.sortedByDescending { it.name }
                    SortOption.PRICE_ASC -> products.sortedBy { it.sellingPrice }
                    SortOption.PRICE_DESC -> products.sortedByDescending { it.sellingPrice }
                    SortOption.STOCK_LOW -> products.sortedBy { it.currentStock }
                    SortOption.STOCK_HIGH -> products.sortedByDescending { it.currentStock }
                }
            }
    }

    // Category counts
    val categoryCounts = remember(sampleProducts) {
        sampleProducts.groupingBy { it.category }.eachCount()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddProduct.route) },
                containerColor = CopperPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header section
            item {
                ProductsHeader(
                    showSensitiveInfo = showSensitiveInfo,
                    onToggleSensitiveInfo = { showSensitiveInfo = !showSensitiveInfo },
                    onSortClick = { showFilterSheet = true }
                )
            }

            // Search bar
            item {
                ModernSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { focusManager.clearFocus() },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // Category filter chips
            item {
                Spacer(modifier = Modifier.height(16.dp))
                CategoryFilterChips(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it },
                    categoryCounts = categoryCounts
                )
            }

            // Products count and sort indicator
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredProducts.size} products",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    TextButton(
                        onClick = { showFilterSheet = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SwapVert,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = sortOption.displayName,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Products list or empty state
            if (filteredProducts.isEmpty()) {
                item {
                    EmptyProductsState(
                        searchQuery = searchQuery,
                        onAddProduct = { navController.navigate(Screen.AddProduct.route) }
                    )
                }
            } else {
                items(filteredProducts, key = { it.id }) { product ->
                    ProductCard(
                        productName = product.name,
                        category = product.category,
                        currentStock = product.currentStock,
                        minStockThreshold = product.minStockThreshold,
                        sellingPrice = product.sellingPrice,
                        buyingPrice = product.buyingPrice,
                        showSensitiveInfo = showSensitiveInfo,
                        onClick = {
                            navController.navigate(Screen.ProductDetail.createRoute(product.id))
                        },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }

    // Sort & Filter bottom sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            SortFilterBottomSheet(
                currentSort = sortOption,
                onSortSelected = {
                    sortOption = it
                    showFilterSheet = false
                },
                selectedCategory = selectedCategory,
                onCategorySelected = {
                    selectedCategory = it
                }
            )
        }
    }
}

@Composable
private fun ProductsHeader(
    showSensitiveInfo: Boolean,
    onToggleSensitiveInfo: () -> Unit,
    onSortClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Products",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Sensitive info toggle
                IconButton(
                    onClick = onToggleSensitiveInfo,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (showSensitiveInfo)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(
                        imageVector = if (showSensitiveInfo)
                            Icons.Outlined.Visibility
                        else
                            Icons.Outlined.VisibilityOff,
                        contentDescription = if (showSensitiveInfo)
                            "Hide cost & margin"
                        else
                            "Show cost & margin"
                    )
                }

                // Filter button
                IconButton(onClick = onSortClick) {
                    Icon(
                        imageVector = Icons.Outlined.FilterList,
                        contentDescription = "Sort & Filter"
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Search products...",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            },
            trailingIcon = {
                AnimatedVisibility(
                    visible = query.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() })
        )
    }
}

@Composable
private fun CategoryFilterChips(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    categoryCounts: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val categories = listOf("All", "Beverages", "Food", "Toiletries", "Cooking Oil")

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = selectedCategory == category
            val count = if (category == "All") null else categoryCounts[category]

            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = category,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                        if (count != null && count > 0) {
                            Text(
                                text = "$count",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CopperPrimary,
                    selectedLabelColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    selectedBorderColor = CopperPrimary,
                    enabled = true,
                    selected = isSelected
                )
            )
        }
    }
}

@Composable
private fun EmptyProductsState(
    searchQuery: String,
    onAddProduct: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Inventory2,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (searchQuery.isEmpty()) "No products yet" else "No products found",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (searchQuery.isEmpty())
                "Add your first product to get started"
            else
                "Try adjusting your search or filters",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        if (searchQuery.isEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAddProduct,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CopperPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Product")
            }
        }
    }
}

@Composable
private fun SortFilterBottomSheet(
    currentSort: SortOption,
    onSortSelected: (SortOption) -> Unit,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Text(
            text = "Sort & Filter",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Sort options
        Text(
            text = "SORT BY",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        SortOption.values().forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (currentSort == option)
                            CopperPrimary.copy(alpha = 0.1f)
                        else
                            Color.Transparent
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = currentSort == option,
                    onClick = { onSortSelected(option) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = CopperPrimary
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = option.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (currentSort == option) FontWeight.Medium else FontWeight.Normal
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Category filter
        Text(
            text = "CATEGORY",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        val categories = listOf("All", "Beverages", "Food", "Toiletries", "Cooking Oil", "Household", "Other")

        categories.forEach { category ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (selectedCategory == category)
                            CopperPrimary.copy(alpha = 0.1f)
                        else
                            Color.Transparent
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = CopperPrimary
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (selectedCategory == category) FontWeight.Medium else FontWeight.Normal
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// Sort options enum
private enum class SortOption(val displayName: String) {
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)"),
    PRICE_ASC("Price (Low to High)"),
    PRICE_DESC("Price (High to Low)"),
    STOCK_LOW("Stock (Low First)"),
    STOCK_HIGH("Stock (High First)")
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
