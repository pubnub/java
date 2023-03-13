package com.pubnub.contract.access.state

import com.pubnub.apikt.models.consumer.access_manager.v3.PNGrant
import com.pubnub.apikt.models.consumer.access_manager.v3.PNGrantTokenResult
import com.pubnub.apikt.models.consumer.access_manager.v3.PNToken

class GrantTokenState {
    var parsedToken: PNToken? = null
    var TTL: Long? = null
    var result: PNGrantTokenResult? = null
    var authorizedUUID: String? = null
    val definedGrants: MutableList<FutureCallGrant> = mutableListOf()
    var currentGrant: FutureCallGrant? = null
        set(value) {
            if (value != null) definedGrants.add(value)
            field = value
        }
    var currentResourcePermissions: PNToken.PNResourcePermissions? = null
}

class FutureCallGrant(
    private val initGrant: PNGrant
) {
    private val actions: MutableList<(PNGrant) -> PNGrant> = mutableListOf()

    fun addAction(action: (PNGrant) -> PNGrant): FutureCallGrant {
        actions.add(action)
        return this
    }

    fun evaluate(): PNGrant {
        return actions.fold(initGrant) { acc, act ->
            act(acc)
        }
    }
}
