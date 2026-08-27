package org.nha.project.feature.capture.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import hem.shared.generated.resources.Res
import hem.shared.generated.resources.capture
import org.nha.project.core.ui.components.PlaceholderScreen
import org.nha.project.core.ui.theme.HemTheme

@Composable
fun CaptureScreen(onContinue: () -> Unit) {
    PlaceholderScreen(
        title = "Capture Documents",
        imageRes = Res.drawable.capture,
        onContinue = onContinue,
    )
}

@Preview
@Composable
private fun CaptureScreenPreview() {
    HemTheme {
        CaptureScreen(onContinue = {})
    }
}
