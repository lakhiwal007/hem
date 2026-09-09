package org.nha.project.core.capture

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private fun String.decodeBitmap(): Bitmap? {
    val bytes = Base64.decode(this, Base64.NO_WRAP)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

private fun ByteArray.toBase64(): String = Base64.encodeToString(this, Base64.NO_WRAP)

class AndroidImageEditor : ImageEditor {
    override suspend fun rotate90(base64Jpeg: String): String =
        withContext(Dispatchers.Default) {
            try {
                val bitmap = base64Jpeg.decodeBitmap() ?: return@withContext base64Jpeg
                val matrix = Matrix().apply { postRotate(90f) }
                val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                rotated.compressUnder(MAX_CAPTURED_IMAGE_BYTES).toBase64()
            } catch (e: Exception) {
                base64Jpeg
            }
        }

    override suspend fun crop(
        base64Jpeg: String,
        rect: NormalizedCropRect,
    ): String =
        withContext(Dispatchers.Default) {
            try {
                if (rect.isFullImage) return@withContext base64Jpeg
                val bitmap = base64Jpeg.decodeBitmap() ?: return@withContext base64Jpeg
                val x = (rect.left * bitmap.width).toInt().coerceIn(0, bitmap.width - 1)
                val y = (rect.top * bitmap.height).toInt().coerceIn(0, bitmap.height - 1)
                val width = ((rect.right - rect.left) * bitmap.width).toInt().coerceIn(1, bitmap.width - x)
                val height = ((rect.bottom - rect.top) * bitmap.height).toInt().coerceIn(1, bitmap.height - y)
                val cropped = Bitmap.createBitmap(bitmap, x, y, width, height)
                cropped.compressUnder(MAX_CAPTURED_IMAGE_BYTES).toBase64()
            } catch (e: Exception) {
                base64Jpeg
            }
        }
}
