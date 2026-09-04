package org.nha.project.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.nha.project.feature.hospital.domain.Hospital
import org.nha.project.feature.hospital.domain.Service
import org.nha.project.feature.hospital.domain.Speciality

sealed interface Route : NavKey {
    @Serializable
    data object Splash : Route

    @Serializable
    data object LocationPermission : Route

    @Serializable
    data object Login : Route

    @Serializable
    data object HospitalList : Route

    @Serializable
    data class HospitalLocationVerification(
        val hospital: Hospital,
    ) : Route

    @Serializable
    data class HospitalSpecialities(
        val hospital: Hospital,
    ) : Route

    @Serializable
    data class HospitalServices(
        val speciality: Speciality,
    ) : Route

    @Serializable
    data class Capture(
        val service: Service,
        val speciality: String,
    ) : Route

    @Serializable
    data class HospitalOtpVerification(
        val hospital: Hospital,
    ) : Route

    @Serializable
    data class PhysicalVerifyImages(
        val service: Service,
    ) : Route

    @Serializable
    data object Status : Route
}
