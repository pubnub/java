package com.pubnub.apikt.integration

import com.google.gson.Gson
import com.pubnub.apikt.CommonUtils.assertPnException
import com.pubnub.apikt.CommonUtils.randomChannel
import com.pubnub.apikt.CommonUtils.randomValue
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.await
import com.pubnub.apikt.callbacks.SubscribeCallback
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.listen
import com.pubnub.apikt.models.consumer.PNStatus
import com.pubnub.apikt.models.consumer.pubsub.PNSignalResult
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean

class SignalIntegrationTests : BaseIntegrationTest() {

    lateinit var expectedChannel: String
    lateinit var expectedPayload: String

    override fun onBefore() {
        expectedChannel = randomChannel()
        expectedPayload = randomValue(5)
    }

    @Test
    fun testPublishSignalMessageAsync() {
        pubnub.signal(
            message = expectedPayload,
            channel = expectedChannel
        ).await { result, status ->
            assertFalse(status.error)
            assertEquals(PNOperationType.PNSignalOperation, status.operation)
            assertEquals(status.uuid, pubnub.configuration.userId.value)
            assertNotNull(result)
        }
    }

    @Test
    fun testPublishSignalMessageSync() {
        pubnub.signal(
            message = expectedPayload,
            channel = expectedChannel
        ).sync()!!
    }

    @Test
    fun testReceiveSignalMessage() {
        val success = AtomicBoolean()

        val observerClient = createPubNub()

        observerClient.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                if (pnStatus.operation == PNOperationType.PNSubscribeOperation &&
                    pnStatus.affectedChannels.contains(expectedChannel)
                ) {
                    pubnub.signal(
                        message = expectedPayload,
                        channel = expectedChannel
                    ).async { result, status ->
                        assertFalse(status.error)
                        assertEquals(PNOperationType.PNSignalOperation, status.operation)
                        assertEquals(status.uuid, pubnub.configuration.userId.value)
                        assertNotNull(result)
                    }
                }
            }

            override fun signal(pubnub: PubNub, pnSignalResult: PNSignalResult) {
                assertEquals(pubnub.configuration.userId.value, pnSignalResult.publisher)
                assertEquals(expectedChannel, pnSignalResult.channel)
                assertEquals(expectedPayload, Gson().fromJson(pnSignalResult.message, String::class.java))
                success.set(true)
            }
        })

        observerClient.subscribe(
            channels = listOf(expectedChannel)
        )

        success.listen()
    }

    @Test
    fun testPublishSignalMessageSyncWithoutSubKey() {
        try {
            pubnub.configuration.subscribeKey = ""
            pubnub.signal(
                channel = randomChannel(),
                message = randomValue()
            ).sync()
        } catch (e: Exception) {
            assertPnException(PubNubError.SUBSCRIBE_KEY_MISSING, e)
        }
    }
}
