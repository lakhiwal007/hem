package org.nha.project.feature.permission.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import hem.shared.generated.resources.Res
import hem.shared.generated.resources.onboarding_screen_background
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.nha.project.core.location.LocationAccessObserver
import org.nha.project.core.location.LocationAccessStatus
import org.nha.project.core.location.rememberRequestLocationPermission
import org.nha.project.core.ui.components.LocationAccessSheet
import org.nha.project.core.ui.theme.HemTheme

@Composable
fun LocationPermissionScreen(onContinue: () -> Unit) {
    val observer = koinInject<LocationAccessObserver>()
    val status by observer
        .observe()
        .collectAsState(initial = LocationAccessStatus(permissionGranted = false, servicesEnabled = false))
    val requestPermission = rememberRequestLocationPermission(onResult = {})

    LaunchedEffect(status.isAccessible) {
        if (status.isAccessible) onContinue()
    }

    LocationPermissionContent(
        onAction = {
            if (!status.permissionGranted) requestPermission() else observer.openLocationSettings()
        },
    )
}

@Composable
private fun LocationPermissionContent(onAction: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.onboarding_screen_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f)),
        )
        LocationAccessSheet(
            onAction = onAction,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Preview
@Composable
private fun LocationPermissionScreenPreview() {
    HemTheme {
        LocationPermissionContent(onAction = {})
    }
}
