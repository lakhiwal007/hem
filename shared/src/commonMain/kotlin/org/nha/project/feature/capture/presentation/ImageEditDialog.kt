package org.nha.project.feature.capture.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.nha.project.core.capture.ImageEditor
import org.nha.project.core.capture.NormalizedCropRect
import org.nha.project.core.ui.components.decodeBase64Image
import org.nha.project.core.ui.theme.HemPrimary

private enum class DragHandle {
    MOVE,
    TOP,
    BOTTOM,
    LEFT,
    RIGHT,
}

/**
 * Full-screen crop/rotate editor shown right after a photo is captured and before it's stamped
 * with metadata and uploaded. [onConfirm] receives the edited (or unedited, if untouched)
 * base64 JPEG; [onCancel] discards the edit entirely (the photo is not uploaded).
 */
@Composable
fun ImageEditDialog(
    base64: String,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit,
    imageEditor: ImageEditor = koinInject(),
) {
    var currentBase64 by remember(base64) { mutableStateOf(base64) }
    val bitmap = remember(currentBase64) { decodeBase64Image(currentBase64) }
    var cropRect by remember(currentBase64) { mutableStateOf<Rect?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            val density = LocalDensity.current
            val topReservePx = with(density) { 96.dp.toPx() }
            val bottomReservePx = with(density) { 120.dp.toPx() }
            val sidePaddingPx = with(density) { 16.dp.toPx() }
            val availableWidthPx = with(density) { maxWidth.toPx() } - sidePaddingPx * 2
            val availableHeightPx =
                (with(density) { maxHeight.toPx() } - topReservePx - bottomReservePx)
                    .coerceAtLeast(1f)

            if (bitmap != null) {
                val aspect = bitmap.width.toFloat() / bitmap.height.toFloat()
                val (dispWidthPx, dispHeightPx) =
                    if (availableWidthPx / availableHeightPx > aspect) {
                        (availableHeightPx * aspect) to availableHeightPx
                    } else {
                        availableWidthPx to (availableWidthPx / aspect)
                    }
                val effectiveRect = cropRect ?: Rect(0f, 0f, dispWidthPx, dispHeightPx)

                Box(
                    modifier =
                        Modifier
                            .size(with(density) { dispWidthPx.toDp() }, with(density) { dispHeightPx.toDp() })
                            .align(Alignment.Center),
                ) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Photo being edited",
                        modifier = Modifier.fillMaxSize(),
                    )
                    CropOverlay(
                        imageSize = Size(dispWidthPx, dispHeightPx),
                        cropRect = effectiveRect,
                        onCropRectChange = { cropRect = it },
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                Row(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = { cropRect = null },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    ) {
                        Text("RESET")
                    }
                    Button(
                        onClick = {
                            val rect = effectiveRect
                            scope.launch {
                                isProcessing = true
                                val normalized =
                                    NormalizedCropRect(
                                        left = (rect.left / dispWidthPx).coerceIn(0f, 1f),
                                        top = (rect.top / dispHeightPx).coerceIn(0f, 1f),
                                        right = (rect.right / dispWidthPx).coerceIn(0f, 1f),
                                        bottom = (rect.bottom / dispHeightPx).coerceIn(0f, 1f),
                                    )
                                val finalBase64 =
                                    if (normalized.isFullImage) {
                                        currentBase64
                                    } else {
                                        imageEditor.crop(currentBase64, normalized)
                                    }
                                isProcessing = false
                                onConfirm(finalBase64)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = HemPrimary),
                    ) {
                        Text("DONE", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
            }

            Text(
                text = "Drag an edge handle to resize, or the middle to move the crop area.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f),
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(16.dp),
            )

            Row(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                EditToolButton(
                    label = "⟳",
                    onClick = {
                        scope.launch {
                            isProcessing = true
                            currentBase64 = imageEditor.rotate90(currentBase64)
                            isProcessing = false
                        }
                    },
                )
                EditToolButton(label = "✕", onClick = onCancel)
            }

            if (isProcessing) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun EditToolButton(
    label: String,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Composable
private fun CropOverlay(
    imageSize: Size,
    cropRect: Rect,
    onCropRectChange: (Rect) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val handleTouchRadiusPx = with(density) { 32.dp.toPx() }
    val minCropSizePx = with(density) { 64.dp.toPx() }
    val barLengthPx = with(density) { 48.dp.toPx() }
    val barThicknessPx = with(density) { 8.dp.toPx() }
    // detectDragGestures runs inside this pointerInput block, which is only ever restarted when
    // imageSize changes (i.e. after a rotate) - a plain closure over the cropRect parameter would
    // go stale after the first recomposition and keep computing deltas against the original rect,
    // which is what caused the crop box to jitter/snap back while dragging. rememberUpdatedState
    // keeps a live reference the gesture can read at each drag start, and accumulating the drag
    // locally (dragStartRect + total offset since press) makes each frame's move self-consistent
    // instead of depending on the (possibly stale) rect from the previous frame.
    val latestCropRect = rememberUpdatedState(cropRect)
    var dragHandle by remember { mutableStateOf<DragHandle?>(null) }

    Canvas(
        modifier =
            modifier.pointerInput(imageSize) {
                var dragStartRect = Rect.Zero
                var accumulatedDrag = Offset.Zero
                detectDragGestures(
                    onDragStart = { offset ->
                        val rect = latestCropRect.value
                        dragHandle = detectDragHandle(offset, rect, handleTouchRadiusPx)
                        dragStartRect = rect
                        accumulatedDrag = Offset.Zero
                    },
                    onDragEnd = { dragHandle = null },
                    onDragCancel = { dragHandle = null },
                ) { change, dragAmount ->
                    change.consume()
                    val handle = dragHandle ?: return@detectDragGestures
                    accumulatedDrag += dragAmount
                    onCropRectChange(applyDrag(dragStartRect, handle, accumulatedDrag, imageSize, minCropSizePx))
                }
            },
    ) {
        val maskPath =
            Path().apply {
                addRect(Rect(Offset.Zero, imageSize))
                addRect(cropRect)
                fillType = PathFillType.EvenOdd
            }
        drawPath(maskPath, color = Color.Black.copy(alpha = 0.55f))
        drawRect(
            color = Color.White,
            topLeft = cropRect.topLeft,
            size = cropRect.size,
            style = Stroke(width = 2.dp.toPx()),
        )

        val midX = (cropRect.left + cropRect.right) / 2f
        val midY = (cropRect.top + cropRect.bottom) / 2f
        val cornerRadius = CornerRadius(barThicknessPx / 2)

        // Top/bottom handles resize height only; left/right handles resize width only - easier
        // to grab and predict than a single diagonal corner drag.
        listOf(cropRect.top, cropRect.bottom).forEach { y ->
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(midX - barLengthPx / 2, y - barThicknessPx / 2),
                size = Size(barLengthPx, barThicknessPx),
                cornerRadius = cornerRadius,
            )
        }
        listOf(cropRect.left, cropRect.right).forEach { x ->
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(x - barThicknessPx / 2, midY - barLengthPx / 2),
                size = Size(barThicknessPx, barLengthPx),
                cornerRadius = cornerRadius,
            )
        }
    }
}

private fun detectDragHandle(
    offset: Offset,
    rect: Rect,
    handleTouchRadius: Float,
): DragHandle? {
    fun hitBox(center: Offset) =
        Rect(
            center.x - handleTouchRadius,
            center.y - handleTouchRadius,
            center.x + handleTouchRadius,
            center.y + handleTouchRadius,
        )
    val midX = (rect.left + rect.right) / 2f
    val midY = (rect.top + rect.bottom) / 2f
    return when {
        hitBox(Offset(midX, rect.top)).contains(offset) -> DragHandle.TOP
        hitBox(Offset(midX, rect.bottom)).contains(offset) -> DragHandle.BOTTOM
        hitBox(Offset(rect.left, midY)).contains(offset) -> DragHandle.LEFT
        hitBox(Offset(rect.right, midY)).contains(offset) -> DragHandle.RIGHT
        rect.contains(offset) -> DragHandle.MOVE
        else -> null
    }
}

private fun applyDrag(
    rect: Rect,
    handle: DragHandle,
    drag: Offset,
    bounds: Size,
    minSize: Float,
): Rect =
    when (handle) {
        DragHandle.MOVE -> {
            val newLeft = (rect.left + drag.x).coerceIn(0f, bounds.width - rect.width)
            val newTop = (rect.top + drag.y).coerceIn(0f, bounds.height - rect.height)
            Rect(newLeft, newTop, newLeft + rect.width, newTop + rect.height)
        }
        DragHandle.TOP -> {
            val newTop = (rect.top + drag.y).coerceIn(0f, rect.bottom - minSize)
            Rect(rect.left, newTop, rect.right, rect.bottom)
        }
        DragHandle.BOTTOM -> {
            val newBottom = (rect.bottom + drag.y).coerceIn(rect.top + minSize, bounds.height)
            Rect(rect.left, rect.top, rect.right, newBottom)
        }
        DragHandle.LEFT -> {
            val newLeft = (rect.left + drag.x).coerceIn(0f, rect.right - minSize)
            Rect(newLeft, rect.top, rect.right, rect.bottom)
        }
        DragHandle.RIGHT -> {
            val newRight = (rect.right + drag.x).coerceIn(rect.left + minSize, bounds.width)
            Rect(rect.left, rect.top, newRight, rect.bottom)
        }
    }
