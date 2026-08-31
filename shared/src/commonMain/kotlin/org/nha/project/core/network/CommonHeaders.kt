package org.nha.project.core.network

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.headers

private val COMMON_HEADERS =
    mapOf(
        "accept" to "application/json, text/plain, */*",
        "appname" to "BIS",
        "access-control-allow-origin" to "https://bisbeta.nha.gov.in",
        "cache-control" to "no-cache",
        "priority" to "u=1, i",
        "origin" to "https://bisbeta.nha.gov.in",
        "referer" to "https://bisbeta.nha.gov.in",
        "pragma" to "no-cache",
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

fun HttpRequestBuilder.applyCommonHeaders() {
    headers {
        COMMON_HEADERS.forEach { (key, value) -> append(key, value) }
    }
}
