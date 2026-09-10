package org.nha.project.feature.hospital.domain

import kotlinx.serialization.Serializable

@Serializable
data class Service(
    val name: String,
    val id: Long = 0L,
    val specialityId: Long = 0L,
    val showCheckmark: Boolean = false,
    val verificationStatus: String? = null,
    val verifierComments: String? = null,
)
