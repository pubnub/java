package com.pubnub.apikt.legacy.managers

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.google.gson.reflect.TypeToken
import com.pubnub.apikt.CommonUtils.emptyJson
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.PubNubUtil
import com.pubnub.apikt.callbacks.SubscribeCallback
import com.pubnub.apikt.enums.PNHeartbeatNotificationOptions
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.enums.PNStatusCategory
import com.pubnub.apikt.legacy.BaseTest
import com.pubnub.apikt.models.consumer.PNStatus
import com.pubnub.apikt.models.consumer.pubsub.PNMessageResult
import com.pubnub.apikt.models.consumer.pubsub.PNPresenceEventResult
import com.pubnub.apikt.toCsv
import org.awaitility.Awaitility
import org.hamcrest.Matchers
import org.hamcrest.core.IsEqual
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class SubscriptionManagerTest : BaseTest() {

    @Test
    fun testGetSubscribedChannels() {
        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
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
                                    "text": "Message"
                                  },
                                  "b": "coolChan-bnel"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )

        pubnub.subscribe(
            channels = listOf("ch1", "ch2")
        )

        val channels = pubnub.getSubscribedChannels()
        assertTrue(channels.contains("ch1"))
        assertTrue(channels.contains("ch2"))
    }

    @Test
    fun testGetSubscribedEmptyChannel() {
        val gotMessages = AtomicInteger()

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
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
                                    "text": "Message"
                                  },
                                  "b": "coolChan-bnel"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )

        pubnub.subscribe(
            channels = listOf("")
        )

        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                gotMessages.addAndGet(1)
            }

            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                gotMessages.addAndGet(1)
            }

            override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {
                gotMessages.addAndGet(1)
            }
        })

        Awaitility.await()
            .atMost(3, TimeUnit.SECONDS)
            .untilAtomic(gotMessages, IsEqual.equalTo(0))
    }

    @Test
    fun testGetSubscribedEmptyChannelGroup() {
        val gotMessages = AtomicInteger()

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
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
                                    "text": "Message"
                                  },
                                  "b": "coolChan-bnel"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )

        pubnub.subscribe(
            channelGroups = listOf("")
        )

        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                gotMessages.addAndGet(1)
            }

            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                gotMessages.addAndGet(1)
            }

            override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {
                gotMessages.addAndGet(1)
            }
        })

        Awaitility.await()
            .atMost(3, TimeUnit.SECONDS)
            .untilAtomic(gotMessages, IsEqual.equalTo(0))
    }

    @Test
    fun testGetSubscribedChannelGroups() {
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

        pubnub.subscribe(
            channelGroups = listOf("cg1", "cg2")
        )

        val groups: List<String> = pubnub.getSubscribedChannelGroups()
        assertTrue(groups.contains("cg1"))
        assertTrue(groups.contains("cg2"))
    }

    @Test
    fun testPubNubUnsubscribeAll() {
        stubFor(
            get(urlPathMatching("/v2/subscribe/mySubscribeKey/.*"))
                .willReturn(emptyJson())
        )

        pubnub.subscribe(
            channels = listOf("ch1", "ch2"),
            channelGroups = listOf("cg1", "cg2"),
            withPresence = true
        )

        var channels = pubnub.getSubscribedChannels()
        assertTrue(channels.contains("ch1"))
        assertTrue(channels.contains("ch2"))

        var groups = pubnub.getSubscribedChannelGroups()
        assertTrue(groups.contains("cg1"))
        assertTrue(groups.contains("cg2"))

        pubnub.unsubscribeAll()

        channels = pubnub.getSubscribedChannels()
        assertEquals(0, channels.size)

        groups = pubnub.getSubscribedChannelGroups()
        assertEquals(0, groups.size)
    }

    @Test
    fun testSubscribeBuilder() {
        val gotStatus = AtomicInteger()

        val gotMessage = AtomicBoolean()
        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
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
                                  "i": "Publisher-A",
                                  "p": {
                                    "t": "14607577960925503",
                                    "r": 1
                                  },
                                  "o": {
                                    "t": "14737141991877032",
                                    "r": 2
                                  },
                                  "k": "sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f",
                                  "c": "coolChannel",
                                  "d": {
                                    "text": "Message"
                                  },
                                  "b": "coolChannel"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )
        pubnub.addListener(object : SubscribeCallback() {

            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                if (pnStatus.category == PNStatusCategory.PNConnectedCategory) {
                    gotStatus.addAndGet(1)
                }
            }

            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                val requests = findAll(getRequestedFor(urlMatching("/v2/subscribe.*")))
                assertTrue(requests.size > 0)
                assertEquals("Message", pubnub.mapper.elementToString(pnMessageResult.message, "text"))
                assertEquals("coolChannel", pnMessageResult.channel)
                assertEquals(null, pnMessageResult.subscription)
                assertEquals("Publisher-A", pnMessageResult.publisher)
                gotMessage.set(true)
            }
        })

        pubnub.subscribe(
            channels = listOf("ch1", "ch2")
        )

        Awaitility.await()
            .atMost(3, TimeUnit.SECONDS)
            .untilAtomic(gotMessage, IsEqual.equalTo(true))

        Awaitility.await()
            .atMost(3, TimeUnit.SECONDS)
            .untilAtomic(gotStatus, IsEqual.equalTo(1))
    }

    @Test
    fun testSubscribeDuplicateDisabledBuilder() {
        val gotMessages = AtomicInteger()

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .withQueryParam("tt", matching("0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "t": {
                                "t": "5",
                                "r": 1
                              },
                              "m": [
                                {
                                  "a": "4",
                                  "f": 0,
                                  "i": "Publisher-A",
                                  "p": {
                                    "t": "14607577960925503",
                                    "r": 1
                                  },
                                  "o": {
                                    "t": "14737141991877032",
                                    "r": 2
                                  },
                                  "k": "sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f",
                                  "c": "coolChannel",
                                  "d": {
                                    "text": "Message"
                                  },
                                  "b": "coolChannel"
                                },
                                {
                                  "a": "4",
                                  "f": 0,
                                  "i": "Publisher-A",
                                  "p": {
                                    "t": "14607577960925503",
                                    "r": 1
                                  },
                                  "o": {
                                    "t": "14737141991877032",
                                    "r": 2
                                  },
                                  "k": "sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f",
                                  "c": "coolChannel",
                                  "d": {
                                    "text": "Message"
                                  },
                                  "b": "coolChannel"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .withQueryParam("tt", matching("5"))
                .willReturn(emptyJson())
        )

        pubnub.addListener(object : SubscribeCallback() {
            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                gotMessages.addAndGet(1)
            }

            override fun status(pubnub: PubNub, pnStatus: PNStatus) {}
        })

        pubnub.subscribe(
            channels = listOf("ch1", "ch2")
        )

        Awaitility.await()
            .atMost(3, TimeUnit.SECONDS)
            .untilAtomic(gotMessages, IsEqual.equalTo(2))
    }

    @Test
    fun testSubscribeDuplicateBuilder() {
        pubnub.configuration.dedupOnSubscribe = true

        val gotMessages = AtomicInteger()

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .withQueryParam("tt", matching("0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "t": {
                                "t": "5",
                                "r": 1
                              },
                              "m": [
                                {
                                  "a": "4",
                                  "f": 0,
                                  "i": "Publisher-A",
                                  "p": {
                                    "t": "14607577960925503",
                                    "r": 1
                                  },
                                  "o": {
                                    "t": "14737141991877032",
                                    "r": 2
                                  },
                                  "k": "sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f",
                                  "c": "coolChannel",
                                  "d": {
                                    "text": "Message"
                                  },
                                  "b": "coolChannel"
                                },
                                {
                                  "a": "4",
                                  "f": 0,
                                  "i": "Publisher-A",
                                  "p": {
                                    "t": "14607577960925503",
                                    "r": 1
                                  },
                                  "o": {
                                    "t": "14737141991877032",
                                    "r": 2
                                  },
                                  "k": "sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f",
                                  "c": "coolChannel",
                                  "d": {
                                    "text": "Message"
                                  },
                                  "b": "coolChannel"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .withQueryParam("tt", matching("5"))
                .willReturn(emptyJson())
        )

        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {}
            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                gotMessages.addAndGet(1)
            }
        })

        pubnub.subscribe(
            channels = listOf("ch1", "ch2")
        )

        Awaitility.await()
            .atMost(3, TimeUnit.SECONDS)
            .untilAtomic(gotMessages, IsEqual.equalTo(1))
    }

    @Test
    fun testSubscribeDuplicateWithLimitBuilder() {
        pubnub.configuration.dedupOnSubscribe = true
        pubnub.configuration.maximumMessagesCacheSize = 1

        val gotMessages = AtomicInteger()

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .withQueryParam("tt", matching("0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "t": {
                                "t": "5",
                                "r": 1
                              },
                              "m": [
                                {
                                  "a": "4",
                                  "f": 0,
                                  "i": "Publisher-A",
                                  "p": {
                                    "t": "14607577960925503",
                                    "r": 1
                                  },
                                  "o": {
                                    "t": "14737141991877032",
                                    "r": 2
                                  },
                                  "k": "sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f",
                                  "c": "coolChannel",
                                  "d": {
                                    "text": "Message1"
                                  },
                                  "b": "coolChannel"
                                },
                                {
                                  "a": "4",
                                  "f": 0,
                                  "i": "Publisher-A",
                                  "p": {
                                    "t": "14607577960925503",
                                    "r": 1
                                  },
                                  "o": {
                                    "t": "14737141991877032",
                                    "r": 2
                                  },
                                  "k": "sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f",
                                  "c": "coolChannel",
                                  "d": {
                                    "text": "Message2"
                                  },
                                  "b": "coolChannel"
                                },
                                {
                                  "a": "4",
                                  "f": 0,
                                  "i": "Publisher-A",
                                  "p": {
                                    "t": "14607577960925503",
                                    "r": 1
                                  },
                                  "o": {
                                    "t": "14737141991877032",
                                    "r": 2
                                  },
                                  "k": "sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f",
                                  "c": "coolChannel",
                                  "d": {
                                    "text": "Message1"
                                  },
                                  "b": "coolChannel"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .withQueryParam("tt", matching("5"))
                .willReturn(notFound())
        )

        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {}
            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                gotMessages.addAndGet(1)
            }
        })

        pubnub.subscribe(
            channels = listOf("ch1", "ch2")
        )

        Awaitility.await()
            .atMost(3, TimeUnit.SECONDS)
            .untilAtomic(gotMessages, IsEqual.equalTo(3))
    }

    @Test
    fun testQueueNotificationsBuilderNoThresholdSpecified() {
        pubnub.configuration.requestMessageCountThreshold = null

        val gotStatus = AtomicBoolean()

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
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
                                  "o": {
                                    "t": "14737141991877032",
                                    "r": 2
                                  },
                                  "k": "sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f",
                                  "c": "coolChannel",
                                  "d": {
                                    "text": "Message"
                                  },
                                  "b": "coolChannel"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )

        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                if (pnStatus.category == PNStatusCategory.PNRequestMessageCountExceededCategory) {
                    gotStatus.set(true)
                }
            }
        })

        pubnub.subscribe(
            channels = listOf("ch1", "ch2")
        )

        Awaitility.await()
            .atMost(2, TimeUnit.SECONDS)
            .untilAtomic(gotStatus, IsEqual.equalTo(false))
    }

    @Test
    fun testQueueNotificationsBuilderBelowThreshold() {
        pubnub.configuration.requestMessageCountThreshold = 10

        val gotStatus = AtomicBoolean()

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
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
                                  "o": {
                                    "t": "14737141991877032",
                                    "r": 2
                                  },
                                  "k": "sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f",
                                  "c": "coolChannel",
                                  "d": {
                                    "text": "Message"
                                  },
                                  "b": "coolChannel"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )

        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                if (pnStatus.category == PNStatusCategory.PNRequestMessageCountExceededCategory) {
                    gotStatus.set(true)
                }
            }
        })

        pubnub.subscribe(
            channels = listOf("ch1", "ch2")
        )

        Awaitility.await()
            .atMost(2, TimeUnit.SECONDS)
            .untilAtomic(gotStatus, IsEqual.equalTo(false))
    }

    @Test
    fun testQueueNotificationsBuilderThresholdMatched() {
        pubnub.configuration.requestMessageCountThreshold = 1

        val gotStatus = AtomicBoolean()

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
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
                                  "o": {
                                    "t": "14737141991877032",
                                    "r": 2
                                  },
                                  "k": "sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f",
                                  "c": "coolChannel",
                                  "d": {
                                    "text": "Message"
                                  },
                                  "b": "coolChannel"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )

        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                if (pnStatus.category == PNStatusCategory.PNRequestMessageCountExceededCategory) {
                    gotStatus.set(true)
                }
            }
        })

        pubnub.subscribe(
            channels = listOf("ch1", "ch2")
        )

        Awaitility.await()
            .atMost(2, TimeUnit.SECONDS)
            .untilAtomic(gotStatus, IsEqual.equalTo(true))
    }

    @Test
    fun testQueueNotificationsBuilderThresholdExceeded() {
        pubnub.configuration.requestMessageCountThreshold = 1

        val gotStatus = AtomicBoolean()

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "m": [
                                {
                                  "a": "4",
                                  "b": "coolChannel",
                                  "c": "coolChannel",
                                  "d": {
                                    "text": "Message"
                                  },
                                  "f": 0,
                                  "i": "Client-g5d4g",
                                  "k": "sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f",
                                  "o": {
                                    "r": 2,
                                    "t": "14737141991877032"
                                  },
                                  "p": {
                                    "r": 1,
                                    "t": "14607577960925503"
                                  }
                                },
                                {
                                  "a": "5",
                                  "b": "coolChannel2",
                                  "c": "coolChannel2",
                                  "d": {
                                    "text": "Message2"
                                  },
                                  "f": 0,
                                  "i": "Client-g5d4g",
                                  "k": "sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4g",
                                  "o": {
                                    "r": 2,
                                    "t": "14737141991877033"
                                  },
                                  "p": {
                                    "r": 1,
                                    "t": "14607577960925504"
                                  }
                                }
                              ],
                              "t": {
                                "r": 1,
                                "t": "14607577960932487"
                              }
                            }
                        """.trimIndent()
                    )
                )
        )

        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                if (pnStatus.category == PNStatusCategory.PNRequestMessageCountExceededCategory) {
                    gotStatus.set(true)
                }
            }
        })

        pubnub.subscribe(
            channels = listOf("ch1", "ch2")
        )

        Awaitility.await()
            .atMost(2, TimeUnit.SECONDS)
            .untilAtomic(gotStatus, IsEqual.equalTo(true))
    }

    @Test
    fun testSubscribeBuilderWithAccessManager403Error() {
        val gotStatus = AtomicInteger()

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .willReturn(
                    aResponse().withStatus(403).withBody(
                        """
                            {
                              "message": "Forbidden",
                              "payload": {
                                "channels": [
                                  "ch1",
                                  "ch2"
                                ],
                                "channel-groups": [
                                  ":cg1",
                                  ":cg2"
                                ]
                              },
                              "error": true,
                              "service": "Access Manager",
                              "status": 403
                            }
                        """.trimIndent()
                    )
                )
        )

        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                if (pnStatus.category == PNStatusCategory.PNAccessDeniedCategory) {
                    assertEquals(PNStatusCategory.PNAccessDeniedCategory, pnStatus.category)
                    assertEquals(listOf("ch1", "ch2"), pnStatus.affectedChannels)
                    assertEquals(listOf("cg1", "cg2"), pnStatus.affectedChannelGroups)
                    gotStatus.addAndGet(1)
                }
            }
        })

        pubnub.subscribe(
            channels = listOf("ch1", "ch2")
        )

        Awaitility.await()
            .atMost(2, TimeUnit.SECONDS)
            .untilAtomic(gotStatus, IsEqual.equalTo(1))
    }

    @Test
    fun testNamingSubscribeChannelGroupBuilder() {
        val gotStatus = AtomicBoolean()
        val gotMessage = AtomicBoolean()

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
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
                                    "text": "Message"
                                  },
                                  "b": "coolChannelGroup"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )

        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                if (pnStatus.category == PNStatusCategory.PNConnectedCategory) {
                    assertEquals(2, pnStatus.affectedChannels.size)
                    gotStatus.set(true)
                }
            }

            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                val requests = findAll(getRequestedFor(urlMatching("/v2/subscribe.*")))
                assertTrue(requests.size > 0)
                assertEquals("Message", pubnub.mapper.elementToString(pnMessageResult.message, "text"))
                assertEquals("coolChannel", pnMessageResult.channel)
                assertEquals("coolChannelGroup", pnMessageResult.subscription)
                gotMessage.set(true)
            }
        })

        pubnub.subscribe(
            channels = listOf("ch1", "ch2")
        )

        Awaitility.await().atMost(4, TimeUnit.SECONDS).untilTrue(gotMessage)
        Awaitility.await().atMost(4, TimeUnit.SECONDS).untilTrue(gotStatus)
    }

    @Test
    fun testPresenceSubscribeBuilder() {
        val gotStatus = AtomicInteger()
        val gotMessage = AtomicBoolean()

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "t": {
                                "t": "14614512228786519",
                                "r": 1
                              },
                              "m": [
                                {
                                  "a": "4",
                                  "f": 0,
                                  "p": {
                                    "t": "14614512228418349",
                                    "r": 2
                                  },
                                  "k": "sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f",
                                  "c": "coolChannel-pnpres",
                                  "d": {
                                    "action": "join",
                                    "timestamp": 1461451222,
                                    "uuid": "4a6d5df7-e301-4e73-a7b7-6af9ab484eb0",
                                    "occupancy": 1
                                  },
                                  "b": "coolChannel-pnpres"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )
        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                if (pnStatus.category == PNStatusCategory.PNConnectedCategory) {
                    gotStatus.addAndGet(1)
                }
            }

            override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {
                val requests = findAll(getRequestedFor(urlMatching("/v2/subscribe.*")))
                assertTrue(requests.size >= 1)
                assertEquals("coolChannel", pnPresenceEventResult.channel)
                assertEquals(null, pnPresenceEventResult.subscription)
                gotMessage.set(true)
            }
        })

        pubnub.subscribe(
            channels = listOf("ch1", "ch2")
        )

        Awaitility.await()
            .atMost(2, TimeUnit.SECONDS)
            .untilAtomic(gotMessage, IsEqual.equalTo(true))

        Awaitility.await()
            .atMost(2, TimeUnit.SECONDS)
            .untilAtomic(gotStatus, IsEqual.equalTo(1))
    }

    @Test
    fun testPresenceChannelGroupSubscribeBuilder() {
        val gotStatus = AtomicInteger()
        val gotMessage = AtomicBoolean()

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "t": {
                                "t": "14614512228786519",
                                "r": 1
                              },
                              "m": [
                                {
                                  "a": "4",
                                  "f": 0,
                                  "p": {
                                    "t": "14614512228418349",
                                    "r": 2
                                  },
                                  "k": "sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f",
                                  "c": "coolChannel-pnpres",
                                  "d": {
                                    "action": "join",
                                    "timestamp": 1461451222,
                                    "uuid": "4a6d5df7-e301-4e73-a7b7-6af9ab484eb0",
                                    "occupancy": 1
                                  },
                                  "b": "coolChannelGroup-pnpres"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )

        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                if (pnStatus.category == PNStatusCategory.PNConnectedCategory) {
                    gotStatus.addAndGet(1)
                }
            }

            override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {
                val requests = findAll(getRequestedFor(urlMatching("/v2/subscribe.*")))
                assertTrue(requests.size >= 1)
                assertEquals("coolChannel", pnPresenceEventResult.channel)
                assertEquals("coolChannelGroup", pnPresenceEventResult.subscription)
                gotMessage.set(true)
            }
        })

        pubnub.subscribe(
            channels = listOf("ch1", "ch2")
        )

        Awaitility.await()
            .atMost(2, TimeUnit.SECONDS)
            .untilAtomic(gotMessage, IsEqual.equalTo(true))

        Awaitility.await()
            .atMost(2, TimeUnit.SECONDS)
            .untilAtomic(gotStatus, IsEqual.equalTo(1))
    }

    @Test
    fun testSubscribeSlidingBuilder() {
        val gotMessage1 = AtomicBoolean()
        val gotMessage2 = AtomicBoolean()
        val gotMessage3 = AtomicBoolean()

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .withQueryParam("tt", matching("0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "t": {
                                "t": "3",
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
                                    "text": "Message"
                                  },
                                  "b": "coolChan-bnel"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )
        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .withQueryParam("tt", matching("3"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "t": {
                                "t": "10",
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
                                    "text": "Message3"
                                  },
                                  "b": "coolChan-bnel"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )
        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .withQueryParam("tt", matching("10"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "t": {
                                "t": "20",
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
                                    "text": "Message10"
                                  },
                                  "b": "coolChan-bnel"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .withQueryParam("tt", matching("20"))
                .willReturn(emptyJson())
        )

        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
            }

            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                when (pnMessageResult.message.asJsonObject["text"].asString) {
                    "Message" -> {
                        gotMessage1.set(true)
                    }
                    "Message3" -> {
                        gotMessage2.set(true)
                    }
                    "Message10" -> {
                        gotMessage3.set(true)
                    }
                }
            }
        })

        pubnub.subscribe(
            channels = listOf("ch1", "ch2")
        )

        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAtomic(
            gotMessage1,
            IsEqual.equalTo(true)
        )

        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAtomic(
            gotMessage2,
            IsEqual.equalTo(true)
        )

        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAtomic(
            gotMessage3,
            IsEqual.equalTo(true)
        )
    }

    @Test
    fun testSubscribeBuilderNumber() {
        val atomic = AtomicInteger(0)
        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
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
                                  "d": 10,
                                  "b": "coolChan-bnel"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )
        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {}
            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                val requests = findAll(getRequestedFor(urlMatching("/v2/subscribe.*")))
                assertTrue(requests.size >= 1)
                assertEquals(10, pnMessageResult.message.asInt)
                atomic.addAndGet(1)
            }
        })

        pubnub.subscribe(
            channels = listOf("ch1", "ch2")
        )

        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAtomic(atomic, Matchers.greaterThan(0))
    }

    @Test
    fun testSubscribeBuilderWithMetadata() {
        val atomic = AtomicInteger(0)
        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "t": {
                                "t": "14858178301085322",
                                "r": 7
                              },
                              "m": [
                                {
                                  "a": "4",
                                  "f": 512,
                                  "i": "02a7b822-220c-49b0-90c4-d9cbecc0fd85",
                                  "s": 1,
                                  "p": {
                                    "t": "14858178301075219",
                                    "r": 7
                                  },
                                  "k": "demo-36",
                                  "c": "chTest",
                                  "u": {
                                    "status_update": {
                                      "lat": 55.752023906250656,
                                      "lon": 37.61749036080494,
                                      "driver_id": 4722
                                    }
                                  },
                                  "d": {
                                    "City": "Goiania",
                                    "Name": "Marcelo"
                                  }
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )
        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {}
            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                val requests = findAll(getRequestedFor(urlMatching("/v2/subscribe.*")))
                assertTrue(requests.size >= 1)
                assertEquals(
                    """{"status_update":{"lat":55.752023906250656,"lon":37.61749036080494,"driver_id":4722}}""",
                    pnMessageResult.userMetadata.toString()
                )
                atomic.addAndGet(1)
            }
        })

        pubnub.subscribe(
            channels = listOf("ch1", "ch2")
        )

        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAtomic(atomic, Matchers.greaterThan(0))
    }

    @Test
    fun testSubscribeBuilderWithState() {
        val subscribeHits = AtomicInteger(0)
        val heartbeatHits = AtomicInteger(0)
        val expectedPayload = PubNubUtil.urlDecode(
            """%7B%22ch1%22%3A%5B%22p1%22%2C%22p2%22%5D%2C%22cg2%22%3A%5B%22p1%22%2C%22p2%22%5D%7D"""
        )

        val expectedMap = pubnub.mapper.fromJson<HashMap<String, Any>?>(
            expectedPayload,
            object : TypeToken<HashMap<String, Any>?>() {}.type
        )

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
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

        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch2,ch1/heartbeat"))
                .willReturn(emptyJson())
        )

        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1/uuid/myUUID/data"))
                .willReturn(emptyJson())
        )

        pubnub.configuration.presenceTimeout = 20
        pubnub.configuration.heartbeatNotificationOptions = PNHeartbeatNotificationOptions.ALL

        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                val heartbeatRequests = findAll(
                    getRequestedFor(
                        urlMatching(
                            """/v2/presence/sub-key/${pubnub.configuration.subscribeKey}/channel/ch2,ch1/heartbeat.*"""
                        )
                    )
                )
                val subscribeRequests = findAll(
                    getRequestedFor(
                        urlMatching(
                            """/v2/subscribe/${pubnub.configuration.subscribeKey}/ch2,ch1/.*"""
                        )
                    )
                )
                for (request in subscribeRequests) {
                    val stateString = PubNubUtil.urlDecode(request.queryParameter("state").firstValue())
                    var actualMap: HashMap<String, Any>? = null
                    try {
                        actualMap = pubnub.mapper.fromJson(
                            stateString,
                            object : TypeToken<HashMap<String, Any>?>() {}.type
                        )
                    } catch (e: PubNubException) {
                        e.printStackTrace()
                    }
                    if (actualMap != null && actualMap == expectedMap) {
                        subscribeHits.getAndAdd(1)
                    }
                }
                for (request in heartbeatRequests) {
                    if (!request.queryParams.containsKey("state")) {
                        heartbeatHits.getAndAdd(1)
                    }
                }
            }
        })

        pubnub.subscribe(
            channels = listOf("ch1", "ch2"),
            channelGroups = listOf("cg1", "cg2")
        )

        pubnub.setPresenceState(
            channels = listOf("ch1"),
            channelGroups = listOf("cg2"),
            state = listOf("p1", "p2")
        ).async { _, _ -> }

        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .until { subscribeHits.get() > 0 && heartbeatHits.get() > 0 }
    }

    @Test
    fun testSubscribeChannelGroupBuilder() {
        val atomic = AtomicBoolean()
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
        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {}
            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                val requests = findAll(getRequestedFor(urlMatching("/v2/subscribe.*")))
                for (request in requests) {
                    val channelGroupQuery = request.queryParameter("channel-group")
                    if (channelGroupQuery != null && channelGroupQuery.firstValue() == "cg1,cg2") {
                        atomic.set(true)
                    }
                }
            }
        })

        pubnub.subscribe(
            channelGroups = listOf("cg1", "cg2")
        )

        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .untilTrue(atomic)
    }

    @Test
    fun testSubscribeChannelGroupWithPresenceBuilder() {
        val atomic = AtomicInteger(0)
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
        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {}
            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                val requests = findAll(getRequestedFor(urlMatching("/v2/subscribe.*")))
                for (request in requests) {
                    val channelGroups = request.queryParameter("channel-group")
                        .firstValue()
                        .split(",")
                        .toMutableList()
                        .sorted()
                    if ("cg1,cg1-pnpres,cg2,cg2-pnpres" == channelGroups.toCsv()) {
                        atomic.addAndGet(1)
                    }
                }
            }
        })

        pubnub.subscribe(
            channelGroups = listOf("cg1", "cg2"),
            withPresence = true
        )

        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAtomic(atomic, Matchers.greaterThan(0))
    }

    @Test
    fun testSubscribeWithFilterExpressionBuilder() {
        val atomic = AtomicBoolean()

        pubnub.configuration.filterExpression = "much=filtering"

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("pnsdk", matching("PubNub-Kotlin/.*"))
                .withQueryParam("filter-expr", matching("much=filtering"))
                .withQueryParam("tt", matching("0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "t": {
                                "t": "5",
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

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .withQueryParam("tt", matching("5"))
                .willReturn(notFound())
        )

        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {}
            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                val requests =
                    findAll(getRequestedFor(urlMatching("/v2/subscribe.*")))
                assertTrue(requests.size > 0)
                atomic.set(true)
            }
        })

        pubnub.subscribe(
            channels = listOf("ch1", "ch2")
        )

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilTrue(atomic)
    }

    @Test
    fun testSubscribeWithEncryption() {
        val atomic = AtomicInteger(0)
        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "t": {
                                "t": "14718972508742569",
                                "r": 1
                              },
                              "m": [
                                {
                                  "a": "4",
                                  "f": 512,
                                  "i": "ff374d0b-b866-40db-9ced-42d205bb808b",
                                  "p": {
                                    "t": "14718972508739738",
                                    "r": 1
                                  },
                                  "k": "demo-36",
                                  "c": "max_ch1",
                                  "d": "6QoqmS9CnB3W9+I4mhmL7w=="
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )
        pubnub.configuration.cipherKey = "hello"
        pubnub.configuration.useRandomInitializationVector = false
        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {}
            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                val requests = findAll(getRequestedFor(urlMatching("/v2/subscribe.*")))
                assertTrue(requests.size > 0)
                assertEquals("hey", pubnub.mapper.elementToString(pnMessageResult.message, "text"))
                atomic.addAndGet(1)
            }
        })

        pubnub.subscribe(
            channels = listOf("ch1", "ch2")
        )

        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAtomic(atomic, Matchers.greaterThan(0))
    }

    @Test
    fun testSubscribeWithEncryptionPNOther() {
        val atomic = AtomicInteger(0)
        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "t": {
                                "t": "14718972508742569",
                                "r": 1
                              },
                              "m": [
                                {
                                  "a": "4",
                                  "f": 512,
                                  "i": "ff374d0b-b866-40db-9ced-42d205bb808b",
                                  "p": {
                                    "t": "14718972508739738",
                                    "r": 1
                                  },
                                  "k": "demo-36",
                                  "c": "max_ch1",
                                  "d": {
                                    "pn_other": "6QoqmS9CnB3W9+I4mhmL7w=="
                                  }
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )

        pubnub.configuration.useRandomInitializationVector = false
        pubnub.configuration.cipherKey = "hello"

        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {}
            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                val requests = findAll(getRequestedFor(urlMatching("/v2/subscribe.*")))
                assertTrue(requests.size > 0)
                assertEquals(
                    "hey",
                    pnMessageResult.message.asJsonObject["pn_other"].asJsonObject["text"].asString
                )
                atomic.addAndGet(1)
            }
        })

        pubnub.subscribe(
            channels = listOf("ch1", "ch2")
        )

        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAtomic(atomic, Matchers.greaterThan(0))
    }

    @Test
    fun testSubscribePresenceBuilder() {
        val atomic = AtomicInteger(0)
        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1,ch2-pnpres,ch1-pnpres/0"))
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
        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {}
            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                val requests =
                    findAll(getRequestedFor(urlMatching("/v2/subscribe.*")))
                assertTrue(requests.size >= 1)
                assertEquals("""{"text":"Enter Message Here"}""", pnMessageResult.message.toString())
                atomic.addAndGet(1)
            }
        })

        pubnub.subscribe(
            channels = listOf("ch1", "ch2"),
            withPresence = true
        )

        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAtomic(atomic, Matchers.greaterThan(0))
    }

    @Test
    fun testSubscribePresencePayloadHereNowRefreshDeltaBuilder() {
        val atomic = AtomicInteger(0)
        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1,ch2-pnpres,ch1-pnpres/0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "t": {
                                "t": "14901247588021627",
                                "r": 2
                              },
                              "m": [
                                {
                                  "a": "4",
                                  "f": 0,
                                  "p": {
                                    "t": "14901247587675704",
                                    "r": 1
                                  },
                                  "k": "demo-36",
                                  "c": "moon-interval-deltas-pnpres",
                                  "d": {
                                    "action": "interval",
                                    "timestamp": 1490124758,
                                    "occupancy": 2,
                                    "here_now_refresh": true,
                                    "join": [
                                      "2220E216-5A30-49AD-A89C-1E0B5AE26AD7",
                                      "4262AE3F-3202-4487-BEE0-1A0D91307DEB"
                                    ]
                                  },
                                  "b": "moon-interval-deltas-pnpres"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )
        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {}
            override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {
                if (atomic.get() == 0) {
                    assertEquals(true, pnPresenceEventResult.hereNowRefresh)
                    assertTrue(pnPresenceEventResult.occupancy!! == 2)
                    atomic.incrementAndGet()
                }
            }
        })

        pubnub.subscribe(
            channels = listOf("ch1", "ch2"),
            withPresence = true
        )

        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAtomic(atomic, IsEqual.equalTo(1))
    }

    @Test
    fun testSubscribePresencePayloadJoinDeltaBuilder() {
        val atomic = AtomicInteger(0)
        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1,ch2-pnpres,ch1-pnpres/0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "t": {
                                "t": "14901247588021627",
                                "r": 2
                              },
                              "m": [
                                {
                                  "a": "4",
                                  "f": 0,
                                  "p": {
                                    "t": "14901247587675704",
                                    "r": 1
                                  },
                                  "k": "demo-36",
                                  "c": "moon-interval-deltas-pnpres",
                                  "d": {
                                    "action": "interval",
                                    "timestamp": 1490124758,
                                    "occupancy": 2,
                                    "join": [
                                      "2220E216-5A30-49AD-A89C-1E0B5AE26AD7",
                                      "4262AE3F-3202-4487-BEE0-1A0D91307DEB"
                                    ]
                                  },
                                  "b": "moon-interval-deltas-pnpres"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )
        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {}
            override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {
                if (atomic.get() == 0) {
                    val joinList: MutableList<String> = ArrayList()
                    joinList.add("2220E216-5A30-49AD-A89C-1E0B5AE26AD7")
                    joinList.add("4262AE3F-3202-4487-BEE0-1A0D91307DEB")
                    assertEquals("interval", pnPresenceEventResult.event)
                    assertEquals(joinList, pnPresenceEventResult.join)
                    assertTrue(pnPresenceEventResult.occupancy!! == 2)
                    atomic.incrementAndGet()
                }
            }
        })

        pubnub.subscribe(
            channels = listOf("ch1", "ch2"),
            withPresence = true
        )

        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAtomic(atomic, IsEqual.equalTo(1))
    }

    @Test
    fun testSubscribePresencePayloadLeaveDeltaBuilder() {
        val atomic = AtomicInteger(0)
        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1,ch2-pnpres,ch1-pnpres/0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "t": {
                                "t": "14901247588021627",
                                "r": 2
                              },
                              "m": [
                                {
                                  "a": "4",
                                  "f": 0,
                                  "p": {
                                    "t": "14901247587675704",
                                    "r": 1
                                  },
                                  "k": "demo-36",
                                  "c": "moon-interval-deltas-pnpres",
                                  "d": {
                                    "action": "interval",
                                    "timestamp": 1490124758,
                                    "occupancy": 2,
                                    "leave": [
                                      "2220E216-5A30-49AD-A89C-1E0B5AE26AD7",
                                      "4262AE3F-3202-4487-BEE0-1A0D91307DEB"
                                    ]
                                  },
                                  "b": "moon-interval-deltas-pnpres"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )
        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {}
            override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {
                if (atomic.get() == 0) {
                    val leaveList: MutableList<String> = ArrayList()
                    leaveList.add("2220E216-5A30-49AD-A89C-1E0B5AE26AD7")
                    leaveList.add("4262AE3F-3202-4487-BEE0-1A0D91307DEB")
                    assertEquals("interval", pnPresenceEventResult.event)
                    assertEquals(leaveList, pnPresenceEventResult.leave)
                    assertTrue(pnPresenceEventResult.occupancy!! == 2)
                    atomic.incrementAndGet()
                }
            }
        })

        pubnub.subscribe(
            channels = listOf("ch1", "ch2"),
            withPresence = true
        )

        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAtomic(atomic, IsEqual.equalTo(1))
    }

    @Test
    fun testSubscribePresencePayloadTimeoutDeltaBuilder() {
        val atomic = AtomicInteger(0)
        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1,ch2-pnpres,ch1-pnpres/0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "t": {
                                "t": "14901247588021627",
                                "r": 2
                              },
                              "m": [
                                {
                                  "a": "4",
                                  "f": 0,
                                  "p": {
                                    "t": "14901247587675704",
                                    "r": 1
                                  },
                                  "k": "demo-36",
                                  "c": "moon-interval-deltas-pnpres",
                                  "d": {
                                    "action": "interval",
                                    "timestamp": 1490124758,
                                    "occupancy": 2,
                                    "timeout": [
                                      "2220E216-5A30-49AD-A89C-1E0B5AE26AD7",
                                      "4262AE3F-3202-4487-BEE0-1A0D91307DEB"
                                    ]
                                  },
                                  "b": "moon-interval-deltas-pnpres"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )
        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {}
            override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {
                if (atomic.get() == 0) {
                    val timeoutList = listOf(
                        "2220E216-5A30-49AD-A89C-1E0B5AE26AD7",
                        "4262AE3F-3202-4487-BEE0-1A0D91307DEB"
                    )
                    assertEquals("interval", pnPresenceEventResult.event)
                    assertEquals(timeoutList, pnPresenceEventResult.timeout)
                    assertTrue(pnPresenceEventResult.occupancy!! == 2)
                    atomic.incrementAndGet()
                }
            }
        })

        pubnub.subscribe(
            channels = listOf("ch1", "ch2"),
            withPresence = true
        )

        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAtomic(atomic, IsEqual.equalTo(1))
    }

    @Test
    fun testSubscribePresencePayloadBuilder() {
        val atomic = AtomicInteger(0)
        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1,ch2-pnpres,ch1-pnpres/0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "t": {
                                "t": "14614512228786519",
                                "r": 1
                              },
                              "m": [
                                {
                                  "a": "4",
                                  "f": 0,
                                  "p": {
                                    "t": "14614512228418349",
                                    "r": 2
                                  },
                                  "k": "sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f",
                                  "c": "coolChannel-pnpres",
                                  "d": {
                                    "action": "join",
                                    "timestamp": 1461451222,
                                    "uuid": "4a6d5df7-e301-4e73-a7b7-6af9ab484eb0",
                                    "occupancy": 1
                                  },
                                  "b": "coolChannel-pnpres"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )

        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {}
            override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {
                if (atomic.get() == 0) {
                    assertEquals("join", pnPresenceEventResult.event)
                    assertEquals("4a6d5df7-e301-4e73-a7b7-6af9ab484eb0", pnPresenceEventResult.uuid)
                    assertTrue(pnPresenceEventResult.occupancy!! == 1)
                    assertTrue(pnPresenceEventResult.timestamp!! == 1461451222L)
                    atomic.incrementAndGet()
                }
            }
        })

        pubnub.subscribe(
            channels = listOf("ch1", "ch2"),
            withPresence = true
        )

        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAtomic(atomic, IsEqual.equalTo(1))
    }

    @Test
    fun testSubscribePresenceStateCallback() {
        val atomic = AtomicBoolean()
        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch10,ch10-pnpres/0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "t": {
                                "t": "14637536741734954",
                                "r": 1
                              },
                              "m": [
                                {
                                  "a": "4",
                                  "f": 512,
                                  "p": {
                                    "t": "14637536740940378",
                                    "r": 1
                                  },
                                  "k": "demo-36",
                                  "c": "ch10-pnpres",
                                  "d": {
                                    "action": "join",
                                    "timestamp": 1463753674,
                                    "uuid": "24c9bb19-1fcd-4c40-a6f1-522a8a1329ef",
                                    "occupancy": 3
                                  },
                                  "b": "ch10-pnpres"
                                },
                                {
                                  "a": "4",
                                  "f": 512,
                                  "p": {
                                    "t": "14637536741726901",
                                    "r": 1
                                  },
                                  "k": "demo-36",
                                  "c": "ch10-pnpres",
                                  "d": {
                                    "action": "state-change",
                                    "timestamp": 1463753674,
                                    "data": {
                                      "state": "cool"
                                    },
                                    "uuid": "24c9bb19-1fcd-4c40-a6f1-522a8a1329ef",
                                    "occupancy": 3
                                  },
                                  "b": "ch10-pnpres"
                                }
                              ]
                            }
                        """.trimIndent()
                    )
                )
        )
        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {}
            override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {
                if (pnPresenceEventResult.event == "state-change") {
                    if (pnPresenceEventResult.state!!.asJsonObject.has("state") &&
                        pnPresenceEventResult.state!!.asJsonObject.get("state").asString == "cool"
                    ) {
                        atomic.set(true)
                    }
                }
            }
        })

        pubnub.subscribe(
            channels = listOf("ch10"),
            withPresence = true
        )

        Awaitility.await()
            .atMost(2, TimeUnit.SECONDS)
            .untilAtomic(
                atomic, IsEqual.equalTo(true)
            )
    }

    @Test
    fun testSubscribeRegionBuilder() {
        val atomic = AtomicBoolean()
        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1,ch2-pnpres,ch1-pnpres/0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "t": {
                                "t": "14607577960932487",
                                "r": 8
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
        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {}
            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                val requests = findAll(getRequestedFor(urlMatching("/v2/subscribe.*")))
                if (requests.size > 1) {
                    assertEquals("8", requests[1].queryParameter("tr").firstValue())
                    atomic.set(true)
                }
            }
        })

        pubnub.subscribe(
            channels = listOf("ch1", "ch2"),
            withPresence = true
        )

        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAtomic(atomic, IsEqual.equalTo(true))
    }

    @Test
    fun testRemoveListener() {
        stubFor(
            get(urlPathMatching("/v2/subscribe/mySubscribeKey/.*"))
                .willReturn(emptyJson())
        )

        val atomic = AtomicInteger(0)
        val sub1: SubscribeCallback = object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                atomic.addAndGet(1)
            }

            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                atomic.addAndGet(1)
            }

            override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {
                atomic.addAndGet(1)
            }
        }

        pubnub.addListener(sub1)
        pubnub.removeListener(sub1)

        pubnub.subscribe(
            channels = listOf("ch1", "ch2"),
            withPresence = true
        )

        Awaitility.await()
            .atMost(2, TimeUnit.SECONDS)
            .untilAtomic(atomic, IsEqual.equalTo(0))
    }

    @Test
    fun testUnsubscribe() {
        val statusReceived = AtomicBoolean()
        val messageReceived = AtomicBoolean()
        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1,ch2-pnpres,ch1-pnpres/0"))
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
        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch2-pnpres/0"))
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
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1/leave"))
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
        val sub1: SubscribeCallback = object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                if (pnStatus.category == PNStatusCategory.PNConnectedCategory) {
                    pubnub.unsubscribe(
                        channels = listOf("ch1")
                    )
                }
                val affectedChannels = pnStatus.affectedChannels
                if (affectedChannels.size == 1 && pnStatus.operation == PNOperationType.PNUnsubscribeOperation) {
                    if (affectedChannels[0] == "ch1") {
                        statusReceived.set(true)
                    }
                }
            }

            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                val requests = findAll(
                    getRequestedFor(
                        urlMatching("/v2/subscribe/mySubscribeKey/ch2,ch2-pnpres/0.*")
                    )
                )
                if (requests.isNotEmpty()) {
                    messageReceived.set(true)
                }
            }
        }
        pubnub.addListener(sub1)

        pubnub.subscribe(
            channels = listOf("ch1", "ch2"),
            withPresence = true
        )

        Awaitility.await()
            .atMost(2, TimeUnit.SECONDS)
            .untilAtomic(
                messageReceived, IsEqual.equalTo(true)
            )

        Awaitility.await()
            .atMost(2, TimeUnit.SECONDS)
            .untilAtomic(
                statusReceived, IsEqual.equalTo(true)
            )
    }

    @Test
    fun testAllHeartbeats() {
        val statusRecieved = AtomicBoolean()
        pubnub.configuration.presenceTimeout = 20
        pubnub.configuration.heartbeatNotificationOptions = PNHeartbeatNotificationOptions.ALL
        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1,ch2-pnpres,ch1-pnpres/0"))
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
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch2,ch1/heartbeat"))
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
        val sub1: SubscribeCallback = object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                if (pnStatus.operation == PNOperationType.PNHeartbeatOperation && !pnStatus.error) {
                    statusRecieved.set(true)
                }
            }
        }
        pubnub.addListener(sub1)

        pubnub.subscribe(
            channels = listOf("ch1", "ch2"),
            withPresence = true
        )

        Awaitility.await()
            .atMost(2, TimeUnit.SECONDS)
            .untilAtomic(statusRecieved, IsEqual.equalTo(true))
    }

    @Test
    fun testAllHeartbeatsViaPresence() {
        val statusReceived = AtomicBoolean()
        pubnub.configuration.presenceTimeout = 20
        pubnub.configuration.heartbeatNotificationOptions = PNHeartbeatNotificationOptions.ALL

        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch2,ch1/heartbeat"))
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
        val sub1: SubscribeCallback = object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                if (pnStatus.operation == PNOperationType.PNHeartbeatOperation && !pnStatus.error) {
                    statusReceived.set(true)
                }
            }
        }
        assertNotNull(sub1)
        pubnub.addListener(sub1)

        pubnub.presence(
            channels = listOf("ch1", "ch2"),
            connected = true
        )

        Awaitility.await()
            .atMost(2, TimeUnit.SECONDS)
            .untilAtomic(
                statusReceived, IsEqual.equalTo(true)
            )
    }

    @Test
    fun testAllHeartbeatsLeaveViaPresence() {
        val statusReceived = AtomicBoolean()
        pubnub.configuration.heartbeatNotificationOptions = PNHeartbeatNotificationOptions.ALL
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1,ch2/leave"))
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
        val sub1: SubscribeCallback = object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                if (pnStatus.operation == PNOperationType.PNUnsubscribeOperation && !pnStatus.error) {
                    statusReceived.set(true)
                }
            }
        }

        pubnub.addListener(sub1)

        pubnub.presence(
            channels = listOf("ch1", "ch2"),
            connected = false
        )

        Awaitility.await()
            .atMost(2, TimeUnit.SECONDS)
            .untilAtomic(statusReceived, IsEqual.equalTo(true))
    }

    @Test
    fun testSuccessOnFailureVerbosityHeartbeats() {
        val statusReceived = AtomicBoolean()

        pubnub.configuration.presenceTimeout = 20
        pubnub.configuration.heartbeatNotificationOptions = PNHeartbeatNotificationOptions.FAILURES

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1,ch2-pnpres,ch1-pnpres/0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "t": {
                                "t": "5",
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

        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch2,ch1/heartbeat"))
                .willReturn(aResponse().withStatus(404).withBody("{}"))
        )

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1,ch2-pnpres,ch1-pnpres/0"))
                .withQueryParam("tt", matching("5"))
                .willReturn(notFound())
        )

        val sub1: SubscribeCallback = object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                if (pnStatus.operation == PNOperationType.PNHeartbeatOperation) {
                    statusReceived.set(true)
                }
            }
        }

        pubnub.addListener(sub1)

        pubnub.subscribe(
            channels = listOf("ch1", "ch2"),
            withPresence = true
        )

        Awaitility.await()
            .atMost(2, TimeUnit.SECONDS)
            .untilAtomic(statusReceived, IsEqual.equalTo(true))
    }

    @Test
    fun testFailedHeartbeats() {
        val statusReceived = AtomicBoolean()

        pubnub.configuration.presenceTimeout = 20
        pubnub.configuration.heartbeatNotificationOptions = PNHeartbeatNotificationOptions.ALL

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1,ch2-pnpres,ch1-pnpres/0"))
                .withQueryParam("tt", matching("0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "t": {
                                "t": "5",
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

        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch2,ch1/heartbeat"))
                .willReturn(emptyJson().withStatus(403))
        )

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1,ch2-pnpres,ch1-pnpres/0"))
                .withQueryParam("tt", matching("5"))
                .willReturn(notFound())
        )

        val sub1: SubscribeCallback = object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                if (pnStatus.operation == PNOperationType.PNHeartbeatOperation && pnStatus.error) {
                    statusReceived.set(true)
                }
            }
        }
        pubnub.addListener(sub1)

        pubnub.subscribe(
            channels = listOf("ch1", "ch2"),
            withPresence = true
        )

        Awaitility.await()
            .atMost(2, TimeUnit.SECONDS)
            .untilAtomic(statusReceived, IsEqual.equalTo(true))
    }

    @Test
    fun testSilencedHeartbeats() {
        val statusReceived = AtomicBoolean()

        pubnub.configuration.heartbeatNotificationOptions = PNHeartbeatNotificationOptions.NONE

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1,ch2-pnpres,ch1-pnpres/0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                            	"t": {
                            		"t": "14607577960932487",
                            		"r": 1
                            	},
                            	"m": [{
                            		"a": "4",
                            		"f": 0,
                            		"i": "Client-g5d4g",
                            		"p": {
                            			"t": "14607577960925503",
                            			"r": 1
                            		}
                            		"k": "sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f",
                            		"c": "coolChannel",
                            		"d": {
                            			"text": "Enter Message Here"
                            		},
                            		"b": "coolChan-bnel"
                            	}]
                            }
                        """.trimIndent()
                    )
                )
        )

        val sub1: SubscribeCallback = object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                if (pnStatus.operation == PNOperationType.PNHeartbeatOperation) {
                    statusReceived.set(true)
                }
            }
        }

        pubnub.addListener(sub1)

        pubnub.subscribe(
            channels = listOf("ch1", "ch2"),
            withPresence = true
        )

        Awaitility.await()
            .atMost(2, TimeUnit.SECONDS)
            .untilAtomic(statusReceived, IsEqual.equalTo(false))
    }

    @Test
    fun testFailedNoneHeartbeats() {
        val statusReceived = AtomicBoolean()

        pubnub.configuration.heartbeatNotificationOptions = PNHeartbeatNotificationOptions.NONE

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1,ch2-pnpres,ch1-pnpres/0"))
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

        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch2,ch1/heartbeat"))
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

        val sub1: SubscribeCallback = object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                if (pnStatus.operation != PNOperationType.PNHeartbeatOperation) {
                    statusReceived.set(true)
                }
            }
        }
        pubnub.addListener(sub1)

        pubnub.subscribe(
            channels = listOf("ch1", "ch2"),
            withPresence = true
        )

        Awaitility.await()
            .atMost(4, TimeUnit.SECONDS)
            .untilTrue(statusReceived)
    }

    @Test
    fun testHeartbeatsDisabled() {
        val subscribeSuccess = AtomicBoolean()
        val heartbeatFail = AtomicBoolean()

        pubnub.configuration.heartbeatNotificationOptions = PNHeartbeatNotificationOptions.ALL

        assertEquals(PNHeartbeatNotificationOptions.ALL, pubnub.configuration.heartbeatNotificationOptions)
        assertEquals(300, pubnub.configuration.presenceTimeout)
        assertEquals(0, pubnub.configuration.heartbeatInterval)

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch1,ch1-pnpres/0"))
                .willReturn(
                    aResponse()
                        .withBody(
                            """
                            {
                              "t": {
                                "t": null,
                                "r": 12
                              },
                              "m": []
                            }
                            """.trimIndent()
                        )
                        .withStatus(200)
                )
        )
        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1/heartbeat"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withBody(
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
        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                if (!pnStatus.error) {
                    if (pnStatus.operation == PNOperationType.PNSubscribeOperation) {
                        subscribeSuccess.set(true)
                    }
                    if (pnStatus.operation == PNOperationType.PNHeartbeatOperation) {
                        heartbeatFail.set(true)
                    }
                }
            }
        })

        pubnub.subscribe(
            channels = listOf("ch1"),
            withPresence = true
        )

        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .until { subscribeSuccess.get() && !heartbeatFail.get() }
    }

    @Test
    fun testHeartbeatsEnabled() {
        val subscribeSuccess = AtomicBoolean()
        val heartbeatSuccess = AtomicBoolean()

        pubnub.configuration.heartbeatNotificationOptions = PNHeartbeatNotificationOptions.ALL

        assertEquals(PNHeartbeatNotificationOptions.ALL, pubnub.configuration.heartbeatNotificationOptions)
        assertEquals(300, pubnub.configuration.presenceTimeout)
        assertEquals(0, pubnub.configuration.heartbeatInterval)

        pubnub.configuration.presenceTimeout = 20

        assertEquals(20, pubnub.configuration.presenceTimeout)
        assertEquals(9, pubnub.configuration.heartbeatInterval)

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch1,ch1-pnpres/0"))
                .willReturn(
                    aResponse()
                        .withBody(
                            """
                            {
                              "t": {
                                "t": null,
                                "r": 12
                              },
                              "m": []
                            }
                            """.trimIndent()
                        )
                        .withStatus(200)
                )
        )

        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1/heartbeat"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withBody(
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

        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                if (!pnStatus.error) {
                    if (pnStatus.operation == PNOperationType.PNSubscribeOperation) {
                        subscribeSuccess.set(true)
                    }
                    if (pnStatus.operation == PNOperationType.PNHeartbeatOperation) {
                        heartbeatSuccess.set(true)
                    }
                }
            }
        })

        pubnub.subscribe(
            channels = listOf("ch1"),
            withPresence = true
        )

        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .until { subscribeSuccess.get() && heartbeatSuccess.get() }
    }

    @Test
    fun testMinimumPresenceValueNoInterval() {
        pubnub.configuration.presenceTimeout = 10
        assertEquals(20, pubnub.configuration.presenceTimeout)
        assertEquals(9, pubnub.configuration.heartbeatInterval)
    }

    @Test
    fun testMinimumPresenceValueWithInterval() {
        pubnub.configuration.presenceTimeout = 20
        pubnub.configuration.heartbeatInterval = 50
        assertEquals(20, pubnub.configuration.presenceTimeout)
        assertEquals(50, pubnub.configuration.heartbeatInterval)
    }

    @Test
    fun testUnsubscribeAll() {
        val statusReceived = AtomicBoolean()

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1,ch2-pnpres,ch1-pnpres/0"))
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

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch2-pnpres/0"))
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

        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1/leave"))
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

        stubFor(
            get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch2/leave"))
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

        val sub1: SubscribeCallback = object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                if (pnStatus.category == PNStatusCategory.PNConnectedCategory) {
                    pubnub.unsubscribe(
                        channels = listOf("ch1")
                    )
                }

                val affectedChannels = pnStatus.affectedChannels

                if (affectedChannels.size == 1 && pnStatus.operation == PNOperationType.PNUnsubscribeOperation) {
                    if (affectedChannels[0] == "ch1") {
                        pubnub.unsubscribe(
                            channels = listOf("ch2")
                        )
                    }
                }

                if (affectedChannels.size == 1 && pnStatus.operation == PNOperationType.PNUnsubscribeOperation) {
                    if (affectedChannels[0] == "ch2") {
                        statusReceived.set(true)
                    }
                }
            }
        }

        pubnub.addListener(sub1)

        pubnub.subscribe(
            channels = listOf("ch1", "ch2"),
            withPresence = true
        )

        Awaitility.await()
            .atMost(4, TimeUnit.SECONDS)
            .untilTrue(statusReceived)
    }

    @Test
    fun testSubscribeWithCustomTimetoken() {
        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .withQueryParam("tt", equalTo("0"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                              "t": {
                                "t": "999",
                                "r": 1
                              },
                              "m": []
                            }
                        """.trimIndent()
                    )
                )
        )

        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .withQueryParam("tt", equalTo("555"))
                .willReturn(emptyJson())
        )

        pubnub.subscribe(
            channels = listOf("ch1", "ch2"),
            withTimetoken = 555L
        )

        Awaitility.await()
            .atMost(5, TimeUnit.SECONDS)
            .pollDelay(2, TimeUnit.SECONDS)
            .until {
                val requests = findAll(
                    getRequestedFor(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                        .withQueryParam("tt", equalTo("555"))
                )
                assertEquals(1, requests.size)
                true
            }
    }
}
