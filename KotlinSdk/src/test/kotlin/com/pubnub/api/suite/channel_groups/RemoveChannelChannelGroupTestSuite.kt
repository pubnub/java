package com.pubnub.apikt.suite.channel_groups

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.pubnub.apikt.endpoints.channel_groups.RemoveChannelChannelGroup
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.PNStatus
import com.pubnub.apikt.models.consumer.channel_group.PNChannelGroupsRemoveChannelResult
import com.pubnub.apikt.suite.*
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

class RemoveChannelChannelGroupTestSuite :
    EndpointTestSuite<RemoveChannelChannelGroup, PNChannelGroupsRemoveChannelResult>() {

    override fun telemetryParamName() = "l_cg"

    override fun pnOperation() = PNOperationType.PNRemoveChannelsFromGroupOperation

    override fun requiredKeys() = SUB + AUTH

    override fun snippet(): RemoveChannelChannelGroup {
        return pubnub.removeChannelsFromChannelGroup(
            channelGroup = "cg1",
            channels = listOf("ch1", "ch2")
        )
    }

    override fun verifyResultExpectations(result: PNChannelGroupsRemoveChannelResult) {}

    override fun successfulResponseBody() = """{"payload":{"groups":["cg1","cg2"]}}"""

    override fun unsuccessfulResponseBodyList() = emptyList<String>()

    override fun mappingBuilder(): MappingBuilder {
        return get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/cg1"))
            .withQueryParam("remove", equalTo("ch1,ch2"))
    }

    override fun affectedChannelsAndGroups() = listOf("ch1", "ch2") to listOf("cg1")

    override fun voidResponse() = true

    override fun optionalScenarioList(): List<OptionalScenario<PNChannelGroupsRemoveChannelResult>> {
        return listOf(
            OptionalScenario<PNChannelGroupsRemoveChannelResult>().apply {
                responseBuilder = { withBody("").withStatus(400) }
                result = Result.FAIL
                additionalChecks = { pnStatus: PNStatus, result: PNChannelGroupsRemoveChannelResult? ->
                    assertTrue(pnStatus.error)
                    assertNull(result)
                }
            }
        )
    }
}
