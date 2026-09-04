package org.nha.project.feature.verification.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nha.project.core.ui.toast.ToastController
import org.nha.project.feature.hospital.domain.Hospital
import org.nha.project.feature.verification.data.PhysicalVerifierMockRepository

class HospitalOtpVerificationViewModel(
    private val repository: PhysicalVerifierMockRepository,
    private val toastController: ToastController,
    private val hospital: Hospital,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HospitalOtpVerificationUiState())
    val uiState: StateFlow<HospitalOtpVerificationUiState> = _uiState.asStateFlow()

    init {
        sendOtp()
    }

    fun sendOtp() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSendingOtp = true) }
            repository.sendOtp(hospital)
            _uiState.update { it.copy(isSendingOtp = false) }
            toastController.info("OTP sent to the hospital admin's registered mobile number")
        }
    }

    fun updateOtp(value: String) {
        _uiState.update { it.copy(otp = value.filter { c -> c.isDigit() }.take(OTP_LENGTH)) }
    }

    fun submitOtp() {
        val state = _uiState.value
        if (!state.canSubmit) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val isValid = repository.verifyOtp(state.otp)
            _uiState.update {
                it.copy(
                    isSubmitting = false,
                    result = if (isValid) OtpVerificationResult.SUCCESS else OtpVerificationResult.FAILURE,
                )
            }
        }
    }

    fun dismissResult() {
        _uiState.update { state ->
            val clearOtp = state.result == OtpVerificationResult.FAILURE
            state.copy(result = null, otp = if (clearOtp) "" else state.otp)
        }
    }
}
