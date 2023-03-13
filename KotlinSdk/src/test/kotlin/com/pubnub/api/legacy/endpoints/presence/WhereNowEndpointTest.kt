package com.pubnub.apikt.legacy.endpoints.presence

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.pubnub.apikt.CommonUtils.assertPnException
import com.pubnub.apikt.CommonUtils.failTest
import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.enums.PNStatusCategory
import com.pubnub.apikt.legacy.BaseTest
import org.awaitility.Awaitility
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.core.IsEqual
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.atomic.AtomicInteger

class WhereNowEndpointTest : BaseTest() {

    @Test
    fun testSyncSuccess() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
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
                          "service": "Presence"
                        }
                        """.trimIndent()
                    )
                )
        )

        val response = pubnub.whereNow().sync()!!
        assertThat(response.channels, Matchers.contains("a", "b"))
    }

    @Test
    fun testSyncSuccessCustomUUID() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/customUUID"))
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
                              "service": "Presence"
                            }
                        """.trimIndent()
                    )
                )
        )

        val response = pubnub.whereNow(
            uuid = "customUUID"
        ).sync()!!

        assertThat(response.channels, Matchers.contains("a", "b"))
    }

    @Test
    fun testSyncBrokenWithString() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
                .willReturn(
                    aResponse().withBody(
                        "{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\"" +
                            ": [zimp]}, \"service\": \"Presence\"}"
                    )
                )
        )

        try {
            pubnub.whereNow().sync()
            failTest()
        } catch (e: Exception) {
            assertPnException(PubNubError.PARSING_ERROR, e)
        }
    }

    @Test
    fun testSyncBrokenWithoutJSON() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
                .willReturn(
                    aResponse().withBody(
                        "{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": " +
                            "zimp}, \"service\": \"Presence\"}"
                    )
                )
        )

        try {
            pubnub.whereNow().sync()
            failTest()
        } catch (e: Exception) {
            assertPnException(PubNubError.PARSING_ERROR, e)
        }
    }

    @Test
    fun testSyncBrokenWithout200() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
                .willReturn(
                    aResponse()
                        .withStatus(404)
                        .withBody(
                            "{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": [\"a\",\"b\"]}," +
                                " \"service\": \"Presence\"}"
                        )
                )
        )

        try {
            pubnub.whereNow().sync()
            failTest()
        } catch (e: Exception) {
            assertPnException(PubNubError.HTTP_ERROR, e)
        }
    }

    @Test
    fun testAsyncSuccess() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
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
                              "service": "Presence"
                            }
                        """.trimIndent()
                    )
                )
        )

        val atomic = AtomicInteger(0)

        pubnub.whereNow().async { result, status ->
            assertFalse(status.error)
            assertThat(result!!.channels, Matchers.contains("a", "b"))
            atomic.incrementAndGet()
        }

        Awaitility.await().atMost(5, SECONDS).untilAtomic(atomic, IsEqual.equalTo(1))
    }

    @Test
    fun testAsyncBrokenWithString() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
                .willReturn(
                    aResponse().withBody(
                        "{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": " +
                            "[zimp]}, \"service\": \"Presence\"}"
                    )
                )
        )

        val atomic = AtomicInteger(0)

        pubnub.whereNow().async { _, status ->
            assertTrue(status.error)
            assertPnException(PubNubError.PARSING_ERROR, status)
            atomic.incrementAndGet()
        }

        Awaitility.await().atMost(5, SECONDS).untilAtomic(atomic, IsEqual.equalTo(1))
    }

    @Test
    fun testAsyncBrokenWithoutJSON() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
                .willReturn(
                    aResponse().withBody(
                        "{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": " +
                            "zimp}, \"service\": \"Presence\"}"
                    )
                )
        )

        val atomic = AtomicInteger(0)

        pubnub.whereNow().async { _, status ->
            assertTrue(status.error)
            assertPnException(PubNubError.PARSING_ERROR, status)
            atomic.incrementAndGet()
        }

        Awaitility.await().atMost(5, SECONDS).untilAtomic(atomic, IsEqual.equalTo(1))
    }

    @Test
    fun testAsyncBrokenWithout200() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
                .willReturn(
                    aResponse()
                        .withStatus(400)
                        .withBody(
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
                              "service": "Presence"
                            }
                            """.trimIndent()
                        )
                )
        )

        val atomic = AtomicInteger(0)

        pubnub.whereNow().async { _, status ->
            assertTrue(status.error)
            assertPnException(PubNubError.HTTP_ERROR, status)
            assertEquals(PNStatusCategory.PNBadRequestCategory, status.category)
            atomic.incrementAndGet()
        }

        Awaitility.await().atMost(5, SECONDS).untilAtomic(atomic, IsEqual.equalTo(1))
    }

    @Test
    fun testIsAuthRequiredSuccessSync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
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
                              "service": "Presence"
                            }
                        """.trimIndent()
                    )
                )
        )

        pubnub.configuration.authKey = "myKey"

        pubnub.whereNow().sync()!!

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("myKey", requests[0].queryParameter("auth").firstValue())
    }

    @Test
    fun testBlankSubKeySync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
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
                              "service": "Presence"
                            }
                        """.trimIndent()
                    )
                )
        )

        pubnub.configuration.subscribeKey = " "

        try {
            pubnub.whereNow().sync()!!
        } catch (e: Exception) {
            assertPnException(PubNubError.SUBSCRIBE_KEY_MISSING, e)
        }
    }

    @Test
    fun testEmptySubKeySync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
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
                              "service": "Presence"
                            }
                        """.trimIndent()
                    )
                )
        )

        pubnub.configuration.subscribeKey = ""

        try {
            pubnub.whereNow().sync()!!
        } catch (e: Exception) {
            assertPnException(PubNubError.SUBSCRIBE_KEY_MISSING, e)
        }
    }

    @Test
    fun testNullPayloadSync() {
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
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
            pubnub.whereNow().sync()!!
        } catch (e: Exception) {
            assertPnException(PubNubError.PARSING_ERROR, e)
        }
    }
}
