package com.pubnub.apikt.managers

import com.pubnub.apikt.PubNub
import com.pubnub.apikt.callbacks.Listener
import com.pubnub.apikt.callbacks.SubscribeCallback
import com.pubnub.apikt.models.consumer.PNStatus
import com.pubnub.apikt.models.consumer.pubsub.PNMessageResult
import com.pubnub.apikt.models.consumer.pubsub.PNPresenceEventResult
import com.pubnub.apikt.models.consumer.pubsub.PNSignalResult
import com.pubnub.apikt.models.consumer.pubsub.files.PNFileEventResult
import com.pubnub.apikt.models.consumer.pubsub.message_actions.PNMessageActionResult
import com.pubnub.apikt.models.consumer.pubsub.objects.PNObjectEventResult

internal class ListenerManager(val pubnub: PubNub) {
    private val listeners = mutableListOf<SubscribeCallback>()

    fun addListener(listener: SubscribeCallback) {
        synchronized(listeners) {
            listeners.add(listener)
        }
    }

    fun removeListener(listener: Listener) {
        synchronized(listeners) {
            listeners.remove(listener)
        }
    }

    private fun getListeners(): List<SubscribeCallback> {
        val tempCallbackList = ArrayList<SubscribeCallback>()
        synchronized(listeners) {
            tempCallbackList.addAll(listeners)
        }
        return tempCallbackList
    }

    fun announce(status: PNStatus) {
        getListeners().forEach { it.status(pubnub, status) }
    }

    fun announce(message: PNMessageResult) {
        getListeners().forEach { it.message(pubnub, message) }
    }

    fun announce(presence: PNPresenceEventResult) {
        getListeners().forEach { it.presence(pubnub, presence) }
    }

    fun announce(signal: PNSignalResult) {
        getListeners().forEach { it.signal(pubnub, signal) }
    }

    fun announce(messageAction: PNMessageActionResult) {
        getListeners().forEach { it.messageAction(pubnub, messageAction) }
    }

    fun announce(pnObjectEventResult: PNObjectEventResult) {
        getListeners().forEach { it.objects(pubnub, pnObjectEventResult) }
    }

    fun announce(pnFileEventResult: PNFileEventResult) {
        getListeners().forEach { it.file(pubnub, pnFileEventResult) }
    }
}
