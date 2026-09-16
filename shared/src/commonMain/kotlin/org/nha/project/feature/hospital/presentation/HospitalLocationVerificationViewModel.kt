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
import org.nha.project.core.location.GeoPoint
import org.nha.project.core.location.LocationResult
import org.nha.project.core.location.ReverseGeocoder
import org.nha.project.core.location.distanceMetersTo
import org.nha.project.core.location.districtStateLabel
import org.nha.project.core.network.ApiResult
import org.nha.project.core.reference.INDIAN_STATE_NAMES
import org.nha.project.core.ui.toast.ToastController
import org.nha.project.feature.hospital.data.DistrictApi
import org.nha.project.feature.hospital.domain.Hospital

private const val LOCATION_TIMEOUT_MILLIS = 15_000L

class HospitalLocationVerificationViewModel(
    private val locationProvider: CurrentLocationProvider,
    private val reverseGeocoder: ReverseGeocoder,
    private val districtApi: DistrictApi,
    private val toastController: ToastController,
    private val hospital: Hospital,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HospitalLocationVerificationUiState())
    val uiState: StateFlow<HospitalLocationVerificationUiState> = _uiState.asStateFlow()

    init {
        refreshLocation()
        loadHfrLocationLabel()
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
                    loadCurrentLocationLabel(result.location.latitude, result.location.longitude)
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

    private fun loadCurrentLocationLabel(
        latitude: Double,
        longitude: Double,
    ) {
        viewModelScope.launch {
            val placeName =
                runCatching { reverseGeocoder.reverseGeocode(GeoPoint(latitude, longitude)) }.getOrNull()
            _uiState.update { it.copy(currentLocationLabel = placeName.districtStateLabel()) }
        }
    }

    private fun loadHfrLocationLabel() {
        val stateCode = hospital.stateCode
        val stateName = stateCode?.let { INDIAN_STATE_NAMES[it] }
        val districtCode = hospital.districtCode
        if (stateCode == null || districtCode == null) {
            _uiState.update { it.copy(hfrLocationLabel = stateName) }
            return
        }
        viewModelScope.launch {
            val result = districtApi.getDistricts(stateCode)
            val districtName =
                (result as? ApiResult.Success)
                    ?.data
                    ?.firstOrNull { it.lgdcode == districtCode }
                    ?.lgdname
                    ?.toTitleCase()
            val label = listOfNotNull(stateName, "India").joinToString(", ").ifBlank { null }
            _uiState.update { it.copy(hfrLocationLabel = label) }
        }
    }

    private fun String.toTitleCase(): String =
        lowercase().split(" ").joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
}
