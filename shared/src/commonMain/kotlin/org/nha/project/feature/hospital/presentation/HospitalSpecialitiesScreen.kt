package org.nha.project.feature.hospital.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hem.shared.generated.resources.Res
import hem.shared.generated.resources.right_arrow
import org.jetbrains.compose.resources.painterResource
import org.nha.project.core.location.GeoPoint
import org.nha.project.core.ui.components.BrandedHeader
import org.nha.project.core.ui.theme.HemPrimary
import org.nha.project.core.ui.theme.HemTheme
import org.nha.project.feature.hospital.domain.Hospital
import org.nha.project.feature.hospital.domain.HospitalStatus

@Composable
fun HospitalSpecialitiesScreen(
    hospital: Hospital,
    onBack: () -> Unit,
    onSpecialityClick: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        BrandedHeader(onBack = onBack)

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .offset(y = (-16).dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp, bottom = 24.dp),
        ) {
            Text(
                text = hospital.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Specialities",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = HemPrimary,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                hospital.specialities.forEach { speciality ->
                    SpecialityCard(
                        speciality = speciality,
                        onClick = { onSpecialityClick(speciality) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SpecialityCard(
    speciality: String,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(width = 1.dp, color = HemPrimary.copy(alpha = 0.2f), shape = RoundedCornerShape(10.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = speciality,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.DarkGray,
            modifier = Modifier.weight(1f),
        )
        Image(
            painter = painterResource(Res.drawable.right_arrow),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Preview
@Composable
private fun HospitalSpecialitiesScreenPreview() {
    HemTheme {
        HospitalSpecialitiesScreen(
            hospital =
                Hospital(
                    name = "Shree Ganga Ram Hospital",
                    description = "",
                    phone = "",
                    status = HospitalStatus.EMPANELLED,
                    hfrLocation = GeoPoint(28.6139, 77.2090),
                    specialities = listOf("CTVS", "Cardiology", "Interventional Neuroradiology", "Medical Oncology"),
                ),
            onBack = {},
            onSpecialityClick = {},
        )
    }
}
