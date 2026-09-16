package org.nha.project.feature.verification.presentation

import org.nha.project.feature.verification.data.UploadedImage
import org.nha.project.feature.verification.data.VerificationAction

data class ImageVerification(
    val image: UploadedImage,
    val action: VerificationAction? = null,
    val comment: String = "",
    val isReadOnly: Boolean = false,
)

data class PhysicalVerifyImagesUiState(
    val serviceName: String,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val submissionId: Long? = null,
    val images: List<ImageVerification> = emptyList(),
    val submitted: Boolean = false,
) {
    val pendingImages: List<ImageVerification> get() = images.filterNot { it.isReadOnly }

    val allReviewed: Boolean get() = images.isNotEmpty() && pendingImages.isEmpty()

    val canSubmit: Boolean
        get() =
            submissionId != null &&
                !isSubmitting &&
                pendingImages.isNotEmpty() &&
                pendingImages.all { it.action != null && it.comment.isNotBlank() }
}
