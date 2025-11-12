package com.example.dukaai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dukaai.data.sync.ConflictResolution
import com.example.dukaai.data.sync.SyncConfig
import com.example.dukaai.data.sync.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Sync Settings
 */
@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncRepository: SyncRepository
) : ViewModel() {

    // Sync state
    val isSyncing: StateFlow<Boolean> = syncRepository.isSyncing

    val syncProgress: StateFlow<Float> = syncRepository.syncProgress

    // Sync configuration
    val syncConfig: StateFlow<SyncConfig> = syncRepository.syncConfig
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SyncConfig()
        )

    // Last sync info
    val lastSyncTimestamp: StateFlow<Long> = syncRepository.lastSyncTimestamp
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    val lastSyncStatus: StateFlow<String> = syncRepository.lastSyncStatus
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Never synced"
        )

    // Conflicts
    val pendingConflictsCount: StateFlow<Int> = syncRepository.pendingConflictsCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Network status
    val networkType = syncRepository.observeNetworkType()

    /**
     * Perform manual sync
     */
    fun performManualSync() {
        viewModelScope.launch {
            syncRepository.performSync()
        }
    }

    /**
     * Upload to cloud
     */
    fun uploadToCloud() {
        viewModelScope.launch {
            syncRepository.uploadToCloud()
        }
    }

    /**
     * Download from cloud
     */
    fun downloadFromCloud() {
        viewModelScope.launch {
            syncRepository.downloadFromCloud()
        }
    }

    /**
     * Set auto sync
     */
    fun setAutoSync(enabled: Boolean) {
        viewModelScope.launch {
            syncRepository.setAutoSync(enabled)
        }
    }

    /**
     * Set WiFi only sync
     */
    fun setSyncWiFiOnly(wifiOnly: Boolean) {
        viewModelScope.launch {
            syncRepository.setSyncWiFiOnly(wifiOnly)
        }
    }

    /**
     * Set sync interval
     */
    fun setSyncInterval(intervalMillis: Long) {
        viewModelScope.launch {
            syncRepository.setSyncInterval(intervalMillis)
        }
    }

    /**
     * Set conflict resolution strategy
     */
    fun setConflictResolution(strategy: ConflictResolution) {
        viewModelScope.launch {
            syncRepository.setConflictResolution(strategy)
        }
    }
}
