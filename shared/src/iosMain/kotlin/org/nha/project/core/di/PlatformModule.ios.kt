package org.nha.project.core.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.nha.project.core.location.IosLocationAccessChecker
import org.nha.project.core.location.LocationAccessChecker
import org.nha.project.core.storage.AppDatabase
import org.nha.project.core.storage.getDatabaseBuilder
import org.nha.project.core.storage.getRoomDatabase

actual val platformModule: Module =
    module {
        single<HttpClientEngine> { Darwin.create() }
        single<AppDatabase> { getRoomDatabase(getDatabaseBuilder()) }
        single<LocationAccessChecker> { IosLocationAccessChecker() }
    }
