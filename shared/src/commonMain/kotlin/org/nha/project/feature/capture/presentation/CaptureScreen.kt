package org.nha.project.feature.capture.presentation

import androidx.compose.runtime.Composable
import hem.shared.generated.resources.Res
import hem.shared.generated.resources.capture
import org.nha.project.core.ui.components.PlaceholderScreen

@Composable
fun CaptureScreen(onContinue: () -> Unit) {
    PlaceholderScreen(
        title = "Capture Documents",
        imageRes = Res.drawable.capture,
        onContinue = onContinue,
    )
}
