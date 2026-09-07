package org.nha.project.feature.capture.domain

import org.nha.project.core.location.GeoPoint

data class CapturedImage(
    val base64: String?,
    val fileName: String,
    val location: GeoPoint?,
)
