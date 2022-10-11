package com.pubnub.contract.objectV2.uuidmetadata.state

import com.pubnub.api.models.consumer.objects_api.uuid.PNGetUUIDMetadataResult
import com.pubnub.api.models.consumer.objects_api.uuid.PNUUIDMetadata

class GetUUIDMetadataState {
    var id: String? = null
    var pnUUIDMetadata: PNUUIDMetadata? = null
    var result: PNGetUUIDMetadataResult? = null
}