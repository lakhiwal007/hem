package org.nha.project.feature.hospital.domain

import kotlinx.serialization.Serializable
import org.nha.project.core.location.GeoPoint

@Serializable
enum class HospitalStatus { EMPANELLED, IN_PROGRESS }

@Serializable
data class Hospital(
    val hospitalId: Long = 0L,
    val name: String,
    val description: String,
    val phone: String,
    val status: HospitalStatus,
    val hfrLocation: GeoPoint,
    val hfrId: String = "",
    val schemeCode: String = "PMJAY",
)
