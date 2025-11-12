package com.example.dukaai.ui.screens.credit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.dukaai.ui.components.*
import com.example.dukaai.ui.navigation.Screen

/**
 * Credit Ledger Screen
 * Shows all customers with outstanding credit
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditLedgerScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(CreditFilter.ALL) }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Credit Ledger") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddCustomer.route) }
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Customer")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Summary card
            CreditSummaryCard(
                totalOutstanding = totalOutstanding,
                customerCount = sampleCustomers.size,
                overdueCount = overdueCount
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = { /* Search */ },
                placeholder = "Search customers..."
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filter chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CreditFilter.entries.toTypedArray()) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter.label) },
                        leadingIcon = if (selectedFilter == filter) {
                            {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Customer list
            if (filteredCustomers.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.CreditCard,
                    title = "No customers found",
                    message = if (searchQuery.isEmpty()) {
                        "Add customers who buy on credit"
                    } else {
                        "Try adjusting your search"
                    },
                    actionText = if (searchQuery.isEmpty()) "Add Customer" else null,
                    onAction = if (searchQuery.isEmpty()) {
                        { navController.navigate(Screen.AddCustomer.route) }
                    } else null
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "${filteredCustomers.size} customers",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    items(filteredCustomers) { customer ->
                        CustomerCard(
                            customerName = customer.name,
                            totalDebt = customer.totalDebt,
                            phoneNumber = customer.phoneNumber,
                            unpaidTransactions = customer.unpaidTransactions,
                            lastPurchaseDate = customer.lastPurchaseDate,
                            paymentStatus = customer.paymentStatus,
                            onClick = {
                                navController.navigate(
                                    Screen.CustomerDetail.createRoute(customer.id)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CreditSummaryCard(
    totalOutstanding: Double,
    customerCount: Int,
    overdueCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "TOTAL OUTSTANDING",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "K ${String.format("%.2f", totalOutstanding)}",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "$customerCount customers",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }

                if (overdueCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = com.example.dukaai.ui.theme.ErrorRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "$overdueCount overdue",
                            style = MaterialTheme.typography.bodyLarge,
                            color = com.example.dukaai.ui.theme.ErrorRed,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
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
