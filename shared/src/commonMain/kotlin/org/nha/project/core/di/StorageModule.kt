package org.nha.project.core.di

import org.koin.dsl.module
import org.nha.project.core.storage.AppDatabase
import org.nha.project.feature.auth.data.SessionStorage

val storageModule =
    module {
        single { SessionStorage(get<AppDatabase>().sessionDao()) }
    }
