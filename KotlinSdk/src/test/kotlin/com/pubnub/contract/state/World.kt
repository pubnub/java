package com.pubnub.contract.state

import com.pubnub.apikt.PNConfiguration
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.UserId
import com.pubnub.apikt.enums.PNLogVerbosity
import com.pubnub.contract.ContractTestConfig

class World {
    val configuration: PNConfiguration by lazy {
        PNConfiguration(userId = UserId(PubNub.generateUUID())).apply {
            origin = ContractTestConfig.serverHostPort
            secure = false
            logVerbosity = PNLogVerbosity.BODY
        }
    }

    val pubnub: PubNub by lazy { PubNub(configuration) }
    var pnException: PubNubException? = null
    var tokenString: String? = null
    var responseStatus: Int? = null
}
