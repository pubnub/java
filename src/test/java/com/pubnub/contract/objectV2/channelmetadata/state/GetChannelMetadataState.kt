package com.pubnub.contract.objectV2.channelmetadata.state

import com.pubnub.api.models.consumer.objects_api.channel.PNChannelMetadata

class GetChannelMetadataState {
    var id: String? = null
    var pnChannelMetadata: PNChannelMetadata? = null
    //response status is stored in World.kt so that we can have one common step "I receive a successful response"
}