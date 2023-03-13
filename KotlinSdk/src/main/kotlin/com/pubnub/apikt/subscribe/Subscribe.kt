@file:Suppress("UNUSED_PARAMETER")

package com.pubnub.apikt.subscribe

object Subscribe {
    fun subscribe(
        channels: List<String>,
        channelGroups: List<String>,
        withPresence: Boolean,
        withTimetoken: Long
    ) {
        TODO("Not yet implemented")
    }

    fun unsubscribe(
        channels: List<String> = emptyList(),
        channelGroups: List<String> = emptyList()
    ) {
        TODO("Not yet implemented")
    }

    fun unsubscribeAll() {
        TODO("Not yet implemented")
    }

    fun getSubscribedChannels(): List<String> {
        TODO("Not yet implemented")
    }

    fun getSubscribedChannelGroups(): List<String> {
        TODO("Not yet implemented")
    }
}
