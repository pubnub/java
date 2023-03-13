package com.pubnub.apikt.suite.presence

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.pubnub.apikt.endpoints.presence.Leave
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.suite.AUTH
import com.pubnub.apikt.suite.EndpointTestSuite
import com.pubnub.apikt.suite.SUB
import org.junit.Assert.assertTrue

class LeaveTestSuite : EndpointTestSuite<Leave, Boolean>() {

    override fun telemetryParamName() = "l_pres"

    override fun pnOperation() = PNOperationType.PNUnsubscribeOperation

    override fun requiredKeys() = SUB + AUTH

    override fun snippet(): Leave {
        return Leave(pubnub).apply {
            channels = listOf("ch1")
        }
    }

    override fun verifyResultExpectations(result: Boolean) {
        assertTrue(result)
    }

    override fun successfulResponseBody() = """
        {  
            "status": 200,
            "message": "OK",  
            "service": "Presence"
        }
    """.trimIndent()

    override fun unsuccessfulResponseBodyList() = emptyList<String>()

    override fun mappingBuilder(): MappingBuilder =
        get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1/leave"))

    override fun affectedChannelsAndGroups() = listOf("ch1") to emptyList<String>()

    override fun voidResponse() = true
}
