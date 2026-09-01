package org.nha.project.core.capture

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.UIKit.UIApplication
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImageOrientation.UIImageOrientationUp
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject
import platform.posix.memcpy
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray =
    ByteArray(length.toInt()).apply {
        usePinned { pinned ->
            memcpy(pinned.addressOf(0), bytes, length)
        }
    }

/**
 * Redraws the image into a fresh bitmap with orientation baked into the pixels themselves,
 * instead of relying on the [imageOrientation] EXIF-style tag - our own decoder (Base64Image, via
 * Skia) does not apply that tag, so a captured photo held sideways would otherwise render sideways.
 * Also downscales it if it's larger than needed for a verification photo, since an unscaled
 * capture from a modern camera can be tens of megapixels.
 */
@OptIn(ExperimentalForeignApi::class)
private fun UIImage.normalizedAndScaled(maxDimensionPx: Double): UIImage {
    val (width, height) = size.useContents { width to height }
    val largestSide = maxOf(width, height)
    val scale = if (largestSide > maxDimensionPx) maxDimensionPx / largestSide else 1.0
    if (imageOrientation == UIImageOrientationUp && scale == 1.0) return this

    val targetSize = CGSizeMake(width * scale, height * scale)
    val renderer = UIGraphicsImageRenderer(size = targetSize)
    return renderer.imageWithActions { _ ->
        drawInRect(
            CGRectMake(
                x = 0.0,
                y = 0.0,
                width = targetSize.useContents { width },
                height = targetSize.useContents { height },
            ),
        )
    }
}

/** Compresses to JPEG, stepping quality down until the result fits under [maxBytes]. */
private fun UIImage.compressUnder(maxBytes: Int): NSData? {
    var quality = 0.85
    var data = UIImageJPEGRepresentation(this, quality)
    while (data != null && data.length > maxBytes.toULong() && quality > 0.3) {
        quality -= 0.1
        data = UIImageJPEGRepresentation(this, quality)
    }
    return data
}

@OptIn(ExperimentalEncodingApi::class, ExperimentalForeignApi::class)
@Composable
actual fun rememberCameraCapture(onResult: (String?) -> Unit): () -> Unit {
    val delegate =
        remember {
            object : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
                override fun imagePickerController(
                    picker: UIImagePickerController,
                    didFinishPickingMediaWithInfo: Map<Any?, *>,
                ) {
                    val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
                    val base64 =
                        image
                            ?.normalizedAndScaled(MAX_CAPTURED_IMAGE_DIMENSION_PX.toDouble())
                            ?.compressUnder(MAX_CAPTURED_IMAGE_BYTES)
                            ?.toByteArray()
                            ?.let { Base64.encode(it) }
                    picker.presentingViewController?.dismissViewControllerAnimated(true, completion = null)
                    onResult(base64)
                }

                override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                    picker.presentingViewController?.dismissViewControllerAnimated(true, completion = null)
                    onResult(null)
                }
            }
        }

    return {
        val picker =
            UIImagePickerController().apply {
                sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
                this.delegate = delegate
            }
        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
            picker,
            animated = true,
            completion = null,
        )
    }
}
