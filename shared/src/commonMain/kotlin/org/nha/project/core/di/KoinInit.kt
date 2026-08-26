package org.nha.project.core.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

fun initKoin(platformDeclaration: KoinAppDeclaration? = null, extraModules: List<Module> = emptyList()) {
    startKoin {
        platformDeclaration?.invoke(this)
        modules(appModules + extraModules)
    }
}
