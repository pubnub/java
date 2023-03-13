package com.pubnub.apikt.suite.channel_groups

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.pubnub.apikt.endpoints.channel_groups.AddChannelChannelGroup
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.channel_group.PNChannelGroupsAddChannelResult
import com.pubnub.apikt.suite.AUTH
import com.pubnub.apikt.suite.EndpointTestSuite
import com.pubnub.apikt.suite.SUB

class AddChannelChannelGroupTestSuite : EndpointTestSuite<AddChannelChannelGroup, PNChannelGroupsAddChannelResult>() {

    override fun telemetryParamName() = "l_cg"

    override fun pnOperation() = PNOperationType.PNAddChannelsToGroupOperation

    override fun requiredKeys() = SUB + AUTH

    override fun snippet(): AddChannelChannelGroup {
        return pubnub.addChannelsToChannelGroup(
            channelGroup = "cg1",
            channels = listOf("ch1", "ch2")
        )
    }

    override fun verifyResultExpectations(result: PNChannelGroupsAddChannelResult) {
    }

    override fun successfulResponseBody() = """
        {
         "status": 200,
         "message": "OK",
         "service": "channel-registry",
         "error": false
        }
    """.trimIndent()

    override fun unsuccessfulResponseBodyList() = emptyList<String>()

    override fun mappingBuilder(): MappingBuilder {
        return get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/cg1"))
            .withQueryParam("add", equalTo("ch1,ch2"))
    }

    override fun affectedChannelsAndGroups() = listOf("ch1", "ch2") to listOf("cg1")

    override fun voidResponse() = true
}
