package org.nha.project.feature.hospital.presentation

import androidx.compose.runtime.Composable
import hem.shared.generated.resources.Res
import hem.shared.generated.resources.empanelled_hospitals
import org.nha.project.core.ui.components.PlaceholderScreen

@Composable
fun HospitalStatusScreen() {
    PlaceholderScreen(
        title = "Empanelment Status",
        imageRes = Res.drawable.empanelled_hospitals,
        onContinue = null,
    )
}
