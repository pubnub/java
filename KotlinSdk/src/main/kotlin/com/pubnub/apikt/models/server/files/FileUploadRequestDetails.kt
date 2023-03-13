package com.pubnub.apikt.models.server.files

import com.pubnub.apikt.models.consumer.files.PNFile

data class FileUploadRequestDetails(
    val status: Int,
    val data: PNFile,
    val url: String,
    val method: String,
    val expirationDate: String,
    val keyFormField: FormField,
    val formFields: List<FormField>
)
