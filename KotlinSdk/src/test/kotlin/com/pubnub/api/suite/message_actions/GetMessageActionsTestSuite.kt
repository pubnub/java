package com.pubnub.apikt.suite.message_actions

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.pubnub.apikt.endpoints.message_actions.GetMessageActions
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.message_actions.PNGetMessageActionsResult
import com.pubnub.apikt.suite.AUTH
import com.pubnub.apikt.suite.EndpointTestSuite
import com.pubnub.apikt.suite.SUB
import org.junit.Assert.assertEquals

class GetMessageActionsTestSuite : EndpointTestSuite<GetMessageActions, PNGetMessageActionsResult>() {

    override fun telemetryParamName() = "l_msga"

    override fun pnOperation() = PNOperationType.PNGetMessageActions

    override fun requiredKeys() = SUB + AUTH

    override fun snippet(): GetMessageActions =
        pubnub.getMessageActions(
            channel = "ch1"
        )

    override fun successfulResponseBody() = """
        {
          "status": 200,
          "data": [
            {
              "messageTimetoken": "100",
              "type": "reaction",
              "uuid": "abc",
              "value": "smiley",
              "actionTimetoken": "200"
            }
          ]
        }
    """.trimIndent()

    override fun unsuccessfulResponseBodyList() = listOf(
        """{"status":200,"data":null}""",
        """{"status":200}"""
    )

    override fun verifyResultExpectations(result: PNGetMessageActionsResult) {
        assertEquals(1, result.actions.size)
        result.actions[0].apply {
            assertEquals(100L, messageTimetoken)
            assertEquals("reaction", type)
            assertEquals("abc", uuid)
            assertEquals("smiley", value)
            assertEquals(200L, actionTimetoken)
        }
    }

    override fun mappingBuilder() =
        get(urlPathEqualTo("/v1/message-actions/mySubscribeKey/channel/ch1"))
            .withQueryParam("start", absent())
            .withQueryParam("end", absent())
            .withQueryParam("limit", absent())

    override fun affectedChannelsAndGroups() = listOf("ch1") to emptyList<String>()
}
