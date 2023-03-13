package com.pubnub.apikt.suite.history.v3

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.endpoints.FetchMessages
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.enums.PNStatusCategory
import com.pubnub.apikt.models.consumer.PNStatus
import com.pubnub.apikt.models.consumer.history.PNFetchMessagesResult
import com.pubnub.apikt.suite.*
import org.junit.Assert.*

class FetchMessagesTestSuite : EndpointTestSuite<FetchMessages, PNFetchMessagesResult>() {

    override fun telemetryParamName() = "l_hist"

    override fun pnOperation() = PNOperationType.PNFetchMessagesOperation

    override fun requiredKeys() = SUB + AUTH

    override fun snippet(): FetchMessages =
        pubnub.fetchMessages(
            channels = listOf("ch1")
        )

    override fun verifyResultExpectations(result: PNFetchMessagesResult) {
        assertEquals(1, result.channels.size)
        assertTrue(result.channels.containsKey("ch1"))
        assertEquals("hello", result.channels["ch1"]!![0].message.asString)
        assertEquals(100, result.channels["ch1"]!![0].timetoken)
        assertNull(result.channels["ch1"]!![0].meta)
    }

    override fun successfulResponseBody() = """
        {
         "channels": {
          "ch1": [
           {
            "message": "hello",
            "timetoken": 100
           }
          ]
         }
        }
    """.trimIndent()

    override fun unsuccessfulResponseBodyList() = listOf(
        """
            {
             "channels": {
              "ch1": [
               {
                "timetoken": 100
               }
              ]
             }
            }
        """.trimIndent(),
        """
            {
             "channels": {
              "ch1": [
               {
                "entry": "hello",
                "timetoken": 100
               }
              ],
              "ch2": [],
              "ch3": null
             }
            }
        """.trimIndent()
    )

    override fun mappingBuilder(): MappingBuilder {
        return get(urlPathEqualTo("/v3/history/sub-key/mySubscribeKey/channel/ch1"))
            .withQueryParam("max", equalTo("100"))
            .withQueryParam("include_meta", absent())
    }

    override fun affectedChannelsAndGroups() = listOf("ch1") to emptyList<String>()

    override fun optionalScenarioList(): List<OptionalScenario<PNFetchMessagesResult>> {
        return listOf(
            OptionalScenario<PNFetchMessagesResult>().apply {
                responseBuilder = { withBody("""{"channels":{"ch3":null}}""") }
                result = Result.FAIL
                pnError = PubNubError.PARSING_ERROR
                additionalChecks = { status: PNStatus, result: PNFetchMessagesResult? ->
                    assertTrue(status.error)
                    assertNull(result)
                    assertEquals(PNStatusCategory.PNMalformedResponseCategory, status.category)
                }
            }
        )
    }
}
