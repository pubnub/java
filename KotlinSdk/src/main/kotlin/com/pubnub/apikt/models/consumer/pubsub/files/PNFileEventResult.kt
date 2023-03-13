package com.pubnub.apikt.models.consumer.pubsub.files

import com.google.gson.JsonElement
import com.pubnub.apikt.models.consumer.files.PNDownloadableFile
import com.pubnub.apikt.models.consumer.pubsub.PNEvent

data class PNFileEventResult(
    val channel: String,
    val timetoken: Long?, // timetoken in every other event model is nullable
    val publisher: String?,
    val message: Any?,
    val file: PNDownloadableFile,
    val jsonMessage: JsonElement
) : PNEvent
