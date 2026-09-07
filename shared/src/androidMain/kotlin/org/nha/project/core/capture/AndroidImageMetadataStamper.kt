package org.nha.project.core.capture

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Base64
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun Bitmap.drawWatermark(lines: List<String>): Bitmap {
    if (lines.isEmpty()) return this
    val result = copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(result)
    val textSize = result.width * 0.032f
    val padding = result.width * 0.03f
    val lineSpacing = textSize * 0.35f

    val textPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            this.textSize = textSize
            isFakeBoldText = true
        }

    val boxWidth = (lines.maxOfOrNull { textPaint.measureText(it) } ?: 0f) + padding * 2
    val boxHeight = lines.size * (textSize + lineSpacing) + padding
    val boxTop = result.height - boxHeight

    val boxPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            alpha = 140
        }
    canvas.drawRect(
        RectF(0f, boxTop, boxWidth.coerceAtMost(result.width.toFloat()), result.height.toFloat()),
        boxPaint,
    )

    var baseline = boxTop + padding / 2 + textSize
    lines.forEach { line ->
        canvas.drawText(line, padding / 2, baseline, textPaint)
        baseline += textSize + lineSpacing
    }
    return result
}

class AndroidImageMetadataStamper(
    private val context: Context,
) : ImageMetadataStamper {
    override suspend fun stamp(
        base64Jpeg: String,
        watermarkLines: List<String>,
        latitude: Double?,
        longitude: Double?,
        timestampMillis: Long,
    ): String =
        withContext(Dispatchers.Default) {
            try {
                val sourceBytes = Base64.decode(base64Jpeg, Base64.NO_WRAP)
                val bitmap =
                    BitmapFactory.decodeByteArray(sourceBytes, 0, sourceBytes.size)
                        ?: return@withContext base64Jpeg
                val watermarked = bitmap.drawWatermark(watermarkLines)
                val jpegBytes = watermarked.compressUnder(MAX_CAPTURED_IMAGE_BYTES)
                val finalBytes = jpegBytes.withExifMetadata(latitude, longitude, timestampMillis)
                Base64.encodeToString(finalBytes, Base64.NO_WRAP)
            } catch (e: Exception) {
                base64Jpeg
            }
        }

    private fun ByteArray.withExifMetadata(
        latitude: Double?,
        longitude: Double?,
        timestampMillis: Long,
    ): ByteArray {
        val tempFile = File.createTempFile("meta_", ".jpg", context.cacheDir)
        return try {
            tempFile.writeBytes(this)
            val exif = ExifInterface(tempFile.absolutePath)
            if (latitude != null && longitude != null) {
                exif.setLatLong(latitude, longitude)
            }
            val dateString = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US).format(Date(timestampMillis))
            exif.setAttribute(ExifInterface.TAG_DATETIME, dateString)
            exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, dateString)
            exif.saveAttributes()
            tempFile.readBytes()
        } finally {
            tempFile.delete()
        }
    }
}
