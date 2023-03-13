package com.pubnub.apikt.suite.message_actions

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.pubnub.apikt.endpoints.message_actions.GetMessageActions
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.PNBoundedPage
import com.pubnub.apikt.models.consumer.message_actions.PNGetMessageActionsResult
import com.pubnub.apikt.suite.AUTH
import com.pubnub.apikt.suite.EndpointTestSuite
import com.pubnub.apikt.suite.SUB
import org.junit.Assert.assertEquals

class GetMessageActionsMultipleTestSuite : EndpointTestSuite<GetMessageActions, PNGetMessageActionsResult>() {

    override fun telemetryParamName() = "l_msga"

    override fun pnOperation() = PNOperationType.PNGetMessageActions

    override fun requiredKeys() = SUB + AUTH

    override fun snippet(): GetMessageActions =
        pubnub.getMessageActions(
            channel = "ch1",
            page = PNBoundedPage(
                start = 991,
                end = 969,
                limit = 5
            )
        )

    override fun successfulResponseBody() = """
        {
         "status": 200,
         "data": [
          {
           "messageTimetoken": "100",
           "type": "reaction",
           "uuid": "abc",
           "value": "😀",
           "actionTimetoken": "970"
          },
          {
           "messageTimetoken": "100",
           "type": "reaction",
           "uuid": "abc",
           "value": "😬",
           "actionTimetoken": "980"
          },
          {
           "messageTimetoken": "200",
           "type": "action",
           "uuid": "xyz",
           "value": "😀",
           "actionTimetoken": "990"
          }
         ]
        }
    """.trimIndent()

    override fun unsuccessfulResponseBodyList() = listOf(
        """{"status":200,"data":null}""",
        """{"status":200}"""
    )

    override fun verifyResultExpectations(result: PNGetMessageActionsResult) {
        assertEquals(3, result.actions.size)
        assertEquals(2, result.actions.filter { it.uuid == "abc" }.size)
        assertEquals(2, result.actions.filter { it.value == "\uD83D\uDE00" }.size)
        assertEquals(2, result.actions.filter { it.messageTimetoken == 100L }.size)
    }

    override fun mappingBuilder() =
        get(urlPathEqualTo("/v1/message-actions/mySubscribeKey/channel/ch1"))
            .withQueryParam("start", equalTo("991"))
            .withQueryParam("end", equalTo("969"))
            .withQueryParam("limit", equalTo("5"))

    override fun affectedChannelsAndGroups() = listOf("ch1") to emptyList<String>()
}
