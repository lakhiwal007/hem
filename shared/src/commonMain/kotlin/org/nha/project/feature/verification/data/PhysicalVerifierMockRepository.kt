package org.nha.project.feature.verification.data

import hem.shared.generated.resources.Res
import hem.shared.generated.resources.capture
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.nha.project.feature.hospital.domain.Hospital
import org.nha.project.feature.hospital.domain.Service

const val MOCK_VALID_OTP = "123456"
private const val MOCK_NETWORK_DELAY_MILLIS = 600L
private const val MOCK_UPLOADED_IMAGE_COUNT = 3

enum class VerificationAction(
    val label: String,
) {
    RECOMMENDED("Recommended"),
    NOT_RECOMMENDED("Not Recommended"),
}

data class UploadedImage(
    val label: String,
    val drawable: DrawableResource,
)

/**
 * No real backend exists yet for the physical-verifier OTP and image-review flow.
 * This in-memory mock stands in until those APIs are ready.
 */
class PhysicalVerifierMockRepository {
    suspend fun sendOtp(hospital: Hospital) {
        delay(MOCK_NETWORK_DELAY_MILLIS)
    }

    suspend fun verifyOtp(code: String): Boolean {
        delay(MOCK_NETWORK_DELAY_MILLIS)
        return code == MOCK_VALID_OTP
    }

    suspend fun getUploadedImages(service: Service): List<UploadedImage> {
        delay(MOCK_NETWORK_DELAY_MILLIS)
        return (1..MOCK_UPLOADED_IMAGE_COUNT).map { index ->
            UploadedImage(label = "Image $index", drawable = Res.drawable.capture)
        }
    }

    suspend fun submitVerification(
        service: Service,
        action: VerificationAction,
        comments: String,
    ) {
        delay(MOCK_NETWORK_DELAY_MILLIS)
    }
}
