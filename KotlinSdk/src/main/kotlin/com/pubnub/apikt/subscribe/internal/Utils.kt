package com.pubnub.apikt.subscribe.internal

import com.pubnub.apikt.PubNub
import com.pubnub.apikt.endpoints.pubsub.Subscribe
import com.pubnub.apikt.endpoints.remoteaction.Cancelable
import com.pubnub.apikt.models.consumer.PNStatus
import com.pubnub.apikt.models.server.SubscribeEnvelope

internal fun PubNub.handshake(
    channels: List<String>,
    channelGroups: List<String>,
    callback: (result: SubscribeEnvelope?, status: PNStatus) -> Unit
): Cancelable {
    return Subscribe(this).also {
        it.channels = channels
        it.channelGroups = channelGroups
        it.timetoken = 0
        it.async(callback)
    }
}

internal fun PubNub.receiveMessages(
    channels: List<String>,
    channelGroups: List<String>,
    timetoken: Long,
    region: String,
    callback: (result: SubscribeEnvelope?, status: PNStatus) -> Unit
): Cancelable {
    return Subscribe(this).also {
        it.channels = channels
        it.channelGroups = channelGroups
        it.timetoken = timetoken
        it.region = region
        it.filterExpression = this.configuration.filterExpression.ifBlank { null }
        it.async(callback)
    }
}
