package com.pubnub.apikt.suite.presence

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.google.gson.JsonObject
import com.pubnub.apikt.endpoints.presence.SetState
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.PNStatus
import com.pubnub.apikt.models.consumer.presence.PNSetStateResult
import com.pubnub.apikt.suite.*
import org.junit.Assert.*

class SetStateTestSuite : EndpointTestSuite<SetState, PNSetStateResult>() {

    override fun telemetryParamName() = "l_pres"

    override fun pnOperation() = PNOperationType.PNSetStateOperation

    override fun requiredKeys() = SUB + AUTH

    override fun snippet(): SetState =
        pubnub.setPresenceState(
            channels = listOf("ch1"),
            state = mapOf("text" to "hello")
        )

    override fun verifyResultExpectations(result: PNSetStateResult) {
        assertEquals(JsonObject().apply { addProperty("text", "hello") }, result.state)
    }

    override fun successfulResponseBody() = """
        {
          "status": 200,
          "message": "OK",
          "payload": {
            "text": "hello"
          },
          "service": "Presence"
        }
    """.trimIndent()

    override fun unsuccessfulResponseBodyList() = listOf(
        """{"payload":null}"""
    )

    override fun mappingBuilder(): MappingBuilder =
        get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1/uuid/myUUID/data"))
            .withQueryParam("state", equalTo("""{"text":"hello"}"""))

    override fun affectedChannelsAndGroups() = listOf("ch1") to emptyList<String>()

    override fun optionalScenarioList(): List<OptionalScenario<PNSetStateResult>> {
        return listOf(
            OptionalScenario<PNSetStateResult>().apply {
                result = Result.SUCCESS
                responseBuilder = { withBody("""{"payload":{}}""") }
                additionalChecks = { pnStatus: PNStatus, result: PNSetStateResult? ->
                    assertFalse(pnStatus.error)
                    assertTrue(result!!.state.asJsonObject.keySet().isEmpty())
                }
            },
            OptionalScenario<PNSetStateResult>().apply {
                result = Result.FAIL
                responseBuilder = { withBody("""{"payload":null}""") }
                additionalChecks = { pnStatus: PNStatus, result: PNSetStateResult? ->
                    assertTrue(pnStatus.error)
                    assertNull(result)
                }
            }
        )
    }
}
