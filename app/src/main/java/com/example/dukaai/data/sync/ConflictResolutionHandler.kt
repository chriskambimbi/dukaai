package com.example.dukaai.data.sync

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Conflict Resolution Handler
 * Manages data conflicts between local and remote data
 */
@Singleton
class ConflictResolutionHandler @Inject constructor() {

    companion object {
        private const val TAG = "ConflictResolution"
    }

    // Queue of conflicts awaiting resolution
    private val _conflictQueue = MutableStateFlow<List<SyncConflict>>(emptyList())
    val conflictQueue: Flow<List<SyncConflict>> = _conflictQueue.asStateFlow()

    // Count of pending conflicts
    private val _pendingConflictsCount = MutableStateFlow(0)
    val pendingConflictsCount: Flow<Int> = _pendingConflictsCount.asStateFlow()

    /**
     * Add conflict to queue
     */
    fun addConflict(conflict: SyncConflict) {
        val currentQueue = _conflictQueue.value.toMutableList()
        currentQueue.add(conflict)
        _conflictQueue.value = currentQueue
        _pendingConflictsCount.value = currentQueue.size
        Log.d(TAG, "Conflict added: ${conflict.entityType}/${conflict.entityId}")
    }

    /**
     * Add multiple conflicts to queue
     */
    fun addConflicts(conflicts: List<SyncConflict>) {
        val currentQueue = _conflictQueue.value.toMutableList()
        currentQueue.addAll(conflicts)
        _conflictQueue.value = currentQueue
        _pendingConflictsCount.value = currentQueue.size
        Log.d(TAG, "Added ${conflicts.size} conflicts")
    }

    /**
     * Remove conflict from queue
     */
    fun removeConflict(conflict: SyncConflict) {
        val currentQueue = _conflictQueue.value.toMutableList()
        currentQueue.remove(conflict)
        _conflictQueue.value = currentQueue
        _pendingConflictsCount.value = currentQueue.size
    }

    /**
     * Clear all conflicts
     */
    fun clearConflicts() {
        _conflictQueue.value = emptyList()
        _pendingConflictsCount.value = 0
        Log.d(TAG, "All conflicts cleared")
    }

    /**
     * Resolve conflict based on strategy
     */
    fun resolveConflict(
        conflict: SyncConflict,
        strategy: ConflictResolution? = null
    ): Map<String, Any?> {
        val resolutionStrategy = strategy ?: conflict.resolution

        val resolvedData = when (resolutionStrategy) {
            ConflictResolution.LOCAL_WINS -> {
                Log.d(TAG, "Resolving with LOCAL_WINS: ${conflict.entityType}/${conflict.entityId}")
                conflict.localData
            }

            ConflictResolution.REMOTE_WINS -> {
                Log.d(TAG, "Resolving with REMOTE_WINS: ${conflict.entityType}/${conflict.entityId}")
                conflict.remoteData
            }

            ConflictResolution.NEWEST_WINS -> {
                Log.d(TAG, "Resolving with NEWEST_WINS: ${conflict.entityType}/${conflict.entityId}")
                if (conflict.localTimestamp > conflict.remoteTimestamp) {
                    conflict.localData
                } else {
                    conflict.remoteData
                }
            }

            ConflictResolution.MANUAL -> {
                // For manual resolution, return local data but keep conflict in queue
                Log.d(TAG, "Manual resolution required: ${conflict.entityType}/${conflict.entityId}")
                conflict.localData
            }
        }

        // Remove from queue if not manual resolution
        if (resolutionStrategy != ConflictResolution.MANUAL) {
            removeConflict(conflict)
        }

        return resolvedData
    }

    /**
     * Resolve all conflicts automatically based on default strategy
     */
    fun resolveAllConflicts(strategy: ConflictResolution): List<ResolvedConflict> {
        val conflicts = _conflictQueue.value
        val resolvedConflicts = mutableListOf<ResolvedConflict>()

        conflicts.forEach { conflict ->
            val resolvedData = resolveConflict(conflict, strategy)
            resolvedConflicts.add(
                ResolvedConflict(
                    conflict = conflict,
                    resolvedData = resolvedData,
                    strategy = strategy
                )
            )
        }

        Log.d(TAG, "Resolved ${resolvedConflicts.size} conflicts with $strategy")
        return resolvedConflicts
    }

    /**
     * Get conflicts by entity type
     */
    fun getConflictsByType(entityType: String): List<SyncConflict> {
        return _conflictQueue.value.filter { it.entityType == entityType }
    }

    /**
     * Check if entity has conflicts
     */
    fun hasConflicts(entityType: String, entityId: String): Boolean {
        return _conflictQueue.value.any {
            it.entityType == entityType && it.entityId == entityId
        }
    }

    /**
     * Merge data fields with preference
     */
    fun mergeData(
        localData: Map<String, Any?>,
        remoteData: Map<String, Any?>,
        preferLocal: Boolean = true
    ): Map<String, Any?> {
        val merged = mutableMapOf<String, Any?>()

        // Start with base data
        if (preferLocal) {
            merged.putAll(remoteData)
            merged.putAll(localData)
        } else {
            merged.putAll(localData)
            merged.putAll(remoteData)
        }

        return merged
    }

    /**
     * Compare data and identify conflicting fields
     */
    fun findConflictingFields(
        localData: Map<String, Any?>,
        remoteData: Map<String, Any?>
    ): List<String> {
        val conflictingFields = mutableListOf<String>()

        val allKeys = (localData.keys + remoteData.keys).distinct()

        allKeys.forEach { key ->
            val localValue = localData[key]
            val remoteValue = remoteData[key]

            if (localValue != remoteValue) {
                conflictingFields.add(key)
            }
        }

        return conflictingFields
    }

    /**
     * Create field-level resolution strategy
     */
    fun resolveByField(
        conflict: SyncConflict,
        fieldResolutions: Map<String, ConflictResolution>
    ): Map<String, Any?> {
        val resolvedData = mutableMapOf<String, Any?>()

        val allKeys = (conflict.localData.keys + conflict.remoteData.keys).distinct()

        allKeys.forEach { key ->
            val resolution = fieldResolutions[key] ?: conflict.resolution
            resolvedData[key] = when (resolution) {
                ConflictResolution.LOCAL_WINS -> conflict.localData[key]
                ConflictResolution.REMOTE_WINS -> conflict.remoteData[key]
                ConflictResolution.NEWEST_WINS -> {
                    if (conflict.localTimestamp > conflict.remoteTimestamp) {
                        conflict.localData[key]
                    } else {
                        conflict.remoteData[key]
                    }
                }
                ConflictResolution.MANUAL -> conflict.localData[key]
            }
        }

        return resolvedData
    }
}

/**
 * Resolved conflict with strategy applied
 */
data class ResolvedConflict(
    val conflict: SyncConflict,
    val resolvedData: Map<String, Any?>,
    val strategy: ConflictResolution
)
