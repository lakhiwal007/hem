package org.nha.project.feature.verification.data

import hem.shared.generated.resources.Res
import hem.shared.generated.resources.capture
import org.jetbrains.compose.resources.DrawableResource

enum class VerificationAction(
    val label: String,
) {
    RECOMMENDED("Recommended"),
    NOT_RECOMMENDED("Not Recommended"),
}

data class UploadedImage(
    val label: String,
    val drawable: DrawableResource = Res.drawable.capture,
)
