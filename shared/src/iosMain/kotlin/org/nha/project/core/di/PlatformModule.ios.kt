package org.nha.project.core.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.engine.darwin.certificates.CertificatePinner
import org.koin.core.module.Module
import org.koin.dsl.module
import org.nha.project.core.capture.ImageEditor
import org.nha.project.core.capture.ImageMetadataStamper
import org.nha.project.core.capture.IosImageEditor
import org.nha.project.core.capture.IosImageMetadataStamper
import org.nha.project.core.location.CurrentLocationProvider
import org.nha.project.core.location.IosCurrentLocationProvider
import org.nha.project.core.location.IosLocationAccessChecker
import org.nha.project.core.location.IosReverseGeocoder
import org.nha.project.core.location.LocationAccessChecker
import org.nha.project.core.location.ReverseGeocoder
import org.nha.project.core.network.CertificatePins
import org.nha.project.core.storage.AppDatabase
import org.nha.project.core.storage.getDatabaseBuilder
import org.nha.project.core.storage.getRoomDatabase

actual val platformModule: Module =
    module {
        single<HttpClientEngine> {
            Darwin.create {
                val pinnerBuilder = CertificatePinner.Builder()
                CertificatePins.SHA256_BASE64.forEach { pin ->
                    pinnerBuilder.add(CertificatePins.API_HOST, "sha256/$pin")
                }
                handleChallenge(pinnerBuilder.build())
            }
        }
        single<AppDatabase> { getRoomDatabase(getDatabaseBuilder()) }
        single<LocationAccessChecker> { IosLocationAccessChecker() }
        single<CurrentLocationProvider> { IosCurrentLocationProvider() }
        single<ReverseGeocoder> { IosReverseGeocoder() }
        single<ImageMetadataStamper> { IosImageMetadataStamper() }
        single<ImageEditor> { IosImageEditor() }
    }
