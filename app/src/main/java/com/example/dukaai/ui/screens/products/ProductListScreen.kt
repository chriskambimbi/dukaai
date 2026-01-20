package com.example.dukaai.ui.screens.products

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.dukaai.ui.components.VoiceInputDialog
import com.example.dukaai.ui.navigation.Screen
import com.example.dukaai.ui.theme.*
import com.example.dukaai.ui.viewmodel.VoiceCommandViewModel
import com.example.dukaai.voice.VoiceCommandResult
import kotlin.math.roundToInt

/**
 * Product List Screen - Enhanced with UX improvements
 * Features:
 * - Clean header with sensitive info toggle
 * - Search with smooth design
 * - Category tabs with emoji icons
 * - Swipeable product cards (right: quick sale, left: edit/delete)
 * - Improved stock status colors (green/orange/red)
 * - Extended FAB with clear label
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    voiceCommandViewModel: VoiceCommandViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSensitiveInfo by remember { mutableStateOf(true) }
    var sortOption by remember { mutableStateOf(SortOption.NAME_ASC) }
    val focusManager = LocalFocusManager.current

    // Voice command state
    var showVoiceInput by remember { mutableStateOf(false) }
    var showVoiceResult by remember { mutableStateOf(false) }
    var voiceResultMessage by remember { mutableStateOf("") }
    var voiceResultSuccess by remember { mutableStateOf(true) }

    // Observe voice command execution result
    val executionResult by voiceCommandViewModel.executionResult.collectAsState()

    // Handle execution result
    LaunchedEffect(executionResult) {
        executionResult?.let { result ->
            when (result) {
                is VoiceCommandResult.Success -> {
                    voiceResultMessage = result.message
                    voiceResultSuccess = true
                    showVoiceResult = true
                }
                is VoiceCommandResult.Failure -> {
                    voiceResultMessage = "${result.error}: ${result.reason}"
                    voiceResultSuccess = false
                    showVoiceResult = true
                }
                is VoiceCommandResult.NeedsConfirmation -> {
                    voiceResultMessage = result.prompt
                    voiceResultSuccess = false
                    showVoiceResult = true
                }
            }
            voiceCommandViewModel.clearExecutionResult()
        }
    }

    // Voice command examples for products
    val voiceCommandExamples = listOf(
        "Sell 5 Coca-Cola",
        "Add new product Fanta at 15 kwacha",
        "Check stock for bread",
        "Show low stock items"
    )

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
        containerColor = SlateBackground,
        floatingActionButton = {
            // Two FABs: Voice and Add Product
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Voice Command FAB
                FloatingActionButton(
                    onClick = { showVoiceInput = true },
                    containerColor = AccentOrange,
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice command"
                    )
                }

                // Extended FAB with clear label
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(Screen.AddProduct.route) },
                    containerColor = EmeraldAccent,
                    contentColor = Color.White,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(
                            text = "Add Product",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Header section
            item {
                ProductsHeader(
                    showSensitiveInfo = showSensitiveInfo,
                    onToggleSensitiveInfo = { showSensitiveInfo = !showSensitiveInfo },
                    onSortClick = { showFilterSheet = true }
                )
            }

            // Search bar with voice support
            item {
                ModernSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { focusManager.clearFocus() },
                    onVoiceSearch = { spokenText ->
                        searchQuery = spokenText
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // Category filter tabs with icons
            item {
                Spacer(modifier = Modifier.height(16.dp))
                CategoryTabsWithIcons(
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
                        color = SlateTextSecondary
                    )

                    // Sort dropdown button
                    Surface(
                        onClick = { showFilterSheet = true },
                        shape = RoundedCornerShape(8.dp),
                        color = SlateSurfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.SwapVert,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = SlateTextSecondary
                            )
                            Text(
                                text = sortOption.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                color = SlateTextPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = SlateTextSecondary
                            )
                        }
                    }
                }

                // Swipe hint
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Swipe cards: → Quick Sale  |  ← Edit/Delete",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateTextTertiary
                    )
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
                    SwipeableProductCard(
                        product = product,
                        showSensitiveInfo = showSensitiveInfo,
                        onProductClick = {
                            navController.navigate(Screen.ProductDetail.createRoute(product.id))
                        },
                        onQuickSale = {
                            navController.navigate(Screen.QuickSale.route)
                        },
                        onEdit = {
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
            containerColor = SlateSurface
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

    // Voice Input Dialog
    VoiceInputDialog(
        isVisible = showVoiceInput,
        onDismiss = { showVoiceInput = false },
        onResult = { spokenText ->
            // Process voice command using FunctionGemma
            voiceCommandViewModel.processTextInput(spokenText)
        },
        title = "Voice Command",
        hint = "Say a command like 'Sell 5 Coca-Cola'",
        exampleCommands = voiceCommandExamples
    )

    // Voice Result Dialog
    if (showVoiceResult) {
        AlertDialog(
            onDismissRequest = { showVoiceResult = false },
            containerColor = SlateSurface,
            icon = {
                Icon(
                    imageVector = if (voiceResultSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (voiceResultSuccess) EmeraldAccent else WarningYellow,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = if (voiceResultSuccess) "Command Executed" else "Command Issue",
                    fontWeight = FontWeight.SemiBold,
                    color = SlateTextPrimary
                )
            },
            text = {
                Text(
                    text = voiceResultMessage,
                    color = SlateTextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { showVoiceResult = false }) {
                    Text("OK", color = EmeraldAccent)
                }
            }
        )
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
                fontWeight = FontWeight.Bold,
                color = SlateTextPrimary
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Sensitive info toggle
                IconButton(
                    onClick = onToggleSensitiveInfo,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (showSensitiveInfo)
                            EmeraldAccent
                        else
                            SlateTextTertiary
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
                        contentDescription = "Sort & Filter",
                        tint = SlateTextSecondary
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
    onVoiceSearch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = SlateSurfaceVariant
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Search products or say it...",
                    color = SlateTextTertiary
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = SlateTextTertiary
                )
            },
            trailingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Voice search button
                    com.example.dukaai.ui.components.VoiceSearchButton(
                        onVoiceResult = { spokenText ->
                            val searchTerm = com.example.dukaai.ui.components.parseProductVoiceCommand(spokenText)
                            onVoiceSearch(searchTerm)
                        },
                        tint = EmeraldAccent
                    )

                    // Clear button
                    AnimatedVisibility(
                        visible = query.isNotEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = SlateTextTertiary
                            )
                        }
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

// Category data with icons
private data class CategoryData(
    val name: String,
    val emoji: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val categoriesWithIcons = listOf(
    CategoryData("All", "📦", Icons.Outlined.Inventory2),
    CategoryData("Beverages", "🥤", Icons.Outlined.LocalDrink),
    CategoryData("Food", "🍞", Icons.Outlined.BakeryDining),
    CategoryData("Toiletries", "🧴", Icons.Outlined.Sanitizer),
    CategoryData("Cooking Oil", "🫒", Icons.Outlined.OilBarrel)
)

@Composable
private fun CategoryTabsWithIcons(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    categoryCounts: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(categoriesWithIcons) { category ->
            val isSelected = selectedCategory == category.name
            val count = if (category.name == "All") null else categoryCounts[category.name]

            Surface(
                onClick = { onCategorySelected(category.name) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) EmeraldAccent else SlateSurface,
                border = if (!isSelected) {
                    androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
                } else null
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Emoji icon
                    Text(
                        text = category.emoji,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Column {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else SlateTextPrimary
                        )
                        if (count != null && count > 0) {
                            Text(
                                text = "$count items",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else SlateTextTertiary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SwipeableProductCard(
    product: ProductItem,
    showSensitiveInfo: Boolean,
    onProductClick: () -> Unit,
    onQuickSale: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = 100f

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        // Background actions (revealed on swipe)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .clip(RoundedCornerShape(12.dp)),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left action (Quick Sale) - revealed on swipe right
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(EmeraldAccent)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Text(
                        text = "Quick Sale",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Right action (Edit/Delete) - revealed on swipe left
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(WarningYellow)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Edit",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }

        // Main card content
        Card(
            onClick = onProductClick,
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                offsetX > swipeThreshold -> {
                                    onQuickSale()
                                    offsetX = 0f
                                }
                                offsetX < -swipeThreshold -> {
                                    onEdit()
                                    offsetX = 0f
                                }
                                else -> {
                                    offsetX = 0f
                                }
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX = (offsetX + dragAmount).coerceIn(-150f, 150f)
                        }
                    )
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = if (offsetX != 0f) 4.dp else 0.dp),
            border = if (offsetX == 0f) {
                androidx.compose.foundation.BorderStroke(1.dp, SlateBorder)
            } else null
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Product avatar with initial
                ProductAvatar(
                    name = product.name,
                    category = product.category
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Product info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = SlateTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = product.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateTextTertiary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Stock status with improved colors
                    StockStatusBadge(
                        currentStock = product.currentStock,
                        minStockThreshold = product.minStockThreshold
                    )
                }

                // Price and margin
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "K ${product.sellingPrice.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldAccent
                    )

                    if (showSensitiveInfo) {
                        val margin = ((product.sellingPrice - product.buyingPrice) / product.buyingPrice * 100).toInt()
                        Text(
                            text = "+$margin%",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (margin >= 20) SuccessGreen else SlateTextTertiary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductAvatar(
    name: String,
    category: String,
    modifier: Modifier = Modifier
) {
    val initial = name.firstOrNull()?.uppercaseChar() ?: '?'
    val backgroundColor = when (category) {
        "Beverages" -> Color(0xFFE0F2FE) // Light blue
        "Food" -> Color(0xFFFEF3C7) // Light yellow
        "Toiletries" -> Color(0xFFF3E8FF) // Light purple
        "Cooking Oil" -> Color(0xFFDCFCE7) // Light green
        else -> SlateSurfaceVariant
    }

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = SlateTextPrimary
        )
    }
}

@Composable
private fun StockStatusBadge(
    currentStock: Int,
    minStockThreshold: Int,
    modifier: Modifier = Modifier
) {
    val (statusText, statusColor, bgColor) = when {
        currentStock == 0 -> Triple("Out of stock", ErrorRed, ErrorBg)
        currentStock < 5 -> Triple("Critical: $currentStock left", ErrorRed, ErrorBg)
        currentStock <= minStockThreshold -> Triple("Low: $currentStock left", WarningYellow, WarningBg)
        else -> Triple("In stock: $currentStock", SuccessGreen, SuccessBg)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = statusColor
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
                .background(SlateSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Inventory2,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = SlateTextTertiary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (searchQuery.isEmpty()) "No products yet" else "No products found",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = SlateTextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (searchQuery.isEmpty())
                "Add your first product to get started"
            else
                "Try adjusting your search or filters",
            style = MaterialTheme.typography.bodyMedium,
            color = SlateTextSecondary
        )

        if (searchQuery.isEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAddProduct,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldAccent
                ),
                shape = RoundedCornerShape(8.dp)
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
            fontWeight = FontWeight.Bold,
            color = SlateTextPrimary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Sort options
        Text(
            text = "SORT BY",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = SlateTextSecondary,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        SortOption.entries.forEach { option ->
            Surface(
                onClick = { onSortSelected(option) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = if (currentSort == option) EmeraldSubtle else Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentSort == option,
                        onClick = { onSortSelected(option) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = EmeraldAccent
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = option.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (currentSort == option) FontWeight.Medium else FontWeight.Normal,
                        color = SlateTextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Category filter
        Text(
            text = "CATEGORY",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = SlateTextSecondary,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        val categories = listOf("All", "Beverages", "Food", "Toiletries", "Cooking Oil", "Household", "Other")

        categories.forEach { category ->
            Surface(
                onClick = { onCategorySelected(category) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = if (selectedCategory == category) EmeraldSubtle else Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedCategory == category,
                        onClick = { onCategorySelected(category) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = EmeraldAccent
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = category,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (selectedCategory == category) FontWeight.Medium else FontWeight.Normal,
                        color = SlateTextPrimary
                    )
                }
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
