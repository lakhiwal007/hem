package org.nha.project.feature.verification.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nha.project.core.network.ApiResult
import org.nha.project.core.ui.toast.ToastController
import org.nha.project.feature.auth.data.SessionStorage
import org.nha.project.feature.capture.data.CaptureApi
import org.nha.project.feature.hospital.domain.Hospital
import org.nha.project.feature.hospital.domain.Service
import org.nha.project.feature.hospital.domain.Speciality
import org.nha.project.feature.verification.data.ReviewDto
import org.nha.project.feature.verification.data.UploadedImage
import org.nha.project.feature.verification.data.VerificationAction
import org.nha.project.feature.verification.data.VerifierApi

private const val VERIFICATION_STATUS_RECOMMEND = "RECOMMEND"
private const val VERIFICATION_STATUS_NOT_RECOMMEND = "NOT_RECOMMEND"

private fun actionFor(status: String?): VerificationAction? =
    when {
        status.equals(VERIFICATION_STATUS_RECOMMEND, ignoreCase = true) -> VerificationAction.RECOMMENDED
        status.equals(VERIFICATION_STATUS_NOT_RECOMMEND, ignoreCase = true) -> VerificationAction.NOT_RECOMMENDED
        else -> null
    }

private fun statusFor(action: VerificationAction): String =
    when (action) {
        VerificationAction.RECOMMENDED -> VERIFICATION_STATUS_RECOMMEND
        VerificationAction.NOT_RECOMMENDED -> VERIFICATION_STATUS_NOT_RECOMMEND
    }

class PhysicalVerifyImagesViewModel(
    private val verifierApi: VerifierApi,
    private val captureApi: CaptureApi,
    private val sessionStorage: SessionStorage,
    private val toastController: ToastController,
    private val hospital: Hospital,
    private val speciality: Speciality,
    private val service: Service,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PhysicalVerifyImagesUiState(serviceName = service.name))
    val uiState: StateFlow<PhysicalVerifyImagesUiState> = _uiState.asStateFlow()

    init {
        loadImages()
    }

    fun loadImages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (
                val result =
                    captureApi.getUploadedImages(
                        hospId = hospital.hospitalId,
                        specialityId = speciality.id,
                        serviceId = service.id,
                    )
            ) {
                is ApiResult.Success -> {
                    val submissionId = result.data.firstNotNullOfOrNull { it.submissionId }
                    val pvReviews = submissionId?.let { loadPvReviews(it) }.orEmpty()
                    val images =
                        result.data.mapIndexed { index, image ->
                            val review = image.imageId?.let { pvReviews[it] }
                            ImageVerification(
                                image =
                                    UploadedImage(
                                        label = image.fileName ?: "Image ${index + 1}",
                                        base64 = image.base64Image,
                                        imageId = image.imageId,
                                    ),
                                action = actionFor(review?.status),
                                comment = review?.comments.orEmpty(),
                                isReadOnly = review != null,
                            )
                        }
                    _uiState.update { it.copy(isLoading = false, images = images, submissionId = submissionId) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    toastController.error("Could not load uploaded images. Please try again.")
                }
            }
        }
    }

    private suspend fun loadPvReviews(submissionId: Long): Map<Long, ReviewDto> {
        val result = verifierApi.getVerificationStatus(submissionId)
        return (result as? ApiResult.Success)
            ?.data
            ?.images
            .orEmpty()
            .mapNotNull { image -> image.imageId?.let { id -> image.pvReview?.let { id to it } } }
            .toMap()
    }

    fun updateImageAction(
        index: Int,
        action: VerificationAction,
    ) {
        _uiState.update { state ->
            state.copy(
                images =
                    state.images.mapIndexed { i, item ->
                        if (i == index && !item.isReadOnly) item.copy(action = action) else item
                    },
            )
        }
    }

    fun updateImageComment(
        index: Int,
        comment: String,
    ) {
        _uiState.update { state ->
            state.copy(
                images =
                    state.images.mapIndexed { i, item ->
                        if (i == index && !item.isReadOnly) item.copy(comment = comment) else item
                    },
            )
        }
    }

    fun submit() {
        val state = _uiState.value
        if (!state.canSubmit) return
        val submissionId = state.submissionId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val session = sessionStorage.session.first()
            val verifiedBy = session?.userId.orEmpty()
            val results =
                state.pendingImages.mapNotNull { item ->
                    val imageId = item.image.imageId ?: return@mapNotNull null
                    val action = item.action ?: return@mapNotNull null
                    verifierApi.verifierAction(
                        submissionId = submissionId,
                        hospId = hospital.hospitalId,
                        specialityId = speciality.id,
                        serviceId = service.id,
                        imageId = imageId,
                        verificationStatus = statusFor(action),
                        comments = item.comment,
                        verifiedBy = verifiedBy,
                    )
                }
            if (results.isNotEmpty() && results.all { it is ApiResult.Success }) {
                _uiState.update { it.copy(isSubmitting = false, submitted = true) }
                toastController.success("Verification submitted for ${service.name}")
            } else {
                _uiState.update { it.copy(isSubmitting = false) }
                toastController.error("Could not submit verification. Please try again.")
            }
        }
    }
}
