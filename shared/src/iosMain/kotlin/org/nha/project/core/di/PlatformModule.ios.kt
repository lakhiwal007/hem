package org.nha.project.core.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.nha.project.core.storage.DATA_STORE_FILE_NAME
import org.nha.project.core.storage.createDataStore
import platform.Foundation.NSHomeDirectory

actual val platformModule: Module =
    module {
        single<HttpClientEngine> { Darwin.create() }
        single<DataStore<Preferences>> {
            createDataStore { NSHomeDirectory() + "/$DATA_STORE_FILE_NAME" }
        }
    }
