package org.nha.project.core.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import org.koin.dsl.module
import org.nha.project.core.network.createHttpClient

val networkModule =
    module {
        single<HttpClient> { createHttpClient(get<HttpClientEngine>()) }
    }
