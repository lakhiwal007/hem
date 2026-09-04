package org.nha.project.feature.hospital.presentation

import org.nha.project.core.location.GeoPoint
import org.nha.project.core.location.MAX_ALLOWED_DISTANCE_METERS

data class HospitalLocationVerificationUiState(
    val isLoading: Boolean = true,
    val currentLocation: GeoPoint? = null,
    val distanceMeters: Double? = null,
) {
    val isWithinRange: Boolean get() = distanceMeters != null && distanceMeters <= MAX_ALLOWED_DISTANCE_METERS
}
