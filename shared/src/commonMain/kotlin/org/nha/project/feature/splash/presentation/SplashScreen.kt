package org.nha.project.feature.splash.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hem.shared.generated.resources.Res
import hem.shared.generated.resources.nha_logo
import hem.shared.generated.resources.onboarding_screen_background
import hem.shared.generated.resources.pmjay_logo
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.nha.project.core.ui.theme.HemTheme

private const val SPLASH_DURATION_MS = 2000L

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION_MS)
        onTimeout()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.onboarding_screen_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Column(
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .fillMaxSize()
                    .background(Color.Transparent),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.nha_logo),
                contentDescription = "National Health Authority",
                Modifier.size(140.dp),
            )

            Image(
                painter = painterResource(Res.drawable.pmjay_logo),
                contentDescription = "PM-JAY",
                modifier =
                    Modifier
                        .height(
                            140.dp,
                        ).clip(CircleShape)
                        .border(width = 1.dp, color = Color.White, shape = CircleShape),
            )
        }
    }
}

@Preview
@Composable
private fun SplashScreenPreview() {
    HemTheme {
        SplashScreen(onTimeout = {})
    }
}
