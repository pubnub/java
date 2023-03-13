package com.pubnub.apikt

import com.pubnub.apikt.PNConfiguration.Companion.isValid

data class UserId(val value: String) {
    init {
        PubNubUtil.require(value.isValid(), PubNubError.USERID_NULL_OR_EMPTY)
    }
}
