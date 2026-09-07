package org.nha.project.core.di

import android.content.Context
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.module.Module
import org.koin.dsl.module
import org.nha.project.core.capture.AndroidImageMetadataStamper
import org.nha.project.core.capture.ImageMetadataStamper
import org.nha.project.core.location.AndroidCurrentLocationProvider
import org.nha.project.core.location.AndroidLocationAccessChecker
import org.nha.project.core.location.CurrentLocationProvider
import org.nha.project.core.location.LocationAccessChecker
import org.nha.project.core.storage.AppDatabase
import org.nha.project.core.storage.getDatabaseBuilder
import org.nha.project.core.storage.getRoomDatabase

actual val platformModule: Module =
    module {
        single<HttpClientEngine> { OkHttp.create() }
        single<AppDatabase> {
            val context = get<Context>()
            getRoomDatabase(getDatabaseBuilder(context))
        }
        single<LocationAccessChecker> { AndroidLocationAccessChecker(get()) }
        single<CurrentLocationProvider> { AndroidCurrentLocationProvider(get()) }
        single<ImageMetadataStamper> { AndroidImageMetadataStamper(get()) }
    }
