package org.nha.project.feature.permission.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hem.shared.generated.resources.Res
import hem.shared.generated.resources.alert
import hem.shared.generated.resources.device_location
import hem.shared.generated.resources.location_accuracy
import hem.shared.generated.resources.onboarding_screen_background
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.nha.project.core.ui.theme.HemPrimary

private const val LOCATION_ACCURACY_COPY =
    "Location Accuracy, which provides more accurate location for apps and " +
        "services. To do this, Google periodically processes information about " +
        "device sensors and wireless signals from your device to crowdsource " +
        "wireless signal locations. These are used without identifying you to " +
        "improve location accuracy and location-based services and to improve, " +
        "provide and maintain Google's services based on Google's and third " +
        "parties' legitimate interests to serve users' needs."

@Composable
fun LocationPermissionScreen(onContinue: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.onboarding_screen_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f)),
        )
        Column(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier =
                    Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color(0xFFDDDDDD), RoundedCornerShape(2.dp)),
            )
            Spacer(modifier = Modifier.height(20.dp))
            Image(
                painter = painterResource(Res.drawable.alert),
                contentDescription = null,
                modifier = Modifier.height(56.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "To access this application your device will need to use location permission.",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "The following settings should be on:",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(16.dp))
            LocationSettingRow(iconRes = Res.drawable.device_location, text = "Device location")
            Spacer(modifier = Modifier.height(16.dp))
            LocationSettingRow(iconRes = Res.drawable.location_accuracy, text = LOCATION_ACCURACY_COPY)
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HemPrimary),
            ) {
                Text("TURN ON YOUR LOCATION")
            }
        }
    }
}

@Composable
private fun LocationSettingRow(
    iconRes: DrawableResource,
    text: String,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.height(20.dp),
        )
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}
