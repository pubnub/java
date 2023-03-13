package com.pubnub.apikt.models.server.files

import com.pubnub.apikt.models.consumer.files.PNBaseFile

data class FileUploadNotification(
    val message: Any?,
    val file: PNBaseFile
)
