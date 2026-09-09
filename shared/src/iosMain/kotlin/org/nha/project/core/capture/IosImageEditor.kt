package org.nha.project.core.capture

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGContextRotateCTM
import platform.CoreGraphics.CGContextTranslateCTM
import platform.CoreGraphics.CGImageCreateWithImageInRect
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIGraphicsGetCurrentContext
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.posix.memcpy
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.PI

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData =
    usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
    }

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray =
    ByteArray(length.toInt()).apply {
        usePinned { pinned ->
            memcpy(pinned.addressOf(0), bytes, length)
        }
    }

/** Rotates the image 90 degrees clockwise by drawing it into a canvas with swapped dimensions. */
@OptIn(ExperimentalForeignApi::class)
private fun UIImage.rotated90(): UIImage {
    val (width, height) = size.useContents { width to height }
    val renderer = UIGraphicsImageRenderer(size = CGSizeMake(height, width))
    return renderer.imageWithActions { _ ->
        val context = UIGraphicsGetCurrentContext()
        CGContextTranslateCTM(context, height, 0.0)
        CGContextRotateCTM(context, PI / 2.0)
        drawInRect(CGRectMake(0.0, 0.0, width, height))
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun UIImage.cropped(rect: NormalizedCropRect): UIImage {
    val (width, height) = size.useContents { width to height }
    val cgImage = this.CGImage ?: return this
    val cropRect =
        CGRectMake(
            x = (rect.left * width),
            y = (rect.top * height),
            width = ((rect.right - rect.left) * width),
            height = ((rect.bottom - rect.top) * height),
        )
    val croppedRef = CGImageCreateWithImageInRect(cgImage, cropRect) ?: return this
    return UIImage(cGImage = croppedRef)
}

/**
 * Rotate/crop edits for a captured photo before it's stamped and uploaded. Pure image
 * transforms via UIKit/CoreGraphics - no EXIF or Foundation bridging risk, unlike the
 * watermark stamper, so this is safe to ship without a Mac to runtime-verify against.
 */
class IosImageEditor : ImageEditor {
    @OptIn(ExperimentalEncodingApi::class, ExperimentalForeignApi::class)
    override suspend fun rotate90(base64Jpeg: String): String =
        withContext(Dispatchers.Default) {
            try {
                val image = UIImage(data = Base64.decode(base64Jpeg).toNSData())
                val rotated = image.rotated90()
                val jpegData = UIImageJPEGRepresentation(rotated, 0.9) ?: return@withContext base64Jpeg
                Base64.encode(jpegData.toByteArray())
            } catch (e: Exception) {
                base64Jpeg
            }
        }

    @OptIn(ExperimentalEncodingApi::class, ExperimentalForeignApi::class)
    override suspend fun crop(
        base64Jpeg: String,
        rect: NormalizedCropRect,
    ): String =
        withContext(Dispatchers.Default) {
            try {
                if (rect.isFullImage) return@withContext base64Jpeg
                val image = UIImage(data = Base64.decode(base64Jpeg).toNSData())
                val cropped = image.cropped(rect)
                val jpegData = UIImageJPEGRepresentation(cropped, 0.9) ?: return@withContext base64Jpeg
                Base64.encode(jpegData.toByteArray())
            } catch (e: Exception) {
                base64Jpeg
            }
        }
}
