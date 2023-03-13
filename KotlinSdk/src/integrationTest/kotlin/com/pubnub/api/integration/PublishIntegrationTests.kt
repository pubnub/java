package com.pubnub.apikt.integration

import com.google.gson.JsonObject
import com.pubnub.apikt.*
import com.pubnub.apikt.CommonUtils.generateMessage
import com.pubnub.apikt.CommonUtils.generatePayload
import com.pubnub.apikt.CommonUtils.generatePayloadJSON
import com.pubnub.apikt.CommonUtils.randomChannel
import com.pubnub.apikt.CommonUtils.retry
import com.pubnub.apikt.callbacks.SubscribeCallback
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.PNBoundedPage
import com.pubnub.apikt.models.consumer.PNStatus
import com.pubnub.apikt.models.consumer.pubsub.PNMessageResult
import org.awaitility.Awaitility
import org.awaitility.Durations
import org.hamcrest.core.IsEqual
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class PublishIntegrationTests : BaseIntegrationTest() {

    @Test
    fun testPublishMessage() {
        val expectedChannel = randomChannel()

        pubnub.publish(
            channel = expectedChannel,
            message = generatePayload()
        ).await { _, status ->
            assertFalse(status.error)
            assertEquals(status.uuid, pubnub.configuration.userId.value)
        }
    }

    @Test
    fun testPublishMessageHistory() {
        val expectedChannel = randomChannel()

        val expectedPayload = JSONObject().apply {
            put("name", "joe")
            put("age", 48)
        }

        val convertedPayload = pubnub.mapper.convertValue(expectedPayload, JsonObject::class.java)

        pubnub.publish(
            channel = expectedChannel,
            message = expectedPayload
        ).sync()!!

        pubnub.fetchMessages(
            channels = listOf(expectedChannel),
            page = PNBoundedPage(
                limit = 1
            )
        ).asyncRetry { result, status ->
            assertFalse(status.error)
            assertEquals(1, result!!.channels.size)
            assertEquals(1, result.channels[expectedChannel]!!.size)
            assertEquals(convertedPayload, result.channels[expectedChannel]!![0].message)
        }
    }

    @Test
    fun testPublishMessageNoHistory() {
        val expectedChannel = randomChannel()
        val messagePayload = generateMessage(pubnub)

        pubnub.publish(
            message = messagePayload,
            channel = expectedChannel,
            shouldStore = false
        ).await { _, status ->
            assertFalse(status.error)
            assertEquals(status.uuid, pubnub.configuration.userId.value)
        }

        pubnub.history(
            count = 1,
            channel = expectedChannel
        ).asyncRetry { result, status ->
            assertFalse(status.error)
            assertEquals(0, result!!.messages.size)
        }
    }

    @Test
    fun testReceiveMessage() {
        val success = AtomicBoolean()
        val expectedChannel = randomChannel()
        val messagePayload = generateMessage(pubnub)

        val observer = createPubNub()

        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                if (pnStatus.operation == PNOperationType.PNSubscribeOperation &&
                    pnStatus.affectedChannels.contains(expectedChannel)
                ) {
                    observer.publish(
                        message = messagePayload,
                        channel = expectedChannel
                    ).async { _, status ->
                        assertFalse(status.error)
                    }
                }
            }

            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                assertEquals(expectedChannel, pnMessageResult.channel)
                assertEquals(observer.configuration.userId.value, pnMessageResult.publisher)
                assertEquals(messagePayload, pnMessageResult.message)
                success.set(true)
            }
        })

        pubnub.subscribeToBlocking(expectedChannel)

        success.listen()
    }

    @Test
    fun testOrgJsonObject_Get_History() {
        val expectedChannel = randomChannel()

        val expectedPayload = JSONObject().apply {
            put("name", "John Doe")
            put("city", "San Francisco")
        }

        pubnub.publish(
            message = expectedPayload,
            channel = expectedChannel,
            usePost = true
        ).sync()!!

        retry {
            pubnub.history(
                channel = expectedChannel,
                count = 1
            ).sync()!!.run {
                assertEquals(
                    expectedPayload.toString(),
                    JSONObject(messages[0].entry.toString()).toString()
                )
            }
        }
    }

    @Test
    fun testOrgJsonObject_Post_History() {
        val expectedChannel = randomChannel()
        val expectedPayload = generatePayloadJSON()

        pubnub.publish(
            channel = expectedChannel,
            message = expectedPayload,
            usePost = true
        ).sync()!!

        retry {
            pubnub.history(
                channel = expectedChannel,
                count = 1
            ).sync()!!.run {
                assertEquals(
                    expectedPayload.toString(),
                    JSONObject(messages[0].entry.toString()).toString()
                )
            }
        }
    }

    @Test
    fun testOrgJsonObject_Get_Receive() {
        val expectedChannel = randomChannel()
        val expectedPayload = generatePayloadJSON()
        val success = AtomicBoolean()

        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {}
            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                success.set(expectedPayload.toString() == JSONObject(pnMessageResult.message.toString()).toString())
            }
        })

        pubnub.subscribeToBlocking(expectedChannel)

        pubnub.publish(
            message = expectedPayload,
            channel = expectedChannel
        ).sync()!!

        success.listen()
    }

    @Test
    fun testOrgJsonObject_Post_Receive() {
        val expectedChannel = randomChannel()
        val expectedPayload = generatePayloadJSON()
        val success = AtomicBoolean()

        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {}
            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                success.set(expectedPayload.toString() == JSONObject(pnMessageResult.message.toString()).toString())
            }
        })

        pubnub.subscribeToBlocking(expectedChannel)

        pubnub.publish(
            message = expectedPayload,
            channel = expectedChannel,
            usePost = true
        ).sync()!!

        success.listen()
    }

    @Test
    fun testOrgJsonArray_Get_History() {
        val expectedChannel = randomChannel()
        val expectedPayload = JSONArray().apply {
            repeat(2) { put(generatePayloadJSON()) }
        }

        pubnub.publish(
            message = expectedPayload,
            channel = expectedChannel
        ).sync()!!

        retry {
            pubnub.history(
                channel = expectedChannel,
                count = 1
            ).sync()!!.run {
                assertEquals(
                    expectedPayload.toString(),
                    JSONArray(messages[0].entry.toString()).toString()
                )
            }
        }
    }

    @Test
    fun testOrgJsonArray_Post_History() {
        val expectedChannel = randomChannel()
        val expectedPayload = JSONArray().apply {
            repeat(2) { put(generatePayloadJSON()) }
        }

        pubnub.publish(
            message = expectedPayload,
            channel = expectedChannel,
            usePost = true
        ).sync()!!

        retry {
            pubnub.history(
                channel = expectedChannel,
                count = 1
            ).sync()!!.run {
                assertEquals(
                    expectedPayload.toString(),
                    JSONArray(messages[0].entry.toString()).toString()
                )
            }
        }
    }

    @Test
    fun testOrgJsonArray_Get_Receive() {
        val expectedChannel = randomChannel()
        val expectedPayload = JSONArray().apply {
            repeat(2) { put(generatePayloadJSON()) }
        }

        val success = AtomicBoolean()

        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {}
            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                success.set(expectedPayload.toString() == JSONArray(pnMessageResult.message.toString()).toString())
            }
        })

        pubnub.subscribeToBlocking(expectedChannel)

        pubnub.publish(
            message = expectedPayload,
            channel = expectedChannel
        ).sync()

        success.listen()
    }

    @Test
    fun testOrgJsonArray_Post_Receive() {
        val expectedChannel = randomChannel()
        val expectedPayload = JSONArray().apply {
            repeat(2) { put(generatePayloadJSON()) }
        }

        val success = AtomicBoolean()

        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {}
            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                success.set(expectedPayload.toString() == JSONArray(pnMessageResult.message.toString()).toString())
            }
        })

        pubnub.subscribeToBlocking(expectedChannel)

        pubnub.publish(
            message = expectedPayload,
            channel = expectedChannel,
            usePost = true
        ).sync()!!

        success.listen()
    }

    @Test
    fun testOrgJson_Combo() {
        val expectedChannel = randomChannel()

        val expectedPayload = JSONObject().apply {
            put("key_1", generatePayloadJSON())
            put("key_2", generatePayloadJSON())
        }
        expectedPayload.put("z_1", JSONObject(expectedPayload.toString()))
        expectedPayload.put("a_2", JSONObject(expectedPayload.toString()))
        expectedPayload.put("d_3", JSONObject(expectedPayload.toString()))
        expectedPayload.put("z_array", JSONArray().apply { repeat(3) { put(generatePayloadJSON()) } })

        val count = AtomicInteger()

        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {}
            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                assertEquals(
                    expectedPayload.toString(),
                    JSONObject(pnMessageResult.message.toString()).toString()
                )
                count.incrementAndGet()
            }
        })

        pubnub.subscribeToBlocking(expectedChannel)

        pubnub.publish(
            channel = expectedChannel,
            message = expectedPayload,
            usePost = true
        ).sync()

        wait()

        pubnub.fetchMessages(
            channels = listOf(expectedChannel),
            page = PNBoundedPage(
                limit = 1
            )
        ).sync()!!.run {
            assertEquals(
                expectedPayload.toString(),
                JSONObject(channels[expectedChannel]!![0].message.toString()).toString()
            )
            count.incrementAndGet()
        }

        Awaitility.await()
            .atMost(Durations.ONE_MINUTE)
            .untilAtomic(count, IsEqual.equalTo(2))
    }
}
