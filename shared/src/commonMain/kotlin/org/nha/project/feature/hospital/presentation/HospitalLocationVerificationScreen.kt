package org.nha.project.feature.hospital.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hem.shared.generated.resources.Res
import hem.shared.generated.resources.location
import hem.shared.generated.resources.success
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.nha.project.core.location.GeoPoint
import org.nha.project.core.ui.components.BrandedHeader
import org.nha.project.core.ui.components.LoadingOverlay
import org.nha.project.core.ui.theme.HemError
import org.nha.project.core.ui.theme.HemPrimary
import org.nha.project.core.ui.theme.HemSuccess
import org.nha.project.core.ui.theme.HemTheme
import org.nha.project.feature.hospital.domain.Hospital
import org.nha.project.feature.hospital.domain.HospitalStatus
import kotlin.math.round
import kotlin.math.roundToInt

private const val NHPR_URL = "https://nhpr.abdm.gov.in/nhpr/v4/home"

@Composable
fun HospitalLocationVerificationScreen(
    hospital: Hospital,
    onCancel: () -> Unit,
    onContinue: () -> Unit,
) {
    val viewModel = koinViewModel<HospitalLocationVerificationViewModel> { parametersOf(hospital) }
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.distanceMeters) {
        if (state.isWithinRange) onContinue()
    }

    HospitalLocationVerificationContent(
        hospital = hospital,
        state = state,
        onBack = onCancel,
        onCancel = onCancel,
        onContinue = onContinue,
        onRetry = viewModel::refreshLocation,
    )
}

@Composable
private fun HospitalLocationVerificationContent(
    hospital: Hospital,
    state: HospitalLocationVerificationUiState,
    onBack: () -> Unit,
    onCancel: () -> Unit,
    onContinue: () -> Unit,
    onRetry: () -> Unit,
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
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Location Verification",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = HemPrimary,
            )

            if (state.currentLocation == null && state.isLoading) {
                Spacer(modifier = Modifier.height(32.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LoadingOverlay(isLoading = state.isLoading) {
                    Column {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text =
                                if (state.isWithinRange) {
                                    "Your current location matches the HFR ID. You may proceed."
                                } else {
                                    "Your current location is not as per HFR ID. Please review the details " +
                                        "below and update your location."
                                },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Location as per HFR ID",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = HemPrimary,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CoordinateCard(placeLabel = "Delhi, India", point = hospital.hfrLocation)

                        val currentLocation = state.currentLocation
                        if (currentLocation != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "New Coordinates As Your Current Location",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = HemPrimary,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            CoordinateCard(
                                placeLabel = "Delhi, India",
                                point = currentLocation,
                                distanceMeters = state.distanceMeters,
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        VerificationBanner(isWithinRange = state.isWithinRange)

                        Spacer(modifier = Modifier.height(20.dp))
                        if (state.isWithinRange) {
                            Button(
                                onClick = onContinue,
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = HemPrimary),
                            ) {
                                Text("CONTINUE", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            OutlinedButton(
                                onClick = onCancel,
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = HemPrimary),
                            ) {
                                Text("CANCEL", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CoordinateCard(
    placeLabel: String,
    point: GeoPoint,
    distanceMeters: Double? = null,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(width = 1.dp, color = HemPrimary.copy(alpha = 0.25f), shape = RoundedCornerShape(10.dp))
                .padding(12.dp),
    ) {
        Image(
            painter = painterResource(Res.drawable.location),
            contentDescription = null,
            modifier = Modifier.size(26.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = placeLabel,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Latitude", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(text = formatCoordinate(point.latitude, "N"), style = MaterialTheme.typography.bodySmall)
                }
                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(text = "Longitude", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(text = formatCoordinate(point.longitude, "E"), style = MaterialTheme.typography.bodySmall)
                }
            }
            if (distanceMeters != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Distance from HFR coordinates",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                )
                Text(
                    text = "${distanceMeters.roundToInt()}m",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun VerificationBanner(isWithinRange: Boolean) {
    val accent = if (isWithinRange) HemSuccess else HemError
    val uriHandler = LocalUriHandler.current
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(accent.copy(alpha = 0.08f))
                .padding(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        if (isWithinRange) {
            Image(
                painter = painterResource(Res.drawable.success),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        } else {
            Box(
                modifier = Modifier.size(20.dp).clip(CircleShape).background(accent),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text =
                    if (isWithinRange) {
                        "Your current location matches HFR ID"
                    } else {
                        "Your current location is not as per HFR ID"
                    },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = accent,
            )
            if (!isWithinRange) {
                Row {
                    Text(
                        text = "To update location please ",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                    )
                    Text(
                        text = "click here",
                        style = MaterialTheme.typography.bodySmall,
                        color = HemPrimary,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { uriHandler.openUri(NHPR_URL) },
                    )
                }
            }
        }
    }
}

private fun formatCoordinate(
    value: Double,
    suffix: String,
): String {
    val rounded = round(value * 10000) / 10000
    return "$rounded° $suffix"
}

@Preview
@Composable
private fun HospitalLocationVerificationScreenMismatchPreview() {
    HemTheme {
        HospitalLocationVerificationContent(
            hospital =
                Hospital(
                    name = "Shree Ganga Ram Hospital",
                    description = "",
                    phone = "",
                    status = HospitalStatus.EMPANELLED,
                    hfrLocation = GeoPoint(28.6139, 77.2090),
                ),
            state =
                HospitalLocationVerificationUiState(
                    isLoading = false,
                    currentLocation = GeoPoint(28.6157, 77.2090),
                    distanceMeters = 200.0,
                ),
            onBack = {},
            onCancel = {},
            onContinue = {},
            onRetry = {},
        )
    }
}
