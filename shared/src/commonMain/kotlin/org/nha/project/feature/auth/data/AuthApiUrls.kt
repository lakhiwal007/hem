package org.nha.project.feature.auth.data

internal object AuthApiUrls {
    private const val BASE_URL = "https://apisbeta.nha.gov.in/pmjay/stgbis"

    const val GEN_TOKEN = "$BASE_URL/configbis/bis/token/data"
    const val GEN_CAPTCHA = "$BASE_URL/authService/bis/auth/generateCaptcha"
    const val RESEND_CAPTCHA = "$BASE_URL/authService/bis/auth/V3/resendCaptcha"
    const val CHECK = "$BASE_URL/authService/bis/auth/V3/check"
    const val INIT = "$BASE_URL/authService/bis/auth/V3/init"
    const val VALIDATE = "$BASE_URL/authService/bis/auth/V3/validate"
    const val DECRYPT = "$BASE_URL/authService/bis/auth/V3/decrypt"
    const val AUDIT_LOGIN_LOGOUT = "$BASE_URL/authService/bis/auth/audit/storeLoginLogoutDetails"
    const val REFRESH_TOKEN = "$BASE_URL/authService/bis/auth/token/refreshToken"
}
