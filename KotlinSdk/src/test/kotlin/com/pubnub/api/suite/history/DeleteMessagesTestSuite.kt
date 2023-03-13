package com.pubnub.apikt.suite.history

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.pubnub.apikt.endpoints.DeleteMessages
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.history.PNDeleteMessagesResult
import com.pubnub.apikt.suite.AUTH
import com.pubnub.apikt.suite.EndpointTestSuite
import com.pubnub.apikt.suite.SUB

class DeleteMessagesTestSuite : EndpointTestSuite<DeleteMessages, PNDeleteMessagesResult>() {

    override fun telemetryParamName() = "l_hist"

    override fun pnOperation() = PNOperationType.PNDeleteMessagesOperation

    override fun requiredKeys() = SUB + AUTH

    override fun snippet(): DeleteMessages =
        pubnub.deleteMessages(
            channels = listOf("ch1")
        )

    override fun verifyResultExpectations(result: PNDeleteMessagesResult) {
    }

    override fun successfulResponseBody() = """
        {
         "status": 200,
         "error": false,
         "error_message": ""
        }
    """.trimIndent()

    override fun unsuccessfulResponseBodyList() = emptyList<String>()

    override fun mappingBuilder(): MappingBuilder {
        return delete(urlPathEqualTo("/v3/history/sub-key/mySubscribeKey/channel/ch1"))
            .withQueryParam("start", absent())
            .withQueryParam("end", absent())
    }

    override fun affectedChannelsAndGroups() = emptyList<String>() to emptyList<String>()

    override fun voidResponse() = true
}
