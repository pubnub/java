package com.pubnub.apikt.models.consumer

import com.pubnub.apikt.PubNub

/**
 * Result of the [PubNub.time] operation.
 *
 * @property timetoken Current time token.
 */
class PNTimeResult internal constructor(
    val timetoken: Long
)
