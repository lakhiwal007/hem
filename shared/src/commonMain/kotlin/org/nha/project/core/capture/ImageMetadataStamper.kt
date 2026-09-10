package org.nha.project.core.capture

interface ImageMetadataStamper {
    suspend fun stamp(
        base64Jpeg: String,
        watermarkLines: List<String>,
        latitude: Double?,
        longitude: Double?,
        timestampMillis: Long,
    ): String
}
