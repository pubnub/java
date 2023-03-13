package com.pubnub.apikt.suite.history.v2

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.endpoints.History
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.PNStatus
import com.pubnub.apikt.models.consumer.history.PNHistoryResult
import com.pubnub.apikt.suite.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class HistoryMetaTestSuite : EndpointTestSuite<History, PNHistoryResult>() {

    override fun telemetryParamName() = "l_hist"

    override fun pnOperation() = PNOperationType.PNHistoryOperation

    override fun requiredKeys() = SUB + AUTH

    override fun snippet(): History =
        pubnub.history(
            channel = "ch1",
            includeMeta = true,
            includeTimetoken = true
        )

    override fun verifyResultExpectations(result: PNHistoryResult) {
        assertEquals(100L, result.startTimetoken)
        assertEquals(200L, result.endTimetoken)
        assertEquals(2, result.messages.size)
        assertEquals("msg1", result.messages[0].entry.asString)
        assertEquals("msg2", result.messages[1].entry.asString)
        assertTrue(result.messages[0].meta!!.asString.isBlank())
        assertEquals(
            mapOf("color" to "red"),
            Gson().fromJson(
                result.messages[1].meta!!.asJsonObject,
                object : TypeToken<Map<String, String>?>() {}.type
            )
        )
        assertEquals(100L, result.messages[0].timetoken)
        assertEquals(200L, result.messages[1].timetoken)
    }

    override fun successfulResponseBody() = """
        [
         [
          {
           "message": "msg1",
           "timetoken": 100,
           "meta": ""
          },
          {
           "message": "msg2",
           "timetoken": 200,
           "meta": {
            "color": "red"
           }
          }
         ],
         100,
         200
        ]
    """.trimIndent()

    override fun unsuccessfulResponseBodyList() = listOf(
        "[]",
        "[{}]"
    )

    override fun mappingBuilder(): MappingBuilder {
        return get(
            urlPathEqualTo("/v2/history/sub-key/mySubscribeKey/channel/ch1")
        )
            .withQueryParam("include_token", equalTo("true"))
            .withQueryParam("count", equalTo("100"))
            .withQueryParam("include_meta", equalTo("true"))
            .withQueryParam("reverse", equalTo("false"))
    }

    override fun affectedChannelsAndGroups() = listOf("ch1") to emptyList<String>()

    override fun optionalScenarioList() = listOf(
        OptionalScenario<PNHistoryResult>().apply {
            responseBuilder = {
                withBody("""["First Element Not An Array",0,0]""")
            }
            additionalChecks = { status: PNStatus, _: PNHistoryResult? ->
                assertTrue(status.error)
                assertEquals(status.exception!!.errorMessage, "History is disabled")
            }
            result = Result.FAIL
            pnError = PubNubError.HTTP_ERROR
        }
    )
}
