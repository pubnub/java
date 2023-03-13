package com.pubnub.apikt.models.consumer.push

import com.pubnub.apikt.PubNub

/**
 * Result of [PubNub.addPushNotificationsOnChannels] operation.
 */
class PNPushAddChannelResult

class PNPushListProvisionsResult internal constructor(
    val channels: List<String>
)

class PNPushRemoveAllChannelsResult

class PNPushRemoveChannelResult
