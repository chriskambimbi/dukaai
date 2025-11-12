package com.example.dukaai.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
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

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showThresholdDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

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
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Stock Alerts",
                    subtitle = "Enable notifications for low stock",
                    onClick = { /* TODO: Navigate to notification settings */ }
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
                    onClick = { /* TODO: Implement backup */ }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.CloudUpload,
                    title = "Restore Data",
                    subtitle = "Import data from backup",
                    onClick = { /* TODO: Implement restore */ }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.DeleteForever,
                    title = "Clear All Data",
                    subtitle = "Delete all products, sales, and credits",
                    onClick = { /* TODO: Implement clear data with confirmation */ },
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
                    subtitle = "Require PIN to access the app",
                    onClick = { /* TODO: Navigate to PIN setup */ }
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
                Divider(modifier = Modifier.padding(vertical = 8.dp))
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
