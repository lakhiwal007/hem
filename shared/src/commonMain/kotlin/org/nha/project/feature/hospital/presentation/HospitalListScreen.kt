package org.nha.project.feature.hospital.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import hem.shared.generated.resources.Res
import hem.shared.generated.resources.hospitals
import org.nha.project.core.ui.components.PlaceholderScreen
import org.nha.project.core.ui.theme.HemTheme

@Composable
fun HospitalListScreen(onContinue: () -> Unit) {
    PlaceholderScreen(
        title = "Hospitals",
        imageRes = Res.drawable.hospitals,
        onContinue = onContinue,
    )
}

@Preview
@Composable
private fun HospitalListScreenPreview() {
    HemTheme {
        HospitalListScreen(onContinue = {})
    }
}
