package com.pubnub.apikt.suite.channel_groups

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.pubnub.apikt.endpoints.channel_groups.DeleteChannelGroup
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.channel_group.PNChannelGroupsDeleteGroupResult
import com.pubnub.apikt.suite.AUTH
import com.pubnub.apikt.suite.EndpointTestSuite
import com.pubnub.apikt.suite.SUB

class DeleteChannelGroupTestSuite : EndpointTestSuite<DeleteChannelGroup, PNChannelGroupsDeleteGroupResult>() {

    override fun telemetryParamName() = "l_cg"

    override fun pnOperation() = PNOperationType.PNRemoveGroupOperation

    override fun requiredKeys() = SUB + AUTH

    override fun snippet(): DeleteChannelGroup {
        return pubnub.deleteChannelGroup(
            channelGroup = "cg1"
        )
    }

    override fun verifyResultExpectations(result: PNChannelGroupsDeleteGroupResult) {
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
        return get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/cg1/remove"))
    }

    override fun affectedChannelsAndGroups() = emptyList<String>() to listOf("cg1")

    override fun voidResponse() = true
}
