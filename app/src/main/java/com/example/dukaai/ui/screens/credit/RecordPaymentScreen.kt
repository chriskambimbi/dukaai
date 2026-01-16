package com.example.dukaai.ui.screens.credit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.dukaai.data.local.entity.PaymentEntity
import com.example.dukaai.ui.viewmodel.CreditViewModel
import com.example.dukaai.ui.theme.SlateBackground
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen for recording a payment for a customer's credit
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordPaymentScreen(
    customerId: String,
    navController: NavController,
    viewModel: CreditViewModel = hiltViewModel()
) {
    // Load customer data
    LaunchedEffect(customerId) {
        viewModel.loadCustomer(customerId)
    }

    // Observe states
    val customer by viewModel.selectedCustomer.collectAsState()
    val customerCredits by viewModel.customerCredits.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Form state
    var selectedCreditId by remember { mutableStateOf<String?>(null) }
    var paymentAmount by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("CASH") }
    var notes by remember { mutableStateOf("") }
    var showMethodMenu by remember { mutableStateOf(false) }

    // Validation states
    var amountError by remember { mutableStateOf<String?>(null) }

    // Success dialog
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Payment methods
    val paymentMethods = listOf(
        "CASH" to "Cash",
        "MOBILE_MONEY" to "Mobile Money",
        "BANK_TRANSFER" to "Bank Transfer"
    )

    // Calculate totals
    val totalDebt = customerCredits.sumOf { it.amountRemaining }
    val unpaidCredits = customerCredits.filter { it.status != "PAID" }

    // Get selected credit
    val selectedCredit = customerCredits.find { it.id == selectedCreditId }

    // Validation
    fun validatePayment(): Boolean {
        val amount = paymentAmount.toDoubleOrNull()

        if (paymentAmount.isBlank()) {
            amountError = "Payment amount is required"
            return false
        }
        if (amount == null || amount <= 0) {
            amountError = "Amount must be greater than 0"
            return false
        }
        if (selectedCredit != null && amount > selectedCredit.amountRemaining) {
            amountError = "Amount exceeds remaining balance (K${String.format("%.2f", selectedCredit.amountRemaining)})"
            return false
        }
        if (selectedCreditId == null && amount > totalDebt) {
            amountError = "Amount exceeds total debt (K${String.format("%.2f", totalDebt)})"
            return false
        }

        amountError = null
        return true
    }

    // Submit payment
    fun submitPayment() {
        if (!validatePayment()) return

        val amount = paymentAmount.toDouble()

        // If specific credit selected, record payment for that credit
        if (selectedCreditId != null) {
            val payment = PaymentEntity(
                creditId = selectedCreditId!!,
                amount = amount,
                paymentMethod = paymentMethod,
                notes = notes.trim().takeIf { it.isNotBlank() }
            )
            viewModel.recordPayment(selectedCreditId!!, payment)
            showSuccessDialog = true
        } else {
            // If no credit selected, apply to oldest unpaid credits first
            var remainingAmount = amount
            unpaidCredits.sortedBy { it.createdAt }.forEach { credit ->
                if (remainingAmount > 0) {
                    val paymentForCredit = minOf(remainingAmount, credit.amountRemaining)
                    val payment = PaymentEntity(
                        creditId = credit.id,
                        amount = paymentForCredit,
                        paymentMethod = paymentMethod,
                        notes = notes.trim().takeIf { it.isNotBlank() }
                    )
                    viewModel.recordPayment(credit.id, payment)
                    remainingAmount -= paymentForCredit
                }
            }
            showSuccessDialog = true
        }
    }

    Scaffold(
        containerColor = SlateBackground,
        topBar = {
            TopAppBar(
                title = { Text("Record Payment") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (customer == null) {
            // Loading state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Customer info card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Column {
                                Text(
                                    text = customer!!.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                if (customer!!.phoneNumber != null) {
                                    Text(
                                        text = customer!!.phoneNumber!!,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Total debt summary
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (totalDebt > 0) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.tertiaryContainer
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "OUTSTANDING BALANCE",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "K ${String.format("%.2f", totalDebt)}",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${unpaidCredits.size} unpaid transaction(s)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                // Credit selection (if multiple credits)
                if (unpaidCredits.size > 1) {
                    Text(
                        text = "Payment For",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // All credits option
                        FilterChip(
                            selected = selectedCreditId == null,
                            onClick = { selectedCreditId = null },
                            label = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("All Outstanding Credits")
                                    Text(
                                        text = "K ${String.format("%.2f", totalDebt)}",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Individual credits
                        unpaidCredits.forEach { credit ->
                            FilterChip(
                                selected = selectedCreditId == credit.id,
                                onClick = { selectedCreditId = credit.id },
                                label = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Credit #${credit.id.take(8)}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                                    .format(Date(credit.createdAt)),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                        Text(
                                            text = "K ${String.format("%.2f", credit.amountRemaining)}",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Payment amount
                OutlinedTextField(
                    value = paymentAmount,
                    onValueChange = {
                        paymentAmount = it
                        amountError = null
                    },
                    label = { Text("Payment Amount *") },
                    placeholder = { Text("0.00") },
                    leadingIcon = {
                        Text(
                            "K",
                            modifier = Modifier.padding(start = 12.dp),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    isError = amountError != null,
                    supportingText = {
                        if (amountError != null) {
                            Text(
                                text = amountError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text("Enter the amount received from customer")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                // Calculate remaining balance
                val amount = paymentAmount.toDoubleOrNull() ?: 0.0
                if (amount > 0) {
                    val applicableDebt = selectedCredit?.amountRemaining ?: totalDebt
                    val remaining = applicableDebt - amount

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (remaining <= 0) {
                                MaterialTheme.colorScheme.tertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Payment Amount:")
                                Text(
                                    text = "K ${String.format("%.2f", amount)}",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Current Balance:")
                                Text(
                                    text = "K ${String.format("%.2f", applicableDebt)}",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Divider()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Remaining Balance:",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "K ${String.format("%.2f", maxOf(0.0, remaining))}",
                                    fontWeight = FontWeight.Bold,
                                    color = if (remaining <= 0) {
                                        MaterialTheme.colorScheme.tertiary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                            if (remaining <= 0) {
                                Text(
                                    text = "✓ Customer will be paid in full!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Payment method
                ExposedDropdownMenuBox(
                    expanded = showMethodMenu,
                    onExpandedChange = { showMethodMenu = !showMethodMenu }
                ) {
                    OutlinedTextField(
                        value = paymentMethods.find { it.first == paymentMethod }?.second ?: "Cash",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Method") },
                        leadingIcon = {
                            Icon(Icons.Default.Payment, contentDescription = null)
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showMethodMenu)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        singleLine = true
                    )

                    ExposedDropdownMenu(
                        expanded = showMethodMenu,
                        onDismissRequest = { showMethodMenu = false }
                    ) {
                        paymentMethods.forEach { (value, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    paymentMethod = value
                                    showMethodMenu = false
                                },
                                leadingIcon = {
                                    if (paymentMethod == value) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                }
                            )
                        }
                    }
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("e.g., Partial payment, paid via MTN") },
                    leadingIcon = {
                        Icon(Icons.Default.Notes, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Submit button
                Button(
                    onClick = { submitPayment() },
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
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Record Payment")
                    }
                }

                // Cancel button
                OutlinedButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text("Cancel")
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
    }

    // Success dialog
    if (showSuccessDialog) {
        val amount = paymentAmount.toDoubleOrNull() ?: 0.0
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Payment Recorded Successfully") },
            text = {
                Column {
                    Text("Payment of K${String.format("%.2f", amount)} has been recorded for ${customer?.name}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "New balance: K${String.format("%.2f", maxOf(0.0, totalDebt - amount))}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    navController.navigateUp()
                }) {
                    Text("Done")
                }
            }
        )
    }
}
