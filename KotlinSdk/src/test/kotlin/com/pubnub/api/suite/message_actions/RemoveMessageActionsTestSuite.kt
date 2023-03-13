package com.pubnub.apikt.suite.message_actions

import com.github.tomakehurst.wiremock.client.WireMock.delete
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.pubnub.apikt.endpoints.message_actions.RemoveMessageAction
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.message_actions.PNRemoveMessageActionResult
import com.pubnub.apikt.suite.AUTH
import com.pubnub.apikt.suite.EndpointTestSuite
import com.pubnub.apikt.suite.SUB

class RemoveMessageActionsTestSuite : EndpointTestSuite<RemoveMessageAction, PNRemoveMessageActionResult>() {

    override fun telemetryParamName() = "l_msga"

    override fun pnOperation() = PNOperationType.PNDeleteMessageAction

    override fun requiredKeys() = SUB + AUTH

    override fun snippet(): RemoveMessageAction {
        return pubnub.removeMessageAction(
            channel = "ch1",
            messageTimetoken = 100,
            actionTimetoken = 200
        )
    }

    override fun successfulResponseBody() = """
        {
         "status": 200,
         "data": {}
        }
    """.trimIndent()

    override fun unsuccessfulResponseBodyList() = emptyList<String>()

    override fun verifyResultExpectations(result: PNRemoveMessageActionResult) {
    }

    override fun mappingBuilder() =
        delete(urlPathEqualTo("/v1/message-actions/mySubscribeKey/channel/ch1/message/100/action/200"))

    override fun affectedChannelsAndGroups() = listOf("ch1") to emptyList<String>()

    override fun voidResponse() = true
}
