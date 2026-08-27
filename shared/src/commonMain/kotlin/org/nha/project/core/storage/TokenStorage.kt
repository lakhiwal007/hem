package org.nha.project.core.storage

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TokenStorage(
    private val tokenDao: TokenDao,
) {
    val authToken: Flow<String?> = tokenDao.observeToken().map { it?.value }

    suspend fun saveToken(token: String) {
        tokenDao.upsertToken(TokenEntity(value = token))
    }

    suspend fun clearToken() {
        tokenDao.clearToken()
    }
}
