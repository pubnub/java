package com.pubnub.api.endpoints.channel_groups;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.pubnub.api.PubNubException;
import com.pubnub.api.endpoints.TestHarness;
import com.pubnub.api.models.consumer.channel_group.PNChannelGroupsDeleteGroupResult;
import org.junit.Before;
import org.junit.Rule;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertNotNull;

public class DeleteChannelGroupEndpointTest extends TestHarness {
    private DeleteChannelGroup partialDeleteChannelGroup;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    @Before
    public void beforeEach() throws IOException {
        partialDeleteChannelGroup = this.createPubNubInstance(8080).deleteChannelGroup();
    }

    @org.junit.Test
    public void testSyncSuccess() throws IOException, PubNubException, InterruptedException {
        stubFor(get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/groupA/remove"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {}, \"service\": \"ChannelGroups\"}")));

        PNChannelGroupsDeleteGroupResult response = partialDeleteChannelGroup.channelGroup("groupA").sync();
        assertNotNull(response);
    }

}
