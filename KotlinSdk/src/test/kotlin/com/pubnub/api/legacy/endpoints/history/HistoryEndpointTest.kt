package com.pubnub.apikt.legacy.endpoints.history

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.pubnub.apikt.CommonUtils.assertPnException
import com.pubnub.apikt.CommonUtils.failTest
import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.legacy.BaseTest
import com.pubnub.apikt.listen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean

class HistoryEndpointTest : BaseTest() {

    @Test
    fun testSyncDisabled() {
        val payload = """
            [
              "Use of the history API requires the Storage & Playback which is not enabled for this subscribe key. Login to your PubNub Dashboard Account and enable Storage & Playback. Contact support@pubnub.com if you require further assistance.",
              0,
              0
            ]
        """.trimIndent()

        stubFor(
            get(urlPathEqualTo("/v2/history/sub-key/mySubscribeKey/channel/niceChannel"))
                .willReturn(aResponse().withBody(payload))
        )

        try {
            pubnub.history(
                channel = "niceChannel"
            ).sync()!!
            throw RuntimeException()
        } catch (e: PubNubException) {
            assertPnException(PubNubError.HTTP_ERROR, e)
            assertEquals("History is disabled", e.errorMessage)
        }
    }

    @Test
    fun testSyncWithTokensDisabled() {
        val payload = """
            [
              "Use of the history API requires the Storage & Playback which is not enabled for this subscribe key. Login to your PubNub Dashboard Account and enable Storage & Playback. Contact support@pubnub.com if you require further assistance.",
              0,
              0
            ]
        """.trimIndent()

        stubFor(
            get(urlPathEqualTo("/v2/history/sub-key/mySubscribeKey/channel/niceChannel"))
                .willReturn(aResponse().withBody(payload))
        )

        try {
            pubnub.history(
                channel = "niceChannel",
                includeTimetoken = true
            ).sync()!!
            failTest()
        } catch (e: PubNubException) {
            assertPnException(PubNubError.HTTP_ERROR, e)
            assertEquals("History is disabled", e.errorMessage)
        }
    }

    @Test
    fun testSyncSuccess() {
        val historyItem1 = mapOf(
            "a" to 11,
            "b" to 22
        )
        val historyEnvelope1 = mapOf(
            "timetoken" to 1111,
            "message" to historyItem1
        )

        val historyItem2 = mapOf(
            "a" to 33,
            "b" to 44
        )
        val historyEnvelope2 = mapOf(
            "timetoken" to 2222,
            "message" to historyItem2
        )

        val historyItems = listOf(
            historyEnvelope1,
            historyEnvelope2
        )
        val testArray = listOf(
            historyItems,
            1234,
            4321
        )

        stubFor(
            get(urlPathEqualTo("/v2/history/sub-key/mySubscribeKey/channel/niceChannel"))
                .willReturn(aResponse().withBody(pubnub.mapper.toJson(testArray)))
        )

        val result = pubnub.history(
            channel = "niceChannel",
            includeTimetoken = true
        ).sync()!!

        with(result) {
            assertEquals(1234L, startTimetoken)
            assertEquals(4321L, endTimetoken)

            assertEquals(2, messages.size)

            assertEquals(1111L, messages[0].timetoken)
            assertEquals(11, messages[0].entry.asJsonObject["a"].asInt)
            assertEquals(22, messages[0].entry.asJsonObject["b"].asInt)

            assertEquals(2222L, messages[1].timetoken)
            assertEquals(33, messages[1].entry.asJsonObject["a"].asInt)
            assertEquals(44, messages[1].entry.asJsonObject["b"].asInt)
        }
    }

    @Test
    fun testSyncAuthSuccess() {
        pubnub.configuration.authKey = "authKey"

        val historyItem1 = mapOf(
            "a" to 11,
            "b" to 22
        )
        val historyEnvelope1 = mapOf(
            "timetoken" to 1111,
            "message" to historyItem1
        )

        val historyItem2 = mapOf(
            "a" to 33,
            "b" to 44
        )
        val historyEnvelope2 = mapOf(
            "timetoken" to 2222,
            "message" to historyItem2
        )

        val historyItems = listOf(
            historyEnvelope1,
            historyEnvelope2
        )
        val testArray = listOf(
            historyItems,
            1234,
            4321
        )

        stubFor(
            get(urlPathEqualTo("/v2/history/sub-key/mySubscribeKey/channel/niceChannel"))
                .willReturn(aResponse().withBody(pubnub.mapper.toJson(testArray)))
        )

        pubnub.history(
            channel = "niceChannel",
            includeTimetoken = true
        ).sync()!!

        val requests = findAll(getRequestedFor(anyUrl()))
        assertEquals(1, requests.size)
        assertEquals("authKey", requests.first().queryParameter("auth").firstValue())
    }

    @Test
    fun testSyncEncryptedSuccess() {
        pubnub.configuration.cipherKey = "testCipher"
        pubnub.configuration.useRandomInitializationVector = false

        stubFor(
            get(urlPathEqualTo("/v2/history/sub-key/mySubscribeKey/channel/niceChannel"))
                .willReturn(
                    aResponse().withBody(
                        """
                            [
                              [
                                "EGwV+Ti43wh2TprPIq7o0KMuW5j6B3yWy352ucWIOmU=\n",
                                "EGwV+Ti43wh2TprPIq7o0KMuW5j6B3yWy352ucWIOmU=\n",
                                "EGwV+Ti43wh2TprPIq7o0KMuW5j6B3yWy352ucWIOmU=\n"
                              ],
                              14606134331557852,
                              14606134485013970
                            ]
                        """.trimIndent()
                    )
                )
        )

        val result = pubnub.history(
            channel = "niceChannel",
            includeTimetoken = false
        ).sync()!!

        with(result) {
            assertEquals(14606134331557852, startTimetoken)
            assertEquals(14606134485013970, endTimetoken)

            assertEquals(3, messages.size)

            assertNull(messages[0].timetoken)

            assertEquals("m1", messages[0].entry.asJsonArray[0].asString)
            assertEquals("m2", messages[0].entry.asJsonArray[1].asString)
            assertEquals("m3", messages[0].entry.asJsonArray[2].asString)

            assertEquals("m1", messages[1].entry.asJsonArray[0].asString)
            assertEquals("m2", messages[1].entry.asJsonArray[1].asString)
            assertEquals("m3", messages[1].entry.asJsonArray[2].asString)

            assertEquals("m1", messages[2].entry.asJsonArray[0].asString)
            assertEquals("m2", messages[2].entry.asJsonArray[1].asString)
            assertEquals("m3", messages[2].entry.asJsonArray[2].asString)
        }
    }

    @Test
    fun testSyncEncryptedWithPNOtherSuccess() {
        pubnub.configuration.cipherKey = "hello"
        pubnub.configuration.useRandomInitializationVector = false

        stubFor(
            get(urlPathEqualTo("/v2/history/sub-key/mySubscribeKey/channel/niceChannel"))
                .willReturn(
                    aResponse().withBody(
                        """
                        [
                          [
                            {
                              "pn_other": "6QoqmS9CnB3W9+I4mhmL7w=="
                            }
                          ],
                          14606134331557852,
                          14606134485013970
                        ]
                        """.trimIndent()
                    )
                )
        )

        val result = pubnub.history(
            channel = "niceChannel",
            includeTimetoken = false
        ).sync()!!

        with(result) {
            assertEquals(14606134331557852, startTimetoken)
            assertEquals(14606134485013970, endTimetoken)

            assertEquals(1, messages.size)

            assertNull(messages[0].timetoken)
            assertEquals("hey", messages[0].entry.asJsonObject.get("pn_other").asJsonObject.get("text").asString)
        }
    }

    @Test
    fun testSyncSuccessWithoutTimeToken() {
        val historyItem1 = mapOf(
            "a" to 11,
            "b" to 22
        )
        val historyItem2 = mapOf(
            "a" to 33,
            "b" to 44
        )

        val testArray = listOf(
            listOf(
                historyItem1,
                historyItem2
            ),
            1234,
            4321
        )

        stubFor(
            get(urlPathEqualTo("/v2/history/sub-key/mySubscribeKey/channel/niceChannel"))
                .willReturn(aResponse().withBody(pubnub.mapper.toJson(testArray)))
        )

        val result = pubnub.history(
            channel = "niceChannel"
        ).sync()!!

        with(result) {
            assertEquals(1234, startTimetoken)
            assertEquals(4321, endTimetoken)

            assertEquals(2, messages.size)

            assertNull(messages[0].timetoken)
            assertEquals(11, messages[0].entry.asJsonObject["a"].asInt)
            assertEquals(22, messages[0].entry.asJsonObject["b"].asInt)

            assertNull(messages[1].timetoken)
            assertEquals(33, messages[1].entry.asJsonObject["a"].asInt)
            assertEquals(44, messages[1].entry.asJsonObject["b"].asInt)
        }
    }

    @Test
    fun testChannelIsEmpty() {
        try {
            pubnub.history(
                channel = ""
            ).sync()!!
            throw RuntimeException()
        } catch (e: PubNubException) {
            assertPnException(PubNubError.CHANNEL_MISSING, e)
        }
    }

    @Test
    fun testChannelIsBlank() {
        try {
            pubnub.history(
                channel = "   "
            ).sync()!!
            throw RuntimeException()
        } catch (e: PubNubException) {
            assertPnException(PubNubError.CHANNEL_MISSING, e)
        }
    }

    @Test
    fun testOperationTypeSuccessAsync() {
        val historyEnvelope1 = mapOf(
            "timetoken" to 1111,
            "message" to mapOf(
                "a" to 11,
                "b" to 22
            )
        )

        val historyEnvelope2 = mapOf(
            "timetoken" to 2222,
            "message" to mapOf(
                "a" to 33,
                "b" to 44
            )
        )

        val historyItems = listOf(
            historyEnvelope1,
            historyEnvelope2
        )
        val testArray = listOf(
            historyItems,
            1234,
            4321
        )

        stubFor(
            get(urlPathEqualTo("/v2/history/sub-key/mySubscribeKey/channel/niceChannel"))
                .willReturn(aResponse().withBody(pubnub.mapper.toJson(testArray)))
        )

        val success = AtomicBoolean()

        pubnub.history(
            channel = "niceChannel",
            includeTimetoken = true
        ).async { result, status ->

            assertEquals(PNOperationType.PNHistoryOperation, status.operation)

            with(result!!) {
                assertEquals(1234L, startTimetoken)
                assertEquals(4321L, endTimetoken)

                assertEquals(2, messages.size)

                assertEquals(1111L, messages[0].timetoken)
                assertEquals(11, messages[0].entry.asJsonObject["a"].asInt)
                assertEquals(22, messages[0].entry.asJsonObject["b"].asInt)

                assertEquals(2222L, messages[1].timetoken)
                assertEquals(33, messages[1].entry.asJsonObject["a"].asInt)
                assertEquals(44, messages[1].entry.asJsonObject["b"].asInt)
            }

            success.set(true)
        }

        success.listen()
    }

    @Test
    fun testSyncCountReverseStartEndSuccess() {
        val historyItem1 = mapOf(
            "a" to 11,
            "b" to 22
        )
        val historyEnvelope1 = mapOf(
            "timetoken" to 1111,
            "message" to historyItem1
        )

        val historyItem2 = mapOf(
            "a" to 33,
            "b" to 44
        )
        val historyEnvelope2 = mapOf(
            "timetoken" to 2222,
            "message" to historyItem2
        )

        val historyItems = listOf(
            historyEnvelope1,
            historyEnvelope2
        )
        val testArray = listOf(
            historyItems,
            1234,
            4321
        )

        stubFor(
            get(urlPathEqualTo("/v2/history/sub-key/mySubscribeKey/channel/niceChannel"))
                .willReturn(aResponse().withBody(pubnub.mapper.toJson(testArray)))
        )

        val result = pubnub.history(
            channel = "niceChannel",
            count = 5,
            reverse = true,
            start = 1L,
            end = 2L,
            includeTimetoken = true
        ).sync()!!

        val requests = findAll(
            getRequestedFor(
                urlMatching("/v2/history/sub-key/mySubscribeKey/channel/niceChannel.*")
            )
        )

        assertEquals(1, requests.size)

        assertEquals("true", requests.first().queryParameter("reverse").firstValue())
        assertEquals("5", requests.first().queryParameter("count").firstValue())
        assertEquals("1", requests.first().queryParameter("start").firstValue())
        assertEquals("2", requests.first().queryParameter("end").firstValue())
        assertEquals("true", requests.first().queryParameter("include_token").firstValue())

        with(result) {
            assertEquals(1234L, startTimetoken)
            assertEquals(4321L, endTimetoken)

            assertEquals(2, messages.size)

            assertEquals(1111L, messages[0].timetoken)
            assertEquals(11, messages[0].entry.asJsonObject["a"].asInt)
            assertEquals(22, messages[0].entry.asJsonObject["b"].asInt)

            assertEquals(2222L, messages[1].timetoken)
            assertEquals(33, messages[1].entry.asJsonObject["a"].asInt)
            assertEquals(44, messages[1].entry.asJsonObject["b"].asInt)
        }
    }

    @Test
    fun testSyncProcessMessageError() {
        val historyItem1 = mapOf(
            "a" to 11,
            "b" to 22
        )
        val historyEnvelope1 = mapOf(
            "timetoken" to 1111,
            "message" to historyItem1
        )

        val historyItem2 = mapOf(
            "a" to 33,
            "b" to 44
        )
        val historyEnvelope2 = mapOf(
            "timetoken" to 2222,
            "message" to historyItem2
        )

        val historyItems = listOf(
            historyEnvelope1,
            historyEnvelope2
        )
        val testArray = listOf(
            historyItems,
            1234,
            4321
        )

        stubFor(
            get(urlPathEqualTo("/v2/history/sub-key/mySubscribeKey/channel/niceChannel"))
                .willReturn(aResponse().withBody(pubnub.mapper.toJson(testArray)))
        )

        pubnub.configuration.cipherKey = "Test"

        try {
            pubnub.history(
                channel = "niceChannel",
                count = 5,
                reverse = true,
                start = 1L,
                end = 2L,
                includeTimetoken = true
            ).sync()!!
            failTest()
        } catch (e: UnsupportedOperationException) {
        }
    }
}
