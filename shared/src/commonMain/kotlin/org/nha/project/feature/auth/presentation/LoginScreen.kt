package org.nha.project.feature.auth.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import hem.shared.generated.resources.Res
import hem.shared.generated.resources.login
import org.nha.project.core.ui.components.PlaceholderScreen
import org.nha.project.core.ui.theme.HemTheme

@Composable
fun LoginScreen(onContinue: () -> Unit) {
    PlaceholderScreen(
        title = "Login",
        imageRes = Res.drawable.login,
        onContinue = onContinue,
    )
}

@Preview
@Composable
private fun LoginScreenPreview() {
    HemTheme {
        LoginScreen(onContinue = {})
    }
}
