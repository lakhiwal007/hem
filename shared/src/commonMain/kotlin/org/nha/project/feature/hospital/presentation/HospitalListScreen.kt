package org.nha.project.feature.hospital.presentation

import androidx.compose.runtime.Composable
import hem.shared.generated.resources.Res
import hem.shared.generated.resources.hospitals
import org.nha.project.core.ui.components.PlaceholderScreen

@Composable
fun HospitalListScreen(onContinue: () -> Unit) {
    PlaceholderScreen(
        title = "Hospitals",
        imageRes = Res.drawable.hospitals,
        onContinue = onContinue,
    )
}
