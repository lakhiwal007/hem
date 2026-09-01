package org.nha.project.feature.capture.presentation

import org.nha.project.feature.capture.domain.CapturedImage

data class CaptureUiState(
    val serviceName: String,
    val images: List<CapturedImage> = emptyList(),
    val maxImages: Int = 3,
    val isLoading: Boolean = false,
) {
    val canAddMore: Boolean get() = images.size < maxImages
    val canSubmit: Boolean get() = images.isNotEmpty()
}
