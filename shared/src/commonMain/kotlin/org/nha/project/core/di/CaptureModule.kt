package org.nha.project.core.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.nha.project.feature.capture.presentation.CaptureViewModel

val captureModule =
    module {
        viewModel { params -> CaptureViewModel(get(), params.get(), params.get()) }
    }
