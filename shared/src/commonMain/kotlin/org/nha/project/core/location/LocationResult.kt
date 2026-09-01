package org.nha.project.core.location

sealed interface LocationResult {
    data class Success(
        val location: GeoPoint,
    ) : LocationResult

    data object PermissionDenied : LocationResult

    data object ProviderDisabled : LocationResult

    data class Failed(
        val reason: String?,
    ) : LocationResult
}
