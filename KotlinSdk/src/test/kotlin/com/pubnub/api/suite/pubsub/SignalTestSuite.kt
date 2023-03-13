package com.pubnub.apikt.suite.pubsub

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.google.gson.Gson
import com.pubnub.apikt.endpoints.pubsub.Signal
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.PNPublishResult
import com.pubnub.apikt.suite.AUTH
import com.pubnub.apikt.suite.EndpointTestSuite
import com.pubnub.apikt.suite.PUB
import com.pubnub.apikt.suite.SUB
import org.junit.Assert.assertEquals
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class SignalTestSuite : EndpointTestSuite<Signal, PNPublishResult>() {

    override fun telemetryParamName() = "l_sig"

    override fun pnOperation() = PNOperationType.PNSignalOperation

    override fun requiredKeys() = SUB + PUB + AUTH

    override fun snippet(): Signal {
        return pubnub.signal(
            channel = "ch1",
            message = "ch2"
        )
    }

    override fun verifyResultExpectations(result: PNPublishResult) {
        assertEquals(15883272000000000L, result.timetoken)
    }

    override fun successfulResponseBody() = """[1,"Sent","15883272000000000"]"""

    override fun unsuccessfulResponseBodyList() = emptyList<String>()

    override fun mappingBuilder(): MappingBuilder {
        return get(
            urlPathEqualTo(
                "/signal/myPublishKey/mySubscribeKey/0/ch1/0/%s".format(
                    URLEncoder.encode(Gson().toJson("ch2"), StandardCharsets.UTF_8.name())
                )
            )
        )!!
    }

    override fun affectedChannelsAndGroups() = listOf("ch1") to emptyList<String>()
}
