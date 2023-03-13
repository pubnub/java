package com.pubnub.apikt.suite.grant

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.pubnub.apikt.endpoints.access.Grant
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.access_manager.PNAccessManagerGrantResult
import com.pubnub.apikt.suite.EndpointTestSuite
import com.pubnub.apikt.suite.SEC
import com.pubnub.apikt.suite.SUB
import org.junit.Assert.assertEquals

class GrantTestSuite : EndpointTestSuite<Grant, PNAccessManagerGrantResult>() {

    override fun onBefore() {
        super.onBefore()
        pubnub.configuration.secretKey = "mySecretKey"
    }

    override fun telemetryParamName() = "l_pam"

    override fun pnOperation() = PNOperationType.PNAccessManagerGrant

    override fun requiredKeys() = SUB + SEC

    override fun snippet(): Grant {
        return pubnub.grant().apply {
        }
    }

    override fun verifyResultExpectations(result: PNAccessManagerGrantResult) {
        assertEquals("mySubscribeKey", result.subscribeKey)
        assertEquals(0, result.channels.size)
        assertEquals(0, result.channelGroups.size)
        assertEquals("subkey", result.level)
        assertEquals(1, result.ttl)
    }

    override fun successfulResponseBody() = """
        {
         "message": "Success",
         "payload": {
          "level": "subkey",
          "subscribe_key": "mySubscribeKey",
          "ttl": 1,
          "r": 0,
          "w": 0,
          "m": 0,
          "d": 0
         },
         "service": "Access Manager",
         "status": 200
        }
    """.trimIndent()

    override fun unsuccessfulResponseBodyList() = listOf(
        """{"payload":{}}""",
        """{"payload":null}"""
    )

    override fun mappingBuilder(): MappingBuilder {
        return get(urlPathEqualTo("/v2/auth/grant/sub-key/mySubscribeKey"))
            .withQueryParam("r", equalTo("0"))
            .withQueryParam("w", equalTo("0"))
            .withQueryParam("d", equalTo("0"))
            .withQueryParam("m", equalTo("0"))
            .withQueryParam("ttl", equalTo("-1"))
            .withQueryParam("signature", matching(".*"))
    }

    override fun affectedChannelsAndGroups() = emptyList<String>() to emptyList<String>()
}
