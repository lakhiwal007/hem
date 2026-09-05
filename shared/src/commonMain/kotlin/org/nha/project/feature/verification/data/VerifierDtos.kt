package org.nha.project.feature.verification.data

import kotlinx.serialization.Serializable
import org.nha.project.core.location.GeoPoint
import org.nha.project.feature.hospital.domain.Hospital
import org.nha.project.feature.hospital.domain.HospitalStatus

@Serializable
data class VerifierWorklistItemDto(
    val hospId: Long,
    val facilityId: String? = null,
    val facilityName: String? = null,
    val dueDate: String? = null,
    val status: Int? = null,
    val schemeCode: String? = null,
    val orderId: String? = null,
    val remarks: String? = null,
    val hospMobileNumber: Long? = null,
    val hospEmailId: String? = null,
    val hospContactNumber: String? = null,
    val hospLatitude: String? = null,
    val hospLongitude: String? = null,
    val specialityList: String? = null,
)

@Serializable
data class ValidateOtpRequest(
    val transactionId: String,
    val otp: String,
)

@Serializable
data class ValidateOtpResponseDto(
    val id: Long? = null,
    val transactionId: String? = null,
    val mobileNumber: Long? = null,
)

@Serializable
data class GenerateOtpResponseDto(
    val message: String? = null,
    val hospitalId: Long? = null,
    val transactionid: String? = null,
)

@Serializable
data class VerifierActionRequest(
    val submissionId: Long,
    val hospId: Long,
    val specialityId: Long,
    val serviceId: Long,
    val verificationStatus: String,
    val comments: String,
    val verifiedBy: String,
)

@Serializable
data class VerifierActionResponseDto(
    val verificationId: Long? = null,
    val verificationStatus: String? = null,
    val comments: String? = null,
    val verifiedBy: String? = null,
    val verifiedOn: String? = null,
)

fun List<VerifierWorklistItemDto>.toHospitals(): List<Hospital> = mapNotNull { it.toHospital() }

private fun VerifierWorklistItemDto.toHospital(): Hospital? {
    val latitude = hospLatitude?.toDoubleOrNull() ?: return null
    val longitude = hospLongitude?.toDoubleOrNull() ?: return null
    return Hospital(
        hospitalId = hospId,
        name = facilityName.orEmpty(),
        description = dueDate?.let { "Verification due $it" }.orEmpty(),
        phone = hospMobileNumber?.toString() ?: hospContactNumber.orEmpty(),
        status = HospitalStatus.EMPANELLED,
        hfrLocation = GeoPoint(latitude = latitude, longitude = longitude),
        hfrId = facilityId.orEmpty(),
        schemeCode = schemeCode ?: "PMJAY",
    )
}
