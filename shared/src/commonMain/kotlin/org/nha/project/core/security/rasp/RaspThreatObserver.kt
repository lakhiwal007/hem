package org.nha.project.core.security.rasp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

private const val RASP_POLL_INTERVAL_MILLIS = 10_000L

class RaspThreatObserver(
    private val detector: RaspDetector,
) {
    fun observe(): Flow<List<RaspCheckResult>> =
        flow {
            while (true) {
                emit(withContext(Dispatchers.Default) { detector.runAllChecks().filter { it.detected } })
                delay(RASP_POLL_INTERVAL_MILLIS)
            }
        }
}
