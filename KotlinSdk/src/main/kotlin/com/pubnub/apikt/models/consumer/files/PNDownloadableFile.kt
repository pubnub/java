package com.pubnub.apikt.models.consumer.files

data class PNDownloadableFile(
    override val id: String,
    override val name: String,
    val url: String
) : PNFile
