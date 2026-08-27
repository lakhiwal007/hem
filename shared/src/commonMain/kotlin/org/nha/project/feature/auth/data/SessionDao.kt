package org.nha.project.feature.auth.data

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM session WHERE id = 0")
    fun observeSession(): Flow<SessionEntity?>

    @Upsert
    suspend fun upsertSession(session: SessionEntity)

    @Query("DELETE FROM session")
    suspend fun clearSession()
}
