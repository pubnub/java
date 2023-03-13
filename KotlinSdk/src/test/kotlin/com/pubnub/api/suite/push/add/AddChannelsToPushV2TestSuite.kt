package com.pubnub.apikt.suite.push.add

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.pubnub.apikt.endpoints.push.AddChannelsToPush
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.enums.PNPushType
import com.pubnub.apikt.models.consumer.push.PNPushAddChannelResult
import com.pubnub.apikt.suite.AUTH
import com.pubnub.apikt.suite.EndpointTestSuite
import com.pubnub.apikt.suite.SUB

class AddChannelsToPushV2TestSuite : EndpointTestSuite<AddChannelsToPush, PNPushAddChannelResult>() {

    override fun telemetryParamName() = "l_push"

    override fun pnOperation() = PNOperationType.PNAddPushNotificationsOnChannelsOperation

    override fun requiredKeys() = SUB + AUTH

    override fun snippet(): AddChannelsToPush {
        return pubnub.addPushNotificationsOnChannels(
            pushType = PNPushType.APNS2,
            channels = listOf("ch1", "ch2"),
            deviceId = "12345",
            topic = "news"
        )
    }

    override fun verifyResultExpectations(result: PNPushAddChannelResult) {
    }

    override fun successfulResponseBody(): String {
        return """[1, "Modified Channels"]"""
    }

    override fun unsuccessfulResponseBodyList() = emptyList<String>()

    override fun mappingBuilder() =
        get(urlPathEqualTo("/v2/push/sub-key/mySubscribeKey/devices-apns2/12345"))
            .withQueryParam("type", absent())
            .withQueryParam("add", equalTo("ch1,ch2"))
            .withQueryParam("environment", equalTo("development"))
            .withQueryParam("topic", equalTo("news"))

    override fun affectedChannelsAndGroups() = listOf("ch1", "ch2") to emptyList<String>()

    override fun voidResponse() = true
}
