package org.nha.project.feature.hospital.domain

import kotlinx.serialization.Serializable

@Serializable
data class Service(
    val name: String,
    val hasUploadedImages: Boolean = false,
)
