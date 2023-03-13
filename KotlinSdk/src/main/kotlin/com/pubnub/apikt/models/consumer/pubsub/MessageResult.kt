package com.pubnub.apikt.models.consumer.pubsub

import com.google.gson.JsonElement

/**
 * @property message The actual message content
 */
interface MessageResult : PubSubResult {
    val message: JsonElement
}
