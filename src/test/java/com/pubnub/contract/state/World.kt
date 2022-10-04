package com.pubnub.contract.state

import com.pubnub.contract.CONTRACT_TEST_CONFIG
import com.pubnub.api.PNConfiguration
import com.pubnub.api.PubNub
import com.pubnub.api.PubNubException
import com.pubnub.api.enums.PNLogVerbosity

class World {
    val configuration: PNConfiguration by lazy { PNConfiguration(PubNub.generateUUID()).apply {
        origin = CONTRACT_TEST_CONFIG.serverHostPort()
        isSecure = false
        logVerbosity = PNLogVerbosity.BODY
    } }
    val pubnub: PubNub by lazy { PubNub(configuration) }
    var pnException: PubNubException? = null
    var tokenString: String? = null
    var responseStatus: Int? = null
}
