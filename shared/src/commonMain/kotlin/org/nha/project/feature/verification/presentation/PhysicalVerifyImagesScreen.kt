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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import org.nha.project.core.ui.components.Base64Image
import org.nha.project.core.ui.components.LoadingOverlay
import org.nha.project.core.ui.theme.HemError
import org.nha.project.core.ui.theme.HemFocusBorder
import org.nha.project.core.ui.theme.HemPrimary
import org.nha.project.core.ui.theme.HemSuccess
import org.nha.project.core.ui.theme.HemTheme
import org.nha.project.feature.hospital.domain.Hospital
import org.nha.project.feature.hospital.domain.Service
import org.nha.project.feature.hospital.domain.Speciality
import org.nha.project.feature.verification.data.UploadedImage
import org.nha.project.feature.verification.data.VerificationAction

@Composable
fun VerifyAccordionContent(
    hospital: Hospital,
    speciality: Speciality,
    service: Service,
    onSubmitted: () -> Unit,
) {
    val viewModel =
        koinViewModel<PhysicalVerifyImagesViewModel>(key = "verify-${service.id}") {
            parametersOf(hospital, speciality, service)
        }
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.submitted) {
        if (state.submitted) onSubmitted()
    }

    var zoomedImage by remember { mutableStateOf<UploadedImage?>(null) }

    if (state.images.isEmpty() && state.isLoading) {
        Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LoadingOverlay(isLoading = state.isSubmitting) {
        Column {
            Text(
                text = "Review the uploaded images and approve or reject each one.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
            )

            if (state.isReadOnly) {
                Spacer(modifier = Modifier.height(12.dp))
                VerificationDecidedBanner(action = state.decidedAction, comments = state.decidedComments)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                state.images.forEachIndexed { index, item ->
                    VerifiableImageRow(
                        item = item,
                        enabled = !state.isReadOnly,
                        onZoom = { zoomedImage = item.image },
                        onActionChange = { viewModel.updateImageAction(index, it) },
                        onCommentChange = { viewModel.updateImageComment(index, it) },
                    )
                }
            }

            if (!state.isReadOnly) {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = viewModel::submit,
                    enabled = state.canSubmit,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HemPrimary),
                ) {
                    Text("FINAL SUBMIT", fontWeight = FontWeight.Bold)
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
private fun VerifiableImageRow(
    item: ImageVerification,
    enabled: Boolean,
    onZoom: () -> Unit,
    onActionChange: (VerificationAction) -> Unit,
    onCommentChange: (String) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        UploadedImageThumbnail(image = item.image, onZoom = onZoom)
        Column(modifier = Modifier.weight(1f)) {
            ActionDropdown(selected = item.action, enabled = enabled, onSelected = onActionChange)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = item.comment,
                onValueChange = onCommentChange,
                enabled = enabled,
                placeholder = { Text("Comment") },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(10.dp),
            )
        }
    }
}

@Composable
private fun UploadedImageThumbnail(
    image: UploadedImage,
    onZoom: () -> Unit,
) {
    Box(modifier = Modifier.size(96.dp)) {
        val base64 = image.base64
        if (base64 != null) {
            Base64Image(
                base64 = base64,
                contentDescription = image.label,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop,
            )
        } else {
            Image(
                painter = painterResource(image.drawable),
                contentDescription = image.label,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop,
            )
        }
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
            val base64 = image.base64
            if (base64 != null) {
                Base64Image(
                    base64 = base64,
                    contentDescription = image.label,
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Image(
                    painter = painterResource(image.drawable),
                    contentDescription = image.label,
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentScale = ContentScale.Fit,
                )
            }
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
    selected: VerificationAction?,
    onSelected: (VerificationAction) -> Unit,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = it },
    ) {
        OutlinedTextField(
            value = selected?.label.orEmpty(),
            onValueChange = {},
            placeholder = { Text("Select action") },
            readOnly = true,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            shape = RoundedCornerShape(10.dp),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled) },
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = HemFocusBorder,
                ),
        )
        DropdownMenu(expanded = expanded && enabled, onDismissRequest = { expanded = false }) {
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

@Composable
private fun VerificationDecidedBanner(
    action: VerificationAction?,
    comments: String?,
) {
    val (label, color) =
        when (action) {
            VerificationAction.RECOMMENDED -> "Already approved" to HemSuccess
            VerificationAction.NOT_RECOMMENDED -> "Already rejected" to HemError
            null -> "Already verified" to HemFocusBorder
        }
    Column {
        Box(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(50))
                    .background(color.copy(alpha = 0.1f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = color,
            )
        }
        if (!comments.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = comments,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
            )
        }
    }
}

@Preview
@Composable
private fun VerifyAccordionContentPreview() {
    HemTheme {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            VerifiableImageRow(
                item = ImageVerification(image = UploadedImage("Image 1", Res.drawable.zoom)),
                enabled = true,
                onZoom = {},
                onActionChange = {},
                onCommentChange = {},
            )
        }
    }
}
