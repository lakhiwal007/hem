package org.nha.project.feature.hospital.presentation

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
import org.nha.project.feature.auth.data.AuthApi
import org.nha.project.feature.auth.data.SessionStorage
import org.nha.project.feature.auth.domain.isPhysicalVerifier
import org.nha.project.feature.hospital.data.HospitalApi
import org.nha.project.feature.hospital.data.toDomain
import org.nha.project.feature.verification.data.VerifierApi
import org.nha.project.feature.verification.data.toHospitals

class HospitalListViewModel(
    private val hospitalApi: HospitalApi,
    private val verifierApi: VerifierApi,
    private val authApi: AuthApi,
    private val sessionStorage: SessionStorage,
    private val toastController: ToastController,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HospitalListUiState())
    val uiState: StateFlow<HospitalListUiState> = _uiState.asStateFlow()

    init {
        loadHospitals()
    }

    fun loadHospitals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val isPhysicalVerifier = sessionStorage.session.first()?.isPhysicalVerifier() == true
            val result =
                if (isPhysicalVerifier) {
                    verifierApi.getWorklist().let { result ->
                        when (result) {
                            is ApiResult.Success -> ApiResult.Success(result.data.toHospitals())
                            is ApiResult.Error -> result
                        }
                    }
                } else {
                    hospitalApi.getHospitals().let { result ->
                        when (result) {
                            is ApiResult.Success -> ApiResult.Success(result.data.flatMap { it.toDomain() })
                            is ApiResult.Error -> result
                        }
                    }
                }
            when (result) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, hospitals = result.data) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    toastController.error("Could not load hospitals. Please try again.")
                }
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            val session = sessionStorage.session.first()
            sessionStorage.clear()
            onLoggedOut()
            if (session != null) {
                authApi.storeLoginLogoutDetails(session, "Logout")
            }
        }
    }
}
