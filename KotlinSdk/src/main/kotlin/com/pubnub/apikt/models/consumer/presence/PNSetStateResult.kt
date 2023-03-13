package com.pubnub.apikt.models.consumer.presence

import com.google.gson.JsonElement
import com.pubnub.apikt.PubNub

/**
 * Result of the [PubNub.setPresenceState] operation.
 *
 * @property state The actual state object.
 */
class PNSetStateResult internal constructor(
    val state: JsonElement
)
