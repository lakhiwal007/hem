package org.nha.project.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TokenStorage(private val dataStore: DataStore<Preferences>) {
    private val authTokenKey = stringPreferencesKey("auth_token")

    val authToken: Flow<String?> = dataStore.data.map { it[authTokenKey] }

    suspend fun saveToken(token: String) {
        dataStore.edit { it[authTokenKey] = token }
    }

    suspend fun clearToken() {
        dataStore.edit { it.remove(authTokenKey) }
    }
}
