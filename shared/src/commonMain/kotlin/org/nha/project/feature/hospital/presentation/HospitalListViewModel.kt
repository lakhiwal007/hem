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
import org.nha.project.feature.hospital.data.toDomain

class HospitalListViewModel(
    private val hospitalApi: HospitalApi,
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
            when (val result = hospitalApi.getHospitals()) {
                is ApiResult.Success -> {
                    val hospitals = result.data.flatMap { it.toDomain() }
                    _uiState.update { it.copy(isLoading = false, hospitals = hospitals) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    toastController.error("Could not load hospitals. Please try again.")
                }
            }
        }
    }
}
