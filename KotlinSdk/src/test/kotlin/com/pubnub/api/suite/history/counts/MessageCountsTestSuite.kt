package com.pubnub.apikt.suite.history.counts

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.pubnub.apikt.endpoints.MessageCounts
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.history.PNMessageCountResult
import com.pubnub.apikt.suite.AUTH
import com.pubnub.apikt.suite.EndpointTestSuite
import com.pubnub.apikt.suite.SUB
import org.junit.Assert.assertEquals

class MessageCountsTestSuite : EndpointTestSuite<MessageCounts, PNMessageCountResult>() {

    override fun telemetryParamName() = "l_mc"

    override fun pnOperation() = PNOperationType.PNMessageCountOperation

    override fun requiredKeys() = SUB + AUTH

    override fun snippet(): MessageCounts =
        pubnub.messageCounts(
            channels = listOf("ch1"),
            channelsTimetoken = listOf(1588284000000)
        )

    override fun verifyResultExpectations(result: PNMessageCountResult) {
        assertEquals(1, result.channels.keys.size)
        assertEquals(5L, result.channels["ch1"])
    }

    override fun successfulResponseBody() = """
        {
         "status": 200,
         "error": false,
         "error_message": "",
         "channels": {
          "ch1": 5
         }
        }
    """.trimIndent()

    override fun unsuccessfulResponseBodyList() = emptyList<String>()

    override fun mappingBuilder(): MappingBuilder {
        return get(urlPathEqualTo("/v3/history/sub-key/mySubscribeKey/message-counts/ch1"))
            .withQueryParam("timetoken", equalTo("1588284000000"))
    }

    override fun affectedChannelsAndGroups() = listOf("ch1") to emptyList<String>()
}
