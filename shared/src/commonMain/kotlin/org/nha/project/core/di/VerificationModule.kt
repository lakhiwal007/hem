package org.nha.project.core.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.nha.project.feature.verification.data.VerifierApi
import org.nha.project.feature.verification.presentation.HospitalOtpVerificationViewModel
import org.nha.project.feature.verification.presentation.PhysicalVerifyImagesViewModel

val verificationModule =
    module {
        single { VerifierApi(get(), get(), get()) }
        viewModel { params -> HospitalOtpVerificationViewModel(get(), get(), params.get()) }
        viewModel { params ->
            PhysicalVerifyImagesViewModel(get(), get(), get(), get(), params.get(), params.get(), params.get())
        }
    }
