package com.pubnub.apikt.legacy.endpoints.pubsub

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.pubnub.apikt.CommonUtils.assertPnException
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.callbacks.SubscribeCallback
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.legacy.BaseTest
import com.pubnub.apikt.listen
import com.pubnub.apikt.models.consumer.PNStatus
import com.pubnub.apikt.models.consumer.pubsub.PNMessageResult
import com.pubnub.apikt.models.consumer.pubsub.PNPresenceEventResult
import com.pubnub.apikt.models.consumer.pubsub.PNSignalResult
import com.pubnub.apikt.models.consumer.pubsub.message_actions.PNMessageActionResult
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.junit.Assert.*
import org.junit.Test
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class SignalTest : BaseTest() {

    @Test
    fun testSignalGetSuccessSync() {
        stubFor(
            get(urlMatching("/signal/myPublishKey/mySubscribeKey/0/coolChannel.*"))
                .willReturn(
                    aResponse()
                        .withBody(
                            """
                            [
                              1,
                              "Sent",
                              "1000"
                            ]
                            """.trimIndent()
                        )
                )
        )

        val payload = mapOf("text" to "hello")

        pubnub.signal(
            channel = "coolChannel",
            message = payload
        ).sync()

        val requests = findAll(getRequestedFor(urlMatching("/signal.*")))
        assertEquals(1, requests.size)
        val request = requests[0]
        assertEquals("myUUID", request.queryParameter("uuid").firstValue())

        val httpUrl = request.absoluteUrl.toHttpUrlOrNull()
        var decodedSignalPayload: String? = null
        if (httpUrl != null) {
            decodedSignalPayload = httpUrl.pathSegments[httpUrl.pathSize - 1]
        }
        assertEquals(pubnub.mapper.toJson(payload), decodedSignalPayload)
    }

    @Test
    fun testSignalGetSuccessAsync() {
        stubFor(
            get(urlMatching("/signal/myPublishKey/mySubscribeKey/0/coolChannel.*"))
                .willReturn(
                    aResponse()
                        .withBody(
                            """
                            [
                              1,
                              "Sent",
                              "1000"
                            ]
                            """.trimIndent()
                        )
                )
        )

        val payload = UUID.randomUUID().toString()

        val success = AtomicBoolean()

        pubnub.signal(
            channel = "coolChannel",
            message = payload
        ).async { result, status ->
            result!!
            assertFalse(status.error)
            assertEquals(PNOperationType.PNSignalOperation, status.operation)
            assertEquals("1000", result.timetoken.toString())
            success.set(true)
        }

        success.listen()
    }

    @Test
    fun testSignalSuccessReceive() {
        stubFor(
            get(urlMatching("/v2/subscribe/mySubscribeKey/coolChannel/0.*"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "m": [
                                {
                                  "c": "coolChannel",
                                  "f": "0",
                                  "i": "uuid",
                                  "d": "hello",
                                  "e": 1,
                                  "p": {
                                    "t": 1000,
                                    "r": 1
                                  },
                                  "k": "mySubscribeKey",
                                  "b": "coolChannel"
                                }
                              ],
                              "t": {
                                "r": "56",
                                "t": 1000
                              }
                            }
                        """.trimIndent()
                    )
                )
        )

        val success = AtomicBoolean()

        pubnub.addListener(object : SubscribeCallback() {
            override fun signal(pubnub: PubNub, pnSignalResult: PNSignalResult) {
                assertEquals("coolChannel", pnSignalResult.channel)
                assertEquals("hello", pnSignalResult.message.asString)
                assertEquals("uuid", pnSignalResult.publisher)
                success.set(true)
            }

            override fun status(pubnub: PubNub, pnStatus: PNStatus) {}
            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {}
            override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {}
            override fun messageAction(pubnub: PubNub, pnMessageActionResult: PNMessageActionResult) {}
        })

        pubnub.subscribe(
            channels = listOf("coolChannel")
        )

        success.listen()
    }

    @Test
    fun testSignalFailBlankChannel() {
        try {
            pubnub.signal(
                channel = " ",
                message = UUID.randomUUID().toString()
            ).sync()!!
            throw RuntimeException()
        } catch (e: PubNubException) {
            assertPnException(PubNubError.CHANNEL_MISSING, e)
        }
    }

    @Test
    fun testSignalTelemetryParam() {
        stubFor(
            get(urlMatching("/signal/myPublishKey/mySubscribeKey/0/coolChannel.*")).willReturn(
                aResponse().withBody(
                    """
                    [
                      1,
                      "Sent",
                      "1000"
                    ]
                    """.trimIndent()
                )
            )
        )
        stubFor(
            get(urlMatching("/time/0.*"))
                .willReturn(aResponse().withBody("[1000]"))
        )
        pubnub.signal(
            channel = "coolChannel",
            message = UUID.randomUUID().toString()
        ).sync()!!

        pubnub.time()
            .sync()

        val requests = findAll(getRequestedFor(urlMatching("/time/0.*")))
        assertEquals(1, requests.size)
        val request = requests[0]
        assertTrue(request.queryParameter("l_sig").isPresent)
    }
}
