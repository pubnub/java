package com.pubnub.apikt.legacy.endpoints.pubsub

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.pubnub.apikt.CommonUtils.assertPnException
import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.endpoints.pubsub.Subscribe
import com.pubnub.apikt.legacy.BaseTest
import com.pubnub.apikt.models.server.SubscribeMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SubscribeEndpointTest : BaseTest() {

    @Test
    fun subscribeChannelSync() {
        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/coolChannel/0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "t": {
                                "t": "14607577960932487",
                                "r": 1
                              },
                              "m": [
                                {
                                  "a": "4",
                                  "f": 0,
                                  "i": "Client-g5d4g",
                                  "p": {
                                    "t": "14607577960925503",
                                    "r": 1
                                  },
                                  "k": "someSubKey",
                                  "c": "coolChannel",
                                  "d": {
                                    "text": "Enter Message Here"
                                  },
                                  "b": "coolChan-bnel"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )

        val subscribeEnvelope = Subscribe(pubnub).apply {
            channels = listOf("coolChannel")
        }.sync()!!

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("1", subscribeEnvelope.metadata.region)
        assertEquals(14607577960932487L, subscribeEnvelope.metadata.timetoken)
        assertEquals(1, subscribeEnvelope.messages.size)

        val subscribeMessage: SubscribeMessage = subscribeEnvelope.messages[0]
        assertEquals("4", subscribeMessage.shard)
        assertEquals("0", subscribeMessage.flags)
        assertEquals("coolChannel", subscribeMessage.channel)
        assertEquals("coolChan-bnel", subscribeMessage.subscriptionMatch)
        assertEquals("someSubKey", subscribeMessage.subscribeKey)
        assertEquals("Client-g5d4g", subscribeMessage.issuingClientId)
        assertEquals("""{"text":"Enter Message Here"}""", subscribeMessage.payload.toString())
    }

    @Test
    fun subscribeChannelsSync() {
        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/coolChannel/0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "t": {
                                "t": "14607577960932487",
                                "r": 1
                              },
                              "m": [
                                {
                                  "a": "4",
                                  "f": 0,
                                  "i": "Client-g5d4g",
                                  "p": {
                                    "t": "14607577960925503",
                                    "r": 1
                                  },
                                  "k": "someSubKey",
                                  "c": "coolChannel",
                                  "d": {
                                    "text": "Enter Message Here"
                                  },
                                  "b": "coolChan-bnel"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )

        val subscribeEnvelope = Subscribe(pubnub).apply {
            channels = listOf("coolChannel")
        }.sync()!!

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("1", subscribeEnvelope.metadata.region)
        assertTrue(subscribeEnvelope.metadata.timetoken == 14607577960932487L)
        assertEquals(1, subscribeEnvelope.messages.size)

        val subscribeMessage: SubscribeMessage = subscribeEnvelope.messages[0]
        assertEquals("4", subscribeMessage.shard)
        assertEquals("0", subscribeMessage.flags)
        assertEquals("coolChannel", subscribeMessage.channel)
        assertEquals("coolChan-bnel", subscribeMessage.subscriptionMatch)
        assertEquals("someSubKey", subscribeMessage.subscribeKey)
        assertEquals("Client-g5d4g", subscribeMessage.issuingClientId)
        assertEquals("""{"text":"Enter Message Here"}""", subscribeMessage.payload.toString())
    }

    @Test
    fun subscribeChannelsAuthSync() {
        pubnub.configuration.authKey = "authKey"

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/coolChannel,coolChannel2/0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "t": {
                                "t": "14607577960932487",
                                "r": 1
                              },
                              "m": [
                                {
                                  "a": "4",
                                  "f": 0,
                                  "i": "Client-g5d4g",
                                  "p": {
                                    "t": "14607577960925503",
                                    "r": 1
                                  },
                                  "k": "sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f",
                                  "c": "coolChannel",
                                  "d": {
                                    "text": "Enter Message Here"
                                  },
                                  "b": "coolChan-bnel"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )

        Subscribe(pubnub).apply {
            channels = listOf("coolChannel", "coolChannel2")
        }.sync()!!

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("authKey", requests[0].queryParameter("auth").firstValue())
    }

    @Test
    fun subscribeChannelsWithGroupSync() {
        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/coolChannel,coolChannel2/0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "t": {
                                "t": "14607577960932487",
                                "r": 1
                              },
                              "m": [
                                {
                                  "a": "4",
                                  "f": 0,
                                  "i": "Client-g5d4g",
                                  "p": {
                                    "t": "14607577960925503",
                                    "r": 1
                                  },
                                  "k": "sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f",
                                  "c": "coolChannel",
                                  "d": {
                                    "text": "Enter Message Here"
                                  },
                                  "b": "coolChan-bnel"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )

        Subscribe(pubnub).apply {
            channels = listOf("coolChannel", "coolChannel2")
            channelGroups = listOf("cg1")
        }.sync()!!

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("cg1", requests[0].queryParameter("channel-group").firstValue())
    }

    @Test
    fun subscribeGroupsSync() {
        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/,/0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "t": {
                                "t": "14607577960932487",
                                "r": 1
                              },
                              "m": [
                                {
                                  "a": "4",
                                  "f": 0,
                                  "i": "Client-g5d4g",
                                  "p": {
                                    "t": "14607577960925503",
                                    "r": 1
                                  },
                                  "k": "sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f",
                                  "c": "coolChannel",
                                  "d": {
                                    "text": "Enter Message Here"
                                  },
                                  "b": "coolChan-bnel"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )

        Subscribe(pubnub).apply {
            channelGroups = listOf("cg1", "cg2")
        }.sync()!!

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("cg1,cg2", requests[0].queryParameter("channel-group").firstValue())
    }

    @Test
    @Throws(PubNubException::class)
    fun subscribeGroupSync() {
        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/,/0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "t": {
                                "t": "14607577960932487",
                                "r": 1
                              },
                              "m": [
                                {
                                  "a": "4",
                                  "f": 0,
                                  "i": "Client-g5d4g",
                                  "p": {
                                    "t": "14607577960925503",
                                    "r": 1
                                  },
                                  "k": "sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f",
                                  "c": "coolChannel",
                                  "d": {
                                    "text": "Enter Message Here"
                                  },
                                  "b": "coolChan-bnel"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )

        Subscribe(pubnub).apply {
            channelGroups = listOf("cg1")
        }.sync()!!

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("cg1", requests[0].queryParameter("channel-group").firstValue())
    }

    @Test
    @Throws(PubNubException::class)
    fun subscribeWithTimeTokenSync() {
        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/,/0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "t": {
                                "t": "14607577960932487",
                                "r": 1
                              },
                              "m": [
                                {
                                  "a": "4",
                                  "f": 0,
                                  "i": "Client-g5d4g",
                                  "p": {
                                    "t": "14607577960925503",
                                    "r": 1
                                  },
                                  "k": "sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f",
                                  "c": "coolChannel",
                                  "d": {
                                    "text": "Enter Message Here"
                                  },
                                  "b": "coolChan-bnel"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )

        Subscribe(pubnub).apply {
            channelGroups = listOf("cg1")
            timetoken = 1337L
        }.sync()!!

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("cg1", requests[0].queryParameter("channel-group").firstValue())
        assertEquals("1337", requests[0].queryParameter("tt").firstValue())
    }

    @Test
    fun subscribeWithFilter() {
        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/,/0"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("pnsdk", matching("PubNub-Kotlin.*"))
                .withQueryParam("filter-expr", matching("this=1&that=cool"))
                .withQueryParam("channel-group", matching("cg1"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "t": {
                                "t": "14607577960932487",
                                "r": 1
                              },
                              "m": [
                                {
                                  "a": "4",
                                  "f": 0,
                                  "i": "Client-g5d4g",
                                  "p": {
                                    "t": "14607577960925503",
                                    "r": 1
                                  },
                                  "k": "sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f",
                                  "c": "coolChannel",
                                  "d": {
                                    "text": "Enter Message Here"
                                  },
                                  "b": "coolChan-bnel"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )

        Subscribe(pubnub).apply {
            channelGroups = listOf("cg1")
            filterExpression = "this=1&that=cool"
        }.sync()!!

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
    }

    @Test
    fun subscribeWithRegion() {
        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/,/0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "t": {
                                "t": "14607577960932487",
                                "r": 1
                              },
                              "m": [
                                {
                                  "a": "4",
                                  "f": 0,
                                  "i": "Client-g5d4g",
                                  "p": {
                                    "t": "14607577960925503",
                                    "r": 1
                                  },
                                  "k": "sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f",
                                  "c": "coolChannel",
                                  "d": {
                                    "text": "Enter Message Here"
                                  },
                                  "b": "coolChan-bnel"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )

        Subscribe(pubnub).apply {
            channelGroups = listOf("cg1")
            region = "10"
        }.sync()!!

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("cg1", requests[0].queryParameter("channel-group").firstValue())
        assertEquals("10", requests[0].queryParameter("tr").firstValue())
    }

    @Test
    fun subscribeMissingChannelAndGroupSync() {
        try {
            Subscribe(pubnub).apply {
            }.sync()!!
            throw RuntimeException()
        } catch (e: PubNubException) {
            assertPnException(PubNubError.CHANNEL_AND_GROUP_MISSING, e)
        }
    }

    @Test
    fun testMissingSubKeySync() {
        pubnub.configuration.subscribeKey = " "

        try {
            Subscribe(pubnub).apply {
            }.sync()!!
            throw RuntimeException()
        } catch (e: PubNubException) {
            assertPnException(PubNubError.SUBSCRIBE_KEY_MISSING, e)
        }
    }

    @Test
    fun stopAndReconnect() {
        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/coolChannel,coolChannel2/0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "t": {
                                "t": "14607577960932487",
                                "r": 1
                              },
                              "m": [
                                {
                                  "a": "4",
                                  "f": 0,
                                  "i": "Client-g5d4g",
                                  "p": {
                                    "t": "14607577960925503",
                                    "r": 1
                                  },
                                  "k": "sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f",
                                  "c": "coolChannel",
                                  "d": {
                                    "text": "Enter Message Here"
                                  },
                                  "b": "coolChan-bnel"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )

        Subscribe(pubnub).apply {
            channels = listOf("coolChannel", "coolChannel2")
        }.sync()!!

        pubnub.disconnect()
        pubnub.reconnect()

        Subscribe(pubnub).apply {
            channels = listOf("coolChannel", "coolChannel2")
        }.sync()!!

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(2, requests.size)
    }

    @Test
    fun testSuccessIncludeState() {
        pubnub.configuration.presenceTimeout = 123

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch1,ch2/0"))
                .willReturn(aResponse().withStatus(200))
        )

        try {
            Subscribe(pubnub).apply {
                channels = listOf("ch1", "ch2")
                state = mapOf(
                    "CH1" to "this-is-channel1",
                    "CH2" to "this-is-channel2"
                )
            }.sync()
        } catch (e: Exception) {
            // e.printStackTrace()
        }

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)

        val request = requests[0]
        assertEquals("myUUID", request.queryParameter("uuid").firstValue())
        assertEquals("123", request.queryParameter("heartbeat").firstValue())
        assertEquals(
            """{"CH1":"this-is-channel1","CH2":"this-is-channel2"}""",
            request.queryParameter("state").firstValue()
        )
    }
}
