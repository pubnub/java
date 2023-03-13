package com.pubnub.contract.channelmetadata.state

import com.pubnub.apikt.models.consumer.objects.channel.PNChannelMetadata

class ChannelMetadataState {
    var channelMetadatas: Collection<PNChannelMetadata>? = null
    var channelMetadata: PNChannelMetadata? = null
    lateinit var channelId: String
    var name: String? = null
}
