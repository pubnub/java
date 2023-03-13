package com.pubnub.apikt.suite

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import com.pubnub.apikt.*
import com.pubnub.apikt.CommonUtils.assertPnException
import com.pubnub.apikt.CommonUtils.emptyJson
import com.pubnub.apikt.CommonUtils.failTest
import com.pubnub.apikt.CommonUtils.getSpecialCharsMap
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.enums.PNStatusCategory
import com.pubnub.apikt.legacy.BaseTest
import com.pubnub.apikt.models.consumer.PNStatus
import org.awaitility.Awaitility
import org.awaitility.Durations
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

typealias AsyncCheck<T> = (status: PNStatus, result: T?) -> Unit

abstract class EndpointTestSuite<T : Endpoint<*, R>, R> : BaseTest() {

    private lateinit var expectedStub: StubMapping

    abstract fun telemetryParamName(): String
    abstract fun pnOperation(): PNOperationType
    abstract fun requiredKeys(): Int
    abstract fun snippet(): T
    abstract fun verifyResultExpectations(result: R)
    abstract fun successfulResponseBody(): String
    abstract fun unsuccessfulResponseBodyList(): List<String>
    abstract fun mappingBuilder(): MappingBuilder
    abstract fun affectedChannelsAndGroups(): Pair<List<String>, List<String>>

    open fun optionalScenarioList(): List<OptionalScenario<R>> = emptyList()
    open fun voidResponse() = false

    override fun onBefore() {
        super.onBefore()
        expectedStub = stubFor(mappingBuilder().willReturn(aResponse().withBody(successfulResponseBody())))
        pubnub.configuration.includeInstanceIdentifier = false
        pubnub.configuration.includeRequestIdentifier = false
    }

    @Test
    fun testTelemetryParameter() {
        if (pnOperation() == PNOperationType.PNSubscribeOperation)
            return

        stubTimeEndpoint()

        wireMockServer.removeStub(expectedStub)

        stubFor(
            mappingBuilder()
                .willReturn(
                    aResponse()
                        .withFixedDelay(50)
                        .withBody(successfulResponseBody())
                )
        )

        lateinit var telemetryParamName: String

        snippet().await { _, status ->
            assertFalse(status.error)
            assertEquals(pnOperation(), status.operation)
            assertEquals(PNStatusCategory.PNAcknowledgmentCategory, status.category)
            telemetryParamName = "l_${status.operation.queryParam!!}"
            assertEquals(telemetryParamName(), telemetryParamName)
        }

        Awaitility.await()
            .pollInterval(Durations.FIVE_HUNDRED_MILLISECONDS)
            .pollDelay(Durations.FIVE_HUNDRED_MILLISECONDS)
            .atMost(Durations.FIVE_SECONDS)
            .until {
                val latch = CountDownLatch(1)

                pubnub.time().async { _, status ->
                    assertFalse(status.error)
                    assertNotNull(status.param(telemetryParamName))
                    latch.countDown()
                }

                latch.await(500, TimeUnit.MILLISECONDS)
            }
    }

    @Test
    fun testSuccessAsync() {
        snippet().await { result, status ->
            // todo
            // status.exception?.printStackTrace()
            assertFalse(status.error)
            assertEquals(PNStatusCategory.PNAcknowledgmentCategory, status.category)
            assertEquals(pnOperation(), status.operation)
            assertEquals(status.affectedChannels, affectedChannelsAndGroups().first)
            assertEquals(status.affectedChannelGroups, affectedChannelsAndGroups().second)
            verifyResultExpectations(result!!)
        }
    }

    @Test
    fun testSuccessSync() {
        runSync()
    }

    @Test
    fun testKeys() {
        testSubscribeKey()
        testPublishKey()
        testAuthKeySync()
        testAuthKeyAsync()
        testSecretKey()
    }

    @Test
    fun testUnsuccessfulResponsesSync() {
        wireMockServer.removeStub(expectedStub)

        unsuccessfulResponseBodyList().forEach {
            val stub = stubFor(mappingBuilder().willReturn(aResponse().withBody(it)))
            try {
                testSuccessSync()
                failTest()
            } catch (e: Exception) {
                e.printStackTrace()
                assertPnException(PubNubError.PARSING_ERROR, e)
            }
            wireMockServer.removeStub(stub)
        }
    }

    @Test
    fun testUnsuccessfulResponsesAsync() {
        wireMockServer.removeStub(expectedStub)

        unsuccessfulResponseBodyList().forEach {
            val stub = stubFor(mappingBuilder().willReturn(aResponse().withBody(it)))
            snippet().await { _, status ->
                assertTrue(status.error)
                assertPnException(PubNubError.PARSING_ERROR, status)
            }
            wireMockServer.removeStub(stub)
        }
    }

    @Test
    fun testUsualWrongResponses() {
        wireMockServer.removeStub(expectedStub)

        val map = hashMapOf(
            "empty_json" to emptyJson(),
            "no_content" to noContent(),
            "malformed" to aResponse().withBody("{")
        )

        if (pnOperation() == PNOperationType.PNSubscribeOperation)
            map.remove("empty_json")

        map.forEach {
            val stub = stubFor(mappingBuilder().willReturn(it.value))
            try {
                val result = snippet().sync()
                if (voidResponse()) {
                    assertNotNull(result)
                } else {
                    failTest(it.key)
                }
            } catch (e: Exception) {
                if (voidResponse()) {
                    failTest(it.key)
                }
                assertPnException(PubNubError.PARSING_ERROR, e)
            }

            snippet().await { result, status ->
                assertEquals(!voidResponse(), status.error)

                if (!voidResponse()) {
                    assertNull(result)
                    assertPnException(PubNubError.PARSING_ERROR, status)
                    assertEquals(PNStatusCategory.PNMalformedResponseCategory, status.category)
                } else {
                    assertNotNull(result)
                    assertEquals(PNStatusCategory.PNAcknowledgmentCategory, status.category)
                }
            }

            wireMockServer.removeStub(stub)
        }
    }

    @Test
    fun testOptionalResponsesSync() {
        wireMockServer.removeStub(expectedStub)

        optionalScenarioList().forEach {
            val stub = stubFor(mappingBuilder().willReturn(it.build()))

            try {
                snippet().sync()!!
                if (it.result == Result.FAIL) {
                    failTest()
                }
            } catch (e: Exception) {
                if (it.result == Result.FAIL) {
                    it.pnError?.let { pubNubError ->
                        assertPnException(pubNubError, e)
                    }
                }
            }

            wireMockServer.removeStub(stub)
        }
    }

    @Test
    fun testOptionalResponsesAsync() {
        wireMockServer.removeStub(expectedStub)

        optionalScenarioList().forEach {
            val stub = stubFor(mappingBuilder().willReturn(it.build()))

            snippet().await { result, status ->
                it.additionalChecks.invoke(status, result)
                if (it.result == Result.SUCCESS) {
                    assertFalse(status.error)
                    result!!
                } else if (it.result == Result.FAIL) {
                    assertTrue(status.error)
                    assertNull(result)
                    it.pnError?.let { pubNubError ->
                        assertPnException(pubNubError, status)
                    }
                }
            }

            wireMockServer.removeStub(stub)
        }
    }

    @Test
    fun testUrlEncoding() {
        snippet().apply {
            queryParam += getSpecialCharsMap().map {
                it.name to it.regular
            }.toMap()
        }.await { result, status ->
            assertFalse(status.error)
            assertNotNull(result)

            getSpecialCharsMap().shuffled().forEach {
                val encodedParam = status.encodedParam(it.name)
                assertEquals(it.encoded, encodedParam)
            }
        }
    }

    private fun testSubscribeKey() {
        pubnub.configuration.subscribeKey = " "
        try {
            testSuccessSync()
            if (requiredKeys().contains(SUB)) {
                failTest()
            }
        } catch (e: Exception) {
            if (requiredKeys().contains(SUB)) {
                assertPnException(PubNubError.SUBSCRIBE_KEY_MISSING, e)
            }
        }
        pubnub.configuration.subscribeKey = "mySubscribeKey"
    }

    private fun testPublishKey() {
        pubnub.configuration.publishKey = " "
        try {
            testSuccessSync()
            if (requiredKeys().contains(PUB)) {
                failTest()
            }
        } catch (e: Exception) {
            if (requiredKeys().contains(PUB)) {
                assertPnException(PubNubError.PUBLISH_KEY_MISSING, e)
            }
        }
        pubnub.configuration.publishKey = "myPublishKey"
    }

    private fun testAuthKeySync() {
        pubnub.configuration.authKey = "someAuthKey"
        testSuccessSync()

        val requests = findAll(anyRequestedFor(mappingBuilder().build().request.urlMatcher))

        if (requiredKeys().contains(AUTH)) {
            assertEquals(pubnub.configuration.authKey, requests.last().queryParameter("auth").firstValue())
        } else {
            requests.forEach {
                assertFalse(it.queryParams.containsKey("auth"))
            }
        }
    }

    private fun testAuthKeyAsync() {
        pubnub.configuration.authKey = "someAuthKey"

        snippet().await { _, status ->
            assertFalse(status.error)

            if (requiredKeys().contains(AUTH)) {
                assertEquals(pubnub.configuration.authKey, status.param("auth"))
            } else {
                assertNull(status.param("auth"))
            }
        }
    }

    private fun testSecretKey() {
        pubnub.configuration.secretKey = " "
        try {
            testSuccessSync()
            if (requiredKeys().contains(SEC)) {
                failTest()
            }
        } catch (e: Exception) {
            if (requiredKeys().contains(SEC)) {
                assertPnException(PubNubError.SECRET_KEY_MISSING, e)
            }
        }
        pubnub.configuration.secretKey = "mySecretKey"
    }

    private fun runSync() {
        val result = snippet().sync()!!
        verifyResultExpectations(result)
    }

    private fun stubTimeEndpoint() {
        stubFor(
            get(urlMatching("/time/0.*"))
                .willReturn(
                    aResponse()
                        .withBody("[1000]")
                )
        )
    }
}

private fun extractKeys(value: Int): List<Int> {
    val keys = mutableListOf<Int>()
    var n = value
    while (n > 0) {
        val power = {
            var res = 0
            for (i in n downTo 1) {
                if (i and i - 1 == 0) {
                    res = i
                    break
                }
            }
            res
        }.invoke()
        keys.add(power)
        n -= power
    }
    return keys
}

val SUB = 0b001
val PUB = 0b010
val AUTH = 0b100
val SEC = 0b1000

private fun Int.contains(sub: Int): Boolean {
    return extractKeys(this).contains(sub)
}

class OptionalScenario<R> {
    var responseBuilder: ResponseDefinitionBuilder.() -> ResponseDefinitionBuilder = { this }

    var additionalChecks: AsyncCheck<R> = { _: PNStatus, _: R? -> }
    var result: Result = Result.SUCCESS
    var pnError: PubNubError? = null

    internal fun build(): ResponseDefinitionBuilder {
        return aResponse().responseBuilder()
    }
}

enum class Result {
    SUCCESS,
    FAIL
}
