package org.nha.project.core.location

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LocationVerificationTarget(
    val hospitalName: String,
    val hfrLocation: GeoPoint,
)

class HospitalLocationTracker {
    private val _activeTarget = MutableStateFlow<LocationVerificationTarget?>(null)
    val activeTarget: StateFlow<LocationVerificationTarget?> = _activeTarget.asStateFlow()

    fun setTarget(target: LocationVerificationTarget) {
        _activeTarget.value = target
    }

    fun clear() {
        _activeTarget.value = null
    }
}
