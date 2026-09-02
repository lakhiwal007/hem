package org.nha.project.feature.hospital.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import hem.shared.generated.resources.success
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.nha.project.core.ui.components.BrandedHeader
import org.nha.project.core.ui.components.LoadingOverlay
import org.nha.project.core.ui.theme.HemPrimary
import org.nha.project.core.ui.theme.HemTheme
import org.nha.project.feature.hospital.domain.Service
import org.nha.project.feature.hospital.domain.Speciality

@Composable
fun HospitalServicesScreen(
    speciality: Speciality,
    onBack: () -> Unit,
    onServiceClick: (Service) -> Unit,
) {
    val viewModel = koinViewModel<HospitalServicesViewModel> { parametersOf(speciality) }
    val state by viewModel.uiState.collectAsState()

    HospitalServicesContent(
        specialityName = speciality.description,
        state = state,
        onBack = onBack,
        onServiceClick = onServiceClick,
    )
}

@Composable
private fun HospitalServicesContent(
    specialityName: String,
    state: HospitalServicesUiState,
    onBack: () -> Unit,
    onServiceClick: (Service) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        BrandedHeader(onBack = onBack)

        if (state.services.isEmpty() && state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        LoadingOverlay(
            isLoading = state.isLoading,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .offset(y = (-16).dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(MaterialTheme.colorScheme.background),
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp, bottom = 24.dp),
            ) {
                Text(
                    text = specialityName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(20.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.services.forEach { service ->
                        ServiceCard(service = service, onClick = { onServiceClick(service) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceCard(
    service: Service,
    onClick: () -> Unit,
) {
    val backgroundColor = if (service.hasUploadedImages) Color(0xFFF0F0F0) else MaterialTheme.colorScheme.surface
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(backgroundColor)
                .let {
                    if (service.hasUploadedImages) {
                        it
                    } else {
                        it.border(
                            width = 1.dp,
                            color = HemPrimary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(10.dp),
                        )
                    }
                }.clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = service.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.DarkGray,
            modifier = Modifier.weight(1f),
        )
        if (service.hasUploadedImages) {
            Image(
                painter = painterResource(Res.drawable.success),
                contentDescription = "Images uploaded",
                modifier = Modifier.size(20.dp).clip(CircleShape),
            )
        } else {
            Image(
                painter = painterResource(Res.drawable.right_arrow),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Preview
@Composable
private fun HospitalServicesScreenPreview() {
    HemTheme {
        HospitalServicesContent(
            specialityName = "CTVS",
            state =
                HospitalServicesUiState(
                    services =
                        listOf(
                            Service(name = "Heart Lung Machines", hasUploadedImages = true),
                            Service(name = "Blood Gas And Electrolyte Analysers"),
                            Service(name = "OT"),
                            Service(name = "ICU"),
                        ),
                ),
            onBack = {},
            onServiceClick = {},
        )
    }
}
