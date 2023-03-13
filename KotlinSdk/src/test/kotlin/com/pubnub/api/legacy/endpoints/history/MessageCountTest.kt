package com.pubnub.apikt.legacy.endpoints.history

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.legacy.BaseTest
import org.junit.Assert.*
import org.junit.Test

class MessageCountTest : BaseTest() {

    @Test
    fun testSingleChannelWithSingleToken() {
        stubFor(
            get(urlPathEqualTo("/v3/history/sub-key/mySubscribeKey/message-counts/my_channel"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                             "status": 200,
                             "error": false,
                             "error_message": "",
                             "channels": {
                              "my_channel": 19
                             }
                            }
                        """.trimIndent()
                    )
                )
        )

        val response = pubnub.messageCounts(
            channels = listOf("my_channel"),
            channelsTimetoken = listOf(10000L)
        ).sync()!!

        assertEquals(response.channels.size, 1)
        assertFalse(response.channels.containsKey("channel_does_not_exist"))
        assertTrue(response.channels.containsKey("my_channel"))
        for ((key, value) in response.channels) {
            assertEquals("my_channel", key)
            assertEquals(java.lang.Long.valueOf("19"), value)
        }
    }

    @Test
    fun testSingleChannelWithMultipleTokens() {
        stubFor(
            get(urlPathEqualTo("/v3/history/sub-key/mySubscribeKey/message-counts/my_channel"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                             "status": 200,
                             "error": false,
                             "error_message": "",
                             "channels": {
                              "my_channel": 19
                             }
                            }
                        """.trimIndent()
                    )
                )
        )
        var exception: PubNubException? = null
        try {
            pubnub.messageCounts(
                channels = listOf("my_channel"),
                channelsTimetoken = listOf(10000L, 20000L)
            ).sync()
        } catch (e: PubNubException) {
            exception = e
        } finally {
            assertNotNull(exception)
            assertEquals(
                PubNubError.CHANNELS_TIMETOKEN_MISMATCH.message,
                exception!!.pubnubError!!.message
            )
        }
    }

    @Test
    @Throws(PubNubException::class)
    fun testMultipleChannelsWithSingleToken() {
        stubFor(
            get(urlPathEqualTo("/v3/history/sub-key/mySubscribeKey/message-counts/my_channel,new_channel"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                             "status": 200,
                             "error": false,
                             "error_message": "",
                             "channels": {
                              "my_channel": 19,
                              "new_channel": 5
                             }
                            }
                        """.trimIndent()
                    )
                )
        )

        val response = pubnub.messageCounts(
            channels = listOf("my_channel", "new_channel"),
            channelsTimetoken = listOf(10000L)
        ).sync()!!

        assertEquals(response.channels.size, 2)
        assertFalse(response.channels.containsKey("channel_does_not_exist"))
        assertTrue(response.channels.containsKey("my_channel"))
        assertTrue(response.channels.containsKey("new_channel"))
        for ((key, value) in response.channels) {
            if (key == "my_channel") {
                assertEquals(java.lang.Long.valueOf("19"), value)
            } else if (key == "new_channel") {
                assertEquals(java.lang.Long.valueOf("5"), value)
            }
        }
    }

    @Test
    @Throws(PubNubException::class)
    fun testMultipleChannelsWithMultipleTokens() {
        stubFor(
            get(urlPathEqualTo("/v3/history/sub-key/mySubscribeKey/message-counts/my_channel,new_channel"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                             "status": 200,
                             "error": false,
                             "error_message": "",
                             "channels": {
                              "my_channel": 19,
                              "new_channel": 5
                             }
                            }
                        """.trimIndent()
                    )
                )
        )

        val response = pubnub.messageCounts(
            channels = listOf("my_channel", "new_channel"),
            channelsTimetoken = listOf(10000L, 20000L)
        ).sync()!!

        assertEquals(response.channels.size, 2)
        assertFalse(response.channels.containsKey("channel_does_not_exist"))
        assertTrue(response.channels.containsKey("my_channel"))
        assertTrue(response.channels.containsKey("new_channel"))
        for ((key, value) in response.channels) {
            if (key == "my_channel") {
                assertEquals(java.lang.Long.valueOf("19"), value)
            } else if (key == "new_channel") {
                assertEquals(java.lang.Long.valueOf("5"), value)
            }
        }
    }

    @Test
    fun testWitEmptyChannelsSingleToken() {
        stubFor(
            get(urlPathEqualTo("/v3/history/sub-key/mySubscribeKey/message-counts/my_channel"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                             "status": 200,
                             "error": false,
                             "error_message": "",
                             "channels": {
                              "my_channel": 19,
                              "new_channel": 5
                             }
                            }
                        """.trimIndent()
                    )
                )
        )
        var exception: PubNubException? = null
        try {
            pubnub.messageCounts(
                channels = listOf(),
                channelsTimetoken = listOf(10000L)
            ).sync()
        } catch (ex: PubNubException) {
            exception = ex
        } finally {
            assertNotNull(exception)
            assertEquals(
                PubNubError.CHANNEL_MISSING.message,
                exception!!.pubnubError!!.message
            )
        }
    }

    @Test
    fun testWithEmptyChannelsMultipleTokens() {
        stubFor(
            get(urlPathEqualTo("/v3/history/sub-key/mySubscribeKey/message-counts/my_channel"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                             "status": 200,
                             "error": false,
                             "error_message": "",
                             "channels": {
                              "my_channel": 19,
                              "new_channel": 5
                             }
                            }
                        """.trimIndent()
                    )
                )
        )
        var exception: PubNubException? = null
        try {
            pubnub.messageCounts(
                channels = listOf(),
                channelsTimetoken = listOf(10000L, 20000L)
            ).sync()
        } catch (ex: PubNubException) {
            exception = ex
        } finally {
            assertNotNull(exception)
            assertEquals(
                PubNubError.CHANNEL_MISSING.message,
                exception!!.pubnubError!!.message
            )
        }
    }

    @Test
    fun testChannelWithSingleEmptyToken() {
        stubFor(
            get(urlPathEqualTo("/v3/history/sub-key/mySubscribeKey/message-counts/my_channel"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                         "status": 200,
                         "error": false,
                         "error_message": "",
                         "channels": {
                          "my_channel": 19
                         }
                        }
                        """.trimIndent()
                    )
                )
        )
        var exception: PubNubException? = null
        try {
            pubnub.messageCounts(
                channels = listOf("my_channel"),
                channelsTimetoken = listOf()
            ).sync()
        } catch (ex: PubNubException) {
            exception = ex
        } finally {
            assertNotNull(exception)
            assertEquals(
                PubNubError.TIMETOKEN_MISSING.message,
                exception!!.pubnubError!!.message
            )
        }
    }
}
