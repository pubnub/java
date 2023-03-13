package com.pubnub.apikt.legacy

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.pubnub.apikt.CommonUtils.DEFAULT_LISTEN_DURATION
import com.pubnub.apikt.PNConfiguration
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.UserId
import com.pubnub.apikt.enums.PNLogVerbosity
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before

abstract class BaseTest {

    lateinit var wireMockServer: WireMockServer
    protected lateinit var pubnub: PubNub private set
    protected lateinit var config: PNConfiguration private set

    @Before
    fun beforeEach() {
        wireMockServer = WireMockServer(
            wireMockConfig()
                .bindAddress("localhost")
                .dynamicPort()
        )
        wireMockServer.start()
        WireMock.configureFor("http", "localhost", wireMockServer.port())
        DEFAULT_LISTEN_DURATION = 2

        onBefore()
    }

    @After
    fun afterEach() {
        wireMockServer.stop()
        wireMockServer.findAllUnmatchedRequests().forEach {
            println("Unmatched ${it.url}")
        }
        assertTrue(wireMockServer.findAllUnmatchedRequests().isEmpty())
        onAfter()
    }

    open fun onBefore() {
        initConfiguration()
        initPubNub()
    }

    open fun onAfter() {
        pubnub.forceDestroy()
    }

    fun initConfiguration() {
        config = run {
            PNConfiguration(userId = UserId("myUUID")).apply {
                subscribeKey = "mySubscribeKey"
                publishKey = "myPublishKey"
                origin = wireMockServer.baseUrl().toHttpUrlOrNull()!!.run { "$host:$port" }
                secure = false
                logVerbosity = PNLogVerbosity.BODY
            }
        }
    }

    fun clearConfiguration() {
        config = PNConfiguration(userId = UserId(PubNub.generateUUID()))
    }

    fun initPubNub() {
        if (::pubnub.isInitialized) pubnub.destroy()
        pubnub = PubNub(config)
    }
}
