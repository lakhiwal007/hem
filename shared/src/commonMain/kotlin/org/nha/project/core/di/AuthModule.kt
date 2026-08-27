package org.nha.project.core.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.nha.project.feature.auth.data.AuthApi
import org.nha.project.feature.auth.presentation.LoginViewModel

val authModule =
    module {
        single { AuthApi(get()) }
        viewModel { LoginViewModel(get(), get()) }
    }
