package com.pubnub.apikt.models.consumer.objects.channel

data class PNChannelMetadata(
    val id: String,
    val name: String?,
    val description: String?,
    val custom: Any?,
    val updated: String?,
    val eTag: String?,
    val type: String?,
    val status: String?
)
