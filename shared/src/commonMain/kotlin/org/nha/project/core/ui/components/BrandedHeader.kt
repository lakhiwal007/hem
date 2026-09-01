package org.nha.project.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import hem.shared.generated.resources.Res
import hem.shared.generated.resources.back_arrow
import hem.shared.generated.resources.hero_banner
import hem.shared.generated.resources.nha_logo
import hem.shared.generated.resources.pmjay_logo
import org.jetbrains.compose.resources.painterResource

@Composable
fun BrandedHeader(
    onBack: (() -> Unit)? = null,
    height: Dp = 200.dp,
) {
    Box(modifier = Modifier.fillMaxWidth().height(height)) {
        Image(
            painter = painterResource(Res.drawable.hero_banner),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        if (onBack != null) {
            Image(
                painter = painterResource(Res.drawable.back_arrow),
                contentDescription = "Back",
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
                        .padding(8.dp)
                        .clickable(onClick = onBack),
            )
        }
        Image(
            painter = painterResource(Res.drawable.nha_logo),
            contentDescription = "National Health Authority",
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(16.dp)
                    .height(32.dp),
        )
        Image(
            painter = painterResource(Res.drawable.pmjay_logo),
            contentDescription = "PM-JAY",
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(width = 1.dp, color = Color.White, shape = CircleShape)
                    .padding(6.dp),
        )
    }
}
