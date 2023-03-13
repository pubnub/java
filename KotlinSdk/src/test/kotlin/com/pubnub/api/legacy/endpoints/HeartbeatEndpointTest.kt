package com.pubnub.apikt.legacy.endpoints

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.pubnub.apikt.CommonUtils.assertPnException
import com.pubnub.apikt.CommonUtils.failTest
import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.endpoints.presence.Heartbeat
import com.pubnub.apikt.legacy.BaseTest
import org.junit.Assert.assertEquals
import org.junit.Test

class HeartbeatEndpointTest : BaseTest() {

    @Test
    fun testSuccessOneChannel() {
        pubnub.configuration.presenceTimeout = 123

        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1/heartbeat"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "message": "OK",
                          "service": "Presence"
                        }
                        """.trimIndent()
                    )
                )
        )

        Heartbeat(pubnub, listOf("ch1")).sync()

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)

        val request = requests[0]
        assertEquals("myUUID", request.queryParameter("uuid").firstValue())
        assertEquals("123", request.queryParameter("heartbeat").firstValue())
    }

    @Test
    fun testSuccessManyChannels() {
        pubnub.configuration.presenceTimeout = 123

        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1,ch2/heartbeat"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "message": "OK",
                          "service": "Presence"
                        }
                        """.trimIndent()
                    )
                )
        )

        Heartbeat(pubnub, listOf("ch1", "ch2")).sync()

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)

        val request = requests[0]
        assertEquals("myUUID", request.queryParameter("uuid").firstValue())
        assertEquals("123", request.queryParameter("heartbeat").firstValue())
    }

    @Test
    fun testSuccessOneChannelGroup() {
        pubnub.configuration.presenceTimeout = 123
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/,/heartbeat"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "message": "OK",
                          "service": "Presence"
                        }
                        """.trimIndent()
                    )
                )
        )

        Heartbeat(pubnub, channelGroups = listOf("cg1")).sync()

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)

        val request = requests[0]
        assertEquals("myUUID", request.queryParameter("uuid").firstValue())
        assertEquals("cg1", request.queryParameter("channel-group").firstValue())
        assertEquals("123", request.queryParameter("heartbeat").firstValue())
    }

    @Test
    fun testSuccessManyChannelGroups() {
        pubnub.configuration.presenceTimeout = 123

        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/,/heartbeat"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "message": "OK",
                          "service": "Presence"
                        }
                        """.trimIndent()
                    )
                )
        )

        Heartbeat(pubnub, channelGroups = listOf("cg1", "cg2")).sync()!!

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)

        val request = requests[0]
        assertEquals("myUUID", request.queryParameter("uuid").firstValue())
        assertEquals("cg1,cg2", request.queryParameter("channel-group").firstValue())
        assertEquals("123", request.queryParameter("heartbeat").firstValue())
    }

    @Test
    fun testMissingChannelAndGroupSync() {
        pubnub.configuration.presenceTimeout = 123

        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1/heartbeat"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "message": "OK",
                          "service": "Presence"
                        }
                        """.trimIndent()
                    )
                )
        )

        try {
            Heartbeat(pubnub).sync()!!
            failTest()
        } catch (e: Exception) {
            assertPnException(
                PubNubError.CHANNEL_AND_GROUP_MISSING,
                e
            )
        }
    }

    @Test
    fun testIsAuthRequiredSuccessSync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1/heartbeat"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "message": "OK",
                          "service": "Presence"
                        }
                        """.trimIndent()
                    )
                )
        )

        pubnub.configuration.authKey = "myKey"

        Heartbeat(pubnub, listOf("ch1")).sync()

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("myKey", requests[0].queryParameter("auth").firstValue())
    }

    @Test
    fun testBlankSubKeySync() {
        pubnub.configuration.presenceTimeout = 123

        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1/heartbeat"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "message": "OK",
                          "service": "Presence"
                        }
                        """.trimIndent()
                    )
                )
        )
        pubnub.configuration.subscribeKey = " "

        try {
            Heartbeat(pubnub, listOf("ch1")).sync()!!
            failTest()
        } catch (e: Exception) {
            assertPnException(
                PubNubError.SUBSCRIBE_KEY_MISSING,
                e
            )
        }
    }

    @Test
    fun testEmptySubKeySync() {
        pubnub.configuration.presenceTimeout = 123

        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1/heartbeat"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "message": "OK",
                          "service": "Presence"
                        }
                        """.trimIndent()
                    )
                )
        )
        pubnub.configuration.subscribeKey = ""

        try {
            Heartbeat(pubnub, listOf("ch1")).sync()!!
            failTest()
        } catch (e: Exception) {
            assertPnException(
                PubNubError.SUBSCRIBE_KEY_MISSING,
                e
            )
        }
    }
}
