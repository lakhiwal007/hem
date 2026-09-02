package org.nha.project.feature.hospital.presentation

import org.nha.project.feature.hospital.domain.Speciality

data class HospitalSpecialitiesUiState(
    val isLoading: Boolean = false,
    val specialities: List<Speciality> = emptyList(),
)
