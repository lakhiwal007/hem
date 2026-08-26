package org.nha.project

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.nha.project.core.navigation.AppNavHost
import org.nha.project.core.ui.theme.HemTheme

@Composable
@Preview
fun App() {
    HemTheme {
        AppNavHost()
    }
}
