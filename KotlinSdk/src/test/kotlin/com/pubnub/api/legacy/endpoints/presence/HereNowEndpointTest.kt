package com.pubnub.apikt.legacy.endpoints.presence

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.pubnub.apikt.PubNubError.SUBSCRIBE_KEY_MISSING
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.enums.PNOperationType.PNHereNowOperation
import com.pubnub.apikt.legacy.BaseTest
import com.pubnub.apikt.models.consumer.presence.PNHereNowResult
import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.atomic.AtomicInteger

class HereNowEndpointTest : BaseTest() {

    @Test
    fun testMultipleChannelStateSync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub_key/mySubscribeKey/channel/ch1,ch2")).willReturn(
                aResponse().withBody(
                    """
                    {
                      "status": 200,
                      "message": "OK",
                      "payload": {
                        "total_occupancy": 3,
                        "total_channels": 2,
                        "channels": {
                          "ch1": {
                            "occupancy": 1,
                            "uuids": [
                              {
                                "uuid": "user1",
                                "state": {
                                  "age": 10
                                }
                              }
                            ]
                          },
                          "ch2": {
                            "occupancy": 2,
                            "uuids": [
                              {
                                "uuid": "user1",
                                "state": {
                                  "age": 10
                                }
                              },
                              {
                                "uuid": "user3",
                                "state": {
                                  "age": 30
                                }
                              }
                            ]
                          }
                        }
                      },
                      "service": "Presence"
                    }
                    """.trimIndent()
                )
            )
        )

        val response = pubnub.hereNow(
            channels = listOf("ch1", "ch2"),
            includeState = true
        ).sync()!!

        assertEquals(response.totalChannels, 2)
        assertEquals(response.totalOccupancy, 3)

        assertEquals(response.channels["ch1"]!!.channelName, "ch1")
        assertEquals(response.channels["ch1"]!!.occupancy, 1)
        assertEquals(response.channels["ch1"]!!.occupants.size, 1)
        assertEquals(response.channels["ch1"]!!.occupants[0].uuid, "user1")
        assertEquals(response.channels["ch1"]!!.occupants[0].state.toString(), """{"age":10}""")

        assertEquals(response.channels["ch2"]!!.channelName, "ch2")
        assertEquals(response.channels["ch2"]!!.occupancy, 2)
        assertEquals(response.channels["ch2"]!!.occupants.size, 2)
        assertEquals(response.channels["ch2"]!!.occupants[0].uuid, "user1")
        assertEquals(response.channels["ch2"]!!.occupants[0].state.toString(), """{"age":10}""")
        assertEquals(response.channels["ch2"]!!.occupants[1].uuid, "user3")
        assertEquals(response.channels["ch2"]!!.occupants[1].state.toString(), """{"age":30}""")

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("1", requests[0].queryParameter("state").firstValue())
    }

    @Test
    fun testMultipleChannelSync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub_key/mySubscribeKey/channel/ch1,ch2"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "status": 200,
                              "message": "OK",
                              "payload": {
                                "total_occupancy": 3,
                                "total_channels": 2,
                                "channels": {
                                  "ch1": {
                                    "occupancy": 1,
                                    "uuids": [
                                      {
                                        "uuid": "user1"
                                      }
                                    ]
                                  },
                                  "ch2": {
                                    "occupancy": 2,
                                    "uuids": [
                                      {
                                        "uuid": "user1"
                                      },
                                      {
                                        "uuid": "user3"
                                      }
                                    ]
                                  }
                                }
                              },
                              "service": "Presence"
                            }
                        """.trimIndent()
                    )
                )
        )

        val response = pubnub.hereNow(
            channels = listOf("ch1", "ch2"),
            includeState = true
        ).sync()!!

        assertEquals(response.totalChannels, 2)
        assertEquals(response.totalOccupancy, 3)

        assertEquals(response.channels["ch1"]!!.channelName, "ch1")
        assertEquals(response.channels["ch1"]!!.occupancy, 1)
        assertEquals(response.channels["ch1"]!!.occupants.size, 1)
        assertEquals(response.channels["ch1"]!!.occupants[0].uuid, "user1")
        assertNull(response.channels["ch1"]!!.occupants[0].state)

        assertEquals(response.channels["ch2"]!!.channelName, "ch2")
        assertEquals(response.channels["ch2"]!!.occupancy, 2)
        assertEquals(response.channels["ch2"]!!.occupants.size, 2)
        assertEquals(response.channels["ch2"]!!.occupants[0].uuid, "user1")
        assertNull(response.channels["ch2"]!!.occupants[0].state)
        assertEquals(response.channels["ch2"]!!.occupants[1].uuid, "user3")
        assertNull(response.channels["ch2"]!!.occupants[1].state)

        val requests = findAll(getRequestedFor(anyUrl()))
        assertEquals(1, requests.size)
        assertEquals("1", requests[0].queryParameter("state").firstValue())
    }

    @Test
    fun testMultipleChannelWithoutStateSync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub_key/mySubscribeKey/channel/game1,game2"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "status": 200,
                              "message": "OK",
                              "payload": {
                                "channels": {
                                  "game1": {
                                    "uuids": [
                                      "a3ffd012-a3b9-478c-8705-64089f24d71e"
                                    ],
                                    "occupancy": 1
                                  }
                                },
                                "total_channels": 1,
                                "total_occupancy": 1
                              },
                              "service": "Presence"
                            }
                        """.trimIndent()
                    )
                )
        )

        val response = pubnub.hereNow(
            channels = listOf("game1", "game2"),
            includeState = false
        ).sync()!!

        assertEquals(response.totalChannels, 1)
        assertEquals(response.totalOccupancy, 1)

        assertEquals(response.channels["game1"]!!.channelName, "game1")
        assertEquals(response.channels["game1"]!!.occupancy, 1)
        assertEquals(response.channels["game1"]!!.occupants.size, 1)
        assertEquals(
            response.channels["game1"]!!.occupants[0].uuid,
            "a3ffd012-a3b9-478c-8705-64089f24d71e"
        )
        assertNull(response.channels["game1"]!!.occupants[0].state)
    }

    @Test
    fun testMultipleChannelWithoutStateUUIDsSync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub_key/mySubscribeKey/channel/game1,game2"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "message": "OK",
                          "payload": {
                            "channels": {
                              "game1": {
                                "occupancy": 1
                              }
                            },
                            "total_channels": 1,
                            "total_occupancy": 1
                          },
                          "service": "Presence"
                        }
                        """.trimIndent()
                    )
                )
        )

        val response = pubnub.hereNow(
            channels = listOf("game1", "game2"),
            includeState = false,
            includeUUIDs = false
        ).sync()!!

        assertEquals(response.totalChannels, 1)
        assertEquals(response.totalOccupancy, 1)

        assertEquals(response.channels["game1"]!!.channelName, "game1")
        assertEquals(response.channels["game1"]!!.occupancy, 1)
        assertTrue(response.channels["game1"]!!.occupants.isEmpty())

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("1", requests[0].queryParameter("disable_uuids").firstValue())
    }

    @Test
    fun testSingularChannelWithoutStateUUIDsSync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub_key/mySubscribeKey/channel/game1"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "status": 200,
                              "message": "OK",
                              "service": "Presence",
                              "occupancy": 3
                            }
                        """.trimIndent()
                    )
                )
        )

        val response = pubnub.hereNow(
            channels = listOf("game1"),
            includeUUIDs = false
        ).sync()!!

        assertEquals(response.totalChannels, 1)
        assertEquals(response.totalOccupancy, 3)

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("1", requests[0].queryParameter("disable_uuids").firstValue())
    }

    @Test
    fun testSingularChannelWithoutStateSync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub_key/mySubscribeKey/channel/game1"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "status": 200,
                              "message": "OK",
                              "service": "Presence",
                              "uuids": [
                                "a3ffd012-a3b9-478c-8705-64089f24d71e"
                              ],
                              "occupancy": 1
                            }
                        """.trimIndent()
                    )
                )
        )

        val response = pubnub.hereNow(
            channels = listOf("game1"),
            includeState = false
        ).sync()!!

        assertEquals(response.totalChannels, 1)
        assertEquals(response.totalOccupancy, 1)
        assertEquals(response.channels.size, 1)
        assertEquals(response.channels["game1"]!!.occupancy, 1)
        assertEquals(response.channels["game1"]!!.occupants.size, 1)
        assertEquals(
            response.channels["game1"]!!.occupants[0].uuid,
            "a3ffd012-a3b9-478c-8705-64089f24d71e"
        )
        assertEquals(response.channels["game1"]!!.occupants[0].state, null)
    }

    @Test
    fun testSingularChannelSync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub_key/mySubscribeKey/channel/game1"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "status": 200,
                              "message": "OK",
                              "service": "Presence",
                              "uuids": [
                                {
                                  "uuid": "a3ffd012-a3b9-478c-8705-64089f24d71e",
                                  "state": {
                                    "age": 10
                                  }
                                }
                              ],
                              "occupancy": 1
                            }
                        """.trimIndent()
                    )
                )
        )
        val response = pubnub.hereNow(
            channels = listOf("game1"),
            includeState = true
        ).sync()!!

        assertEquals(response.totalChannels, 1)
        assertEquals(response.totalOccupancy, 1)
        assertEquals(response.channels.size, 1)
        assertEquals(response.channels["game1"]!!.occupancy, 1)
        assertEquals(response.channels["game1"]!!.occupants.size, 1)
        assertEquals(
            response.channels["game1"]!!.occupants[0].uuid,
            "a3ffd012-a3b9-478c-8705-64089f24d71e"
        )
        assertEquals(response.channels["game1"]!!.occupants[0].state.toString(), """{"age":10}""")
        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("1", requests[0].queryParameter("state").firstValue())
    }

    @Test
    fun testSingularChannelAndGroupSync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub_key/mySubscribeKey/channel/game1"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "status": 200,
                              "message": "OK",
                              "payload": {
                                "channels": {},
                                "total_channels": 0,
                                "total_occupancy": 0
                              },
                              "service": "Presence"
                            }
                        """.trimIndent()
                    )
                )
        )

        val response = pubnub.hereNow(
            channels = listOf("game1"),
            channelGroups = listOf("grp1"),
            includeState = true
        ).sync()!!

        assertEquals(response.totalOccupancy, 0)
    }

    @Test
    fun testIsAuthRequiredSuccessSync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub_key/mySubscribeKey/channel/ch1,ch2"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "status": 200,
                              "message": "OK",
                              "payload": {
                                "total_occupancy": 3,
                                "total_channels": 2,
                                "channels": {
                                  "ch1": {
                                    "occupancy": 1,
                                    "uuids": [
                                      {
                                        "uuid": "user1",
                                        "state": {
                                          "age": 10
                                        }
                                      }
                                    ]
                                  },
                                  "ch2": {
                                    "occupancy": 2,
                                    "uuids": [
                                      {
                                        "uuid": "user1",
                                        "state": {
                                          "age": 10
                                        }
                                      },
                                      {
                                        "uuid": "user3",
                                        "state": {
                                          "age": 30
                                        }
                                      }
                                    ]
                                  }
                                }
                              },
                              "service": "Presence"
                            }
                        """.trimIndent()
                    )
                )
        )

        pubnub.configuration.authKey = "myKey"

        pubnub.hereNow(
            channels = listOf("ch1", "ch2"),
            includeState = true
        ).sync()!!

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("myKey", requests[0].queryParameter("auth").firstValue())
    }

    @Test
    fun testOperationTypeSuccessAsync() {
        stubFor(
            get(urlPathMatching("/v2/presence/sub_key/mySubscribeKey.*"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "message": "OK",
                          "payload": {
                            "channels": {
                              "ch1": {
                                "occupancy": 1,
                                "uuids": [
                                  "myUUID"
                                ]
                              }
                            },
                            "total_channels": 1,
                            "total_occupancy": 1
                          },
                          "service": "Presence"
                        }
                        """.trimIndent()
                    )
                )
        )

        val atomic = AtomicInteger(0)

        pubnub.hereNow().async { result, status ->
            if (status.operation == PNHereNowOperation) {
                assertEquals(1, result!!.channels.size)
                assertEquals(pubnub.configuration.userId.value, result.channels["ch1"]!!.occupants[0].uuid)
                atomic.incrementAndGet()
            }
        }

        Awaitility.await().atMost(5, SECONDS).untilAtomic(atomic, IsEqual.equalTo(1))
    }

    @Test
    fun testEmptySubKeySync() {
        pubnub.configuration.subscribeKey = ""

        var response: PNHereNowResult? = null
        try {
            response = pubnub.hereNow(
                channels = listOf("ch1", "ch2"),
                includeState = true
            ).sync()
        } catch (e: Exception) {
            assertNull(response)
            assertTrue((e as PubNubException).pubnubError == SUBSCRIBE_KEY_MISSING)
        }
    }
}
