package org.nha.project.core.network

import io.ktor.client.HttpClient
import io.ktor.client.request.head
import kotlinx.coroutines.withTimeoutOrNull

private const val CONNECTIVITY_PROBE_URL = "https://apisbeta.nha.gov.in/pmjay/stgbis/configbis/bis/token/data"
private const val CONNECTIVITY_CHECK_TIMEOUT_MILLIS = 6_000L

/**
 * Confirms real internet reachability by round-tripping to the app's own backend, rather than
 * reading OS-level link state - a device can show "connected" to Wi-Fi/cellular while having no
 * actual internet (captive portal, DNS-only, etc.), and an HTTP round trip is the same reliable
 * signal regardless of platform, so this deliberately avoids any platform-specific connectivity
 * APIs. Any HTTP response at all (even an error status, since the client has expectSuccess=false)
 * proves the network path works; only a timeout or a genuine connection failure means offline.
 */
class NetworkConnectivityChecker(
    private val httpClient: HttpClient,
) {
    suspend fun isConnected(): Boolean =
        try {
            withTimeoutOrNull(CONNECTIVITY_CHECK_TIMEOUT_MILLIS) {
                httpClient.head(CONNECTIVITY_PROBE_URL)
                true
            } ?: false
        } catch (e: Exception) {
            false
        }
}
