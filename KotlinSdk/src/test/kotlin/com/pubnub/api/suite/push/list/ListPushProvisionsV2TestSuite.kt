package com.pubnub.apikt.suite.push.list

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.pubnub.apikt.endpoints.push.ListPushProvisions
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.enums.PNPushType
import com.pubnub.apikt.models.consumer.PNStatus
import com.pubnub.apikt.models.consumer.push.PNPushListProvisionsResult
import com.pubnub.apikt.suite.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse

class ListPushProvisionsV2TestSuite : EndpointTestSuite<ListPushProvisions, PNPushListProvisionsResult>() {

    override fun telemetryParamName() = "l_push"

    override fun pnOperation() = PNOperationType.PNPushNotificationEnabledChannelsOperation

    override fun requiredKeys() = SUB + AUTH

    override fun snippet(): ListPushProvisions {
        return pubnub.auditPushChannelProvisions(
            pushType = PNPushType.APNS2,
            deviceId = "12345",
            topic = "news"
        )
    }

    override fun verifyResultExpectations(result: PNPushListProvisionsResult) {
        assertEquals(2, result.channels.size)
        assertEquals("ch1", result.channels[0])
        assertEquals("ch2", result.channels[1])
    }

    override fun successfulResponseBody(): String {
        return """["ch1", "ch2"]"""
    }

    override fun unsuccessfulResponseBodyList() = listOf(
        """["ch1","ch2",{}]"""
    )

    override fun mappingBuilder() =
        get(urlPathEqualTo("/v2/push/sub-key/mySubscribeKey/devices-apns2/12345"))
            .withQueryParam("type", absent())
            .withQueryParam("environment", equalTo("development"))
            .withQueryParam("topic", equalTo("news"))

    override fun affectedChannelsAndGroups() = emptyList<String>() to emptyList<String>()

    override fun optionalScenarioList(): List<OptionalScenario<PNPushListProvisionsResult>> {
        return listOf(
            OptionalScenario<PNPushListProvisionsResult>().apply {
                responseBuilder = { withBody("[]") }
                result = Result.SUCCESS
                additionalChecks = { status: PNStatus, result: PNPushListProvisionsResult? ->
                    assertFalse(status.error)
                    assertEquals(0, result!!.channels.size)
                }
            }
        )
    }
}
