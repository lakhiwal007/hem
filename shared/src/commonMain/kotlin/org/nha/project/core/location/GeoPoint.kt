package org.nha.project.core.location

import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Serializable
data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
)

private const val EARTH_RADIUS_METERS = 6_371_000.0

fun GeoPoint.distanceMetersTo(other: GeoPoint): Double {
    val lat1 = latitude * PI / 180
    val lat2 = other.latitude * PI / 180
    val dLat = (other.latitude - latitude) * PI / 180
    val dLon = (other.longitude - longitude) * PI / 180

    val a = sin(dLat / 2) * sin(dLat / 2) + cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return EARTH_RADIUS_METERS * c
}
