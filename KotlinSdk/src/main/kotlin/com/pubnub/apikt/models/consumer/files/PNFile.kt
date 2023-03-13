package com.pubnub.apikt.models.consumer.files

interface PNFile {
    val id: String
    val name: String
}

data class PNUploadedFile(
    override val id: String,
    override val name: String,
    val size: Int,
    val created: String
) : PNFile

data class PNBaseFile(
    override val id: String,
    override val name: String
) : PNFile
