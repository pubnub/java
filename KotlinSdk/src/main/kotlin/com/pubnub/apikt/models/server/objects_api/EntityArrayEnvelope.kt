package com.pubnub.apikt.models.server.objects_api

data class EntityArrayEnvelope<T>(
    val status: Int = 0,
    val data: Collection<T> = listOf(),
    val totalCount: Int? = null,
    val next: String? = null,
    val prev: String? = null
)
