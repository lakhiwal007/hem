package org.nha.project.feature.hospital.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.nha.project.core.network.ApiResult
import org.nha.project.core.ui.toast.ToastController
import org.nha.project.feature.capture.data.CaptureApi
import org.nha.project.feature.hospital.data.HospitalApi
import org.nha.project.feature.hospital.data.toServices
import org.nha.project.feature.hospital.domain.Hospital
import org.nha.project.feature.hospital.domain.Service
import org.nha.project.feature.hospital.domain.Speciality

class HospitalServicesViewModel(
    private val hospitalApi: HospitalApi,
    private val captureApi: CaptureApi,
    private val toastController: ToastController,
    private val hospital: Hospital,
    private val speciality: Speciality,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HospitalServicesUiState())
    val uiState: StateFlow<HospitalServicesUiState> = _uiState.asStateFlow()

    fun loadServices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = hospitalApi.getServices(speciality.id)) {
                is ApiResult.Success -> {
                    val services = withUploadStatus(result.data.toServices())
                    _uiState.update { it.copy(isLoading = false, services = services) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    toastController.error("Could not load services. Please try again.")
                }
            }
        }
    }

    private suspend fun withUploadStatus(services: List<Service>): List<Service> =
        coroutineScope {
            services
                .map { service ->
                    async {
                        val result =
                            captureApi.getUploadedImages(
                                hospId = hospital.hospitalId,
                                specialityId = speciality.id,
                                serviceId = service.id,
                            )
                        val hasUploadedImages = (result as? ApiResult.Success)?.data?.isNotEmpty() ?: false
                        service.copy(hasUploadedImages = hasUploadedImages)
                    }
                }.awaitAll()
        }
}
