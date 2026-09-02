package org.nha.project.core.network

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.headers
import org.nha.project.feature.auth.domain.UserSession

private val HEM_STATIC_HEADERS =
    mapOf(
        "accept" to "application/json, text/plain, */*",
        "appname" to "HEM",
        "access-control-allow-origin" to "https://hemltm.nha.gov.in/",
        "cache-control" to "no-cache",
        "priority" to "u=1, i",
        "origin" to "https://hemltm.nha.gov.in",
        "referer" to "https://hemltm.nha.gov.in/",
        "pragma" to "no-cache",
        "dcode" to "null",
        "request-agent" to "web",
        "content-type" to "application/json; charset=UTF-8",
        "sec-ch-ua-mobile" to "?1",
        "sec-ch-ua-platform" to "Android",
        "sec-fetch-dest" to "empty",
        "sec-fetch-mode" to "cors",
        "sec-fetch-site" to "cross-site",
        "sec-ch-ua" to "'Chromium';v='134', 'Not:A-Brand';v='24', 'Google Chrome';v='134'",
        "user-agent" to
            "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/134.0.0.0 Mobile Safari/537.36",
    )

fun HttpRequestBuilder.applyHemHeaders(session: UserSession) {
    headers {
        HEM_STATIC_HEADERS.forEach { (key, value) -> append(key, value) }
        append("cid", session.clusterId)
        append("pid", session.parentEntityId.toString())
        append("hid", session.entityId.toString())
        append("scode", session.stateCode)
        append("uid", session.userId)
        append("urole", session.roleName)
        append("username", session.username)
        append("tid", session.transactionId)
        append("uauthorization", "Bearer ${session.authToken}")
    }
}
