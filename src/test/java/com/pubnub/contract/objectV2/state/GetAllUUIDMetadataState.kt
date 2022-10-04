package com.pubnub.contract.objectV2.state

import com.pubnub.api.models.consumer.objects_api.uuid.PNGetAllUUIDMetadataResult
import com.pubnub.api.models.consumer.objects_api.uuid.PNUUIDMetadata

class GetAllUUIDMetadataState {
    var id: String? = null
    var pnUUIDMetadataList: List<PNUUIDMetadata>? = null
    var result: PNGetAllUUIDMetadataResult? = null
}