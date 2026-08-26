package org.nha.project.core.di

import org.koin.dsl.module
import org.nha.project.core.storage.TokenStorage

val storageModule = module {
    single { TokenStorage(get()) }
}
