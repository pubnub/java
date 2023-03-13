package com.pubnub.apikt.models.consumer.files

import com.pubnub.apikt.models.consumer.objects.PNPage

data class PNListFilesResult(
    val count: Int,
    val next: PNPage.PNNext?,
    val status: Int,
    val data: Collection<PNUploadedFile>
)
