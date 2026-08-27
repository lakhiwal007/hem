package org.nha.project.core.location

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

private const val POLL_INTERVAL_MS = 2000L

class LocationAccessObserver(
    private val checker: LocationAccessChecker,
) {
    fun observe(): Flow<LocationAccessStatus> =
        flow {
            while (true) {
                emit(checker.currentStatus())
                delay(POLL_INTERVAL_MS)
            }
        }

    fun openLocationSettings() = checker.openLocationSettings()
}
