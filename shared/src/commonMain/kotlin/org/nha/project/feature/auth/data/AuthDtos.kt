package org.nha.project.feature.auth.data

import kotlinx.serialization.Serializable

@Serializable
data class CaptchaResponse(
    val captcha: String,
    val transactionid: String,
    val message: String,
    val authtransaction: String? = null,
)

@Serializable
data class ResendCaptchaResponse(
    val transactionid: String,
    val captcha: String,
)

@Serializable
data class CaptchaCheckResponse(
    val userid: String,
    val userLoginStatus: String? = null,
    val transactionId: String? = null,
    val defaultAuthMode: Int,
    val mobileUpdateStatus: String? = null,
    val authmodes: List<String>,
)

@Serializable
data class InitApiResponse(
    val message: String? = null,
    val userid: String? = null,
    val transactionid: String? = null,
    val captcha: String? = null,
    val authtransaction: String? = null,
)

@Serializable
data class ValidateResponse(
    val transactionid: String? = null,
    val authtoken: String? = null,
)

@Serializable
data class DecryptedProfile(
    val userid: String,
    val username: String,
    val state: String,
    val entityapprolelist: List<EntityAppRole>,
)

@Serializable
data class EntityAppRole(
    val entityType: String,
    val parentEntityId: Long,
    val roleName: String,
    val entityId: Long,
)
