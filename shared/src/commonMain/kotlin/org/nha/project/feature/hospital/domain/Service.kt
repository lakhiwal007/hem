package org.nha.project.feature.hospital.domain

import kotlinx.serialization.Serializable

@Serializable
data class Service(
    val name: String,
    val id: Long = 0L,
    val specialityId: Long = 0L,
    val hasUploadedImages: Boolean = false,
)
