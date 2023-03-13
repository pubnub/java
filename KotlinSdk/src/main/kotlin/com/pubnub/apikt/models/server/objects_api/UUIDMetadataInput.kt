package com.pubnub.apikt.models.server.objects_api

internal data class UUIDMetadataInput(
    val name: String? = null,
    val externalId: String? = null,
    val profileUrl: String? = null,
    val email: String? = null,
    val custom: Any? = null,
    val type: String? = null,
    val status: String? = null
)
