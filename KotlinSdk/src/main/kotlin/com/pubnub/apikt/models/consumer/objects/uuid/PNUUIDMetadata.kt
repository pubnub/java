package com.pubnub.apikt.models.consumer.objects.uuid

data class PNUUIDMetadata(
    val id: String,
    val name: String?,
    val externalId: String?,
    val profileUrl: String?,
    val email: String?,
    val custom: Any?,
    val updated: String?,
    val eTag: String?,
    val type: String?,
    val status: String?
)
