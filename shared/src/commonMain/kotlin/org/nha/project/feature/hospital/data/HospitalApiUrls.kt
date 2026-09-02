package org.nha.project.feature.hospital.data

internal object HospitalApiUrls {
    private const val BASE_URL = "https://apiltm.nha.gov.in/pmjay/tms/hem"

    const val HOSPITALS_LIST = "$BASE_URL/mobile/getHospitalsDetailsList"
    const val SPECIALITIES = "$BASE_URL/getHospitalSpecialityInfoMobile"
    const val SERVICES = "$BASE_URL/getServices"
}
