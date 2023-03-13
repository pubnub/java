package com.pubnub.apikt.suite.push.remove

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.pubnub.apikt.endpoints.push.RemoveAllPushChannelsForDevice
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.enums.PNPushType
import com.pubnub.apikt.models.consumer.push.PNPushRemoveAllChannelsResult
import com.pubnub.apikt.suite.AUTH
import com.pubnub.apikt.suite.EndpointTestSuite
import com.pubnub.apikt.suite.SUB

class RemoveAllFromPushV2TestSuite :
    EndpointTestSuite<RemoveAllPushChannelsForDevice, PNPushRemoveAllChannelsResult>() {

    override fun telemetryParamName() = "l_push"

    override fun pnOperation() = PNOperationType.PNRemoveAllPushNotificationsOperation

    override fun requiredKeys() = SUB + AUTH

    override fun snippet(): RemoveAllPushChannelsForDevice {
        return pubnub.removeAllPushNotificationsFromDeviceWithPushToken(
            pushType = PNPushType.APNS2,
            deviceId = "12345",
            topic = "news"
        )
    }

    override fun verifyResultExpectations(result: PNPushRemoveAllChannelsResult) {
    }

    override fun successfulResponseBody(): String {
        return """[1, "Removed Device"]"""
    }

    override fun unsuccessfulResponseBodyList() = emptyList<String>()

    override fun mappingBuilder() =
        get(urlPathEqualTo("/v2/push/sub-key/mySubscribeKey/devices-apns2/12345/remove"))
            .withQueryParam("type", absent())
            .withQueryParam("environment", equalTo("development"))
            .withQueryParam("topic", equalTo("news"))

    override fun affectedChannelsAndGroups() = emptyList<String>() to emptyList<String>()

    override fun voidResponse() = true
}
