package org.nha.project.feature.verification.data

internal object VerifierApiUrls {
    private const val BASE_URL = "https://apisbeta.nha.gov.in/pmjay/hem/hem"

    const val WORKLIST = "$BASE_URL/verifierWorklist"
    const val GENERATE_OTP = "$BASE_URL/generateMobileOTP"
    const val VALIDATE_OTP = "$BASE_URL/validateOTP"
    const val VERIFIER_ACTION = "$BASE_URL/verifierAction"
    const val VERIFICATION_STATUS = "$BASE_URL/getVerificationStatus"
}
