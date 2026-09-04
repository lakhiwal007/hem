package org.nha.project.feature.verification.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nha.project.core.ui.toast.ToastController
import org.nha.project.feature.hospital.domain.Service
import org.nha.project.feature.verification.data.PhysicalVerifierMockRepository
import org.nha.project.feature.verification.data.VerificationAction

class PhysicalVerifyImagesViewModel(
    private val repository: PhysicalVerifierMockRepository,
    private val toastController: ToastController,
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
            val images = repository.getUploadedImages(service)
            _uiState.update { it.copy(isLoading = false, images = images) }
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
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            repository.submitVerification(service, state.action, state.comments)
            _uiState.update { it.copy(isSubmitting = false, submitted = true) }
            toastController.success("Verification submitted for ${service.name}")
        }
    }
}
