package org.nha.project.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidCurrentLocationProvider(
    private val context: Context,
) : CurrentLocationProvider {
    override suspend fun getCurrentLocation(): LocationResult {
        val hasFinePermission =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        val hasCoarsePermission =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        if (!hasFinePermission && !hasCoarsePermission) return LocationResult.PermissionDenied

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!LocationManagerCompat.isLocationEnabled(locationManager)) return LocationResult.ProviderDisabled

        val priority = if (hasFinePermission) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val cancellationTokenSource = CancellationTokenSource()

        return try {
            suspendCancellableCoroutine { continuation ->
                continuation.invokeOnCancellation { cancellationTokenSource.cancel() }
                fusedClient
                    .getCurrentLocation(priority, cancellationTokenSource.token)
                    .addOnSuccessListener { location ->
                        if (!continuation.isActive) return@addOnSuccessListener
                        val result =
                            if (location != null) {
                                LocationResult.Success(GeoPoint(location.latitude, location.longitude))
                            } else {
                                LocationResult.Failed(reason = null)
                            }
                        continuation.resume(result)
                    }.addOnFailureListener { exception ->
                        if (continuation.isActive) continuation.resume(LocationResult.Failed(exception.message))
                    }
            }
        } catch (e: SecurityException) {
            LocationResult.PermissionDenied
        }
    }
}
