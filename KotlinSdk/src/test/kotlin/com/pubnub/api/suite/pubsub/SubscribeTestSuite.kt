package com.pubnub.apikt.suite.pubsub

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.pubnub.apikt.endpoints.pubsub.Subscribe
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.server.SubscribeEnvelope
import com.pubnub.apikt.suite.AUTH
import com.pubnub.apikt.suite.EndpointTestSuite
import com.pubnub.apikt.suite.SUB
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class SubscribeTestSuite : EndpointTestSuite<Subscribe, SubscribeEnvelope>() {

    override fun telemetryParamName() = ""

    override fun pnOperation() = PNOperationType.PNSubscribeOperation

    override fun requiredKeys() = SUB + AUTH

    override fun snippet(): Subscribe {
        return Subscribe(pubnub).apply {
            channels = listOf("ch1")
        }
    }

    override fun verifyResultExpectations(result: SubscribeEnvelope) {
        assertEquals(100, result.metadata.timetoken)
        assertEquals("1", result.metadata.region)
        assertTrue(result.messages.isEmpty())
    }

    override fun successfulResponseBody() = """
        {
          "t": {
            "t": "100",
            "r": 1
          },
          "m": []
        }
    """.trimIndent()

    override fun unsuccessfulResponseBodyList() = emptyList<String>()

    override fun mappingBuilder() =
        get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch1/0"))
            .withQueryParam("tt", absent())
            .withQueryParam("tr", absent())
            .withQueryParam("filter-expr", absent())
            .withQueryParam("state", absent())
            .withQueryParam("channel-group", absent())
            .withQueryParam("heartbeat", equalTo("300"))

    override fun affectedChannelsAndGroups() = listOf("ch1") to emptyList<String>()
}
