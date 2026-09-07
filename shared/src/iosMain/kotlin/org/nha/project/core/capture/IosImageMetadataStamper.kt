package org.nha.project.core.capture

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGContextFillRect
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.create
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIGraphicsGetCurrentContext
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.drawAtPoint
import platform.UIKit.sizeWithAttributes
import platform.posix.memcpy
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

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

/**
 * Burns [lines] as a semi-transparent bottom-left watermark onto the image. This intentionally
 * skips writing the same data into EXIF (unlike the Android stamper) - correctly bridging
 * Foundation/UIKit objects into the raw CoreFoundation types ImageIO's EXIF-writing APIs expect
 * needs manual retain-counted bridging that cannot be verified at runtime from this Windows
 * dev machine (no Mac/simulator available), so it was left out rather than risk a native crash.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun UIImage.drawWatermark(lines: List<String>): UIImage {
    if (lines.isEmpty()) return this
    val nsLines = lines.map { NSString.create(string = it) }
    val (width, height) = size.useContents { width to height }
    val renderer = UIGraphicsImageRenderer(size = CGSizeMake(width, height))
    return renderer.imageWithActions { _ ->
        drawInRect(CGRectMake(0.0, 0.0, width, height))

        val fontSize = width * 0.032
        val padding = width * 0.03
        val lineHeight = fontSize * 1.35
        val attributes =
            mapOf<Any?, Any?>(
                NSFontAttributeName to UIFont.boldSystemFontOfSize(fontSize),
                NSForegroundColorAttributeName to UIColor.whiteColor,
            )
        val maxLineWidth =
            nsLines.maxOf { line -> line.sizeWithAttributes(attributes).useContents { this.width } }
        val boxWidth = minOf(maxLineWidth + padding * 2, width)
        val boxHeight = lines.size * lineHeight + padding

        UIColor.colorWithWhite(0.0, alpha = 0.55).setFill()
        CGContextFillRect(UIGraphicsGetCurrentContext(), CGRectMake(0.0, height - boxHeight, boxWidth, boxHeight))

        var y = height - boxHeight + padding / 2
        nsLines.forEach { line ->
            line.drawAtPoint(CGPointMake(padding / 2, y), withAttributes = attributes)
            y += lineHeight
        }
    }
}

class IosImageMetadataStamper : ImageMetadataStamper {
    @OptIn(ExperimentalEncodingApi::class, ExperimentalForeignApi::class)
    override suspend fun stamp(
        base64Jpeg: String,
        watermarkLines: List<String>,
        latitude: Double?,
        longitude: Double?,
        timestampMillis: Long,
    ): String =
        withContext(Dispatchers.Default) {
            try {
                val sourceBytes = Base64.decode(base64Jpeg)
                val image = UIImage(data = sourceBytes.toNSData())
                val watermarked = image.drawWatermark(watermarkLines)
                val jpegData = UIImageJPEGRepresentation(watermarked, 0.9) ?: return@withContext base64Jpeg
                Base64.encode(jpegData.toByteArray())
            } catch (e: Exception) {
                base64Jpeg
            }
        }
}
