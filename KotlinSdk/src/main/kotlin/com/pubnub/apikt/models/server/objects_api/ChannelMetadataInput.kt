package com.pubnub.apikt.models.server.objects_api

internal data class ChannelMetadataInput(
    val name: String? = null,
    val description: String? = null,
    val custom: Any? = null,
    val type: String? = null,
    val status: String? = null
)
