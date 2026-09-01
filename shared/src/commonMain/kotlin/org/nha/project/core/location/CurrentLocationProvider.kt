package org.nha.project.core.location

interface CurrentLocationProvider {
    suspend fun getCurrentLocation(): LocationResult
}
