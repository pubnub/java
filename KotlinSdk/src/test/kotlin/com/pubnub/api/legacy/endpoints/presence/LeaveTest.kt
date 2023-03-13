package com.pubnub.apikt.legacy.endpoints.presence

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.pubnub.apikt.CommonUtils.assertPnException
import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.endpoints.presence.Leave
import com.pubnub.apikt.legacy.BaseTest
import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class LeaveTest : BaseTest() {

    @Test
    fun subscribeChannelSync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/coolChannel/leave"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "message": "OK",
                          "service": "Presence",
                          "action": "leave"
                        }
                        """.trimIndent()
                    )
                )
        )

        Leave(pubnub).apply {
            channels = listOf("coolChannel")
        }.sync()!!

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
    }

    @Test
    fun subscribeChannelsSync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/coolChannel,coolChannel2/leave"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "message": "OK",
                          "service": "Presence",
                          "action": "leave"
                        }
                        """.trimIndent()
                    )
                )
        )

        Leave(pubnub).apply {
            channels = listOf("coolChannel", "coolChannel2")
        }.sync()

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
    }

    @Test
    fun subscribeChannelsWithGroupSync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/coolChannel,coolChannel2/leave"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "message": "OK",
                          "service": "Presence",
                          "action": "leave"
                        }
                        """.trimIndent()
                    )
                )
        )

        Leave(pubnub).apply {
            channels = listOf("coolChannel", "coolChannel2")
            channelGroups = listOf("cg1")
        }.sync()

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("cg1", requests[0].queryParameter("channel-group").firstValue())
    }

    @Test
    fun subscribeChannelsWithGroupASync() {
        val statusArrived = AtomicBoolean()
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/coolChannel,coolChannel2/leave"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "message": "OK",
                          "service": "Presence",
                          "action": "leave"
                        }
                        """.trimIndent()
                    )
                )
        )

        Leave(pubnub).apply {
            channels = listOf("coolChannel", "coolChannel2")
            channelGroups = listOf("cg1")
        }.async { _, status ->
            assertEquals(status.affectedChannels[0], "coolChannel")
            assertEquals(status.affectedChannels[1], "coolChannel2")
            assertEquals(status.affectedChannelGroups[0], "cg1")
            statusArrived.set(true)
        }

        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAtomic(
            statusArrived,
            IsEqual.equalTo(true)
        )
    }

    @Test
    fun subscribeGroupsSync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/,/leave"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "message": "OK",
                          "service": "Presence",
                          "action": "leave"
                        }
                        """.trimIndent()
                    )
                )
        )

        Leave(pubnub).apply {
            channelGroups = listOf("cg1", "cg2")
        }.sync()

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("cg1,cg2", requests[0].queryParameter("channel-group").firstValue())
    }

    @Test
    fun subscribeGroupSync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/,/leave"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "message": "OK",
                          "service": "Presence",
                          "action": "leave"
                        }
                        """.trimIndent()
                    )
                )
        )

        Leave(pubnub).apply {
            channelGroups = listOf("cg1")
        }.sync()

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("cg1", requests[0].queryParameter("channel-group").firstValue())
    }

    @Test
    fun testMissingChannelAndGroupSync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/coolChannel/leave"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "status": 200,
                              "message": "OK",
                              "service": "Presence",
                              "action": "leave"
                            }
                        """.trimIndent()
                    )
                )
        )

        try {
            Leave(pubnub).apply {}.sync()
        } catch (e: Exception) {
            assertPnException(PubNubError.CHANNEL_AND_GROUP_MISSING, e)
        }
    }

    @Test
    fun testBlankSubKeySync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/coolChannel/leave"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "message": "OK",
                          "service": "Presence",
                          "action": "leave"
                        }
                        """.trimIndent()
                    )
                )
        )

        pubnub.configuration.subscribeKey = " "

        try {
            Leave(pubnub).apply {}.sync()
        } catch (e: Exception) {
            assertPnException(PubNubError.SUBSCRIBE_KEY_MISSING, e)
        }
    }

    @Test
    fun testEmptySubKeySync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/coolChannel/leave"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "message": "OK",
                          "service": "Presence",
                          "action": "leave"
                        }
                        """.trimIndent()
                    )
                )
        )

        pubnub.configuration.subscribeKey = ""

        try {
            Leave(pubnub).apply {}.sync()
        } catch (e: Exception) {
            assertPnException(PubNubError.SUBSCRIBE_KEY_MISSING, e)
        }
    }

    @Test
    fun testIsAuthRequiredSuccessSync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/coolChannel/leave"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "message": "OK",
                          "service": "Presence",
                          "action": "leave"
                        }
                        """.trimIndent()
                    )
                )
        )

        pubnub.configuration.authKey = "myKey"

        Leave(pubnub).apply {
            channels = listOf("coolChannel")
        }.sync()

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("myKey", requests[0].queryParameter("auth").firstValue())
    }
}
