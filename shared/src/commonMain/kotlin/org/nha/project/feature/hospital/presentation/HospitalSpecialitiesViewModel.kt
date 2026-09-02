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
import org.nha.project.feature.hospital.data.toSpecialities
import org.nha.project.feature.hospital.domain.Hospital

class HospitalSpecialitiesViewModel(
    private val hospitalApi: HospitalApi,
    private val toastController: ToastController,
    private val hospital: Hospital,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HospitalSpecialitiesUiState())
    val uiState: StateFlow<HospitalSpecialitiesUiState> = _uiState.asStateFlow()

    init {
        loadSpecialities()
    }

    fun loadSpecialities() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = hospitalApi.getSpecialities(hospital.hospitalId)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, specialities = result.data.toSpecialities()) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    toastController.error("Could not load specialities. Please try again.")
                }
            }
        }
    }
}
