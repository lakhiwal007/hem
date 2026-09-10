package org.nha.project.feature.auth.presentation

data class LoginUiState(
    val isLoading: Boolean = false,
    val captcha1Image: String? = null,
    val userIdInput: String = "",
    val captcha1Input: String = "",
    val verifiedUserId: String? = null,
    val authModes: List<String> = emptyList(),
    val selectedAuthMode: String? = null,
    val captcha2Image: String? = null,
    val otpInput: String = "",
    val captcha2Input: String = "",
    val initMessage: String? = null,
    val showAlreadyLoggedInSheet: Boolean = false,
) {
    val isStepTwoVisible: Boolean get() = verifiedUserId != null
}
