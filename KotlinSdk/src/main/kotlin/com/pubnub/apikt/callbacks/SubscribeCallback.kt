package com.pubnub.apikt.callbacks

import com.pubnub.apikt.PubNub
import com.pubnub.apikt.enums.PNStatusCategory
import com.pubnub.apikt.models.consumer.PNStatus
import com.pubnub.apikt.models.consumer.pubsub.PNMessageResult
import com.pubnub.apikt.models.consumer.pubsub.PNPresenceEventResult
import com.pubnub.apikt.models.consumer.pubsub.PNSignalResult
import com.pubnub.apikt.models.consumer.pubsub.files.PNFileEventResult
import com.pubnub.apikt.models.consumer.pubsub.message_actions.PNMessageActionResult
import com.pubnub.apikt.models.consumer.pubsub.objects.PNObjectEventResult

interface Listener

interface Disposable {
    fun dispose()
}

class DisposableListener(
    private val pubNub: PubNub,
    private val listener: Listener
) : Disposable, Listener by listener {
    override fun dispose() {
        pubNub.removeListener(listener)
    }
}

abstract class SubscribeCallback : Listener {

    /**
     * Receive status events like
     * [PNStatusCategory.PNAcknowledgmentCategory],
     * [PNStatusCategory.PNConnectedCategory],
     * [PNStatusCategory.PNReconnectedCategory]
     *
     * and other events related to the subscribe loop and channel mix.
     *
     * @param pubnub The client instance which has this listener attached.
     * @param pnStatus API operation metadata.
     */
    abstract fun status(pubnub: PubNub, pnStatus: PNStatus)

    /**
     * Receive messages at subscribed channels.
     *
     * @see [PubNub.subscribe]
     *
     * @param pubnub The client instance which has this listener attached.
     * @param pnMessageResult Wrapper around the actual message content.
     */
    open fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {}

    /**
     * Receive presence events for channels subscribed to with presence enabled via `withPresence = true` in
     * [PubNub.subscribe]
     *
     * @param pubnub The client instance which has this listener attached.
     * @param pnPresenceEventResult Wrapper around a presence event.
     */
    open fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {}

    /**
     * Receive signals at subscribed channels.
     *
     * @see [PubNub.signal]
     *
     * @param pubnub The client instance which has this listener attached.
     * @param pnSignalResult Wrapper around a signal event.
     */
    open fun signal(pubnub: PubNub, pnSignalResult: PNSignalResult) {}

    /**
     * Receive message actions for messages in subscribed channels.
     *
     * @param pubnub The client instance which has this listener attached.
     * @param pnMessageActionResult Wrapper around a message action event.
     */
    open fun messageAction(pubnub: PubNub, pnMessageActionResult: PNMessageActionResult) {}

    open fun objects(pubnub: PubNub, objectEvent: PNObjectEventResult) {}
    open fun file(pubnub: PubNub, pnFileEventResult: PNFileEventResult) {}
}
