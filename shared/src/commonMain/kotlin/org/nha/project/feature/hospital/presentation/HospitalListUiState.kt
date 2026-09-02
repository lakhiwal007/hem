package org.nha.project.feature.hospital.presentation

import org.nha.project.feature.hospital.domain.Hospital
import org.nha.project.feature.hospital.domain.HospitalStatus

data class HospitalListUiState(
    val isLoading: Boolean = false,
    val hospitals: List<Hospital> = emptyList(),
) {
    val empanelled: List<Hospital> get() = hospitals.filter { it.status == HospitalStatus.EMPANELLED }
    val inProgress: List<Hospital> get() = hospitals.filter { it.status == HospitalStatus.IN_PROGRESS }
}
