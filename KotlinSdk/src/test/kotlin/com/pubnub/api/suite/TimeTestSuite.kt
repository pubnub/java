package com.pubnub.apikt.suite

import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.pubnub.apikt.endpoints.Time
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.PNStatus
import com.pubnub.apikt.models.consumer.PNTimeResult
import org.junit.Assert.*

class TimeTestSuite : EndpointTestSuite<Time, PNTimeResult>() {
    override fun telemetryParamName() = "l_time"

    override fun pnOperation() = PNOperationType.PNTimeOperation

    override fun requiredKeys() = 0

    override fun snippet(): Time {
        return pubnub.time()
    }

    override fun verifyResultExpectations(result: PNTimeResult) {
        assertEquals(1000, result.timetoken)
    }

    override fun successfulResponseBody() = """[1000]"""

    override fun unsuccessfulResponseBodyList() = listOf(
        """{}""",
        """[false]"""
    )

    override fun optionalScenarioList(): List<OptionalScenario<PNTimeResult>> {
        return listOf(
            OptionalScenario<PNTimeResult>().apply {
                responseBuilder = { withBody("[wrong]") }
                result = Result.FAIL
                additionalChecks = { status: PNStatus, result: PNTimeResult? ->
                    assertTrue(status.error)
                    assertNull(result)
                }
            },
            OptionalScenario<PNTimeResult>().apply {
                responseBuilder = { withBody("[123]") }
                additionalChecks = { status: PNStatus, result: PNTimeResult? ->
                    assertFalse(status.error)
                    assertEquals(123, result!!.timetoken)
                }
            }
        )
    }

    override fun mappingBuilder() = get(urlPathEqualTo("/time/0"))

    override fun affectedChannelsAndGroups() = emptyList<String>() to emptyList<String>()
}
