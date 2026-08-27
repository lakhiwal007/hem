package org.nha.project.feature.auth.domain

data class UserSession(
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
)
