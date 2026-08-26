package org.nha.project.feature.onboarding.presentation

import androidx.compose.runtime.Composable
import hem.shared.generated.resources.Res
import hem.shared.generated.resources.onboarding_screen_background
import org.nha.project.core.ui.components.PlaceholderScreen

@Composable
fun OnboardingScreen(onContinue: () -> Unit) {
    PlaceholderScreen(
        title = "Onboarding",
        imageRes = Res.drawable.onboarding_screen_background,
        onContinue = onContinue,
    )
}
