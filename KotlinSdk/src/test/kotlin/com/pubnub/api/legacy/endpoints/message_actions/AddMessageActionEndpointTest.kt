package com.pubnub.apikt.legacy.endpoints.message_actions

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.pubnub.apikt.CommonUtils.assertPnException
import com.pubnub.apikt.CommonUtils.emptyJson
import com.pubnub.apikt.CommonUtils.failTest
import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.legacy.BaseTest
import com.pubnub.apikt.listen
import com.pubnub.apikt.models.consumer.message_actions.PNMessageAction
import com.pubnub.apikt.param
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean

class AddMessageActionEndpointTest : BaseTest() {

    @Test
    fun testSyncSuccess() {
        stubFor(
            post(urlPathEqualTo("/v1/message-actions/mySubscribeKey/channel/coolChannel/message/123"))
                .withRequestBody(equalToJson("""{"type":"emoji","value":"smiley"}"""))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "data": {
                            "messageTimetoken": "123",
                            "type": "emoji",
                            "uuid": "someUuid",
                            "value": "smiley",
                            "actionTimetoken": "1000"
                          }
                        }
                        """.trimIndent()
                    )
                )
        )

        val result = pubnub.addMessageAction(
            channel = "coolChannel",
            messageAction = PNMessageAction(
                type = "emoji",
                value = "smiley",
                messageTimetoken = 123
            )
        ).sync()!!

        assertEquals(result.messageTimetoken, 123L)
        assertEquals(result.type, "emoji")
        assertEquals(result.uuid, "someUuid")
        assertEquals(result.value, "smiley")
        assertEquals(result.actionTimetoken, 1000L)
    }

    @Test
    fun testAsyncSuccess() {
        stubFor(
            post(urlPathEqualTo("/v1/message-actions/mySubscribeKey/channel/coolChannel/message/123"))
                .withRequestBody(equalToJson("""{"type":"emoji","value":"smiley"}"""))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "data": {
                            "messageTimetoken": "123",
                            "type": "emoji",
                            "uuid": "someUuid",
                            "value": "smiley",
                            "actionTimetoken": "1000"
                          }
                        }
                        """.trimIndent()
                    )
                )
        )

        val success = AtomicBoolean()

        pubnub.addMessageAction(
            channel = "coolChannel",
            messageAction = PNMessageAction(
                type = "emoji",
                value = "smiley",
                messageTimetoken = 123
            )
        ).async { result, status ->
            assertFalse(status.error)
            assertEquals(PNOperationType.PNAddMessageAction, status.operation)
            assertEquals(result!!.messageTimetoken, 123L)
            assertEquals(result.type, "emoji")
            assertEquals(result.uuid, "someUuid")
            assertEquals(result.value, "smiley")
            assertEquals(result.actionTimetoken, 1000L)
            success.set(true)
        }

        success.listen()
    }

    @Test
    fun testMalformedJson() {
        stubFor(
            post(urlPathEqualTo("/v1/message-actions/mySubscribeKey/channel/coolChannel/message/123"))
                .withRequestBody(equalToJson("""{"type":"emoji","value":"smiley"}"""))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "data": {
                            "messageTimetoken": "123",
                            "type": "emoji",
                            "uuid": "someUuid",
                            "value": "smiley",
                            "actionTimetoken": "1000"
                        }
                        """.trimIndent()
                    )
                )
        )

        try {
            pubnub.addMessageAction(
                channel = "coolChannel",
                messageAction = PNMessageAction(
                    type = "emoji",
                    value = "smiley",
                    messageTimetoken = 123
                )
            ).sync()!!
            failTest()
        } catch (e: Exception) {
            assertPnException(PubNubError.PARSING_ERROR, e)
        }
    }

    @Test
    fun testEmptyBody() {
        stubFor(
            post(urlPathEqualTo("/v1/message-actions/mySubscribeKey/channel/coolChannel/message/123"))
                .withRequestBody(equalToJson("""{"type":"emoji","value":"smiley"}"""))
                .willReturn(emptyJson())
        )

        try {
            pubnub.addMessageAction(
                channel = "coolChannel",
                messageAction = PNMessageAction(
                    type = "emoji",
                    value = "smiley",
                    messageTimetoken = 123
                )
            ).sync()!!
            failTest()
        } catch (e: Exception) {
            assertPnException(PubNubError.PARSING_ERROR, e)
        }
    }

    @Test
    fun testNoBody() {
        stubFor(
            post(urlPathEqualTo("/v1/message-actions/mySubscribeKey/channel/coolChannel/message/123"))
                .withRequestBody(equalToJson("""{"type":"emoji","value":"smiley"}"""))
                .willReturn(noContent())
        )

        try {
            pubnub.addMessageAction(
                channel = "coolChannel",
                messageAction = PNMessageAction(
                    type = "emoji",
                    value = "smiley",
                    messageTimetoken = 123
                )
            ).sync()!!
            failTest()
        } catch (e: Exception) {
            assertPnException(PubNubError.PARSING_ERROR, e)
        }
    }

    @Test
    fun testNoData() {
        stubFor(
            post(urlPathEqualTo("/v1/message-actions/mySubscribeKey/channel/coolChannel/message/123"))
                .withRequestBody(equalToJson("""{"type":"emoji","value":"smiley"}"""))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200
                        }
                        """.trimIndent()
                    )
                )
        )

        try {
            pubnub.addMessageAction(
                channel = "coolChannel",
                messageAction = PNMessageAction(
                    type = "emoji",
                    value = "smiley",
                    messageTimetoken = 123
                )
            ).sync()!!
            failTest()
        } catch (e: Exception) {
            assertPnException(PubNubError.PARSING_ERROR, e)
        }
    }

    @Test
    fun testNullData() {
        stubFor(
            post(urlPathEqualTo("/v1/message-actions/mySubscribeKey/channel/coolChannel/message/123"))
                .withRequestBody(equalToJson("""{"type":"emoji","value":"smiley"}"""))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "data": null
                        }
                        """.trimIndent()
                    )
                )
        )

        try {
            pubnub.addMessageAction(
                channel = "coolChannel",
                messageAction = PNMessageAction(
                    type = "emoji",
                    value = "smiley",
                    messageTimetoken = 123
                )
            ).sync()!!
            failTest()
        } catch (e: Exception) {
            assertPnException(PubNubError.PARSING_ERROR, e)
        }
    }

    @Test
    fun testBlankChannel() {
        try {
            pubnub.addMessageAction(
                channel = " ",
                messageAction = PNMessageAction(
                    type = "emoji",
                    value = "smiley",
                    messageTimetoken = 123
                )
            ).sync()!!
            failTest()
        } catch (e: Exception) {
            assertPnException(PubNubError.CHANNEL_MISSING, e)
        }
    }

    @Test
    fun testBlankType() {
        try {
            pubnub.addMessageAction(
                channel = "coolChannel",
                messageAction = PNMessageAction(
                    type = " ",
                    value = "smiley",
                    messageTimetoken = 123
                )
            ).sync()!!
            failTest()
        } catch (e: Exception) {
            assertPnException(PubNubError.MESSAGE_ACTION_TYPE_MISSING, e)
        }
    }

    @Test
    fun testBlankValue() {
        try {
            pubnub.addMessageAction(
                channel = "coolChannel",
                messageAction = PNMessageAction(
                    type = "emoji",
                    value = " ",
                    messageTimetoken = 123
                )
            ).sync()!!
            failTest()
        } catch (e: Exception) {
            assertPnException(PubNubError.MESSAGE_ACTION_VALUE_MISSING, e)
        }
    }

    @Test
    fun testInvalidSubKey() {
        pubnub.configuration.subscribeKey = " "
        try {
            pubnub.addMessageAction(
                channel = "coolChannel",
                messageAction = PNMessageAction(
                    type = "emoji",
                    value = " ",
                    messageTimetoken = 123
                )
            ).sync()!!
            failTest()
        } catch (e: Exception) {
            assertPnException(PubNubError.SUBSCRIBE_KEY_MISSING, e)
        }
    }

    @Test
    fun testAuthKeyRequired() {
        stubFor(
            post(urlPathEqualTo("/v1/message-actions/mySubscribeKey/channel/coolChannel/message/123"))
                .withRequestBody(equalToJson("""{"type":"emoji","value":"smiley"}"""))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "data": {
                            "messageTimetoken": "123",
                            "type": "emoji",
                            "uuid": "someUuid",
                            "value": "smiley",
                            "actionTimetoken": "1000"
                          }
                        }
                        """.trimIndent()
                    )
                )
        )

        pubnub.configuration.authKey = "authKey"

        pubnub.addMessageAction(
            channel = "coolChannel",
            messageAction = PNMessageAction(
                type = "emoji",
                value = "smiley",
                messageTimetoken = 123
            )
        ).sync()!!

        val requests = findAll(postRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)

        assertEquals("authKey", requests[0].queryParameter("auth").firstValue())
    }

    @Test
    fun testTelemetryParam() {
        val success = AtomicBoolean()

        stubFor(
            post(urlPathEqualTo("/v1/message-actions/mySubscribeKey/channel/coolChannel/message/123"))
                .withRequestBody(equalToJson("""{"type":"emoji","value":"smiley"}"""))
                .willReturn(
                    aResponse().withBody(
                        """
                        {
                          "status": 200,
                          "data": {
                            "messageTimetoken": "123",
                            "type": "emoji",
                            "uuid": "someUuid",
                            "value": "smiley",
                            "actionTimetoken": "1000"
                          }
                        }
                        """.trimIndent()
                    )
                )
        )

        stubFor(
            get(urlMatching("/time/0.*"))
                .willReturn(aResponse().withBody("[1000]"))
        )

        lateinit var telemetryParamName: String

        pubnub.addMessageAction(
            channel = "coolChannel",
            messageAction = PNMessageAction(
                type = "emoji",
                value = "smiley",
                messageTimetoken = 123
            )
        ).async { _, status ->
            assertFalse(status.error)
            assertEquals(PNOperationType.PNAddMessageAction, status.operation)
            telemetryParamName = "l_${status.operation.queryParam}"
            assertEquals("l_msga", telemetryParamName)
            success.set(true)
        }

        success.listen()

        pubnub.time().async { _, status ->
            assertFalse(status.error)
            assertNotNull(status.param(telemetryParamName))
            success.set(true)
        }

        success.listen()
    }
}
