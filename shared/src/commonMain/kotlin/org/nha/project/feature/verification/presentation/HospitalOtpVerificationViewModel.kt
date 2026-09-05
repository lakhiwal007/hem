package org.nha.project.feature.verification.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nha.project.core.network.ApiResult
import org.nha.project.core.ui.toast.ToastController
import org.nha.project.feature.hospital.domain.Hospital
import org.nha.project.feature.verification.data.VerifierApi

class HospitalOtpVerificationViewModel(
    private val verifierApi: VerifierApi,
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
            _uiState.update { it.copy(isSendingOtp = true, transactionId = null) }
            when (val result = verifierApi.generateOtp(hospital.phone)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSendingOtp = false, transactionId = result.data.transactionid) }
                    toastController.info("OTP sent to the hospital admin's registered mobile number")
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isSendingOtp = false) }
                    toastController.error("Could not send OTP. Please try again.")
                }
            }
        }
    }

    fun updateOtp(value: String) {
        _uiState.update { it.copy(otp = value.filter { c -> c.isDigit() }.take(OTP_LENGTH)) }
    }

    fun submitOtp() {
        val state = _uiState.value
        val transactionId = state.transactionId
        if (!state.canSubmit || transactionId == null) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            when (verifierApi.validateOtp(transactionId, state.otp)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, result = OtpVerificationResult.SUCCESS) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isSubmitting = false, result = OtpVerificationResult.FAILURE) }
                }
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
