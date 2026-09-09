package org.nha.project.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import hem.shared.generated.resources.Res
import hem.shared.generated.resources.alert
import org.jetbrains.compose.resources.painterResource
import org.nha.project.core.ui.theme.HemError
import kotlin.math.roundToInt

@Composable
fun HospitalOutOfRangeSheet(
    hospitalName: String,
    distanceMeters: Double?,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
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
            text = "You have moved away from $hospitalName",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text =
                "Please go to the hospital facility to continue. " +
                    "This message will go away once you're back in range.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center,
        )
        if (distanceMeters != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(HemError.copy(alpha = 0.1f))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(
                    text = "${distanceMeters.roundToInt()}m from facility",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = HemError,
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onExit,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HemError),
        ) {
            Text("BACK TO HOSPITALS")
        }
    }
}
