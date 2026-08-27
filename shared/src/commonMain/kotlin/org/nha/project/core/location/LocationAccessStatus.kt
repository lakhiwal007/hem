package org.nha.project.core.location

data class LocationAccessStatus(
    val permissionGranted: Boolean,
    val servicesEnabled: Boolean,
) {
    val isAccessible: Boolean get() = permissionGranted && servicesEnabled
}

interface LocationAccessChecker {
    fun currentStatus(): LocationAccessStatus

    fun openLocationSettings()
}
