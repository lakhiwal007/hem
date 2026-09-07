package org.nha.project.core.capture

/**
 * Burns a visible watermark of [watermarkLines] onto the photo and writes [latitude]/[longitude]
 * plus [timestampMillis] into the JPEG's EXIF GPS/DateTimeOriginal tags, so the metadata survives
 * both a quick visual check and any tooling that reads EXIF - even if the backend strips one of
 * the two forms.
 */
interface ImageMetadataStamper {
    suspend fun stamp(
        base64Jpeg: String,
        watermarkLines: List<String>,
        latitude: Double?,
        longitude: Double?,
        timestampMillis: Long,
    ): String
}
