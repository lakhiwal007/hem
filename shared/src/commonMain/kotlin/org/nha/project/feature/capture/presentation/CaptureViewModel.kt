package org.nha.project.feature.capture.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.nha.project.core.location.CurrentLocationProvider
import org.nha.project.core.location.GeoPoint
import org.nha.project.core.location.LocationResult
import org.nha.project.feature.capture.domain.CapturedImage
import org.nha.project.feature.hospital.domain.Service

private const val LOCATION_TIMEOUT_MILLIS = 8_000L

class CaptureViewModel(
    private val locationProvider: CurrentLocationProvider,
    private val service: Service,
    private val speciality: String,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CaptureUiState(serviceName = service.name))
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    fun addImage(base64: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val location = fetchLocationOrNull()
            _uiState.update { state ->
                val next =
                    if (state.images.size >= state.maxImages) {
                        state
                    } else {
                        val image =
                            CapturedImage(
                                base64 = base64,
                                fileName = fileNameFor(state.images.size),
                                location = location,
                            )
                        state.copy(images = state.images + image)
                    }
                next.copy(isLoading = false)
            }
        }
    }

    fun retakeImage(
        index: Int,
        base64: String,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val location = fetchLocationOrNull()
            _uiState.update { state ->
                state.copy(
                    images =
                        state.images.mapIndexed { i, existing ->
                            if (i == index) existing.copy(base64 = base64, location = location) else existing
                        },
                    isLoading = false,
                )
            }
        }
    }

    fun removeImage(index: Int) {
        _uiState.update { state ->
            state.copy(images = state.images.filterIndexed { i, _ -> i != index })
        }
    }

    private suspend fun fetchLocationOrNull(): GeoPoint? =
        (
            withTimeoutOrNull(LOCATION_TIMEOUT_MILLIS) {
                locationProvider.getCurrentLocation()
            } as? LocationResult.Success
        )?.location

    private fun fileNameFor(index: Int): String {
        val specialitySegment = sanitizeFileNameSegment(speciality)
        val serviceSegment = sanitizeFileNameSegment(service.name)
        return "${specialitySegment}_${serviceSegment}_${index + 1}.jpg"
    }

    private fun sanitizeFileNameSegment(value: String): String =
        value.trim().replace(Regex("[^A-Za-z0-9]+"), "_").trim('_')
}
