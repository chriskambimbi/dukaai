package com.example.dukaai.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.dukaai.ui.viewmodel.OperationResult
import com.example.dukaai.ui.viewmodel.SettingsViewModel

/**
 * Settings Screen
 * Provides app configuration and preferences
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val selectedLanguage by viewModel.language.collectAsState()
    val selectedCurrency by viewModel.currency.collectAsState()
    val lowStockThreshold by viewModel.lowStockThreshold.collectAsState()
    val pinEnabled by viewModel.pinEnabled.collectAsState()
    val stockAlertsEnabled by viewModel.stockAlertsEnabled.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val operationResult by viewModel.operationResult.collectAsState()

    val context = LocalContext.current

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showThresholdDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }

    // Handle operation result
    LaunchedEffect(operationResult) {
        operationResult?.let { result ->
            when (result) {
                is OperationResult.Success -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
                is OperationResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
            }
            viewModel.clearOperationResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // General Section
            item {
                SectionHeader("General")
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Language,
                    title = "Language",
                    subtitle = selectedLanguage,
                    onClick = { showLanguageDialog = true }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.AttachMoney,
                    title = "Currency",
                    subtitle = selectedCurrency,
                    onClick = { showCurrencyDialog = true }
                )
            }

            // Inventory Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader("Inventory")
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Inventory,
                    title = "Low Stock Threshold",
                    subtitle = "Alert when stock falls below $lowStockThreshold units",
                    onClick = { showThresholdDialog = true }
                )
            }

            item {
                SettingsToggleItem(
                    icon = Icons.Default.Notifications,
                    title = "Stock Alerts",
                    subtitle = if (stockAlertsEnabled) "Notifications enabled" else "Notifications disabled",
                    checked = stockAlertsEnabled,
                    onCheckedChange = { viewModel.setStockAlertsEnabled(it) }
                )
            }

            // Data Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader("Data")
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Sync,
                    title = "Sync Settings",
                    subtitle = "Configure cloud backup and sync",
                    onClick = { navController.navigate("sync_settings") }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Backup,
                    title = "Backup Data",
                    subtitle = "Export your data to storage",
                    onClick = { showBackupDialog = true }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.CloudUpload,
                    title = "Restore Data",
                    subtitle = "Import data from backup",
                    onClick = { showRestoreDialog = true }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.DeleteForever,
                    title = "Clear All Data",
                    subtitle = "Delete all products, sales, and credits",
                    onClick = { showClearDataDialog = true },
                    isDestructive = true
                )
            }

            // Security Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader("Security")
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "PIN Protection",
                    subtitle = if (pinEnabled) "PIN protection enabled" else "Set up PIN to secure the app",
                    onClick = { showPinDialog = true }
                )
            }

            // About Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader("About")
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "App Version",
                    subtitle = "Duka.AI v1.0.0 (Build 1)",
                    onClick = { showAboutDialog = true }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Description,
                    title = "Privacy Policy",
                    subtitle = "View our privacy policy",
                    onClick = { /* TODO: Open privacy policy */ }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Help,
                    title = "Help & Support",
                    subtitle = "Get help using Duka.AI",
                    onClick = { /* TODO: Navigate to help */ }
                )
            }
        }
    }

    // Language Selection Dialog
    if (showLanguageDialog) {
        val languages = listOf("English", "Nyanja", "Bemba")
        SelectionDialog(
            title = "Select Language",
            options = languages,
            selectedOption = selectedLanguage,
            onOptionSelected = { viewModel.setLanguage(it) },
            onDismiss = { showLanguageDialog = false }
        )
    }

    // Currency Selection Dialog
    if (showCurrencyDialog) {
        val currencies = listOf(
            "ZMW (Zambian Kwacha)",
            "USD (US Dollar)",
            "EUR (Euro)",
            "GBP (British Pound)"
        )
        SelectionDialog(
            title = "Select Currency",
            options = currencies,
            selectedOption = selectedCurrency,
            onOptionSelected = { viewModel.setCurrency(it) },
            onDismiss = { showCurrencyDialog = false }
        )
    }

    // Low Stock Threshold Dialog
    if (showThresholdDialog) {
        ThresholdDialog(
            currentThreshold = lowStockThreshold,
            onThresholdChanged = { viewModel.setLowStockThreshold(it) },
            onDismiss = { showThresholdDialog = false }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AboutDialog(
            onDismiss = { showAboutDialog = false }
        )
    }

    // PIN Setup Dialog
    if (showPinDialog) {
        PinSetupDialog(
            pinEnabled = pinEnabled,
            onDismiss = { showPinDialog = false },
            onPinSet = { pin ->
                viewModel.setPinCode(pin)
                showPinDialog = false
            },
            onPinDisabled = {
                viewModel.setPinEnabled(false)
                showPinDialog = false
            }
        )
    }

    // Backup Dialog
    if (showBackupDialog) {
        BackupDialog(
            isLoading = isLoading,
            onDismiss = { showBackupDialog = false },
            onBackup = {
                viewModel.backupData { success, message ->
                    if (success) {
                        showBackupDialog = false
                    }
                }
            }
        )
    }

    // Restore Dialog
    if (showRestoreDialog) {
        RestoreDialog(
            availableBackups = viewModel.getAvailableBackups(),
            isLoading = isLoading,
            onDismiss = { showRestoreDialog = false },
            onRestore = { backupPath ->
                viewModel.restoreData(backupPath) { success, message ->
                    if (success) {
                        showRestoreDialog = false
                    }
                }
            }
        )
    }

    // Clear Data Confirmation Dialog
    if (showClearDataDialog) {
        ClearDataDialog(
            isLoading = isLoading,
            onDismiss = { showClearDataDialog = false },
            onConfirm = {
                viewModel.clearAllData { success, message ->
                    if (success) {
                        showClearDataDialog = false
                    }
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isDestructive)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isDestructive)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SelectionDialog(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn {
                items(options) { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onOptionSelected(option)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == selectedOption,
                            onClick = {
                                onOptionSelected(option)
                                onDismiss()
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = option)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ThresholdDialog(
    currentThreshold: Int,
    onThresholdChanged: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var thresholdText by remember { mutableStateOf(currentThreshold.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Low Stock Threshold") },
        text = {
            Column {
                Text(
                    text = "Products will be marked as low stock when quantity falls below this value.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = thresholdText,
                    onValueChange = { thresholdText = it },
                    label = { Text("Threshold") },
                    suffix = { Text("units") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    thresholdText.toIntOrNull()?.let {
                        if (it > 0) {
                            onThresholdChanged(it)
                            onDismiss()
                        }
                    }
                }
            ) {
                Text("Save")
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
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun AboutDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.ShoppingBag,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Duka.AI",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Version 1.0.0 (Build 1)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Smart inventory management for Zambian retailers",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "© 2025 Duka.AI. All rights reserved.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun PinSetupDialog(
    pinEnabled: Boolean,
    onDismiss: () -> Unit,
    onPinSet: (String) -> Unit,
    onPinDisabled: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Lock, contentDescription = null) },
        title = { Text(if (pinEnabled) "Update PIN" else "Set Up PIN") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Create a 4-digit PIN to protect your app.",
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 4) pin = it },
                    label = { Text("Enter PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 4) confirmPin = it },
                    label = { Text("Confirm PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (pinEnabled) {
                    TextButton(
                        onClick = onPinDisabled,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Disable PIN Protection")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        pin.length != 4 -> error = "PIN must be 4 digits"
                        pin != confirmPin -> error = "PINs do not match"
                        else -> onPinSet(pin)
                    }
                },
                enabled = pin.length == 4 && confirmPin.length == 4
            ) {
                Text("Set PIN")
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
private fun BackupDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onBackup: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        icon = { Icon(Icons.Default.Backup, contentDescription = null) },
        title = { Text("Backup Data") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Create a backup of all your data including:")
                Text("• Products and inventory")
                Text("• Customers and credit records")
                Text("• Sales history")
                Text("• Settings")

                if (isLoading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Text("Creating backup...")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onBackup,
                enabled = !isLoading
            ) {
                Text("Create Backup")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun RestoreDialog(
    availableBackups: List<java.io.File>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onRestore: (String) -> Unit
) {
    var selectedBackup by remember { mutableStateOf<java.io.File?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        icon = { Icon(Icons.Default.CloudUpload, contentDescription = null) },
        title = { Text("Restore Data") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (availableBackups.isEmpty()) {
                    Text("No backups found. Create a backup first.")
                } else {
                    Text("Select a backup to restore:")
                    availableBackups.forEach { backup ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedBackup = backup },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedBackup == backup)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedBackup == backup,
                                    onClick = { selectedBackup = backup }
                                )
                                Column(modifier = Modifier.padding(start = 8.dp)) {
                                    Text(
                                        text = backup.name,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = java.text.SimpleDateFormat("MMM d, yyyy HH:mm", java.util.Locale.getDefault())
                                            .format(java.util.Date(backup.lastModified())),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                if (isLoading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Text("Restoring data...")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedBackup?.let { onRestore(it.absolutePath) } },
                enabled = !isLoading && selectedBackup != null
            ) {
                Text("Restore")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ClearDataDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var confirmText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                "Clear All Data",
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "This will permanently delete ALL data including:",
                    fontWeight = FontWeight.Bold
                )
                Text("• All products and inventory")
                Text("• All customers and credit records")
                Text("• All sales history")
                Text("• All settings")

                Text(
                    "This action cannot be undone!",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )

                HorizontalDivider()

                Text("Type \"DELETE\" to confirm:")
                OutlinedTextField(
                    value = confirmText,
                    onValueChange = { confirmText = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (isLoading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Text("Clearing data...")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading && confirmText == "DELETE",
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Clear All Data")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}
