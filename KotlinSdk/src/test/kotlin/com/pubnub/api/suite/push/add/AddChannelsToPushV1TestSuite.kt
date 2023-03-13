package com.pubnub.apikt.suite.push.add

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.endpoints.push.AddChannelsToPush
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.enums.PNPushType
import com.pubnub.apikt.models.consumer.PNStatus
import com.pubnub.apikt.models.consumer.push.PNPushAddChannelResult
import com.pubnub.apikt.suite.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class AddChannelsToPushV1TestSuite : EndpointTestSuite<AddChannelsToPush, PNPushAddChannelResult>() {

    override fun telemetryParamName() = "l_push"

    override fun pnOperation() = PNOperationType.PNAddPushNotificationsOnChannelsOperation

    override fun requiredKeys() = SUB + AUTH

    override fun snippet(): AddChannelsToPush {
        return pubnub.addPushNotificationsOnChannels(
            pushType = PNPushType.FCM,
            channels = listOf("ch1", "ch2"),
            deviceId = "12345"
        )
    }

    override fun verifyResultExpectations(result: PNPushAddChannelResult) {
    }

    override fun successfulResponseBody(): String {
        return """[1, "Modified Channels"]"""
    }

    override fun unsuccessfulResponseBodyList() = emptyList<String>()

    override fun mappingBuilder() =
        get(urlPathEqualTo("/v1/push/sub-key/mySubscribeKey/devices/12345"))
            .withQueryParam("type", equalTo("gcm"))
            .withQueryParam("add", equalTo("ch1,ch2"))
            .withQueryParam("environment", absent())
            .withQueryParam("topic", absent())

    override fun affectedChannelsAndGroups() = listOf("ch1", "ch2") to emptyList<String>()

    override fun voidResponse() = true

    override fun optionalScenarioList(): List<OptionalScenario<PNPushAddChannelResult>> {
        return listOf(
            OptionalScenario<PNPushAddChannelResult>().apply {
                val body = "{\"error\":\"Use of the mobile push notifications API requires Push Notifications" +
                    " which is not enabled for this subscribe key. Login to your PubNub Dashboard Account" +
                    " and enable Push Notifications. " +
                    "Contact support@pubnub.com if you require further assistance.\"}"
                result = Result.FAIL
                responseBuilder = { withBody(body).withStatus(400) }
                pnError = PubNubError.HTTP_ERROR
                additionalChecks = { status: PNStatus, _: PNPushAddChannelResult? ->
                    assertTrue(voidResponse())
                    assertEquals(body, status.exception!!.jso)
                }
            }
        )
    }
}
