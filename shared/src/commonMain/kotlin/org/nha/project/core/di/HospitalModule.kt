package org.nha.project.core.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.nha.project.feature.hospital.presentation.HospitalLocationVerificationViewModel

val hospitalModule =
    module {
        viewModel { params -> HospitalLocationVerificationViewModel(get(), get(), params.get()) }
    }
