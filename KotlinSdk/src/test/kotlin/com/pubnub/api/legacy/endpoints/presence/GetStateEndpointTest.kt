package com.pubnub.apikt.legacy.endpoints.presence

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.pubnub.apikt.CommonUtils.assertPnException
import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.legacy.BaseTest
import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class GetStateEndpointTest : BaseTest() {

    @Test
    fun testOneChannelSync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel/uuid/sampleUUID"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "message": "OK",
                          "payload": {
                            "age": 20,
                            "status": "online"
                          },
                          "service": "Presence"
                        }
                        """.trimIndent()
                    )
                )
        )

        val result = pubnub.getPresenceState(
            channels = listOf("testChannel"),
            uuid = "sampleUUID"
        ).sync()!!

        val ch1Data = result.stateByUUID["testChannel"]!!
        assertEquals(pubnub.mapper.elementToInt(ch1Data, "age"), 20)
        assertEquals(pubnub.mapper.elementToString(ch1Data, "status"), "online")
    }

    @Test
    fun testOneChannelWithoutUUIDSync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel/uuid/myUUID"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "message": "OK",
                          "payload": {
                            "age": 20,
                            "status": "online"
                          },
                          "service": "Presence"
                        }
                        """.trimIndent()
                    )
                )
        )

        val result = pubnub.getPresenceState(
            channels = listOf("testChannel")
        ).sync()!!

        val ch1Data = result.stateByUUID["testChannel"]!!
        assertEquals(pubnub.mapper.elementToInt(ch1Data, "age"), 20)
        assertEquals(pubnub.mapper.elementToString(ch1Data, "status"), "online")
    }

    @Test
    fun testFailedPayloadSync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel/uuid/sampleUUID"))
                .willReturn(
                    aResponse().withBody(
                        """
                        { "status": 200, "message": "OK", "payload": "age" : 20
                        """.trimIndent()
                    )
                )
        )

        try {
            pubnub.getPresenceState(
                channels = listOf("testChannel"),
                uuid = "sampleUUID"
            ).sync()
        } catch (e: Exception) {
            assertPnException(PubNubError.PARSING_ERROR, e)
        }
    }

    @Test
    fun testMultipleChannelSync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1,ch2/uuid/sampleUUID"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "message": "OK",
                          "payload": {
                            "ch1": {
                              "age": 20,
                              "status": "online"
                            },
                            "ch2": {
                              "age": 100,
                              "status": "offline"
                            }
                          },
                          "service": "Presence"
                        }
                        """.trimIndent()
                    )
                )
        )

        val result = pubnub.getPresenceState(
            channels = listOf("ch1", "ch2"),
            uuid = "sampleUUID"
        ).sync()!!

        val ch1Data = result.stateByUUID["ch1"]!!
        assertEquals(pubnub.mapper.elementToInt(ch1Data, "age"), 20)
        assertEquals(pubnub.mapper.elementToString(ch1Data, "status"), "online")

        val ch2Data = result.stateByUUID["ch2"]!!
        assertEquals(pubnub.mapper.elementToInt(ch2Data, "age"), 100)
        assertEquals(pubnub.mapper.elementToString(ch2Data, "status"), "offline")
    }

    @Test
    fun testOneChannelGroupSync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/,/uuid/sampleUUID"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "message": "OK",
                          "payload": {
                            "chcg1": {
                              "age": 20,
                              "status": "online"
                            },
                            "chcg2": {
                              "age": 100,
                              "status": "offline"
                            }
                          },
                          "service": "Presence"
                        }
                        """.trimIndent()
                    )
                )
        )

        val result = pubnub.getPresenceState(
            channelGroups = listOf("cg1"),
            uuid = "sampleUUID"
        ).sync()!!

        val ch1Data = result.stateByUUID["chcg1"]!!
        assertEquals(pubnub.mapper.elementToInt(ch1Data, "age"), 20)
        assertEquals(pubnub.mapper.elementToString(ch1Data, "status"), "online")

        val ch2Data = result.stateByUUID["chcg2"]!!
        assertEquals(pubnub.mapper.elementToInt(ch2Data, "age"), 100)
        assertEquals(pubnub.mapper.elementToString(ch2Data, "status"), "offline")

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("cg1", requests[0].queryParameter("channel-group").firstValue())
    }

    @Test
    fun testManyChannelGroupSync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/,/uuid/sampleUUID"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "message": "OK",
                          "payload": {
                            "chcg1": {
                              "age": 20,
                              "status": "online"
                            },
                            "chcg2": {
                              "age": 100,
                              "status": "offline"
                            }
                          },
                          "service": "Presence"
                        }
                        """.trimIndent()
                    )
                )
        )

        val result = pubnub.getPresenceState(
            channelGroups = listOf("cg1", "cg2"),
            uuid = "sampleUUID"
        ).sync()!!

        val ch1Data = result.stateByUUID["chcg1"]!!
        assertEquals(pubnub.mapper.elementToInt(ch1Data, "age"), 20)
        assertEquals(pubnub.mapper.elementToString(ch1Data, "status"), "online")

        val ch2Data = result.stateByUUID["chcg2"]!!
        assertEquals(pubnub.mapper.elementToInt(ch2Data, "age"), 100)
        assertEquals(pubnub.mapper.elementToString(ch2Data, "status"), "offline")

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("cg1,cg2", requests[0].queryParameter("channel-group").firstValue())
    }

    @Test
    fun testCombinationSync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1/uuid/sampleUUID"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "message": "OK",
                          "payload": {
                            "chcg1": {
                              "age": 20,
                              "status": "online"
                            },
                            "chcg2": {
                              "age": 100,
                              "status": "offline"
                            }
                          },
                          "service": "Presence"
                        }
                        """.trimIndent()
                    )
                )
        )

        val result = pubnub.getPresenceState(
            channels = listOf("ch1"),
            channelGroups = listOf("cg1", "cg2"),
            uuid = "sampleUUID"
        ).sync()!!

        val ch1Data = result.stateByUUID["chcg1"]!!
        assertEquals(pubnub.mapper.elementToInt(ch1Data, "age"), 20)
        assertEquals(pubnub.mapper.elementToString(ch1Data, "status"), "online")

        val ch2Data = result.stateByUUID["chcg2"]!!
        assertEquals(pubnub.mapper.elementToInt(ch2Data, "age"), 100)
        assertEquals(pubnub.mapper.elementToString(ch2Data, "status"), "offline")

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("cg1,cg2", requests[0].queryParameter("channel-group").firstValue())
    }

    @Test
    fun testMissingChannelAndGroupSync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel/uuid/sampleUUID"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "message": "OK",
                          "payload": {
                            "age": 20,
                            "status": "online"
                          },
                          "service": "Presence"
                        }
                        """.trimIndent()
                    )
                )
        )

        try {
            pubnub.getPresenceState(
                uuid = "sampleUUID"
            ).sync()!!
        } catch (e: Exception) {
            assertPnException(PubNubError.CHANNEL_AND_GROUP_MISSING, e)
        }
    }

    @Test
    fun testIsAuthRequiredSuccessSync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel/uuid/sampleUUID"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "message": "OK",
                          "payload": {
                            "age": 20,
                            "status": "online"
                          },
                          "service": "Presence"
                        }
                        """.trimIndent()
                    )
                )
        )

        pubnub.configuration.authKey = "myKey"

        pubnub.getPresenceState(
            channels = listOf("testChannel"),
            uuid = "sampleUUID"
        ).sync()!!

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("myKey", requests[0].queryParameter("auth").firstValue())
    }

    @Test
    fun testOperationTypeSuccessAsync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel/uuid/sampleUUID"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "message": "OK",
                          "payload": {
                            "age": 20,
                            "status": "online"
                          },
                          "service": "Presence"
                        }
                        """.trimIndent()
                    )
                )
        )

        val atomic = AtomicInteger(0)

        pubnub.getPresenceState(
            channels = listOf("testChannel"),
            uuid = "sampleUUID"
        ).async { _, status ->
            if (status.operation == PNOperationType.PNGetState) {
                atomic.incrementAndGet()
            }
        }

        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAtomic(atomic, IsEqual.equalTo(1))
    }

    @Test
    fun testBlankSubKeySync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel/uuid/sampleUUID"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "message": "OK",
                          "payload": {
                            "age": 20,
                            "status": "online"
                          },
                          "service": "Presence"
                        }
                        """.trimIndent()
                    )
                )
        )

        pubnub.configuration.subscribeKey = " "

        try {
            pubnub.getPresenceState(
                channels = listOf("testChannel"),
                uuid = "sampleUUID"
            ).sync()!!
        } catch (e: Exception) {
            assertPnException(PubNubError.SUBSCRIBE_KEY_MISSING, e)
        }
    }

    @Test
    fun testEmptySubKeySync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel/uuid/sampleUUID"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "message": "OK",
                          "payload": {
                            "age": 20,
                            "status": "online"
                          },
                          "service": "Presence"
                        }
                        """.trimIndent()
                    )
                )
        )

        pubnub.configuration.subscribeKey = ""

        try {
            pubnub.getPresenceState(
                channels = listOf("testChannel"),
                uuid = "sampleUUID"
            ).sync()!!
        } catch (e: Exception) {
            assertPnException(PubNubError.SUBSCRIBE_KEY_MISSING, e)
        }
    }
}
