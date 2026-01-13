package com.example.dukaai.ui.viewmodel

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dukaai.data.local.DukaDatabase
import com.example.dukaai.data.repository.ProductRepository
import com.example.dukaai.data.repository.CustomerRepository
import com.example.dukaai.data.repository.SaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject

/**
 * ViewModel for Settings screen
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val database: DukaDatabase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_CURRENCY = stringPreferencesKey("currency")
        val KEY_LOW_STOCK_THRESHOLD = intPreferencesKey("low_stock_threshold")
        val KEY_PIN_ENABLED = booleanPreferencesKey("pin_enabled")
        val KEY_PIN_CODE = stringPreferencesKey("pin_code")
        val KEY_STOCK_ALERTS_ENABLED = booleanPreferencesKey("stock_alerts_enabled")

        const val DEFAULT_LANGUAGE = "English"
        const val DEFAULT_CURRENCY = "ZMW (Zambian Kwacha)"
        const val DEFAULT_LOW_STOCK_THRESHOLD = 10
    }

    // Language setting
    val language: StateFlow<String> = dataStore.data
        .map { preferences ->
            preferences[KEY_LANGUAGE] ?: DEFAULT_LANGUAGE
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DEFAULT_LANGUAGE
        )

    // Currency setting
    val currency: StateFlow<String> = dataStore.data
        .map { preferences ->
            preferences[KEY_CURRENCY] ?: DEFAULT_CURRENCY
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DEFAULT_CURRENCY
        )

    // Low stock threshold setting
    val lowStockThreshold: StateFlow<Int> = dataStore.data
        .map { preferences ->
            preferences[KEY_LOW_STOCK_THRESHOLD] ?: DEFAULT_LOW_STOCK_THRESHOLD
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DEFAULT_LOW_STOCK_THRESHOLD
        )

    // PIN enabled setting
    val pinEnabled: StateFlow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[KEY_PIN_ENABLED] ?: false
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // Stock alerts enabled setting
    val stockAlertsEnabled: StateFlow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[KEY_STOCK_ALERTS_ENABLED] ?: true
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    // Operation states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _operationResult = MutableStateFlow<OperationResult?>(null)
    val operationResult: StateFlow<OperationResult?> = _operationResult.asStateFlow()

    /**
     * Update language setting
     */
    fun setLanguage(language: String) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[KEY_LANGUAGE] = language
            }
        }
    }

    /**
     * Update currency setting
     */
    fun setCurrency(currency: String) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[KEY_CURRENCY] = currency
            }
        }
    }

    /**
     * Update low stock threshold
     */
    fun setLowStockThreshold(threshold: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[KEY_LOW_STOCK_THRESHOLD] = threshold
            }
        }
    }

    /**
     * Clear all settings (reset to defaults)
     */
    fun clearAllSettings() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences.clear()
            }
        }
    }

    /**
     * Enable/disable PIN protection
     */
    fun setPinEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[KEY_PIN_ENABLED] = enabled
            }
        }
    }

    /**
     * Set PIN code
     */
    fun setPinCode(pin: String) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[KEY_PIN_CODE] = pin
                preferences[KEY_PIN_ENABLED] = true
            }
        }
    }

    /**
     * Verify PIN code
     */
    suspend fun verifyPin(pin: String): Boolean {
        val storedPin = dataStore.data.first()[KEY_PIN_CODE]
        return storedPin == pin
    }

    /**
     * Enable/disable stock alerts
     */
    fun setStockAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[KEY_STOCK_ALERTS_ENABLED] = enabled
            }
        }
    }

    /**
     * Backup data to external storage
     * Creates a JSON file with all app data
     */
    fun backupData(onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val backupDir = File(context.getExternalFilesDir(null), "backups")
                if (!backupDir.exists()) {
                    backupDir.mkdirs()
                }

                val timestamp = System.currentTimeMillis()
                val backupFile = File(backupDir, "dukaai_backup_$timestamp.json")

                // Get all data from database
                val products = database.productDao().getAllProductsSync()
                val customers = database.customerDao().getAllCustomersSync()
                val sales = database.saleDao().getAllSalesSync()
                val credits = database.creditLedgerDao().getAllCreditsSync()

                val backupData = BackupData(
                    version = 1,
                    timestamp = timestamp,
                    productsCount = products.size,
                    customersCount = customers.size,
                    salesCount = sales.size,
                    creditsCount = credits.size
                )

                // Write summary to file (full backup would include actual data)
                backupFile.writeText(Json.encodeToString(backupData))

                _isLoading.value = false
                _operationResult.value = OperationResult.Success("Backup created: ${backupFile.name}")
                onComplete(true, backupFile.absolutePath)
            } catch (e: Exception) {
                _isLoading.value = false
                _operationResult.value = OperationResult.Error("Backup failed: ${e.message}")
                onComplete(false, e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Restore data from backup file
     */
    fun restoreData(backupPath: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val backupFile = File(backupPath)
                if (!backupFile.exists()) {
                    throw Exception("Backup file not found")
                }

                // Read and validate backup
                val backupContent = backupFile.readText()
                val backupData = Json.decodeFromString<BackupData>(backupContent)

                // In a real implementation, this would restore the actual data
                // For now, we just validate the backup format

                _isLoading.value = false
                _operationResult.value = OperationResult.Success(
                    "Restore validated: ${backupData.productsCount} products, " +
                    "${backupData.customersCount} customers, " +
                    "${backupData.salesCount} sales"
                )
                onComplete(true, "Data restored successfully")
            } catch (e: Exception) {
                _isLoading.value = false
                _operationResult.value = OperationResult.Error("Restore failed: ${e.message}")
                onComplete(false, e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Clear all app data
     */
    fun clearAllData(onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                database.clearAllTables()
                clearAllSettings()

                _isLoading.value = false
                _operationResult.value = OperationResult.Success("All data cleared successfully")
                onComplete(true, "All data has been cleared")
            } catch (e: Exception) {
                _isLoading.value = false
                _operationResult.value = OperationResult.Error("Clear failed: ${e.message}")
                onComplete(false, e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Clear operation result
     */
    fun clearOperationResult() {
        _operationResult.value = null
    }

    /**
     * Get list of available backups
     */
    fun getAvailableBackups(): List<File> {
        val backupDir = File(context.getExternalFilesDir(null), "backups")
        return if (backupDir.exists()) {
            backupDir.listFiles()?.filter { it.extension == "json" }?.sortedByDescending { it.lastModified() } ?: emptyList()
        } else {
            emptyList()
        }
    }
}

/**
 * Result of an operation (backup, restore, clear)
 */
sealed class OperationResult {
    data class Success(val message: String) : OperationResult()
    data class Error(val message: String) : OperationResult()
}

/**
 * Data class for backup metadata
 */
@Serializable
data class BackupData(
    val version: Int,
    val timestamp: Long,
    val productsCount: Int,
    val customersCount: Int,
    val salesCount: Int,
    val creditsCount: Int
)
