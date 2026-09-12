package org.nha.project.feature.hospital.presentation

import org.nha.project.feature.hospital.domain.Service

data class HospitalServicesUiState(
    val isLoading: Boolean = false,
    val services: List<Service> = emptyList(),
    val isPhysicalVerifier: Boolean = false,
    val expandedServiceId: Long? = null,
)
