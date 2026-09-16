package org.nha.project.core.location

data class PlaceName(
    val locality: String? = null,
    val district: String? = null,
    val state: String? = null,
)

interface ReverseGeocoder {
    suspend fun reverseGeocode(point: GeoPoint): PlaceName?
}

fun PlaceName?.districtStateLabel(): String? = listOfNotNull(this?.state, "India").joinToString(", ").ifBlank { null }
