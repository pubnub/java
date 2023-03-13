package com.pubnub.apikt.models.consumer.pubsub.objects

import com.google.gson.JsonElement

data class ObjectPayload(
    val source: String,
    val version: String,
    val event: String,
    val type: String,
    val data: JsonElement
)
