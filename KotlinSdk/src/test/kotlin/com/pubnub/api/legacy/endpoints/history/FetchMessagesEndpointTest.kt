package com.pubnub.apikt.legacy.endpoints.history

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.pubnub.apikt.endpoints.FetchMessages
import com.pubnub.apikt.legacy.BaseTest
import com.pubnub.apikt.models.consumer.PNBoundedPage
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random.Default.nextInt

class FetchMessagesEndpointTest : BaseTest() {

    companion object {
        private const val MAX_FOR_FETCH_MESSAGES = 100
        private const val MAX_FOR_FETCH_MESSAGES_WITH_ACTIONS = 25
        private const val MULTIPLE_CHANNELS_MAX_FOR_FETCH_MESSAGES = 25
        private const val EXPECTED_SINGLE_CHANNEL_DEFAULT_MESSAGES = 100
        private const val EXPECTED_MULTIPLE_CHANNEL_DEFAULT_MESSAGES = 25
        private const val EXPECTED_DEFAULT_MESSAGES_WITH_ACTIONS = 25
        private const val EXPECTED_MAX_MESSAGES_WITH_ACTIONS = 25
        private const val DEFAULT_INCLUDE_MESSAGE_ACTIONS = false
    }

    @Test
    fun testSyncSuccess() {
        stubFor(
            get(urlPathEqualTo("/v3/history/sub-key/mySubscribeKey/channel/mychannel,my_channel"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "status": 200,
                              "error": false,
                              "error_message": "",
                              "channels": {
                                "my_channel": [
                                  {
                                    "message": "hihi",
                                    "uuid": "my-uuid",
                                    "timetoken": "14698320467224036"
                                  },
                                  {
                                    "message": "Hey",
                                    "uuid": "my-uuid",
                                    "timetoken": "14698320468265639"
                                  }
                                ],
                                "mychannel": [
                                  {
                                    "message": "sample message",
                                    "uuid": "my-uuid",
                                    "timetoken": "14369823849575729"
                                  }
                                ]
                              }
                            }
                        """.trimIndent()
                    )
                )
        )

        val response = pubnub.fetchMessages(
            channels = listOf("mychannel", "my_channel"),
            page = PNBoundedPage(
                limit = 25
            )
        ).sync()!!

        assertEquals(response.channels.size, 2)

        assertTrue(response.channels.containsKey("mychannel"))
        assertTrue(response.channels.containsKey("my_channel"))

        assertEquals(response.channels["mychannel"]!!.size, 1)
        assertEquals(response.channels["my_channel"]!!.size, 2)

        assertEquals(response.channels["mychannel"]!!.count { it.uuid == "my-uuid" }, 1)
        assertEquals(response.channels["my_channel"]!!.count { it.uuid == "my-uuid" }, 2)
    }

    @Test
    fun testSyncWithoutUUIDSuccess() {
        stubFor(
            get(urlPathEqualTo("/v3/history/sub-key/mySubscribeKey/channel/mychannel,my_channel"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "status": 200,
                              "error": false,
                              "error_message": "",
                              "channels": {
                                "my_channel": [
                                  {
                                    "message": "hihi",
                                    "timetoken": "14698320467224036"
                                  },
                                  {
                                    "message": "Hey",
                                    "timetoken": "14698320468265639"
                                  }
                                ],
                                "mychannel": [
                                  {
                                    "message": "sample message",
                                    "timetoken": "14369823849575729"
                                  }
                                ]
                              }
                            }
                        """.trimIndent()
                    )
                )
        )

        val response = pubnub.fetchMessages(
            channels = listOf("mychannel", "my_channel"),
            page = PNBoundedPage(
                limit = 25
            ),
            includeUUID = false
        ).sync()!!

        assertEquals(response.channels.size, 2)
        assertTrue(response.channels.containsKey("mychannel"))
        assertTrue(response.channels.containsKey("my_channel"))

        assertEquals(response.channels["mychannel"]!!.size, 1)
        assertEquals(response.channels["my_channel"]!!.size, 2)

        assertEquals(response.channels["mychannel"]!!.count { it.uuid == null }, 1)
        assertEquals(response.channels["my_channel"]!!.count { it.uuid == null }, 2)
    }

    @Test
    fun testSyncAuthSuccess() {
        stubFor(
            get(urlPathEqualTo("/v3/history/sub-key/mySubscribeKey/channel/mychannel,my_channel"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "status": 200,
                              "error": false,
                              "error_message": "",
                              "channels": {
                                "my_channel": [
                                  {
                                    "message": "hihi",
                                    "timetoken": "14698320467224036"
                                  },
                                  {
                                    "message": "Hey",
                                    "timetoken": "14698320468265639"
                                  }
                                ],
                                "mychannel": [
                                  {
                                    "message": "sample message",
                                    "timetoken": "14369823849575729"
                                  }
                                ]
                              }
                            }
                        """.trimIndent()
                    )
                )
        )

        pubnub.configuration.authKey = "authKey"

        val response = pubnub.fetchMessages(
            channels = listOf("mychannel", "my_channel"),
            page = PNBoundedPage(
                limit = 25
            )
        ).sync()!!

        assertEquals(response.channels.size, 2)
        assertTrue(response.channels.containsKey("mychannel"))
        assertTrue(response.channels.containsKey("my_channel"))
        assertEquals(response.channels["mychannel"]!!.size, 1)
        assertEquals(response.channels["my_channel"]!!.size, 2)
        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals("authKey", requests[0].queryParameter("auth").firstValue())
        assertEquals(1, requests.size)
    }

    @Test
    fun testSyncEncryptedSuccess() {
        pubnub.configuration.cipherKey = "testCipher"
        pubnub.configuration.useRandomInitializationVector = false

        stubFor(
            get(urlPathEqualTo("/v3/history/sub-key/mySubscribeKey/channel/mychannel,my_channel"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "status": 200,
                              "error": false,
                              "error_message": "",
                              "channels": {
                                "my_channel": [
                                  {
                                    "message": "jC/yJ2y99BeYFYMQ7c53pg==",
                                    "timetoken": "14797423056306675"
                                  }
                                ],
                                "mychannel": [
                                  {
                                    "message": "jC/yJ2y99BeYFYMQ7c53pg==",
                                    "timetoken": "14797423056306675"
                                  }
                                ]
                              }
                            }
                        """.trimIndent()
                    )
                )
        )

        val response = pubnub.fetchMessages(
            channels = listOf("mychannel", "my_channel"),
            page = PNBoundedPage(
                limit = 25
            )
        ).sync()!!

        assertEquals(response.channels.size, 2)
        assertTrue(response.channels.containsKey("mychannel"))
        assertTrue(response.channels.containsKey("my_channel"))
        assertEquals(response.channels["mychannel"]!!.size, 1)
        assertEquals(response.channels["my_channel"]!!.size, 1)
    }

    @Test
    fun forSingleChannelFetchMessagesAlwaysPassMaxWhenItIsInBound() {
        // given
        val maximumPerChannel = randomInt(MAX_FOR_FETCH_MESSAGES)

        // when
        val result = FetchMessages.effectiveMax(
            maximumPerChannel = maximumPerChannel,
            includeMessageActions = DEFAULT_INCLUDE_MESSAGE_ACTIONS,
            numberOfChannels = 1
        )

        // then
        assertThat(result, Matchers.equalTo(maximumPerChannel))
    }

    @Test
    fun forSingleChannelFetchMessagesAlwaysPassDefaultWhenNonPositiveMaxSpecified() {
        // given
        val maximumPerChannel = -(randomInt(100) - 1)

        // when
        val result = FetchMessages.effectiveMax(
            maximumPerChannel = maximumPerChannel,
            includeMessageActions = DEFAULT_INCLUDE_MESSAGE_ACTIONS,
            numberOfChannels = 1
        )

        // then
        assertThat(result, Matchers.equalTo(EXPECTED_SINGLE_CHANNEL_DEFAULT_MESSAGES))
    }
    @Test
    fun forSingleChannelFetchMessagesAlwaysPassDefaultWhenMaxNotSpecified() {
        // given
        val maximumPerChannel = null

        // when
        val result = FetchMessages.effectiveMax(
            maximumPerChannel = maximumPerChannel,
            includeMessageActions = DEFAULT_INCLUDE_MESSAGE_ACTIONS,
            numberOfChannels = 1
        )

        // then
        assertThat(result, Matchers.equalTo(EXPECTED_SINGLE_CHANNEL_DEFAULT_MESSAGES))
    }

    @Test
    fun forSingleChannelFetchMessagesAlwaysPassDefaultMaxWhenMaxExceeds() {
        // given
        val maximumPerChannel = MAX_FOR_FETCH_MESSAGES + randomInt(MAX_FOR_FETCH_MESSAGES)

        // when
        val result = FetchMessages.effectiveMax(
            maximumPerChannel = maximumPerChannel,
            includeMessageActions = DEFAULT_INCLUDE_MESSAGE_ACTIONS,
            numberOfChannels = 1
        )

        // then
        assertThat(result, Matchers.equalTo(EXPECTED_SINGLE_CHANNEL_DEFAULT_MESSAGES))
    }

    @Test
    fun forMultipleChannelsFetchMessagesAlwaysPassMaxWhenItIsInBound() {
        // given
        val maximumPerChannel = randomInt(MULTIPLE_CHANNELS_MAX_FOR_FETCH_MESSAGES)

        // when
        val result = FetchMessages.effectiveMax(
            maximumPerChannel = maximumPerChannel,
            includeMessageActions = DEFAULT_INCLUDE_MESSAGE_ACTIONS,
            numberOfChannels = 2
        )

        // then
        assertThat(result, Matchers.equalTo(maximumPerChannel))
    }

    @Test
    fun forMultipleChannelsFetchMessagesAlwaysPassDefaultWhenNonPositiveMaxSpecified() {
        // given
        val maximumPerChannel = -(randomInt(100) - 1)

        // when
        val result = FetchMessages.effectiveMax(
            maximumPerChannel = maximumPerChannel,
            includeMessageActions = DEFAULT_INCLUDE_MESSAGE_ACTIONS,
            numberOfChannels = 2
        )

        // then
        assertThat(result, Matchers.equalTo(EXPECTED_MULTIPLE_CHANNEL_DEFAULT_MESSAGES))
    }

    @Test
    fun forMultipleChannelsFetchMessagesAlwaysPassDefaultWhenMaxNotSpecified() {
        // given
        val maximumPerChannel = null

        // when
        val result = FetchMessages.effectiveMax(
            maximumPerChannel = maximumPerChannel,
            includeMessageActions = DEFAULT_INCLUDE_MESSAGE_ACTIONS,
            numberOfChannels = 2
        )

        // then
        assertThat(result, Matchers.equalTo(EXPECTED_MULTIPLE_CHANNEL_DEFAULT_MESSAGES))
    }

    @Test
    fun forMultipleChannelsFetchMessagesAlwaysPassDefaultMaxWhenMaxExceeds() {
        // given
        val maximumPerChannel =
            MULTIPLE_CHANNELS_MAX_FOR_FETCH_MESSAGES + randomInt(MULTIPLE_CHANNELS_MAX_FOR_FETCH_MESSAGES)

        // when
        val result = FetchMessages.effectiveMax(
            maximumPerChannel = maximumPerChannel,
            includeMessageActions = DEFAULT_INCLUDE_MESSAGE_ACTIONS,
            numberOfChannels = 2
        )

        // then
        assertThat(result, Matchers.equalTo(EXPECTED_MULTIPLE_CHANNEL_DEFAULT_MESSAGES))
    }

    @Test
    fun forSingleChannelFetchMessagesWithActionAlwaysPassMaxWhenItIsInBound() {
        // given
        val maximumPerChannel = randomInt(MAX_FOR_FETCH_MESSAGES_WITH_ACTIONS)

        // when
        val result = FetchMessages.effectiveMax(
            maximumPerChannel = maximumPerChannel,
            includeMessageActions = true,
            numberOfChannels = 1
        )

        // then
        assertThat(result, Matchers.equalTo(maximumPerChannel))
    }

    @Test
    fun forSingleChannelFetchMessagesWithActionAlwaysPassDefaultWhenMaxNotSpecified() {
        // given
        val maximumPerChannel = null

        // when
        val result = FetchMessages.effectiveMax(
            maximumPerChannel = maximumPerChannel,
            includeMessageActions = true,
            numberOfChannels = 1
        )

        // then
        assertThat(result, Matchers.equalTo(EXPECTED_DEFAULT_MESSAGES_WITH_ACTIONS))
    }

    @Test
    fun forSingleChannelFetchMessagesWithActionAlwaysPassDefaultMaxWhenMaxExceeds() {
        // given
        val maximumPerChannel = MAX_FOR_FETCH_MESSAGES_WITH_ACTIONS +
            randomInt(MAX_FOR_FETCH_MESSAGES_WITH_ACTIONS)

        // when
        val result = FetchMessages.effectiveMax(
            maximumPerChannel = maximumPerChannel,
            includeMessageActions = true,
            numberOfChannels = 1
        )

        // then
        assertThat(result, Matchers.equalTo(EXPECTED_MAX_MESSAGES_WITH_ACTIONS))
    }

    private fun randomInt(max: Int): Int = nextInt(max) + 1
}
