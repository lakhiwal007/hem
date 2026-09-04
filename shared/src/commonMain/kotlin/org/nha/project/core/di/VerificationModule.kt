package org.nha.project.core.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.nha.project.feature.verification.data.PhysicalVerifierMockRepository
import org.nha.project.feature.verification.presentation.HospitalOtpVerificationViewModel
import org.nha.project.feature.verification.presentation.PhysicalVerifyImagesViewModel

val verificationModule =
    module {
        single { PhysicalVerifierMockRepository() }
        viewModel { params -> HospitalOtpVerificationViewModel(get(), get(), params.get()) }
        viewModel { params -> PhysicalVerifyImagesViewModel(get(), get(), params.get()) }
    }
