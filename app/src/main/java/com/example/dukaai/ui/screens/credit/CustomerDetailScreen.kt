package com.example.dukaai.ui.screens.credit

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.dukaai.ui.components.*
import com.example.dukaai.ui.navigation.Screen
import com.example.dukaai.ui.viewmodel.CreditViewModel
import com.example.dukaai.util.WhatsAppHelper
import com.example.dukaai.ui.theme.SlateBackground

/**
 * Customer Detail Screen
 * Shows customer's debt history and payment records
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customerId: String,
    navController: NavController,
    viewModel: CreditViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    LaunchedEffect(customerId) {
        viewModel.loadCustomer(customerId)
    }

    val customer by viewModel.selectedCustomer.collectAsState()
    val customerCredits by viewModel.customerCredits.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showWhatsAppDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Loading state
    if (isLoading && customer == null) {
        LoadingState()
        return
    }

    // Error state
    error?.let { errorMessage ->
        ErrorState(
            message = errorMessage,
            onRetry = { viewModel.loadCustomer(customerId) }
        )
        return
    }

    // Empty state
    val currentCustomer = customer
    if (currentCustomer == null) {
        EmptyState(message = "Customer not found")
        return
    }

    // Calculate debt summary
    val unpaidCredits = customerCredits.filter { it.amountRemaining > 0 }
    val totalDebt = unpaidCredits.sumOf { it.amountRemaining }
    val unpaidTransactions = unpaidCredits.size

    Scaffold(
        containerColor = SlateBackground,
        topBar = {
            CustomerDetailTopBar(
                customerName = currentCustomer.name,
                hasPhone = !currentCustomer.phoneNumber.isNullOrBlank(),
                onBackClick = { navController.popBackStack() },
                onMessageClick = { showWhatsAppDialog = true }
            )
        },
        floatingActionButton = {
            if (totalDebt > 0) {
                ExtendedFloatingActionButton(
                    text = { Text("Record Payment") },
                    icon = { Icon(Icons.Default.Payment, contentDescription = null) },
                    onClick = {
                        navController.navigate(Screen.RecordPayment.createRoute(customerId))
                    }
                )
            }
        }
    ) { paddingValues ->
        CustomerDetailContent(
            customer = currentCustomer,
            customerCredits = customerCredits,
            totalDebt = totalDebt,
            unpaidTransactions = unpaidTransactions,
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        )
    }

    // WhatsApp reminder dialog
    if (showWhatsAppDialog) {
        WhatsAppReminderDialog(
            customerName = currentCustomer.name,
            totalDebt = totalDebt,
            phoneNumber = currentCustomer.phoneNumber ?: "",
            onDismiss = { showWhatsAppDialog = false },
            onSend = { message ->
                val phoneNumber = currentCustomer.phoneNumber ?: ""
                if (phoneNumber.isNotBlank()) {
                    val success = WhatsAppHelper.sendWhatsAppMessage(
                        context = context,
                        phoneNumber = phoneNumber,
                        message = message
                    )
                    if (!success) {
                        Toast.makeText(
                            context,
                            "Could not open WhatsApp. Please ensure it is installed.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                showWhatsAppDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerDetailTopBar(
    customerName: String,
    hasPhone: Boolean,
    onBackClick: () -> Unit,
    onMessageClick: () -> Unit
) {
    TopAppBar(
        title = { Text(customerName) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            if (hasPhone) {
                IconButton(onClick = onMessageClick) {
                    Icon(Icons.Default.Message, contentDescription = "Send Reminder")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
private fun CustomerDetailContent(
    customer: com.example.dukaai.data.local.entity.CustomerEntity,
    customerCredits: List<com.example.dukaai.data.local.entity.CreditLedgerEntity>,
    totalDebt: Double,
    unpaidTransactions: Int,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Customer header
        item {
            CustomerHeaderCard(customer)
        }

        // Outstanding debt summary
        item {
            DebtSummaryCard(totalDebt, unpaidTransactions)
        }

        // Credit transactions section
        item {
            SectionHeader(title = "CREDIT TRANSACTIONS")
        }

        if (customerCredits.isEmpty()) {
            item {
                EmptyStateCard(message = "No credit transactions yet")
            }
        } else {
            items(customerCredits) { credit ->
                CreditTransactionCard(credit)
            }
        }

        // Payment history section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(title = "PAYMENT HISTORY")
        }

        item {
            EmptyStateCard(
                message = "Payment tracking coming soon",
                icon = {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Error loading customer",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
