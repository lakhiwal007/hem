package org.nha.project.core.capture

import androidx.compose.runtime.Composable

/**
 * Returns a launcher that opens the device camera and delivers a base64-encoded JPEG
 * of the captured photo via [onResult], or null if the user cancelled.
 */
@Composable
expect fun rememberCameraCapture(onResult: (String?) -> Unit): () -> Unit
