package org.nha.project.core.network

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private const val NETWORK_POLL_INTERVAL_MS = 5_000L

class NetworkConnectivityObserver(
    private val checker: NetworkConnectivityChecker,
) {
    fun observe(): Flow<Boolean> =
        flow {
            while (true) {
                emit(checker.isConnected())
                delay(NETWORK_POLL_INTERVAL_MS)
            }
        }
}
