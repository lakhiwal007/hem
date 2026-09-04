package org.nha.project.feature.verification.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import hem.shared.generated.resources.Res
import hem.shared.generated.resources.alert
import hem.shared.generated.resources.success
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.nha.project.core.location.GeoPoint
import org.nha.project.core.ui.components.BrandedHeader
import org.nha.project.core.ui.components.LoadingOverlay
import org.nha.project.core.ui.theme.HemPrimary
import org.nha.project.core.ui.theme.HemTheme
import org.nha.project.feature.hospital.domain.Hospital
import org.nha.project.feature.hospital.domain.HospitalStatus

@Composable
fun HospitalOtpVerificationScreen(
    hospital: Hospital,
    onBack: () -> Unit,
    onVerified: () -> Unit,
) {
    val viewModel = koinViewModel<HospitalOtpVerificationViewModel> { parametersOf(hospital) }
    val state by viewModel.uiState.collectAsState()

    HospitalOtpVerificationContent(
        hospital = hospital,
        state = state,
        onBack = onBack,
        onOtpChange = viewModel::updateOtp,
        onSubmit = viewModel::submitOtp,
        onDismissResult = viewModel::dismissResult,
        onVerified = onVerified,
    )
}

@Composable
private fun HospitalOtpVerificationContent(
    hospital: Hospital,
    state: HospitalOtpVerificationUiState,
    onBack: () -> Unit,
    onOtpChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onDismissResult: () -> Unit,
    onVerified: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        BrandedHeader(onBack = onBack)

        LoadingOverlay(isLoading = state.isSendingOtp) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .offset(y = (-16).dp)
                        .background(
                            MaterialTheme.colorScheme.background,
                            RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                        ).padding(horizontal = 20.dp)
                        .padding(top = 20.dp, bottom = 24.dp),
            ) {
                Text(
                    text = "To start physical verification enter the OTP provided by the hospital",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Request Hospital Admin/Facility Manager For The OTP",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                )

                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Enter 6 digit OTP",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = HemPrimary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OtpBoxInput(value = state.otp, onValueChange = onOtpChange)

                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onSubmit,
                    enabled = state.canSubmit,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HemPrimary),
                ) {
                    Text("INITIATE VERIFICATION", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    when (state.result) {
        OtpVerificationResult.SUCCESS ->
            OtpResultDialog(
                icon = Res.drawable.success,
                message = "OTP verified successfully.",
                buttonLabel = "OK",
                onDismiss = {
                    onDismissResult()
                    onVerified()
                },
            )
        OtpVerificationResult.FAILURE ->
            OtpResultDialog(
                icon = Res.drawable.alert,
                message = "The OTP you entered is invalid. Please enter a valid 6 digit OTP.",
                buttonLabel = "TRY AGAIN",
                onDismiss = onDismissResult,
            )
        null -> Unit
    }
}

@Composable
private fun OtpBoxInput(
    value: String,
    onValueChange: (String) -> Unit,
    length: Int = OTP_LENGTH,
) {
    Box(contentAlignment = Alignment.CenterStart) {
        BasicTextField(
            value = value,
            onValueChange = { new -> if (new.length <= length && new.all(Char::isDigit)) onValueChange(new) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            textStyle = TextStyle(color = Color.Transparent),
            cursorBrush = SolidColor(Color.Transparent),
            modifier = Modifier.fillMaxWidth().height(48.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(length) { index ->
                val char = value.getOrNull(index)?.toString()
                Box(
                    modifier =
                        Modifier
                            .size(44.dp)
                            .border(
                                width = 1.dp,
                                color = HemPrimary.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(8.dp),
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = char ?: "*",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (char != null) Color.DarkGray else Color.LightGray,
                    )
                }
            }
        }
    }
}

@Composable
private fun OtpResultDialog(
    icon: DrawableResource,
    message: String,
    buttonLabel: String,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(72.dp),
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HemPrimary),
            ) {
                Text(buttonLabel, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview
@Composable
private fun HospitalOtpVerificationScreenPreview() {
    HemTheme {
        HospitalOtpVerificationContent(
            hospital =
                Hospital(
                    name = "Shree Ganga Ram Hospital",
                    description = "",
                    phone = "",
                    status = HospitalStatus.EMPANELLED,
                    hfrLocation = GeoPoint(28.6139, 77.2090),
                ),
            state = HospitalOtpVerificationUiState(otp = "123"),
            onBack = {},
            onOtpChange = {},
            onSubmit = {},
            onDismissResult = {},
            onVerified = {},
        )
    }
}
