package org.nha.project.core.location

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import org.nha.project.core.navigation.Route
import org.nha.project.core.ui.components.HospitalOutOfRangeSheet

private const val DISTANCE_POLL_INTERVAL_MS = 5_000L

private fun Route?.isWithinHospitalFlow(): Boolean =
    this is Route.HospitalLocationVerification ||
        this is Route.HospitalSpecialities ||
        this is Route.HospitalServices ||
        this is Route.Capture ||
        this is Route.HospitalOtpVerification ||
        this is Route.PhysicalVerifyImages

@Composable
fun HospitalDistanceGuard(
    currentRoute: Route?,
    onExitToHospitalList: () -> Unit,
    content: @Composable () -> Unit,
) {
    val locationProvider = koinInject<CurrentLocationProvider>()
    val tracker = koinInject<HospitalLocationTracker>()
    val target by tracker.activeTarget.collectAsState()
    val isWithinHospitalFlow = currentRoute.isWithinHospitalFlow()

    LaunchedEffect(currentRoute) {
        when (currentRoute) {
            is Route.HospitalLocationVerification ->
                tracker.setTarget(
                    LocationVerificationTarget(currentRoute.hospital.name, currentRoute.hospital.hfrLocation),
                )
            is Route.HospitalSpecialities ->
                tracker.setTarget(
                    LocationVerificationTarget(currentRoute.hospital.name, currentRoute.hospital.hfrLocation),
                )
            is Route.HospitalOtpVerification ->
                tracker.setTarget(
                    LocationVerificationTarget(currentRoute.hospital.name, currentRoute.hospital.hfrLocation),
                )
            Route.HospitalList, Route.Login, Route.Splash, Route.LocationPermission ->
                tracker.clear()
            else -> Unit
        }
    }

    var isOutOfRange by remember { mutableStateOf(false) }
    var distanceMeters by remember { mutableStateOf<Double?>(null) }

    LaunchedEffect(target, isWithinHospitalFlow) {
        val activeTarget = target
        if (activeTarget == null || !isWithinHospitalFlow) {
            isOutOfRange = false
            distanceMeters = null
            return@LaunchedEffect
        }
        while (true) {
            val result = locationProvider.getCurrentLocation()
            if (result is LocationResult.Success) {
                val distance = result.location.distanceMetersTo(activeTarget.hfrLocation)
                distanceMeters = distance
                isOutOfRange = distance > MAX_ALLOWED_DISTANCE_METERS
            }
            delay(DISTANCE_POLL_INTERVAL_MS)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        content()

        if (isWithinHospitalFlow && isOutOfRange && target != null) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {},
            )
            HospitalOutOfRangeSheet(
                hospitalName = target?.hospitalName.orEmpty(),
                distanceMeters = distanceMeters,
                onExit = {
                    tracker.clear()
                    onExitToHospitalList()
                },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
