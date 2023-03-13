package com.pubnub.apikt.suite.push.remove

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.pubnub.apikt.endpoints.push.RemoveChannelsFromPush
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.enums.PNPushType
import com.pubnub.apikt.models.consumer.push.PNPushRemoveChannelResult
import com.pubnub.apikt.suite.AUTH
import com.pubnub.apikt.suite.EndpointTestSuite
import com.pubnub.apikt.suite.SUB

class RemoveChannelsFromPushV2TestSuite : EndpointTestSuite<RemoveChannelsFromPush, PNPushRemoveChannelResult>() {

    override fun telemetryParamName() = "l_push"

    override fun pnOperation() = PNOperationType.PNRemovePushNotificationsFromChannelsOperation

    override fun requiredKeys() = SUB + AUTH

    override fun snippet(): RemoveChannelsFromPush {
        return pubnub.removePushNotificationsFromChannels(
            pushType = PNPushType.APNS2,
            channels = listOf("ch1", "ch2"),
            deviceId = "12345",
            topic = "news"
        )
    }

    override fun verifyResultExpectations(result: PNPushRemoveChannelResult) {
    }

    override fun successfulResponseBody(): String {
        return """[1, "Modified Channels"]"""
    }

    override fun unsuccessfulResponseBodyList() = emptyList<String>()

    override fun mappingBuilder() =
        get(urlPathEqualTo("/v2/push/sub-key/mySubscribeKey/devices-apns2/12345"))
            .withQueryParam("type", absent())
            .withQueryParam("remove", equalTo("ch1,ch2"))
            .withQueryParam("environment", equalTo("development"))
            .withQueryParam("topic", equalTo("news"))

    override fun affectedChannelsAndGroups() = listOf("ch1", "ch2") to emptyList<String>()

    override fun voidResponse() = true
}
