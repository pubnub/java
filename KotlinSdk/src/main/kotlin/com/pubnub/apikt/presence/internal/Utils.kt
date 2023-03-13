package com.pubnub.apikt.presence.internal

import com.pubnub.apikt.PubNub
import com.pubnub.apikt.endpoints.presence.Heartbeat
import com.pubnub.apikt.endpoints.presence.Leave
import com.pubnub.apikt.endpoints.presence.SetState
import com.pubnub.apikt.endpoints.remoteaction.Cancelable
import com.pubnub.apikt.models.consumer.PNStatus
import com.pubnub.apikt.models.consumer.presence.PNSetStateResult

internal fun PubNub.iAmHere(
    channels: List<String>,
    channelGroups: List<String>,
    callback: (result: Boolean?, status: PNStatus) -> Unit
): Cancelable {
    return Heartbeat(
        this,
        channels = channels,
        channelGroups = channelGroups
    ).also {
        it.async(callback)
    }
}

internal fun PubNub.iAmAway(
    channels: List<String>,
    channelGroups: List<String>,
    callback: (result: Boolean?, status: PNStatus) -> Unit
): Cancelable {
    return Leave(this).also {
        it.channels = channels
        it.channelGroups = channelGroups
        it.async(callback)
    }
}

internal fun PubNub.setPresenceState(
    channels: List<String>,
    channelGroups: List<String>,
    state: Any,
    callback: (result: PNSetStateResult?, status: PNStatus) -> Unit
): Cancelable {
    return SetState(
        pubnub = this,
        channels = channels,
        channelGroups = channelGroups,
        state = state,
        uuid = this.configuration.userId.value
    ).also {
        it.async(callback)
    }
}
