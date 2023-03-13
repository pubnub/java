package com.pubnub.apikt.models.server.files

import com.pubnub.apikt.models.consumer.files.PNUploadedFile
import com.pubnub.apikt.models.consumer.objects.PNPage

data class ListFilesResult(
    val count: Int,
    val next: PNPage.PNNext?,
    val status: Int,
    val data: Collection<PNUploadedFile>
)
