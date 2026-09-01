package org.nha.project.feature.hospital.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.nha.project.core.location.CurrentLocationProvider
import org.nha.project.core.location.LocationResult
import org.nha.project.core.location.distanceMetersTo
import org.nha.project.core.ui.toast.ToastController
import org.nha.project.feature.hospital.domain.Hospital

private const val LOCATION_TIMEOUT_MILLIS = 15_000L

class HospitalLocationVerificationViewModel(
    private val locationProvider: CurrentLocationProvider,
    private val toastController: ToastController,
    private val hospital: Hospital,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HospitalLocationVerificationUiState())
    val uiState: StateFlow<HospitalLocationVerificationUiState> = _uiState.asStateFlow()

    init {
        refreshLocation()
    }

    fun refreshLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = withTimeoutOrNull(LOCATION_TIMEOUT_MILLIS) { locationProvider.getCurrentLocation() }
            _uiState.update { it.copy(isLoading = false) }

            when (result) {
                is LocationResult.Success -> {
                    _uiState.update {
                        it.copy(
                            currentLocation = result.location,
                            distanceMeters = result.location.distanceMetersTo(hospital.hfrLocation),
                        )
                    }
                }
                LocationResult.PermissionDenied ->
                    toastController.error("Location permission is required. Please enable it in Settings.")
                LocationResult.ProviderDisabled ->
                    toastController.error("Please turn on device location (GPS) and try again.")
                is LocationResult.Failed ->
                    toastController.error(result.reason ?: "Could not get your current location. Please try again.")
                null ->
                    toastController.error(
                        "Could not get a location fix. Make sure GPS is enabled and try again outdoors.",
                    )
            }
        }
    }
}
