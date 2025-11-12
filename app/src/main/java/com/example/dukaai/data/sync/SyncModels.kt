package com.example.dukaai.data.sync

/**
 * Sync status for entities
 */
enum class SyncStatus {
    SYNCED,        // Up to date with cloud
    PENDING,       // Waiting to be synced
    SYNCING,       // Currently syncing
    CONFLICT,      // Conflict detected
    ERROR          // Sync error
}

/**
 * Sync operation type
 */
enum class SyncOperation {
    CREATE,
    UPDATE,
    DELETE
}

/**
 * Sync state for tracking last sync time
 */
data class SyncState(
    val entityType: String,
    val lastSyncTimestamp: Long = 0L,
    val status: SyncStatus = SyncStatus.SYNCED,
    val errorMessage: String? = null
)

/**
 * Sync item representing a change to be synced
 */
data class SyncItem(
    val id: String,
    val entityType: String,
    val entityId: String,
    val operation: SyncOperation,
    val timestamp: Long,
    val data: Map<String, Any?>,
    val status: SyncStatus = SyncStatus.PENDING,
    val retryCount: Int = 0
)

/**
 * Conflict resolution strategy
 */
enum class ConflictResolution {
    LOCAL_WINS,     // Keep local changes
    REMOTE_WINS,    // Accept remote changes
    NEWEST_WINS,    // Keep the newest version based on timestamp
    MANUAL          // Require manual resolution
}

/**
 * Sync conflict
 */
data class SyncConflict(
    val entityType: String,
    val entityId: String,
    val localData: Map<String, Any?>,
    val remoteData: Map<String, Any?>,
    val localTimestamp: Long,
    val remoteTimestamp: Long,
    val resolution: ConflictResolution = ConflictResolution.NEWEST_WINS
)

/**
 * Sync result
 */
sealed class SyncResult {
    data class Success(
        val syncedCount: Int,
        val conflicts: List<SyncConflict> = emptyList()
    ) : SyncResult()

    data class Failure(
        val error: String,
        val failedItems: List<SyncItem> = emptyList()
    ) : SyncResult()

    data class Partial(
        val syncedCount: Int,
        val failedCount: Int,
        val conflicts: List<SyncConflict> = emptyList()
    ) : SyncResult()
}

/**
 * Sync configuration
 */
data class SyncConfig(
    val autoSync: Boolean = true,
    val syncOnWiFiOnly: Boolean = true,
    val syncInterval: Long = 15 * 60 * 1000L, // 15 minutes
    val conflictResolution: ConflictResolution = ConflictResolution.NEWEST_WINS,
    val maxRetries: Int = 3
)

/**
 * Network type for sync
 */
enum class NetworkType {
    WIFI,
    MOBILE,
    NONE
}
