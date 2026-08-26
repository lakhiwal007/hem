package org.nha.project

import androidx.compose.ui.window.ComposeUIViewController
import org.nha.project.core.di.initKoin

@Suppress("ktlint:standard:function-naming")
fun MainViewController() =
    run {
        initKoin()
        ComposeUIViewController { App() }
    }
