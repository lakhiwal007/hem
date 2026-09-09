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
import org.nha.project.feature.auth.data.SessionStorage
import org.nha.project.feature.auth.domain.isPhysicalVerifier
import org.nha.project.feature.hospital.data.HospitalApi
import org.nha.project.feature.hospital.data.toServices
import org.nha.project.feature.hospital.domain.Hospital
import org.nha.project.feature.hospital.domain.Speciality

class HospitalServicesViewModel(
    private val hospitalApi: HospitalApi,
    private val sessionStorage: SessionStorage,
    private val toastController: ToastController,
    private val hospital: Hospital,
    private val speciality: Speciality,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HospitalServicesUiState())
    val uiState: StateFlow<HospitalServicesUiState> = _uiState.asStateFlow()

    fun loadServices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val isPhysicalVerifier = sessionStorage.session.first()?.isPhysicalVerifier() == true
            when (val result = hospitalApi.getServices(hospId = hospital.hospitalId, specialityId = speciality.id)) {
                is ApiResult.Success -> {
                    val services = result.data.toServices(speciality.id, isPhysicalVerifier)
                    _uiState.update { it.copy(isLoading = false, services = services) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    toastController.error("Could not load services. Please try again.")
                }
            }
        }
    }
}
