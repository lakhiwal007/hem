package org.nha.project.core.location

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.koin.compose.koinInject
import org.nha.project.core.navigation.Route
import org.nha.project.core.ui.components.LocationAccessSheet

private val ROUTES_WITHOUT_GUARD = setOf(Route.Splash, Route.LocationPermission)

@Composable
fun LocationGuard(
    currentRoute: Route?,
    content: @Composable () -> Unit,
) {
    val observer = koinInject<LocationAccessObserver>()
    val status by observer
        .observe()
        .collectAsState(initial = LocationAccessStatus(permissionGranted = true, servicesEnabled = true))
    val requestPermission = rememberRequestLocationPermission(onResult = {})

    Box(modifier = Modifier.fillMaxSize()) {
        content()

        val shouldBlock = !status.isAccessible && currentRoute != null && currentRoute !in ROUTES_WITHOUT_GUARD
        if (shouldBlock) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {},
            )
            LocationAccessSheet(
                onAction = {
                    if (!status.permissionGranted) requestPermission() else observer.openLocationSettings()
                },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
