package com.pubnub.api;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.pubnub.api.endpoints.TestHarness;
import com.pubnub.api.endpoints.pubsub.Publish;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertEquals;


public class PubNubExceptionTest extends TestHarness {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(this.PORT), false);

    private Publish instance;

    @Before
    public void beforeEach() throws IOException, PubNubException {
        PubNub pubnub = this.createPubNubInstance();
        instance = pubnub.publish();
        wireMockRule.start();
    }

    @After
    public void cleanup() {
        instance = null;
        wireMockRule.stop();
    }

    @Test
    public void testPubnubError() {
        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/coolChannel/0/%22hi%22"))
                .willReturn(aResponse().withStatus(404).withBody("[1,\"Sent\",\"14598111595318003\"]")));

        int statusCode = -1;

        try {
            instance.channel("coolChannel").message("hi").sync();
        } catch (PubNubException error) {
            statusCode = error.getStatusCode();
        }

        assertEquals(404, statusCode);
    }

}
