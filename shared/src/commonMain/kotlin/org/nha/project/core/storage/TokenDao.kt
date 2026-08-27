package org.nha.project.core.storage

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TokenDao {
    @Query("SELECT * FROM token WHERE id = 0")
    fun observeToken(): Flow<TokenEntity?>

    @Upsert
    suspend fun upsertToken(token: TokenEntity)

    @Query("DELETE FROM token")
    suspend fun clearToken()
}
