package com.pubnub.apikt.legacy.endpoints.message_actions

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.pubnub.apikt.CommonUtils.failTest
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.callbacks.SubscribeCallback
import com.pubnub.apikt.legacy.BaseTest
import com.pubnub.apikt.listen
import com.pubnub.apikt.models.consumer.PNStatus
import com.pubnub.apikt.models.consumer.pubsub.PNMessageResult
import com.pubnub.apikt.models.consumer.pubsub.PNPresenceEventResult
import com.pubnub.apikt.models.consumer.pubsub.PNSignalResult
import com.pubnub.apikt.models.consumer.pubsub.message_actions.PNMessageActionResult
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class ReceiveMessageActions : BaseTest() {

    @Test
    fun testReceiveMessageAction() {
        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/coolChannel/0"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                         "t": {
                          "t": "1000",
                          "r": 12
                         },
                         "m": [
                          {
                           "a": "1",
                           "f": 0,
                           "e": 3,
                           "i": "client-1639ed91",
                           "p": {
                            "t": "1000",
                            "r": 12
                           },
                           "k": "mySubscribeKey",
                           "c": "coolChannel",
                           "d": {
                            "source": "actions",
                            "version": "1.0",
                            "data": {
                             "messageTimetoken": "500",
                             "type": "reaction",
                             "value": "smiley",
                             "actionTimetoken": "600"
                            },
                            "event": "added"
                           }
                          }
                         ]
                        }
                        """.trimIndent()
                    )
                )
        )

        val success = AtomicBoolean()

        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
            }

            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                failTest()
            }

            override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {
                failTest()
            }

            override fun signal(pubnub: PubNub, pnSignalResult: PNSignalResult) {
                failTest()
            }

            override fun messageAction(pubnub: PubNub, pnMessageActionResult: PNMessageActionResult) {
                assertEquals(pnMessageActionResult.channel, "coolChannel")
                assertEquals(pnMessageActionResult.messageAction.messageTimetoken, 500L)
                assertEquals(pnMessageActionResult.messageAction.uuid, "client-1639ed91")
                assertEquals(pnMessageActionResult.messageAction.actionTimetoken, 600L)
                assertEquals(pnMessageActionResult.messageAction.type, "reaction")
                assertEquals(pnMessageActionResult.messageAction.value, "smiley")
                success.set(true)
            }
        })

        pubnub.subscribe(
            channels = listOf("coolChannel")
        )

        success.listen()
    }

    @Test
    fun testReceiveMessageActionMulti() {
        stubFor(
            get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/coolChannel/0"))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                         "t": {
                          "t": "1000",
                          "r": 12
                         },
                         "m": [
                          {
                           "a": "1",
                           "f": 0,
                           "e": 3,
                           "i": "client-1639ed91",
                           "p": {
                            "t": "1000",
                            "r": 12
                           },
                           "k": "mySubscribeKey",
                           "c": "coolChannel",
                           "d": {
                            "source": "actions",
                            "version": "1.0",
                            "data": {
                             "messageTimetoken": "500",
                             "type": "reaction",
                             "value": "smiley",
                             "actionTimetoken": "600"
                            },
                            "event": "added"
                           }
                          },
                          {
                           "a": "1",
                           "f": 0,
                           "e": 3,
                           "i": "client-1639ed91",
                           "p": {
                            "t": "1000",
                            "r": 12
                           },
                           "k": "mySubscribeKey",
                           "c": "coolChannel",
                           "d": {
                            "source": "actions",
                            "version": "1.0",
                            "data": {
                             "messageTimetoken": "500",
                             "type": "reaction",
                             "value": "grinning",
                             "actionTimetoken": "699"
                            },
                            "event": "added"
                           }
                          }
                         ]
                        }
                        """.trimIndent()
                    )
                )
        )

        val count = AtomicInteger()
        val success = AtomicBoolean()

        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
            }

            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                failTest()
                pnMessageResult.message
            }

            override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {
                failTest()
            }

            override fun signal(pubnub: PubNub, pnSignalResult: PNSignalResult) {
                failTest()
            }

            override fun messageAction(pubnub: PubNub, pnMessageActionResult: PNMessageActionResult) {
                count.incrementAndGet()
                if (count.get() == 2) {
                    success.set(true)
                }
            }
        })

        pubnub.subscribe(
            channels = listOf("coolChannel")
        )

        success.listen()
    }
}
