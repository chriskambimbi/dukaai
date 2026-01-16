package com.example.dukaai.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.dukaai.data.sync.ConflictResolution
import com.example.dukaai.data.sync.NetworkType
import com.example.dukaai.ui.viewmodel.SyncViewModel
import com.example.dukaai.ui.theme.SlateBackground

/**
 * Sync Settings Screen
 * Manages cloud backup and sync configuration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncSettingsScreen(
    navController: NavController,
    viewModel: SyncViewModel = hiltViewModel()
) {
    val syncConfig by viewModel.syncConfig.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncProgress by viewModel.syncProgress.collectAsState()
    val lastSyncTime by viewModel.lastSyncTimestamp.collectAsState()
    val lastSyncStatus by viewModel.lastSyncStatus.collectAsState()
    val pendingConflictsCount by viewModel.pendingConflictsCount.collectAsState()
    val networkType by viewModel.networkType.collectAsState(initial = NetworkType.NONE)

    var showIntervalDialog by remember { mutableStateOf(false) }
    var showConflictResolutionDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = SlateBackground,
        topBar = {
            TopAppBar(
                title = { Text("Sync Settings") },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sync Status Card
            item {
                SyncStatusCard(
                    isSyncing = isSyncing,
                    syncProgress = syncProgress,
                    lastSyncTime = lastSyncTime,
                    lastSyncStatus = lastSyncStatus,
                    networkType = networkType,
                    onManualSync = { viewModel.performManualSync() }
                )
            }

            // Conflicts Alert (if any)
            if (pendingConflictsCount > 0) {
                item {
                    ConflictsAlert(
                        conflictsCount = pendingConflictsCount,
                        onResolveClick = { /* TODO: Navigate to conflicts screen */ }
                    )
                }
            }

            // Auto Sync Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader("Automatic Sync")
            }

            item {
                SyncToggleCard(
                    title = "Auto Sync",
                    subtitle = "Automatically sync data in background",
                    icon = Icons.Default.Sync,
                    checked = syncConfig.autoSync,
                    onCheckedChange = { viewModel.setAutoSync(it) }
                )
            }

            item {
                SyncToggleCard(
                    title = "WiFi Only",
                    subtitle = "Sync only when connected to WiFi",
                    icon = Icons.Default.Wifi,
                    checked = syncConfig.syncOnWiFiOnly,
                    onCheckedChange = { viewModel.setSyncWiFiOnly(it) },
                    enabled = syncConfig.autoSync
                )
            }

            item {
                SyncSettingCard(
                    title = "Sync Interval",
                    subtitle = "${syncConfig.syncInterval / 60000} minutes",
                    icon = Icons.Default.Schedule,
                    onClick = { showIntervalDialog = true },
                    enabled = syncConfig.autoSync
                )
            }

            // Conflict Resolution Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader("Conflict Resolution")
            }

            item {
                SyncSettingCard(
                    title = "Resolution Strategy",
                    subtitle = getConflictResolutionLabel(syncConfig.conflictResolution),
                    icon = Icons.Default.CompareArrows,
                    onClick = { showConflictResolutionDialog = true }
                )
            }

            // Manual Actions Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader("Manual Actions")
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.uploadToCloud() },
                        modifier = Modifier.weight(1f),
                        enabled = !isSyncing
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Upload")
                    }

                    OutlinedButton(
                        onClick = { viewModel.downloadFromCloud() },
                        modifier = Modifier.weight(1f),
                        enabled = !isSyncing
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download")
                    }
                }
            }
        }
    }

    // Sync Interval Dialog
    if (showIntervalDialog) {
        SyncIntervalDialog(
            currentInterval = syncConfig.syncInterval,
            onIntervalSelected = { viewModel.setSyncInterval(it) },
            onDismiss = { showIntervalDialog = false }
        )
    }

    // Conflict Resolution Dialog
    if (showConflictResolutionDialog) {
        ConflictResolutionDialog(
            currentStrategy = syncConfig.conflictResolution,
            onStrategySelected = { viewModel.setConflictResolution(it) },
            onDismiss = { showConflictResolutionDialog = false }
        )
    }
}

@Composable
private fun SyncStatusCard(
    isSyncing: Boolean,
    syncProgress: Float,
    lastSyncTime: Long,
    lastSyncStatus: String,
    networkType: NetworkType,
    onManualSync: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Sync Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = lastSyncStatus,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }

                // Network indicator
                Icon(
                    imageVector = when (networkType) {
                        NetworkType.WIFI -> Icons.Default.Wifi
                        NetworkType.MOBILE -> Icons.Default.SignalCellularAlt
                        NetworkType.NONE -> Icons.Default.SignalWifiOff
                    },
                    contentDescription = "Network status",
                    tint = when (networkType) {
                        NetworkType.WIFI -> MaterialTheme.colorScheme.primary
                        NetworkType.MOBILE -> MaterialTheme.colorScheme.tertiary
                        NetworkType.NONE -> MaterialTheme.colorScheme.error
                    }
                )
            }

            if (isSyncing) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Syncing... ${(syncProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall
                    )
                    LinearProgressIndicator(
                        progress = syncProgress,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Button(
                    onClick = onManualSync,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sync Now")
                }
            }

            if (lastSyncTime > 0) {
                Text(
                    text = "Last synced: ${getFormattedTime(lastSyncTime)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun ConflictsAlert(
    conflictsCount: Int,
    onResolveClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Column {
                    Text(
                        text = "$conflictsCount Sync Conflicts",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Requires resolution",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                    )
                }
            }

            TextButton(onClick = onResolveClick) {
                Text("Resolve")
            }
        }
    }
}

@Composable
private fun SyncToggleCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column {
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
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
    }
}

@Composable
private fun SyncSettingCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        enabled = enabled,
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column {
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
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SyncIntervalDialog(
    currentInterval: Long,
    onIntervalSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val intervals = listOf(
        5L * 60 * 1000 to "5 minutes",
        15L * 60 * 1000 to "15 minutes",
        30L * 60 * 1000 to "30 minutes",
        60L * 60 * 1000 to "1 hour",
        3L * 60 * 60 * 1000 to "3 hours",
        6L * 60 * 60 * 1000 to "6 hours"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sync Interval") },
        text = {
            LazyColumn {
                items(intervals.size) { index ->
                    val (interval, label) = intervals[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = interval == currentInterval,
                            onClick = {
                                onIntervalSelected(interval)
                                onDismiss()
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label)
                    }
                }
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
private fun ConflictResolutionDialog(
    currentStrategy: ConflictResolution,
    onStrategySelected: (ConflictResolution) -> Unit,
    onDismiss: () -> Unit
) {
    val strategies = listOf(
        Triple(ConflictResolution.NEWEST_WINS, "Newest Wins", "Use the most recently updated version"),
        Triple(ConflictResolution.LOCAL_WINS, "Local Wins", "Always keep local changes"),
        Triple(ConflictResolution.REMOTE_WINS, "Remote Wins", "Always accept cloud changes"),
        Triple(ConflictResolution.MANUAL, "Manual", "Review each conflict manually")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Conflict Resolution") },
        text = {
            LazyColumn {
                items(strategies.size) { index ->
                    val (strategy, label, description) = strategies[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        RadioButton(
                            selected = strategy == currentStrategy,
                            onClick = {
                                onStrategySelected(strategy)
                                onDismiss()
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(label, fontWeight = FontWeight.Medium)
                            Text(
                                description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

private fun getConflictResolutionLabel(strategy: ConflictResolution): String {
    return when (strategy) {
        ConflictResolution.NEWEST_WINS -> "Newest Wins"
        ConflictResolution.LOCAL_WINS -> "Local Wins"
        ConflictResolution.REMOTE_WINS -> "Remote Wins"
        ConflictResolution.MANUAL -> "Manual Resolution"
    }
}

private fun getFormattedTime(timestamp: Long): String {
    if (timestamp == 0L) return "Never"

    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000} minutes ago"
        diff < 86400_000 -> "${diff / 3600_000} hours ago"
        diff < 604800_000 -> "${diff / 86400_000} days ago"
        else -> "Over a week ago"
    }
}
