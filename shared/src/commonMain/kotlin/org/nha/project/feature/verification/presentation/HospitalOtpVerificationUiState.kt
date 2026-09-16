package org.nha.project.feature.verification.presentation

const val OTP_LENGTH = 6

enum class OtpVerificationResult { SUCCESS, FAILURE }

data class HospitalOtpVerificationUiState(
    val otp: String = "",
    val transactionId: String? = null,
    val isSendingOtp: Boolean = false,
    val isSubmitting: Boolean = false,
    val result: OtpVerificationResult? = null,
    val resendSecondsRemaining: Int = 0,
) {
    val canSubmit: Boolean get() = otp.length == OTP_LENGTH && !isSubmitting && transactionId != null
    val canResend: Boolean get() = resendSecondsRemaining <= 0 && !isSendingOtp
}
