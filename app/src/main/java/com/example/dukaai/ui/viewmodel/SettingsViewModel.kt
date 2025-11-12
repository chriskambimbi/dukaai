package com.example.dukaai.ui.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Settings screen
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    companion object {
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_CURRENCY = stringPreferencesKey("currency")
        val KEY_LOW_STOCK_THRESHOLD = intPreferencesKey("low_stock_threshold")

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
}
