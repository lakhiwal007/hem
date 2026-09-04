package org.nha.project.feature.verification.presentation

import org.nha.project.feature.verification.data.UploadedImage
import org.nha.project.feature.verification.data.VerificationAction

data class PhysicalVerifyImagesUiState(
    val serviceName: String,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val images: List<UploadedImage> = emptyList(),
    val comments: String = "",
    val action: VerificationAction = VerificationAction.RECOMMENDED,
    val submitted: Boolean = false,
)
