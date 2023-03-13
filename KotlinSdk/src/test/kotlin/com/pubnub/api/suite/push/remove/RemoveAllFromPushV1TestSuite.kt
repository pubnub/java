package com.pubnub.apikt.suite.push.remove

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.pubnub.apikt.endpoints.push.RemoveAllPushChannelsForDevice
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.enums.PNPushType
import com.pubnub.apikt.models.consumer.push.PNPushRemoveAllChannelsResult
import com.pubnub.apikt.suite.AUTH
import com.pubnub.apikt.suite.EndpointTestSuite
import com.pubnub.apikt.suite.SUB

class RemoveAllFromPushV1TestSuite :
    EndpointTestSuite<RemoveAllPushChannelsForDevice, PNPushRemoveAllChannelsResult>() {

    override fun telemetryParamName() = "l_push"

    override fun pnOperation() = PNOperationType.PNRemoveAllPushNotificationsOperation

    override fun requiredKeys() = SUB + AUTH

    override fun snippet(): RemoveAllPushChannelsForDevice {
        return pubnub.removeAllPushNotificationsFromDeviceWithPushToken(
            pushType = PNPushType.FCM,
            deviceId = "12345"
        )
    }

    override fun verifyResultExpectations(result: PNPushRemoveAllChannelsResult) {
    }

    override fun successfulResponseBody(): String {
        return """[1, "Removed Device"]"""
    }

    override fun unsuccessfulResponseBodyList() = emptyList<String>()

    override fun mappingBuilder() =
        get(urlPathEqualTo("/v1/push/sub-key/mySubscribeKey/devices/12345/remove"))
            .withQueryParam("type", equalTo("gcm"))
            .withQueryParam("environment", absent())
            .withQueryParam("topic", absent())

    override fun affectedChannelsAndGroups() = emptyList<String>() to emptyList<String>()

    override fun voidResponse() = true
}
