package org.nha.project.feature.auth.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import org.nha.project.feature.auth.domain.UserSession

@Entity(tableName = "session")
data class SessionEntity(
    @PrimaryKey val id: Int = 0,
    val clientToken: String,
    val authToken: String,
    val transactionId: String,
    val userId: String,
    val username: String,
    val state: String,
    val entityType: String,
    val roleName: String,
    val entityId: Long,
    val parentEntityId: Long,
    val stateCode: String = "",
    val clusterId: String = "0",
)

fun SessionEntity.toDomain() =
    UserSession(
        clientToken = clientToken,
        authToken = authToken,
        transactionId = transactionId,
        userId = userId,
        username = username,
        state = state,
        entityType = entityType,
        roleName = roleName,
        entityId = entityId,
        parentEntityId = parentEntityId,
        stateCode = stateCode,
        clusterId = clusterId,
    )

fun UserSession.toEntity() =
    SessionEntity(
        clientToken = clientToken,
        authToken = authToken,
        transactionId = transactionId,
        userId = userId,
        username = username,
        state = state,
        entityType = entityType,
        roleName = roleName,
        entityId = entityId,
        parentEntityId = parentEntityId,
        stateCode = stateCode,
        clusterId = clusterId,
    )
