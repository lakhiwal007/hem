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
                    val submission = result.data
                    val images =
                        submission.images.mapIndexed { index, image ->
                            UploadedImage(label = image.fileName ?: "Image ${index + 1}")
                        }
                    _uiState.update {
                        it.copy(isLoading = false, submissionId = submission.submissionId, images = images)
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    toastController.error("Could not load uploaded images. Please try again.")
                }
            }
        }
    }

    fun updateComments(value: String) {
        _uiState.update { it.copy(comments = value) }
    }

    fun updateAction(value: VerificationAction) {
        _uiState.update { it.copy(action = value) }
    }

    fun submit() {
        val state = _uiState.value
        val submissionId = state.submissionId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val session = sessionStorage.session.first()
            val result =
                verifierApi.verifierAction(
                    submissionId = submissionId,
                    hospId = hospital.hospitalId,
                    specialityId = speciality.id,
                    serviceId = service.id,
                    verificationStatus =
                        if (state.action == VerificationAction.RECOMMENDED) {
                            VERIFICATION_STATUS_APPROVED
                        } else {
                            VERIFICATION_STATUS_REJECTED
                        },
                    comments = state.comments,
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
