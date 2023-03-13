package com.pubnub.apikt.legacy

import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.enums.PNReconnectionPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PubNubTest : BaseTest() {

    override fun onBefore() {
        clearConfiguration()
        initPubNub()
        config.subscribeKey = "demo"
        config.publishKey = "demo"
    }

    @Test
    fun testCreateSuccess() {
        assertEquals(true, pubnub.configuration.secure)
        assertEquals("https://ps.pndsn.com", pubnub.baseUrl())
    }

    @Test
    @Throws(PubNubException::class)
    fun testEncryptCustomKey() {
        config.useRandomInitializationVector = false
        assertEquals("iALQtn3PfIXe74CT/wrS7g==", pubnub.encrypt("test1", "cipherKey").trim())
    }

    @Test
    @Throws(PubNubException::class)
    fun testEncryptConfigurationKey() {
        config.cipherKey = "cipherKey"
        config.useRandomInitializationVector = false
        initPubNub()
        assertEquals("iALQtn3PfIXe74CT/wrS7g==", pubnub.encrypt("test1").trim())
    }

    @Test
    @Throws(PubNubException::class)
    fun testDecryptCustomKey() {
        config.useRandomInitializationVector = false
        assertEquals("test1", pubnub.decrypt("iALQtn3PfIXe74CT/wrS7g==", "cipherKey").trim())
    }

    @Test
    @Throws(PubNubException::class)
    fun testDecryptConfigurationKey() {
        config.cipherKey = "cipherKey"
        config.useRandomInitializationVector = false
        initPubNub()
        assertEquals("test1", pubnub.decrypt("iALQtn3PfIXe74CT/wrS7g==").trim())
    }

    @Test
    fun testconfig() {
        config.subscribeTimeout = 3000
        config.connectTimeout = 4000
        config.nonSubscribeRequestTimeout = 5000
        config.reconnectionPolicy = PNReconnectionPolicy.NONE
        initPubNub()

        assertEquals("https://ps.pndsn.com", pubnub.baseUrl())
        assertEquals(3000, config.subscribeTimeout)
        assertEquals(4000, config.connectTimeout)
        assertEquals(5000, config.nonSubscribeRequestTimeout)
    }

    @Test
    fun getVersionAndTimeStamp() {
        val version = pubnub.version
        val timeStamp = pubnub.timestamp()
        assertEquals("7.4.2", version)
        assertTrue(timeStamp > 0)
    }
}
