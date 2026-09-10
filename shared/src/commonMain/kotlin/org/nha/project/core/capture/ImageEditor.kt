package org.nha.project.core.capture

data class NormalizedCropRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val isFullImage: Boolean
        get() = left <= 0.001f && top <= 0.001f && right >= 0.999f && bottom >= 0.999f
}

interface ImageEditor {
    suspend fun rotate90(base64Jpeg: String): String

    suspend fun crop(
        base64Jpeg: String,
        rect: NormalizedCropRect,
    ): String
}
