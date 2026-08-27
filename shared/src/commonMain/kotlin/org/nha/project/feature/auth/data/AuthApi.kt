package org.nha.project.feature.auth.data

import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import org.nha.project.core.network.ApiResult
import org.nha.project.core.network.NetworkException
import org.nha.project.core.secrets.AppSecrets
import org.nha.project.core.security.IdamCrypto
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val REQUEST_AGENT = "nhaMobile"

class AuthApi(
    private val httpClient: HttpClient,
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend fun generateToken(): ApiResult<String> =
        runCatchingApi {
            val data = "${Uuid.random()}${currentTimestamp()}"
            val hashData = IdamCrypto.encrypt(AppSecrets.IDAM_KEY, data)
            httpClient.post(AuthApiUrls.GEN_TOKEN) {
                headers {
                    append(HttpHeaders.ContentType, "text/plain")
                    append("Request-Agent", REQUEST_AGENT)
                }
                setBody(hashData)
            }
        }

    suspend fun generateCaptcha(token: String): ApiResult<CaptchaResponse> =
        postJson(AuthApiUrls.GEN_CAPTCHA, token, "{}")

    suspend fun resendCaptcha(
        token: String,
        transactionId: String,
    ): ApiResult<ResendCaptchaResponse> =
        postJson(
            AuthApiUrls.RESEND_CAPTCHA,
            token,
            json.encodeToString(mapOf("role" to "user", "transactionid" to transactionId)),
        )

    suspend fun checkCaptcha(
        token: String,
        transactionId: String,
        loginId: String,
        encryptedCaptcha: String,
    ): ApiResult<CaptchaCheckResponse> =
        postJson(
            AuthApiUrls.CHECK,
            token,
            json.encodeToString(
                mapOf(
                    "role" to "user",
                    "captchaId" to transactionId,
                    "captcha" to encryptedCaptcha,
                    "loginid" to loginId,
                ),
            ),
        )

    suspend fun init(
        token: String,
        userId: String,
        authMode: String,
    ): ApiResult<InitApiResponse> =
        postJson(
            AuthApiUrls.INIT,
            token,
            json.encodeToString(mapOf("role" to "user", "authmode" to authMode, "userid" to userId)),
        )

    suspend fun validate(
        token: String,
        transactionId: String,
        encryptedCaptcha: String,
        encryptedPassOtp: String,
        authTransaction: String,
    ): ApiResult<ValidateResponse> =
        postJson(
            AuthApiUrls.VALIDATE,
            token,
            json.encodeToString(
                mapOf(
                    "role" to "user",
                    "captcha" to encryptedCaptcha,
                    "transactionid" to transactionId,
                    "token" to encryptedPassOtp,
                    "language" to "English",
                    "authtransaction" to authTransaction,
                ),
            ),
        )

    suspend fun decrypt(
        token: String,
        encryptedBody: String,
    ): ApiResult<String> =
        runCatchingApi {
            httpClient.post(AuthApiUrls.DECRYPT) {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                    append(HttpHeaders.ContentType, "application/json; charset=UTF-8")
                    append("Request-Agent", REQUEST_AGENT)
                }
                setBody(encryptedBody)
            }
        }

    private suspend inline fun <reified T> postJson(
        url: String,
        token: String,
        body: String,
    ): ApiResult<T> =
        runCatchingApi {
            httpClient.post(url) {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                    append(HttpHeaders.ContentType, "application/json; charset=UTF-8")
                    append("Request-Agent", REQUEST_AGENT)
                }
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }.let { result ->
            when (result) {
                is ApiResult.Success -> {
                    try {
                        ApiResult.Success(json.decodeFromString<T>(result.data))
                    } catch (e: Exception) {
                        ApiResult.Error(NetworkException.Unknown(e))
                    }
                }
                is ApiResult.Error -> result
            }
        }

    private suspend inline fun runCatchingApi(block: () -> HttpResponse): ApiResult<String> =
        try {
            val response = block()
            if (response.status.value == 200) {
                ApiResult.Success(response.bodyAsText())
            } else {
                ApiResult.Error(NetworkException.ServerError(response.status.value, response.bodyAsText()))
            }
        } catch (e: Exception) {
            ApiResult.Error(NetworkException.Unknown(e))
        }
}

@OptIn(ExperimentalTime::class)
private fun currentTimestamp(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return buildString {
        append(now.year.toString().padStart(4, '0'))
        @Suppress("DEPRECATION")
        append(now.monthNumber.toString().padStart(2, '0'))
        @Suppress("DEPRECATION")
        append(now.dayOfMonth.toString().padStart(2, '0'))
        append(now.hour.toString().padStart(2, '0'))
        append(now.minute.toString().padStart(2, '0'))
        append(now.second.toString().padStart(2, '0'))
    }
}
