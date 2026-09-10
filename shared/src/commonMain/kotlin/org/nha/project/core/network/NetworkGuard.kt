package org.nha.project.core.network

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.koin.compose.koinInject
import org.nha.project.core.navigation.Route
import org.nha.project.core.ui.components.NoNetworkSheet

private val ROUTES_WITHOUT_NETWORK_GUARD = setOf(Route.Splash)

@Composable
fun NetworkGuard(
    currentRoute: Route?,
    content: @Composable () -> Unit,
) {
    val observer = koinInject<NetworkConnectivityObserver>()
    var retryTrigger by remember { mutableStateOf(0) }
    val isConnected by remember(retryTrigger) { observer.observe() }.collectAsState(initial = true)

    Box(modifier = Modifier.fillMaxSize()) {
        content()

        val shouldBlock = !isConnected && currentRoute != null && currentRoute !in ROUTES_WITHOUT_NETWORK_GUARD
        if (shouldBlock) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {},
            )
            NoNetworkSheet(
                onRetry = { retryTrigger++ },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
