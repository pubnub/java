package com.pubnub.apikt.integration

import com.pubnub.apikt.CommonUtils.createInterceptor
import com.pubnub.apikt.Keys
import com.pubnub.apikt.PNConfiguration
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.UserId
import com.pubnub.apikt.enums.PNLogVerbosity
import org.junit.After
import org.junit.Before
import org.slf4j.LoggerFactory
import java.util.*

abstract class BaseIntegrationTest {

    protected val logger = LoggerFactory.getLogger(this.javaClass.simpleName)

    val pubnub: PubNub by lazy { createPubNub() }
    val server: PubNub by lazy { createServer() }

    private var mGuestClients = mutableListOf<PubNub>()

    @Before
    fun before() {
        onPrePubnub()
        onBefore()
    }

    @After
    fun after() {
        onAfter()
        for (guestClient in mGuestClients) {
            destroyClient(guestClient)
        }
        mGuestClients.clear()
    }

    protected fun createPubNub(): PubNub {
        var pnConfiguration = provideStagingConfiguration()
        if (pnConfiguration == null) {
            pnConfiguration = getBasicPnConfiguration()
        }
        val pubNub = PubNub(pnConfiguration)
        registerGuestClient(pubNub)
        return pubNub
    }

    private fun createServer(): PubNub {
        val pubNub = PubNub(getServerPnConfiguration())
        registerGuestClient(pubNub)
        return pubNub
    }

    private fun registerGuestClient(guestClient: PubNub) {
        mGuestClients.add(guestClient)
    }

    protected open fun getBasicPnConfiguration(): PNConfiguration {
        val pnConfiguration = PNConfiguration(userId = UserId(PubNub.generateUUID()))
        if (!needsServer()) {
            pnConfiguration.subscribeKey = Keys.subKey
            pnConfiguration.publishKey = Keys.pubKey
        } else {
            pnConfiguration.subscribeKey = Keys.pamSubKey
            pnConfiguration.publishKey = Keys.pamPubKey
            pnConfiguration.authKey = provideAuthKey()!!
        }
        pnConfiguration.logVerbosity = PNLogVerbosity.NONE
        pnConfiguration.httpLoggingInterceptor = createInterceptor(logger)

        pnConfiguration.userId = UserId("client-${UUID.randomUUID()}")
        return pnConfiguration
    }

    private fun getServerPnConfiguration(): PNConfiguration {
        val pnConfiguration = PNConfiguration(userId = UserId(PubNub.generateUUID()))
        pnConfiguration.subscribeKey = Keys.pamSubKey
        pnConfiguration.publishKey = Keys.pamPubKey
        pnConfiguration.secretKey = Keys.pamSecKey
        pnConfiguration.logVerbosity = PNLogVerbosity.NONE
        pnConfiguration.httpLoggingInterceptor = createInterceptor(logger)

        pnConfiguration.userId = UserId("server-${UUID.randomUUID()}")
        return pnConfiguration
    }

    private fun destroyClient(client: PubNub) {
        client.unsubscribeAll()
        client.forceDestroy()
    }

    private fun needsServer() = provideAuthKey() != null

    protected open fun onBefore() {}
    protected open fun onAfter() {}
    protected open fun onPrePubnub() {}

    protected open fun provideAuthKey(): String? = null

    protected open fun provideStagingConfiguration(): PNConfiguration? = null

    fun wait(seconds: Int = 3) {
        Thread.sleep((seconds * 1_000).toLong())
    }
}
