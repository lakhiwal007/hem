package org.nha.project.feature.verification.presentation

const val OTP_LENGTH = 6

enum class OtpVerificationResult { SUCCESS, FAILURE }

data class HospitalOtpVerificationUiState(
    val otp: String = "",
    val isSendingOtp: Boolean = false,
    val isSubmitting: Boolean = false,
    val result: OtpVerificationResult? = null,
) {
    val canSubmit: Boolean get() = otp.length == OTP_LENGTH && !isSubmitting
}
