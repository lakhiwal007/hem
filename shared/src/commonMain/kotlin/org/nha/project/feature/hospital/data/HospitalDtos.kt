package org.nha.project.feature.hospital.data

import kotlinx.serialization.Serializable
import org.nha.project.core.location.GeoPoint
import org.nha.project.feature.hospital.domain.Hospital
import org.nha.project.feature.hospital.domain.HospitalStatus
import org.nha.project.feature.hospital.domain.Service
import org.nha.project.feature.hospital.domain.Speciality

@Serializable
data class HospitalGroupDto(
    val statusFlag: String? = null,
    val totalRecords: Int? = null,
    val hospitals: List<HospitalDto> = emptyList(),
)

@Serializable
data class HospitalDto(
    val hospitalId: Long,
    val hospName: String? = null,
    val hospAddress: String? = null,
    val hospContactNumber: String? = null,
    val hospMobileNumber: Long? = null,
    val hfrId: String? = null,
    val hospLatitude: String? = null,
    val hospLongitude: String? = null,
    val statusFlag: String? = null,
    val schemeCode: String? = null,
)

@Serializable
data class SpecialityDto(
    val hospId: Long? = null,
    val specialityId: Long,
    val specialityCode: String? = null,
    val specialityDescription: String? = null,
    val schemeCode: String? = null,
)

@Serializable
data class ServiceDto(
    val serviceId: Long? = null,
    val serviceName: String? = null,
    val submissionStatus: String? = null,
    val verificationStatus: String? = null,
    val verifierComments: String? = null,
)

fun HospitalGroupDto.toDomain(): List<Hospital> = hospitals.mapNotNull { it.toDomain(statusFlag) }

private fun HospitalDto.toDomain(groupStatusFlag: String?): Hospital? {
    val latitude = hospLatitude?.toDoubleOrNull() ?: return null
    val longitude = hospLongitude?.toDoubleOrNull() ?: return null
    val flag = groupStatusFlag ?: statusFlag
    val status =
        if (flag.equals("Empanelled", ignoreCase = true)) {
            HospitalStatus.EMPANELLED
        } else {
            HospitalStatus.IN_PROGRESS
        }
    return Hospital(
        hospitalId = hospitalId,
        name = hospName.orEmpty(),
        description = hospAddress.orEmpty(),
        phone = hospContactNumber?.takeIf { it.isNotBlank() } ?: hospMobileNumber?.toString().orEmpty(),
        status = status,
        hfrLocation = GeoPoint(latitude = latitude, longitude = longitude),
        hfrId = hfrId.orEmpty(),
        schemeCode = schemeCode ?: "PMJAY",
    )
}

fun List<SpecialityDto>.toSpecialities(): List<Speciality> =
    mapNotNull { dto ->
        val description = dto.specialityDescription
        if (description.isNullOrBlank()) {
            null
        } else {
            Speciality(id = dto.specialityId, code = dto.specialityCode.orEmpty(), description = description)
        }
    }.distinctBy { it.id }

fun List<ServiceDto>.toServices(
    specialityId: Long,
    isPhysicalVerifier: Boolean,
): List<Service> =
    mapNotNull { dto ->
        val name = dto.serviceName?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        val id = dto.serviceId ?: return@mapNotNull null
        val showCheckmark =
            if (isPhysicalVerifier) {
                !dto.verificationStatus.isNullOrBlank() && !dto.verificationStatus.equals("PENDING", ignoreCase = true)
            } else {
                dto.submissionStatus.equals("SUBMITTED", ignoreCase = true)
            }
        Service(
            name = name,
            id = id,
            specialityId = specialityId,
            showCheckmark = showCheckmark,
            verificationStatus = dto.verificationStatus,
            verifierComments = dto.verifierComments,
        )
    }.distinctBy { it.id }
