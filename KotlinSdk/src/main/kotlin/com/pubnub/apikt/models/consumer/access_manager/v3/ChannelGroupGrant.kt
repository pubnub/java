package com.pubnub.apikt.models.consumer.access_manager.v3

interface ChannelGroupGrant : PNGrant {
    companion object {
        fun id(
            id: String,
            read: Boolean = false,
            manage: Boolean = false
        ): ChannelGroupGrant = PNChannelGroupResourceGrant(
            id = id,
            read = read,
            manage = manage
        )

        fun pattern(
            pattern: String,
            read: Boolean = false,
            manage: Boolean = false
        ): ChannelGroupGrant = PNChannelGroupPatternGrant(
            id = pattern,
            read = read,
            manage = manage
        )
    }
}
