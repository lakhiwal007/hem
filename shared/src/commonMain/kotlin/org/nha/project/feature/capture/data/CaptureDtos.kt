package org.nha.project.feature.capture.data

import kotlinx.serialization.Serializable

@Serializable
data class UploadImageRequest(
    val hospId: Long,
    val specialityId: Long,
    val serviceId: Long,
    val imageSlot: Int,
    val fileName: String,
    val uploadedBy: String,
    val attachment: AttachmentDto,
)

@Serializable
data class AttachmentDto(
    val attachmentname: String,
    val attachmentcontent: String,
)

@Serializable
data class FinalSubmitRequest(
    val hospId: Long,
    val specialityId: Long,
    val serviceId: Long,
)

@Serializable
data class SubmissionDto(
    val submissionId: Long? = null,
    val hospId: Long? = null,
    val specialityId: Long? = null,
    val serviceId: Long? = null,
    val submissionVersion: Int? = null,
    val submissionStatus: String? = null,
    val count: Int? = null,
    val uploadedCount: Int? = null,
    val pendingCount: Int? = null,
    val allMandatoryImagesUploaded: Boolean? = null,
    val finalSubmitAllowed: Boolean? = null,
    val images: List<SubmissionImageDto> = emptyList(),
)

@Serializable
data class SubmissionImageDto(
    val imageId: Long? = null,
    val imageSlot: Int? = null,
    val imageVersion: Int? = null,
    val fileName: String? = null,
    val fileSize: Long? = null,
    val imageStatus: String? = null,
    val viewUrl: String? = null,
    val uploadedBy: String? = null,
    val uploadedOn: String? = null,
)

@Serializable
data class ViewImageDto(
    val imageId: Long? = null,
    val imageSlot: Int? = null,
    val imageVersion: Int? = null,
    val fileName: String? = null,
    val base64Image: String? = null,
    val submissionId: Long? = null,
)
