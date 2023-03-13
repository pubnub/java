package com.pubnub.apikt.models.consumer.files

import java.io.InputStream

data class PNDownloadFileResult(
    val fileName: String,
    val byteStream: InputStream?
)
