package com.pubnub.apikt.suite.presence

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.pubnub.apikt.endpoints.presence.GetState
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.presence.PNGetStateResult
import com.pubnub.apikt.suite.AUTH
import com.pubnub.apikt.suite.EndpointTestSuite
import com.pubnub.apikt.suite.OptionalScenario
import com.pubnub.apikt.suite.SUB
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse

class GetStateTestSuite : EndpointTestSuite<GetState, PNGetStateResult>() {

    override fun telemetryParamName() = "l_pres"

    override fun pnOperation() = PNOperationType.PNGetState

    override fun requiredKeys() = SUB + AUTH

    override fun snippet(): GetState =
        pubnub.getPresenceState(
            channels = listOf("ch1")
        )

    override fun verifyResultExpectations(result: PNGetStateResult) {
        assertEquals(1, result.stateByUUID.keys.size)
        assertEquals(JsonObject().apply { addProperty("text", "hello") }, result.stateByUUID["ch1"])
    }

    override fun successfulResponseBody() = """
        {
         "status": 200,
         "message": "OK",
         "payload": {
          "text": "hello"
         },
         "uuid": "myUUID",
         "channel": "ch1",
         "service": "Presence"
        }
    """.trimIndent()

    override fun unsuccessfulResponseBodyList() = emptyList<String>()

    override fun mappingBuilder(): MappingBuilder =
        get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1/uuid/myUUID"))

    override fun affectedChannelsAndGroups() = listOf("ch1") to emptyList<String>()

    override fun optionalScenarioList(): List<OptionalScenario<PNGetStateResult>> {
        return listOf(
            OptionalScenario<PNGetStateResult>().apply {
                responseBuilder = { withBody("""{"payload":{}}""") }
                additionalChecks = { pnStatus, result ->
                    assertFalse(pnStatus.error)
                    assertEquals(JsonObject(), result!!.stateByUUID["ch1"])
                }
            },
            OptionalScenario<PNGetStateResult>().apply {
                responseBuilder = { withBody("""{"payload":null}""") }
                additionalChecks = { pnStatus, result ->
                    assertFalse(pnStatus.error)
                    assertEquals(JsonNull.INSTANCE, result!!.stateByUUID["ch1"])
                }
            }
        )
    }
}
