package org.nha.project.feature.capture.data

internal object CaptureApiUrls {
    private const val BASE_URL = "https://apisbeta.nha.gov.in/pmjay/hem/hem"

    const val UPLOAD = "$BASE_URL/upload"
    const val VIEW_IMAGES = "$BASE_URL/viewImages"
    const val FINAL_SUBMIT = "$BASE_URL/finalSubmit"
}
