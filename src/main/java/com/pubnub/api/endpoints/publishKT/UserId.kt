package com.pubnub.api.endpoints.publishKT

import com.pubnub.api.endpoints.publishKT.PNConfiguration.Companion.isValid

data class UserId(val value: String) {
    init {
        PubNubUtil.require(value.isValid(), PubNubError.USERID_NULL_OR_EMPTY)
    }
}
