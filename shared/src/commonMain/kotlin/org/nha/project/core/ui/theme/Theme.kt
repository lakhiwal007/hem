package org.nha.project.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val HemLightColors =
    lightColorScheme(
        primary = HemPrimary,
        onPrimary = HemOnPrimary,
        secondary = HemSecondary,
        background = HemBackground,
        surface = HemSurface,
        error = HemError,
    )

private val HemDarkColors =
    darkColorScheme(
        primary = HemPrimary,
        onPrimary = HemOnPrimary,
        secondary = HemSecondary,
        error = HemError,
    )

@Composable
fun HemTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) HemDarkColors else HemLightColors,
        typography = HemTypography,
        content = content,
    )
}
