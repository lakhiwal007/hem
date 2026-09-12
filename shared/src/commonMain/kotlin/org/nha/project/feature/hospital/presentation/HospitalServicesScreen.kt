package org.nha.project.feature.hospital.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import org.nha.project.core.ui.theme.HemPrimary
import org.nha.project.core.ui.theme.HemTheme
import org.nha.project.feature.capture.presentation.CaptureAccordionContent
import org.nha.project.feature.hospital.domain.Hospital
import org.nha.project.feature.hospital.domain.Service
import org.nha.project.feature.hospital.domain.Speciality
import org.nha.project.feature.verification.presentation.VerifyAccordionContent

@Composable
fun HospitalServicesScreen(
    hospital: Hospital,
    speciality: Speciality,
    onBack: () -> Unit,
) {
    val viewModel = koinViewModel<HospitalServicesViewModel> { parametersOf(hospital, speciality) }
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadServices()
    }

    HospitalServicesContent(
        hospital = hospital,
        speciality = speciality,
        state = state,
        onBack = onBack,
        onRefresh = viewModel::loadServices,
        onServiceClick = { service -> viewModel.toggleExpanded(service.id) },
        onSubmitted = viewModel::loadServices,
    )
}

@Composable
private fun HospitalServicesContent(
    hospital: Hospital,
    speciality: Speciality,
    state: HospitalServicesUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onServiceClick: (Service) -> Unit,
    onSubmitted: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        BrandedHeader(onBack = onBack)

        if (state.services.isEmpty() && state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = onRefresh,
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .offset(y = (-16).dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(MaterialTheme.colorScheme.background),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp, bottom = 24.dp),
            ) {
                Text(
                    text = speciality.description,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(20.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.services.forEach { service ->
                        ServiceAccordionItem(
                            hospital = hospital,
                            speciality = speciality,
                            service = service,
                            expanded = state.expandedServiceId == service.id,
                            isPhysicalVerifier = state.isPhysicalVerifier,
                            onClick = { onServiceClick(service) },
                            onSubmitted = onSubmitted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceAccordionItem(
    hospital: Hospital,
    speciality: Speciality,
    service: Service,
    expanded: Boolean,
    isPhysicalVerifier: Boolean,
    onClick: () -> Unit,
    onSubmitted: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .let {
                    if (expanded) {
                        it
                    } else {
                        it.border(
                            width = 1.dp,
                            color = HemPrimary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(10.dp),
                        )
                    }
                }.background(if (service.showCheckmark) Color(0xFFF0F0F0) else MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
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
            if (service.showCheckmark) {
                Image(
                    painter = painterResource(Res.drawable.success),
                    contentDescription = "Images uploaded",
                    modifier = Modifier.size(20.dp).clip(CircleShape),
                )
                Spacer(modifier = Modifier.size(8.dp))
            }
            val arrowRotation by animateFloatAsState(targetValue = if (expanded) 90f else 0f)
            Image(
                painter = painterResource(Res.drawable.right_arrow),
                contentDescription = null,
                modifier = Modifier.size(16.dp).rotate(arrowRotation),
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
            ) {
                if (isPhysicalVerifier) {
                    VerifyAccordionContent(
                        hospital = hospital,
                        speciality = speciality,
                        service = service,
                        onSubmitted = onSubmitted,
                    )
                } else {
                    CaptureAccordionContent(
                        hospital = hospital,
                        speciality = speciality,
                        service = service,
                        onSubmitted = onSubmitted,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun HospitalServicesScreenPreview() {
    HemTheme {
        HospitalServicesContent(
            hospital =
                Hospital(
                    name = "Shree Ganga Ram Hospital",
                    description = "",
                    phone = "",
                    status = org.nha.project.feature.hospital.domain.HospitalStatus.EMPANELLED,
                    hfrLocation =
                        org.nha.project.core.location
                            .GeoPoint(28.6139, 77.2090),
                ),
            speciality = Speciality(id = 1L, code = "CTVS", description = "CTVS"),
            state =
                HospitalServicesUiState(
                    services =
                        listOf(
                            Service(name = "Heart Lung Machines", showCheckmark = true),
                            Service(name = "Blood Gas And Electrolyte Analysers"),
                            Service(name = "OT"),
                            Service(name = "ICU"),
                        ),
                ),
            onBack = {},
            onRefresh = {},
            onServiceClick = {},
            onSubmitted = {},
        )
    }
}
