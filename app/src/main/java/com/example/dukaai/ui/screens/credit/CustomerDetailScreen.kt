package com.example.dukaai.ui.screens.credit

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.dukaai.data.local.entity.CustomerEntity
import com.example.dukaai.data.local.entity.CreditLedgerEntity
import com.example.dukaai.ui.components.PaymentStatus
import com.example.dukaai.ui.navigation.Screen
import com.example.dukaai.ui.viewmodel.CreditViewModel
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    // Load customer data from ViewModel
    LaunchedEffect(customerId) {
        viewModel.loadCustomer(customerId)
    }

    val customer by viewModel.selectedCustomer.collectAsState()
    val customerCredits by viewModel.customerCredits.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showWhatsAppDialog by remember { mutableStateOf(false) }

    // Show loading state
    if (isLoading && customer == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Show error state
    if (error != null) {
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
                    text = error ?: "Unknown error",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(onClick = { viewModel.loadCustomer(customerId) }) {
                    Text("Retry")
                }
            }
        }
        return
    }

    // Show empty state if customer not found
    if (customer == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Customer not found",
                style = MaterialTheme.typography.titleMedium
            )
        }
        return
    }

    val currentCustomer = customer!!

    // Calculate total debt and unpaid transactions from credits
    val unpaidCredits = customerCredits.filter { it.amountRemaining > 0 }
    val totalDebt = unpaidCredits.sumOf { it.amountRemaining }
    val unpaidTransactions = unpaidCredits.size

    // TODO: Payment history will come from PaymentRepository once implemented
    val paymentHistory = remember {
        emptyList<PaymentRecord>()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentCustomer.name) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (currentCustomer.phoneNumber != null && currentCustomer.phoneNumber!!.isNotBlank()) {
                        IconButton(onClick = { showWhatsAppDialog = true }) {
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
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Customer header
            item {
                CustomerHeaderCard(currentCustomer)
            }

            // Outstanding debt summary
            item {
                DebtSummaryCard(totalDebt, unpaidTransactions)
            }

            // Credit transactions section
            item {
                Text(
                    text = "CREDIT TRANSACTIONS",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (customerCredits.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No credit transactions yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(customerCredits) { credit ->
                    CreditTransactionCard(credit)
                }
            }

            // Payment history section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "PAYMENT HISTORY",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (paymentHistory.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Payment tracking coming soon",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(paymentHistory) { payment ->
                    PaymentRecordCard(payment)
                }
            }
        }
    }

    // WhatsApp reminder dialog
    if (showWhatsAppDialog) {
        WhatsAppReminderDialog(
            customerName = currentCustomer.name,
            totalDebt = totalDebt,
            phoneNumber = currentCustomer.phoneNumber ?: "",
            onDismiss = { showWhatsAppDialog = false },
            onSend = {
                // TODO: Handle WhatsApp send
                showWhatsAppDialog = false
            }
        )
    }
}

@Composable
private fun CustomerHeaderCard(
    customer: CustomerEntity,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar
            Surface(
                modifier = Modifier.size(64.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // Customer info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                if (customer.phoneNumber != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = customer.phoneNumber,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }

                if (customer.address != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = customer.address,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DebtSummaryCard(
    totalDebt: Double,
    unpaidTransactions: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (totalDebt > 0) {
                com.example.dukaai.ui.theme.ErrorRed.copy(alpha = 0.1f)
            } else {
                com.example.dukaai.ui.theme.SuccessGreen.copy(alpha = 0.1f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "OUTSTANDING DEBT",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "K ${String.format("%.2f", totalDebt)}",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = if (totalDebt > 0) {
                    com.example.dukaai.ui.theme.ErrorRed
                } else {
                    com.example.dukaai.ui.theme.SuccessGreen
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$unpaidTransactions unpaid transactions",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun CreditTransactionCard(
    credit: CreditLedgerEntity,
    modifier: Modifier = Modifier
) {
    // Format dates
    val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    val createdDate = dateFormat.format(Date(credit.createdAt))
    val dueDate = credit.dueDate?.let { dateFormat.format(Date(it)) } ?: "No due date"

    // Determine status and color
    val (statusText, statusColor) = when (credit.status) {
        "OVERDUE" -> "Overdue" to com.example.dukaai.ui.theme.ErrorRed
        "PAID" -> "Paid" to com.example.dukaai.ui.theme.SuccessGreen
        "PARTIAL" -> "Partial" to com.example.dukaai.ui.theme.WarningYellow
        else -> "Pending" to com.example.dukaai.ui.theme.WarningYellow
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Credit #${credit.id.take(8)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Purchased: $createdDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Text(
                        text = "Due: $dueDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    if (credit.amountPaid > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Paid: K ${String.format("%.2f", credit.amountPaid)} / Remaining: K ${String.format("%.2f", credit.amountRemaining)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "K ${String.format("%.2f", credit.amount)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = statusColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = statusText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentRecordCard(
    payment: PaymentRecord,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = com.example.dukaai.ui.theme.SuccessGreen,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Payment received",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = payment.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    if (payment.note != null) {
                        Text(
                            text = payment.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }

            Text(
                text = "K ${String.format("%.2f", payment.amount)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = com.example.dukaai.ui.theme.SuccessGreen
            )
        }
    }
}

@Composable
private fun RecordPaymentDialog(
    customerName: String,
    totalDebt: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double, String?) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val paymentAmount = amount.toDoubleOrNull() ?: 0.0
    val remainingDebt = totalDebt - paymentAmount

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Payment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Customer: $customerName",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "Total Owed: K ${String.format("%.2f", totalDebt)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Divider()

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Payment Amount") },
                    placeholder = { Text("0.00") },
                    leadingIcon = { Text("K") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (Optional)") },
                    placeholder = { Text("e.g., Partial payment") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                if (paymentAmount > 0) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
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
                                Text("Payment:")
                                Text(
                                    text = "K ${String.format("%.2f", paymentAmount)}",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Remaining:")
                                Text(
                                    text = "K ${String.format("%.2f", remainingDebt)}",
                                    fontWeight = FontWeight.Bold,
                                    color = if (remainingDebt <= 0) {
                                        com.example.dukaai.ui.theme.SuccessGreen
                                    } else {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    }
                                )
                            }

                            if (remainingDebt <= 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "🎉 Customer will be paid in full!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = com.example.dukaai.ui.theme.SuccessGreen
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(paymentAmount, note.ifBlank { null }) },
                enabled = paymentAmount > 0 && paymentAmount <= totalDebt
            ) {
                Text("Record Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun WhatsAppReminderDialog(
    customerName: String,
    totalDebt: Double,
    phoneNumber: String,
    onDismiss: () -> Unit,
    onSend: () -> Unit
) {
    val message = "Hello $customerName, friendly reminder that you have a balance of K ${String.format("%.2f", totalDebt)} at Duka.AI. Thank you!"

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Message, contentDescription = null) },
        title = { Text("Send WhatsApp Reminder") },
        text = {
            Column {
                Text("To: $phoneNumber")
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onSend) {
                Text("Send via WhatsApp")
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
private data class PaymentRecord(
    val id: String,
    val amount: Double,
    val date: String,
    val note: String?
)
