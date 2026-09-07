package org.nha.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.nha.project.core.navigation.AppNavDisplay
import org.nha.project.core.ui.theme.HemTheme
import org.nha.project.core.ui.toast.ToastController
import org.nha.project.core.ui.toast.ToastHost

@Composable
@Preview
fun App() {
    HemTheme {
        val toastController = koinInject<ToastController>()
        Box(modifier = Modifier.fillMaxSize()) {
            AppNavDisplay()
            ToastHost(controller = toastController, modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}
