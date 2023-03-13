package com.pubnub.apikt.suite.presence

import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.pubnub.apikt.endpoints.presence.WhereNow
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.presence.PNWhereNowResult
import com.pubnub.apikt.suite.AUTH
import com.pubnub.apikt.suite.EndpointTestSuite
import com.pubnub.apikt.suite.SUB
import org.junit.Assert.assertEquals

class WhereNowTestSuite : EndpointTestSuite<WhereNow, PNWhereNowResult>() {

    override fun telemetryParamName() = "l_pres"

    override fun pnOperation() = PNOperationType.PNWhereNowOperation

    override fun requiredKeys() = SUB + AUTH

    override fun snippet(): WhereNow {
        return pubnub.whereNow().apply {
        }
    }

    override fun verifyResultExpectations(result: PNWhereNowResult) {
        assertEquals(1, result.channels.size)
        assertEquals("ch1", result.channels[0])
    }

    override fun successfulResponseBody() = """
        {
          "status": 200,
          "message": "OK",
          "payload": {
            "channels": [
              "ch1"
            ]
          },
          "service": "Presence"
        }
    """.trimIndent()

    override fun unsuccessfulResponseBodyList() = listOf(
        """{"payload":{"channels":null}}""",
        """{"payload": {}}""",
        """{"payload": null}"""
    )

    override fun mappingBuilder() =
        get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))

    override fun affectedChannelsAndGroups() = emptyList<String>() to emptyList<String>()
}
