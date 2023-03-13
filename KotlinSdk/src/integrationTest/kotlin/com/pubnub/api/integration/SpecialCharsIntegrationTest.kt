package com.pubnub.apikt.integration

import com.pubnub.apikt.CommonUtils
import com.pubnub.apikt.SignatureUtils
import com.pubnub.apikt.listen
import org.awaitility.Awaitility
import org.awaitility.Durations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean

class SpecialCharsIntegrationTest : BaseIntegrationTest() {

    @Test
    fun testSpecialCharsPathAndUrl() {
        val expectedChannel = CommonUtils.randomChannel()
        val keyName = "special_char"
        val value = CommonUtils.getSpecialCharsMap().map { it.regular }.shuffled().joinToString("")

        val success = AtomicBoolean()

        server.publish(
            channel = expectedChannel,
            message = value,
            meta = mapOf(
                keyName to value
            )
        ).apply {
            queryParam += mapOf(
                "za" to value,
                "aa" to value,
                "s" to value,
                "Zz" to value,
                "ZZZ" to value,
                "123" to value
            )
        }.async { _, status ->
            assertFalse(status.error)
            SignatureUtils.decomposeAndVerifySignature(server.configuration, status.clientRequest!!)
            success.set(true)
        }

        success.listen()

        Awaitility.await()
            .pollInterval(Durations.FIVE_HUNDRED_MILLISECONDS)
            .atMost(Durations.FIVE_SECONDS)
            .until {
                val messages = server.history(
                    channel = expectedChannel,
                    includeMeta = true
                ).sync()!!.messages

                assertEquals(1, messages.size)
                assertEquals(value, messages[0].meta!!.asJsonObject[keyName].asString)
                assertEquals(value, messages[0].entry.asString)
                true
            }
    }

    override fun provideAuthKey() = ""
}
