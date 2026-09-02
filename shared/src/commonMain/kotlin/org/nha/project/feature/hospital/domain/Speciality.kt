package org.nha.project.feature.hospital.domain

import kotlinx.serialization.Serializable

@Serializable
data class Speciality(
    val id: Long,
    val code: String,
    val description: String,
)
