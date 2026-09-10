package org.nha.project.feature.auth.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hem.shared.generated.resources.Res
import hem.shared.generated.resources.nha_logo
import hem.shared.generated.resources.onboarding_screen_background
import hem.shared.generated.resources.pmjay_logo
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.nha.project.core.ui.components.Base64Image
import org.nha.project.core.ui.components.LoadingOverlay
import org.nha.project.core.ui.components.OtpInputFields
import org.nha.project.core.ui.theme.HemError
import org.nha.project.core.ui.theme.HemFocusBorder
import org.nha.project.core.ui.theme.HemPrimary
import org.nha.project.core.ui.theme.HemTheme

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val viewModel = koinViewModel<LoginViewModel>()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.loginSuccessEvents.collect { onLoginSuccess() }
    }

    LaunchedEffect(Unit) {
        viewModel.retryCaptcha1()
    }

    LoginContent(
        state = state,
        onUserIdChange = viewModel::onUserIdChange,
        onCaptcha1Change = viewModel::onCaptcha1Change,
        onOtpChange = viewModel::onOtpChange,
        onCaptcha2Change = viewModel::onCaptcha2Change,
        onAuthModeSelected = viewModel::onAuthModeSelected,
        onRetryCaptcha1 = viewModel::retryCaptcha1,
        onResendCaptcha2 = viewModel::resendCaptcha2,
        onVerifyUserId = viewModel::verifyUserId,
        onSubmitLogin = viewModel::submitLogin,
    )

    if (state.showAlreadyLoggedInSheet) {
        AlreadyLoggedInSheet(
            onLogoutAllSessions = viewModel::logoutAllSessionsAndRetry,
            onDismiss = viewModel::dismissAlreadyLoggedInSheet,
        )
    }
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
    onResendCaptcha2: () -> Unit,
    onVerifyUserId: () -> Unit,
    onSubmitLogin: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    LoadingOverlay(
        isLoading = state.isLoading,
        modifier = Modifier.fillMaxSize(),
    ) {
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
                    .verticalScroll(state = ScrollState(initial = 0))
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
            OutlinedTextField(
                value = state.userIdInput,
                onValueChange = onUserIdChange,
                enabled = !state.isStepTwoVisible,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    TextButton(
                        onClick = onVerifyUserId,
                        enabled = !state.isStepTwoVisible,
                        colors =
                            ButtonDefaults.textButtonColors(
                                contentColor = HemFocusBorder,
                                disabledContentColor = HemFocusBorder.copy(alpha = 0.38f),
                            ),
                    ) {
                        Text("VERIFY", fontWeight = FontWeight.Bold)
                    }
                },
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = HemFocusBorder,
                        unfocusedBorderColor = Color.Transparent,
                        disabledContainerColor = Color.White,
                    ),
            )

            if (state.isStepTwoVisible) {
                Spacer(modifier = Modifier.height(16.dp))
                FieldLabel("Auth Mode")
                AuthModeDropdown(
                    options = state.authModes,
                    selected = state.selectedAuthMode,
                    onSelected = onAuthModeSelected,
                )

                Spacer(modifier = Modifier.height(16.dp))
                val isPasswordMode = state.selectedAuthMode == "Password"
                FieldLabel(if (isPasswordMode) "Enter Password" else "Enter OTP")
                if (isPasswordMode) {
                    PasswordInput(value = state.otpInput, onValueChange = onOtpChange)
                } else {
                    OtpInputFields(value = state.otpInput, onValueChange = onOtpChange)
                }

                if (!state.initMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.initMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                CaptchaField(
                    label = "Captcha",
                    captchaImage = state.captcha2Image,
                    input = state.captcha2Input,
                    onInputChange = onCaptcha2Change,
                    onRefresh = onResendCaptcha2,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Don't have an account? ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                    )
                    Text(
                        text = "Register",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                        modifier =
                            Modifier.clickable {
                                uriHandler.openUri("https://ump.pmjay.gov.in/signup")
                            },
                    )
                }
                Button(
                    onClick = onSubmitLogin,
                    enabled = state.isStepTwoVisible && !state.isLoading,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.White),
                    colors = ButtonDefaults.buttonColors(containerColor = HemPrimary, contentColor = Color.White),
                ) {
                    Text("LOG IN »", color = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlreadyLoggedInSheet(
    onLogoutAllSessions: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    fun dismissThen(action: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { action() }
    }

    ModalBottomSheet(
        onDismissRequest = { dismissThen(onDismiss) },
        sheetState = sheetState,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
            Text(
                text = "Already logged in",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text =
                    "You have an active session in the application. Do you want to logout the currently logged-in session and re-login with a new session?",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { dismissThen(onDismiss) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = { dismissThen(onLogoutAllSessions) },
                    colors = ButtonDefaults.buttonColors(containerColor = HemError),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Logout")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
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
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp))
                .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (captchaImage != null) {
            Base64Image(
                base64 = captchaImage,
                contentDescription = "Captcha",
                modifier = Modifier.fillMaxWidth().height(50.dp),
            )
        } else {
            Box(modifier = Modifier.fillMaxWidth().height(50.dp), contentAlignment = Alignment.Center) {
                Text(text = "Loading…", style = MaterialTheme.typography.titleMedium)
            }
        }
        HorizontalDivider(thickness = 1.dp, color = Color.DarkGray)

        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier.weight(0.5f),
                shape = RoundedCornerShape(12.dp),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent,
                        errorBorderColor = Color.Transparent,
                    ),
            )
            VerticalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = Color.DarkGray,
            )
            if (onRefresh != null) {
                IconButton(onClick = onRefresh) {
                    Text("⟳", style = MaterialTheme.typography.titleLarge)
                }
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
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            shape = RoundedCornerShape(12.dp),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = HemFocusBorder,
                ),
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
private fun PasswordInput(
    value: String,
    onValueChange: (String) -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        trailingIcon = {
            TextButton(
                onClick = { visible = !visible },
                colors = ButtonDefaults.textButtonColors(contentColor = HemFocusBorder),
            ) {
                Text(if (visible) "HIDE" else "SHOW", fontWeight = FontWeight.Bold)
            }
        },
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = HemFocusBorder,
            ),
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
            onResendCaptcha2 = {},
            onVerifyUserId = {},
            onSubmitLogin = {},
        )
    }
}
