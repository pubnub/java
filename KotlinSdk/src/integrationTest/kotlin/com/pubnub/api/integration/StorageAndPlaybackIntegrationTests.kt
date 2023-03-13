package com.pubnub.apikt.integration

import com.pubnub.apikt.CommonUtils.randomChannel
import com.pubnub.apikt.CommonUtils.randomValue
import com.pubnub.apikt.asyncRetry
import com.pubnub.apikt.await
import org.junit.Assert.*
import org.junit.Test

class StorageAndPlaybackIntegrationTests : BaseIntegrationTest() {

    @Test
    fun testHistoryMessages() {
        val expectedMessage = randomValue()
        val expectedChannel = randomChannel()

        pubnub.publish(
            channel = expectedChannel,
            message = expectedMessage
        ).await { _, _ -> }

        pubnub.history(
            channel = expectedChannel
        ).asyncRetry { result, status ->
            assertFalse(status.error)
            assertEquals(expectedMessage, result!!.messages[0].entry.asString)
        }
    }

    @Test
    fun testHistoryMessagesWithTimeToken() {
        val expectedChannel = randomChannel()

        repeat(3) {
            pubnub.publish(
                channel = expectedChannel,
                message = randomValue()
            ).sync()!!
        }

        pubnub.history(
            channel = expectedChannel,
            includeTimetoken = true
        ).asyncRetry { result, status ->
            assertFalse(status.error)
            result!!.messages.forEach {
                assertNotNull(it.timetoken)
            }
        }
    }

    @Test
    fun testLoadingHistoryMessagesWithLimit() {
        val expectedChannel = randomChannel()

        repeat(20) {
            pubnub.publish(
                channel = expectedChannel,
                message = randomValue()
            ).sync()!!
        }

        pubnub.history(
            channel = expectedChannel,
            count = 10
        ).asyncRetry { result, status ->
            assertFalse(status.error)
            assertEquals(10, result!!.messages.size)
        }
    }

    @Test
    fun testLoadingHistoryWithSpecificTimeInterval() {
        val expectedChannel = randomChannel()

        val before = System.currentTimeMillis() * 10000
        wait(5)
        repeat(3) {
            pubnub.publish(
                channel = expectedChannel,
                message = randomValue()
            ).sync()!!
        }
        wait(5)
        val now = System.currentTimeMillis() * 10000

        pubnub.history(
            channel = expectedChannel,
            includeTimetoken = true,
            start = now,
            end = before,
            count = 10
        ).await { result, status ->
            assertFalse(status.error)
            assertEquals(3, result!!.messages.size)
        }
    }

    @Test
    fun testReverseHistoryPaging() {
        val expectedChannel: String = randomValue()
        val message1: String = randomValue(20)
        val message2: String = randomValue(20)

        pubnub.publish(
            channel = expectedChannel,
            message = message1
        ).sync()!!

        pubnub.publish(
            channel = expectedChannel,
            message = message2
        ).sync()!!

        pubnub.history(
            channel = expectedChannel,
            count = 10,
            reverse = true
        ).asyncRetry { result, status ->
            assertFalse(status.error)
            assertEquals(message1, result!!.messages[0].entry.asString)
            assertEquals(message2, result.messages[1].entry.asString)
        }
    }
}
