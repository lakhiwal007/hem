package org.nha.project.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import org.jetbrains.compose.resources.decodeToImageBitmap
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
private fun decodeBase64Image(base64: String): ImageBitmap? =
    try {
        val cleaned = base64.substringAfter("base64,", base64)
        val bytes = Base64.decode(cleaned)
        bytes.decodeToImageBitmap()
    } catch (_: Exception) {
        null
    }

@Composable
fun Base64Image(
    base64: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val bitmap = remember(base64) { decodeBase64Image(base64) }
    if (bitmap != null) {
        Image(bitmap = bitmap, contentDescription = contentDescription, modifier = modifier)
    }
}
