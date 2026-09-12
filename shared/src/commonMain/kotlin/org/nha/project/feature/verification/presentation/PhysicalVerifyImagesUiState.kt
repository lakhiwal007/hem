package org.nha.project.feature.verification.presentation

import org.nha.project.feature.verification.data.UploadedImage
import org.nha.project.feature.verification.data.VerificationAction

data class ImageVerification(
    val image: UploadedImage,
    val action: VerificationAction? = null,
    val comment: String = "",
)

data class PhysicalVerifyImagesUiState(
    val serviceName: String,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val submissionId: Long? = null,
    val images: List<ImageVerification> = emptyList(),
    val submitted: Boolean = false,
    val isReadOnly: Boolean = false,
    val decidedAction: VerificationAction? = null,
    val decidedComments: String? = null,
) {
    val canSubmit: Boolean
        get() =
            !isReadOnly &&
                submissionId != null &&
                !isSubmitting &&
                images.isNotEmpty() &&
                images.all { it.action != null } &&
                images.any { it.comment.isNotBlank() }
}
