package org.nha.project.feature.capture.data

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
import org.nha.project.core.network.SessionExpiryNotifier
import org.nha.project.core.network.applyHemHeaders
import org.nha.project.core.network.isUnauthorized
import org.nha.project.feature.auth.data.SessionStorage
import org.nha.project.feature.auth.domain.UserSession

private const val ATTACHMENT_DATA_URI_PREFIX = "data:image/jpeg;base64,"

class CaptureApi(
    private val httpClient: HttpClient,
    private val sessionStorage: SessionStorage,
    private val sessionExpiryNotifier: SessionExpiryNotifier,
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    suspend fun uploadImage(
        hospId: Long,
        specialityId: Long,
        serviceId: Long,
        imageSlot: Int,
        fileName: String,
        uploadedBy: String,
        base64: String,
    ): ApiResult<SubmissionDto> =
        request { session ->
            httpClient.post(CaptureApiUrls.UPLOAD) {
                applyHemHeaders(session)
                contentType(ContentType.Application.Json)
                setBody(
                    json.encodeToString(
                        UploadImageRequest(
                            hospId = hospId,
                            specialityId = specialityId,
                            serviceId = serviceId,
                            imageSlot = imageSlot,
                            fileName = fileName,
                            uploadedBy = uploadedBy,
                            attachment =
                                AttachmentDto(
                                    attachmentname = fileName,
                                    attachmentcontent = "$ATTACHMENT_DATA_URI_PREFIX$base64",
                                ),
                        ),
                    ),
                )
            }
        }

    suspend fun getUploadedImages(
        hospId: Long,
        specialityId: Long,
        serviceId: Long,
    ): ApiResult<List<ViewImageDto>> =
        request { session ->
            httpClient.get(CaptureApiUrls.VIEW_IMAGES) {
                applyHemHeaders(session)
                parameter("hospId", hospId)
                parameter("specialityId", specialityId)
                parameter("serviceId", serviceId)
            }
        }

    suspend fun finalSubmit(
        hospId: Long,
        specialityId: Long,
        serviceId: Long,
    ): ApiResult<Unit> {
        val session = sessionStorage.session.first() ?: return ApiResult.Error(NetworkException.Unknown())
        return try {
            val response =
                httpClient.post(CaptureApiUrls.FINAL_SUBMIT) {
                    applyHemHeaders(session)
                    contentType(ContentType.Application.Json)
                    setBody(json.encodeToString(FinalSubmitRequest(hospId, specialityId, serviceId)))
                }
            if (response.status.value == 200) {
                ApiResult.Success(Unit)
            } else {
                ApiResult
                    .Error(NetworkException.ServerError(response.status.value, response.bodyAsText()))
                    .also { notifyIfUnauthorized(it, session) }
            }
        } catch (e: Exception) {
            ApiResult.Error(NetworkException.Unknown(e))
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
                ApiResult
                    .Error(NetworkException.ServerError(response.status.value, response.bodyAsText()))
                    .also { notifyIfUnauthorized(it, session) }
            }
        } catch (e: Exception) {
            ApiResult.Error(NetworkException.Unknown(e))
        }
    }

    private fun notifyIfUnauthorized(
        result: ApiResult<*>,
        session: UserSession,
    ) {
        if (result is ApiResult.Error && result.exception.isUnauthorized()) {
            sessionExpiryNotifier.notifyUnauthorized(session.authToken)
        }
    }
}
