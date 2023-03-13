package com.pubnub.apikt.legacy.endpoints.channel_groups

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.pubnub.apikt.CommonUtils.assertPnException
import com.pubnub.apikt.CommonUtils.failTest
import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.enums.PNStatusCategory
import com.pubnub.apikt.legacy.BaseTest
import com.pubnub.apikt.listen
import com.pubnub.apikt.param
import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class DeleteChannelGroupEndpointTest : BaseTest() {

    @Test
    fun testSyncSuccess() {
        stubFor(
            get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/groupA/remove"))
                .willReturn(
                    aResponse().withBody(
                        """{"status":200,"message":"OK","payload":{},"service":"ChannelGroups"}"""
                    )
                )
        )

        pubnub.deleteChannelGroup(
            channelGroup = "groupA"
        ).sync()!!
    }

    @Test
    fun testSyncEmptyGroup() {
        stubFor(any(anyUrl()).willReturn(aResponse()))

        try {
            pubnub.deleteChannelGroup(
                channelGroup = " "
            ).sync()!!
            failTest()
        } catch (e: PubNubException) {
            assertPnException(PubNubError.GROUP_MISSING, e)
        }
    }

    @Test
    fun testIsAuthRequiredSuccessSync() {
        stubFor(
            get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/groupA/remove"))
                .willReturn(
                    aResponse().withBody(
                        """{"status": 200, "message": "OK", "payload": {}, "service": "ChannelGroups"}"""
                    )
                )
        )

        pubnub.configuration.authKey = "myKey"

        pubnub.deleteChannelGroup(
            channelGroup = "groupA"
        ).sync()!!

        val requests =
            findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("myKey", requests[0].queryParameter("auth").firstValue())
    }

    @Test
    fun testMalformedResponse() {
        stubFor(
            get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/groupA/remove"))
                .willReturn(noContent())
        )

        pubnub.deleteChannelGroup(
            channelGroup = "groupA"
        ).sync()!!
    }

    @Test
    fun testOperationTypeSuccessAsync() {
        stubFor(
            get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/groupA/remove"))
                .willReturn(
                    aResponse().withBody(
                        """{"status": 200, "message": "OK", "payload": {}, "service": "ChannelGroups"}"""
                    )
                )
        )

        val atomic = AtomicInteger(0)

        pubnub.deleteChannelGroup(
            channelGroup = "groupA"
        ).async { _, status ->
            assertFalse(status.error)
            assertEquals(PNOperationType.PNRemoveGroupOperation, status.operation)
            assertEquals(PNStatusCategory.PNAcknowledgmentCategory, status.category)
            assertTrue(status.affectedChannels == emptyList<String>())
            assertTrue(status.affectedChannelGroups == listOf("groupA"))
            atomic.incrementAndGet()
        }

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAtomic(atomic, IsEqual.equalTo(1))
    }

    @Test
    fun testTelemetryParam() {
        val success = AtomicBoolean()

        stubFor(
            get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/groupA/remove"))
                .willReturn(
                    aResponse().withBody(
                        """{"status": 200, "message": "OK", "payload": {}, "service": "ChannelGroups"}"""
                    )
                )
        )

        stubFor(
            get(urlMatching("/time/0.*"))
                .willReturn(aResponse().withBody("[1000]"))
        )

        lateinit var telemetryParamName: String

        pubnub.deleteChannelGroup(
            channelGroup = "groupA"
        ).async { _, status ->
            assertFalse(status.error)
            assertEquals(PNOperationType.PNRemoveGroupOperation, status.operation)
            telemetryParamName = "l_${status.operation.queryParam}"
            assertEquals("l_cg", telemetryParamName)
            success.set(true)
        }

        success.listen()

        pubnub.time().async { _, status ->
            assertFalse(status.error)
            assertNotNull(status.param(telemetryParamName))
            success.set(true)
        }

        success.listen()
    }
}
