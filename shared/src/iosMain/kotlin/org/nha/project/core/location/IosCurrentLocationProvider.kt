package org.nha.project.core.location

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume

class IosCurrentLocationProvider : CurrentLocationProvider {
    @OptIn(ExperimentalForeignApi::class)
    override suspend fun getCurrentLocation(): LocationResult {
        val manager = CLLocationManager()
        val status = manager.authorizationStatus
        val authorized =
            status == kCLAuthorizationStatusAuthorizedAlways || status == kCLAuthorizationStatusAuthorizedWhenInUse
        if (!authorized) return LocationResult.PermissionDenied
        if (!CLLocationManager.locationServicesEnabled()) return LocationResult.ProviderDisabled

        return suspendCancellableCoroutine { continuation ->
            val delegate =
                object : NSObject(), CLLocationManagerDelegateProtocol {
                    override fun locationManager(
                        manager: CLLocationManager,
                        didUpdateLocations: List<*>,
                    ) {
                        val location = didUpdateLocations.lastOrNull() as? CLLocation
                        val result =
                            location?.coordinate?.useContents {
                                LocationResult.Success(GeoPoint(latitude, longitude))
                            } ?: LocationResult.Failed(reason = null)
                        if (continuation.isActive) continuation.resume(result)
                    }

                    override fun locationManager(
                        manager: CLLocationManager,
                        didFailWithError: NSError,
                    ) {
                        if (continuation.isActive) {
                            continuation.resume(LocationResult.Failed(didFailWithError.localizedDescription))
                        }
                    }
                }
            manager.delegate = delegate
            continuation.invokeOnCancellation { manager.stopUpdatingLocation() }
            manager.requestLocation()
        }
    }
}
