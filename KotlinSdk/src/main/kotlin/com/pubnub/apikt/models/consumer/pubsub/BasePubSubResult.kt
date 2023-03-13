package com.pubnub.apikt.models.consumer.pubsub

import com.google.gson.JsonElement

interface PubSubResult : PNEvent {
    val channel: String
    val subscription: String?
    val timetoken: Long?
    val userMetadata: JsonElement?
    val publisher: String?
}

/**
 * @property channel The channel a PubNub API operation is related to.
 * @property subscription The subscription a PubNub API operation is related to.
 * @property timetoken Timetoken of the PubNub API operation in question.
 * @property userMetadata User metadata if any.
 * @property publisher The publisher of the PubNub API operation in question.
 */
data class BasePubSubResult(
    override val channel: String,
    override val subscription: String?,
    override val timetoken: Long?,
    override val userMetadata: JsonElement?,
    override val publisher: String?
) : PubSubResult
