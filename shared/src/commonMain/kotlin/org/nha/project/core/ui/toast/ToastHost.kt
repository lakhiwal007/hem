package org.nha.project.core.ui.toast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.nha.project.core.ui.theme.HemError
import org.nha.project.core.ui.theme.HemInfo
import org.nha.project.core.ui.theme.HemSuccess
import org.nha.project.core.ui.theme.HemWarning

@Composable
fun ToastHost(
    controller: ToastController,
    modifier: Modifier = Modifier,
) {
    val toasts by controller.toasts.collectAsState()

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        toasts.forEach { toast ->
            key(toast.id) {
                ToastCard(toast = toast, onDismiss = { controller.dismiss(toast.id) })
            }
        }
    }
}

private data class ToastStyle(
    val accent: Color,
    val glyph: String,
)

private fun styleFor(type: ToastType): ToastStyle =
    when (type) {
        ToastType.SUCCESS -> ToastStyle(HemSuccess, "✓")
        ToastType.ERROR -> ToastStyle(HemError, "✕")
        ToastType.INFO -> ToastStyle(HemInfo, "i")
        ToastType.WARNING -> ToastStyle(HemWarning, "!")
    }

@Composable
private fun ToastCard(
    toast: ToastMessage,
    onDismiss: () -> Unit,
) {
    var visible by remember(toast.id) { mutableStateOf(false) }

    LaunchedEffect(toast.id) {
        visible = true
        delay(toast.durationMillis)
        visible = false
        delay(200)
        onDismiss()
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(animationSpec = tween(220)) { -it } + fadeIn(tween(220)),
        exit = slideOutVertically(animationSpec = tween(200)) { -it } + fadeOut(tween(200)),
    ) {
        val style = styleFor(toast.type)
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                    .clickable {
                        visible = false
                        onDismiss()
                    }.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(26.dp).background(style.accent, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = style.glyph,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.labelMedium.fontSize,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = toast.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
