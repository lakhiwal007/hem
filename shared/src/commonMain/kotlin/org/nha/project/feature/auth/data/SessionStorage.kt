package org.nha.project.feature.auth.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.nha.project.feature.auth.domain.UserSession

class SessionStorage(
    private val sessionDao: SessionDao,
) {
    val session: Flow<UserSession?> = sessionDao.observeSession().map { it?.toDomain() }
    val isLoggedIn: Flow<Boolean> = session.map { it != null }

    suspend fun save(session: UserSession) {
        sessionDao.upsertSession(session.toEntity())
    }

    suspend fun clear() {
        sessionDao.clearSession()
    }
}
