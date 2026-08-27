package org.nha.project.feature.auth.presentation

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hem.shared.generated.resources.Res
import hem.shared.generated.resources.nha_logo
import hem.shared.generated.resources.onboarding_screen_background
import hem.shared.generated.resources.pmjay_logo
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.nha.project.core.ui.components.Base64Image
import org.nha.project.core.ui.theme.HemPrimary
import org.nha.project.core.ui.theme.HemTheme

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val viewModel = koinViewModel<LoginViewModel>()
    val state by viewModel.uiState.collectAsState()

    if (state.loginSuccess) {
        onLoginSuccess()
        return
    }

    LoginContent(
        state = state,
        onUserIdChange = viewModel::onUserIdChange,
        onCaptcha1Change = viewModel::onCaptcha1Change,
        onOtpChange = viewModel::onOtpChange,
        onCaptcha2Change = viewModel::onCaptcha2Change,
        onAuthModeSelected = viewModel::onAuthModeSelected,
        onRetryCaptcha1 = viewModel::retryCaptcha1,
        onVerifyUserId = viewModel::verifyUserId,
        onSubmitLogin = viewModel::submitLogin,
    )
}

@Composable
private fun LoginContent(
    state: LoginUiState,
    onUserIdChange: (String) -> Unit,
    onCaptcha1Change: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onCaptcha2Change: (String) -> Unit,
    onAuthModeSelected: (String) -> Unit,
    onRetryCaptcha1: () -> Unit,
    onVerifyUserId: () -> Unit,
    onSubmitLogin: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.onboarding_screen_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .padding(top = 48.dp, bottom = 24.dp),
        ) {
            Image(
                painter = painterResource(Res.drawable.nha_logo),
                contentDescription = "National Health Authority",
                modifier = Modifier.align(Alignment.End).height(32.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Image(
                painter = painterResource(Res.drawable.pmjay_logo),
                contentDescription = "PM-JAY",
                modifier =
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(110.dp)
                        .clip(CircleShape)
                        .border(width = 1.dp, color = Color.White, shape = CircleShape),
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Login",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(16.dp))

            CaptchaField(
                label = "Captcha",
                captchaImage = state.captcha1Image,
                input = state.captcha1Input,
                onInputChange = onCaptcha1Change,
                onRefresh = onRetryCaptcha1,
            )
            Spacer(modifier = Modifier.height(16.dp))

            FieldLabel("Registered Mobile Number/User ID")
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = state.userIdInput,
                    onValueChange = onUserIdChange,
                    enabled = !state.isStepTwoVisible,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onVerifyUserId, enabled = !state.isStepTwoVisible) {
                    Text("VERIFY")
                }
            }

            if (state.isStepTwoVisible) {
                Spacer(modifier = Modifier.height(16.dp))
                FieldLabel("Auth Mode")
                AuthModeDropdown(
                    options = state.authModes,
                    selected = state.selectedAuthMode,
                    onSelected = onAuthModeSelected,
                )

                Spacer(modifier = Modifier.height(16.dp))
                FieldLabel("Enter OTP")
                OtpInput(value = state.otpInput, onValueChange = onOtpChange)

                Spacer(modifier = Modifier.height(16.dp))
                CaptchaField(
                    label = "Captcha",
                    captchaImage = state.captcha2Image,
                    input = state.captcha2Input,
                    onInputChange = onCaptcha2Change,
                    onRefresh = null,
                )
            }

            state.errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Don't have an account? Register",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                )
                Button(
                    onClick = onSubmitLogin,
                    enabled = state.isStepTwoVisible && !state.isLoading,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HemPrimary),
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                    } else {
                        Text("LOG IN »", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(text = text, style = MaterialTheme.typography.labelLarge, color = Color.White)
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun CaptchaField(
    label: String,
    captchaImage: String?,
    input: String,
    onInputChange: (String) -> Unit,
    onRefresh: (() -> Unit)?,
) {
    FieldLabel(label)
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (captchaImage != null) {
            Base64Image(base64 = captchaImage, contentDescription = "Captcha", modifier = Modifier.fillMaxSize())
        } else {
            Text(text = "Loading…", style = MaterialTheme.typography.titleMedium)
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = input,
            onValueChange = onInputChange,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
        )
        if (onRefresh != null) {
            IconButton(onClick = onRefresh) {
                Text("⟳", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthModeDropdown(
    options: List<String>,
    selected: String?,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.orEmpty(),
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun OtpInput(
    value: String,
    onValueChange: (String) -> Unit,
    length: Int = 6,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(length) { index ->
            val char = value.getOrNull(index)?.toString() ?: ""
            Box(
                modifier =
                    Modifier
                        .size(44.dp)
                        .background(Color.White, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = char, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
    OutlinedTextField(
        value = value,
        onValueChange = { new -> if (new.length <= length && new.all(Char::isDigit)) onValueChange(new) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        shape = RoundedCornerShape(12.dp),
    )
}

@Preview
@Composable
private fun LoginScreenPreview() {
    HemTheme {
        LoginContent(
            state = LoginUiState(captcha1Image = "preview"),
            onUserIdChange = {},
            onCaptcha1Change = {},
            onOtpChange = {},
            onCaptcha2Change = {},
            onAuthModeSelected = {},
            onRetryCaptcha1 = {},
            onVerifyUserId = {},
            onSubmitLogin = {},
        )
    }
}
