package org.nha.project.core.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class AndroidReverseGeocoder(
    private val context: Context,
) : ReverseGeocoder {
    override suspend fun reverseGeocode(point: GeoPoint): PlaceName? {
        if (!Geocoder.isPresent()) return null
        val geocoder = Geocoder(context)
        val address =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getAddressAsync(geocoder, point)
            } else {
                getAddressLegacy(geocoder, point)
            }
        return address?.let {
            PlaceName(
                locality = it.locality,
                district = it.subAdminArea,
                state = it.adminArea,
            )
        }
    }

    private suspend fun getAddressAsync(
        geocoder: Geocoder,
        point: GeoPoint,
    ): Address? =
        suspendCancellableCoroutine { continuation ->
            geocoder.getFromLocation(point.latitude, point.longitude, 1) { addresses ->
                if (continuation.isActive) continuation.resume(addresses.firstOrNull())
            }
        }

    private suspend fun getAddressLegacy(
        geocoder: Geocoder,
        point: GeoPoint,
    ): Address? =
        withContext(Dispatchers.IO) {
            @Suppress("DEPRECATION")
            runCatching { geocoder.getFromLocation(point.latitude, point.longitude, 1)?.firstOrNull() }.getOrNull()
        }
}
