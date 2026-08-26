package org.nha.project.core.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
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
