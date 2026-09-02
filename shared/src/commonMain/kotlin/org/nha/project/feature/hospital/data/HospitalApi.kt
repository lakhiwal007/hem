package org.nha.project.feature.hospital.data

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

private const val SCHEME_CODE = "PMJAY"

class HospitalApi(
    private val httpClient: HttpClient,
    private val sessionStorage: SessionStorage,
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    suspend fun getHospitals(): ApiResult<List<HospitalGroupDto>> =
        request { session ->
            httpClient.post(HospitalApiUrls.HOSPITALS_LIST) {
                applyHemHeaders(session)
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(mapOf("userId" to session.userId)))
            }
        }

    suspend fun getSpecialities(hospitalId: Long): ApiResult<List<SpecialityDto>> =
        request { session ->
            httpClient.get(HospitalApiUrls.SPECIALITIES) {
                applyHemHeaders(session)
                parameter("hospId", hospitalId)
                parameter("schemeCode", SCHEME_CODE)
            }
        }

    suspend fun getServices(specialityId: Long): ApiResult<List<ServiceDto>> =
        request { session ->
            httpClient.get(HospitalApiUrls.SERVICES) {
                applyHemHeaders(session)
                parameter("specialityId", specialityId)
                parameter("schemeCode", SCHEME_CODE)
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
