package org.nha.project.core.di

import org.koin.core.module.Module

val appModules: List<Module> =
    listOf(
        platformModule,
        networkModule,
        storageModule,
        locationModule,
        uiModule,
        authModule,
    )
