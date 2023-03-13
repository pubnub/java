package com.pubnub.apikt.legacy.managers

import com.pubnub.apikt.legacy.BaseTest
import com.pubnub.apikt.managers.BasePathManager
import org.junit.Assert.assertEquals
import org.junit.Test

class BasePathManagerTest : BaseTest() {

    override fun onBefore() {
        clearConfiguration()
    }

    @Test
    fun stdOriginNotSecure() {
        config.secure = false
        initPubNub()
        val basePathManager = BasePathManager(config)
        assertEquals("http://ps.pndsn.com", basePathManager.basePath())
    }

    @Test
    fun stdOriginSecure() {
        config.secure = true
        initPubNub()
        val basePathManager = BasePathManager(config)
        assertEquals("https://ps.pndsn.com", basePathManager.basePath())
    }

    @Test
    fun customOriginNotSecure() {
        config.origin = "custom.origin.com"
        config.secure = false
        initPubNub()
        val basePathManager = BasePathManager(config)
        assertEquals("http://custom.origin.com", basePathManager.basePath())
    }

    @Test
    fun customOriginSecure() {
        config.origin = "custom.origin.com"
        config.secure = true
        initPubNub()
        val basePathManager = BasePathManager(config)
        assertEquals("https://custom.origin.com", basePathManager.basePath())
    }

    @Test
    fun customOriginNotSecureWithCacheBusting() {
        config.origin = "custom.origin.com"
        config.cacheBusting = true
        config.secure = false
        initPubNub()
        val basePathManager = BasePathManager(config)
        assertEquals("http://custom.origin.com", basePathManager.basePath())
    }

    @Test
    fun customOriginSecureWithCacheBusting() {
        config.origin = "custom.origin.com"
        config.secure = true
        config.cacheBusting = true
        initPubNub()
        val basePathManager = BasePathManager(config)
        assertEquals("https://custom.origin.com", basePathManager.basePath())
    }

    @Test
    fun cacheBustingNotSecure() {
        config.cacheBusting = true
        config.secure = false
        initPubNub()
        val basePathManager = BasePathManager(config)
        assertEquals("http://ps1.pndsn.com", basePathManager.basePath())
        assertEquals("http://ps2.pndsn.com", basePathManager.basePath())
        assertEquals("http://ps3.pndsn.com", basePathManager.basePath())
        assertEquals("http://ps4.pndsn.com", basePathManager.basePath())
        assertEquals("http://ps5.pndsn.com", basePathManager.basePath())
        assertEquals("http://ps6.pndsn.com", basePathManager.basePath())
        assertEquals("http://ps7.pndsn.com", basePathManager.basePath())
        assertEquals("http://ps8.pndsn.com", basePathManager.basePath())
        assertEquals("http://ps9.pndsn.com", basePathManager.basePath())
        assertEquals("http://ps10.pndsn.com", basePathManager.basePath())
        assertEquals("http://ps11.pndsn.com", basePathManager.basePath())
        assertEquals("http://ps12.pndsn.com", basePathManager.basePath())
        assertEquals("http://ps13.pndsn.com", basePathManager.basePath())
        assertEquals("http://ps14.pndsn.com", basePathManager.basePath())
        assertEquals("http://ps15.pndsn.com", basePathManager.basePath())
        assertEquals("http://ps16.pndsn.com", basePathManager.basePath())
        assertEquals("http://ps17.pndsn.com", basePathManager.basePath())
        assertEquals("http://ps18.pndsn.com", basePathManager.basePath())
        assertEquals("http://ps19.pndsn.com", basePathManager.basePath())
        assertEquals("http://ps20.pndsn.com", basePathManager.basePath())
        assertEquals("http://ps1.pndsn.com", basePathManager.basePath())
    }

    @Test
    fun cacheBustingSecure() {
        config.cacheBusting = true
        initPubNub()
        val basePathManager = BasePathManager(config)
        assertEquals("https://ps1.pndsn.com", basePathManager.basePath())
        assertEquals("https://ps2.pndsn.com", basePathManager.basePath())
        assertEquals("https://ps3.pndsn.com", basePathManager.basePath())
        assertEquals("https://ps4.pndsn.com", basePathManager.basePath())
        assertEquals("https://ps5.pndsn.com", basePathManager.basePath())
        assertEquals("https://ps6.pndsn.com", basePathManager.basePath())
        assertEquals("https://ps7.pndsn.com", basePathManager.basePath())
        assertEquals("https://ps8.pndsn.com", basePathManager.basePath())
        assertEquals("https://ps9.pndsn.com", basePathManager.basePath())
        assertEquals("https://ps10.pndsn.com", basePathManager.basePath())
        assertEquals("https://ps11.pndsn.com", basePathManager.basePath())
        assertEquals("https://ps12.pndsn.com", basePathManager.basePath())
        assertEquals("https://ps13.pndsn.com", basePathManager.basePath())
        assertEquals("https://ps14.pndsn.com", basePathManager.basePath())
        assertEquals("https://ps15.pndsn.com", basePathManager.basePath())
        assertEquals("https://ps16.pndsn.com", basePathManager.basePath())
        assertEquals("https://ps17.pndsn.com", basePathManager.basePath())
        assertEquals("https://ps18.pndsn.com", basePathManager.basePath())
        assertEquals("https://ps19.pndsn.com", basePathManager.basePath())
        assertEquals("https://ps20.pndsn.com", basePathManager.basePath())
        assertEquals("https://ps1.pndsn.com", basePathManager.basePath())
    }
}
