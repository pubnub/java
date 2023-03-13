package com.pubnub.apikt.integration

import com.pubnub.apikt.*
import com.pubnub.apikt.callbacks.SubscribeCallback
import com.pubnub.apikt.enums.PNLogVerbosity
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.enums.PNStatusCategory
import com.pubnub.apikt.models.consumer.PNStatus
import com.pubnub.apikt.models.consumer.pubsub.PNMessageResult
import com.pubnub.apikt.models.consumer.pubsub.PNPresenceEventResult
import com.pubnub.apikt.models.consumer.pubsub.PNSignalResult
import com.pubnub.apikt.models.consumer.pubsub.message_actions.PNMessageActionResult
import org.awaitility.Awaitility
import org.awaitility.Durations
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class AppTest {

    lateinit var pubnub: PubNub

    @Before
    fun initPubnub() {
        pubnub = PubNub(
            PNConfiguration(userId = UserId(PubNub.generateUUID())).apply {
                subscribeKey = Keys.subKey
                publishKey = Keys.pubKey
                logVerbosity = PNLogVerbosity.BODY
            }
        )
    }

    @After
    fun cleanUp() {
        pubnub.forceDestroy()
    }

    @Test
    fun testPublishSync() {
        pubnub.publish(
            channel = UUID.randomUUID().toString(),
            message = UUID.randomUUID().toString()
        ).sync().let {
            assertNotNull(it)
        }
    }

    @Test
    fun testPublishAsync() {
        val success = AtomicBoolean()

        pubnub.publish(
            channel = UUID.randomUUID().toString(),
            message = UUID.randomUUID().toString()
        ).async { result, status ->
            assertFalse(status.error)
            result!!.timetoken
            success.set(true)
        }

        success.listen()

        success.set(false)

        Thread.sleep(2000)

        pubnub.publish(
            channel = UUID.randomUUID().toString(),
            message = UUID.randomUUID().toString()
        ).async { result, status ->
            assertFalse(status.error)
            result!!.timetoken
            success.set(true)
        }

        success.listen()
    }

    @Test
    fun testSubscribe() {
        val success = AtomicBoolean()
        val expectedChannel = UUID.randomUUID().toString()

        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                assertTrue(pnStatus.operation == PNOperationType.PNSubscribeOperation)
                assertTrue(pnStatus.category == PNStatusCategory.PNConnectedCategory)
                assertTrue(pnStatus.affectedChannels.contains(expectedChannel))
                success.set(true)
            }

            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {}
            override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {}
            override fun signal(pubnub: PubNub, pnSignalResult: PNSignalResult) {}
            override fun messageAction(pubnub: PubNub, pnMessageActionResult: PNMessageActionResult) {}
        })

        pubnub.subscribe(
            channels = listOf(expectedChannel),
            withPresence = true
        )

        success.listen()
    }

    @Test
    fun testHereNow() {
        val expectedChannels = listOf(UUID.randomUUID().toString())

        pubnub.subscribe(
            channels = expectedChannels,
            withPresence = true
        )

        Awaitility.await()
            .atMost(Durations.FIVE_SECONDS)
            .pollDelay(Durations.ONE_SECOND)
            .pollInterval(Durations.ONE_SECOND)
            .with()
            .until {
                pubnub.whereNow(
                    uuid = pubnub.configuration.userId.value
                ).sync()!!
                    .channels
                    .containsAll(expectedChannels)
            }

        pubnub.hereNow(
            channels = expectedChannels,
            includeUUIDs = false,
            includeState = false
        ).sync()
    }
}
