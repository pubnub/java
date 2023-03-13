package com.pubnub.apikt.suite.channel_groups

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.pubnub.apikt.endpoints.channel_groups.ListAllChannelGroup
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.PNStatus
import com.pubnub.apikt.models.consumer.channel_group.PNChannelGroupsListAllResult
import com.pubnub.apikt.suite.*
import org.junit.Assert.*

class ListAllChannelGroupTestSuite : EndpointTestSuite<ListAllChannelGroup, PNChannelGroupsListAllResult>() {

    override fun telemetryParamName() = "l_cg"

    override fun pnOperation() = PNOperationType.PNChannelGroupsOperation

    override fun requiredKeys() = SUB + AUTH

    override fun snippet(): ListAllChannelGroup {
        return pubnub.listAllChannelGroups()
    }

    override fun verifyResultExpectations(result: PNChannelGroupsListAllResult) {
        assertEquals(listOf("cg1", "cg2"), result.groups)
    }

    override fun successfulResponseBody() = """{"payload":{"groups":["cg1","cg2"]}}"""

    override fun unsuccessfulResponseBodyList() = listOf(
        """{"payload":{"groups":null}}""",
        """{"payload":{"groups":{}}}""",
        """{"payload":{}}""",
        """{"payload":null}"""
    )

    override fun mappingBuilder(): MappingBuilder {
        return get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group"))
    }

    override fun affectedChannelsAndGroups() = emptyList<String>() to emptyList<String>()

    override fun optionalScenarioList(): List<OptionalScenario<PNChannelGroupsListAllResult>> {
        return listOf(
            OptionalScenario<PNChannelGroupsListAllResult>().apply {
                responseBuilder = {
                    withBody("""{"payload":{"groups":[]}}""")
                }
                additionalChecks = { status: PNStatus, result: PNChannelGroupsListAllResult? ->
                    assertFalse(status.error)
                    assertTrue(result!!.groups.isEmpty())
                }
                result = Result.SUCCESS
            }
        )
    }
}
