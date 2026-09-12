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
import org.nha.project.feature.verification.data.UploadedImage
import org.nha.project.feature.verification.data.VerificationAction
import org.nha.project.feature.verification.data.VerifierApi

private const val VERIFICATION_STATUS_APPROVED = "APPROVED"
private const val VERIFICATION_STATUS_REJECTED = "REJECTED"
private const val VERIFICATION_STATUS_PENDING = "PENDING"

private fun isDecided(verificationStatus: String?): Boolean =
    !verificationStatus.isNullOrBlank() && !verificationStatus.equals(VERIFICATION_STATUS_PENDING, ignoreCase = true)

private fun actionFor(verificationStatus: String?): VerificationAction? =
    when {
        verificationStatus.equals(VERIFICATION_STATUS_APPROVED, ignoreCase = true) -> VerificationAction.RECOMMENDED
        verificationStatus.equals(VERIFICATION_STATUS_REJECTED, ignoreCase = true) -> VerificationAction.NOT_RECOMMENDED
        else -> null
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
    private val decidedAction = actionFor(service.verificationStatus)
    private val isReadOnly = isDecided(service.verificationStatus)

    private val _uiState =
        MutableStateFlow(
            PhysicalVerifyImagesUiState(
                serviceName = service.name,
                isReadOnly = isReadOnly,
                decidedAction = decidedAction,
                decidedComments = service.verifierComments.takeIf { isReadOnly },
            ),
        )
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
                    val images =
                        result.data.mapIndexed { index, image ->
                            ImageVerification(
                                image =
                                    UploadedImage(
                                        label = image.fileName ?: "Image ${index + 1}",
                                        base64 = image.base64Image,
                                    ),
                                action = decidedAction,
                            )
                        }
                    val submissionId = result.data.firstNotNullOfOrNull { it.submissionId }
                    _uiState.update { it.copy(isLoading = false, images = images, submissionId = submissionId) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    toastController.error("Could not load uploaded images. Please try again.")
                }
            }
        }
    }

    fun updateImageAction(
        index: Int,
        action: VerificationAction,
    ) {
        if (_uiState.value.isReadOnly) return
        _uiState.update { state ->
            state.copy(
                images =
                    state.images.mapIndexed { i, item ->
                        if (i == index) item.copy(action = action) else item
                    },
            )
        }
    }

    fun updateImageComment(
        index: Int,
        comment: String,
    ) {
        if (_uiState.value.isReadOnly) return
        _uiState.update { state ->
            state.copy(
                images =
                    state.images.mapIndexed { i, item ->
                        if (i == index) item.copy(comment = comment) else item
                    },
            )
        }
    }

    fun submit() {
        val state = _uiState.value
        if (!state.canSubmit) return
        val submissionId = state.submissionId ?: return
        val overallStatus =
            if (state.images.any { it.action == VerificationAction.NOT_RECOMMENDED }) {
                VERIFICATION_STATUS_REJECTED
            } else {
                VERIFICATION_STATUS_APPROVED
            }
        val combinedComments =
            state.images
                .mapIndexedNotNull { index, item ->
                    item.comment.takeIf { it.isNotBlank() }?.let { "Image ${index + 1}: $it" }
                }.joinToString("\n")
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val session = sessionStorage.session.first()
            val result =
                verifierApi.verifierAction(
                    submissionId = submissionId,
                    hospId = hospital.hospitalId,
                    specialityId = speciality.id,
                    serviceId = service.id,
                    verificationStatus = overallStatus,
                    comments = combinedComments,
                    verifiedBy = session?.userId.orEmpty(),
                )
            when (result) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, submitted = true) }
                    toastController.success("Verification submitted for ${service.name}")
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isSubmitting = false) }
                    toastController.error("Could not submit verification. Please try again.")
                }
            }
        }
    }
}
