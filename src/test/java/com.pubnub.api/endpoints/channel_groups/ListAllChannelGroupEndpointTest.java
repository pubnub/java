package com.pubnub.api.endpoints.channel_groups;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.pubnub.api.PubNubException;
import com.pubnub.api.models.consumer.channel_group.PNChannelGroupsListAllResult;
import com.pubnub.api.endpoints.TestHarness;
import org.junit.Before;
import org.junit.Rule;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertThat;

public class ListAllChannelGroupEndpointTest extends TestHarness {
    private ListAllChannelGroup partialChannelGroup;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    @Before
    public void beforeEach() throws IOException {
        partialChannelGroup = this.createPubNubInstance(8080).listAllChannelGroups();
    }

    @org.junit.Test
    public void testSyncSuccess() throws IOException, PubNubException, InterruptedException {
        stubFor(get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {\"groups\": [\"a\",\"b\"]}, \"service\": \"ChannelGroups\"}")));

        PNChannelGroupsListAllResult response = partialChannelGroup.sync();
        assertThat(response.getGroups(), org.hamcrest.Matchers.contains("a", "b"));
    }
}
