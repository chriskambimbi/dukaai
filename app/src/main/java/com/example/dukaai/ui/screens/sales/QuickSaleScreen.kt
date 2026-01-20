package com.example.dukaai.ui.screens.sales

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.dukaai.data.local.entity.ProductEntity
import com.example.dukaai.ui.components.VoiceInputDialog
import com.example.dukaai.ui.navigation.Screen
import com.example.dukaai.ui.theme.*
import com.example.dukaai.ui.viewmodel.ProductViewModel
import com.example.dukaai.ui.viewmodel.SaleViewModel
import com.example.dukaai.ui.viewmodel.VoiceCommandViewModel
import com.example.dukaai.voice.VoiceCommandResult
import kotlinx.coroutines.delay

/**
 * Quick Sale Screen
 * Fast interface for logging sales with multiple input methods
 * Optimized for voice-first markets with visual feedback
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSaleScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    productViewModel: ProductViewModel = hiltViewModel(),
    saleViewModel: SaleViewModel = hiltViewModel(),
    voiceCommandViewModel: VoiceCommandViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var quantity by remember { mutableStateOf(1) }
    var showConfirmation by remember { mutableStateOf(false) }
    var saleType by remember { mutableStateOf(SaleType.CASH) }
    var showSuccessSnackbar by remember { mutableStateOf(false) }
    var showManualEntry by remember { mutableStateOf(false) }
    var isSearchFocused by remember { mutableStateOf(false) }
    var manualBarcode by remember { mutableStateOf("") }

    // Voice command state
    var showVoiceInput by remember { mutableStateOf(false) }
    var showVoiceResult by remember { mutableStateOf(false) }
    var voiceResultMessage by remember { mutableStateOf("") }
    var voiceResultSuccess by remember { mutableStateOf(true) }

    // Observe voice command execution result
    val voiceExecutionResult by voiceCommandViewModel.executionResult.collectAsState()

    // Handle voice execution result
    LaunchedEffect(voiceExecutionResult) {
        voiceExecutionResult?.let { result ->
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

    // Voice command examples for quick sale
    val voiceCommandExamples = listOf(
        "Sell 3 Coca-Cola",
        "2 bread",
        "Sell 5 sugar",
        "One Fanta"
    )

    // Track recently added items for animation
    var recentlyAddedIds by remember { mutableStateOf(setOf<String>()) }

    // Get products from ViewModel
    val products by productViewModel.products.collectAsState()
    val isLoading by productViewModel.isLoading.collectAsState()
    val saleCompleted by saleViewModel.saleCompleted.collectAsState()
    val saleError by saleViewModel.error.collectAsState()

    // Popular items (top sellers based on some criteria - here we'll just take first 5)
    val popularItems = remember(products) {
        products.take(5)
    }

    // Recent items (simulated - in real app would track from history)
    val recentItems = remember(products) {
        products.shuffled().take(3)
    }

    // Filter products based on search query
    val filteredProducts = products.filter {
        searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) ||
                it.barcode?.contains(searchQuery, ignoreCase = true) == true
    }

    // Handle sale completed
    LaunchedEffect(saleCompleted) {
        if (saleCompleted) {
            showSuccessSnackbar = true
            saleViewModel.clearCart()
        }
    }

    // Clear recently added animation after delay
    LaunchedEffect(recentlyAddedIds) {
        if (recentlyAddedIds.isNotEmpty()) {
            delay(1000)
            recentlyAddedIds = emptySet()
        }
    }

    Scaffold(
        containerColor = SlateBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Quick Sale")
                        Text(
                            text = "Record sales instantly",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = {
            if (showSuccessSnackbar) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = SuccessGreen,
                    contentColor = Color.White,
                    action = {
                        TextButton(onClick = { showSuccessSnackbar = false }) {
                            Text("OK", color = Color.White)
                        }
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Text("Sale recorded successfully!")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Primary action: Voice (larger, more prominent)
            VoiceInputSection(
                onVoiceClick = { showVoiceInput = true },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Secondary actions: Scan and Manual entry
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SecondaryActionButton(
                    icon = Icons.Default.QrCodeScanner,
                    label = "Scan Barcode",
                    onClick = { navController.navigate(Screen.CameraScanner.route) },
                    modifier = Modifier.weight(1f)
                )

                SecondaryActionButton(
                    icon = Icons.Outlined.Dialpad,
                    label = "Enter Code",
                    onClick = { showManualEntry = true },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider(color = SlateSurface)

            Spacer(modifier = Modifier.height(16.dp))

            // Search section with suggestions
            Text(
                text = "FIND PRODUCT",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Enhanced search bar
            EnhancedSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onFocusChange = { isSearchFocused = it },
                placeholder = "Search by name or barcode..."
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Show suggestions when search is focused and empty
            AnimatedVisibility(
                visible = isSearchFocused && searchQuery.isBlank(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    // Recent items
                    if (recentItems.isNotEmpty()) {
                        Text(
                            text = "Recent",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(recentItems) { product ->
                                SuggestionChip(
                                    product = product,
                                    onClick = {
                                        selectedProduct = product
                                        quantity = 1
                                        showConfirmation = true
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Popular items
                    if (popularItems.isNotEmpty()) {
                        Text(
                            text = "Popular",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(popularItems) { product ->
                                SuggestionChip(
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

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Product list with animations
            if (isLoading && products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = EmeraldAccent)
                }
            } else if (filteredProducts.isEmpty()) {
                EmptyStateContent(
                    isSearchResult = searchQuery.isNotBlank(),
                    searchQuery = searchQuery,
                    onAddProduct = { navController.navigate(Screen.AddProduct.route) },
                    onScanBarcode = { navController.navigate(Screen.CameraScanner.route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(
                        items = filteredProducts,
                        key = { _, product -> product.id }
                    ) { index, product ->
                        AnimatedProductCard(
                            product = product,
                            isRecentlyAdded = recentlyAddedIds.contains(product.id),
                            animationDelay = index * 50,
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
    }

    // Manual barcode entry dialog
    if (showManualEntry) {
        ManualBarcodeDialog(
            barcode = manualBarcode,
            onBarcodeChange = { manualBarcode = it },
            onDismiss = {
                showManualEntry = false
                manualBarcode = ""
            },
            onSubmit = {
                // Search for product by barcode
                val foundProduct = products.find { it.barcode == manualBarcode }
                if (foundProduct != null) {
                    selectedProduct = foundProduct
                    quantity = 1
                    showConfirmation = true
                } else {
                    // Could show error or suggest adding product
                    searchQuery = manualBarcode
                }
                showManualEntry = false
                manualBarcode = ""
            }
        )
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
                recentlyAddedIds = recentlyAddedIds + selectedProduct!!.id
                saleViewModel.addToCart(selectedProduct!!, quantity)
                if (saleType == SaleType.CASH) {
                    saleViewModel.completeCashSale()
                } else {
                    saleViewModel.completeCashSale()
                }
                showConfirmation = false
                selectedProduct = null
            }
        )
    }

    // Voice Input Dialog
    VoiceInputDialog(
        isVisible = showVoiceInput,
        onDismiss = { showVoiceInput = false },
        onResult = { spokenText ->
            // Process voice command using FunctionGemma
            voiceCommandViewModel.processTextInput(spokenText)
        },
        title = "Voice Sale",
        hint = "Say product name and quantity",
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
                    text = if (voiceResultSuccess) "Sale Recorded" else "Command Issue",
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = voiceResultMessage,
                    color = TextSecondary
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

/**
 * Voice input section - primary action, large and prominent
 */
@Composable
private fun VoiceInputSection(
    onVoiceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onVoiceClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = EmeraldAccent
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Pulsing microphone icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Tap to Speak",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Say the product name and quantity",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

/**
 * Secondary action button (Scan, Manual entry)
 */
@Composable
private fun SecondaryActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.outlinedCardColors(
            containerColor = SlateSurface
        ),
        border = BorderStroke(1.dp, SlateBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = EmeraldAccent,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
}

/**
 * Enhanced search bar with focus state
 */
@Composable
private fun EnhancedSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { onFocusChange(it.isFocused) },
        placeholder = {
            Text(
                text = placeholder,
                color = TextSecondary
            )
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = TextSecondary
            )
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = TextSecondary
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = SlateSurface,
            unfocusedContainerColor = SlateSurface,
            focusedBorderColor = EmeraldAccent,
            unfocusedBorderColor = SlateBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = EmeraldAccent
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
    )
}

/**
 * Suggestion chip for recent/popular items
 */
@Composable
private fun SuggestionChip(
    product: ProductEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = SlateSurface,
        border = BorderStroke(1.dp, SlateBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "K ${String.format("%.0f", product.sellingPrice)}",
                style = MaterialTheme.typography.bodySmall,
                color = EmeraldAccent,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Improved empty state with context
 */
@Composable
private fun EmptyStateContent(
    isSearchResult: Boolean,
    searchQuery: String,
    onAddProduct: () -> Unit,
    onScanBarcode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(SlateSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSearchResult) Icons.Outlined.SearchOff else Icons.Outlined.Inventory2,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isSearchResult) "No products found" else "No products to sell",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isSearchResult) {
                "No products match \"$searchQuery\"\nTry a different search or add this product"
            } else {
                "Add products to your inventory first,\nthen come back to record sales"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onScanBarcode,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = EmeraldAccent
                ),
                border = BorderStroke(1.dp, EmeraldAccent)
            ) {
                Icon(
                    Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Scan")
            }

            Button(
                onClick = onAddProduct,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldAccent,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Product")
            }
        }
    }
}

/**
 * Animated product card with scale-in effect
 */
@Composable
private fun AnimatedProductCard(
    product: ProductEntity,
    isRecentlyAdded: Boolean,
    animationDelay: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val cardAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "alpha"
    )

    // Highlight animation for recently added
    val highlightColor by animateColorAsState(
        targetValue = if (isRecentlyAdded) EmeraldAccent.copy(alpha = 0.3f) else Color.Transparent,
        animationSpec = tween(durationMillis = 500),
        label = "highlight"
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .alpha(cardAlpha),
        colors = CardDefaults.cardColors(
            containerColor = SlateSurface
        ),
        border = if (isRecentlyAdded) BorderStroke(2.dp, EmeraldAccent) else BorderStroke(1.dp, SlateBorder)
    ) {
        Box(
            modifier = Modifier.background(highlightColor)
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
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = product.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        // Stock status with color coding
                        val (stockText, stockColor) = when {
                            product.currentStock == 0 -> "Out of stock" to ErrorRed
                            product.currentStock < 5 -> "${product.currentStock} left" to ErrorRed
                            product.currentStock <= product.minStockThreshold -> "${product.currentStock} left" to WarningYellow
                            else -> "${product.currentStock} in stock" to SuccessGreen
                        }

                        Text(
                            text = stockText,
                            style = MaterialTheme.typography.bodySmall,
                            color = stockColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "K ${String.format("%.2f", product.sellingPrice)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldAccent
                    )

                    // Quick add indicator
                    Text(
                        text = "Tap to sell",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

/**
 * Manual barcode entry dialog with number pad
 */
@Composable
private fun ManualBarcodeDialog(
    barcode: String,
    onBarcodeChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SlateSurface,
        title = {
            Text(
                "Enter Barcode",
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Type the barcode number manually",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Barcode display
                OutlinedTextField(
                    value = barcode,
                    onValueChange = { if (it.length <= 13) onBarcodeChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    ),
                    placeholder = {
                        Text(
                            "0000000000000",
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            color = TextSecondary.copy(alpha = 0.5f)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SlateBackground,
                        unfocusedContainerColor = SlateBackground,
                        focusedBorderColor = EmeraldAccent,
                        unfocusedBorderColor = SlateBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = EmeraldAccent
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { if (barcode.isNotBlank()) onSubmit() }
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Number pad
                NumberPad(
                    onNumberClick = { num ->
                        if (barcode.length < 13) onBarcodeChange(barcode + num)
                    },
                    onBackspace = {
                        if (barcode.isNotEmpty()) onBarcodeChange(barcode.dropLast(1))
                    },
                    onClear = { onBarcodeChange("") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = barcode.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldAccent,
                    contentColor = Color.White
                )
            ) {
                Text("Find Product")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

/**
 * Number pad component
 */
@Composable
private fun NumberPad(
    onNumberClick: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val numbers = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("C", "0", "⌫")
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        numbers.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    NumberPadKey(
                        key = key,
                        onClick = {
                            when (key) {
                                "C" -> onClear()
                                "⌫" -> onBackspace()
                                else -> onNumberClick(key)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun NumberPadKey(
    key: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSpecial = key == "C" || key == "⌫"

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = if (isSpecial) SlateBackground else EmeraldAccent.copy(alpha = 0.1f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = key,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isSpecial) TextSecondary else EmeraldAccent
            )
        }
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
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SlateSurface
        )
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
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = product.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Text(
                        text = "Stock: ${product.currentStock}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (product.currentStock <= product.minStockThreshold) {
                            WarningYellow
                        } else {
                            TextSecondary
                        }
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "K ${String.format("%.2f", product.sellingPrice)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldAccent
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
        containerColor = SlateSurface,
        title = {
            Text(
                "Confirm Sale",
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Product info
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = EmeraldAccent.copy(alpha = 0.1f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Unit price: K ${String.format("%.2f", product.sellingPrice)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                // Quantity selector
                Column {
                    Text(
                        text = "Quantity",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
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
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Decrease",
                                tint = if (quantity > 1) EmeraldAccent else TextSecondary
                            )
                        }

                        Text(
                            text = quantity.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        IconButton(
                            onClick = { if (quantity < product.currentStock) onQuantityChange(quantity + 1) },
                            enabled = quantity < product.currentStock
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Increase",
                                tint = if (quantity < product.currentStock) EmeraldAccent else TextSecondary
                            )
                        }
                    }
                }

                // Sale type selector
                Column {
                    Text(
                        text = "Sale Type",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
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
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldAccent.copy(alpha = 0.2f),
                                selectedLabelColor = EmeraldAccent
                            )
                        )

                        FilterChip(
                            selected = saleType == SaleType.CREDIT,
                            onClick = { onSaleTypeChange(SaleType.CREDIT) },
                            label = { Text("Credit") },
                            leadingIcon = if (saleType == SaleType.CREDIT) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldAccent.copy(alpha = 0.2f),
                                selectedLabelColor = EmeraldAccent
                            )
                        )
                    }
                }

                // Summary
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = SlateBackground
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
                            Text("Total Amount:", color = TextSecondary)
                            Text(
                                text = "K ${String.format("%.2f", totalAmount)}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge,
                                color = EmeraldAccent
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Stock after sale:", color = TextSecondary)
                            Text(
                                text = "$remainingStock units",
                                color = when {
                                    remainingStock == 0 -> ErrorRed
                                    remainingStock < 5 -> ErrorRed
                                    remainingStock <= 10 -> WarningYellow
                                    else -> TextSecondary
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
                            tint = if (remainingStock < 5) ErrorRed else WarningYellow,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (remainingStock == 0) "This will deplete stock!"
                                   else if (remainingStock < 5) "Critical stock level"
                                   else "Low stock warning",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (remainingStock < 5) ErrorRed else WarningYellow
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = quantity > 0 && quantity <= product.currentStock,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldAccent,
                    contentColor = Color.White
                )
            ) {
                Text("Confirm Sale")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

private enum class SaleType {
    CASH, CREDIT
}
