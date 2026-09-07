package org.nha.project.feature.auth.presentation

data class LoginUiState(
    val isLoading: Boolean = false,
    // Step 1: user id + captcha1
    val captcha1Image: String? = null,
    val userIdInput: String = "",
    val captcha1Input: String = "",
    // Step 2: revealed once check() succeeds
    val verifiedUserId: String? = null,
    val authModes: List<String> = emptyList(),
    val selectedAuthMode: String? = null,
    val captcha2Image: String? = null,
    val otpInput: String = "",
    val captcha2Input: String = "",
    val initMessage: String? = null,
) {
    val isStepTwoVisible: Boolean get() = verifiedUserId != null
}
