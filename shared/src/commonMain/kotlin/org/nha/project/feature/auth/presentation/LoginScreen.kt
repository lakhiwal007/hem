package org.nha.project.feature.auth.presentation

import androidx.compose.runtime.Composable
import hem.shared.generated.resources.Res
import hem.shared.generated.resources.login
import org.nha.project.core.ui.components.PlaceholderScreen

@Composable
fun LoginScreen(onContinue: () -> Unit) {
    PlaceholderScreen(
        title = "Login",
        imageRes = Res.drawable.login,
        onContinue = onContinue,
    )
}
