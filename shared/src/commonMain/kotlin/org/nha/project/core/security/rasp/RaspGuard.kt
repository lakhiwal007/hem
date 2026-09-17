package org.nha.project.core.security.rasp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import org.nha.project.core.ui.components.RaspThreatOverlay

@Composable
fun RaspGuard(content: @Composable () -> Unit) {
    val observer = koinInject<RaspThreatObserver>()
    var retryTrigger by remember { mutableStateOf(0) }
    val threats by remember(retryTrigger) { observer.observe() }.collectAsState(initial = emptyList())

    Box(modifier = Modifier.fillMaxSize()) {
        content()

        if (threats.isNotEmpty()) {
            RaspThreatOverlay(
                threats = threats,
                onCheckAgain = { retryTrigger++ },
                modifier = Modifier,
            )
        }
    }
}
