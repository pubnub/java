package com.pubnub.apikt.endpoints.pubsub

import com.pubnub.apikt.PNConfiguration
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.UserId
import com.pubnub.apikt.managers.RetrofitManager
import com.pubnub.apikt.services.PublishService
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.Request
import okio.Timeout
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.Test
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TEST_MESSAGE = "test message in the bottle"
private const val TEST_CHANNEL = "testChannel"
private const val TEST_SUBKEY = "subKey"
private const val TEST_PUBKEY = "pubKey"
private const val CIPHER_KEY = "enigma"

class PublishTest {
    private val pnConfigurationHardcodedIV = PNConfiguration(userId = UserId(PubNub.generateUUID())).apply {
        subscribeKey = TEST_SUBKEY
        publishKey = TEST_PUBKEY
        cipherKey = CIPHER_KEY
    }
    private val pnConfigurationRandomIV = PNConfiguration(userId = UserId(PubNub.generateUUID())).apply {
        subscribeKey = TEST_SUBKEY
        publishKey = TEST_PUBKEY
        cipherKey = CIPHER_KEY
        useRandomInitializationVector = true
    }

    private val pubNubHardcodedIVMock = mockk<PubNub>(relaxed = true)
    private val pubNubRandomIVMock = mockk<PubNub>(relaxed = true)

    private val retrofitManagerMock = mockk<RetrofitManager>()
    private val publishServiceMock = mockk<PublishService>()

    private val encryptedMessageSlot = slot<String>()

    class FakeCall : Call<List<Any>> {
        override fun clone(): Call<List<Any>> = this

        override fun execute(): Response<List<Any>> = Response.success(listOf("test1", "test2", "10"))

        override fun enqueue(callback: Callback<List<Any>>) {
        }

        override fun isExecuted(): Boolean = false

        override fun cancel() {
        }

        override fun isCanceled(): Boolean = false

        override fun request(): Request = Request.Builder().build()

        override fun timeout(): Timeout = Timeout.NONE
    }

    init {
        every {
            publishServiceMock.publish(
                any(),
                any(),
                any(),
                capture(encryptedMessageSlot),
                any()
            )
        } answers { FakeCall() }
        every { retrofitManagerMock.publishService } returns publishServiceMock

        every { pubNubHardcodedIVMock.configuration } returns pnConfigurationHardcodedIV
        every { pubNubRandomIVMock.configuration } returns pnConfigurationRandomIV

        every { pubNubHardcodedIVMock.retrofitManager } returns retrofitManagerMock
        every { pubNubRandomIVMock.retrofitManager } returns retrofitManagerMock
    }

    @Test
    fun `publish with encryption respects random IV setting`() {
        val publishHardcodedIVUnderTest =
            Publish(pubnub = pubNubHardcodedIVMock, message = TEST_MESSAGE, channel = TEST_CHANNEL)
        publishHardcodedIVUnderTest.sync()

        val encryptedMessageHardcodedIV = encryptedMessageSlot.captured

        val publishRandomIVUnderTest =
            Publish(pubnub = pubNubRandomIVMock, message = TEST_MESSAGE, channel = TEST_CHANNEL)
        publishRandomIVUnderTest.sync()

        val encryptedMessageRandomIV = encryptedMessageSlot.captured

        assertThat(encryptedMessageRandomIV, not(`is`(encryptedMessageHardcodedIV)))
    }
}
