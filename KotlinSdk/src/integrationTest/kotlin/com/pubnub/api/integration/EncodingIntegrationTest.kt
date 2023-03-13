package com.pubnub.apikt.integration

import com.pubnub.apikt.CommonUtils.getSpecialCharsMap
import com.pubnub.apikt.CommonUtils.randomChannel
import com.pubnub.apikt.SignatureUtils
import com.pubnub.apikt.encodedParam
import com.pubnub.apikt.listen
import org.awaitility.Awaitility
import org.awaitility.Durations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(Parameterized::class)
class EncodingIntegrationTest(
    val name: String,
    val regular: String,
    val encoded: String
) : BaseIntegrationTest() {

    @Test
    fun testVerifySignature() {
        val success = AtomicBoolean()

        server.configuration.includeRequestIdentifier = false
        server.configuration.includeInstanceIdentifier = false

        val expectedChannel = randomChannel()
        val expectedMessage = "msg${regular}msg"
        val expectedMetadata = "meta${regular}meta"

        val propertyName = "target"

        server.publish(
            channel = expectedChannel,
            message = expectedMessage,
            meta = expectedMetadata,
            usePost = false
        ).apply {
            queryParam += mapOf(propertyName to regular)
        }.async { _, status ->
            assertFalse(status.error)
            val encodedParam = status.encodedParam(propertyName)
            assertEquals(encoded, encodedParam)
            SignatureUtils.decomposeAndVerifySignature(server.configuration, status.clientRequest!!)
            success.set(true)
        }

        success.listen()

        val historyTest = {
            Awaitility.await()
                .pollInterval(Durations.FIVE_HUNDRED_MILLISECONDS)
                .atMost(Durations.FIVE_SECONDS)
                .until {
                    val latch = CountDownLatch(1)
                    server.history(
                        channel = expectedChannel,
                        includeMeta = true
                    ).async { result, status ->
                        assertEquals(1, result!!.messages.size)
                        SignatureUtils.decomposeAndVerifySignature(
                            server.configuration,
                            status.clientRequest!!
                        )
                        assertFalse(status.error)
                        assertEquals(
                            expectedMessage,
                            result.messages[0].entry.asString
                        )
                        assertEquals(
                            expectedMetadata,
                            result.messages[0].meta!!.asString
                        )
                        assertEquals(
                            regular,
                            result.messages[0].entry.asString.replace("msg", "")
                        )
                        assertEquals(
                            regular,
                            result.messages[0].meta!!.asString.replace("meta", "")
                        )
                        latch.countDown()
                    }
                    latch.await(1, TimeUnit.SECONDS)
                }
        }

        historyTest.invoke()
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters(name = "testWith \"{0}\" {1}")
        fun data(): List<Array<String>> {
            return getSpecialCharsMap()
                .map { arrayOf(it.name, it.regular, it.encoded) }
                .toList()
        }
    }

    override fun provideAuthKey() = ""
}
