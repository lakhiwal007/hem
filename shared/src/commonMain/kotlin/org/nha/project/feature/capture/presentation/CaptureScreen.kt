package org.nha.project.feature.capture.presentation

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import hem.shared.generated.resources.Res
import hem.shared.generated.resources.add_more
import hem.shared.generated.resources.capture
import hem.shared.generated.resources.guidelines
import hem.shared.generated.resources.retake
import hem.shared.generated.resources.success
import hem.shared.generated.resources.zoom
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.nha.project.core.capture.rememberCameraCapture
import org.nha.project.core.ui.components.Base64Image
import org.nha.project.core.ui.components.LoadingOverlay
import org.nha.project.core.ui.theme.HemPrimary
import org.nha.project.core.ui.theme.HemSuccess
import org.nha.project.core.ui.theme.HemTheme
import org.nha.project.core.ui.theme.HemWarning
import org.nha.project.feature.capture.domain.CapturedImage
import org.nha.project.feature.hospital.domain.Hospital
import org.nha.project.feature.hospital.domain.Service
import org.nha.project.feature.hospital.domain.Speciality

private val GUIDELINES =
    listOf(
        "Capture real-time image using your camera.",
        "Ensure the equipment is clearly visible.",
        "Avoid blurry or dark images.",
        "Only capture allowed",
    )

@Composable
fun CaptureAccordionContent(
    hospital: Hospital,
    speciality: Speciality,
    service: Service,
    onSubmitted: () -> Unit,
) {
    val viewModel =
        koinViewModel<CaptureViewModel>(key = "capture-${service.id}") {
            parametersOf(hospital, speciality, service)
        }
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.submitted) {
        if (state.submitted) onSubmitted()
    }

    var pendingRetakeIndex by remember { mutableStateOf<Int?>(null) }
    var pendingEdit by remember { mutableStateOf<PendingEditImage?>(null) }
    val captureImage =
        rememberCameraCapture(onResult = { base64 ->
            if (base64 != null) {
                pendingEdit = PendingEditImage(base64 = base64, retakeIndex = pendingRetakeIndex)
            }
            pendingRetakeIndex = null
        })

    var zoomedImage by remember { mutableStateOf<CapturedImage?>(null) }

    CaptureContent(
        state = state,
        onCapture = {
            pendingRetakeIndex = null
            captureImage()
        },
        onRetake = { index ->
            pendingRetakeIndex = index
            captureImage()
        },
        onZoom = { image -> zoomedImage = image },
        onSubmit = viewModel::submit,
    )

    val zoomed = zoomedImage
    if (zoomed != null) {
        ImagePreviewDialog(image = zoomed, onDismiss = { zoomedImage = null })
    }

    val edit = pendingEdit
    if (edit != null) {
        ImageEditDialog(
            base64 = edit.base64,
            onConfirm = { finalBase64 ->
                val index = edit.retakeIndex
                if (index != null) viewModel.retakeImage(index, finalBase64) else viewModel.addImage(finalBase64)
                pendingEdit = null
            },
            onCancel = { pendingEdit = null },
        )
    }
}

private data class PendingEditImage(
    val base64: String,
    val retakeIndex: Int?,
)

@Composable
private fun CaptureContent(
    state: CaptureUiState,
    onCapture: () -> Unit,
    onRetake: (Int) -> Unit,
    onZoom: (CapturedImage) -> Unit,
    onSubmit: () -> Unit,
) {
    LoadingOverlay(
        isLoading = state.isLoading || state.isSubmitting,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Text(
                text = "Upload clear images of ${state.serviceName} for verification.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
            )

            Spacer(modifier = Modifier.height(16.dp))
            GuidelinesCard()

            if (state.images.isEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Capture Image",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = HemPrimary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                CaptureDropZone(onClick = onCapture)
            } else {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Uploaded Images (${state.images.size}/${state.maxImages})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = HemPrimary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRowImages(
                    images = state.images,
                    canAddMore = state.canAddMore,
                    onRetake = onRetake,
                    onZoom = onZoom,
                    onCapture = onCapture,
                )
                Spacer(modifier = Modifier.height(8.dp))
                UploadProgressStatus(state = state)
            }

            Spacer(modifier = Modifier.height(16.dp))
            NoteCard()

            if (state.canSubmit) {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onSubmit,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HemPrimary),
                ) {
                    Text("SUBMIT", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun FlowRowImages(
    images: List<CapturedImage>,
    canAddMore: Boolean,
    onRetake: (Int) -> Unit,
    onZoom: (CapturedImage) -> Unit,
    onCapture: () -> Unit,
) {
    val itemsPerRow = 3
    val rows = (images.size + (if (canAddMore) 1 else 0) + itemsPerRow - 1) / itemsPerRow
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(rows) { rowIndex ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (column in 0 until itemsPerRow) {
                    val index = rowIndex * itemsPerRow + column
                    when {
                        index < images.size ->
                            CapturedThumbnail(
                                image = images[index],
                                onRetake = { onRetake(index) },
                                onZoom = { onZoom(images[index]) },
                            )
                        index == images.size && canAddMore -> AddMoreTile(onClick = onCapture)
                    }
                }
            }
        }
    }
}

@Composable
private fun GuidelinesCard() {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(HemPrimary.copy(alpha = 0.06f))
                .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(Res.drawable.guidelines),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Guidelines",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = HemPrimary,
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Column(modifier = Modifier.padding(start = 26.dp)) {
            GUIDELINES.forEach { line ->
                Text(
                    text = "• $line",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray,
                )
            }
        }
    }
}

@Composable
private fun CaptureDropZone(onClick: () -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .dashedBorder(color = HemPrimary.copy(alpha = 0.4f), cornerRadius = 12.dp)
                .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.capture),
            contentDescription = null,
            modifier = Modifier.size(56.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap to capture image",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.DarkGray,
        )
    }
}

@Composable
private fun CapturedThumbnail(
    image: CapturedImage,
    onRetake: () -> Unit,
    onZoom: () -> Unit,
) {
    Box(modifier = Modifier.size(96.dp)) {
        val base64 = image.base64
        if (base64 != null) {
            Base64Image(
                base64 = base64,
                contentDescription = "Captured image",
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp))
                        .background(HemPrimary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.capture),
                    contentDescription = "Previously uploaded image",
                    modifier = Modifier.size(36.dp),
                )
            }
        }
        Image(
            painter = painterResource(Res.drawable.success),
            contentDescription = "Uploaded",
            modifier = Modifier.align(Alignment.TopStart).padding(4.dp).size(18.dp),
        )
        Image(
            painter = painterResource(Res.drawable.retake),
            contentDescription = "Retake",
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(28.dp)
                    .clickable(onClick = onRetake),
        )
        if (base64 != null) {
            Image(
                painter = painterResource(Res.drawable.zoom),
                contentDescription = "Zoom",
                modifier =
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .size(32.dp)
                        .clickable(onClick = onZoom),
            )
        }
    }
}

@Composable
private fun AddMoreTile(onClick: () -> Unit) {
    Column(
        modifier =
            Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(10.dp))
                .dashedBorder(color = HemPrimary.copy(alpha = 0.4f), cornerRadius = 10.dp)
                .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.add_more),
            contentDescription = "Add more",
            modifier = Modifier.size(28.dp),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Add more", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
private fun UploadProgressStatus(state: CaptureUiState) {
    val remaining = state.remainingRequiredCount
    val message =
        when {
            state.finalSubmitAllowed -> "All required images uploaded. You can now submit."
            remaining != null && remaining > 0 ->
                "Upload $remaining more ${if (remaining == 1) "image" else "images"} to enable submit."
            else -> null
        }
    if (message != null) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = if (state.finalSubmitAllowed) HemSuccess else HemWarning,
        )
    }
}

@Composable
private fun NoteCard() {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF2F2F2))
                .padding(12.dp),
    ) {
        Row {
            Text(
                text = "Note",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray,
            )
            Text(
                text = ": You can only capture new images. Upload from gallery is not allowed.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
            )
        }
    }
}

@Composable
private fun ImagePreviewDialog(
    image: CapturedImage,
    onDismiss: () -> Unit,
) {
    val base64 = image.base64 ?: return
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
            Base64Image(
                base64 = base64,
                contentDescription = "Captured image preview",
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentScale = ContentScale.Fit,
            )
            ImageMetadataOverlay(
                image = image,
                modifier = Modifier.align(Alignment.BottomStart).padding(24.dp),
            )
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(20.dp)
                        .size(36.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "✕", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ImageMetadataOverlay(
    image: CapturedImage,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(
            text = image.fileName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}

private fun Modifier.dashedBorder(
    color: Color,
    cornerRadius: Dp,
    strokeWidth: Dp = 1.dp,
    dashLength: Dp = 6.dp,
    gapLength: Dp = 4.dp,
): Modifier =
    drawBehind {
        val stroke =
            Stroke(
                width = strokeWidth.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength.toPx(), gapLength.toPx()), 0f),
            )
        drawRoundRect(
            color = color,
            style = stroke,
            cornerRadius = CornerRadius(cornerRadius.toPx()),
            size = Size(size.width - stroke.width, size.height - stroke.width),
            topLeft =
                androidx.compose.ui.geometry
                    .Offset(stroke.width / 2, stroke.width / 2),
        )
    }

@Preview
@Composable
private fun CaptureContentPreview() {
    HemTheme {
        CaptureContent(
            state = CaptureUiState(serviceName = "Blood Gas And Electrolyte Analysers"),
            onCapture = {},
            onRetake = {},
            onZoom = {},
            onSubmit = {},
        )
    }
}
