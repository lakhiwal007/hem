package org.nha.project.feature.hospital.data

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.nha.project.core.network.ApiResult
import org.nha.project.core.network.NetworkException
import org.nha.project.core.network.SessionExpiryNotifier
import org.nha.project.core.network.applyHemHeaders
import org.nha.project.core.network.isUnauthorized
import org.nha.project.feature.auth.data.SessionStorage
import org.nha.project.feature.auth.domain.UserSession

private const val LGD_URL = "https://apisbeta.nha.gov.in/pmjay/stgbis/configbis/bis/v1/getlistOfLGD"
private const val LGD_TYPE_DISTRICT = "ST"

@Serializable
private data class LgdRequest(
    val lgdtype: String,
    val state: String,
    val parentcd: String,
)

@Serializable
data class LgdItemDto(
    val lgdcode: String? = null,
    val lgdname: String? = null,
)

class DistrictApi(
    private val httpClient: HttpClient,
    private val sessionStorage: SessionStorage,
    private val sessionExpiryNotifier: SessionExpiryNotifier,
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    suspend fun getDistricts(stateCode: String): ApiResult<List<LgdItemDto>> =
        request { session ->
            httpClient.post(LGD_URL) {
                applyHemHeaders(session)
                contentType(ContentType.Application.Json)
                setBody(
                    json.encodeToString(
                        LgdRequest(lgdtype = LGD_TYPE_DISTRICT, state = stateCode, parentcd = stateCode),
                    ),
                )
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
