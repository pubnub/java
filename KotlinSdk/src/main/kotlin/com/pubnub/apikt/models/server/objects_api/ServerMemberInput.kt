package com.pubnub.apikt.models.server.objects_api

internal data class ServerMemberInput(
    val uuid: UUIDId,
    val custom: Any? = null,
    val status: String? = null
)

internal data class UUIDId(val id: String)
