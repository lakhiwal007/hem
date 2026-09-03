package org.nha.project.feature.hospital.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import hem.shared.generated.resources.hospitals
import hem.shared.generated.resources.in_progress_hospitals
import hem.shared.generated.resources.phone
import hem.shared.generated.resources.right_arrow
import hem.shared.generated.resources.success
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.nha.project.core.location.GeoPoint
import org.nha.project.core.ui.components.BrandedHeader
import org.nha.project.core.ui.theme.HemFocusBorder
import org.nha.project.core.ui.theme.HemSuccess
import org.nha.project.core.ui.theme.HemTheme
import org.nha.project.core.ui.theme.HemWarning
import org.nha.project.feature.hospital.domain.Hospital
import org.nha.project.feature.hospital.domain.HospitalStatus

@Composable
fun HospitalListScreen(
    onHospitalClick: (Hospital) -> Unit,
    onLogout: () -> Unit,
    onBack: (() -> Unit)? = null,
) {
    val viewModel = koinViewModel<HospitalListViewModel>()
    val state by viewModel.uiState.collectAsState()

    HospitalListContent(
        state = state,
        onHospitalClick = onHospitalClick,
        onRefresh = viewModel::loadHospitals,
        onLogout = { viewModel.logout(onLogout) },
        onBack = onBack,
    )
}

@Composable
private fun HospitalListContent(
    state: HospitalListUiState,
    onHospitalClick: (Hospital) -> Unit,
    onRefresh: () -> Unit,
    onLogout: () -> Unit,
    onBack: (() -> Unit)? = null,
) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        BrandedHeader(onBack = onBack, onLogout = onLogout)

        if (state.hospitals.isEmpty() && state.isLoading) {
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
                    text = "Hospitals/अस्पताल",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "as per your current location",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (state.empanelled.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    HospitalSectionHeader(
                        icon = Res.drawable.success,
                        label = "Empanelled Hospitals (${state.empanelled.size})",
                        color = HemSuccess,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        state.empanelled.forEach { hospital ->
                            HospitalCard(hospital = hospital, onClick = { onHospitalClick(hospital) })
                        }
                    }
                }

                if (state.inProgress.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    HospitalSectionHeader(
                        icon = Res.drawable.in_progress_hospitals,
                        label = "In Progress Hospitals (${state.inProgress.size})",
                        color = HemWarning,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        state.inProgress.forEach { hospital ->
                            HospitalCard(hospital = hospital, onClick = { onHospitalClick(hospital) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HospitalSectionHeader(
    icon: DrawableResource,
    label: String,
    color: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

@Composable
private fun HospitalCard(
    hospital: Hospital,
    onClick: () -> Unit,
) {
    val tint = if (hospital.status == HospitalStatus.EMPANELLED) HemSuccess else HemWarning
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(tint.copy(alpha = 0.10f))
                .clickable(onClick = onClick)
                .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(Res.drawable.hospitals),
            contentDescription = null,
            modifier = Modifier.size(44.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = hospital.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = hospital.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(Res.drawable.phone),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = hospital.phone,
                    style = MaterialTheme.typography.bodySmall,
                    color = HemFocusBorder,
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Image(
            painter = painterResource(Res.drawable.right_arrow),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Preview
@Composable
private fun HospitalListScreenPreview() {
    HemTheme {
        HospitalListContent(
            state =
                HospitalListUiState(
                    hospitals =
                        listOf(
                            Hospital(
                                name = "Shree Ganga Ram Hospital",
                                description = "Multi Speciality - Karol Bagh, Delhi - 110002",
                                phone = "9827364738",
                                status = HospitalStatus.EMPANELLED,
                                hfrLocation = GeoPoint(latitude = 28.6379, longitude = 77.1900),
                            ),
                            Hospital(
                                name = "City Centre Hospital",
                                description = "Multi Speciality - Karol Bagh, Delhi - 110002",
                                phone = "9827364738",
                                status = HospitalStatus.IN_PROGRESS,
                                hfrLocation = GeoPoint(latitude = 28.6296, longitude = 77.2187),
                            ),
                        ),
                ),
            onHospitalClick = {},
            onRefresh = {},
            onLogout = {},
        )
    }
}
