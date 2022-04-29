package com.pubnub.contract.access.state

import com.pubnub.api.models.consumer.access_manager.v3.PNGrantTokenResult
import com.pubnub.api.models.consumer.access_manager.v3.PNResource
import com.pubnub.api.models.consumer.access_manager.v3.PNToken

class GrantTokenState {
    var parsedToken: PNToken? = null
    var TTL: Long? = null
    var result: PNGrantTokenResult? = null
    var authorizedUUID: String? = null
    var definedGrants: MutableList<PNResource<*>> = mutableListOf()
    var currentGrant: PNResource<*>? = null
        set(value) {
            if (value != null) definedGrants.add(value)
            field = value
        }
    var currentResourcePermissions: PNToken.PNResourcePermissions? = null
}
