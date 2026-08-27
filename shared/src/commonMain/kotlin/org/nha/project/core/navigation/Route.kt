package org.nha.project.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Route : NavKey {
    @Serializable
    data object Splash : Route

    @Serializable
    data object LocationPermission : Route

    @Serializable
    data object Onboarding : Route

    @Serializable
    data object Login : Route

    @Serializable
    data object HospitalList : Route

    @Serializable
    data object Capture : Route

    @Serializable
    data object Status : Route
}
