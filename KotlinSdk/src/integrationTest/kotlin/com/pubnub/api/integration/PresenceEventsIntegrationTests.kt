package com.pubnub.apikt.integration

import com.pubnub.apikt.*
import com.pubnub.apikt.CommonUtils.generatePayload
import com.pubnub.apikt.CommonUtils.randomChannel
import com.pubnub.apikt.callbacks.SubscribeCallback
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.enums.PNStatusCategory
import com.pubnub.apikt.models.consumer.PNStatus
import com.pubnub.apikt.models.consumer.pubsub.PNPresenceEventResult
import org.awaitility.Awaitility
import org.awaitility.Durations
import org.hamcrest.core.IsEqual
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class PresenceEventsIntegrationTests : BaseIntegrationTest() {

    lateinit var guest: PubNub

    override fun onBefore() {
        guest = createPubNub()
    }

    @Test
    fun testJoinChannel() {
        val success = AtomicBoolean()
        val expectedChannel = randomChannel()

        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {}

            override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {
                assertEquals("join", pnPresenceEventResult.event)
                assertEquals(expectedChannel, pnPresenceEventResult.channel)
                success.set(true)
            }
        })

        pubnub.subscribeToBlocking(expectedChannel)

        success.listen()
    }

    @Test
    fun testLeaveChannel() {
        val success = AtomicBoolean()
        val expectedChannel = randomChannel()

        pubnub.subscribeToBlocking(expectedChannel)
        guest.subscribeToBlocking(expectedChannel)

        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {}

            override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {
                if (pnPresenceEventResult.event == "leave") {
                    assertEquals(expectedChannel, pnPresenceEventResult.channel)
                    success.set(true)
                }
            }
        })

        // todo: check it
        // guest.unsubscribeFromBlocking(expectedChannel)
        // success.listen()

        Awaitility.await()
            .atMost(CommonUtils.DEFAULT_LISTEN_DURATION.toLong(), TimeUnit.SECONDS)
            .with()
            .pollInterval(Durations.TWO_SECONDS)
            .until {
                guest.unsubscribeFromBlocking(expectedChannel)
                success.get()
            }
    }

    @Test
    fun testTimeoutFromChannel() {
        val expectedChannel = randomChannel()
        val expectedTimeout = 20

        //
        val subscribed = AtomicBoolean()
        val timeoutReceived = AtomicBoolean()
        //

        pubnub.configuration.presenceTimeout = expectedTimeout
        pubnub.configuration.heartbeatInterval = 0

        pubnub.addListener(object : SubscribeCallback() {

            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                //
                if (pnStatus.category == PNStatusCategory.PNConnectedCategory &&
                    pnStatus.operation == PNOperationType.PNSubscribeOperation
                ) {
                    subscribed.set(true)
                }
                //
            }

            override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {
                if (pnPresenceEventResult.event == "timeout") {
                    assertEquals(expectedChannel, pnPresenceEventResult.channel)
                    timeoutReceived.set(true)
                }
            }
        })

        pubnub.subscribeToBlocking(expectedChannel)

        Awaitility.await()
            .atMost(10, TimeUnit.SECONDS)
            .pollInterval(Durations.ONE_SECOND)
            .untilTrue(subscribed)

        Awaitility.await()
            .atMost(expectedTimeout + 3L, TimeUnit.SECONDS)
            .pollInterval(Durations.ONE_SECOND)
            .untilAtomic(timeoutReceived, IsEqual.equalTo(true))
    }

    @Test
    fun testStateChangeEvent() {
        val success = AtomicBoolean()
        val expectedChannel = randomChannel()

        pubnub.subscribeToBlocking(expectedChannel)

        pubnub.addListener(object : SubscribeCallback() {

            override fun status(pubnub: PubNub, pnStatus: PNStatus) {}

            override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {
                if (pnPresenceEventResult.event == "state-change") {
                    assertEquals(pubnub.configuration.userId.value, pnPresenceEventResult.uuid)
                    success.set(true)
                }
            }
        })

        pubnub.setPresenceState(
            channels = listOf(expectedChannel),
            state = generatePayload()
        ).sync()!!

        success.listen()
    }
}
