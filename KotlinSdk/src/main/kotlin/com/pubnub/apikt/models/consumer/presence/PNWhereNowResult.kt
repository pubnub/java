package com.pubnub.apikt.models.consumer.presence

import com.pubnub.apikt.PubNub

/**
 * Result of the [PubNub.whereNow] operation.
 *
 * @property channels List of channels where a UUID is present.
 */
class PNWhereNowResult internal constructor(
    val channels: List<String>
)
