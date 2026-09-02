package org.nha.project.feature.auth.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorBody(
    val timestamp: String? = null,
    @SerialName("api_call_id") val apiCallId: String? = null,
    @SerialName("correlation_id") val correlationId: String? = null,
    val captcha: String? = null,
    val error: ApiErrorDetail? = null,
)

@Serializable
data class ApiErrorDetail(
    val code: String? = null,
    val message: String? = null,
)

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
    val stateCode: Int? = null,
    val clusterId: List<Int>? = null,
    val appRoleList: Map<String, List<String>>? = null,
)
