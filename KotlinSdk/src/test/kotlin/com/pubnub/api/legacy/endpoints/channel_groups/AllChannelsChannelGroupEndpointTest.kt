package com.pubnub.apikt.legacy.endpoints.channel_groups

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.pubnub.apikt.CommonUtils.assertPnException
import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.enums.PNStatusCategory
import com.pubnub.apikt.legacy.BaseTest
import com.pubnub.apikt.listen
import com.pubnub.apikt.param
import org.awaitility.Awaitility
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.core.IsEqual
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class AllChannelsChannelGroupEndpointTest : BaseTest() {

    @Test
    fun testSyncSuccess() {
        stubFor(
            get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/groupA"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "message": "OK",
                          "payload": {
                            "channels": [
                              "a",
                              "b"
                            ]
                          },
                          "service": "ChannelGroups"
                        }
                        """.trimIndent()
                    )
                )
        )

        val response = pubnub.listChannelsForChannelGroup(
            channelGroup = "groupA"
        ).sync()!!

        assertThat(response.channels, Matchers.contains("a", "b"))
    }

    @Test
    fun testSyncEmptyGroup() {
        try {
            pubnub.listChannelsForChannelGroup(
                channelGroup = " "
            ).sync()!!
        } catch (e: Exception) {
            assertPnException(PubNubError.GROUP_MISSING, e)
        }
    }

    @Test
    fun testIsAuthRequiredSuccessSync() {
        stubFor(
            get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/groupA"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "status": 200,
                              "message": "OK",
                              "payload": {
                                "channels": [
                                  "a",
                                  "b"
                                ]
                              },
                              "service": "ChannelGroups"
                            }
                        """.trimIndent()
                    )
                )
        )
        pubnub.configuration.authKey = "myKey"

        pubnub.listChannelsForChannelGroup(
            channelGroup = "groupA"
        ).sync()!!

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("myKey", requests[0].queryParameter("auth").firstValue())
    }

    @Test
    fun testOperationTypeSuccessAsync() {
        stubFor(
            get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/groupA"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "status": 200,
                              "message": "OK",
                              "payload": {
                                "channels": [
                                  "a",
                                  "b"
                                ]
                              },
                              "service": "ChannelGroups"
                            }
                        """.trimIndent()
                    )
                )
        )

        val atomic = AtomicInteger(0)

        pubnub.listChannelsForChannelGroup(
            channelGroup = "groupA"
        ).async { result, status ->
            assertFalse(status.error)
            assertEquals(PNOperationType.PNChannelsForGroupOperation, status.operation)
            assertEquals(PNStatusCategory.PNAcknowledgmentCategory, status.category)
            assertTrue(status.affectedChannels.isEmpty())
            assertEquals(listOf("groupA"), status.affectedChannelGroups)
            assertEquals(listOf("a", "b"), result!!.channels)
            atomic.incrementAndGet()
        }

        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAtomic(atomic, IsEqual.equalTo(1))
    }

    @Test
    fun testIsSubRequiredSuccessSync() {
        pubnub.configuration.subscribeKey = " "
        try {
            pubnub.listChannelsForChannelGroup(
                channelGroup = "groupA"
            ).sync()!!
            throw RuntimeException()
        } catch (e: PubNubException) {
            assertPnException(PubNubError.SUBSCRIBE_KEY_MISSING, e)
        }
    }

    @Test
    fun testTelemetryParam() {
        val success = AtomicBoolean()

        stubFor(
            get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/groupA"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "status": 200,
                              "message": "OK",
                              "payload": {
                                "channels": [
                                  "a",
                                  "b"
                                ]
                              },
                              "service": "ChannelGroups"
                            }
                        """.trimIndent()
                    )
                )
        )

        stubFor(
            get(urlMatching("/time/0.*"))
                .willReturn(aResponse().withBody("[1000]"))
        )

        lateinit var telemetryParamName: String

        pubnub.listChannelsForChannelGroup(
            channelGroup = "groupA"
        ).async { _, status ->
            assertFalse(status.error)
            assertEquals(PNOperationType.PNChannelsForGroupOperation, status.operation)
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
