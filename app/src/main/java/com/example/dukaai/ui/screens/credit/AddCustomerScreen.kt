package com.example.dukaai.ui.screens.credit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.dukaai.data.local.entity.CustomerEntity
import com.example.dukaai.ui.viewmodel.CreditViewModel
import com.example.dukaai.ui.theme.SlateBackground

/**
 * Screen for adding a new customer
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomerScreen(
    navController: NavController,
    viewModel: CreditViewModel = hiltViewModel()
) {
    // Form state
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Validation error states
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    // Success dialog state
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Observe loading and error states
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    /**
     * Validate Zambian phone number
     * Accepts formats: +260971234567, 0971234567, 971234567
     */
    fun isValidZambianPhone(phone: String): Boolean {
        if (phone.isBlank()) return true // Optional field

        // Remove spaces and hyphens
        val cleanPhone = phone.replace(Regex("[\\s-]"), "")

        // Zambian phone patterns
        val patterns = listOf(
            "^\\+260[7-9][0-9]{8}$".toRegex(),  // +260971234567
            "^0[7-9][0-9]{8}$".toRegex(),        // 0971234567
            "^[7-9][0-9]{8}$".toRegex()          // 971234567
        )

        return patterns.any { it.matches(cleanPhone) }
    }

    /**
     * Format phone number for display
     */
    fun formatPhoneNumber(phone: String): String {
        val cleanPhone = phone.replace(Regex("[\\s-]"), "")
        return when {
            cleanPhone.startsWith("+260") && cleanPhone.length == 13 -> {
                "+260 ${cleanPhone.substring(4, 6)} ${cleanPhone.substring(6, 9)} ${cleanPhone.substring(9)}"
            }
            cleanPhone.startsWith("0") && cleanPhone.length == 10 -> {
                "${cleanPhone.substring(0, 4)} ${cleanPhone.substring(4, 7)} ${cleanPhone.substring(7)}"
            }
            cleanPhone.length == 9 -> {
                "${cleanPhone.substring(0, 2)} ${cleanPhone.substring(2, 5)} ${cleanPhone.substring(5)}"
            }
            else -> phone
        }
    }

    /**
     * Normalize phone number for storage
     */
    fun normalizePhoneNumber(phone: String): String {
        val cleanPhone = phone.replace(Regex("[\\s-]"), "")
        return when {
            cleanPhone.startsWith("+260") -> cleanPhone
            cleanPhone.startsWith("0") -> "+260${cleanPhone.substring(1)}"
            cleanPhone.startsWith("7") || cleanPhone.startsWith("9") -> "+260$cleanPhone"
            else -> cleanPhone
        }
    }

    // Validation function
    fun validateForm(): Boolean {
        var isValid = true

        // Name validation
        if (name.isBlank()) {
            nameError = "Customer name is required"
            isValid = false
        } else if (name.length < 2) {
            nameError = "Name must be at least 2 characters"
            isValid = false
        } else {
            nameError = null
        }

        // Phone validation
        if (phoneNumber.isNotBlank() && !isValidZambianPhone(phoneNumber)) {
            phoneError = "Invalid phone number format. Use +260971234567 or 0971234567"
            isValid = false
        } else {
            phoneError = null
        }

        return isValid
    }

    // Handle save
    fun saveCustomer() {
        if (validateForm()) {
            val customer = CustomerEntity(
                name = name.trim(),
                phoneNumber = if (phoneNumber.isNotBlank()) {
                    normalizePhoneNumber(phoneNumber.trim())
                } else null,
                address = address.trim().takeIf { it.isNotBlank() },
                notes = notes.trim().takeIf { it.isNotBlank() }
            )
            viewModel.addCustomer(customer)
            showSuccessDialog = true
        }
    }

    Scaffold(
        containerColor = SlateBackground,
        topBar = {
            TopAppBar(
                title = { Text("Add New Customer") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Add customer details to track credit sales and payment history",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Customer name field
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = null
                },
                label = { Text("Customer Name *") },
                placeholder = { Text("e.g., John Banda") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                },
                isError = nameError != null,
                supportingText = {
                    if (nameError != null) {
                        Text(
                            text = nameError!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Phone number field
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = {
                    phoneNumber = it
                    phoneError = null
                },
                label = { Text("Phone Number (Optional)") },
                placeholder = { Text("+260 97 123 4567") },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null)
                },
                isError = phoneError != null,
                supportingText = {
                    when {
                        phoneError != null -> {
                            Text(
                                text = phoneError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        phoneNumber.isNotBlank() && isValidZambianPhone(phoneNumber) -> {
                            Text(
                                text = "✓ Valid phone number",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        else -> {
                            Text(
                                text = "Zambian format: +260971234567 or 0971234567",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            // Phone format examples
            if (phoneNumber.isBlank()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Accepted phone formats:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "• +260 97 123 4567",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "• 0971234567",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "• 971234567",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Address field
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address (Optional)") },
                placeholder = { Text("e.g., Meanwood, Lusaka") },
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )

            // Notes field
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                placeholder = { Text("Any additional information about the customer") },
                leadingIcon = {
                    Icon(Icons.Default.Notes, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Save button
            Button(
                onClick = { saveCustomer() },
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
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Customer")
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

    // Success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Customer Added Successfully") },
            text = {
                Column {
                    Text("$name has been added to your customer list")
                    if (phoneNumber.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Phone: ${formatPhoneNumber(phoneNumber)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    navController.navigateUp()
                }) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    // Reset form for adding another customer
                    showSuccessDialog = false
                    name = ""
                    phoneNumber = ""
                    address = ""
                    notes = ""
                    viewModel.clearError()
                }) {
                    Text("Add Another")
                }
            }
        )
    }
}
