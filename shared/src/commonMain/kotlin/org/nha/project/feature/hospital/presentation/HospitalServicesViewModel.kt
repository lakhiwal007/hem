package org.nha.project.feature.hospital.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nha.project.core.network.ApiResult
import org.nha.project.core.ui.toast.ToastController
import org.nha.project.feature.hospital.data.HospitalApi
import org.nha.project.feature.hospital.data.toServices
import org.nha.project.feature.hospital.domain.Speciality

class HospitalServicesViewModel(
    private val hospitalApi: HospitalApi,
    private val toastController: ToastController,
    private val speciality: Speciality,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HospitalServicesUiState())
    val uiState: StateFlow<HospitalServicesUiState> = _uiState.asStateFlow()

    init {
        loadServices()
    }

    fun loadServices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = hospitalApi.getServices(speciality.id)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, services = result.data.toServices()) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    toastController.error("Could not load services. Please try again.")
                }
            }
        }
    }
}
