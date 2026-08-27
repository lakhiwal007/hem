package org.nha.project.core.location

import androidx.compose.runtime.Composable

@Composable
expect fun rememberRequestLocationPermission(onResult: (Boolean) -> Unit): () -> Unit
