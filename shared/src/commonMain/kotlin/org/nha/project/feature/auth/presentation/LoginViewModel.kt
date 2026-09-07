package org.nha.project.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.nha.project.core.network.ApiResult
import org.nha.project.core.network.NetworkException
import org.nha.project.core.secrets.AppSecrets
import org.nha.project.core.security.IdamCrypto
import org.nha.project.core.security.SessionCrypto
import org.nha.project.core.ui.toast.ToastController
import org.nha.project.feature.auth.data.AuthApi
import org.nha.project.feature.auth.data.DecryptedProfile
import org.nha.project.feature.auth.data.EntityAppRole
import org.nha.project.feature.auth.data.SessionStorage
import org.nha.project.feature.auth.domain.UserSession

private val EXCLUDED_AUTH_MODES = setOf("Aadhaar_Fingerprint", "Aadhaar_Iris")

private val ALLOWED_HEM_ROLES = setOf("ADMIN", "PHYSICALVERIFIER")

private fun normalizeRoleName(value: String): String = value.uppercase().filter { it.isLetterOrDigit() }

private fun EntityAppRole.isAllowedHemRole(): Boolean {
    val hemRoleNames = appRoleList?.get("HEM") ?: return false
    return hemRoleNames.any { normalizeRoleName(it) in ALLOWED_HEM_ROLES }
}

private fun defaultAuthModeFor(
    code: Int,
    available: List<String>,
): String? {
    val preferred =
        when (code) {
            16 -> "Password"
            56 -> "Mobile_OTP"
            17 -> "Aadhaar_OTP"
            else -> null
        }
    return preferred?.takeIf { it in available } ?: available.firstOrNull()
}

class LoginViewModel(
    private val authApi: AuthApi,
    private val sessionStorage: SessionStorage,
    private val toastController: ToastController,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _loginSuccessEvents = Channel<Unit>(Channel.BUFFERED)
    val loginSuccessEvents: Flow<Unit> = _loginSuccessEvents.receiveAsFlow()

    private var clientToken: String? = null
    private var captcha1TransactionId: String? = null
    private var transactionId2: String? = null
    private var authTransaction: String? = null

    fun onUserIdChange(value: String) {
        _uiState.update { it.copy(userIdInput = value) }
    }

    fun onCaptcha1Change(value: String) {
        _uiState.update { it.copy(captcha1Input = value) }
    }

    fun onOtpChange(value: String) {
        _uiState.update { it.copy(otpInput = value) }
    }

    fun onCaptcha2Change(value: String) {
        _uiState.update { it.copy(captcha2Input = value) }
    }

    fun onAuthModeSelected(mode: String) {
        if (mode == _uiState.value.selectedAuthMode) return
        _uiState.update { it.copy(selectedAuthMode = mode) }
        startAuthInit(mode)
    }

    fun retryCaptcha1() {
        clientToken = null
        captcha1TransactionId = null
        transactionId2 = null
        authTransaction = null
        _uiState.update {
            it.copy(
                userIdInput = "",
                captcha1Input = "",
                verifiedUserId = null,
                authModes = emptyList(),
                selectedAuthMode = null,
                captcha2Image = null,
                otpInput = "",
                captcha2Input = "",
                initMessage = null,
            )
        }
        loadCaptcha1()
    }

    fun resendCaptcha2() {
        val txId2 = transactionId2
        if (txId2 == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = callWithTokenRetry { token -> authApi.resendCaptcha(token, txId2) }) {
                is ApiResult.Success -> {
                    val newTxId2 = result.data.transactionid
                    transactionId2 = newTxId2
                    val captchaImage = IdamCrypto.decrypt(newTxId2, result.data.captcha)
                    _uiState.update {
                        it.copy(isLoading = false, captcha2Image = captchaImage, captcha2Input = "")
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    toastController.error(
                        serverErrorMessage(result.exception, "Could not refresh captcha. Please try again."),
                    )
                }
            }
        }
    }

    fun verifyUserId() {
        val state = _uiState.value
        val captchaTransactionId = captcha1TransactionId
        if (captchaTransactionId == null) return
        if (state.userIdInput.isBlank() || state.captcha1Input.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val encryptedCaptcha = IdamCrypto.encrypt(captchaTransactionId, state.captcha1Input)
            when (
                val result =
                    callWithTokenRetry { token ->
                        authApi.checkCaptcha(
                            token = token,
                            transactionId = captchaTransactionId,
                            loginId = state.userIdInput,
                            encryptedCaptcha = encryptedCaptcha,
                        )
                    }
            ) {
                is ApiResult.Success -> {
                    val filteredAuthModes = result.data.authmodes.filterNot { it in EXCLUDED_AUTH_MODES }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            verifiedUserId = result.data.userid,
                            authModes = filteredAuthModes,
                        )
                    }
                    val defaultAuthMode = defaultAuthModeFor(result.data.defaultAuthMode, filteredAuthModes)
                    if (defaultAuthMode != null) {
                        _uiState.update { it.copy(selectedAuthMode = defaultAuthMode) }
                        startAuthInit(defaultAuthMode)
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    toastController.error(serverErrorMessage(result.exception, "Could not verify user ID or captcha."))
                    loadCaptcha1()
                }
            }
        }
    }

    fun submitLogin() {
        val state = _uiState.value
        val txId2 = transactionId2
        if (txId2 == null) return
        if (state.otpInput.isBlank() || state.captcha2Input.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val encryptedCaptcha2 = IdamCrypto.encrypt(txId2, state.captcha2Input)
            val encryptedOtp = IdamCrypto.encrypt(txId2, state.otpInput)
            when (
                val result =
                    callWithTokenRetry { token ->
                        authApi.validate(
                            token = token,
                            transactionId = txId2,
                            encryptedCaptcha = encryptedCaptcha2,
                            encryptedPassOtp = encryptedOtp,
                            authTransaction = authTransaction ?: "null",
                        )
                    }
            ) {
                is ApiResult.Success -> {
                    val authToken = result.data.authtoken
                    val transactionId = result.data.transactionid
                    if (authToken == null || transactionId == null) {
                        _uiState.update { it.copy(isLoading = false) }
                        toastController.error("Login failed. Please try again.")
                        return@launch
                    }
                    fetchProfileAndFinish(transactionId, authToken)
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    toastController.error(serverErrorMessage(result.exception, "Incorrect OTP or captcha."))
                }
            }
        }
    }

    private fun loadCaptcha1() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val token = clientToken ?: fetchClientToken()
            if (token == null) {
                _uiState.update { it.copy(isLoading = false) }
                toastController.error("Could not connect. Please try again.")
                return@launch
            }
            when (val result = authApi.generateCaptcha(token)) {
                is ApiResult.Success -> {
                    captcha1TransactionId = result.data.transactionid
                    _uiState.update { it.copy(isLoading = false, captcha1Image = result.data.captcha) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    toastController.error("Could not load captcha. Please try again.")
                }
            }
        }
    }

    private suspend fun fetchClientToken(): String? =
        when (val result = authApi.generateToken()) {
            is ApiResult.Success -> result.data.also { clientToken = it }
            is ApiResult.Error -> null
        }

    private suspend fun <T> callWithTokenRetry(block: suspend (String) -> ApiResult<T>): ApiResult<T> {
        val token = clientToken ?: fetchClientToken() ?: return ApiResult.Error(NetworkException.Unknown())
        val result = block(token)
        if (result is ApiResult.Error && isInvalidTokenError(result.exception)) {
            val refreshedToken = fetchClientToken() ?: return result
            return block(refreshedToken)
        }
        return result
    }

    private fun isInvalidTokenError(exception: NetworkException): Boolean =
        (exception as? NetworkException.ApiError)?.message?.contains("token", ignoreCase = true) == true

    private fun startAuthInit(authMode: String) {
        val userId = _uiState.value.verifiedUserId
        if (userId == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = callWithTokenRetry { token -> authApi.init(token, userId, authMode) }) {
                is ApiResult.Success -> {
                    val encryptedCaptcha2 = result.data.captcha
                    val txId2 = result.data.transactionid
                    if (encryptedCaptcha2 == null || txId2 == null) {
                        _uiState.update { it.copy(isLoading = false) }
                        toastController.error("Could not start login. Please try again.")
                        return@launch
                    }
                    transactionId2 = txId2
                    authTransaction = result.data.authtransaction
                    val captchaImage = IdamCrypto.decrypt(txId2, encryptedCaptcha2)
                    _uiState.update {
                        it.copy(isLoading = false, captcha2Image = captchaImage, initMessage = result.data.message)
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    toastController.error("Could not start login. Please try again.")
                }
            }
        }
    }

    private suspend fun fetchProfileAndFinish(
        transactionId: String,
        authToken: String,
    ) {
        val requestBody =
            json.encodeToString(
                mapOf(
                    "role" to "user",
                    "transactionId" to transactionId,
                    "authToken" to authToken,
                ),
            )
        val encryptedBody = SessionCrypto.encrypt(AppSecrets.IDAM_KEY2, requestBody)

        when (val result = callWithTokenRetry { token -> authApi.decrypt(token, encryptedBody) }) {
            is ApiResult.Success -> {
                val plainProfile = SessionCrypto.decrypt(AppSecrets.IDAM_KEY2, result.data)
                val profile = json.decodeFromString<DecryptedProfile>(plainProfile)
                saveSessionAndFinish(clientToken.orEmpty(), authToken, transactionId, profile)
            }
            is ApiResult.Error -> {
                _uiState.update { it.copy(isLoading = false) }
                toastController.error("Login succeeded but profile could not be loaded.")
            }
        }
    }

    private suspend fun saveSessionAndFinish(
        clientToken: String,
        authToken: String,
        transactionId: String,
        profile: DecryptedProfile,
    ) {
        val role = profile.entityapprolelist.find { it.isAllowedHemRole() }
        if (role == null) {
            _uiState.update { it.copy(isLoading = false) }
            toastController.error("Please login with correct credentials.")
            return
        }
        val session =
            UserSession(
                clientToken = clientToken,
                authToken = authToken,
                transactionId = transactionId,
                userId = profile.userid,
                username = profile.username,
                state = profile.state,
                entityType = role.entityType,
                roleName = role.roleName,
                entityId = role.entityId,
                parentEntityId = role.parentEntityId,
                stateCode = role.stateCode?.toString().orEmpty(),
                clusterId = role.clusterId?.firstOrNull()?.toString() ?: "0",
            )
        sessionStorage.save(session)
        authApi.storeLoginLogoutDetails(session, "Login")
        _uiState.update { it.copy(isLoading = false) }
        toastController.success("Welcome back, ${profile.username}!")
        _loginSuccessEvents.send(Unit)
    }

    private fun serverErrorMessage(
        exception: NetworkException,
        fallback: String,
    ): String = (exception as? NetworkException.ApiError)?.message ?: fallback
}
