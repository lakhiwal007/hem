package org.nha.project.feature.verification.presentation

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import hem.shared.generated.resources.Res
import hem.shared.generated.resources.zoom
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.nha.project.core.ui.components.BrandedHeader
import org.nha.project.core.ui.components.LoadingOverlay
import org.nha.project.core.ui.theme.HemFocusBorder
import org.nha.project.core.ui.theme.HemPrimary
import org.nha.project.core.ui.theme.HemTheme
import org.nha.project.feature.hospital.domain.Hospital
import org.nha.project.feature.hospital.domain.Service
import org.nha.project.feature.hospital.domain.Speciality
import org.nha.project.feature.verification.data.UploadedImage
import org.nha.project.feature.verification.data.VerificationAction

@Composable
fun PhysicalVerifyImagesScreen(
    hospital: Hospital,
    speciality: Speciality,
    service: Service,
    onBack: () -> Unit,
    onSubmitted: () -> Unit,
) {
    val viewModel = koinViewModel<PhysicalVerifyImagesViewModel> { parametersOf(hospital, speciality, service) }
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.submitted) {
        if (state.submitted) onSubmitted()
    }

    PhysicalVerifyImagesContent(
        state = state,
        onBack = onBack,
        onCommentsChange = viewModel::updateComments,
        onActionChange = viewModel::updateAction,
        onSubmit = viewModel::submit,
        onCancel = onBack,
    )
}

@Composable
private fun PhysicalVerifyImagesContent(
    state: PhysicalVerifyImagesUiState,
    onBack: () -> Unit,
    onCommentsChange: (String) -> Unit,
    onActionChange: (VerificationAction) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
) {
    var zoomedImage by remember { mutableStateOf<UploadedImage?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        BrandedHeader(onBack = onBack)

        if (state.images.isEmpty() && state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        LoadingOverlay(isLoading = state.isSubmitting) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .offset(y = (-16).dp)
                        .background(
                            MaterialTheme.colorScheme.background,
                            RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                        ).padding(horizontal = 20.dp)
                        .padding(top = 20.dp, bottom = 24.dp),
            ) {
                Text(
                    text = "Verify & Upload images",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = HemPrimary,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = state.serviceName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Review the uploaded images and approve or reject.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                )

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Images uploaded by Hospital",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = HemPrimary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.images.forEach { image ->
                        UploadedImageThumbnail(image = image, onZoom = { zoomedImage = image })
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Comments",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = HemPrimary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.comments,
                    onValueChange = onCommentsChange,
                    placeholder = { Text("Type here") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(10.dp),
                )

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Action*",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = HemPrimary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                ActionDropdown(selected = state.action, onSelected = onActionChange)

                Spacer(modifier = Modifier.height(28.dp))
                Button(
                    onClick = onSubmit,
                    enabled = state.canSubmit,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HemPrimary),
                ) {
                    Text("SUBMIT", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
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

    val zoomed = zoomedImage
    if (zoomed != null) {
        UploadedImageZoomDialog(image = zoomed, onDismiss = { zoomedImage = null })
    }
}

@Composable
private fun UploadedImageThumbnail(
    image: UploadedImage,
    onZoom: () -> Unit,
) {
    Box(modifier = Modifier.size(96.dp)) {
        Image(
            painter = painterResource(image.drawable),
            contentDescription = image.label,
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Crop,
        )
        Image(
            painter = painterResource(Res.drawable.zoom),
            contentDescription = "Zoom",
            modifier =
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp)
                    .size(20.dp)
                    .clickable(onClick = onZoom),
        )
    }
}

@Composable
private fun UploadedImageZoomDialog(
    image: UploadedImage,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(image.drawable),
                contentDescription = image.label,
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentScale = ContentScale.Fit,
            )
            Text(
                text = image.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier =
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionDropdown(
    selected: VerificationAction,
    onSelected: (VerificationAction) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.label,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            shape = RoundedCornerShape(10.dp),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = HemFocusBorder,
                ),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            VerificationAction.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Preview
@Composable
private fun PhysicalVerifyImagesScreenPreview() {
    HemTheme {
        PhysicalVerifyImagesContent(
            state =
                PhysicalVerifyImagesUiState(
                    serviceName = "Blood Gas And Electrolyte Analysers",
                    images =
                        listOf(
                            UploadedImage("Image 1", Res.drawable.zoom),
                            UploadedImage("Image 2", Res.drawable.zoom),
                            UploadedImage("Image 3", Res.drawable.zoom),
                        ),
                ),
            onBack = {},
            onCommentsChange = {},
            onActionChange = {},
            onSubmit = {},
            onCancel = {},
        )
    }
}
