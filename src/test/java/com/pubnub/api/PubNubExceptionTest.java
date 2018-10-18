package com.pubnub.api;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.pubnub.api.endpoints.TestHarness;
import com.pubnub.api.endpoints.pubsub.Publish;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;


public class PubNubExceptionTest extends TestHarness {
    @Rule
    public WireMockRule wireMockRule;
    private Publish instance;

    @Before
    public void beforeEach() throws IOException {
        PubNub pubnub = this.createPubNubInstance(8080);
        instance = pubnub.publish();
        wireMockRule = new WireMockRule(8080);
        wireMockRule.start();
    }

    @Test
    public void testPubnubError() {
        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/coolChannel/0/%22hi%22"))
                .willReturn(aResponse().withStatus(404).withBody("[1,\"Sent\",\"14598111595318003\"]")));

        /*stubFor(get(urlEqualTo("/publish/myPublishKey/mySubscribeKey/0/coolChannel/0/%22hi%22"))
                .withHeader("Accept", equalTo("text/xml"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "text/xml")
                        .withBody("[1,\"Sent\",\"14598111595318003\"]")));*/

        int statusCode = -1;

        try {
            instance.channel("coolChannel").message(new Object()).sync();
        } catch (PubNubException error) {
            statusCode = error.getStatusCode();
        }

        assertEquals(404, statusCode);
    }

}
