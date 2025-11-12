package com.example.dukaai.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Connectivity Manager for monitoring network state
 * Provides WiFi-first connectivity information
 */
@Singleton
class DukaConnectivityManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * Check if device is connected to WiFi
     */
    fun isConnectedToWiFi(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    /**
     * Check if device is connected to mobile data
     */
    fun isConnectedToMobile(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    /**
     * Check if device has any internet connection
     */
    fun isConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Get current network type
     */
    fun getNetworkType(): NetworkType {
        return when {
            isConnectedToWiFi() -> NetworkType.WIFI
            isConnectedToMobile() -> NetworkType.MOBILE
            else -> NetworkType.NONE
        }
    }

    /**
     * Observe network connectivity changes
     * Emits NetworkType whenever connectivity changes
     */
    fun observeNetworkType(): Flow<NetworkType> = callbackFlow {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(getNetworkType())
            }

            override fun onLost(network: Network) {
                trySend(NetworkType.NONE)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                trySend(getNetworkType())
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        // Send initial state
        trySend(getNetworkType())

        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }.distinctUntilChanged()

    /**
     * Check if sync should proceed based on network type and config
     */
    fun shouldSync(syncConfig: SyncConfig): Boolean {
        val networkType = getNetworkType()

        return when {
            networkType == NetworkType.NONE -> false
            syncConfig.syncOnWiFiOnly && networkType != NetworkType.WIFI -> false
            else -> true
        }
    }
}
