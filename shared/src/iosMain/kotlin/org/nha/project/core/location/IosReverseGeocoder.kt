package org.nha.project.core.location

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLGeocoder
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLPlacemark
import kotlin.coroutines.resume

class IosReverseGeocoder : ReverseGeocoder {
    @OptIn(ExperimentalForeignApi::class)
    override suspend fun reverseGeocode(point: GeoPoint): PlaceName? =
        suspendCancellableCoroutine { continuation ->
            val geocoder = CLGeocoder()
            val location = CLLocation(latitude = point.latitude, longitude = point.longitude)
            geocoder.reverseGeocodeLocation(location) { placemarks, _ ->
                val placemark = placemarks?.firstOrNull() as? CLPlacemark
                val result =
                    placemark?.let {
                        PlaceName(
                            locality = it.locality,
                            district = it.subAdministrativeArea,
                            state = it.administrativeArea,
                        )
                    }
                if (continuation.isActive) continuation.resume(result)
            }
            continuation.invokeOnCancellation { geocoder.cancelGeocode() }
        }
}
