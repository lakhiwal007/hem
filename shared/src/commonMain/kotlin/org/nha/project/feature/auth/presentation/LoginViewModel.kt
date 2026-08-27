package org.nha.project.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.nha.project.core.network.ApiResult
import org.nha.project.core.secrets.AppSecrets
import org.nha.project.core.security.IdamCrypto
import org.nha.project.core.security.SessionCrypto
import org.nha.project.feature.auth.data.AuthApi
import org.nha.project.feature.auth.data.DecryptedProfile
import org.nha.project.feature.auth.data.SessionStorage
import org.nha.project.feature.auth.domain.UserSession

class LoginViewModel(
    private val authApi: AuthApi,
    private val sessionStorage: SessionStorage,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private var clientToken: String? = null
    private var captcha1TransactionId: String? = null
    private var transactionId2: String? = null
    private var authTransaction: String? = null

    init {
        loadCaptcha1()
    }

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
        loadCaptcha1()
    }

    fun verifyUserId() {
        val state = _uiState.value
        val token = clientToken
        val captchaTransactionId = captcha1TransactionId
        if (token == null || captchaTransactionId == null) return
        if (state.userIdInput.isBlank() || state.captcha1Input.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val encryptedCaptcha = IdamCrypto.encrypt(AppSecrets.IDAM_KEY, state.captcha1Input)
            when (
                val result =
                    authApi.checkCaptcha(
                        token = token,
                        transactionId = captchaTransactionId,
                        loginId = state.userIdInput,
                        encryptedCaptcha = encryptedCaptcha,
                    )
            ) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            verifiedUserId = result.data.userid,
                            authModes = result.data.authmodes,
                        )
                    }
                    val defaultAuthMode =
                        result.data.authmodes.getOrNull(result.data.defaultAuthMode)
                            ?: result.data.authmodes.firstOrNull()
                    if (defaultAuthMode != null) {
                        _uiState.update { it.copy(selectedAuthMode = defaultAuthMode) }
                        startAuthInit(defaultAuthMode)
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Could not verify user ID or captcha.",
                        )
                    }
                    loadCaptcha1()
                }
            }
        }
    }

    fun submitLogin() {
        val state = _uiState.value
        val token = clientToken
        val txId2 = transactionId2
        val authTx = authTransaction
        if (token == null || txId2 == null || authTx == null) return
        if (state.otpInput.isBlank() || state.captcha2Input.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val encryptedCaptcha2 = IdamCrypto.encrypt(AppSecrets.IDAM_KEY, state.captcha2Input)
            val encryptedOtp = IdamCrypto.encrypt(AppSecrets.IDAM_KEY, state.otpInput)
            when (
                val result =
                    authApi.validate(
                        token = token,
                        transactionId = txId2,
                        encryptedCaptcha = encryptedCaptcha2,
                        encryptedPassOtp = encryptedOtp,
                        authTransaction = authTx,
                    )
            ) {
                is ApiResult.Success -> {
                    val authToken = result.data.authtoken
                    val transactionId = result.data.transactionid
                    if (authToken == null || transactionId == null) {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Login failed. Please try again.") }
                        return@launch
                    }
                    fetchProfileAndFinish(token, transactionId, authToken)
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Incorrect OTP or captcha.") }
                }
            }
        }
    }

    private fun loadCaptcha1() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val token = clientToken ?: fetchClientToken()
            if (token == null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Could not connect. Please try again.") }
                return@launch
            }
            when (val result = authApi.generateCaptcha(token)) {
                is ApiResult.Success -> {
                    captcha1TransactionId = result.data.transactionid
                    _uiState.update { it.copy(isLoading = false, captcha1Image = result.data.captcha) }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Could not load captcha. Please try again.",
                        )
                    }
                }
            }
        }
    }

    private suspend fun fetchClientToken(): String? =
        when (val result = authApi.generateToken()) {
            is ApiResult.Success -> result.data.also { clientToken = it }
            is ApiResult.Error -> null
        }

    private fun startAuthInit(authMode: String) {
        val token = clientToken
        val userId = _uiState.value.verifiedUserId
        if (token == null || userId == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = authApi.init(token, userId, authMode)) {
                is ApiResult.Success -> {
                    val encryptedCaptcha2 = result.data.captcha
                    val txId2 = result.data.transactionid
                    val authTx = result.data.authtransaction
                    if (encryptedCaptcha2 == null || txId2 == null || authTx == null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Could not start login. Please try again.",
                            )
                        }
                        return@launch
                    }
                    transactionId2 = txId2
                    authTransaction = authTx
                    val captchaImage = IdamCrypto.decrypt(AppSecrets.IDAM_KEY, encryptedCaptcha2)
                    _uiState.update { it.copy(isLoading = false, captcha2Image = captchaImage) }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Could not start login. Please try again.",
                        )
                    }
                }
            }
        }
    }

    private suspend fun fetchProfileAndFinish(
        token: String,
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

        when (val result = authApi.decrypt(token, encryptedBody)) {
            is ApiResult.Success -> {
                val plainProfile = SessionCrypto.decrypt(AppSecrets.IDAM_KEY2, result.data)
                val profile = json.decodeFromString<DecryptedProfile>(plainProfile)
                saveSessionAndFinish(clientToken.orEmpty(), authToken, transactionId, profile)
            }
            is ApiResult.Error -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Login succeeded but profile could not be loaded.",
                    )
                }
            }
        }
    }

    private suspend fun saveSessionAndFinish(
        clientToken: String,
        authToken: String,
        transactionId: String,
        profile: DecryptedProfile,
    ) {
        val role = profile.entityapprolelist.firstOrNull()
        sessionStorage.save(
            UserSession(
                clientToken = clientToken,
                authToken = authToken,
                transactionId = transactionId,
                userId = profile.userid,
                username = profile.username,
                state = profile.state,
                entityType = role?.entityType.orEmpty(),
                roleName = role?.roleName.orEmpty(),
                entityId = role?.entityId ?: 0L,
                parentEntityId = role?.parentEntityId ?: 0L,
            ),
        )
        _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
    }
}
