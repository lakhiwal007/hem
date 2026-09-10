package org.nha.project.core.capture

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File

private fun Bitmap.scaledDownIfNeeded(maxDimensionPx: Int): Bitmap {
    val largestSide = maxOf(width, height)
    if (largestSide <= maxDimensionPx) return this
    val scale = maxDimensionPx.toFloat() / largestSide
    val newWidth = (width * scale).toInt().coerceAtLeast(1)
    val newHeight = (height * scale).toInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(this, newWidth, newHeight, true)
}

internal fun Bitmap.compressUnder(maxBytes: Int): ByteArray {
    var quality = 85
    var bytes: ByteArray
    do {
        val stream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, quality, stream)
        bytes = stream.toByteArray()
        quality -= 10
    } while (bytes.size > maxBytes && quality >= 30)
    return bytes
}

private fun orientedBase64Jpeg(path: String): String? {
    val bitmap = BitmapFactory.decodeFile(path) ?: return null
    val orientation =
        runCatching {
            ExifInterface(path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
    val rotationDegrees =
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
    val oriented =
        if (rotationDegrees == 0f) {
            bitmap
        } else {
            val matrix = Matrix().apply { postRotate(rotationDegrees) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }
    val resized = oriented.scaledDownIfNeeded(MAX_CAPTURED_IMAGE_DIMENSION_PX)
    val bytes = resized.compressUnder(MAX_CAPTURED_IMAGE_BYTES)
    return Base64.encodeToString(bytes, Base64.NO_WRAP)
}

@Composable
actual fun rememberCameraCapture(onResult: (String?) -> Unit): () -> Unit {
    val context = LocalContext.current
    var pendingFile by remember { mutableStateOf<File?>(null) }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            val file = pendingFile
            pendingFile = null
            onResult(if (success && file != null) orientedBase64Jpeg(file.absolutePath) else null)
            file?.delete()
        }

    return {
        val file = File.createTempFile("capture_", ".jpg", context.cacheDir)
        pendingFile = file
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        launcher.launch(uri)
    }
}
