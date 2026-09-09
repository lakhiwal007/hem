package org.nha.project.core.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.nha.project.feature.hospital.data.HospitalApi
import org.nha.project.feature.hospital.presentation.HospitalListViewModel
import org.nha.project.feature.hospital.presentation.HospitalLocationVerificationViewModel
import org.nha.project.feature.hospital.presentation.HospitalServicesViewModel
import org.nha.project.feature.hospital.presentation.HospitalSpecialitiesViewModel

val hospitalModule =
    module {
        single { HospitalApi(get(), get(), get()) }
        viewModel { HospitalListViewModel(get(), get(), get(), get(), get()) }
        viewModel { params -> HospitalLocationVerificationViewModel(get(), get(), params.get()) }
        viewModel { params -> HospitalSpecialitiesViewModel(get(), get(), params.get()) }
        viewModel { params -> HospitalServicesViewModel(get(), get(), get(), params.get(), params.get()) }
    }
