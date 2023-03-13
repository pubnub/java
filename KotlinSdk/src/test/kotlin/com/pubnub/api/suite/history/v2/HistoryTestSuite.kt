package com.pubnub.apikt.suite.history.v2

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.endpoints.History
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.PNStatus
import com.pubnub.apikt.models.consumer.history.PNHistoryResult
import com.pubnub.apikt.suite.*
import org.junit.Assert.*

class HistoryTestSuite : EndpointTestSuite<History, PNHistoryResult>() {

    override fun telemetryParamName() = "l_hist"

    override fun pnOperation() = PNOperationType.PNHistoryOperation

    override fun requiredKeys() = SUB + AUTH

    override fun snippet(): History =
        pubnub.history(
            channel = "ch1"
        )

    override fun verifyResultExpectations(result: PNHistoryResult) {
        assertEquals(100L, result.startTimetoken)
        assertEquals(200L, result.endTimetoken)
        assertEquals(2, result.messages.size)
        assertEquals("msg1", result.messages[0].entry.asString)
        assertEquals("msg2", result.messages[1].entry.asString)
        assertNull(result.messages[0].meta)
        assertNull(result.messages[1].meta)
        assertNull(result.messages[0].timetoken)
        assertNull(result.messages[1].timetoken)
    }

    override fun successfulResponseBody() = """[["msg1","msg2"],100,200]"""

    override fun unsuccessfulResponseBodyList() = listOf(
        "[]",
        "[{}]"
    )

    override fun mappingBuilder(): MappingBuilder {
        return get(
            urlPathEqualTo("/v2/history/sub-key/mySubscribeKey/channel/ch1")
        )
            .withQueryParam("include_token", equalTo("false"))
            .withQueryParam("count", equalTo("100"))
            .withQueryParam("include_meta", equalTo("false"))
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
