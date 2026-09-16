package org.nha.project.core.di

import android.content.Context
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.CertificatePinner
import org.koin.core.module.Module
import org.koin.dsl.module
import org.nha.project.core.capture.AndroidImageEditor
import org.nha.project.core.capture.AndroidImageMetadataStamper
import org.nha.project.core.capture.ImageEditor
import org.nha.project.core.capture.ImageMetadataStamper
import org.nha.project.core.location.AndroidCurrentLocationProvider
import org.nha.project.core.location.AndroidLocationAccessChecker
import org.nha.project.core.location.AndroidReverseGeocoder
import org.nha.project.core.location.CurrentLocationProvider
import org.nha.project.core.location.LocationAccessChecker
import org.nha.project.core.location.ReverseGeocoder
import org.nha.project.core.network.CertificatePins
import org.nha.project.core.storage.AppDatabase
import org.nha.project.core.storage.getDatabaseBuilder
import org.nha.project.core.storage.getRoomDatabase

actual val platformModule: Module =
    module {
        single<HttpClientEngine> {
            OkHttp.create {
                config {
                    val pinnerBuilder = CertificatePinner.Builder()
                    CertificatePins.SHA256_BASE64.forEach { pin ->
                        pinnerBuilder.add(CertificatePins.API_HOST, "sha256/$pin")
                    }
                    certificatePinner(pinnerBuilder.build())
                }
            }
        }
        single<AppDatabase> {
            val context = get<Context>()
            getRoomDatabase(getDatabaseBuilder(context))
        }
        single<LocationAccessChecker> { AndroidLocationAccessChecker(get()) }
        single<CurrentLocationProvider> { AndroidCurrentLocationProvider(get()) }
        single<ReverseGeocoder> { AndroidReverseGeocoder(get()) }
        single<ImageMetadataStamper> { AndroidImageMetadataStamper(get()) }
        single<ImageEditor> { AndroidImageEditor() }
    }
