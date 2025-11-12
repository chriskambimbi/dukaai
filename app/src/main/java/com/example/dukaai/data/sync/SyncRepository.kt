package com.example.dukaai.data.sync

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sync Repository
 * Manages sync operations, state, and configuration
 */
@Singleton
class SyncRepository @Inject constructor(
    private val firebaseSyncService: FirebaseSyncService,
    private val conflictResolutionHandler: ConflictResolutionHandler,
    private val connectivityManager: DukaConnectivityManager,
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private const val TAG = "SyncRepository"

        // DataStore keys
        private val KEY_AUTO_SYNC = booleanPreferencesKey("auto_sync")
        private val KEY_SYNC_WIFI_ONLY = booleanPreferencesKey("sync_wifi_only")
        private val KEY_SYNC_INTERVAL = longPreferencesKey("sync_interval")
        private val KEY_CONFLICT_RESOLUTION = stringPreferencesKey("conflict_resolution")
        private val KEY_LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
        private val KEY_LAST_SYNC_STATUS = stringPreferencesKey("last_sync_status")

        // Default values
        private const val DEFAULT_SYNC_INTERVAL = 15L * 60 * 1000L // 15 minutes
    }

    // Sync state
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncProgress = MutableStateFlow(0f)
    val syncProgress: StateFlow<Float> = _syncProgress.asStateFlow()

    private val _lastSyncResult = MutableStateFlow<SyncResult?>(null)
    val lastSyncResult: StateFlow<SyncResult?> = _lastSyncResult.asStateFlow()

    // Sync configuration from DataStore
    val syncConfig: Flow<SyncConfig> = dataStore.data.map { preferences ->
        SyncConfig(
            autoSync = preferences[KEY_AUTO_SYNC] ?: true,
            syncOnWiFiOnly = preferences[KEY_SYNC_WIFI_ONLY] ?: true,
            syncInterval = preferences[KEY_SYNC_INTERVAL] ?: DEFAULT_SYNC_INTERVAL,
            conflictResolution = ConflictResolution.valueOf(
                preferences[KEY_CONFLICT_RESOLUTION] ?: ConflictResolution.NEWEST_WINS.name
            )
        )
    }

    // Last sync info
    val lastSyncTimestamp: Flow<Long> = dataStore.data.map { preferences ->
        preferences[KEY_LAST_SYNC_TIMESTAMP] ?: 0L
    }

    val lastSyncStatus: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_LAST_SYNC_STATUS] ?: "Never synced"
    }

    // Conflict queue from handler
    val pendingConflicts: Flow<List<SyncConflict>> = conflictResolutionHandler.conflictQueue

    val pendingConflictsCount: Flow<Int> = conflictResolutionHandler.pendingConflictsCount

    /**
     * Get current sync configuration
     */
    suspend fun getSyncConfig(): SyncConfig {
        return syncConfig.first()
    }

    /**
     * Update auto-sync setting
     */
    suspend fun setAutoSync(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_AUTO_SYNC] = enabled
        }
        Log.d(TAG, "Auto-sync ${if (enabled) "enabled" else "disabled"}")
    }

    /**
     * Update WiFi-only sync setting
     */
    suspend fun setSyncWiFiOnly(wifiOnly: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_SYNC_WIFI_ONLY] = wifiOnly
        }
        Log.d(TAG, "WiFi-only sync ${if (wifiOnly) "enabled" else "disabled"}")
    }

    /**
     * Update sync interval
     */
    suspend fun setSyncInterval(intervalMillis: Long) {
        dataStore.edit { preferences ->
            preferences[KEY_SYNC_INTERVAL] = intervalMillis
        }
        Log.d(TAG, "Sync interval set to ${intervalMillis / 60000} minutes")
    }

    /**
     * Update conflict resolution strategy
     */
    suspend fun setConflictResolution(strategy: ConflictResolution) {
        dataStore.edit { preferences ->
            preferences[KEY_CONFLICT_RESOLUTION] = strategy.name
        }
        Log.d(TAG, "Conflict resolution set to $strategy")
    }

    /**
     * Perform manual sync
     */
    suspend fun performSync(forceUpload: Boolean = false): SyncResult {
        if (_isSyncing.value) {
            Log.w(TAG, "Sync already in progress")
            return SyncResult.Failure("Sync already in progress")
        }

        return try {
            _isSyncing.value = true
            _syncProgress.value = 0f

            Log.d(TAG, "Starting sync...")

            // Check connectivity
            val config = getSyncConfig()
            if (!connectivityManager.shouldSync(config)) {
                Log.w(TAG, "Sync skipped - network conditions not met")
                return SyncResult.Failure("Network not available or not on WiFi")
            }

            _syncProgress.value = 0.3f

            // Perform two-way sync
            val result = firebaseSyncService.performTwoWaySync()

            _syncProgress.value = 0.9f

            // Handle conflicts
            when (result) {
                is SyncResult.Success -> {
                    if (result.conflicts.isNotEmpty()) {
                        conflictResolutionHandler.addConflicts(result.conflicts)
                        Log.d(TAG, "Sync completed with ${result.conflicts.size} conflicts")
                    } else {
                        Log.d(TAG, "Sync completed successfully: ${result.syncedCount} items")
                    }
                }
                is SyncResult.Partial -> {
                    if (result.conflicts.isNotEmpty()) {
                        conflictResolutionHandler.addConflicts(result.conflicts)
                    }
                    Log.d(TAG, "Partial sync: ${result.syncedCount} synced, ${result.failedCount} failed")
                }
                is SyncResult.Failure -> {
                    Log.e(TAG, "Sync failed: ${result.error}")
                }
            }

            _syncProgress.value = 1f

            // Save sync result
            _lastSyncResult.value = result
            updateLastSyncInfo(result)

            result
        } catch (e: Exception) {
            Log.e(TAG, "Sync error", e)
            val failureResult = SyncResult.Failure(e.message ?: "Unknown error")
            _lastSyncResult.value = failureResult
            updateLastSyncInfo(failureResult, isError = true)
            failureResult
        } finally {
            _isSyncing.value = false
            _syncProgress.value = 0f
        }
    }

    /**
     * Upload local changes to cloud
     */
    suspend fun uploadToCloud(): SyncResult {
        return try {
            _isSyncing.value = true
            Log.d(TAG, "Uploading to cloud...")

            val result = firebaseSyncService.syncToCloud()
            _lastSyncResult.value = result
            updateLastSyncInfo(result)

            Log.d(TAG, "Upload completed")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Upload error", e)
            SyncResult.Failure(e.message ?: "Unknown error")
        } finally {
            _isSyncing.value = false
        }
    }

    /**
     * Download changes from cloud
     */
    suspend fun downloadFromCloud(): SyncResult {
        return try {
            _isSyncing.value = true
            Log.d(TAG, "Downloading from cloud...")

            val result = firebaseSyncService.syncFromCloud()
            _lastSyncResult.value = result
            updateLastSyncInfo(result)

            Log.d(TAG, "Download completed")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Download error", e)
            SyncResult.Failure(e.message ?: "Unknown error")
        } finally {
            _isSyncing.value = false
        }
    }

    /**
     * Resolve conflict
     */
    fun resolveConflict(conflict: SyncConflict, strategy: ConflictResolution? = null): Map<String, Any?> {
        return conflictResolutionHandler.resolveConflict(conflict, strategy)
    }

    /**
     * Resolve all conflicts
     */
    fun resolveAllConflicts(strategy: ConflictResolution): List<ResolvedConflict> {
        return conflictResolutionHandler.resolveAllConflicts(strategy)
    }

    /**
     * Clear all conflicts
     */
    fun clearConflicts() {
        conflictResolutionHandler.clearConflicts()
    }

    /**
     * Get conflicts by entity type
     */
    fun getConflictsByType(entityType: String): List<SyncConflict> {
        return conflictResolutionHandler.getConflictsByType(entityType)
    }

    /**
     * Check if sync is available
     */
    fun isSyncAvailable(): Boolean {
        return connectivityManager.isConnected()
    }

    /**
     * Observe network connectivity
     */
    fun observeNetworkType(): Flow<NetworkType> {
        return connectivityManager.observeNetworkType()
    }

    /**
     * Update last sync information
     */
    private suspend fun updateLastSyncInfo(result: SyncResult, isError: Boolean = false) {
        dataStore.edit { preferences ->
            preferences[KEY_LAST_SYNC_TIMESTAMP] = System.currentTimeMillis()
            preferences[KEY_LAST_SYNC_STATUS] = when (result) {
                is SyncResult.Success -> "Success (${result.syncedCount} items)"
                is SyncResult.Failure -> "Failed: ${result.error}"
                is SyncResult.Partial -> "Partial (${result.syncedCount}/${result.syncedCount + result.failedCount})"
            }
        }
    }

    /**
     * Get formatted last sync time
     */
    fun getFormattedLastSyncTime(timestamp: Long): String {
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
}
