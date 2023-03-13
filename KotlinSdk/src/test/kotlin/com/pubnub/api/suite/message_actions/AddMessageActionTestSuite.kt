package com.pubnub.apikt.suite.message_actions

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.pubnub.apikt.endpoints.message_actions.AddMessageAction
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.message_actions.PNAddMessageActionResult
import com.pubnub.apikt.models.consumer.message_actions.PNMessageAction
import com.pubnub.apikt.suite.AUTH
import com.pubnub.apikt.suite.EndpointTestSuite
import com.pubnub.apikt.suite.SUB
import org.junit.Assert.assertEquals

class AddMessageActionTestSuite : EndpointTestSuite<AddMessageAction, PNAddMessageActionResult>() {

    override fun telemetryParamName() = "l_msga"

    override fun pnOperation() = PNOperationType.PNAddMessageAction

    override fun requiredKeys() = SUB + AUTH

    override fun snippet(): AddMessageAction =
        pubnub.addMessageAction(
            channel = "ch1",
            messageAction = PNMessageAction(
                type = "reaction",
                value = "smiley",
                messageTimetoken = 1000
            )
        )

    override fun successfulResponseBody() = """
            {
             "status": 200,
             "data": {
              "messageTimetoken": "123",
              "type": "reaction",
              "uuid": "someUuid",
              "value": "smiley",
              "actionTimetoken": "1000"
             }
            }
    """.trimIndent()

    override fun unsuccessfulResponseBodyList() = listOf(
        """{"status":200,"data":null}""",
        """{"status":200}"""
    )

    override fun verifyResultExpectations(result: PNAddMessageActionResult) {
        assertEquals(123L, result.messageTimetoken)
        assertEquals("reaction", result.type)
        assertEquals("someUuid", result.uuid)
        assertEquals("smiley", result.value)
        assertEquals(1000L, result.actionTimetoken)
    }

    override fun mappingBuilder() =
        post(urlMatching("/v1/message-actions/mySubscribeKey/channel/ch1/message/1000.*"))
            .withRequestBody(
                equalToJson("""{"type":"reaction","value":"smiley"}""")
            )

    override fun affectedChannelsAndGroups() = listOf("ch1") to emptyList<String>()
}
