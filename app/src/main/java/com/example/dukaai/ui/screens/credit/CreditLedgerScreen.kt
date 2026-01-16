package com.example.dukaai.ui.screens.credit

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dukaai.ui.components.*
import com.example.dukaai.ui.navigation.Screen
import com.example.dukaai.ui.theme.*
import kotlinx.coroutines.launch

// WhatsApp brand green
private val WhatsAppGreen = Color(0xFF25D366)

/**
 * Credit Ledger Screen - Modern Slate & Emerald Design
 *
 * Features:
 * - Clean header without solid AppBar
 * - Slate Dark summary card with bulk actions
 * - Two-row customer cards with prominent actions
 * - Payment bottom sheet (no navigation)
 * - Transaction history bottom sheet
 * - Date range filters
 * - Bulk send reminders
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditLedgerScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(CreditFilter.ALL) }
    var selectedDateFilter by remember { mutableStateOf(DateFilter.ALL_TIME) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Bottom sheet states
    var showPaymentSheet by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var selectedCustomer by remember { mutableStateOf<CreditCustomer?>(null) }
    val paymentSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val historySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Payment amount state
    var paymentAmount by remember { mutableStateOf("") }

    // Sample customer data (will be replaced with ViewModel)
    val sampleCustomers = remember {
        listOf(
            CreditCustomer(
                id = "1",
                name = "Ba John",
                phoneNumber = "0977123456",
                totalDebt = 174.0,
                unpaidTransactions = 2,
                lastPurchaseDate = "Nov 10",
                paymentStatus = PaymentStatus.OVERDUE,
                transactions = listOf(
                    CreditTransaction("Nov 10", "Purchase", 100.0, null),
                    CreditTransaction("Nov 8", "Purchase", 74.0, null),
                    CreditTransaction("Nov 5", "Payment", null, 50.0)
                )
            ),
            CreditCustomer(
                id = "2",
                name = "Mary Banda",
                phoneNumber = "0966555444",
                totalDebt = 85.0,
                unpaidTransactions = 1,
                lastPurchaseDate = "Today",
                paymentStatus = PaymentStatus.DUE_SOON,
                transactions = listOf(
                    CreditTransaction("Today", "Purchase", 85.0, null)
                )
            ),
            CreditCustomer(
                id = "3",
                name = "Ba Peter",
                phoneNumber = "0955888777",
                totalDebt = 50.0,
                unpaidTransactions = 1,
                lastPurchaseDate = "Yesterday",
                paymentStatus = PaymentStatus.ON_TIME,
                transactions = listOf(
                    CreditTransaction("Yesterday", "Purchase", 50.0, null)
                )
            ),
            CreditCustomer(
                id = "4",
                name = "Grace Mwale",
                phoneNumber = null,
                totalDebt = 120.0,
                unpaidTransactions = 3,
                lastPurchaseDate = "Nov 8",
                paymentStatus = PaymentStatus.OVERDUE,
                transactions = listOf(
                    CreditTransaction("Nov 8", "Purchase", 45.0, null),
                    CreditTransaction("Nov 6", "Purchase", 40.0, null),
                    CreditTransaction("Nov 4", "Purchase", 35.0, null)
                )
            ),
            CreditCustomer(
                id = "5",
                name = "Samuel Phiri",
                phoneNumber = "0971222333",
                totalDebt = 95.0,
                unpaidTransactions = 2,
                lastPurchaseDate = "Nov 9",
                paymentStatus = PaymentStatus.DUE_SOON,
                transactions = listOf(
                    CreditTransaction("Nov 9", "Purchase", 60.0, null),
                    CreditTransaction("Nov 7", "Purchase", 35.0, null)
                )
            )
        )
    }

    // Filter customers
    val filteredCustomers = sampleCustomers.filter { customer ->
        val matchesSearch = customer.name.contains(searchQuery, ignoreCase = true) ||
                customer.phoneNumber?.contains(searchQuery) == true
        val matchesFilter = when (selectedFilter) {
            CreditFilter.ALL -> true
            CreditFilter.OVERDUE -> customer.paymentStatus == PaymentStatus.OVERDUE
            CreditFilter.DUE_SOON -> customer.paymentStatus == PaymentStatus.DUE_SOON
            CreditFilter.ON_TIME -> customer.paymentStatus == PaymentStatus.ON_TIME
        }
        // Date filter would be applied here with real data
        matchesSearch && matchesFilter
    }

    val totalOutstanding = sampleCustomers.sumOf { it.totalDebt }
    val overdueCount = sampleCustomers.count { it.paymentStatus == PaymentStatus.OVERDUE }
    val overdueCustomersWithPhone = sampleCustomers.filter {
        it.paymentStatus == PaymentStatus.OVERDUE && it.phoneNumber != null
    }

    // WhatsApp intent helper
    fun openWhatsApp(phoneNumber: String, customerName: String, debt: Double) {
        val message = "Hello $customerName, this is a friendly reminder about your outstanding balance of K${String.format("%.2f", debt)}. Please visit us to settle. Thank you!"
        val formattedNumber = phoneNumber.replace(Regex("[^0-9]"), "")
        val fullNumber = if (formattedNumber.startsWith("0")) "+260${formattedNumber.substring(1)}" else formattedNumber
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://wa.me/$fullNumber?text=${Uri.encode(message)}")
        }
        context.startActivity(intent)
    }

    // Bulk send reminders
    fun sendAllReminders() {
        overdueCustomersWithPhone.forEach { customer ->
            customer.phoneNumber?.let { phone ->
                openWhatsApp(phone, customer.name, customer.totalDebt)
            }
        }
    }

    Scaffold(
        containerColor = SlateBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddCustomer.route) },
                containerColor = EmeraldAccent,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Customer")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header
            item {
                CreditHeader()
            }

            // Summary card with bulk action
            item {
                ModernCreditSummaryCard(
                    totalOutstanding = totalOutstanding,
                    customerCount = sampleCustomers.size,
                    overdueCount = overdueCount,
                    overdueWithPhoneCount = overdueCustomersWithPhone.size,
                    onSendAllReminders = { sendAllReminders() },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // Search bar
            item {
                Spacer(modifier = Modifier.height(20.dp))
                ModernSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = { focusManager.clearFocus() },
                    placeholder = "Search customers...",
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }

            // Filter chips - Status
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "STATUS",
                    style = MaterialTheme.typography.labelSmall,
                    color = SlateTextTertiary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                CreditFilterChips(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it }
                )
            }

            // Filter chips - Date range
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "TIME PERIOD",
                    style = MaterialTheme.typography.labelSmall,
                    color = SlateTextTertiary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                DateFilterChips(
                    selectedFilter = selectedDateFilter,
                    onFilterSelected = { selectedDateFilter = it }
                )
            }

            // Customer count
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "${filteredCustomers.size} customers",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateTextSecondary,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Customer list or empty state
            if (filteredCustomers.isEmpty()) {
                item {
                    EmptyCreditState(
                        searchQuery = searchQuery,
                        onAddCustomer = { navController.navigate(Screen.AddCustomer.route) }
                    )
                }
            } else {
                items(filteredCustomers, key = { it.id }) { customer ->
                    CustomerCard(
                        customerName = customer.name,
                        totalDebt = customer.totalDebt,
                        phoneNumber = customer.phoneNumber,
                        unpaidTransactions = customer.unpaidTransactions,
                        lastPurchaseDate = customer.lastPurchaseDate,
                        paymentStatus = customer.paymentStatus,
                        onClick = {
                            navController.navigate(Screen.CustomerDetail.createRoute(customer.id))
                        },
                        onWhatsAppClick = if (customer.phoneNumber != null) {
                            { openWhatsApp(customer.phoneNumber, customer.name, customer.totalDebt) }
                        } else null,
                        onPayClick = {
                            selectedCustomer = customer
                            paymentAmount = ""
                            showPaymentSheet = true
                        },
                        onViewHistoryClick = {
                            selectedCustomer = customer
                            showHistorySheet = true
                        },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }

    // Payment Bottom Sheet
    if (showPaymentSheet && selectedCustomer != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showPaymentSheet = false
                selectedCustomer = null
            },
            sheetState = paymentSheetState,
            containerColor = SlateSurface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            PaymentBottomSheetContent(
                customer = selectedCustomer!!,
                paymentAmount = paymentAmount,
                onPaymentAmountChange = { paymentAmount = it },
                onRecordPayment = {
                    // Record payment logic here
                    scope.launch {
                        paymentSheetState.hide()
                        showPaymentSheet = false
                        selectedCustomer = null
                    }
                },
                onDismiss = {
                    scope.launch {
                        paymentSheetState.hide()
                        showPaymentSheet = false
                        selectedCustomer = null
                    }
                }
            )
        }
    }

    // History Bottom Sheet
    if (showHistorySheet && selectedCustomer != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showHistorySheet = false
                selectedCustomer = null
            },
            sheetState = historySheetState,
            containerColor = SlateSurface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            TransactionHistorySheetContent(
                customer = selectedCustomer!!,
                onDismiss = {
                    scope.launch {
                        historySheetState.hide()
                        showHistorySheet = false
                        selectedCustomer = null
                    }
                }
            )
        }
    }
}

@Composable
private fun CreditHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Text(
            text = "Credit Ledger",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = SlateTextPrimary
        )
    }
}

@Composable
private fun ModernCreditSummaryCard(
    totalOutstanding: Double,
    customerCount: Int,
    overdueCount: Int,
    overdueWithPhoneCount: Int,
    onSendAllReminders: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SlatePrimaryDark
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "TOTAL OUTSTANDING",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.7f),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "K ${String.format("%.2f", totalOutstanding)}",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.People,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "$customerCount customers",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                if (overdueCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ErrorRed)
                        )
                        Text(
                            text = "$overdueCount overdue",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ErrorRed,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Bulk action button
            if (overdueWithPhoneCount > 0) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onSendAllReminders,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Send Reminders to All Overdue ($overdueWithPhoneCount)",
                        fontWeight = FontWeight.SemiBold
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
    placeholder: String,
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
                    text = placeholder,
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
private fun CreditFilterChips(
    selectedFilter: CreditFilter,
    onFilterSelected: (CreditFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(CreditFilter.entries.toTypedArray()) { filter ->
            val isSelected = selectedFilter == filter

            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = filter.label,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                leadingIcon = if (filter.icon != null) {
                    {
                        Icon(
                            imageVector = filter.icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = when (filter) {
                        CreditFilter.OVERDUE -> ErrorBg
                        CreditFilter.DUE_SOON -> WarningBg
                        CreditFilter.ON_TIME -> SuccessBg
                        else -> SlatePrimaryDark
                    },
                    selectedLabelColor = when (filter) {
                        CreditFilter.OVERDUE -> ErrorRed
                        CreditFilter.DUE_SOON -> WarningYellow
                        CreditFilter.ON_TIME -> SuccessGreen
                        else -> Color.White
                    }
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = SlateBorder,
                    selectedBorderColor = when (filter) {
                        CreditFilter.OVERDUE -> ErrorRed
                        CreditFilter.DUE_SOON -> WarningYellow
                        CreditFilter.ON_TIME -> SuccessGreen
                        else -> SlatePrimaryDark
                    },
                    enabled = true,
                    selected = isSelected
                )
            )
        }
    }
}

@Composable
private fun DateFilterChips(
    selectedFilter: DateFilter,
    onFilterSelected: (DateFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(DateFilter.entries.toTypedArray()) { filter ->
            val isSelected = selectedFilter == filter

            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = filter.label,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = EmeraldAccent.copy(alpha = 0.15f),
                    selectedLabelColor = EmeraldAccent
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = SlateBorder,
                    selectedBorderColor = EmeraldAccent,
                    enabled = true,
                    selected = isSelected
                )
            )
        }
    }
}

@Composable
private fun EmptyCreditState(
    searchQuery: String,
    onAddCustomer: () -> Unit,
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
                imageVector = Icons.Outlined.CreditCard,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = SlateTextTertiary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (searchQuery.isEmpty()) "No customers yet" else "No customers found",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = SlateTextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (searchQuery.isEmpty())
                "Add customers who buy on credit"
            else
                "Try adjusting your search",
            style = MaterialTheme.typography.bodyMedium,
            color = SlateTextSecondary
        )

        if (searchQuery.isEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAddCustomer,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldAccent
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Customer")
            }
        }
    }
}

@Composable
private fun PaymentBottomSheetContent(
    customer: CreditCustomer,
    paymentAmount: String,
    onPaymentAmountChange: (String) -> Unit,
    onRecordPayment: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        // Header
        Text(
            text = "Record Payment",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = SlateTextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "from ${customer.name}",
            style = MaterialTheme.typography.bodyMedium,
            color = SlateTextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Outstanding balance card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = ErrorBg
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Outstanding Balance",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateTextSecondary
                )
                Text(
                    text = "K ${String.format("%.2f", customer.totalDebt)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ErrorRed
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Payment amount input
        Text(
            text = "Payment Amount",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = SlateTextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = paymentAmount,
            onValueChange = { value ->
                // Only allow valid decimal numbers
                if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                    onPaymentAmountChange(value)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("0.00") },
            prefix = { Text("K ", fontWeight = FontWeight.SemiBold) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldAccent,
                unfocusedBorderColor = SlateBorder,
                focusedContainerColor = SlateSurfaceVariant,
                unfocusedContainerColor = SlateSurfaceVariant
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Quick amount buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(50.0, 100.0, customer.totalDebt).forEach { amount ->
                val label = if (amount == customer.totalDebt) "Full" else "K ${amount.toInt()}"
                OutlinedButton(
                    onClick = { onPaymentAmountChange(String.format("%.2f", amount)) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = EmeraldAccent
                    ),
                    border = BorderStroke(1.dp, EmeraldAccent.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = SlateTextSecondary
                )
            ) {
                Text("Cancel")
            }

            Button(
                onClick = onRecordPayment,
                modifier = Modifier.weight(1f),
                enabled = paymentAmount.isNotEmpty() && paymentAmount.toDoubleOrNull()?.let { it > 0 } == true,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldAccent
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Payment,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Record Payment", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun TransactionHistorySheetContent(
    customer: CreditCustomer,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Transaction History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SlateTextPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateTextSecondary
                )
            }

            // Current balance
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Balance",
                    style = MaterialTheme.typography.labelSmall,
                    color = SlateTextTertiary
                )
                Text(
                    text = "K ${String.format("%.2f", customer.totalDebt)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (customer.totalDebt > 0) ErrorRed else EmeraldAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Transaction list
        if (customer.transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.Receipt,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = SlateTextTertiary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No transactions yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateTextSecondary
                    )
                }
            }
        } else {
            customer.transactions.forEach { transaction ->
                TransactionItem(transaction = transaction)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Close button
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SlatePrimaryDark
            )
        ) {
            Text("Close", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: CreditTransaction,
    modifier: Modifier = Modifier
) {
    val isPayment = transaction.paymentAmount != null
    val amount = transaction.paymentAmount ?: transaction.purchaseAmount ?: 0.0

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isPayment) SuccessBg else SlateSurfaceVariant
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isPayment) SuccessGreen.copy(alpha = 0.2f) else SlateTextTertiary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPayment) Icons.Outlined.Payment else Icons.Outlined.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (isPayment) SuccessGreen else SlateTextSecondary
                    )
                }

                Column {
                    Text(
                        text = transaction.type,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = SlateTextPrimary
                    )
                    Text(
                        text = transaction.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateTextTertiary
                    )
                }
            }

            Text(
                text = "${if (isPayment) "-" else "+"}K ${String.format("%.2f", amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isPayment) SuccessGreen else SlateTextPrimary
            )
        }
    }
}

// Data classes
private data class CreditCustomer(
    val id: String,
    val name: String,
    val phoneNumber: String?,
    val totalDebt: Double,
    val unpaidTransactions: Int,
    val lastPurchaseDate: String,
    val paymentStatus: PaymentStatus,
    val transactions: List<CreditTransaction> = emptyList()
)

private data class CreditTransaction(
    val date: String,
    val type: String,
    val purchaseAmount: Double?,
    val paymentAmount: Double?
)

private enum class CreditFilter(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector?) {
    ALL("All", null),
    OVERDUE("Overdue", Icons.Default.Warning),
    DUE_SOON("Due Soon", Icons.Default.Schedule),
    ON_TIME("On Time", Icons.Default.CheckCircle)
}

private enum class DateFilter(val label: String) {
    ALL_TIME("All Time"),
    DUE_THIS_WEEK("Due This Week"),
    DUE_TODAY("Due Today"),
    LAST_7_DAYS("Last 7 Days"),
    LAST_30_DAYS("Last 30 Days")
}
