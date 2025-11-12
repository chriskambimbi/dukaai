package com.example.dukaai.data.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Background Sync Worker
 * Handles periodic and one-time sync operations using WorkManager
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncRepository: SyncRepository,
    private val connectivityManager: DukaConnectivityManager
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "SyncWorker"
        const val WORK_NAME = "duka_sync_work"
        const val WORK_TAG = "sync"

        // Input/Output keys
        const val KEY_SYNC_TYPE = "sync_type"
        const val KEY_FORCE_SYNC = "force_sync"
        const val KEY_SYNCED_COUNT = "synced_count"
        const val KEY_FAILED_COUNT = "failed_count"
        const val KEY_CONFLICTS_COUNT = "conflicts_count"

        /**
         * Schedule periodic sync
         */
        fun schedulePeriodicSync(
            context: Context,
            intervalMinutes: Long = 15,
            requiresWiFi: Boolean = true
        ) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(
                    if (requiresWiFi) androidx.work.NetworkType.UNMETERED else androidx.work.NetworkType.CONNECTED
                )
                .setRequiresBatteryNotLow(true)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                intervalMinutes, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .addTag(WORK_TAG)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    syncRequest
                )

            Log.d(TAG, "Scheduled periodic sync every $intervalMinutes minutes (WiFi: $requiresWiFi)")
        }

        /**
         * Schedule one-time sync
         */
        fun scheduleOneTimeSync(
            context: Context,
            forceSync: Boolean = false,
            requiresWiFi: Boolean = true
        ): Operation {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(
                    if (requiresWiFi) androidx.work.NetworkType.UNMETERED else androidx.work.NetworkType.CONNECTED
                )
                .build()

            val inputData = Data.Builder()
                .putBoolean(KEY_FORCE_SYNC, forceSync)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .setInputData(inputData)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .addTag(WORK_TAG)
                .build()

            Log.d(TAG, "Scheduled one-time sync (force: $forceSync, WiFi: $requiresWiFi)")

            return WorkManager.getInstance(context)
                .enqueue(syncRequest)
        }

        /**
         * Cancel all sync work
         */
        fun cancelSync(context: Context) {
            WorkManager.getInstance(context)
                .cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "Cancelled all sync work")
        }

        /**
         * Cancel pending sync work
         */
        fun cancelPendingSync(context: Context) {
            WorkManager.getInstance(context)
                .cancelAllWorkByTag(WORK_TAG)
            Log.d(TAG, "Cancelled pending sync work")
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Sync worker started (attempt ${runAttemptCount + 1})")

        try {
            // Check if we should sync based on connectivity
            val config = syncRepository.getSyncConfig()
            if (!connectivityManager.shouldSync(config)) {
                Log.d(TAG, "Sync skipped - network conditions not met")
                return@withContext Result.retry()
            }

            val forceSync = inputData.getBoolean(KEY_FORCE_SYNC, false)

            // Set syncing state
            setProgress(
                Data.Builder()
                    .putString(KEY_SYNC_TYPE, "in_progress")
                    .build()
            )

            // Perform sync
            val result = syncRepository.performSync(forceUpload = forceSync)

            when (result) {
                is SyncResult.Success -> {
                    Log.d(TAG, "Sync completed successfully: ${result.syncedCount} items synced")

                    val outputData = Data.Builder()
                        .putInt(KEY_SYNCED_COUNT, result.syncedCount)
                        .putInt(KEY_CONFLICTS_COUNT, result.conflicts.size)
                        .build()

                    Result.success(outputData)
                }

                is SyncResult.Failure -> {
                    Log.e(TAG, "Sync failed: ${result.error}")

                    // Retry with exponential backoff
                    if (runAttemptCount < 3) {
                        Log.d(TAG, "Retrying sync (attempt ${runAttemptCount + 1}/3)")
                        Result.retry()
                    } else {
                        Log.e(TAG, "Max retries reached, giving up")
                        Result.failure()
                    }
                }

                is SyncResult.Partial -> {
                    Log.w(TAG, "Partial sync: ${result.syncedCount} synced, ${result.failedCount} failed")

                    val outputData = Data.Builder()
                        .putInt(KEY_SYNCED_COUNT, result.syncedCount)
                        .putInt(KEY_FAILED_COUNT, result.failedCount)
                        .putInt(KEY_CONFLICTS_COUNT, result.conflicts.size)
                        .build()

                    // Consider partial as success
                    Result.success(outputData)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync worker error", e)

            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
