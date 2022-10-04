package com.pubnub.contract.objectV2.state

import com.pubnub.api.models.consumer.objects_api.uuid.PNSetUUIDMetadataResult
import com.pubnub.api.models.consumer.objects_api.uuid.PNUUIDMetadata

class SetUUIDMetadataState {
    var id: String? = null
    var pnUUIDMetadata: PNUUIDMetadata = PNUUIDMetadata()
    var result: PNSetUUIDMetadataResult? = null
}