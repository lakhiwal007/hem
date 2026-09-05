package org.nha.project.core.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.nha.project.feature.capture.data.CaptureApi
import org.nha.project.feature.capture.presentation.CaptureViewModel

val captureModule =
    module {
        single { CaptureApi(get(), get()) }
        viewModel { params -> CaptureViewModel(get(), get(), get(), get(), params.get(), params.get(), params.get()) }
    }
