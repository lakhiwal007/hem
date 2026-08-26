package org.nha.project.core.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

fun initKoin(extraModules: List<Module> = emptyList(), platformDeclaration: KoinAppDeclaration? = null) {
    startKoin {
        platformDeclaration?.invoke(this)
        modules(appModules + extraModules)
    }
}
