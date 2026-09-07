package org.nha.project.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import hem.shared.generated.resources.Res
import hem.shared.generated.resources.back_arrow
import hem.shared.generated.resources.hero_banner
import hem.shared.generated.resources.pmjay_logo
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.nha.project.core.ui.theme.HemError

@Composable
fun BrandedHeader(
    onBack: (() -> Unit)? = null,
    onLogout: (() -> Unit)? = null,
    height: Dp = 200.dp,
) {
    var showLogoutConfirm by remember { mutableStateOf(false) }

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
                        .padding(start = 16.dp)
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
                        .padding(8.dp)
                        .clickable(onClick = onBack),
            )
        }
        Row(
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onLogout != null) {
                Text(
                    text = "Logout",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                            .clickable { showLogoutConfirm = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
//            Image(
//                painter = painterResource(Res.drawable.nha_logo),
//                contentDescription = "National Health Authority",
//                modifier = Modifier.height(32.dp),
//            )
        }
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

    if (showLogoutConfirm && onLogout != null) {
        LogoutConfirmationSheet(
            onConfirm = {
                showLogoutConfirm = false
                onLogout()
            },
            onDismiss = { showLogoutConfirm = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogoutConfirmationSheet(
    onConfirm: () -> Unit,
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
                text = "Logout",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Are you sure you want to logout?",
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
                    onClick = { dismissThen(onConfirm) },
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
