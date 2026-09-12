package org.nha.project.feature.capture.presentation

import org.nha.project.feature.capture.domain.CapturedImage

data class CaptureUiState(
    val serviceName: String,
    val images: List<CapturedImage> = emptyList(),
    val maxImages: Int = 10,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val finalSubmitAllowed: Boolean = true,
    val requiredImageCount: Int? = null,
    val submitted: Boolean = false,
) {
    val canAddMore: Boolean get() = images.size < maxImages
    val canSubmit: Boolean get() = images.isNotEmpty() && !isSubmitting && finalSubmitAllowed
    val remainingRequiredCount: Int?
        get() = requiredImageCount?.let { (it - images.size).coerceAtLeast(0) }
}
