package org.nha.project.feature.capture.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.nha.project.core.location.CurrentLocationProvider
import org.nha.project.core.location.GeoPoint
import org.nha.project.core.location.LocationResult
import org.nha.project.core.network.ApiResult
import org.nha.project.core.ui.toast.ToastController
import org.nha.project.feature.auth.data.SessionStorage
import org.nha.project.feature.capture.data.CaptureApi
import org.nha.project.feature.capture.domain.CapturedImage
import org.nha.project.feature.hospital.domain.Hospital
import org.nha.project.feature.hospital.domain.Service
import org.nha.project.feature.hospital.domain.Speciality

private const val LOCATION_TIMEOUT_MILLIS = 8_000L

class CaptureViewModel(
    private val captureApi: CaptureApi,
    private val sessionStorage: SessionStorage,
    private val locationProvider: CurrentLocationProvider,
    private val toastController: ToastController,
    private val hospital: Hospital,
    private val speciality: Speciality,
    private val service: Service,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CaptureUiState(serviceName = service.name))
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    fun addImage(base64: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val location = fetchLocationOrNull()
            uploadImage(
                slot = _uiState.value.images.size + 1,
                base64 = base64,
                location = location,
                existingIndex = null,
            )
        }
    }

    fun retakeImage(
        index: Int,
        base64: String,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val location = fetchLocationOrNull()
            uploadImage(slot = index + 1, base64 = base64, location = location, existingIndex = index)
        }
    }

    fun removeImage(index: Int) {
        _uiState.update { state ->
            state.copy(images = state.images.filterIndexed { i, _ -> i != index })
        }
    }

    fun submit() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            val result =
                captureApi.finalSubmit(
                    hospId = hospital.hospitalId,
                    specialityId = speciality.id,
                    serviceId = service.id,
                )
            when (result) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, submitted = true) }
                    toastController.success("Images submitted for verification")
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isSubmitting = false) }
                    toastController.error("Could not submit images. Please try again.")
                }
            }
        }
    }

    private suspend fun uploadImage(
        slot: Int,
        base64: String,
        location: GeoPoint?,
        existingIndex: Int?,
    ) {
        val session = sessionStorage.session.first()
        val fileName = fileNameFor(slot - 1)
        val result =
            captureApi.uploadImage(
                hospId = hospital.hospitalId,
                specialityId = speciality.id,
                serviceId = service.id,
                imageSlot = slot,
                fileName = fileName,
                uploadedBy = session?.username.orEmpty(),
                base64 = base64,
            )
        when (result) {
            is ApiResult.Success -> {
                val submission = result.data
                _uiState.update { state ->
                    val image = CapturedImage(base64 = base64, fileName = fileName, location = location)
                    val images =
                        if (existingIndex != null) {
                            state.images.mapIndexed { i, existing -> if (i == existingIndex) image else existing }
                        } else {
                            state.images + image
                        }
                    state.copy(
                        images = images,
                        isLoading = false,
                        finalSubmitAllowed = submission.finalSubmitAllowed ?: state.finalSubmitAllowed,
                    )
                }
            }
            is ApiResult.Error -> {
                _uiState.update { it.copy(isLoading = false) }
                toastController.error("Could not upload image. Please try again.")
            }
        }
    }

    private suspend fun fetchLocationOrNull(): GeoPoint? =
        (
            withTimeoutOrNull(LOCATION_TIMEOUT_MILLIS) {
                locationProvider.getCurrentLocation()
            } as? LocationResult.Success
        )?.location

    private fun fileNameFor(index: Int): String {
        val specialitySegment = sanitizeFileNameSegment(speciality.description)
        val serviceSegment = sanitizeFileNameSegment(service.name)
        return "${specialitySegment}_${serviceSegment}_${index + 1}.jpg"
    }

    private fun sanitizeFileNameSegment(value: String): String =
        value.trim().replace(Regex("[^A-Za-z0-9]+"), "_").trim('_')
}
