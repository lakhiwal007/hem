package org.nha.project.core.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import org.koin.dsl.module
import org.nha.project.core.network.NetworkConnectivityChecker
import org.nha.project.core.network.NetworkConnectivityObserver
import org.nha.project.core.network.SessionExpiryNotifier
import org.nha.project.core.network.createHttpClient

val networkModule =
    module {
        single<HttpClient> { createHttpClient(get<HttpClientEngine>()) }
        single { SessionExpiryNotifier(get()) }
        single { NetworkConnectivityChecker(get()) }
        single { NetworkConnectivityObserver(get()) }
    }
