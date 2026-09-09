package org.nha.project.core.network

sealed class ApiResult<out T> {
    data class Success<T>(
        val data: T,
    ) : ApiResult<T>()

    data class Error(
        val exception: NetworkException,
    ) : ApiResult<Nothing>()
}

sealed class NetworkException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    class NoConnection(
        cause: Throwable? = null,
    ) : NetworkException("No network connection", cause)

    class Timeout(
        cause: Throwable? = null,
    ) : NetworkException("Request timed out", cause)

    class ServerError(
        val code: Int,
        serverMessage: String,
    ) : NetworkException("Server error $code: $serverMessage")

    class ApiError(
        val code: String?,
        serverMessage: String,
    ) : NetworkException(serverMessage)

    class Unknown(
        cause: Throwable? = null,
    ) : NetworkException(cause?.message ?: "Unknown network error", cause)
}

fun NetworkException.isUnauthorized(): Boolean = this is NetworkException.ServerError && code == 401
