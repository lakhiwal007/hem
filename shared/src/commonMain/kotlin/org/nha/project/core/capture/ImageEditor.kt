package org.nha.project.core.capture

/** A crop rectangle expressed as fractions (0f..1f) of the source image's width/height. */
data class NormalizedCropRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val isFullImage: Boolean
        get() = left <= 0.001f && top <= 0.001f && right >= 0.999f && bottom >= 0.999f
}

/** Applies rotate/crop edits to a captured photo before it's stamped and uploaded. */
interface ImageEditor {
    suspend fun rotate90(base64Jpeg: String): String

    suspend fun crop(
        base64Jpeg: String,
        rect: NormalizedCropRect,
    ): String
}
