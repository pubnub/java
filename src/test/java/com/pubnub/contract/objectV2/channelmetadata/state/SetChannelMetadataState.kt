package com.pubnub.contract.objectV2.channelmetadata.state

import com.pubnub.api.models.consumer.objects_api.channel.PNChannelMetadata
import com.pubnub.api.models.consumer.objects_api.channel.PNSetChannelMetadataResult

class SetChannelMetadataState {
    var id: String? = null
    var pnChannelMetadata: PNChannelMetadata? = null
    var result: PNSetChannelMetadataResult? = null
}
