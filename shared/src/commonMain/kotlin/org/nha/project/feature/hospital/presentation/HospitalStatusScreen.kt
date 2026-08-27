package org.nha.project.feature.hospital.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import hem.shared.generated.resources.Res
import hem.shared.generated.resources.empanelled_hospitals
import org.nha.project.core.ui.components.PlaceholderScreen
import org.nha.project.core.ui.theme.HemTheme

@Composable
fun HospitalStatusScreen() {
    PlaceholderScreen(
        title = "Empanelment Status",
        imageRes = Res.drawable.empanelled_hospitals,
        onContinue = null,
    )
}

@Preview
@Composable
private fun HospitalStatusScreenPreview() {
    HemTheme {
        HospitalStatusScreen()
    }
}
