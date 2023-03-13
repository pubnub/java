package com.pubnub.apikt.models.consumer.presence

import com.google.gson.JsonElement
import com.pubnub.apikt.PubNub

/**
 * Result of the [PubNub.getPresenceState] operation.
 *
 * @property stateByUUID Map of UUIDs and the user states.
 */
class PNGetStateResult internal constructor(
    val stateByUUID: Map<String, JsonElement>
)
