package com.pubnub.apikt.models.consumer.push.payload

interface PushPayloadSerializer {

    fun toMap(): Map<String, Any>
}
