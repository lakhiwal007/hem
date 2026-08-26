package org.nha.project.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.module.Module
import org.koin.dsl.module
import org.nha.project.core.storage.DATA_STORE_FILE_NAME
import org.nha.project.core.storage.createDataStore

actual val platformModule: Module =
    module {
        single<HttpClientEngine> { OkHttp.create() }
        single<DataStore<Preferences>> {
            val context = get<Context>()
            createDataStore { context.filesDir.resolve(DATA_STORE_FILE_NAME).absolutePath }
        }
    }
