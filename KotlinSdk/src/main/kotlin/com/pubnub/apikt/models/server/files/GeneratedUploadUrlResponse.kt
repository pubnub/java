package com.pubnub.apikt.models.server.files

import com.google.gson.annotations.SerializedName
import com.pubnub.apikt.models.consumer.files.PNUploadedFile

data class GeneratedUploadUrlResponse(
    val status: Int,
    val data: PNUploadedFile,
    @SerializedName("file_upload_request")
    val fileUploadRequest: FileUploadRequest
) {

    data class FileUploadRequest(
        val url: String,
        val method: String,
        @SerializedName("expiration_date")
        val expirationDate: String,
        @SerializedName("form_fields")
        val formFields: List<FormField>
    )
}
