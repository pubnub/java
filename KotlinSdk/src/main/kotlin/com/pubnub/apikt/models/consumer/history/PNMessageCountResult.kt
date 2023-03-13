package com.pubnub.apikt.models.consumer.history

import com.pubnub.apikt.PubNub

/**
 * Result of the [PubNub.messageCounts] operation.
 *
 * @property channels A map with values of Long for each channel. Channels without messages have a count of 0.
 * Channels with 10,000 messages or more have a count of `10000`.
 */
class PNMessageCountResult(
    val channels: Map<String, Long>
)
