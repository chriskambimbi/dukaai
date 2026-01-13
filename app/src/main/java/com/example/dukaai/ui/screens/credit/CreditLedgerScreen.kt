package com.example.dukaai.ui.screens.credit

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
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
 * Credit Ledger Screen - Modern Slate & Emerald Design
 *
 * Features:
 * - Clean header without solid AppBar
 * - Slate Dark summary card
 * - Initials avatars with pastel backgrounds
 * - WhatsApp and Pay quick actions
 * - No dividers - whitespace separation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditLedgerScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(CreditFilter.ALL) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

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
                paymentStatus = PaymentStatus.OVERDUE
            ),
            CreditCustomer(
                id = "2",
                name = "Mary Banda",
                phoneNumber = "0966555444",
                totalDebt = 85.0,
                unpaidTransactions = 1,
                lastPurchaseDate = "Today",
                paymentStatus = PaymentStatus.DUE_SOON
            ),
            CreditCustomer(
                id = "3",
                name = "Ba Peter",
                phoneNumber = "0955888777",
                totalDebt = 50.0,
                unpaidTransactions = 1,
                lastPurchaseDate = "Yesterday",
                paymentStatus = PaymentStatus.ON_TIME
            ),
            CreditCustomer(
                id = "4",
                name = "Grace Mwale",
                phoneNumber = null,
                totalDebt = 120.0,
                unpaidTransactions = 3,
                lastPurchaseDate = "Nov 8",
                paymentStatus = PaymentStatus.OVERDUE
            ),
            CreditCustomer(
                id = "5",
                name = "Samuel Phiri",
                phoneNumber = "0971222333",
                totalDebt = 95.0,
                unpaidTransactions = 2,
                lastPurchaseDate = "Nov 9",
                paymentStatus = PaymentStatus.DUE_SOON
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
        }
        matchesSearch && matchesFilter
    }

    val totalOutstanding = sampleCustomers.sumOf { it.totalDebt }
    val overdueCount = sampleCustomers.count { it.paymentStatus == PaymentStatus.OVERDUE }

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

            // Summary card
            item {
                ModernCreditSummaryCard(
                    totalOutstanding = totalOutstanding,
                    customerCount = sampleCustomers.size,
                    overdueCount = overdueCount,
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

            // Filter chips
            item {
                Spacer(modifier = Modifier.height(16.dp))
                CreditFilterChips(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it }
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
                            navController.navigate(Screen.RecordPayment.createRoute(customer.id))
                        },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }
            }
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
                horizontalArrangement = Arrangement.SpaceBetween
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
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SlatePrimaryDark,
                    selectedLabelColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = SlateBorder,
                    selectedBorderColor = SlatePrimaryDark,
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

// Data classes
private data class CreditCustomer(
    val id: String,
    val name: String,
    val phoneNumber: String?,
    val totalDebt: Double,
    val unpaidTransactions: Int,
    val lastPurchaseDate: String,
    val paymentStatus: PaymentStatus
)

private enum class CreditFilter(val label: String) {
    ALL("All"),
    OVERDUE("Overdue"),
    DUE_SOON("Due Soon")
}
