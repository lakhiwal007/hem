package org.nha.project.core.network

import io.ktor.client.HttpClient
import io.ktor.client.request.head
import kotlinx.coroutines.withTimeoutOrNull

private const val CONNECTIVITY_PROBE_URL = "https://apisbeta.nha.gov.in/pmjay/stgbis/configbis/bis/token/data"
private const val CONNECTIVITY_CHECK_TIMEOUT_MILLIS = 6_000L

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
