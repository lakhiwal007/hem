package org.nha.project.feature.verification.data

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import org.nha.project.core.network.ApiResult
import org.nha.project.core.network.NetworkException
import org.nha.project.core.network.applyHemHeaders
import org.nha.project.feature.auth.data.SessionStorage
import org.nha.project.feature.auth.domain.UserSession

class VerifierApi(
    private val httpClient: HttpClient,
    private val sessionStorage: SessionStorage,
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    suspend fun getWorklist(): ApiResult<List<VerifierWorklistItemDto>> =
        request { session ->
            httpClient.get(VerifierApiUrls.WORKLIST) {
                applyHemHeaders(session)
            }
        }

    suspend fun generateOtp(mobileNo: String): ApiResult<GenerateOtpResponseDto> =
        request { session ->
            httpClient.get(VerifierApiUrls.GENERATE_OTP) {
                applyHemHeaders(session)
                parameter("mobileNo", mobileNo)
            }
        }

    suspend fun validateOtp(
        transactionId: String,
        otp: String,
    ): ApiResult<ValidateOtpResponseDto> =
        request { session ->
            httpClient.post(VerifierApiUrls.VALIDATE_OTP) {
                applyHemHeaders(session)
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(ValidateOtpRequest(transactionId = transactionId, otp = otp)))
            }
        }

    suspend fun verifierAction(
        submissionId: Long,
        hospId: Long,
        specialityId: Long,
        serviceId: Long,
        verificationStatus: String,
        comments: String,
        verifiedBy: String,
    ): ApiResult<VerifierActionResponseDto> =
        request { session ->
            httpClient.post(VerifierApiUrls.VERIFIER_ACTION) {
                applyHemHeaders(session)
                contentType(ContentType.Application.Json)
                setBody(
                    json.encodeToString(
                        VerifierActionRequest(
                            submissionId = submissionId,
                            hospId = hospId,
                            specialityId = specialityId,
                            serviceId = serviceId,
                            verificationStatus = verificationStatus,
                            comments = comments,
                            verifiedBy = verifiedBy,
                        ),
                    ),
                )
            }
        }

    suspend fun getVerificationStatus(submissionId: Long): ApiResult<VerifierActionResponseDto> =
        request { session ->
            httpClient.get(VerifierApiUrls.VERIFICATION_STATUS) {
                applyHemHeaders(session)
                parameter("submissionId", submissionId)
            }
        }

    private suspend inline fun <reified T> request(block: (UserSession) -> HttpResponse): ApiResult<T> {
        val session = sessionStorage.session.first() ?: return ApiResult.Error(NetworkException.Unknown())
        return try {
            val response = block(session)
            if (response.status.value == 200) {
                try {
                    ApiResult.Success(json.decodeFromString<T>(response.bodyAsText()))
                } catch (e: Exception) {
                    ApiResult.Error(NetworkException.Unknown(e))
                }
            } else {
                ApiResult.Error(NetworkException.ServerError(response.status.value, response.bodyAsText()))
            }
        } catch (e: Exception) {
            ApiResult.Error(NetworkException.Unknown(e))
        }
    }
}
