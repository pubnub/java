package com.pubnub.apikt.models.consumer.objects.membership

import com.pubnub.apikt.models.consumer.objects.channel.PNChannelMetadata

data class PNChannelMembership(
    val channel: PNChannelMetadata?,
    val custom: Any?,
    val updated: String,
    val eTag: String,
    val status: String?
) {
    data class Partial(
        val channelId: String,
        override val custom: Any? = null,
        override val status: String? = null
    ) : ChannelMembershipInput {
        override val channel: String = channelId
    }
}
