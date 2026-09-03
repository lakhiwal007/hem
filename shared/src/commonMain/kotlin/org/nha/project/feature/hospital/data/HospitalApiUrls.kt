package org.nha.project.feature.hospital.data

internal object HospitalApiUrls {
    private const val BASE_URL = "https://apisbeta.nha.gov.in/pmjay/hem/hem"

    const val HOSPITALS_LIST = "$BASE_URL/mobile/getHospitalsDetailsList"
    const val SPECIALITIES = "$BASE_URL/getHospitalSpecialityInfoMobile"
    const val SERVICES = "$BASE_URL/getServices"
}
