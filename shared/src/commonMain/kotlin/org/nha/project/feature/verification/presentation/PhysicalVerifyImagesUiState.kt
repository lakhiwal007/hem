package org.nha.project.feature.verification.presentation

import org.nha.project.feature.verification.data.UploadedImage
import org.nha.project.feature.verification.data.VerificationAction

data class PhysicalVerifyImagesUiState(
    val serviceName: String,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val submissionId: Long? = null,
    val images: List<UploadedImage> = emptyList(),
    val comments: String = "",
    val action: VerificationAction? = null,
    val submitted: Boolean = false,
    val isReadOnly: Boolean = false,
) {
    val canSubmit: Boolean
        get() = !isReadOnly && submissionId != null && !isSubmitting && comments.isNotBlank() && action != null
}
