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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.nha.project.core.capture.ImageMetadataStamper
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
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private const val LOCATION_TIMEOUT_MILLIS = 8_000L

class CaptureViewModel(
    private val captureApi: CaptureApi,
    private val sessionStorage: SessionStorage,
    private val locationProvider: CurrentLocationProvider,
    private val imageMetadataStamper: ImageMetadataStamper,
    private val toastController: ToastController,
    private val hospital: Hospital,
    private val speciality: Speciality,
    private val service: Service,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CaptureUiState(serviceName = service.name))
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    init {
        loadExistingImages()
    }

    private fun loadExistingImages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result =
                captureApi.getUploadedImages(
                    hospId = hospital.hospitalId,
                    specialityId = speciality.id,
                    serviceId = service.id,
                )
            when (result) {
                is ApiResult.Success -> {
                    val images =
                        result.data
                            .sortedBy { it.imageSlot ?: Int.MAX_VALUE }
                            .mapIndexed { index, image ->
                                CapturedImage(
                                    base64 = image.base64Image,
                                    fileName = image.fileName ?: fileNameFor(index),
                                    location = null,
                                )
                            }
                    _uiState.update { state -> state.copy(images = images, isLoading = false) }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun addImage(base64: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val location = fetchLocationOrNull()
            val stamped = stampMetadata(base64, location)
            uploadImage(
                slot = _uiState.value.images.size + 1,
                base64 = stamped,
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
            val stamped = stampMetadata(base64, location)
            uploadImage(slot = index + 1, base64 = stamped, location = location, existingIndex = index)
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun stampMetadata(
        base64: String,
        location: GeoPoint?,
    ): String {
        val now = Clock.System.now()
        val watermarkLines =
            listOf(
                hospital.name,
                "${speciality.description} - ${service.name}",
                location?.let { "Lat: ${it.latitude.rounded()}, Long: ${it.longitude.rounded()}" }
                    ?: "Location unavailable",
                formatTimestamp(now.toEpochMilliseconds()),
            )
        return imageMetadataStamper.stamp(
            base64Jpeg = base64,
            watermarkLines = watermarkLines,
            latitude = location?.latitude,
            longitude = location?.longitude,
            timestampMillis = now.toEpochMilliseconds(),
        )
    }

    @OptIn(ExperimentalTime::class)
    private fun formatTimestamp(epochMillis: Long): String {
        val dateTime =
            kotlin.time.Instant
                .fromEpochMilliseconds(epochMillis)
                .toLocalDateTime(TimeZone.currentSystemDefault())

        @Suppress("DEPRECATION")
        val date = "${dateTime.dayOfMonth.pad()}-${dateTime.monthNumber.pad()}-${dateTime.year}"
        val time = "${dateTime.hour.pad()}:${dateTime.minute.pad()}"
        return "$date $time"
    }

    private fun Int.pad(): String = toString().padStart(2, '0')

    private fun Double.rounded(): Double = kotlin.math.round(this * 100000) / 100000

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
                        requiredImageCount = submission.count ?: state.requiredImageCount,
                    )
                }
                notifyUploadProgress(_uiState.value)
            }
            is ApiResult.Error -> {
                _uiState.update { it.copy(isLoading = false) }
                toastController.error("Could not upload image. Please try again.")
            }
        }
    }

    private fun notifyUploadProgress(state: CaptureUiState) {
        val message =
            when {
                state.finalSubmitAllowed -> "All required images uploaded. You can now submit."
                else ->
                    state.remainingRequiredCount?.takeIf { it > 0 }?.let { remaining ->
                        val imageWord = if (remaining == 1) "image" else "images"
                        "Image uploaded. Upload $remaining more $imageWord to enable submit."
                    } ?: "Image uploaded."
            }
        toastController.success(message)
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
