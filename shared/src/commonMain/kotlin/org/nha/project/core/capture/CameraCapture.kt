package org.nha.project.core.capture

import androidx.compose.runtime.Composable

@Composable
expect fun rememberCameraCapture(onResult: (String?) -> Unit): () -> Unit
